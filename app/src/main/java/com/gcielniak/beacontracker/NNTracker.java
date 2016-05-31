package com.gcielniak.beacontracker;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.gcielniak.scannerlib.OnReadingListener;
import com.gcielniak.scannerlib.Reading;
import com.gcielniak.scannerlib.UUID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gcielniak on 11/10/2015.
 */
public class NNTracker implements OnReadingListener {
    String TAG = "NNTracker";
    List<Beacon> beacons;
    List<Reading> current_scan;
    List<Integer> beacon_history;
    float alpha;
    int N;
    long timeout_period;

    Reading current_estimate;
    OnReadingListener scan_listener;
    OnScanListListener scan_list_listener;
    File settings_file;
    BufferedReader beacon_settings;

    NNTracker(OnReadingListener scan_listener, OnScanListListener scan_list_listener) {
        this.scan_listener = scan_listener;
        this.scan_list_listener = scan_list_listener;
        beacons = new ArrayList<Beacon>();
        current_scan = new ArrayList<Reading>();
        beacon_history = new ArrayList<Integer>();
        current_estimate = new Reading();
        alpha = 1.0f;
        N = 1;
        timeout_period = 1000000;//1 s.

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
                    beacon.uuid = new UUID(id);
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

    boolean IdentifyBeacon(Reading scan) {

        for (Beacon b: beacons) {
            if (b.mac_address.equals(scan.getMacAddress())) {
                return true;
            }
            else if (b.uuid.equals(scan.uuid)) {
                b.mac_address = scan.getMacAddress();//update beacon's mac address if not present
                return true;
            }
        }

        return false;
    }

    void UpdateCurrentScan(Reading scan) {

        long current_timestamp = SystemClock.elapsedRealtimeNanos() / 1000;

        int indx = current_scan.indexOf(scan);
        if (indx != -1) { //update the current scan
            scan.value = (scan.value * alpha) + (1 - alpha) * current_scan.get(indx).value;
            current_scan.set(indx, scan);
        } else //add new reading
            current_scan.add(scan);

        //check for old scans and remove
        for (int i = current_scan.size(); i > 0; i--) {
            if ((current_timestamp - current_scan.get(i-1).timestamp) > timeout_period)
                current_scan.remove(i-1);
        }

        scan_list_listener.onScanList(current_scan);
    }

    void NNEstimate(Reading scan) {
        //max beacon count from last N scans

        //max beacon
        Reading max_scan = new Reading();
        max_scan.value = Float.NEGATIVE_INFINITY;

        //find the max value
        for (Reading s : current_scan) {
            if (s.value > max_scan.value)
                max_scan = s;
        }

        int beacon_id = -1;
        //find the corresponding beacon id
        for (int i = 0; i < beacons.size(); i++) {
            if (beacons.get(i).mac_address.equals(max_scan.getMacAddress())) {
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
        scan_listener.onReading(current_estimate);
    }

    @Override
    public void onReading(Reading scan) {

        Log.i("NNTRACKER ONSCAN: ",scan.toString());

        if (IdentifyBeacon(scan)) {
            UpdateCurrentScan(scan);
            NNEstimate(scan);
        }

    }
}
