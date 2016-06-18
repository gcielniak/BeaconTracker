package com.gcielniak.beacontracker;

import com.gcielniak.scannerlib.Reading;

import java.util.List;

public interface OnScanListener {
    void onScan(List<Reading> scan);
}
