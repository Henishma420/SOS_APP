package com.eles.traffic_pro_java;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;


public class GestureM extends AppCompatActivity {

    private GestureLibrary gestureLibrary;
    private GestureOverlayView gestureOverlayView;
    private Gesture currentGesture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesturem);

        gestureOverlayView = findViewById(R.id.gestureOverlay_M);
        Button saveButton = findViewById(R.id.saveGestureButton_M);

        // Load gesture library
        File gestureFile = new File(getFilesDir(), "gestures");
        gestureLibrary = GestureLibraries.fromFile(gestureFile);

        if (!gestureLibrary.load()) {
            Toast.makeText(this, "Gesture library could not be loaded.", Toast.LENGTH_SHORT).show();
        }

        // Capture gesture
        gestureOverlayView.addOnGesturePerformedListener((overlay, gesture) -> {
            Log.d(TAG, "Gesture performed");
            List<Prediction> predictions = gestureLibrary.recognize(gesture);

            if (!predictions.isEmpty()) {
                for (Prediction prediction : predictions) {
                    Log.d(TAG, "Detected gesture: " + prediction.name + " with score: " + prediction.score);
                }

                Prediction bestMatch = predictions.get(0);
                if (bestMatch.name.equals("M") && bestMatch.score > 1.5) { // Lower threshold
                    Log.d(TAG, "Gesture 'M' recognized, opening Map Fragment");
                    openMapFragment();
                } else {
                    Log.d(TAG, "Gesture recognized but not matching 'M' or score too low");
                }
            } else {
                Log.d(TAG, "No valid gesture recognized");
            }
        });


        saveButton.setOnClickListener(v -> {
            if (currentGesture != null) {
                gestureLibrary.addGesture("M", currentGesture); // Save gesture as "M"
                gestureLibrary.save();
                Toast.makeText(this, "Gesture 'M' Saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No gesture to save!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openMapFragment() {
        Log.d(TAG, "Opening Map Fragment");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment existingFragment = fragmentManager.findFragmentByTag("MAP_FRAGMENT");

        if (existingFragment == null) {
            Fragment mapFragment = new MapFragment();
            transaction.replace(R.id.fragment_container, mapFragment, "MAP_FRAGMENT");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Log.d(TAG, "MapFragment already loaded, skipping creation");
        }
    }





}
