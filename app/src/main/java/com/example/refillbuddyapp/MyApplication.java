package com.example.refillbuddyapp;

import android.app.Application;
import com.google.firebase.FirebaseApp;

// main application class
public class MyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // firebase setup
        FirebaseApp.initializeApp(this);
    }
} 