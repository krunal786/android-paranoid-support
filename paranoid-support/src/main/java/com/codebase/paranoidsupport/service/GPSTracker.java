package com.codebase.paranoidsupport.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;


public class GPSTracker extends Service implements LocationListener {

    // Flag for GPS status
    boolean isGPSEnabled;

    // Flag for network status
    boolean isNetworkEnabled;

    // Flag for GPS status
//    boolean canGetLocation = false;

    Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 500; // 500 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = /*1000 * 60 * 30*/ 10; // 30 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    private OnLocationChanged locationChangedListener;

    public GPSTracker(Context context) {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        // Getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

//        if (isGPSEnabled || isNetworkEnabled) {
//            this.canGetLocation = true;
//        }
        //setupLocation();
    }

    @SuppressLint("MissingPermission")
    public void setupLocation(OnLocationChanged locationChangedListener) {
        this.locationChangedListener = locationChangedListener;
        try {
            // Getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {
//                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    if (locationManager != null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("TAGGER", "Network");
                        if (location == null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
//                        if (location != null) {
//                            latitude = location.getLatitude();
//                            longitude = location.getLongitude();
//                        }
                    }
                }

                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled) {
                    if (locationManager != null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("TAGGER", "GPS Enabled");
                        if (location == null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                            if (location != null) {
//                                latitude = location.getLatitude();
//                                longitude = location.getLongitude();
//                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app.
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }


    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }


    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/Wi-Fi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        //return this.canGetLocation;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Function to show settings Snackbar.
     * On pressing the Settings button it will launch Settings Options.
     */
    public void showSettingsAlert(View parent, View.OnClickListener listener) {
        Snackbar.make(parent, "Go to settings menu and enable device location.", LENGTH_INDEFINITE)
                .setAction("SETTINGS", listener)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(getApplicationContext(), "changed", Toast.LENGTH_SHORT).show();
        Log.d("TAGGER", "onLocationChanged: changed");
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationChangedListener.locationChanged(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public interface OnLocationChanged {
        void locationChanged(Location location);
    }
}