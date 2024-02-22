package com.example.gpstest;

import android.util.Log;
import android.widget.TextView;

/**
 * The TTFFDataReader class is responsible for reading Time to First Fix (TTFF) data and updating
 * the UI components accordingly.
 * It provides a method to update TextViews with TTFF data.
 */
public class TTFFDataReader {

    private static final String TAG ="TTFFDataReader" ;

    /**
     * Reads and displays Time to First Fix (TTFF) data on the UI.
     *
     * @param mainActivity       The main activity instance.
     * @param ttff               The Time to First Fix (TTFF) value.
     * @param latitude           The latitude value.
     * @param longitude          The longitude value.
     * @param altitude           The altitude value.
     * @param speed              The speed value.
     * @param bearing            The bearing value.
     * @param bearingAccuracy    The bearing accuracy value.
     * @param speedAccuracy      The speed accuracy value.
     */
    public static void ReadDataTTFF(MainActivity mainActivity, String ttff,
                                    String latitude, String longitude, String altitude,
                                    String speed, String bearing,
                                    String bearingAccuracy, String speedAccuracy) {
        try {

        // Retrieve the TextViews from MainActivity
        TextView ehvTextview = mainActivity.findViewById(R.id.ehvTextview);
        TextView altMslTextview = mainActivity.findViewById(R.id.altMslTextview);
        TextView satsTextview = mainActivity.findViewById(R.id.satsTextview);
        TextView pDopTextview = mainActivity.findViewById(R.id.pDopTextview);
        TextView hvDopTextview = mainActivity.findViewById(R.id.hvDopTextview);
        TextView ttffTextview = mainActivity.findViewById(R.id.ttffTextview);
        TextView latTextview = mainActivity.findViewById(R.id.latTextview);
        TextView longTextview = mainActivity.findViewById(R.id.longTextview);
        TextView altTextview = mainActivity.findViewById(R.id.altTextview);
        TextView speedTextview = mainActivity.findViewById(R.id.speedTextview);
        TextView bearingTextview = mainActivity.findViewById(R.id.bearingTextview);
        TextView bAccTextview = mainActivity.findViewById(R.id.bAccTextview);
        TextView sAccTextview = mainActivity.findViewById(R.id.sAccTextview);

        // Update the UI components with the provided data
        ttffTextview.setText("TTFF: " + ttff + " ms");
        latTextview.setText("Latitude: " + latitude);
        longTextview.setText("Longitude: " + longitude);
        altTextview.setText(" " + altitude);
        speedTextview.setText(" " + speed);
        bearingTextview.setText(" " + bearing);
        bAccTextview.setText(" " + bearingAccuracy);
        sAccTextview.setText(" " + speedAccuracy);

        // Log the location data
        Log.d(TAG, "TTFF: " + ttff + " ms");
        Log.d(TAG, "Latitude: " + latitude);
        Log.d(TAG, "Longitude: " + longitude);
        Log.d(TAG, "Altitude: " + altitude);
        Log.d(TAG, "Speed: " + speed);
        Log.d(TAG, "Bearing: " + bearing);
        Log.d(TAG, "Bearing Accuracy: " + bearingAccuracy);
        Log.d(TAG, "Speed Accuracy: " + speedAccuracy);
    } catch (Exception e) {
        // Handle any exceptions that might occur during the update process
        Log.e(TAG, "Error updating UI components: " + e.getMessage());
    }
    }
}
