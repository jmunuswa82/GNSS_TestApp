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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private LocationManager locationManager;
    private long startTime;


    private ToggleButton startGPSButton,startTTFFButton;
    private Button clearGPSButton;
    private TextView latTextview, longTextview, ttffTextview,
            iterCount, timeDelay;
    private TextView altTextview, ehvTextview,
            altMslTextview, satsTextview,
            speedTextview, bearingTextview, sAccTextview,
            bAccTextview, pDopTextview, hvDopTextview;

    private GnssStatus.Callback gnssStatusCallback;
    private GnssStatus gnssStatus;
    private TableLayout satelliteTable;

    private static final String TAG = "GPS_TTFF";
    private static final int NUM_FIXES = 100;
    private LocationDataHelper locationDataHelper;

    /**
     * Called when the activity is created.
     * Initializes UI elements and sets up event listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        //In First layout
        clearGPSButton = findViewById(R.id.clearGPSButton);
        startTTFFButton = findViewById(R.id.startTTFFButton);
        startGPSButton = findViewById(R.id.startGpsButton);
        latTextview = findViewById(R.id.latTextview);
        longTextview = findViewById(R.id.longTextview);
        ttffTextview = findViewById(R.id.ttffTextview);
        timeDelay = findViewById(R.id.timeDelay);
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
        try {
            // Pass satelliteTable to LocationDataHelper constructor
            locationDataHelper = new LocationDataHelper(this,
                    latTextview, longTextview);
        } catch (Exception e) {
            // Handle exception gracefully
            Log.e(TAG, "Error initializing " +
                    "LocationDataHelper: " + e.getMessage());
            e.printStackTrace();
        }


        startGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startGPSButton.isChecked()) {
                    // Start location updates
                    boolean started = locationDataHelper.startLocationUpdates();
                    if (started) {
                        // Location updates started successfully
                        // Now, check if permission is granted
                        if (checkLocationPermission()) {
                            // Permission granted, attempt to get last known location
                            try {
                                Location lastKnownLocation = locationManager.
                                        getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (lastKnownLocation != null) {
                                    // Last known location available, update UI
                                    double latitude = lastKnownLocation.getLatitude();
                                    double longitude = lastKnownLocation.getLongitude();
                                    latTextview.setText("Latitude: " + latitude);
                                    longTextview.setText("Longitude: " + longitude);
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
                    }
                } else {
                    // Stop location updates
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
                        ttffTextview, iterCount, timeDelay, altTextview, ehvTextview,
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
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Location","Allow location permissions");

               locationDataHelper.startLocationUpdates();
            } else {
                Toast.makeText(this,
                        "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Called when the activity is resumed.
     * Registers the GNSS status callback if
     * location permission is granted.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        }
    }

    /**
     * Called when the activity is being destroyed.
     * Stops location updates and logs a message.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Location","Location updates Stopped!");
        locationDataHelper.stopLocationUpdates();
    }
}