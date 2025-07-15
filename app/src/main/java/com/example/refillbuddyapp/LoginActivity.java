package com.example.refillbuddyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// login activity - erste activity die der user sieht
public class LoginActivity extends AppCompatActivity {

    // ui elemente
    private EditText emailInput, passwordInput;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth; // firebase authentication
    private Button loginBtn;
    private TextView registerText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // firebase auth initialisieren
        mAuth = FirebaseAuth.getInstance();

        // alle ui elemente finden
        emailInput = findViewById(R.id.emailField);
        passwordInput = findViewById(R.id.passwordField);
        loginProgress = findViewById(R.id.loginProgress);
        loginBtn = findViewById(R.id.loginButton);
        registerText = findViewById(R.id.registerLink);

        // button click events (lambda expressions sind cooler als anonyme klassen)
        loginBtn.setOnClickListener(v -> loginUser());
        registerText.setOnClickListener(v -> {
            // zu register activity wechseln
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // prüfen ob user schon eingeloggt ist
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // user ist schon eingeloggt, direkt zur main activity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    // login versuchen
    private void loginUser() {
        // text aus den feldern holen
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // prüfen ob felder leer sind
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "bitte email und passwort eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        // progress bar anzeigen
        showProgress(true);

        // firebase login versuchen
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // progress bar verstecken
                    showProgress(false);
                    
                    if (task.isSuccessful()) {
                        // login erfolgreich! zur main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // login fehlgeschlagen
                        Toast.makeText(LoginActivity.this, "login fehlgeschlagen", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // progress bar anzeigen/verstecken
    private void showProgress(boolean show) {
        loginProgress.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        loginBtn.setEnabled(!show); // button deaktivieren während loading
    }
}
