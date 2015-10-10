package com.gcielniak.beacontracker;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gcielniak on 04/10/2015.
 */
public class ScanLogger implements OnScanListener
{
    private List<Scan> current_scan;
    File log_file;
    FileWriter log_file_writer;
    private static final String TAG = "ScanLogger";

    ScanLogger() {
        current_scan = new ArrayList<Scan>();
    }

    public void Start() {
        //log file
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss.ssss");
            Date current_date = new Date();
            log_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "wifi_bt_log_" + sdf.format(current_date) + ".txt");
            log_file_writer = new FileWriter(log_file, false);
            log_file_writer.write(sdf.format(current_date) + "=" + SystemClock.elapsedRealtimeNanos() / 1000 + '\n');
        } catch (IOException exc) {
            Log.i("TAG", "Error opening file: " + log_file.getAbsolutePath());
        }
    }

    public void Stop(Context context) {
        //log file
        try {
            log_file_writer.close();
        } catch (IOException exc) {
            Log.i("TAG", "Error opening the file: " + log_file.getAbsolutePath());
        }

        MediaScannerConnection.scanFile(context,
                new String[]{log_file.getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });

    }

    @Override
    public void onScan(Scan scan) {
        int indx = current_scan.indexOf(scan);
        if (indx != -1) {
            if (current_scan.get(indx).timestamp == scan.timestamp)
                return;
            current_scan.remove(indx);
        }
        current_scan.add(scan);

        Log.i(TAG, scan.toString());

        if (log_file_writer != null) {
            try {
                log_file_writer.write(scan + "\n");
            } catch (IOException exc) {
                Log.i(TAG, "Error writing to file.");
            }
        }
    }
}
