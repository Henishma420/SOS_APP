package com.eles.traffic_pro_java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SOSActivity extends AppCompatActivity {

    private EditText contactName, contactNumber;
    private Button addContactButton, sosButton;
    private RecyclerView contactsRecycler;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private MyLocationNewOverlay locationOverlay;
    private MapView mapView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_sos);

        initializeUI();
        setupRecyclerView();
        setupMap();
        setupListeners();
    }

    private void initializeUI() {
        contactName = findViewById(R.id.contact_name);
        contactNumber = findViewById(R.id.contact_number);
        addContactButton = findViewById(R.id.add_contact_button);
        sosButton = findViewById(R.id.sos_button);

        contactsRecycler = findViewById(R.id.contacts_recycler);
        mapView = findViewById(R.id.mapview);
    }

    private void setupRecyclerView() {
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList);
        contactsRecycler.setAdapter(contactAdapter);
    }

    private void setupMap() {
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);
    }

    private void setupListeners() {
        addContactButton.setOnClickListener(v -> addContact());
    }

    private void addContact() {
        String name = contactName.getText().toString().trim();
        String number = contactNumber.getText().toString().trim();

        if (!name.isEmpty() && !number.isEmpty()) {
            contactList.add(new Contact(name, number));
            contactAdapter.notifyDataSetChanged();
            contactName.setText("");
            contactNumber.setText("");
            Toast.makeText(this, "Contact Added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show();
        }
    }











}
