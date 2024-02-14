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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE =100;
    private static int requestcode = 0;
    boolean iterationCountChanged = true;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private long startTime;
    private List<Long> ttffList;
    private int coldStartCount;
    private int iterationCount;
    private long totalTTFF;
    private Handler handler = new Handler();


    private ToggleButton startGPSButton;
    private Button clearGPSButton, startTTFFButton;
    private TextView latTextview, longTextview, ttffTextview, timeTextview;
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


        ttffList = new ArrayList<>();
        coldStartCount = 0;

        //In First layout
        clearGPSButton = findViewById(R.id.clearGPSButton);
        startTTFFButton = findViewById(R.id.startTTFFButton);
        startGPSButton = findViewById(R.id.startGpsButton);
        latTextview = findViewById(R.id.latTextview);
        longTextview = findViewById(R.id.longTextview);
        ttffTextview = findViewById(R.id.ttffTextview);
        timeTextview = findViewById(R.id.timeTextview);

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

        // Initialize location listener
        locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                //updateLocation(location);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double altitude = location.getAltitude();
                // Calculate TTFF
                long currentTime = System.currentTimeMillis();
                long ttff = (currentTime - startTime) / 1000;

                if (requestcode == LOCATION_PERMISSION_REQUEST_CODE) {
                    if (iterationCount > 0) {

                        if (iterationCountChanged) {
                        latTextview.setText("Latitude: " + latitude);
                        Log.d("TTFF", "Latitude " + latitude);

                        longTextview.setText("Longitude: " + longitude);
                        Log.d("TTFF", "Longitude: " + longitude);

                        altTextview.setText("" + altitude);
                        Log.d("TTFF", "altitude" + altitude);

                        String ttffval = String.valueOf(ttff);
                        ttffTextview.setText("TTFF:" + ttffval + "s");

                            totalTTFF += ttff;
                            Log.d("TTFF", "TTFF for iteration " + iterationCount + ": " + ttff + " s");
                            iterationCountChanged = false; // Reset the flag
                        }

                        // Start next iteration after a delay of 10 seconds
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                iterationCountChanged = false;
                                startNextIteration();
                            }
                        }, 10000); // 10 seconds delay

                        // Continue listening for location updates
                        startTime = currentTime;

                    } else {
                        // Once we receive a location update, we can stop listening for further updates
                        locationManager.removeUpdates(this);
                    }
                } else if (requestcode == REQUEST_LOCATION_PERMISSION) {
                    latTextview.setText("Latitude: " + latitude);
                    Log.d("TTFF", "Latitude " + latitude);

                    longTextview.setText("Longitude: " + longitude);
                    Log.d("TTFF", "Longitude: " + longitude);

                    altTextview.setText("" + altitude);
                    Log.d("TTFF", "altitude" + altitude);

                    String ttffval = String.valueOf(ttff);
                    ttffTextview.setText("TTFF:" + ttffval + "s");

                    Log.d("TTFF", "Time to First Fix (TTFF): " + ttffval + " seconds");

                    // Continue listening for location updates
                    startTime = currentTime;

                    // Once we receive a location update, we can stop listening for further updates
                    locationManager.removeUpdates(this);
                }
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
        // Update time every second
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateTime();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();


        gnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                gnssStatus = status;
                updateSatelliteInfo();
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


    }

    private void startNextIteration() {
        iterationCount++;
        if (iterationCount <100) {
            startTime = System.currentTimeMillis();
            iterationCountChanged = true;
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
        } else {
            double averageTTFF = (double) totalTTFF / 100;
            Log.d("TTFF", "Average TTFF for 100 iterations: " + averageTTFF + " s");
            locationManager.removeUpdates((LocationListener) this);

        }
    }

    private void startTTFFMeasurement() {
        iterationCount = 0;
        totalTTFF = 0;
        startNextIteration();
    }

    private void updateSatelliteInfo() {
        // Clear previous satellite information
        satelliteTable.removeViews(1, satelliteTable.getChildCount() - 1);

        // Iterate through each satellite
        int satelliteCount = gnssStatus.getSatelliteCount();
        for (int i = 0; i < satelliteCount; i++) {
            // Retrieve satellite information
            int prn = gnssStatus.getSvid(i);
            int constellationType = gnssStatus.getConstellationType(i);
            float cn0 = gnssStatus.getCn0DbHz(i);
            float elevation = gnssStatus.getElevationDegrees(i);
            float azimuth = gnssStatus.getAzimuthDegrees(i);

            // Create a new TableRow to hold satellite data
            TableRow satelliteRow = new TableRow(this);

            // Create TextViews for satellite data
            TextView idTextView = new TextView(this);
            idTextView.setText(String.valueOf(prn));
            satelliteRow.addView(idTextView);

            TextView gnssTextView = new TextView(this);
            gnssTextView.setText(getConstellationType(constellationType));
            satelliteRow.addView(gnssTextView);

            TextView cfTextView = new TextView(this);
            cfTextView.setText("CF Data");
            satelliteRow.addView(cfTextView);

            TextView cnoTextView = new TextView(this);
            cnoTextView.setText(cn0 > 0 ? String.valueOf(cn0) : ""); // Display C/No if available
            satelliteRow.addView(cnoTextView);

            TextView flagsTextView = new TextView(this);
            flagsTextView.setText("Flags Data"); // Placeholder for Flags data
            satelliteRow.addView(flagsTextView);

            TextView elevTextView = new TextView(this);
            elevTextView.setText(elevation > 0 ? String.valueOf(elevation) : ""); // Display elevation if available
            satelliteRow.addView(elevTextView);

            TextView azimTextView = new TextView(this);
            azimTextView.setText(azimuth > 0 ? String.valueOf(azimuth) : ""); // Display azimuth if available
            satelliteRow.addView(azimTextView);

            // Add TableRow to TableLayout
            satelliteTable.addView(satelliteRow);
        }
    }

    private String getConstellationType(int constellationType){
        switch (constellationType){
            case GnssStatus.CONSTELLATION_GPS:
                return "GPS";
            case GnssStatus.CONSTELLATION_GLONASS:
                return "GLONASS";
            case GnssStatus.CONSTELLATION_BEIDOU:
                return "BEIDOU";
            case GnssStatus.CONSTELLATION_GALILEO:
                return "GALILEO";
            case GnssStatus.CONSTELLATION_IRNSS:
                return "IRNSS";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        }
    }


    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
        String currentTime = sdf.format(new Date());
        timeTextview.setText("Time: " + currentTime);
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
               Toast.makeText(this, "Settings!", Toast.LENGTH_LONG).show();


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
        Toast.makeText(MainActivity.this, "GPS Stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double altitude = location.getAltitude();


            latTextview.setText("Latitude: " + latitude);
            longTextview.setText("Longitude: " + longitude);
            altTextview.setText(""+altitude);
        }
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("GPS is disabled. Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
