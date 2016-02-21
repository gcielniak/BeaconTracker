package com.gcielniak.beacontracker;

import android.os.AsyncTask;
import android.os.Environment;
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
    FileScanReceiver receiver;

    FileScanner(OnScanListener listener) {
        receiver = new FileScanReceiver(listener);
    }

    void Start() {
        receiver.execute();
    }

    private class FileScanReceiver extends AsyncTask<Void, Void, Void> {

        OnScanListener listener;
        File file;
        BufferedReader reader;

        FileScanReceiver(OnScanListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "wifi_bt_log_20151021T115435.0035.txt");
                Log.i("FILE", file.getAbsolutePath());
                reader = new BufferedReader(new FileReader(file));
                reader.readLine();
                String str;

                while ((str=reader.readLine())!=null && str.length()!=0) {
                    Log.i("File", str);
                    Thread.sleep(1000);
                }
            }
            catch (IOException exc) {
                Log.i("File", "Error opening file: " + file.getAbsolutePath());
            }
            catch (InterruptedException exc) {

            }
            return null;
        }
    }
}
