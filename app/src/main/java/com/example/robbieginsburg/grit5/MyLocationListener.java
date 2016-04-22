package com.example.robbieginsburg.grit5;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class MyLocationListener implements LocationListener {

    LatLng latLong;

    public LatLng getLatLong(){
        return latLong;
    }

    @Override
    public void onLocationChanged(Location location) {

        latLong = null;

        try {
            latLong = new LatLng(location.getLatitude(), location.getLongitude());
        }
        catch (Exception e) {
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
