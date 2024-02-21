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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private LocationManager locationManager;
    private long startTime;


    private ToggleButton startGPSButton,startTTFFButton;
    private Button clearGPSButton;
    private TextView latTextview, longTextview, ttffTextview, iterCount, timeDelay;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            // Attempt to create a new LocationDataHelper instance
            locationDataHelper = new LocationDataHelper(this);
        } catch (Exception e) {
            // If an exception occurs during initialization,
            // log the error and throw a RuntimeException
            Log.e(TAG, "Error initializing LocationDataHelper: " + e.getMessage());
            throw new RuntimeException(e);
        }

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

        //Third Layout
        satelliteTable = findViewById(R.id.satelliteTable);

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
        if (checkLocationPermission()) {
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
        /*clearGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call clear function from SatelliteUtils
                SatelliteUtils.clearGPSData(satelliteTable);
                //clearGPSData(); // Call your existing clear function if needed
            }
        });*/
    }

    private boolean checkLocationPermission() {

        Log.d("Location","Checking Location permissions");
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            String[] permissions,
                                            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Location","Location updates Stopped!");
        locationDataHelper.stopLocationUpdates();
    }
}