package com.eles.traffic_pro_java;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import java.util.Locale;



public class Alerts {
    private Context context;
    private TextToSpeech textToSpeech;
    private GeoPoint userLocation;
    private Marker nearestRedZone;
    private boolean alert100mTriggered = false;
    private boolean alert50mTriggered = false;
    private AlertListener alertListener;

    public Alerts(Context context, AlertListener listener) {
        this.context = context;
        this.alertListener = listener;  // Fix: Assign listener
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported!");
                }
            } else {
                Log.e("TextToSpeech", "TTS Initialization failed!");
            }
        });
    }

    public void updateUserLocation(GeoPoint newLocation) {
        this.userLocation = newLocation;
        checkProximityAlert();
    }

    public void setNearestRedZone(Marker marker) {
        this.nearestRedZone = marker;
    }

    private void checkProximityAlert() {
        if (userLocation == null || nearestRedZone == null) return;

        double distance = distanceBetween(userLocation, nearestRedZone.getPosition());
        Log.d("ProximityAlert", "Distance to red zone: " + distance + " meters");

        if (distance <= 100 && !alert100mTriggered) {
            alert100mTriggered = true;
            speak("Red zone " + (int) distance + " meters away. Be careful.");
            if (alertListener != null) {
                alertListener.onAlertTriggered("Red zone " + (int) distance + " meters away!");
            }
        }

        if (distance <= 50 && !alert50mTriggered) {
            alert50mTriggered = true;
            speak("Caution! Red zone very close " + (int) distance + " meters away.");
            if (alertListener != null) {
                alertListener.onAlertTriggered("Caution! Red zone very close!");
            }
        }

        if (distance > 100) {
            alert100mTriggered = false;
            alert50mTriggered = false;
        }
    }

    private double distanceBetween(GeoPoint point1, GeoPoint point2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(point2.getLatitude() - point1.getLatitude());
        double dLon = Math.toRadians(point2.getLongitude() - point1.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(point1.getLatitude())) * Math.cos(Math.toRadians(point2.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void speak(String message) {
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void release() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
}
