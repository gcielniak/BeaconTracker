package com.gcielniak.beacontracker;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gcielniak on 11/10/2015.
 */
public class NNTracker implements OnScanListener {
    String TAG = "NNTracker";
    List<Beacon> beacons;
    List<Scan> current_scan;
    float alpha;
    Scan current_estimate;
    OnScanListener scan_listener;
    OnScanListListener scan_list_listener;
    File settings_file;
    BufferedReader beacon_settings;

    NNTracker(OnScanListener scan_listener, OnScanListListener scan_list_listener) {
        this.scan_listener = scan_listener;
        this.scan_list_listener = scan_list_listener;
        beacons = new ArrayList<>();
        current_scan = new ArrayList<>();
        current_estimate = new Scan();
        alpha = 1.0f;
        LoadSettings();
    }

    public void LoadSettings() {
        try {
            settings_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "beacon_settings.txt");
            beacon_settings = new BufferedReader(new FileReader(settings_file));
            beacon_settings.readLine();
            String str;

            while ((str=beacon_settings.readLine())!=null && str.length()!=0) {
                String[] res = str.split(" ");
                Beacon beacon = new Beacon();
                beacon.mac_address = res[0].substring(2);
                beacon.x = Double.parseDouble(res[1].substring(2));
                beacon.y = Double.parseDouble(res[2]);
                beacon.z = Double.parseDouble(res[3]);
                beacons.add(beacon);
            }
        } catch (IOException exc) {
            Log.i(TAG, "Error opening file: " + settings_file.getAbsolutePath());
        }
    }

    List<Beacon> Beacons() { return beacons; }

    @Override
    public void onScan(Scan scan) {
        for (Beacon b : beacons) {
            if (b.mac_address.equals(scan.mac_address)) {
                //add scan to list
                int indx = current_scan.indexOf(scan);
                if (indx != -1) {
                    if (current_scan.get(indx).timestamp == scan.timestamp)
                        return;
                    scan.value = (scan.value * alpha) + (1 - alpha) * current_scan.get(indx).value;
                    current_scan.set(indx, scan);
                } else
                    current_scan.add(scan);

                scan_list_listener.onScanList(current_scan);

                //max beacon
                Scan max_scan = new Scan();
                max_scan.value = Float.NEGATIVE_INFINITY;

                for (Scan s : current_scan) {
                    if (s.value > max_scan.value)
                        max_scan = s;
                }

                for (Beacon b_max : beacons) {
                    if (b_max.mac_address.equals(max_scan.mac_address)) {
                        current_estimate.translation[0] = b_max.x;
                        current_estimate.translation[1] = b_max.y;
                        current_estimate.translation[2] = b_max.z;
                        current_estimate.value = max_scan.value;
                        scan_listener.onScan(current_estimate);
                        return;
                    }
                }

                scan_listener.onScan(null);
            }
        }
    }
}
