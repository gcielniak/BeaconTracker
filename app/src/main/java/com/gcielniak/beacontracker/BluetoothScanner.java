package com.gcielniak.beacontracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.SystemClock;

/**
 * Created by gcielniak on 04/10/2015.
 */
public class BluetoothScanner {
    Pose current_pose;
    BluetoothAdapter adapter;
    BluetoothScanReceiver receiver;

    BluetoothScanner(OnScanListener listener) {
        adapter = BluetoothAdapter.getDefaultAdapter();
        receiver = new BluetoothScanReceiver(listener);
        current_pose = new Pose();
    }

    public void UpdatePose(Pose current_pose) {
        this.current_pose = current_pose;
    }

    public void Start() {
        adapter.startLeScan(receiver);
    }

    public void Stop() {
        adapter.stopLeScan(receiver);
    }

    private class BluetoothScanReceiver implements BluetoothAdapter.LeScanCallback {

        OnScanListener listener;

        BluetoothScanReceiver(OnScanListener listener) {
            this.listener = listener;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            Scan scan = new Scan();
            scan.device_type = DeviceType.BT_BEACON;
            scan.mac_address = device.getAddress();
            scan.name = device.getName();
            scan.timestamp = SystemClock.elapsedRealtimeNanos() / 1000;
            scan.value = (double) rssi;
            scan.translation = current_pose.translation;
            scan.rotation = current_pose.rotation;

            listener.onScan(scan);
        }
    }
}
