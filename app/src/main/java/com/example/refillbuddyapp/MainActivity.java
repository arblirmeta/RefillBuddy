package com.example.refillbuddyapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Haupt-Activity: Navigation zwischen Fragments + Helper-Klassen
public class MainActivity extends AppCompatActivity {
    
    // GPS-Helper: Standort abrufen und Distanz berechnen
    public static class LocationHelper {
        private FusedLocationProviderClient fusedLocationClient;
        private Context context;
        
        // Interface für GPS-Callbacks
        public interface LocationCallback {
            void onLocationReceived(Location location);
            void onLocationError(String error);
        }
        
        public LocationHelper(Context context) {
            this.context = context;
            this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }
        
        // Aktuellen GPS-Standort abrufen
        public void getCurrentLocation(LocationCallback callback) {
            // Prüfen ob GPS-Berechtigung da ist
            if (!hasLocationPermission()) {
                callback.onLocationError("Keine GPS-Berechtigung");
                return;
            }
            
            // GPS-Position von Google Play Services holen
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) callback.onLocationReceived(location);
                        else callback.onLocationError("Standort nicht verfügbar");
                    })
                    .addOnFailureListener(e -> callback.onLocationError("GPS-Fehler"));
        }
        
        // GPS-Berechtigung prüfen
        public boolean hasLocationPermission() {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        
        // Entfernung zwischen zwei GPS-Punkten (Haversine-Formel)
        public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            final int R = 6371; // Erdradius in km
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) 
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
            return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        }
        
        // Distanz formatieren: "1.2 km" oder "450 m"
        public static String formatDistance(double distanceKm) {
            return distanceKm < 1.0 ? (int)(distanceKm * 1000) + " m" : String.format("%.1f km", distanceKm);
        }
    }
    
    // Firebase-Helper: Wasserstellen aus Cloud-DB laden/speichern
    public static class FirebaseDataLoader {
        private FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Interface für Firebase-Callbacks
        public interface DataLoadCallback {
            void onDataLoaded(List<WaterStationAdapter.WaterStation> waterStations);
            void onError(Exception e);
        }
        
        // Alle Wasserstellen aus Firebase laden
        public void loadWaterStations(DataLoadCallback callback) {
            db.collection("trinkbrunnen").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<WaterStationAdapter.WaterStation> stations = new ArrayList<>();
                    // Jedes Dokument durchgehen und WaterStation erstellen
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        try {
                            String name = doc.getString("name");
                            String address = doc.getString("address");
                            Double lat = doc.getDouble("latitude");
                            Double lng = doc.getDouble("longitude");
                            
                            if (name != null && lat != null && lng != null) {
                                if (address == null) address = "Keine Adresse";
                                stations.add(new WaterStationAdapter.WaterStation(name, address, lat, lng));
                            }
                        } catch (Exception ignored) {} // Fehlerhafte Dokumente überspringen
                    }
                    callback.onDataLoaded(stations);
                } else {
                    callback.onError(task.getException());
                }
            });
        }
        
        // Neue Wasserstation zu Firebase hinzufügen
        public void addWaterStation(String name, String address, double lat, double lng, DataLoadCallback callback) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("address", address);
            data.put("latitude", lat);
            data.put("longitude", lng);
            
            // Zu "trinkbrunnen" Collection hinzufügen
            db.collection("trinkbrunnen").add(data)
                    .addOnSuccessListener(ref -> {
                        List<WaterStationAdapter.WaterStation> result = new ArrayList<>();
                        result.add(new WaterStationAdapter.WaterStation(name, address, lat, lng));
                        callback.onDataLoaded(result);
                    })
                    .addOnFailureListener(callback::onError);
        }
    }

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigation;
    private TextView toolbarTitle;
    
    // Referenzen zu aktiven Fragments (für Updates)
    private MapFragment currentMapFragment;
    private ListFragment currentListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Toolbar und Navigation Setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Navigation Item Click Handler
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                showHomeScreen();  // Karte anzeigen
                return true;
            } else if (itemId == R.id.nav_list) {
                showListScreen();  // Liste anzeigen
                return true;
            } else if (itemId == R.id.nav_add) {
                showAddScreen();   // Hinzufügen-Screen
                return true;
            } else if (itemId == R.id.nav_profile) {
                showProfileScreen(); // Profil anzeigen
                return true;
            }
            return false;
        });

        // App startet mit der Karte
        showHomeScreen();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    // Karte anzeigen (Home Screen)
    private void showHomeScreen() {
        toolbarTitle.setText("RefillBuddy");
        currentMapFragment = new MapFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, currentMapFragment)
                .commit();
    }

    // Wasserstellen-Liste anzeigen (nach Entfernung sortiert)
    private void showListScreen() {
        toolbarTitle.setText("- Liste");
        currentListFragment = new ListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, currentListFragment)
                .commit();
    }

    // Neue Wasserstelle hinzufügen
    private void showAddScreen() {
        toolbarTitle.setText("- Hinzufügen");
        AddFragment addFragment = new AddFragment();
        // MainActivity-Referenz für Updates übergeben
        addFragment.setMainActivity(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, addFragment)
                .commit();
    }

    // User-Profil anzeigen
    private void showProfileScreen() {
        toolbarTitle.setText("- Profil");
        ProfileFragment profileFragment = new ProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, profileFragment)
                .commit();
    }

    // Toolbar Menu erstellen (Logout Button)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Menu Item Clicks behandeln
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // User abmelden und zum Login zurück
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // Callback: Neue Wasserstelle wurde hinzugefügt
    public void onWaterStationAdded() {
        // Karte neu laden falls aktiv
        if (currentMapFragment != null) {
            currentMapFragment.reloadData();
        }
        
        // Liste neu laden falls aktiv  
        if (currentListFragment != null) {
            currentListFragment.reloadData();
        }
    }
}