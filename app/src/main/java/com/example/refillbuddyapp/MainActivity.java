package com.example.refillbuddyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

// das ist die hauptaktivität meiner app
public class MainActivity extends AppCompatActivity {

    // firebase für login/logout
    private FirebaseAuth mAuth;
    // bottom navigation wie in der vorlesung
    private BottomNavigationView bottomNavigation;
    // toolbar titel ändern
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // firebase initialisieren
        mAuth = FirebaseAuth.getInstance();

        // toolbar aufsetzen (toolbar ist besser als actionbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        // bottom navigation finden
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // hier passiert die navigation - lambda expressions sind cool
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            // je nach tab verschiedene screens zeigen
            if (itemId == R.id.nav_home) {
                showHomeScreen();
                return true;
            } else if (itemId == R.id.nav_list) {
                showListScreen();
                return true;
            } else if (itemId == R.id.nav_favorites) {
                showFavoritesScreen(); // noch nicht implementiert
                return true;
            } else if (itemId == R.id.nav_add) {
                showAddScreen(); // TODO: später machen
                return true;
            } else if (itemId == R.id.nav_profile) {
                showProfileScreen(); // auch noch nicht fertig
                return true;
            }
            return false;
        });

        // direkt die karte laden beim start
        showHomeScreen();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    // karte anzeigen (home screen)
    private void showHomeScreen() {
        toolbarTitle.setText("RefillBuddy");
        
        // fragment erstellen und laden
        MapFragment mapFragment = new MapFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, mapFragment)
                .commit();
    }

    // liste anzeigen
    private void showListScreen() {
        toolbarTitle.setText("- Liste");
        
        // list fragment laden
        ListFragment listFragment = new ListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, listFragment)
                .commit();
    }

    // favoriten screen (noch nicht implementiert)
    private void showFavoritesScreen() {
        toolbarTitle.setText("- Favoriten");
        // placeholder toast
        Toast.makeText(this, "favoriten kommt später...", Toast.LENGTH_SHORT).show();
    }

    // hinzufügen screen (auch noch nicht fertig)
    private void showAddScreen() {
        toolbarTitle.setText("- Hinzufügen");
        // placeholder toast
        Toast.makeText(this, "hinzufügen kommt später...", Toast.LENGTH_SHORT).show();
    }

    // profil screen (kommt auch noch)
    private void showProfileScreen() {
        toolbarTitle.setText("- Profil");
        // placeholder toast
        Toast.makeText(this, "profil kommt später...", Toast.LENGTH_SHORT).show();
    }

    // menü erstellen (mit logout button)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // menü klicks behandeln
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // user ausloggen
            mAuth.signOut();
            // zurück zum login und alle anderen activities schließen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}