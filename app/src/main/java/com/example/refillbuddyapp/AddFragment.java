package com.example.refillbuddyapp;

import android.location.Address;
import android.location.Geocoder;
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

// fragment zum hinzufügen neuer wasserstellen
// einfach name und adresse eingeben
public class AddFragment extends Fragment {

    // ui elemente
    private EditText nameField, descriptionField, addressField;
    private Button addButton;
    private ProgressBar addProgress;
    
    // firebase data loader
    private FirebaseDataLoader dataLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // data loader initialisieren
        dataLoader = new FirebaseDataLoader();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // layout inflaten
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // alle ui elemente finden
        nameField = view.findViewById(R.id.nameField);
        descriptionField = view.findViewById(R.id.descriptionField);
        addressField = view.findViewById(R.id.addressField);
        addButton = view.findViewById(R.id.addButton);
        addProgress = view.findViewById(R.id.addProgress);

        // button click event
        addButton.setOnClickListener(v -> addWaterStation());
    }

    // neue wasserstelle hinzufügen
    private void addWaterStation() {
        // text aus allen feldern holen
        String name = nameField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String address = addressField.getText().toString().trim();

        // prüfen ob alle felder ausgefüllt sind
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(address)) {
            Toast.makeText(getContext(), "bitte alle felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        // progress bar anzeigen
        showProgress(true);

        // echte koordinaten aus adresse mit geocoder holen
        getCoordinatesFromAddress(address, new CoordinateCallback() {
            @Override
            public void onCoordinatesFound(double latitude, double longitude, boolean fromGeocoder) {
                // progress bar verstecken
                showProgress(false);
                
                // debug: koordinaten anzeigen
                String source = fromGeocoder ? "(Geocoder)" : "(Fallback)";
                Toast.makeText(getContext(), "📍 " + source + " " + String.format("%.4f, %.4f", latitude, longitude), Toast.LENGTH_SHORT).show();

                // zu firebase hinzufügen
                dataLoader.addWaterStation(name, address, latitude, longitude, new FirebaseDataLoader.DataLoadCallback() {
                    @Override
                    public void onDataLoaded(List<WaterStationAdapter.WaterStation> waterStations) {                        
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "✅ " + name + " bei " + address + " hinzugefügt!", Toast.LENGTH_LONG).show();
                            // felder leeren
                            clearFields();
                        }
                    }

                    @Override
                    public void onError(Exception e) {                        
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "❌ firebase fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    // progress bar anzeigen/verstecken
    private void showProgress(boolean show) {
        addProgress.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        addButton.setEnabled(!show); // button während loading deaktivieren
    }

    // alle eingabefelder leeren
    private void clearFields() {
        nameField.setText("");
        descriptionField.setText("");
        addressField.setText("");
    }
    
    // callback interface für koordinaten
    private interface CoordinateCallback {
        void onCoordinatesFound(double latitude, double longitude, boolean fromGeocoder);
    }
    
    // echte adresse zu koordinaten mit android geocoder (wie in android tutorials)
    private void getCoordinatesFromAddress(String addressStr, CoordinateCallback callback) {
        // geocoder in hintergrund thread starten (kann langsam sein)
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(getContext(), Locale.GERMAN);
                
                // prüfen ob geocoder verfügbar ist
                if (!Geocoder.isPresent()) {
                    // fallback wenn geocoder nicht verfügbar
                    getFallbackCoordinates(addressStr, callback);
                    return;
                }
                
                // adresse zu koordinaten konvertieren
                List<Address> addresses = geocoder.getFromLocationName(addressStr + ", Deutschland", 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    
                    // zurück zum ui thread
                    requireActivity().runOnUiThread(() -> {
                        callback.onCoordinatesFound(lat, lng, true);
                    });
                } else {
                    // keine ergebnisse gefunden - fallback
                    getFallbackCoordinates(addressStr, callback);
                }
                
            } catch (IOException e) {
                // fehler beim geocoding - fallback verwenden
                getFallbackCoordinates(addressStr, callback);
            }
        }).start();
    }
    
    // fallback methode wenn geocoder nicht funktioniert
    private void getFallbackCoordinates(String address, CoordinateCallback callback) {
        String addressLower = address.toLowerCase();
        double lat, lng;
        
        // bekannte berlin adressen mappen
        if (addressLower.contains("alexanderplatz")) {
            lat = 52.5200; lng = 13.4050;
        } else if (addressLower.contains("potsdamer platz") || addressLower.contains("potsdamer")) {
            lat = 52.5094; lng = 13.3759;
        } else if (addressLower.contains("tiergarten")) {
            lat = 52.5144; lng = 13.3501;
        } else if (addressLower.contains("hackescher markt") || addressLower.contains("hackescher")) {
            lat = 52.5225; lng = 13.4014;
        } else if (addressLower.contains("friedrichshain")) {
            lat = 52.5132; lng = 13.4553;
        } else if (addressLower.contains("brandenburger tor") || addressLower.contains("brandenburger")) {
            lat = 52.5163; lng = 13.3777;
        } else {
            // für andere adressen: konsistente koordinaten in berlin
            int hash = Math.abs(address.hashCode());
            lat = 52.4500 + (hash % 1000) / 10000.0; // zwischen 52.45 und 52.55
            lng = 13.2500 + (hash % 2000) / 10000.0; // zwischen 13.25 und 13.45
        }
        
        // zurück zum ui thread
        requireActivity().runOnUiThread(() -> {
            callback.onCoordinatesFound(lat, lng, false);
        });
    }
} 