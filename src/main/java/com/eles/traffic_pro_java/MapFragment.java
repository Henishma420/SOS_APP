package com.eles.traffic_pro_java;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;


public class MapFragment extends Fragment implements AlertListener {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private IMapController mapController;
    private static final String TAG = "OverpassTraffic";
    private HashSet<String> displayedMarkers = new HashSet<>();
    private double lastLat = 0, lastLon = 0;
    private Alerts proximityAlertManager;
    private TextView alertBox;
    private LocationListener locationListener;
    public MapFragment() {
        // Required empty public constructor
    }
    public void setLocationListener(LocationListener listener) {
        this.locationListener = listener;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize any required components here
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_risk, container, false);

        // Load OSMDroid Configuration
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Map Controller Setup
        mapController = mapView.getController();
        mapController.setZoom(14);

        // Show User's Location
        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(requireContext());
        locationOverlay = new MyLocationNewOverlay(locationProvider, mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);

        proximityAlertManager = new Alerts(requireContext(), this);
        alertBox = view.findViewById(R.id.alert_box);

        // Add a red zone marker 50 meters away from the user's location
        locationOverlay.runOnFirstFix(() -> requireActivity().runOnUiThread(() -> {
            GeoPoint userLocation = locationOverlay.getMyLocation();
            if (userLocation != null) {
                GeoPoint redZoneLocation = new GeoPoint(userLocation.getLatitude() + 0.00045, userLocation.getLongitude());
                addRedZoneMarker(redZoneLocation);
                proximityAlertManager.updateUserLocation(userLocation);
                updateTrafficMarkers(userLocation);
                mapController.setCenter(userLocation);

            }
        }));

// Listen for location updates continuously
        locationOverlay.enableMyLocation();
        locationOverlay.runOnFirstFix(() -> new Thread(() -> {
            while (true) {
                GeoPoint newLocation = locationOverlay.getMyLocation();
                if (newLocation != null) {
                    requireActivity().runOnUiThread(() -> {
                        proximityAlertManager.updateUserLocation(newLocation);
                    });
                }
                try {
                    Thread.sleep(2000); // Check location every 2 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start());


        Button nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(v -> {
            GeoPoint userLocation = locationOverlay.getMyLocation();
            if (userLocation != null) {
                Bundle bundle = new Bundle();
                bundle.putDouble("lat", userLocation.getLatitude());
                bundle.putDouble("lon", userLocation.getLongitude());

                DetailFragment detailFragment = new DetailFragment();
                detailFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button backBtn = view.findViewById(R.id.back_map);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), Navigation.class);
            startActivity(intent);
        });

        return view;
    }



    public void onAlertTriggered(String message) {
        requireActivity().runOnUiThread(() -> {
            alertBox.setText(message);
            alertBox.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    private void updateTrafficMarkers(GeoPoint userLocation) {
        if (userLocation == null) return;

        double userLat = userLocation.getLatitude();
        double userLon = userLocation.getLongitude();

        if (distanceBetween(lastLat, lastLon, userLat, userLon) > 500) {
            lastLat = userLat;
            lastLon = userLon;
            new FetchTrafficDataTask().execute(userLat, userLon);
        }
    }

    private void addRedZoneMarker(GeoPoint redZoneLocation) {
        Marker redZoneMarker = new Marker(mapView);
        redZoneMarker.setPosition(redZoneLocation);
        redZoneMarker.setTitle("Red Zone");
        redZoneMarker.setIcon(getResources().getDrawable(R.drawable.ic_red_marker));
        mapView.getOverlays().add(redZoneMarker);
        mapView.invalidate();
        proximityAlertManager.setNearestRedZone(redZoneMarker);
    }


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

    private class FetchTrafficDataTask extends AsyncTask<Double, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Double... coords) {
            try {
                double lat = coords[0];
                double lon = coords[1];
                String overpassUrl = "https://overpass-api.de/api/interpreter?data=" +
                        "[out:json][timeout:15];" +
                        "(node[\"highway\"~\"traffic_signals|stop|crossing\"](around:1000," + lat + "," + lon + ");" +
                        "way[\"highway\"](around:1000," + lat + "," + lon + "););" +
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
                JSONObject jsonObject = new JSONObject(response.toString());
                return jsonObject.getJSONArray("elements");
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data: " + e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(JSONArray elements) {
            if (elements != null) {

                HashSet<String> newDisplayedMarkers = new HashSet<>();

                int markerLimit = 10; // Limit number of markers
                int count = 0;

                for (int i = 0; i < elements.length() && count < markerLimit; i++) {
                    try {
                        JSONObject element = elements.getJSONObject(i);

                        if (element.has("lat") && element.has("lon")) {
                            double lat = element.getDouble("lat");
                            double lon = element.getDouble("lon");
                            String id = lat + "," + lon;


                            double distance = distanceBetween(lastLat, lastLon, lat, lon);

                            if (!displayedMarkers.contains(id) && distance <= 1000) {
                                newDisplayedMarkers.add(id); // Track new markers


                                Marker marker = new Marker(mapView);
                                marker.setPosition(new GeoPoint(lat, lon));
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                marker.setTitle("High Traffic Area");


                                Drawable redMarker = requireContext().getDrawable(R.drawable.ic_red_marker);
                                if (redMarker != null) {
                                    marker.setIcon(redMarker);
                                }

                                mapView.getOverlays().add(marker);
                                count++;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing element: " + e.getMessage());
                    }
                }

                displayedMarkers = newDisplayedMarkers;

                mapView.invalidate();
            }
        }

    }public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    }

