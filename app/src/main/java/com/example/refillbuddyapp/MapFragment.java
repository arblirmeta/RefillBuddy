package com.example.refillbuddyapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;

// fragment für die karte
public class MapFragment extends Fragment implements OnMapReadyCallback {
    
    private GoogleMap mMap;
    private FirebaseDataLoader dataLoader;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataLoader = new FirebaseDataLoader();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    // wird aufgerufen wenn die karte bereit ist
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // kamera auf berlin setzen
        LatLng berlin = new LatLng(52.5200, 13.4050);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(berlin, 10));
        
        // zoom controls aktivieren
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        // wasserstellen laden
        loadWaterStations();
    }
    
    // fragment wird wieder sichtbar - daten neu laden
    @Override
    public void onResume() {
        super.onResume();
        // wenn karte bereit ist, daten neu laden
        if (mMap != null) {
            mMap.clear(); // alte marker löschen
            loadWaterStations(); // neue daten laden
        }
    }
    
    // wasserstellen aus firebase laden
    private void loadWaterStations() {
        dataLoader.loadWaterStations(new FirebaseDataLoader.DataLoadCallback() {
            @Override
            public void onDataLoaded(List<WaterStationAdapter.WaterStation> waterStations) {
                // alle wasserstellen als marker zur karte hinzufügen
                addWaterStationsToMap(waterStations);
                if (getContext() != null) {
                    // debug: namen der wasserstellen anzeigen
                    StringBuilder stationNames = new StringBuilder();
                    for (int i = 0; i < Math.min(3, waterStations.size()); i++) {
                        if (i > 0) stationNames.append(", ");
                        stationNames.append(waterStations.get(i).getName());
                    }
                    Toast.makeText(getContext(), "🗺️ " + waterStations.size() + " stationen: " + stationNames.toString(), Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onError(Exception e) {
                // wenn firebase nicht geht, beispiel daten zeigen
                addFallbackWaterStations();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "fallback: 3 beispiel stationen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    // wasserstellen als marker zur karte hinzufügen
    private void addWaterStationsToMap(List<WaterStationAdapter.WaterStation> waterStations) {
        for (WaterStationAdapter.WaterStation station : waterStations) {
            // position aus lat/lng erstellen
            LatLng position = new LatLng(station.getLat(), station.getLng());
            // marker hinzufügen
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(station.getName())
                    .snippet(station.getDescription()));
        }
    }
    
    // fallback wenn firebase nicht funktioniert
    private void addFallbackWaterStations() {
        // alexanderplatz
        LatLng alex = new LatLng(52.520008, 13.404954);
        mMap.addMarker(new MarkerOptions()
                .position(alex)
                .title("Alexanderplatz Brunnen")
                .snippet("Alexanderplatz 1, 10178 Berlin"));

        // potsdamer platz
        LatLng potsdamer = new LatLng(52.5096, 13.3765);
        mMap.addMarker(new MarkerOptions()
                .position(potsdamer)
                .title("Potsdamer Platz Station")
                .snippet("Potsdamer Platz 1, 10785 Berlin"));

        // tiergarten
        LatLng tiergarten = new LatLng(52.5144, 13.3501);
        mMap.addMarker(new MarkerOptions()
                .position(tiergarten)
                .title("Tiergarten Wasserspender")
                .snippet("Großer Tiergarten, 10557 Berlin"));
        
        // hackescher markt
        LatLng hackescher = new LatLng(52.5225, 13.4014);
        mMap.addMarker(new MarkerOptions()
                .position(hackescher)
                .title("Hackescher Markt Brunnen")
                .snippet("Hackescher Markt 2, 10178 Berlin"));
        
        // friedrichshain
        LatLng friedrichshain = new LatLng(52.5132, 13.4553);
        mMap.addMarker(new MarkerOptions()
                .position(friedrichshain)
                .title("Friedrichshain Wasserstelle")
                .snippet("Boxhagener Straße 15, 10245 Berlin"));
    }
} 