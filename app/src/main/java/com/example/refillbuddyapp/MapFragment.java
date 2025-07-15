package com.example.refillbuddyapp;

import android.os.Bundle;
import android.util.Log;
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

// map fragment für karte
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("MapFragment", "MapFragment started");

        // karte setup
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapFragment", "Map fragment ist null!");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        Log.d("MapFragment", "Map is ready!");

        // wasserstellen hinzufügen
        addWaterSpots();

        // kamera position - berlin mitte
        LatLng berlin = new LatLng(52.520008, 13.404954);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(berlin, 12));
        
        // zoom buttons einschalten
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        Toast.makeText(getContext(), "karte geladen! 3 wasserstellen", Toast.LENGTH_SHORT).show();
    }

    // wasserstellen zur karte hinzufügen
    private void addWaterSpots() {
        
        Log.d("MapFragment", "Adding water spots to map");
        
        // erste wasserstelle - alexanderplatz
        LatLng alex = new LatLng(52.520008, 13.404954);
        mMap.addMarker(new MarkerOptions()
                .position(alex)
                .title("Wasserstelle Alex")
                .snippet("kostenlos, immer offen"));

        // zweite wasserstelle - potsdamer platz
        LatLng potsdamer = new LatLng(52.5096, 13.3765);
        mMap.addMarker(new MarkerOptions()
                .position(potsdamer)
                .title("Wasserstelle Potsdamer Platz")
                .snippet("beim shopping center"));

        // dritte wasserstelle - tiergarten park
        LatLng tiergarten = new LatLng(52.5144, 13.3501);
        mMap.addMarker(new MarkerOptions()
                .position(tiergarten)
                .title("Wasserstelle Park")
                .snippet("im tiergarten"));
                
        // später: aus datenbank laden!
        // TODO: firestore integration
    }
} 