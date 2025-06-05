package com.eles.traffic_pro_java;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SOS extends AppCompatActivity implements SensorEventListener {

    private EditText contactName, contactNumber;
    private Button addContactButton, sosButton, automaticSosCancelButton;
    private RecyclerView contactsRecycler;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private static final String TAG = "SOS";
    FusedLocationProviderClient fusedLocationClient;
    DatabaseReference databaseRef;
    private SharedPreferences sharedPreferences;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.0F;
    private static final int SHAKE_COUNT_THRESHOLD = 3;
    private long lastShakeTime = 0;
    private int shakeCount = 0;
    private boolean automaticSosActive = false;
    private CountDownTimer automaticSosTimer;
    private AlertDialog automaticSosDialog;
    private TextView automaticSosCountdown;
    private Vibrator vibrator;
    private static final long COUNTDOWN_DURATION = 60000; // 1 minute in milliseconds
    private static final long COUNTDOWN_INTERVAL = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_sos);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        databaseRef = FirebaseDatabase.getInstance().getReference("sos_requests");
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        contactName = findViewById(R.id.contact_name);
        contactNumber = findViewById(R.id.contact_number);
        addContactButton = findViewById(R.id.add_contact_button);
        sosButton = findViewById(R.id.sos_button);
        contactsRecycler = findViewById(R.id.contacts_recycler);

        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList);
        contactsRecycler.setAdapter(contactAdapter);

        addContactButton.setOnClickListener(v -> {
            String name = contactName.getText().toString();
            String number = contactNumber.getText().toString();

            if (!name.isEmpty() && !number.isEmpty()) {
                contactList.add(new Contact(name, number));
                contactAdapter.notifyDataSetChanged();
                contactName.setText("");
                contactNumber.setText("");
                Toast.makeText(SOS.this, "Contact Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SOS.this, "Please enter valid details", Toast.LENGTH_SHORT).show();
            }
        });

        sosButton.setOnClickListener(v -> {
            Log.d(TAG, "SOS button clicked");
            sendSOS();
            sosButton.setEnabled(false);
        });


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(TAG, "Accelerometer sensor registered.");
            } else {
                Log.w(TAG, "Accelerometer sensor not found on this device.");
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private void triggerAutomaticSosCheck() {
        if (automaticSosActive) {
            Log.d(TAG, "Automatic SOS already active, ignoring new trigger.");
            return;
        }
        automaticSosActive = true;
        vibratePhone();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.automatic_sos, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        automaticSosCountdown = dialogView.findViewById(R.id.automatic_sos_countdown);
        automaticSosCancelButton = dialogView.findViewById(R.id.automatic_sos_cancel_button);
        automaticSosCancelButton.setOnClickListener(v -> cancelAutomaticSos());

        automaticSosDialog = builder.create();
        automaticSosDialog.show();

        startAutomaticSosTimer();
    }

    private void startAutomaticSosTimer() {
        automaticSosTimer = new CountDownTimer(COUNTDOWN_DURATION, COUNTDOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                automaticSosCountdown.setText(String.valueOf(millisUntilFinished / 1000));
                vibratePhoneTick(); // Vibrate on each tick
            }

            public void onFinish() {
                if (automaticSosActive) {
                    Log.i(TAG, "Automatic SOS timer finished, sending SOS.");
                    sendSOS();
                    automaticSosActive = false;
                    if (automaticSosDialog != null && automaticSosDialog.isShowing()) {
                        automaticSosDialog.dismiss();
                    }
                }
            }
        }.start();
    }

    private void vibratePhoneTick() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(200); // Vibrate for 200 milliseconds on each tick (adjust as needed)
        }
    }

    private void cancelAutomaticSos() {
        automaticSosActive = false;
        if (automaticSosTimer != null) {
            automaticSosTimer.cancel();
        }
        if (automaticSosDialog != null && automaticSosDialog.isShowing()) {
            automaticSosDialog.dismiss();
        }
        Toast.makeText(this, "Automatic SOS cancelled.", Toast.LENGTH_SHORT).show();
        sosButton.setEnabled(true); // Re-enable the SOS button
    }

    private void sendSOS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 124); // Request location permission
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            sosButton.setEnabled(true);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();

                        DatabaseReference sosRef = FirebaseDatabase.getInstance().getReference("sos_requests");
                        String currentRequestId = sosRef.push().getKey();

                        if (currentRequestId != null) {
                            Map<String, Object> sosData = new HashMap<>();
                            sosData.put("latitude", lat);
                            sosData.put("longitude", lon);
                            sosData.put("requestId", currentRequestId);
                            sosData.put("status", "pending");

                            sosRef.child(currentRequestId).setValue(sosData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SOS.this, "SOS Sent with ID: " + currentRequestId, Toast.LENGTH_SHORT).show();
                                        sosButton.setEnabled(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SOS.this, "Failed to send SOS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        sosButton.setEnabled(true);
                                    });
                        } else {
                            Toast.makeText(SOS.this, "Error generating Request ID.", Toast.LENGTH_SHORT).show();
                            sosButton.setEnabled(true);
                        }
                    } else {
                        Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
                        sosButton.setEnabled(true);
                    }
                });
    }

    private void vibratePhone() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(500); // Vibrate for 500 milliseconds
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) > 100) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float gravityX = x / SensorManager.GRAVITY_EARTH;
                float gravityY = y / SensorManager.GRAVITY_EARTH;
                float gravityZ = z / SensorManager.GRAVITY_EARTH;

                float accelerationMagnitude = (float) Math.sqrt(gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ);

                if (accelerationMagnitude > SHAKE_THRESHOLD_GRAVITY) {
                    long timeDifference = currentTime - lastShakeTime;
                    if (timeDifference < 300) {
                        shakeCount++;
                    } else {
                        shakeCount = 1;
                    }
                    lastShakeTime = currentTime;

                    if (shakeCount >= SHAKE_COUNT_THRESHOLD && !automaticSosActive) {
                        Log.i(TAG, "Shake detected, triggering automatic SOS check.");
                        triggerAutomaticSosCheck();
                        shakeCount = 0; // Reset shake count after triggering
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (automaticSosTimer != null) {
            automaticSosTimer.cancel();
        }
        if (automaticSosDialog != null && automaticSosDialog.isShowing()) {
            automaticSosDialog.dismiss();
        }
    }
}