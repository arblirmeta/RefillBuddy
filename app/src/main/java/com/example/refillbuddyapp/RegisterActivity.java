package com.example.refillbuddyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// register activity für neue accounts
public class RegisterActivity extends AppCompatActivity {

    // ui elemente für registrierung
    private EditText emailInput, passwordInput, confirmPasswordInput;
    private ProgressBar registerProgress;
    private FirebaseAuth mAuth; // firebase authentication
    private Button registerBtn;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // firebase auth initialisieren
        mAuth = FirebaseAuth.getInstance();

        // alle ui elemente finden
        emailInput = findViewById(R.id.emailField);
        passwordInput = findViewById(R.id.passwordField);
        confirmPasswordInput = findViewById(R.id.confirmPasswordField);
        registerProgress = findViewById(R.id.registerProgress);
        registerBtn = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginLink);

        // button events
        registerBtn.setOnClickListener(v -> registerUser());
        loginText.setOnClickListener(v -> {
            // zurück zum login
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    // neuen user registrieren
    private void registerUser() {
        // text aus den feldern holen
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // prüfen ob alle felder ausgefüllt sind
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "bitte alle felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        // passwörter müssen gleich sein
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "passwörter stimmen nicht überein", Toast.LENGTH_SHORT).show();
            return;
        }

        // passwort muss mindestens 6 zeichen haben (firebase regel)
        if (password.length() < 6) {
            Toast.makeText(this, "passwort muss mindestens 6 zeichen haben", Toast.LENGTH_SHORT).show();
            return;
        }

        // progress bar anzeigen
        showProgress(true);

        // firebase registrierung versuchen
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // progress bar verstecken
                    showProgress(false);
                    
                    if (task.isSuccessful()) {
                        // registrierung erfolgreich!
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "konto erstellt!", Toast.LENGTH_SHORT).show();
                        
                        // direkt zur main activity
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // registrierung fehlgeschlagen
                        Toast.makeText(RegisterActivity.this, "registrierung fehlgeschlagen", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // progress bar anzeigen/verstecken
    private void showProgress(boolean show) {
        registerProgress.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        registerBtn.setEnabled(!show); // button deaktivieren während loading
    }
}
