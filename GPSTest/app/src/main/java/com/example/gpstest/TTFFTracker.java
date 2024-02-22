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

     public TTFFTracker(MainActivity mainActivity) {
         this.mainActivity = mainActivity;
     }


    /**
     * Processes a location update and calculates the Time to First Fix (TTFF).
     *
     * @param location  The new location update.
     * @param startTime The start time when location updates were initiated.
     */
    public void processLocationUpdate(Location location, long startTime)
    {
        // Calculate the TTFF
        long ttff = calculateTTFF(startTime);

        // Use the calculated TTFF as needed
        Log.d(TAG, "TTFF for Fix: " + ttff + " ms");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            double speedAccuracy = location.getSpeedAccuracyMetersPerSecond();
            float accuracy = location.getAccuracy();
            double bearingAccuracy = accuracy;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double altitude = location.getAltitude();
            float speed = location.getSpeed();
            float bearing = location.getBearing();

            // Log information
            Log.i(TAG, "Latitude: " + latitude);
            Log.i(TAG, "Longitude: " + longitude);
            Log.i(TAG, "Altitude: " + altitude);
            Log.i(TAG, "Speed: " + speed);
            Log.i(TAG, "Bearing: " + bearing);
            Log.i(TAG, "BearingAccuracy: " + bearingAccuracy);
            Log.i(TAG, "SpeedAccuracy: " + speedAccuracy);

            //Convert to String Value
            String str1 = String.valueOf(ttff);
            String str2 = String.valueOf(latitude);
            String str3 = String.valueOf(longitude);
            String str4 = String.valueOf(altitude);
            String str5 = String.valueOf(speed);
            String str6 = String.valueOf(bearing);
            String str7 = String.valueOf(bearingAccuracy);
            String str8 = String.valueOf(speedAccuracy);

            try {
                TTFFDataReader.ReadDataTTFF(mainActivity, str1, str2, str3, str4,
                        str5, str6, str7, str8);
            } catch (Exception e) {
                // Handle any exceptions that might occur during the method call
                Log.e(TAG, "Error calling TTFFDataReader.ReadDataTTFF: " + e.getMessage());
            }

        }
    }
    /**
     * Calculates the Time to First Fix (TTFF) based on the start time.
     *
     * @param startTime The start time when location updates were initiated.
     * @return The calculated TTFF in milliseconds.
     */
    private long calculateTTFF(long startTime) {
        long currentTime = System.currentTimeMillis();
        return currentTime - startTime;
    }

}
