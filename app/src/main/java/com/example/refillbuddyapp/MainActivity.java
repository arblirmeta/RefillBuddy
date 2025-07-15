package com.example.refillbuddyapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

    // main activity
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigation;
    private TextView toolbarTitle;
    // TODO: add more features later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); // firebase

        // toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        // elemente holen
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // bottom navigation click
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                showHomeScreen();
                return true;
            } else if (itemId == R.id.nav_list) {
                showListScreen();
                return true;
            } else if (itemId == R.id.nav_favorites) {
                showFavoritesScreen();
                return true;
            } else if (itemId == R.id.nav_add) {
                showAddScreen();
                return true;
            } else if (itemId == R.id.nav_profile) {
                showProfileScreen();
                return true;
            }
            return false;
        });

        // direkt karte laden
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        showHomeScreen();
    }

    // home screen zeigen
    private void showHomeScreen() {
        toolbarTitle.setText("RefillBuddy - Karte");
        
        // map fragment laden
        MapFragment mapFragment = new MapFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, mapFragment)
                .commit();
                
        Log.d("MainActivity", "Map fragment loaded");
    }

    // list screen zeigen
    private void showListScreen() {
        toolbarTitle.setText("RefillBuddy - Liste");
        
        Toast.makeText(this, "liste kommt später...", Toast.LENGTH_SHORT).show();
        // TODO: ListFragment machen
    }

    // favorites screen zeigen
    private void showFavoritesScreen() {
        toolbarTitle.setText("RefillBuddy - Favoriten");
        
        Toast.makeText(this, "favoriten noch nicht fertig", Toast.LENGTH_SHORT).show();
        // TODO: FavoritesFragment machen
    }

    // add screen zeigen
    private void showAddScreen() {
        toolbarTitle.setText("RefillBuddy - Hinzufügen");
        
        Toast.makeText(this, "hinzufügen später implementieren", Toast.LENGTH_SHORT).show();
        // TODO: AddFragment machen
    }

    // profile screen zeigen
    private void showProfileScreen() {
        toolbarTitle.setText("RefillBuddy - Profil");
        
        Toast.makeText(this, "profil noch nicht da", Toast.LENGTH_SHORT).show();
        // TODO: ProfileFragment machen
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menü inflaten
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // logout
            mAuth.signOut();
            Toast.makeText(this, "abgemeldet", Toast.LENGTH_SHORT).show();
            // zurück zu login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}