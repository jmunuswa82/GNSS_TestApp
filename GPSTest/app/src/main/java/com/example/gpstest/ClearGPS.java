package com.example.gpstest;

import android.widget.TableLayout;

/**
 * Utility class for clearing GPS data from the specified TableLayout.
 */
public class ClearGPS {
    /**
     * Clears GPS data displayed in the specified TableLayout.
     *
     * @param satelliteTable The TableLayout containing GPS data to be cleared.
     *                       All child views (rows) of the table will be removed.
     */
    public static void clearGPSData(TableLayout satelliteTable) {
            // Clear satellite table
            satelliteTable.removeAllViews();
        }
}
