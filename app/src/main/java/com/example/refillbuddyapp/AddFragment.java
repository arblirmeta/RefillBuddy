package com.example.refillbuddyapp;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

// Fragment: Neue Wasserstelle hinzufügen (mit GPS-Unterstützung)
public class AddFragment extends Fragment {
    private EditText nameField, descriptionField, addressField;
    private Button addButton, gpsButton;
    private ProgressBar addProgress;
    private MainActivity.FirebaseDataLoader dataLoader;
    private MainActivity.LocationHelper locationHelper;
    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Helper-Klassen initialisieren
        dataLoader = new MainActivity.FirebaseDataLoader();
        locationHelper = new MainActivity.LocationHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // UI-Elemente finden
        nameField = view.findViewById(R.id.nameField);
        descriptionField = view.findViewById(R.id.descriptionField);
        addressField = view.findViewById(R.id.addressField);
        addButton = view.findViewById(R.id.addButton);
        gpsButton = view.findViewById(R.id.gpsButton);
        addProgress = view.findViewById(R.id.addProgress);

        // Button-Events setzen
        addButton.setOnClickListener(v -> addWaterStation());
        gpsButton.setOnClickListener(v -> useCurrentLocation());
    }

    // Neue Wasserstelle hinzufügen (Adresse → GPS → Firebase)
    private void addWaterStation() {
        String name = nameField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String address = addressField.getText().toString().trim();

        // Input-Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(address)) {
            Toast.makeText(getContext(), "Alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        // Schritt 1: Adresse zu GPS-Koordinaten konvertieren
        getCoordinatesFromAddress(address, (latitude, longitude, fromGeocoder) -> {
            showProgress(false);
            // Schritt 2: Station zu Firebase hinzufügen
            dataLoader.addWaterStation(name, address, latitude, longitude, new MainActivity.FirebaseDataLoader.DataLoadCallback() {
                @Override
                public void onDataLoaded(List<WaterStationAdapter.WaterStation> stations) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "✅ " + name + " hinzugefügt!", Toast.LENGTH_SHORT).show();
                        clearFields();
                        // MainActivity benachrichtigen → andere Fragments updaten
                        if (mainActivity != null) mainActivity.onWaterStationAdded();
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "❌ Fehler beim Speichern", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    // progress bar anzeigen/verstecken
    // Progress-Spinner an/aus (während Geocoding oder Firebase-Upload)
    private void showProgress(boolean show) {
        addProgress.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        addButton.setEnabled(!show); // Button während Laden deaktivieren
    }

    // Eingabefelder nach erfolgreichem Hinzufügen leeren
    private void clearFields() {
        nameField.setText("");
        descriptionField.setText("");
        addressField.setText("");
    }
    
    // MainActivity-Referenz setzen (für Update-Callbacks)
    public void setMainActivity(MainActivity activity) {
        this.mainActivity = activity;
    }
    
    // Callback für Geocoding-Ergebnisse
    private interface CoordinateCallback {
        void onCoordinatesFound(double latitude, double longitude, boolean fromGeocoder);
    }
    
    // Adresse zu GPS-Koordinaten konvertieren (Android Geocoder)
    private void getCoordinatesFromAddress(String addressStr, CoordinateCallback callback) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(getContext(), Locale.GERMAN);
                
                if (!Geocoder.isPresent()) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "❌ Geocoder nicht verfügbar", Toast.LENGTH_SHORT).show());
                    return;
                }
                
                // Adresse suchen (+ ", Deutschland" für bessere Ergebnisse)
                List<Address> addresses = geocoder.getFromLocationName(addressStr + ", Deutschland", 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    requireActivity().runOnUiThread(() -> 
                        callback.onCoordinatesFound(address.getLatitude(), address.getLongitude(), true));
                } else {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "❌ Adresse nicht gefunden", Toast.LENGTH_SHORT).show());
                }
                
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "❌ Netzwerk-Fehler", Toast.LENGTH_SHORT).show());
            }
        }).start(); // Background-Thread für Netzwerk-Request
    }
    
    // GPS-Button: Aktuellen Standort als Adresse verwenden
    private void useCurrentLocation() {
        if (!locationHelper.hasLocationPermission()) {
            Toast.makeText(getContext(), "GPS-Berechtigung benötigt", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        gpsButton.setEnabled(false);
        gpsButton.setText("📍 Standort wird abgerufen...");
        
        // GPS-Position abrufen
        locationHelper.getCurrentLocation(new MainActivity.LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                if (getContext() == null) return;
                showProgress(false);
                resetGpsButton();
                // GPS → Adresse konvertieren (Reverse Geocoding)
                reverseGeocodeLocation(location);
            }
            
            @Override
            public void onLocationError(String error) {
                if (getContext() == null) return;
                showProgress(false);
                resetGpsButton();
                Toast.makeText(getContext(), "GPS-Fehler", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // GPS-Button in Normal-Zustand zurücksetzen
    private void resetGpsButton() {
        gpsButton.setEnabled(true);
        gpsButton.setText("📍 Meinen Standort verwenden");
    }
    
    // GPS-Koordinaten zu Adresse konvertieren (Reverse Geocoding)
    private void reverseGeocodeLocation(Location location) {
        new Thread(() -> {
            // Fallback: GPS-Koordinaten als Text
            String addressText = "GPS: " + String.format("%.6f, %.6f", location.getLatitude(), location.getLongitude());
            
            try {
                Geocoder geocoder = new Geocoder(getContext(), Locale.GERMAN);
                if (Geocoder.isPresent()) {
                    // Koordinaten zu Adresse konvertieren
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        String street = "";
                        // Adresse zusammenbauen: Straße + Hausnummer + Stadt
                        if (addr.getThoroughfare() != null) street += addr.getThoroughfare();
                        if (addr.getSubThoroughfare() != null) street += " " + addr.getSubThoroughfare();
                        if (addr.getLocality() != null) street += (street.isEmpty() ? "" : ", ") + addr.getLocality();
                        if (!street.isEmpty()) addressText = street;
                    }
                }
            } catch (IOException ignored) {} // Bei Fehler Fallback verwenden
            
            String finalText = addressText;
            requireActivity().runOnUiThread(() -> {
                addressField.setText(finalText); // Adresse in Eingabefeld einfügen
                Toast.makeText(getContext(), "📍 Standort gesetzt", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}