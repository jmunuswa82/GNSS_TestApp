package com.example.gpstest;

import android.location.Location;
import android.os.Build;
import android.util.Log;

/**
 * The TTFFTracker class is responsible for tracking the Time to First Fix (TTFF) for location updates.
 * It calculates the TTFF based on the start time and the time when a location update is received.
 */
public class TTFFTracker
{
    private static final String TAG = "TTFFTracker";

    private MainActivity mainActivity;
    private long ttff;
    private double latitude;
    private double longitude;
    private double altitude;
    private float speed;
    private float bearing;
    private double speedAccuracy;
    private double bearingAccuracy;


    /**
     * Constructs a new TTFFTracker object.
     *
     * @param mainActivity The MainActivity instance. Must not be null.
     */
    public TTFFTracker(MainActivity mainActivity)
    {
        // Set the MainActivity instance
        this.mainActivity = mainActivity;
    }
    /**
     * Processes a location update and calculates the Time to First Fix (TTFF).
     *
     * @param location  The new location update.
     * @param TimeToFirstFix The start time when location updates were initiated.
     */
        public void processLocationUpdate(Location location, long TimeToFirstFix) {
            if (location == null) {
                // Reset values if location is null
                resetValues();
            } else {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Populate values if location is not null
                        ttff = TimeToFirstFix;
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        altitude = location.getAltitude();
                        speed = location.getSpeed();
                        bearing = location.getBearing();
                        bearingAccuracy = location.getAccuracy();
                        speedAccuracy = location.getSpeedAccuracyMetersPerSecond();
                    }
                } catch (Exception e) {
                    // Handle any exceptions that might occur during value retrieval
                    Log.e(TAG, "Error retrieving location values: " + e.getMessage());
                }
            }

            // Log information
            Log.i(TAG, "TTFF: " + ttff);
            Log.i(TAG, "Latitude: " + latitude);
            Log.i(TAG, "Longitude: " + longitude);
            Log.i(TAG, "Altitude: " + altitude);
            Log.i(TAG, "Speed: " + speed);
            Log.i(TAG, "Bearing: " + bearing);
            Log.i(TAG, "BearingAccuracy: " + bearingAccuracy);
            Log.i(TAG, "SpeedAccuracy: " + speedAccuracy);

            // Convert values to String
            String strTTFF = String.valueOf(ttff);
            String strLatitude = String.valueOf(latitude);
            String strLongitude = String.valueOf(longitude);
            String strAltitude = String.valueOf(altitude);
            String strSpeed = String.valueOf(speed);
            String strBearing = String.valueOf(bearing);
            String strBearingAccuracy = String.valueOf(bearingAccuracy);
            String strSpeedAccuracy = String.valueOf(speedAccuracy);

            // Check if MainActivity is null before calling ReadDataTTFF
            if (mainActivity != null) {
                TTFFDataReader.ReadDataTTFF(mainActivity, strTTFF, strLatitude, strLongitude, strAltitude,
                        strSpeed, strBearing, strBearingAccuracy, strSpeedAccuracy);
            } else {
                Log.e(TAG, "MainActivity is null");
            }
        }

        private void resetValues() {
            // Reset all values to default when location is null
            ttff = 0;
            latitude = 0;
            longitude = 0;
            altitude = 0;
            speed = 0;
            bearing = 0;
            speedAccuracy = 0;
            bearingAccuracy = 0;
        }
    }
