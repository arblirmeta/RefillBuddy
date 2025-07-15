package com.example.refillbuddyapp;

import android.content.Intent;
import android.os.Bundle;
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
    private Button exploreBtn;
    // TODO: add more features later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); // firebase

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        // get elements
        bottomNavigation = findViewById(R.id.bottomNavigation);
        exploreBtn = findViewById(R.id.exploreButton);

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

        // explore button click
        exploreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show home screen
                bottomNavigation.setSelectedItemId(R.id.nav_home);
                showHomeScreen();
            }
        });
    }

    // show home screen
    private void showHomeScreen() {
        toolbarTitle.setText("RefillBuddy - Karte");
        Toast.makeText(this, "karte wird geladen...", Toast.LENGTH_SHORT).show();
        // TODO: fragment laden
    }

    // show list screen
    private void showListScreen() {
        toolbarTitle.setText("RefillBuddy - Liste");
        Toast.makeText(this, "liste wird geladen...", Toast.LENGTH_SHORT).show();
        // TODO: fragment laden
    }

    // show favorites screen
    private void showFavoritesScreen() {
        toolbarTitle.setText("RefillBuddy - Favoriten");
        Toast.makeText(this, "favoriten werden geladen...", Toast.LENGTH_SHORT).show();
        // TODO: fragment laden
    }

    // show add screen
    private void showAddScreen() {
        toolbarTitle.setText("RefillBuddy - Hinzufügen");
        Toast.makeText(this, "hinzufügen screen...", Toast.LENGTH_SHORT).show();
        // TODO: fragment laden
    }

    // show profile screen
    private void showProfileScreen() {
        toolbarTitle.setText("RefillBuddy - Profil");
        Toast.makeText(this, "profil wird geladen...", Toast.LENGTH_SHORT).show();
        // TODO: fragment laden
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu inflaten
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // logout
            mAuth.signOut();
            Toast.makeText(this, "abgemeldet", Toast.LENGTH_SHORT).show();
            // back to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}