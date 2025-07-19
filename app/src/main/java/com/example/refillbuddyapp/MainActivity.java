package com.example.refillbuddyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

// hauptaktivität mit navigation
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigation;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                showHomeScreen();
                return true;
            } else if (itemId == R.id.nav_list) {
                showListScreen();
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

        showHomeScreen();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void showHomeScreen() {
        toolbarTitle.setText("RefillBuddy");
        MapFragment mapFragment = new MapFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, mapFragment)
                .commit();
    }

    private void showListScreen() {
        toolbarTitle.setText("- Liste");
        ListFragment listFragment = new ListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, listFragment)
                .commit();
    }

    private void showAddScreen() {
        toolbarTitle.setText("- Hinzufügen");
        AddFragment addFragment = new AddFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, addFragment)
                .commit();
    }

    private void showProfileScreen() {
        toolbarTitle.setText("- Profil");
        ProfileFragment profileFragment = new ProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, profileFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}