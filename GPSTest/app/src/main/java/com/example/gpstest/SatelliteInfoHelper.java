package com.example.gpstest;

import android.location.GnssStatus;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SatelliteInfoHelper {
    // Method to fetch satellite information

    public static void updateSatelliteInfo(GnssStatus gnssStatus, TableLayout satelliteTable) {
        try {
            Log.d("UPDATES", "Updating satellite information...");
            // Clear previous satellite information
            satelliteTable.removeViews(1, satelliteTable.getChildCount() - 1);

            // Iterate through each satellite
            int satelliteCount = gnssStatus.getSatelliteCount();
            Log.d("UPDATES", "Total satellites: " + satelliteCount);

            for (int i = 0; i < satelliteCount; i++) {
                // Retrieve satellite information
                int prn = gnssStatus.getSvid(i);
                int constellationType = gnssStatus.getConstellationType(i);
                float cn0 = gnssStatus.getCn0DbHz(i);
                float elevation = gnssStatus.getElevationDegrees(i);
                float azimuth = gnssStatus.getAzimuthDegrees(i);

                //flag status
                String flagStatus = estimateFlagStatus(constellationType, cn0, elevation, azimuth);

                if (elevation % 2 == 0) {
                    // Create a new TableRow to hold satellite data
                    TableRow satelliteRow = new TableRow(satelliteTable.getContext());

                    // Create TextViews for satellite data
                    TextView idTextView = new TextView(satelliteTable.getContext());
                    idTextView.setText(String.valueOf(prn));
                    satelliteRow.addView(idTextView);

                    TextView gnssTextView = new TextView(satelliteTable.getContext());
                    gnssTextView.setText(getConstellationType(constellationType));
                    satelliteRow.addView(gnssTextView);

                    TextView cfTextView = new TextView(satelliteTable.getContext());
                    // Display estimated CF value
                    cfTextView.setText(estimateCarrierFrequency(prn, constellationType));
                    satelliteRow.addView(cfTextView);


                    TextView cnoTextView = new TextView(satelliteTable.getContext());
                    // Display C/No if available
                    cnoTextView.setText(cn0 > 0 ? String.valueOf(cn0) : "");
                    satelliteRow.addView(cnoTextView);

                    TextView flagStatusTextView = new TextView(satelliteTable.getContext());
                    flagStatusTextView.setText(flagStatus);
                    satelliteRow.addView(flagStatusTextView);

                    TextView elevTextView = new TextView(satelliteTable.getContext());
                    // Display elevation with "°" symbol
                    elevTextView.setText(String.valueOf(elevation) + "°");
                    satelliteRow.addView(elevTextView);

                    TextView azimTextView = new TextView(satelliteTable.getContext());
                    // Display azimuth if available
                    azimTextView.setText(azimuth > 0 ? String.valueOf(azimuth) + "°" : "");
                    satelliteRow.addView(azimTextView);

                    // Add TableRow to TableLayout
                    satelliteTable.addView(satelliteRow);
                }
            }
        } catch (Exception e) {
            // Handle the exception here
            Log.e("UPDATES", "Error updating satellite information: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Estimates the flag status for a satellite based on its constellation type, C/N0, elevation, and azimuth.
     *
     * @param constellationType The constellation type of the satellite.
     * @param cn0               The carrier-to-noise density ratio (C/N0) of the satellite signal.
     * @param elevation         The elevation angle of the satellite in degrees.
     * @param azimuth           The azimuth angle of the satellite in degrees.
     * @return The flag status of the satellite (A for healthy, AE for unhealthy, AU for unknown).
     */
    private static String estimateFlagStatus(int constellationType,
                                             float cn0, float elevation, float azimuth) {
        try {
            switch (constellationType) {
                case GnssStatus.CONSTELLATION_GPS:
                    // For GPS, assume healthy if C/N0 > 40
                    // and elevation > 15 degrees
                    if (cn0 > 40 && elevation > 15) {
                        Log.d("flag", "A - healthy satellite");
                        return "A"; // "A" for healthy

                    } else {
                        Log.d("flag", "AE - Unhealthy satellite");
                        return "AE"; // "AE" for unhealthy
                    }
                case GnssStatus.CONSTELLATION_GLONASS:
                    // For GLONASS, assume healthy if C/N0 > 35
                    // and elevation > 10 degrees
                    if (cn0 > 35 && elevation > 10) {
                        Log.d("flag", "A - healthy satellite");
                        return "A"; // "A" for healthy

                    } else {
                        Log.d("flag", "AE - Unhealthy satellite");
                        return "AE"; // "AE" for unhealthy
                    }
                case GnssStatus.CONSTELLATION_BEIDOU:
                    // For BEIDOU, assume healthy if C/N0 > 38
                    // and azimuth between 30 and 330 degrees
                    if (cn0 > 38 && azimuth > 30 && azimuth < 330) {
                        Log.d("flag", "A - healthy satellite");
                        return "A"; // "A" for healthy
                    } else {
                        Log.d("flag", "AE - Unhealthy satellite");
                        return "AE"; // "AE" for unhealthy
                    }
                case GnssStatus.CONSTELLATION_GALILEO:
                    // For GALILEO, assume healthy if C/N0 > 42 and elevation > 20 degrees
                    if (cn0 > 42 && elevation > 20) {
                        Log.d("flag", "A - healthy satellite");
                        return "A"; // "A" for healthy
                    } else {
                        Log.d("flag", "AE - Unhealthy satellite");
                        return "AE"; // "AE" for unhealthy
                    }
                default:
                    // For other constellations,
                    Log.d("flag", "AU - Unknown health");
                    // return "AU" for unknown health status
                    return "AU"; // "AU" for unknown health status
            }
        } catch (Exception e) {
            Log.e("Flag", "Exception: " + e.getMessage());
            e.printStackTrace();
            return "Unknown"; // Return a default value in case of an exception
        }
    }


    /**
     * Method to estimate CF value in terms of L1, L2, L5, etc.
     * based on PRN and constellation type
     *
     * @returns types of signals
     */
    private static String estimateCarrierFrequency(int prn, int constellationType) {
        try {
            // Define frequency bands for different constellations
            String cf;
            switch (constellationType) {
                case GnssStatus.CONSTELLATION_GPS:
                    // For GPS, even PRN => L1, odd PRN => L2
                    cf = (prn % 2 == 0) ? "L1" : "L2";
                    Log.d("Carrier Frequency", "CF : " + cf);
                    break;
                case GnssStatus.CONSTELLATION_GLONASS:
                    // For GLONASS, L1 = 1602 + 0.5625 * (prn - 64),
                    // L2 = 1246 + 0.4375 * (prn - 64)
                    cf = (prn > 64) ? "L1" : "L2";
                    Log.d("Carrier Frequency", "CF : " + cf);
                    break;
                case GnssStatus.CONSTELLATION_GALILEO:
                    // For GALILEO, E1
                    cf = "E1";
                    Log.d("Carrier Frequency", "CF : " + cf);
                    break;
                case GnssStatus.CONSTELLATION_BEIDOU:
                    // For BEIDOU, B1
                    cf = "B1";
                    Log.d("Carrier Frequency", "CF : " + cf);
                    break;
                case GnssStatus.CONSTELLATION_QZSS:
                    // For QZSS, L1
                    cf = "L1";
                    Log.d("Carrier Frequency", "CF : " + cf);
                    break;
                case GnssStatus.CONSTELLATION_SBAS:
                    // For SBAS, L1
                    cf = "L1";
                    Log.d("Carrier Frequency", "CF : " + cf);
                    break;
                // Add cases for other constellations as needed
                default:
                    cf = "Unknown";
                    Log.d("Carrier Frequency", "Unknown CF");
            }
            return cf;
        } catch (Exception e) {
            Log.e("Carrier Frequency", "Exception: " + e.getMessage());
            e.printStackTrace();
            return "Unknown"; // Return a default value in case of an exception
        }
    }

    /**
     * @param constellationType
     * @return the type of satellite
     */

    private static String getConstellationType(int constellationType) {
        try {
            switch (constellationType) {
                case GnssStatus.CONSTELLATION_GPS:
                    Log.d("GNSS", "GPS - US");
                    return "GPS";
                case GnssStatus.CONSTELLATION_GLONASS:
                    Log.d("GNSS", "GLONASS - Russia");
                    return "GLONASS";
                case GnssStatus.CONSTELLATION_BEIDOU:
                    Log.d("GNSS", "BEIDOU - China");
                    return "BEIDOU";
                case GnssStatus.CONSTELLATION_GALILEO:
                    Log.d("GNSS", "GALILEO - Europe");
                    return "GALILEO";
                case GnssStatus.CONSTELLATION_IRNSS:
                    Log.d("GNSS", "IRNSS - India");
                    return "IRNSS";
                default:
                    return "UNKNOWN";
            }
        } catch (Exception e) {
            Log.e("GNSS", "Exception: " + e.getMessage());
            e.printStackTrace();
            return "Unknown"; // Return a default value in case of an exception
        }
    }
}
