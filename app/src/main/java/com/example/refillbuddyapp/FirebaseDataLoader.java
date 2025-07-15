package com.example.refillbuddyapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

// diese klasse lädt die daten aus firebase
// hab ich nach der vorlesung über firebase gemacht
public class FirebaseDataLoader {
    
    // firestore datenbank instanz
    private FirebaseFirestore db;
    
    // konstruktor
    public FirebaseDataLoader() {
        // firebase datenbank bekommen
        db = FirebaseFirestore.getInstance();
    }
    
    // callback interface für asynchrone calls (wie in der vorlesung erklärt)
    public interface DataLoadCallback {
        void onDataLoaded(List<WaterStationAdapter.WaterStation> waterStations);
        void onError(Exception e);
    }
    
    // wasserstellen aus firebase laden
    public void loadWaterStations(DataLoadCallback callback) {
        // aus der "trinkbrunnen" collection laden
        db.collection("trinkbrunnen")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // liste für die wasserstellen
                        List<WaterStationAdapter.WaterStation> waterStations = new ArrayList<>();
                        
                        // durch alle dokumente gehen
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // daten aus dem dokument holen
                                String name = document.getString("name");
                                String address = document.getString("address");
                                Double latitude = document.getDouble("latitude");
                                Double longitude = document.getDouble("longitude");
                                
                                // prüfen ob die wichtigen daten da sind
                                if (name != null && latitude != null && longitude != null) {
                                    // wenn keine adresse da ist, placeholder setzen
                                    if (address == null || address.isEmpty()) {
                                        address = "Kein Adresse angegeben";
                                    }
                                    
                                    // neue wasserstation erstellen
                                    WaterStationAdapter.WaterStation station = new WaterStationAdapter.WaterStation(
                                            name, address, latitude, longitude
                                    );
                                    waterStations.add(station);
                                }
                            } catch (Exception e) {
                                // wenn fehler beim parsen, einfach überspringen
                                continue;
                            }
                        }
                        
                        // callback aufrufen mit den geladenen daten
                        callback.onDataLoaded(waterStations);
                    } else {
                        // fehler beim laden
                        callback.onError(task.getException());
                    }
                });
    }
    
    // neue wasserstation hinzufügen (für später)
    public void addWaterStation(String name, String address, double latitude, double longitude, DataLoadCallback callback) {
        // neue station erstellen
        WaterStationAdapter.WaterStation station = new WaterStationAdapter.WaterStation(name, address, latitude, longitude);
        
        // zu firebase hinzufügen
        db.collection("trinkbrunnen")
                .add(station)
                .addOnSuccessListener(documentReference -> {
                    // wenn erfolgreich, callback aufrufen
                    List<WaterStationAdapter.WaterStation> singleStation = new ArrayList<>();
                    singleStation.add(station);
                    callback.onDataLoaded(singleStation);
                })
                .addOnFailureListener(callback::onError); // bei fehler error callback
    }
} 