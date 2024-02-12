package com.example.gpstest;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private long startTime;
    private List<Long> ttffList;
    private int coldStartCount;




    private ToggleButton startGPSButton;
    private Button clearGPSButton, startTTFFButton;
    private TextView latTextview, longTextview, ttffTextview,timeTextview;
    private TextView altTextview, ehvTextview,
            altMslTextview, satsTextview,
            speedTextview, bearingTextview, sAccTextview,
            bAccTextview, pDopTextview, hvDopTextview;

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



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Initialize location listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
               // updateLocation(location);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double altitude = location.getAltitude();



                latTextview.setText("Latitude: " + latitude);
                Log.d("TTFF", "Latitude " + latitude);


                longTextview.setText("Longitude: " + longitude);
                Log.d("TTFF", "Longitude: " + longitude);


                altTextview.setText(""+altitude);
                Log.d("TTFF", "altitude" + altitude);

                // Calculate TTFF
                long currentTime = System.currentTimeMillis();
                long ttff = (currentTime - startTime)/1000;
                String ttffval = String.valueOf(ttff);

                ttffTextview.setText("TTFF:"+ttffval+"s");

                Log.d("TTFF", "Time to First Fix (TTFF): " + ttffval + " seconds");
                // Continue listening for location updates
                startTime = currentTime;

                // Once we receive a location update, we can stop listening for further updates
              locationManager.removeUpdates(this);

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

}
    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
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
                Log.d("Location","Allow location permissions");

                startLocationUpdates();
            } else {
                Toast.makeText(this,
                        "Location permission denied", Toast.LENGTH_SHORT).show();
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
