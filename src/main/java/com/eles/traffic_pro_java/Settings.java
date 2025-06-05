package com.eles.traffic_pro_java;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    private Switch audioRecordingSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // You'll need to create this layout

        audioRecordingSwitch = findViewById(R.id.audio_recording_switch); // Ensure this ID matches your layout
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);


        boolean isAudioEnabled = sharedPreferences.getBoolean("isAudioRecordingEnabled", false);
        audioRecordingSwitch.setChecked(isAudioEnabled);

        audioRecordingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the new state of the switch
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isAudioRecordingEnabled", isChecked);
                editor.apply();
            }
        });
    }
}