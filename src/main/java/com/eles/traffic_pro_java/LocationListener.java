package com.eles.traffic_pro_java;

import org.osmdroid.util.GeoPoint;

public interface LocationListener {
    void onLocationReceived(GeoPoint location);
}