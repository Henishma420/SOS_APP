package com.eles.traffic_pro_java;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;

public class GestureActivity extends Activity {

    private GestureLibrary gestureLibrary;
    private GestureOverlayView gestureOverlayView;
    private Gesture currentGesture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestureh);

        gestureOverlayView = findViewById(R.id.gestureOverlay);
        Button saveButton = findViewById(R.id.saveGestureButtonh);


        File gestureFile = new File(getFilesDir(), "gestures");
        gestureLibrary = GestureLibraries.fromFile(gestureFile);

        if (!gestureLibrary.load()) {
            Toast.makeText(this, "Gesture library could not be loaded.", Toast.LENGTH_SHORT).show();
        }

        // Capture gesture
        gestureOverlayView.addOnGesturePerformedListener((overlay, gesture) -> {
            currentGesture = gesture;
            Toast.makeText(this, "Gesture Recorded!", Toast.LENGTH_SHORT).show();
        });

        // Save Gesture when button is clicked
        saveButton.setOnClickListener(v -> {
            if (currentGesture != null) {
                gestureLibrary.addGesture("H", currentGesture); // Save gesture as "H"
                gestureLibrary.save();
                Toast.makeText(this, "Gesture 'H' Saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No gesture to save!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
