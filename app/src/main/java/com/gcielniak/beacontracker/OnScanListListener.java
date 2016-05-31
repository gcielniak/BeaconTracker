package com.gcielniak.beacontracker;

import com.gcielniak.scannerlib.Reading;

import java.util.List;

/**
 * Created by gcielniak on 23/02/2016.
 */
public interface OnScanListListener {
    public void onScanList(List<Reading> scan_list);
}
