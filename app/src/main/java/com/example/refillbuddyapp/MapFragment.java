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

// das ist das fragment für die karte
// google maps war ziemlich schwer zu implementieren
public class MapFragment extends Fragment implements OnMapReadyCallback {
    
    // google map objekt
    private GoogleMap mMap;
    // für daten aus firebase
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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // map fragment finden und async laden
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // callback wird aufgerufen wenn karte fertig ist
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
    
    // wasserstellen aus firebase laden
    private void loadWaterStations() {
        dataLoader.loadWaterStations(new FirebaseDataLoader.DataLoadCallback() {
            @Override
            public void onDataLoaded(List<WaterStationAdapter.WaterStation> waterStations) {
                // alle wasserstellen als marker zur karte hinzufügen
                addWaterStationsToMap(waterStations);
                if (getContext() != null) {
                    // nutzer informieren wieviele stationen geladen wurden
                    Toast.makeText(getContext(), waterStations.size() + " wasserstellen geladen", Toast.LENGTH_SHORT).show();
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
                .title("Wasserstelle Alex")
                .snippet("kostenlos, immer offen"));

        // potsdamer platz
        LatLng potsdamer = new LatLng(52.5096, 13.3765);
        mMap.addMarker(new MarkerOptions()
                .position(potsdamer)
                .title("Wasserstelle Potsdamer Platz")
                .snippet("beim shopping center"));

        // tiergarten
        LatLng tiergarten = new LatLng(52.5144, 13.3501);
        mMap.addMarker(new MarkerOptions()
                .position(tiergarten)
                .title("Wasserstelle Park")
                .snippet("im tiergarten"));
    }
} 