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
    List<Integer> beacon_history;
    float alpha;
    int N;
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
        beacon_history = new ArrayList<>();
        current_estimate = new Scan();
        alpha = 1.0f;
        N = 1;
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
                String id = res[0].substring(2).toUpperCase();
                if (id.length() == 17)
                    beacon.mac_address = id;
                 else if (id.length() == 40)
                    beacon.uuid = new Scan.UUID(id);
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
        int beacon_id = -1;

        for (int i = 0; i < beacons.size(); i++) {
            Beacon b = beacons.get(i);
            if (((b.mac_address != null) && b.mac_address.equals(scan.mac_address)) ||
                    ((b.uuid != null) && b.uuid.equals(scan.uuid))) {
                beacon_id = i;
                break;
            }
        }

        if (beacon_id == -1)
            return;

        if (beacons.get(beacon_id).mac_address == null)
            beacons.get(beacon_id).mac_address = scan.mac_address;

        //update the scan
        int indx = current_scan.indexOf(scan);
        if (indx != -1) {
            if (current_scan.get(indx).timestamp == scan.timestamp)
                return;
            scan.value = (scan.value * alpha) + (1 - alpha) * current_scan.get(indx).value;
            current_scan.set(indx, scan);
        } else
            current_scan.add(scan);

        scan_list_listener.onScanList(current_scan);

        //max beacon count from last N scans

        //max beacon
        Scan max_scan = new Scan();
        max_scan.value = Float.NEGATIVE_INFINITY;

        //find the max value
        for (Scan s : current_scan) {
            if (s.value > max_scan.value)
                max_scan = s;
        }

        //find the corresponding beacon id
        for (int i = 0; i < beacons.size(); i++) {
            if ((beacons.get(i).mac_address != null) && beacons.get(i).mac_address.equals(max_scan.mac_address)) {
                beacon_id = i;
                break;
            }
        }

        //update the beacon history
        beacon_history.add(beacon_id);
        if (beacon_history.size() > N)
            beacon_history.remove(0);

        //count number of beacon ids in the history list
        int b_count[] = new int[beacons.size()];

        for (int i = 0; i < beacon_history.size(); i++)
            b_count[beacon_history.get(i)] += 1;

        //select a beacon with the highest count
        int max_index = 0;
        int max_count = 0;

        for (int i = 0; i < b_count.length; i++)
            if (b_count[i] > max_count) {
                max_count = b_count[i];
                max_index = i;
            }

        //update estimate
        Beacon b_max = beacons.get(max_index);
        current_estimate.translation[0] = b_max.x;
        current_estimate.translation[1] = b_max.y;
        current_estimate.translation[2] = b_max.z;
        current_estimate.value = max_scan.value;
        scan_listener.onScan(current_estimate);
    }
}
