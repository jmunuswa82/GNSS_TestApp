package com.example.gpstest;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private LocationManager locationManager;

    private ToggleButton startGPSButton,startTTFFButton;
    private Button clearGPSButton;

    private TextView latTextview, longTextview, ttffTextview,
            iterCount, timeDelay;

    private GnssStatus.Callback gnssStatusCallback;
    private GnssStatus gnssStatus;
    private TableLayout satelliteTable;

    private static final String TAG = "MainActivity";
    private LocationDataHelper locationDataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //In First layout Textviews
        clearGPSButton = findViewById(R.id.clearGPSButton);
        startTTFFButton = findViewById(R.id.startTTFFButton);
        startGPSButton = findViewById(R.id.startGpsButton);
        latTextview = findViewById(R.id.latTextview);
        longTextview = findViewById(R.id.longTextview);
        ttffTextview = findViewById(R.id.ttffTextview);
        timeDelay = findViewById(R.id.timeDelay);
        iterCount = findViewById(R.id.iterCount);

        //Third Layout Textview
        satelliteTable = findViewById(R.id.satelliteTable);

        // Initialize the LocationManager to access system location services
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "LocationManager initialized successfully.");


        try {
            // Attempt to create an instance of LocationDataHelper
            locationDataHelper = new LocationDataHelper(this);
            Log.d(TAG, "LocationDataHelper initialized successfully.");
        } catch (Exception e) {
            // Log any errors that occur during initialization
            Log.e(TAG, "Error initializing LocationDataHelper: " + e.getMessage());

            // If an exception occurs, rethrow it as a RuntimeException
            throw new RuntimeException(e);
        }


        startGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startGPSButton.isChecked()) {
                    locationDataHelper.startLocationUpdates();
                } else {
                    locationDataHelper.stopLocationUpdates();
                }
            }
        });

        startTTFFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (startTTFFButton.isChecked())
                {
                    locationDataHelper.startLocationUpdates();

                } else {
                    locationDataHelper.stopLocationUpdates();
                }

            }
        });

        /**
         * Callback interface for receiving notifications when the status of GNSS satellites changes.
         * An instance of this interface must be registered with the GNSS system to receive updates.
         */

        gnssStatusCallback = new GnssStatus.Callback() {

            /**
             * Called when the status of GNSS satellites changes.
             *
             * @param status The GnssStatus object containing information about the current status of GNSS satellites.
             *               This method delegates the task of processing the satellite status change to another class
             *               (e.g., SatelliteUtils) to handle the retrieval and updating of satellite information in the UI.
             */
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                gnssStatus = status;
                Log.d("MainActivity", "calling the user library!");
                SatelliteUtils.fetchSatelliteInfo(gnssStatus, satelliteTable);
            }
        };

        //request GNSS update
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        } else {
            //request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        /**
         * Interface definition for a callback to be invoked when a view is clicked.
         */
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             *          This method delegates the task of clearing GPS data to another class (e.g., SatelliteUtils)
             *          to handle the actual clearing of GPS data from the specified TableLayout.
             */
         clearGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call clear function from SatelliteUtils
                SatelliteUtils.clearGPSData(satelliteTable);
                //clearGPSData(); // Call your existing clear function if needed
            }
        });
    }

    /**
     * Callback method triggered when the user responds to a permission request.
     * Handles the result of the location permission request
     * by starting location updates if permission is granted,
     * and shows a toast message if permission is denied.
     *
     * @param requestCode  The request code passed in requestPermissions().
     * @param permissions  The requested permissions.
     * This may be a subset of the permissions requested in the requestPermissions() call.
     * @param grantResults The grant results for the corresponding permissions,
     * indicating whether each permission is granted or denied.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            // Check if the request code matches the location permission request code
            if (requestCode == REQUEST_LOCATION_PERMISSION) {
                // Check if the user granted the location permission
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    // Log that location permissions are allowed
                    Log.d(TAG, "Location permissions granted");

                    // Ensure locationDataHelper is not null before starting location updates
                    if (locationDataHelper != null) {
                        // Start location updates
                        locationDataHelper.startLocationUpdates();
                    } else {
                        // Log an error if locationDataHelper is null
                        Log.e(TAG, "locationDataHelper is null");
                    }
                } else {
                    // Show a toast message indicating that location permission is denied
                    Toast.makeText(this, "Location permission denied",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            // Handle any exceptions that may occur
            Log.e(TAG, "Error in onRequestPermissionsResult(): " + e.getMessage());
        }
    }



    /**
     * Called when the activity is resumed.
     * Registers the GNSS status callback if location permission is granted.
     * If permission is not granted, the GNSS status callback is not registered.
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Check if the ACCESS_FINE_LOCATION permission is granted
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Register GNSS status callback
                locationManager.registerGnssStatusCallback(gnssStatusCallback);
                Log.d(TAG, "GNSS status callback registered");
            }
        } catch (SecurityException e) {
            // Handle security exception
            Log.e(TAG, "Security exception: " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            Log.e(TAG, "Error in onResume(): " + e.getMessage());
        }
    }



    /**
     * Called when the activity is destroyed.
     * Stops location updates and logs a message.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Log a message indicating that location updates are stopped
            Log.d(TAG, "Location updates stopped");

            // Stop location updates
            locationDataHelper.stopLocationUpdates();
        } catch (SecurityException e) {
            // Handle security exception
            Log.e(TAG, "Security exception: " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            Log.e(TAG, "Error in onDestroy(): " + e.getMessage());
        }
    }

}