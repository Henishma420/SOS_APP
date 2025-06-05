package com.eles.traffic_pro_java;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.util.List;

public class Navigation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private GestureLibrary gestureLibrary;
    private static final String TAG = "GestureTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Toggle Button
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load gesture library
        File gestureFile = new File(getFilesDir(), "gestures");
        gestureLibrary = GestureLibraries.fromFile(gestureFile);
        if (!gestureLibrary.load()) {
            Log.e(TAG, "Gesture library failed to load");
            Toast.makeText(this, "Gesture library failed to load", Toast.LENGTH_SHORT).show();
        }

        // Gesture detection
        GestureOverlayView gestureOverlayView = findViewById(R.id.gesture_overlay);
        gestureOverlayView.addOnGesturePerformedListener((overlay, gesture) -> {
            Log.d(TAG, "Gesture performed");
            List<Prediction> predictions = gestureLibrary.recognize(gesture);

            if (!predictions.isEmpty()) {
                Prediction bestMatch = predictions.get(0);
                Log.d(TAG, "Best match: " + bestMatch.name + " with score: " + bestMatch.score);
                if (bestMatch.name.equals("M") && bestMatch.score > 3.0) {
                    Log.d(TAG, "Gesture 'M' recognized, opening Map Fragment");
                    openMapFragment();
                } else {
                    Log.d(TAG, "Gesture recognized but not matching 'M' or score too low");
                }
            } else {
                Log.d(TAG, "No valid gesture recognized");
            }
        });
    }


    private void openMapFragment() {
        Log.d(TAG, "Opening Map Fragment");
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag("MAP_FRAGMENT");

        if (existingFragment == null) {
            Fragment mapFragment = new MapFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, mapFragment, "MAP_FRAGMENT");
            transaction.addToBackStack(null);
            transaction.commit();
            Log.d(TAG, "Map Fragment opened successfully");
        } else {
            Log.d(TAG, "MapFragment already loaded, skipping creation");
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        Class<?> targetActivity = null;

        if (id == R.id.nav_settings) {
            targetActivity = Settings.class;
        } else if (id == R.id.nav_sos) {
            targetActivity = SOS.class;
        } else if (id == R.id.nav_help) {
            targetActivity = Help.class;
        } else if (id == R.id.risk_zones) {
            targetActivity = Fragmentff.class;
        } else if (id == R.id.nav_offline) {
            targetActivity = OfflineMode.class;
        }

        if (targetActivity != null) {
            startActivity(new Intent(this, targetActivity));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
