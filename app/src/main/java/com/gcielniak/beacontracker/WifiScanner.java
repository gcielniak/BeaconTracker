package com.gcielniak.beacontracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by gcielniak on 04/10/2015.
 */
public class WifiScanner {
    Pose current_pose;
    WifiManager wifi;
    WifiScanReceiver receiver;
    Context context;

    WifiScanner(Context context, OnScanListener listener) {
        this.context = context;
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        receiver = new WifiScanReceiver(listener);
        current_pose = new Pose();
    }

    public void UpdatePose(Pose current_pose) {
        this.current_pose = current_pose;
    }

    public void Start() {
        context.registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.startScan();
    }

    public void Stop() {
        context.unregisterReceiver(receiver);
    }

    private class WifiScanReceiver extends BroadcastReceiver {

        OnScanListener listener;

        WifiScanReceiver(OnScanListener listener) {
            this.listener = listener;
        }

        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();

            for (int i = 0; i < wifiScanList.size(); i++) {
                ScanResult result = wifiScanList.get(i);

                Scan scan = new Scan();
                scan.device_type = DeviceType.WIFI_AP;
                scan.mac_address = result.BSSID.toUpperCase();
                scan.name = result.SSID;
                scan.timestamp = result.timestamp;
                scan.value = (double) result.level;
                scan.translation = current_pose.translation;
                scan.rotation = current_pose.rotation;

                listener.onScan(scan);
            }
            wifi.startScan();
        }
    }
}
