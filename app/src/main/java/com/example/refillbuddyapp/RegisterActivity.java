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
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// register activity
public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, confirmPasswordInput;
    private ProgressBar registerProgress;
    private FirebaseAuth mAuth; // firebase auth
    private Button registerBtn;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance(); // firebase setup

        // ui elemente holen
        emailInput = findViewById(R.id.emailField);
        passwordInput = findViewById(R.id.passwordField);
        confirmPasswordInput = findViewById(R.id.confirmPasswordField);
        registerProgress = findViewById(R.id.registerProgress);
        registerBtn = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginLink);

        // button clicks
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // zurück zu login
                finish();
            }
        });
    }

    // register function
    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        Log.d("RegisterActivity", "Registering user with email: " + email);

        // prüfe leere felder
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "alle felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        // prüfe email format
        if (!email.contains("@")) {
            Toast.makeText(this, "email format ist falsch", Toast.LENGTH_SHORT).show();
            return;
        }

        // prüfe passwort übereinstimmung
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "passwörter sind nicht gleich", Toast.LENGTH_SHORT).show();
            return;
        }

        // prüfe passwort länge
        if (password.length() < 6) {
            Toast.makeText(this, "passwort zu kurz (mindestens 6 zeichen)", Toast.LENGTH_SHORT).show();
            return;
        }

        // progress anzeigen
        registerProgress.setVisibility(View.VISIBLE);
        
        // firebase register
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    registerProgress.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        // registrierung erfolgreich
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("RegisterActivity", "Registration successful!");
                        Toast.makeText(this, "account erstellt!", Toast.LENGTH_SHORT).show();
                        
                        // gehe zu main activity
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // registrierung fehlgeschlagen
                        Log.e("RegisterActivity", "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(this, "registrierung fehlgeschlagen. nochmal versuchen.", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
