package com.example.refillbuddyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// fragment für user profil
// zeigt user info und logout möglichkeit
public class ProfileFragment extends Fragment {

    // ui elemente
    private TextView userEmailText, statsText;
    private Button logoutButton;
    
    // firebase auth
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // firebase auth initialisieren
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // layout inflaten
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ui elemente finden
        userEmailText = view.findViewById(R.id.userEmailText);
        statsText = view.findViewById(R.id.statsText);
        logoutButton = view.findViewById(R.id.logoutButton);

        // user info laden
        loadUserInfo();

        // logout button click event
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    // user informationen laden und anzeigen
    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // user email anzeigen
            String email = currentUser.getEmail();
            if (email != null) {
                userEmailText.setText(email);
            } else {
                userEmailText.setText("Keine E-Mail verfügbar");
            }
        }

        // statistiken laden
        loadStats();
    }

    // einfache statistiken
    private void loadStats() {
        // statistiken text zusammenstellen
        String stats = "📍 RefillBuddy aktiv\n" +
                      "💧 Wasserstellen finden leicht gemacht";
        
        statsText.setText(stats);
    }

    // user ausloggen
    private void logoutUser() {
        // firebase logout
        mAuth.signOut();
        
        // zurück zum login screen
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // fragment wird wieder sichtbar - daten neu laden
    @Override
    public void onResume() {
        super.onResume();
        // user info neu laden
        loadUserInfo();
    }
} 