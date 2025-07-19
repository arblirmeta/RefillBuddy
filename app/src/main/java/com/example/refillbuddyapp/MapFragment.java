package com.example.refillbuddyapp;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;

// Fragment: Google Maps mit Wasserstellen-Markern und GPS-Standort
public class MapFragment extends Fragment implements OnMapReadyCallback {
    
    private GoogleMap mMap;
    private MainActivity.FirebaseDataLoader dataLoader;
    private MainActivity.LocationHelper locationHelper;
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    
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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Google Maps Fragment finden und initialisieren
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // onMapReady wird aufgerufen wenn bereit
        }
    }
    
    // Callback: Google Maps ist bereit → Setup durchführen
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Kamera auf Berlin zentrieren (Fallback)
        LatLng berlin = new LatLng(52.5200, 13.4050);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(berlin, 12));
        
        // Zoom-Controls aktivieren
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        // GPS-Standort anzeigen (blauer Marker)
        showCurrentLocation();
        
        // Wasserstellen von Firebase laden
        loadWaterStations();
    }
    
    // Fragment wird wieder sichtbar → Daten neu laden
    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.clear(); // Alte Marker löschen
            showCurrentLocation(); // Standort neu anzeigen
            loadWaterStations(); // Wasserstellen neu laden
        }
    }
    
    // Wasserstellen aus Firebase laden und auf Karte anzeigen
    private void loadWaterStations() {
        dataLoader.loadWaterStations(new MainActivity.FirebaseDataLoader.DataLoadCallback() {
            @Override
            public void onDataLoaded(List<WaterStationAdapter.WaterStation> waterStations) {
                addWaterStationsToMap(waterStations); // Marker zur Karte hinzufügen
            }
            
            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "❌ Fehler beim Laden der Wasserstellen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    // Wasserstellen als rote Marker auf Karte anzeigen
    private void addWaterStationsToMap(List<WaterStationAdapter.WaterStation> waterStations) {
        for (WaterStationAdapter.WaterStation station : waterStations) {
            LatLng position = new LatLng(station.getLat(), station.getLng());
            // Marker erstellen: Titel + Beschreibung beim Antippen
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(station.getName())
                    .snippet(station.getDescription()));
        }
    }
    

    
    // GPS-Standort als blauer Marker auf Karte anzeigen
    private void showCurrentLocation() {
        // GPS-Berechtigung prüfen
        if (!locationHelper.hasLocationPermission()) {
            requestLocationPermission(); // User nach Berechtigung fragen
            return;
        }
        
        // GPS-Position abrufen
        locationHelper.getCurrentLocation(new MainActivity.LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                if (getContext() == null || mMap == null) return;
                
                // Blauen "Mein Standort" Marker hinzufügen
                LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(currentPosition)
                        .title("Mein Standort")
                        .snippet("Hier bin ich!")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                
                // Kamera auf Standort zentrieren (Zoom 14)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 14));
                
                Toast.makeText(getContext(), "📍 Standort gefunden!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onLocationError(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "GPS: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // location permission anfordern
    private void requestLocationPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }
    
    // result von permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // permission erhalten - standort anzeigen
                showCurrentLocation();
            } else {
                // permission verweigert
                if (getContext() != null) {
                    Toast.makeText(getContext(), "GPS-Berechtigung benötigt für Standort", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    // daten neu laden (wird von MainActivity aufgerufen)
    public void reloadData() {
        if (mMap != null) {
            mMap.clear(); // alte marker löschen
            showCurrentLocation(); // standort neu anzeigen  
            loadWaterStations(); // wasserstellen neu laden
        }
    }
} 