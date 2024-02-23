package com.example.gpstest;

import android.location.GnssStatus;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * Utility class for fetching satellite information and clearing GPS data.
 */

public class SatelliteUtils {
    /**
     * Fetches satellite information using the provided
     * GnssStatus object and updates the satellite table.
     *
     * @param gnssStatus     The GnssStatus object containing
     *                       satellite information.
     * @param satelliteTable The TableLayout where the satellite
     *                       information will be displayed.
     *                       The table should have at least one TableRow
     *                       as its child, which serves as the header.
     *                       The method will delegate the task to another
     *                       class (e.g., SatelliteInfoHelper) to handle
     *                       the actual retrieval and display of satellite information.
     */

    public static void fetchSatelliteInfo(GnssStatus gnssStatus,
                                          TableLayout satelliteTable) {
            // Call method from another file (e.g., SatelliteInfoHelper)
            Log.d("SatelliteUtil", "Calling the sub File SatelliteInfoHelper");
            SatelliteInfoHelper.getSatelliteInfo(gnssStatus, satelliteTable);
        }


    /**
     * Clears GPS data displayed in the specified TableLayout.
     *
     * @param satelliteTable The TableLayout containing GPS data to be cleared.
     *                       The method will delegate the task to another class
     *                       (e.g., ClearGPS) to handle
     *                       the actual clearing of GPS data.
     */

    public static void clearGPSData(TableLayout satelliteTable,
                                    TextView[] textViewArray) {
        // Call clear function from ClearGPS
        Log.d("SatelliteUtil", "Calling the sub File ClearGPS");
        ClearGPS.clearGPSData(satelliteTable, textViewArray);
    }


}


