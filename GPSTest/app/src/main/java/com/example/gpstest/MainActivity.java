package com.example.gpstest;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //variables for notifications
    private static final String CHANNEL_ID = "mychannel";
    private static final int NOTIFICATION_ID = 1;
    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Button toggleGpsButton;

    private boolean isGpsStarted = false;
    private boolean hasLocationPermission = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //calling functions for enabling notification
        createNotificationChannel();
        checkNotificationPermissionAndNotify();


        toggleGpsButton = findViewById(R.id.toggleGpsButton);
        toggleGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGpsStarted) {
                    startGps();
                } else {
                    stopGps();
                }
            }
        });


    }


    private void startGps() {
        isGpsStarted = true;
        toggleGpsButton.setText("Stop GPS");
        if (checkLocationPermission()) {
            hasLocationPermission = true;
            Log.d("startgps","Location permissions has been given ");
        } else {
            Log.d("startgps","Requesting for location permissions");
            requestLocationPermission();
        }
        Log.d("Startgps","GPS updates started successfully");
    }

    private void stopGps() {
        isGpsStarted = false;
        Log.d("stopgps","GPS updates stopped successfully");
        toggleGpsButton.setText("Start GPS");
    }

    private boolean checkLocationPermission() {
        Log.d("checkLocationPermission","Checking location permissions");
        return ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestLocationPermission() {
        Log.d("requestLocationPermission","Requesting for location permissions");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE
        );
    }
    private void checkNotificationPermissionAndNotify() {
    Log.d("checkNotificationPermissionAndNotify","Checking Notification Permissions");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_POST_NOTIFICATIONS);
        } else {
            showNotification();
        }
    } else {
        showNotification();
    }
}
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("onRequestPermissionsResult","Show Notification");
                showNotification();
            } else {
                // Handle the case where permission is denied
                Toast.makeText(this, "Notification permission is required to show notifications", Toast.LENGTH_LONG).show();
                Log.d("onRequestPermissionsResult","guide the user to the app settings");
                guideUserToAppSettings();

            }
        }
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermission = true;
            } else {
                // Permission denied
                Log.d("onRequestPermissionsResult","Location permission denied");
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void guideUserToAppSettings() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage("Notification permission was denied. You can enable it in app settings.")
                .setPositiveButton("App Settings", (dialog, which) -> {
                    // Intent to open app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    Log.d("guideUserToAppSettings","Notification permission was denied. You can enable it in app settings");

                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("createNotificationChannel","Notification Channel is created");
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void showNotification() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle("GPSTest App")
                .setContentText("App Running in Background")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        Log.d("showNotification","Show the explicit notification in App bar");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }



}
