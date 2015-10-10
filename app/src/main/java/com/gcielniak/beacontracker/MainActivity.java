package com.gcielniak.beacontracker;

import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnScanListener {

    String TAG = "MainActivity";
    BluetoothScanner bluetooth_scanner;
    File settings_file;
    BufferedReader beacon_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetooth_scanner = new BluetoothScanner(this);

        try {
            settings_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "beacon_settings.txt");
            beacon_settings = new BufferedReader(new FileReader(settings_file));
            beacon_settings.readLine();
            String str;

            while ((str=beacon_settings.readLine())!=null && str.length()!=0) {
                String[] res = str.split(" ");
                String mac_address = res[0].substring(2);
                double x = Double.parseDouble(res[1].substring(2));
                double y = Double.parseDouble(res[2]);
                double z = Double.parseDouble(res[3]);
                Log.i(TAG,mac_address + ": x=" + x + " y=" + y + " z=" + z);
            }
        } catch (IOException exc) {
            Log.i("TAG", "Error opening file: " + settings_file.getAbsolutePath());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
//        bluetooth_scanner.Start();
    }

    @Override
    public void onPause() {
        super.onPause();
        bluetooth_scanner.Stop();
    }

    @Override
    public void onScan(Scan scan) {
        Log.i(TAG,scan.toString());
    }
}
