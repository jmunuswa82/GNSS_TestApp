package com.example.gpstest;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * Helper class for handling location updates and events.
 */
public class LocationDataHelper implements LocationListener {

    private static final String TAG = "LocationDataHelper";
    private static final int REQUEST_LOCATION_PERMISSION = 123;

    private long startTime;
    private MainActivity mainActivity;
    private LocationManager locationManager;
    private TableLayout satelliteTable;
    TTFFTracker ttffTracker;
    private long iterCount = 0;
    TextView itercount;

    /**
     * Constructs a new LocationDataHelper object.
     *
     * @param mainActivity The activity to be associated with this helper.
     * @throws Exception If an error occurs during initialization.
     */
    public LocationDataHelper(MainActivity mainActivity) throws Exception {
        this.mainActivity = mainActivity;
        try {
            // Initialize the location manager
            locationManager
                    = (LocationManager) mainActivity.getSystemService(Activity.LOCATION_SERVICE);
        } catch (Exception e) {
            // Log any errors that occur during initialization
            Log.e(TAG, "Error getting location manager: " + e.getMessage());

            // Throw an exception to indicate initialization failure
            throw new Exception("Error initializing LocationDataHelper", e);
        }
        // Initialize satelliteTable
        satelliteTable = mainActivity.findViewById(R.id.satelliteTable);
        if (satelliteTable == null) {
            throw new IllegalStateException("Failed to retrieve satelliteTable");
        }

        itercount = mainActivity.findViewById(R.id.iterCount);
        if (itercount == null) {
            throw new IllegalStateException("Failed to retrieve iterCount");
        }
    }


    /**
     * Called when the location has changed.
     * This method handles new location updates by passing the location and update time to
     * the method responsible for processing the update.
     *
     * @param location The new location.
     */
    @Override
    public void onLocationChanged(Location location) {
        try {
            // Get the time of the new location update
            long updateTime = System.currentTimeMillis();

            // Pass the location and update time to the method handling the new location update
            handleNewLocationUpdate(location, updateTime);
        } catch (Exception e) {
            // Log any exceptions that occur during the handling of location updates
            Log.e(TAG, "Error handling location update: " + e.getMessage());
        }
    }


    /**
     * Called when the status of the location provider changes.
     *
     * @param provider The name of the location provider.
     * @param status   The new status.
     * @param extras   Optional extras.
     */
    @Override
    public void onStatusChanged(String provider,
                                int status, Bundle extras) {
        try {
            Log.d(TAG, "status of the location provider changed");
        } catch (Exception e) {
            Log.e(TAG, "Error handling status change: " + e.getMessage());
        }
    }

    /**
     * Called when the location provider is enabled.
     *
     * @param provider The name of the location provider.
     */
    @Override
    public void onProviderEnabled(String provider) {
        try {
            Log.d(TAG, "Location provider enabled");

        } catch (Exception e) {
            Log.e(TAG, "Error handling provider enabled event: "
                    + e.getMessage());
        }
    }

    /**
     * Called when the location provider is disabled.
     *
     * @param provider The name of the location provider.
     */
    @Override
    public void onProviderDisabled(String provider) {
        try {
            Log.d(TAG, "Location provider disabled");
        } catch (Exception e) {
            Log.e(TAG, "Error handling provider disabled event: "
                    + e.getMessage());
        }
    }


    /**
     * Starts location updates.
     * This method requests location updates from the GPS provider and handles permission checks.
     */
    public void startLocationUpdates() {
        try {
            // Set startTime just before requesting location updates
            startTime = System.currentTimeMillis();

            // Check if location permission is granted
            if (ContextCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if GPS provider is enabled
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // If GPS is not enabled, prompt user to enable it
                    showLocationSettingsDialog();
                    Log.d(TAG, "GPS provider is not enabled");
                } else {
                    // Request location updates with a interval of 1 second (1000 milliseconds)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1000, 0, this);
                    Log.d(TAG, "Location updates started");
                }
            } else {
                // Request location permission if not granted
                ActivityCompat.requestPermissions(mainActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
                Log.d(TAG, "Location permission not granted");
            }
        } catch (SecurityException e) {
            // Handle permission denied exception
            Log.e(TAG, "Permission denied: " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            Log.e(TAG, "Error starting location updates: " + e.getMessage());
        }
    }


    /**
     * Stops location updates.
     * This method removes location updates from the location manager.
     *
     * @throws SecurityException If the caller does not have the required permission.
     */
    public void stopLocationUpdates() throws SecurityException {
        try {
            // Remove location updates
            locationManager.removeUpdates(this);

            // Log that location updates are stopped
            Log.d(TAG, "Location updates stopped");
        } catch (SecurityException e) {
            // Log the exception if security exception occurs
            Log.e(TAG, "Failed to stop location updates: " + e.getMessage());
            // Rethrow the security exception
            throw e;
        } catch (Exception e) {
            // Log other exceptions
            Log.e(TAG, "Error stopping location updates: " + e.getMessage());
        }
    }



    /**
     * Shows a dialog to prompt the user to enable location settings.
     */
    private void showLocationSettingsDialog() {
        try {
            // Create a dialog builder instance
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

            // Set the message and button actions for the dialog
            builder.setMessage("GPS is disabled. Do you want to enable it?")
                    .setCancelable(false)
                    // Positive button action to open location settings
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Start the activity to open location settings
                            mainActivity.startActivity(
                                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    // Negative button action to cancel the dialog
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Dismiss the dialog
                            dialog.cancel();
                        }
                    });

            // Create and display the dialog
            AlertDialog alert = builder.create();
            alert.show();

            // Log that the dialog is being shown
            Log.d(TAG, "Location settings dialog is shown.");

        } catch (Exception e) {
            // Log any errors that occur during the creation or display of the dialog
            Log.e(TAG, "Error showing location settings dialog: " + e.getMessage());
        }
    }

    /**
     * Method to handle receiving new location updates from other parts of your code.
     * Calculates the time to first fix for the new location update and processes the update.
     *
     * @param newLocation The new Location object representing the updated location.
     * @param updateTime  The time when the location update was received.
     */
    public void handleNewLocationUpdate(Location newLocation, long updateTime) {
        // Calculate the time to first fix for the new location update
        long timeToFirstFix = updateTime - startTime;

        // Increment the iteration count
        iterCount += 1;

        // Update the iteration count TextView
        itercount.setText("IterCount:" + iterCount);

        // Log the iteration count
        Log.d(TAG, "Iteration Count:" + iterCount);

        try {
            // Process the location update
            ttffTracker = new TTFFTracker(mainActivity);
            ttffTracker.processLocationUpdate(newLocation, timeToFirstFix);
        } catch (Exception e) {
            // Handle any exceptions that might occur during the method call
            Log.e(TAG, "Error processing location update: " + e.getMessage());
        }

        // Log information about location updates
        Log.d(TAG, "Location updates delayed for 10 seconds");

        // Stop location updates after 100 iterations
        if (iterCount == 100) {
            stopLocationUpdates();
        }

        // Update the start time for the next update
        startTime = System.currentTimeMillis();
    }


}


