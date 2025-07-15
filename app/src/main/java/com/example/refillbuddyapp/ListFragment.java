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
                    // nutzer informieren
                    Toast.makeText(getContext(), waterStations.size() + " wasserstellen geladen", Toast.LENGTH_SHORT).show();
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
        
        // hardcoded beispiel daten (wie am anfang)
        waterStations.add(new WaterStationAdapter.WaterStation(
            "Alexanderplatz Brunnen", 
            "Großer Trinkbrunnen am Alexanderplatz, immer verfügbar",
            52.5200, 13.4050
        ));
        
        waterStations.add(new WaterStationAdapter.WaterStation(
            "Potsdamer Platz Station", 
            "Wasserstation beim Sony Center, sehr sauber",
            52.5094, 13.3759
        ));
        
        waterStations.add(new WaterStationAdapter.WaterStation(
            "Tiergarten Wasserspender", 
            "Mitten im Park, perfekt für Spaziergänge",
            52.5144, 13.3501
        ));
        
        // adapter mit fallback daten
        adapter = new WaterStationAdapter(waterStations);
        recyclerView.setAdapter(adapter);
    }
} 