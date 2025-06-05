package com.eles.traffic_pro_java;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class DetailFragment extends Fragment {

    private TextView resultTextView;
    private GestureLibrary gestureLibrary;
    private TextToSpeech textToSpeech;
    private double userLat, userLon;
    private String placesInfo = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_detail, container, false);

        // Get user location from arguments
        if (getArguments() != null) {
            userLat = getArguments().getDouble("lat");
            userLon = getArguments().getDouble("lon");
        }

        resultTextView = view.findViewById(R.id.resultTextView);
        Button fetchPlacesButton = view.findViewById(R.id.fetch_places_button);

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        // Load gesture library
        File gestureFile = new File(requireContext().getFilesDir(), "gestures");
        gestureLibrary = GestureLibraries.fromFile(gestureFile);
        if (!gestureLibrary.load()) {
            throw new RuntimeException("Gesture library failed to load");
        }

        // Set up gesture recognition
        GestureOverlayView gestureOverlayView = view.findViewById(R.id.gestureOverlay);
        gestureOverlayView.addOnGesturePerformedListener((overlay, gesture) -> {
            List<Prediction> predictions = gestureLibrary.recognize(gesture);

            if (!predictions.isEmpty()) {
                Prediction bestMatch = predictions.get(0);
                if (bestMatch.name.equals("H") && bestMatch.score > 3.0) {
                    speakOutHospitals(); // Read aloud hospital info
                }
            }
        });


        // Fetch nearby places when button is clicked
        fetchPlacesButton.setOnClickListener(v -> new FetchNearbyPlacesTask().execute(userLat, userLon));

        return view;
    }

    private class FetchNearbyPlacesTask extends AsyncTask<Double, Void, String> {

        @Override
        protected String doInBackground(Double... coords) {
            try {
                double lat = coords[0];
                double lon = coords[1];

                String overpassUrl = "https://overpass-api.de/api/interpreter?data=" +
                        "[out:json][timeout:25];" +
                        "(node[\"amenity\"=\"hospital\"](around:5000," + lat + "," + lon + "););" +
                        "out body;>;out skel qt;";

                URL url = new URL(overpassUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse response JSON
                JSONArray elements = new JSONObject(response.toString()).getJSONArray("elements");
                StringBuilder result = new StringBuilder();
                placesInfo = ""; // Reset

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject element = elements.getJSONObject(i);
                    if (element.has("lat") && element.has("lon")) {
                        double placeLat = element.getDouble("lat");
                        double placeLon = element.getDouble("lon");
                        String placeName = element.has("tags") && element.getJSONObject("tags").has("name")
                                ? element.getJSONObject("tags").getString("name")
                                : "Unnamed Hospital";
                        double distance = distanceBetween(userLat, userLon, placeLat, placeLon);
                        String placeInfo = placeName + " - " + String.format("%.2f", distance) + " meters away\n";
                        result.append(placeInfo);
                        placesInfo += placeInfo; // Store for TTS
                    }
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error fetching data: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            resultTextView.setText(result);
        }
    }

    // Calculate distance between two coordinates
    private double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    // Read aloud hospital names and distances
    private void speakOutHospitals() {
        if (placesInfo.isEmpty()) {
            textToSpeech.speak("No hospitals found nearby.", TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(placesInfo, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
