package com.gcielniak.beacontracker;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by gcielniak on 21/02/2016.
 */
public class FileScanner {
    OnScanListener listener;
    FileScanReceiver receiver;
    boolean stop;
    BufferedReader reader;
    File file;

    FileScanner(OnScanListener listener) {
        this.listener = listener;
    }

    void SetFile(String file_name) {
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    file_name);
            reader = new BufferedReader(new FileReader(file));
            reader.readLine();
        }
        catch (IOException exc) {
            Log.i("File", "Error opening file: " + file.getAbsolutePath());
        }
    }

    void Start() {
        stop = false;
        try {
            if (reader != null)
                reader.reset();
            else
                return;
        }
        catch (IOException exc) {
            return;
        }
        new FileScanReceiver(listener).execute();
    }

    void Stop() {
        try {
            if (reader != null)
                reader.mark(0);
        }
        catch (IOException exc) {}
        stop = true;
    }

    private class FileScanReceiver extends AsyncTask<Void, Void, Void> {

        OnScanListener listener;

        FileScanReceiver(OnScanListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String str;

                long start_system_nanos = SystemClock.elapsedRealtimeNanos();
                long start_file_nanos = 0;

                while (!stop && (str=reader.readLine())!=null && str.length()!=0) {
                    String[] res = str.split(" ");
                    Scan scan = new Scan();
                    if (res[0].equals("BT:"))
                        scan.device_type = DeviceType.BT_BEACON;
                    else if (res[0].equals("WF:")) {
                        scan.device_type = DeviceType.WIFI_AP;

                    } else {
                        continue;
                    }

                    scan.timestamp = Long.parseLong(res[1].substring(2));
                    scan.name = res[2].substring(2, res[2].length() - 1);
                    scan.mac_address = res[3].substring(2);
                    scan.value = Double.parseDouble(res[4].substring(2));
                    scan.translation[0] = Double.parseDouble(res[5].substring(2));
                    scan.translation[1] = Double.parseDouble(res[6]);
                    scan.translation[2] = Double.parseDouble(res[7]);
                    scan.rotation[0] = Double.parseDouble(res[8].substring(2));
                    scan.rotation[1] = Double.parseDouble(res[9]);
                    scan.rotation[2] = Double.parseDouble(res[10]);
                    scan.rotation[3] = Double.parseDouble(res[11]);

                    if (start_file_nanos == 0)
                        start_file_nanos = scan.timestamp*1000;

                    while ((SystemClock.elapsedRealtimeNanos()-start_system_nanos) < (scan.timestamp*1000-start_file_nanos))
                        Thread.sleep(10);

                    listener.onScan(scan);
                }
            }
            catch (IOException exc) {
            }
            catch (InterruptedException exc) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
        }
    }
}
