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

    private static Activity activity;
    private static LocationManager locationManager;
    private static long startTime;
    private int fixCount;
    // New TextView variables for latitude and longitude
    private TextView latTextView;
    private TextView longTextView;
    private TableLayout satelliteTable;

    /**
     * Constructs a new LocationDataHelper object.
     *
     * @param activity The activity to be associated with this helper.
     * @throws Exception If an error occurs during initialization.
     */
    public LocationDataHelper(Activity activity, TextView latTextView, TextView longTextView) throws Exception {
        this.activity = activity;
        try {
            // Store TextView objects
            if (latTextView != null && longTextView != null) {
                this.latTextView = latTextView;
                this.longTextView = longTextView;
            } else {
                throw new IllegalArgumentException("TextView objects cannot be null");
            }


            // Initialize the location manager
            locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
            if (locationManager == null) {
                throw new IllegalStateException("Failed to retrieve LocationManager");
            }

            // Record the start time and initialize fix count
            startTime = System.currentTimeMillis();
            fixCount = 0;
            // Initialize satelliteTable
            satelliteTable = activity.findViewById(R.id.satelliteTable);
            if (satelliteTable == null) {
                throw new IllegalStateException("Failed to retrieve satelliteTable");
            }

        } catch (Exception e) {
            // Log any errors that occur during initialization
            Log.e(TAG, "Error getting location manager: " + e.getMessage());

            // Throw an exception to indicate initialization failure
            throw new Exception("Error initializing LocationDataHelper", e);
        }
    }


    /**
     * Called when the location has changed.
     *
     * @param location The new location.
     */
    @Override
    public void onLocationChanged(Location location) {
        try {

            // Extract latitude and longitude from the Location object
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Update UI elements with latitude and longitude
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    latTextView.setText("Latitude: " + latitude);
                    longTextView.setText("Longitude: " + longitude);
                }
            });

            // Log the latitude and longitude
            Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);

            // You can also update UI elements with the latitude and longitude here if needed
            // For example:
            // latTextview.setText("Latitude: " + latitude);
            // longTextview.setText("Longitude: " + longitude);
            long currentTime = System.currentTimeMillis();
            long ttff = currentTime - startTime;
            Log.d(TAG, "TTFF for Fix " + (++fixCount) + ": " + ttff + " ms");

            // Update start time for the next fix
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            Log.e(TAG, "Error handling location change: " + e.getMessage());
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
    public void onStatusChanged(String provider, int status, Bundle extras) {
        try {
            Log.d(TAG,"status of the location provider changed");
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
            Log.d(TAG,"Location provider enabled");

        } catch (Exception e) {
            Log.e(TAG, "Error handling provider enabled event: " + e.getMessage());
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
            Log.d(TAG,"Location provider disabled");
        } catch (Exception e) {
            Log.e(TAG, "Error handling provider disabled event: " + e.getMessage());
        }
    }


    /**
     * Starts location updates.
     *
     * @return True if location updates are successfully started, false otherwise.
     */
    public boolean startLocationUpdates() {
        try {
            // Check if location permission is granted
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if GPS provider is enabled
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // If GPS is not enabled, prompt user to enable it
                    showLocationSettingsDialog();
                    Log.d(TAG, "GPS provider is not enabled");
                    return false;
                } else {
                    // Record the start time
                    startTime = System.currentTimeMillis();
                    // Request location updates
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1000, 0, this);
                    Log.d(TAG, "Location updates started");
                    return true;
                }
            } else {
                // Request location permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
                Log.d(TAG, "Location permission not granted");
                return false;
            }
        } catch (SecurityException e) {
            // Handle permission denied exception
            Log.e(TAG, "Permission denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Handle other exceptions
            Log.e(TAG, "Error starting location updates: " + e.getMessage());
            return false;
        }
    }

    /**
     * Stops location updates.
     *
     * @throws SecurityException If the caller does not have the required permission.
     */
    public void stopLocationUpdates() throws SecurityException {
        try {
            locationManager.removeUpdates(this);
            Log.d(TAG, "Location updates stopped");
        } catch (SecurityException e) {
            // Log the exception
            Log.e(TAG, "Failed to stop location updates: " + e.getMessage());
            // Rethrow the exception
            throw e;
        } catch (Exception e) {
            // Log other exceptions
            Log.e(TAG, "Error stopping location updates: " + e.getMessage());
        }
    }


    /**
     * Shows a dialog to prompt the user to enable location settings.
     */
    private static void showLocationSettingsDialog() {
        try {
            // Create a dialog builder instance
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            // Set the message and button actions for the dialog
            builder.setMessage("GPS is disabled. Do you want to enable it?")
                    .setCancelable(false)
                    // Positive button action to open location settings
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Start the activity to open location settings
                            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
        } catch (Exception e) {
            // Log any errors that occur during the creation or display of the dialog
            Log.e(TAG, "Error showing location settings dialog: " + e.getMessage());
        }
    }

}


