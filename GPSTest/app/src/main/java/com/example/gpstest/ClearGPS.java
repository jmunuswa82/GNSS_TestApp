package com.example.gpstest;

import android.widget.TableLayout;
import android.widget.TextView;

/**
 * Utility class for clearing GPS data from the specified TableLayout.
 */
public class ClearGPS {
    /**
     * Clears GPS data displayed in the specified TableLayout and TextViews.
     *
     * @param satelliteTable The TableLayout containing GPS data
     *                       to be cleared.
     *                       All child views (rows) of the table
     *                       will be removed.
     * @param textViewArray  Array of TextViews containing GPS data
     *                       to be cleared.
     *                       All text in these TextViews will be cleared.
     */
    public static void clearGPSData(TableLayout satelliteTable,
                                    TextView[] textViewArray) {
        // Clear fetched data in satellite table (start from index 1)
        satelliteTable.removeViews(1,
                satelliteTable.getChildCount() - 1);

        // Clear text in TextViews (start from index 1)
        for (int i = 1; i < textViewArray.length; i++) {
            textViewArray[i].setText("");
        }
    }
}