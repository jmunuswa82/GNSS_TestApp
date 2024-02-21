package com.example.gpstest;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE =100;
    private static int requestcode = 0;
    private LocationListener originalLocationListener;

    // Declare a variable to keep track of the previous iteration count
    private int previousIterationCount = 0;


    private LocationManager locationManager;
    private LocationListener locationListener;
    private long startTime;


    private int iterationCount = 1;
    private long totalTTFF;
    private Handler handler = new Handler();


    private ToggleButton startGPSButton;
    private Button clearGPSButton, startTTFFButton;
    private TextView latTextview, longTextview, ttffTextview, iterCount, timeDelay;
    private TextView altTextview, ehvTextview,
            altMslTextview, satsTextview,
            speedTextview, bearingTextview, sAccTextview,
            bAccTextview, pDopTextview, hvDopTextview;

    private GnssStatus.Callback gnssStatusCallback;
    private GnssStatus gnssStatus;
    private TableLayout satelliteTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

        //3rd layer
        satelliteTable = findViewById(R.id.satelliteTable);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        handler = new Handler();

        // Initialize location listener
        locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                // Check request code
                if (requestcode == LOCATION_PERMISSION_REQUEST_CODE) {
                    // Extract location information
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    double altitude = location.getAltitude();
                    // Get speed in meters per second
                    float speed = location.getSpeed();
                    // Get bearing in degrees clockwise from north
                    float bearing = location.getBearing();

                    // Calculate TTFF
                    long currentTime = System.currentTimeMillis();
                    long ttff = (currentTime - startTime) / 1000;
                    if (ttff > 0) {

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            float speedAccuracy = location.getSpeedAccuracyMetersPerSecond();
                            // Get the estimated accuracy
                            float accuracy = location.getAccuracy();
                            // Assuming that the accuracy value represents both position and bearing accuracy
                            double bearingAccuracy = accuracy;


                            // Check if the iteration count has changed
                            if (iterationCount != previousIterationCount) {
                                // Update the previous iteration count
                                previousIterationCount = iterationCount;
                                // Update UI elements
                                updateUI(latitude, longitude, altitude, ttff, speed, bearing, speedAccuracy, bearingAccuracy);
                            }
                        }
                        // Schedule next iteration after a delay
                        displayTimeDelay();
                        totalTTFF += ttff;
                        scheduleNextIteration();
                    }

                } else {
                    // Stop location updates
                    stopLocationUpdates();
                }
            }

            private void stopLocationUpdates() {

                locationManager.removeUpdates(this);
            }

            private void updateUI(double latitude, double longitude,
                                  double altitude, long ttff, float speed,
                                  float bearing, float speedAccuracy,
                                  double bearingAccuracy) {
                runOnUiThread(() -> {
                    latTextview.setText("Latitude: " + latitude);
                    longTextview.setText("Longitude: " + longitude);
                    altTextview.setText("" + altitude);
                    ttffTextview.setText("TTFF: " + ttff + "s");
                    speedTextview.setText("" + speed);
                    bearingTextview.setText("" + bearing);
                    sAccTextview.setText("" + speedAccuracy);
                    bAccTextview.setText("" + bearingAccuracy);
                    iterCount.setText("IterCount: " + iterationCount);

                    // Log information
                    Log.d("TTFF", "Latitude: " + latitude);
                    Log.d("TTFF", "Longitude: " + longitude);
                    Log.d("TTFF", "Altitude: " + altitude);
                    Log.d("TTFF", "TTFF: " + ttff + "s");
                    Log.d("TTFF", "Speed:" + speed);
                    Log.d("TTFF", "Bearing:" + bearing);
                    Log.d("TTFF", "Speed Accuracy:" + speedAccuracy);
                    Log.d("TTFF", "Bearing Accuracy:" + bearingAccuracy);
                    Log.d("TTFF", "Iteration count: " + iterationCount);

                });
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                // Prompt user to enable location services
                showLocationSettingsDialog();
            }
        };

        startGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startGPSButton.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });

        startTTFFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "TTFF Start", Toast.LENGTH_SHORT).show();
                Log.d("TTFF", "start TTFF pressed");

                // Check for location permission
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request permission if not granted
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    // Permission already granted, start TTFF measurement
                    requestcode = LOCATION_PERMISSION_REQUEST_CODE;
                    startTTFFMeasurement();

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
        clearGPSButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             *          This method delegates the task of clearing GPS data to another class (e.g., SatelliteUtils)
             *          to handle the actual clearing of GPS data from the specified TableLayout.
             */
            @Override
            public void onClick(View v) {
                // Call clear function from SatelliteUtils
                SatelliteUtils.clearGPSData(satelliteTable);
                //clearGPSData(); // Call your existing clear function if needed
            }
        });
    }

    private void scheduleNextIteration() {
        // Schedule after 10 seconds
        handler.postDelayed(this::startNextIteration, 10000);
    }
    private void displayTimeDelay() {
        runOnUiThread(() -> {
            timeDelay.setText("Time Delay: 10s");
            Log.d("TTFF", "Time Delay: 10s");
        });

        handler.postDelayed(() -> {
            // Hide the time delay information
            runOnUiThread(() -> timeDelay.setText("Time Delay:"));
        }, 10000); // Hide after 10 seconds
    }



    private void startNextIteration() {
        iterationCount++;
        if (iterationCount < 100 || iterationCount == 100) {
            startTime = System.currentTimeMillis();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    !=PackageManager.PERMISSION_GRANTED)
            {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        } else if(iterationCount == 101 && previousIterationCount == 100) {
            double averageTTFF = (double) totalTTFF / 100;
            Log.d("TTFF", "Average TTFF for 100 iterations: " + averageTTFF + " s");
            locationManager.removeUpdates(originalLocationListener);
        }
    }

    private void startTTFFMeasurement() {
        iterationCount = 0;
        totalTTFF = 0;
        originalLocationListener = locationListener; // Assign the original listener
        startNextIteration();
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        Log.d("OnCreateOptionsMenu","Menu options Created");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Log.d("Menu","Settings!");
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void startLocationUpdates() {
        if (checkLocationPermission()) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // If GPS is not enabled, prompt user to enable it
                showLocationSettingsDialog();
            } else {
                startTime = System.currentTimeMillis(); // Record the start time
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000, 0, locationListener);
                Toast.makeText(MainActivity.this,
                        "GPS Started", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

        }
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
        Toast.makeText(MainActivity.this,
                "GPS Stopped", Toast.LENGTH_SHORT).show();
    }

   /* private void updateLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double altitude = location.getAltitude();


            latTextview.setText("Latitude: " + latitude);
            longTextview.setText("Longitude: " + longitude);
            altTextview.setText(""+altitude);
        }
    }*/

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("GPS is disabled. Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean checkLocationPermission() {

        Log.d("Location","Checking Location permissions");
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestcode = REQUEST_LOCATION_PERMISSION;
                Log.d("Location","Allow location permissions");

                startLocationUpdates();
            } else {
                Toast.makeText(this,
                        "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start TTFF measurement
                requestcode = LOCATION_PERMISSION_REQUEST_CODE;
                startTTFFMeasurement();
            } else {
                // Permission denied, log a message
                Log.e("TTFF", "Location permission denied");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Location","Location updates Stopped!");
        stopLocationUpdates();
    }
}