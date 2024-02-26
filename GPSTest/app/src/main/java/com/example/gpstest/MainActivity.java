package com.example.gpstest;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
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
    TextView latTextview;
    private TextView longTextview;
    TextView ttffTextview;
    private TextView iterCount;
    private TextView altTextview, ehvTextview,
            altMslTextview, satsTextview,
            speedTextview, bearingTextview, sAccTextview,
            bAccTextview, pDopTextview, hvDopTextview;

    private GnssStatus.Callback gnssStatusCallback;
    private GnssStatus gnssStatus;
    private TableLayout satelliteTable;

    private static final String TAG = "MainActivity";
    LocationDataHelper locationDataHelper;
    TTFFTracker ttffTracker;


    /**
     * Called when the activity is created.
     * Initializes UI elements and sets up event listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        

        try {
            // Pass satelliteTable to LocationDataHelper constructor
            locationDataHelper = new LocationDataHelper(this);
        } catch (Exception e) {
            // Handle exception gracefully
            Log.e(TAG, "Error initializing LocationDataHelper: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // Create a new instance of TTFFTracker
            ttffTracker = new TTFFTracker(this);
        } catch (Exception e) {
            // Handle exception gracefully
            Log.e(TAG, "Error creating TTFFTracker: " + e.getMessage());
            e.printStackTrace();
        }




        //In First layout
        clearGPSButton = findViewById(R.id.clearGPSButton);
        startTTFFButton = findViewById(R.id.startTTFFButton);
        startGPSButton = findViewById(R.id.startGpsButton);
        latTextview = findViewById(R.id.latTextview);
        longTextview = findViewById(R.id.longTextview);
        ttffTextview = findViewById(R.id.ttffTextview);
        iterCount = findViewById(R.id.iterCount);


        //In second Layout
        altTextview = findViewById(R.id.altTextview);
        ehvTextview = findViewById(R.id.ehvTextview);
        altMslTextview = findViewById(R.id.altMslTextview);
        satsTextview = findViewById(R.id.satsTextview);
        speedTextview = findViewById(R.id.speedTextview);
        bearingTextview = findViewById(R.id.bearingTextview);
        sAccTextview = findViewById(R.id.sAccTextview);
        bAccTextview = findViewById(R.id.bAccTextview);
        pDopTextview = findViewById(R.id.pDopTextview);
        hvDopTextview = findViewById(R.id.hvDopTextview);

        // Get references to satelliteTable
        satelliteTable = findViewById(R.id.satelliteTable);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        startGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startGPSButton.isChecked()) {
                    // Start location updates
                     locationDataHelper.startLocationUpdates();
                        // Now, check if permission is granted
                        if (checkLocationPermission()) {
                            // Permission granted, attempt to get last known location
                            try {
                                Location lastKnownLocation = locationManager.
                                        getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (lastKnownLocation != null) {
                                    // Update satellite details
                                    updateSatelliteDetails();
                                }

                            } catch (SecurityException e) {
                                // Handle SecurityException
                                Log.e(TAG, "SecurityException: " + e.getMessage());
                            }

                        } else {
                            // Permission not granted, request it
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION_PERMISSION);
                        }

                } else {
                    // Stop location updates
                    locationDataHelper.stopLocationUpdates();
                }
            }
        });


        /**
         * Set an OnClickListener to handle click events for the start/stop TTFF button.
         * This method allows users to start or stop location updates by clicking the button.
         *
         * @param listener The OnClickListener to be set for the start/stop TTFF button.
         */
        startTTFFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Documentation log
                    Log.d(TAG, "Start/Stop TTFF Button Clicked");

                    // Toggle location updates based on button state
                    if (startTTFFButton.isChecked()) {
                        try {
                            locationDataHelper.startLocationUpdates();
                            Log.d(TAG, "Location updates stopped.");

                        } catch (Exception e) {
                            // Handle any exceptions that might occur
                            // while starting location updates
                            Log.e(TAG, "Error starting location updates: " + e.getMessage());
                            e.printStackTrace();
                        }
                        Log.d(TAG, "Location updates started.");
                    } else {
                        locationDataHelper.stopLocationUpdates();
                        Log.d(TAG, "Location updates stopped.");
                    }
                } catch (Exception e) {
                    // Handle any exceptions that might occur
                    Log.e(TAG, "Error toggling location updates: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });


        /**
         * Callback method to receive notifications
         * when the status of GNSS satellites changes.
         */
        gnssStatusCallback = new GnssStatus.Callback() {

            /**
             * Called when the status of GNSS satellites changes.
             *
             * @param status The GnssStatus object containing information
             *               about the current status of GNSS satellites.
             *               This method delegates the task of processing
             *               the satellite status change to another class
             *               (e.g., SatelliteUtils) to handle the retrieval
             *               and updating of satellite information in the UI.
             */
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                gnssStatus = status;
                // Call method to update satellite details
                //updateSatelliteDetails();
            }
        };

        //request GNSS update
        if (checkLocationPermission()) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        } else {
            //request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         *          This method delegates the task of clearing GPS data
         *          to another class (e.g., SatelliteUtils)
         *          to handle the actual clearing of GPS data from the
         *          specified TableLayout.
         */
        clearGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define an array containing all TextViews that display GPS data
                TextView[] textViewArray = {latTextview, longTextview,
                        ttffTextview, iterCount, altTextview, ehvTextview,
                        altMslTextview, satsTextview, speedTextview, bearingTextview,
                        sAccTextview, bAccTextview, pDopTextview, hvDopTextview};

                // Call SatelliteUtils.clearGPSData with the array
                SatelliteUtils.clearGPSData(satelliteTable, textViewArray);
                //clearGPSData(); // Call your existing clear function if needed
            }
        });
    }

    /**
     * Updates satellite details if GNSS status is available.
     */
    private void updateSatelliteDetails() {
        if (gnssStatus != null) {
            SatelliteUtils.fetchSatelliteInfo(gnssStatus, satelliteTable);
        } else {
            Log.e(TAG, "GNSS status is null");
        }
    }

    /**
     * Checks if the location permission is granted.
     *
     * @return True if the location permission is
     * granted, false otherwise.
     */
    private boolean checkLocationPermission() {

        Log.d("Location","Checking Location permissions");
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Handles the result of the permission request for
     * accessing location.
     *
     * @param requestCode  The request code passed
     *                     to requestPermissions().
     * @param permissions  The requested permissions.
     *                     This is the same as the permissions
     *                     array passed to requestPermissions().
     * @param grantResults The grant results for the corresponding
     *                    permissions, which is either
     *                     PERMISSION_GRANTED or PERMISSION_DENIED.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Call the super method to ensure that the base class's implementation of
        // onRequestPermissionsResult is executed
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            // Check if the request code matches the location permission request code
            if (requestCode == REQUEST_LOCATION_PERMISSION) {
                // Check if the grantResults array is not empty
                // and the first permission is granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Log a message indicating that location permissions are allowed
                    Log.d("Location", "Allow location permissions");
                    // Start location updates if permission is granted
                    locationDataHelper.startLocationUpdates();
                } else {
                    // If permission is denied, display a toast message informing the user
                    Toast.makeText(this,
                            "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            // Handle any exceptions that might occur during
            // the execution of onRequestPermissionsResult()
            Log.e(TAG, "Error in onRequestPermissionsResult(): " + e.getMessage());
            e.printStackTrace();
        }
    }



    /**
     * Called when the activity is resumed.
     * Registers the GNSS status callback if
     * location permission is granted.
     */
    @Override
    protected void onResume() {
        // Call the super method to ensure that the base class's
        // onResume() implementation is executed
        super.onResume();

        try {
            // Check if location permission is granted
            if (checkLocationPermission()) {
                // If permission is granted, register the GNSS status callback
                locationManager.registerGnssStatusCallback(gnssStatusCallback);
                Log.d(TAG, "GNSS status callback registered.");
            }
        } catch (SecurityException e) {
            // Handle the case where permission is not granted
            Log.e(TAG, "Error registering GNSS status callback: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Handle other exceptions that might occur
            Log.e(TAG, "Unexpected error in onResume(): " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Called when the activity is being destroyed.
     * Stops location updates and logs a message.
     */
    @Override
    protected void onDestroy() {
        // Call the super method to ensure that the base class's onDestroy() implementation is executed
        super.onDestroy();

        try {
            // Log a message indicating that location updates are stopped
            Log.d("Location", "Location updates Stopped!");

            // Stop location updates using the locationDataHelper
            locationDataHelper.stopLocationUpdates();
        } catch (Exception e) {
            // Handle any exceptions that might occur during the execution of onDestroy()
            Log.e("MyApp", "Error in onDestroy(): " + e.getMessage());
            e.printStackTrace();
            // You might also want to display a toast or some UI indication for the user
        }
    }

}