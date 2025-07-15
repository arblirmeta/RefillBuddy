package com.example.refillbuddyapp;

import android.app.Application;
import com.google.firebase.FirebaseApp;

// application class
public class RefillBuddyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // firebase setup
        FirebaseApp.initializeApp(this);
    }
} 