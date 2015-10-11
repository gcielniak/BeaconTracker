package com.gcielniak.beacontracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gcielniak on 11/10/2015.
 */
public class NNTracker implements OnScanListener {
    ArrayList<Beacon> beacons;
    List<Scan> current_scan;
    float alpha;
    Scan current_estimate;

    NNTracker() {
        beacons = new ArrayList<>();
        current_scan = new ArrayList<>();
        current_estimate = new Scan();
        alpha = 1.0f;
    }

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

                //max beacon
                Scan max_scan = new Scan();
                max_scan.value = Float.MIN_VALUE;

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
                    }
                }
            }
        }
    }
}
