package com.example.gpstest;
/**
 * Description: This file contains utility methods for handling satellite information.
 */
import android.location.GnssStatus;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Utility class for handling satellite information.
 * @returns satellite's details -
 * satellite's ID
 * GNSS(specifies the type of GNSS)
 * CF (signal)
 * C/No (quantify the strength and quality of the received signal)
 * Flags (Quality of the signal)
 * Elevation(Angle between the horizon, line connecting the satellite and the receiver)
 * Azimuth
 */
public class SatelliteUtils {
        // Method to fetch satellite information
        public static void fetchSatelliteInfo(GnssStatus gnssStatus, TableLayout satelliteTable) {
            // Call method from another file (e.g., SatelliteInfoHelper)
            Log.d("SatelliteUtil", "Calling the sub File SatelliteInfoHelper");
            SatelliteInfoHelper.updateSatelliteInfo(gnssStatus, satelliteTable);
        }

    public static void clearGPSData(TableLayout satelliteTable) {
        // Call clear function from ClearGPS
        ClearGPS.clearGPSData(satelliteTable);
    }
}


