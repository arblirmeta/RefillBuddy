package com.example.refillbuddyapp;

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
import java.util.List;

// fragment für die liste der wasserstellen
// recyclerview war am anfang verwirrend aber geht jetzt
public class ListFragment extends Fragment {
    
    // recyclerview und adapter
    private RecyclerView recyclerView;
    private WaterStationAdapter adapter;
    private List<WaterStationAdapter.WaterStation> waterStations;
    // für firebase daten
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
        // layout inflaten für die liste
        return inflater.inflate(R.layout.fragment_list, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // recyclerview finden und setup
        recyclerView = view.findViewById(R.id.recycler_water_stations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // vertical layout
        
        // daten laden
        loadWaterStations();
    }
    
    // wasserstellen aus firebase laden
    private void loadWaterStations() {
        dataLoader.loadWaterStations(new FirebaseDataLoader.DataLoadCallback() {
            @Override
            public void onDataLoaded(List<WaterStationAdapter.WaterStation> waterStations) {
                // recyclerview mit den daten setup
                setupRecyclerView(waterStations);
                if (getContext() != null) {
                    // debug: namen der wasserstellen anzeigen
                    StringBuilder stationNames = new StringBuilder();
                    for (int i = 0; i < Math.min(3, waterStations.size()); i++) {
                        if (i > 0) stationNames.append(", ");
                        stationNames.append(waterStations.get(i).getName());
                    }
                    Toast.makeText(getContext(), "📝 " + waterStations.size() + " stationen: " + stationNames.toString(), Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onError(Exception e) {
                // wenn firebase nicht geht, beispiel daten
                setupFallbackData();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "fallback: 3 beispiel stationen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    // recyclerview mit echten daten setup
    private void setupRecyclerView(List<WaterStationAdapter.WaterStation> waterStations) {
        this.waterStations = waterStations;
        
        // adapter erstellen und setzen
        adapter = new WaterStationAdapter(waterStations);
        recyclerView.setAdapter(adapter);
    }
    
    // fallback daten wenn firebase nicht funktioniert
    private void setupFallbackData() {
        waterStations = new ArrayList<>();
        
        // hardcoded beispiel daten mit echten adressen
        waterStations.add(new WaterStationAdapter.WaterStation(
            "Alexanderplatz Brunnen", 
            "Alexanderplatz 1, 10178 Berlin",
            52.5200, 13.4050
        ));
        
        waterStations.add(new WaterStationAdapter.WaterStation(
            "Potsdamer Platz Station", 
            "Potsdamer Platz 1, 10785 Berlin",
            52.5094, 13.3759
        ));
        
        waterStations.add(new WaterStationAdapter.WaterStation(
            "Tiergarten Wasserspender", 
            "Großer Tiergarten, 10557 Berlin",
            52.5144, 13.3501
        ));
        
        waterStations.add(new WaterStationAdapter.WaterStation(
            "Hackescher Markt Brunnen", 
            "Hackescher Markt 2, 10178 Berlin",
            52.5225, 13.4014
        ));
        
        waterStations.add(new WaterStationAdapter.WaterStation(
            "Friedrichshain Wasserstelle", 
            "Boxhagener Straße 15, 10245 Berlin",
            52.5132, 13.4553
        ));
        
        // adapter mit fallback daten
        adapter = new WaterStationAdapter(waterStations);
        recyclerView.setAdapter(adapter);
    }
    
    // fragment wird wieder sichtbar - daten neu laden
    @Override
    public void onResume() {
        super.onResume();
        // daten neu laden wenn fragment wieder sichtbar wird
        loadWaterStations();
    }
} 