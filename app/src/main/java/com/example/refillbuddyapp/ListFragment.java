package com.example.refillbuddyapp;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Fragment: Wasserstellen-Liste sortiert nach GPS-Entfernung
public class ListFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private WaterStationAdapter adapter;
    private List<WaterStationAdapter.WaterStation> waterStations;
    private MainActivity.FirebaseDataLoader dataLoader;
    private MainActivity.LocationHelper locationHelper;
    
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
        return inflater.inflate(R.layout.fragment_list, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // RecyclerView Setup (vertical Layout)
        recyclerView = view.findViewById(R.id.recycler_water_stations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Wasserstellen aus Firebase laden
        loadWaterStations();
    }
    
    // Wasserstellen von Firebase laden
    private void loadWaterStations() {
        dataLoader.loadWaterStations(new MainActivity.FirebaseDataLoader.DataLoadCallback() {
            @Override
            public void onDataLoaded(List<WaterStationAdapter.WaterStation> waterStations) {
                setupRecyclerView(waterStations); // RecyclerView mit Daten füllen
            }
            
            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "❌ Fehler beim Laden", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    // RecyclerView mit geladenen Wasserstellen füllen
    private void setupRecyclerView(List<WaterStationAdapter.WaterStation> waterStations) {
        this.waterStations = waterStations;
        
        // Nach GPS-Entfernung sortieren
        sortWaterStationsByDistance(waterStations);
    }
    

    
    // Fragment wird wieder sichtbar → Daten neu laden
    @Override
    public void onResume() {
        super.onResume();
        loadWaterStations(); // Aktuelle Wasserstellen laden
    }
    
    // Liste nach GPS-Entfernung sortieren (nächste zuerst)
    private void sortWaterStationsByDistance(List<WaterStationAdapter.WaterStation> stations) {
        locationHelper.getCurrentLocation(new MainActivity.LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                if (getContext() == null) return;
                
                // Nach Entfernung sortieren (Lambda-Expression)
                Collections.sort(stations, (s1, s2) -> {
                    double d1 = MainActivity.LocationHelper.calculateDistance(location.getLatitude(), location.getLongitude(), s1.getLat(), s1.getLng());
                    double d2 = MainActivity.LocationHelper.calculateDistance(location.getLatitude(), location.getLongitude(), s2.getLat(), s2.getLng());
                    return Double.compare(d1, d2); // Nächste zuerst
                });
                
                // Adapter erstellen und Entfernungen anzeigen
                adapter = new WaterStationAdapter(stations);
                recyclerView.setAdapter(adapter);
                adapter.updateDistances(location); // "1.2 km" anzeigen
            }
            
            @Override
            public void onLocationError(String error) {
                // Bei GPS-Fehler: Liste ohne Sortierung anzeigen
                adapter = new WaterStationAdapter(stations);
                recyclerView.setAdapter(adapter);
            }
        });
    }
    
    // Callback für MainActivity: Daten neu laden
    public void reloadData() {
        loadWaterStations();
    }
} 