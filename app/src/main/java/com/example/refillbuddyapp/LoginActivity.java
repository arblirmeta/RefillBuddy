package com.example.refillbuddyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.regex.Pattern;

// login activity für die app
public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth; // für firebase login
    private Button loginBtn;
    private TextView registerText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance(); // firebase setup

        // finde alle elemente
        emailInput = findViewById(R.id.emailField);
        passwordInput = findViewById(R.id.passwordField);
        loginProgress = findViewById(R.id.loginProgress);
        loginBtn = findViewById(R.id.loginButton);
        registerText = findViewById(R.id.registerLink);

        // button click events
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // gehe zu register
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // prüfe ob user schon eingeloggt ist
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // user ist eingeloggt, gehe zu main
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // login function
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        Log.d("LoginActivity", "Trying to login with email: " + email);

        // prüfe ob felder leer sind
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "bitte email und passwort eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        // einfache email prüfung
        if (!email.contains("@")) {
            Toast.makeText(this, "email format falsch", Toast.LENGTH_SHORT).show();
            return;
        }

        // progress bar anzeigen
        loginProgress.setVisibility(View.VISIBLE);
        
        // firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginProgress.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        // login erfolgreich
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("LoginActivity", "Login successful for user: " + user.getEmail());
                        Toast.makeText(this, "login erfolgreich!", Toast.LENGTH_SHORT).show();
                        
                        // gehe zu main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // login fehlgeschlagen
                        Log.e("LoginActivity", "Login failed: " + task.getException().getMessage());
                        Toast.makeText(this, "login fehler. versuche nochmal.", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
