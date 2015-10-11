package com.gcielniak.beacontracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnScanListener {

    String TAG = "MainActivity";
    BluetoothScanner bluetooth_scanner;
    File settings_file;
    BufferedReader beacon_settings;
    ArrayList<Beacon> beacons;
    List<Scan> current_scan;
    TextView info_view;
    String info_view_format = "a=%s p=%.2f %.2f %.2f v=%.0f";
    Scan current_estimate;
    MapView map_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        map_view = new MapView(this);
        setContentView(map_view);

        current_estimate = new Scan();

        info_view = (TextView) findViewById(R.id.info_text_view);

        bluetooth_scanner = new BluetoothScanner(this);

        beacons = new ArrayList<>();
        current_scan = new ArrayList<>();

        try {
            settings_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "beacon_settings.txt");
            beacon_settings = new BufferedReader(new FileReader(settings_file));
            beacon_settings.readLine();
            String str;

            while ((str=beacon_settings.readLine())!=null && str.length()!=0) {
                String[] res = str.split(" ");
                Beacon beacon = new Beacon();
                beacon.mac_address = res[0].substring(2);
                beacon.x = Double.parseDouble(res[1].substring(2));
                beacon.y = Double.parseDouble(res[2]);
                beacon.z = Double.parseDouble(res[3]);
                Log.i(TAG, beacon.toString());
                beacons.add(beacon);
            }
        } catch (IOException exc) {
            Log.i("TAG", "Error opening file: " + settings_file.getAbsolutePath());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        bluetooth_scanner.Start();
    }

    @Override
    public void onPause() {
        super.onPause();
        bluetooth_scanner.Stop();
    }

    @Override
    public void onScan(Scan scan) {
        double alpha = 0.3;

        for (Beacon b : beacons) {
            if (b.mac_address.equals(scan.mac_address)) {
                //add scan to list
                int indx = current_scan.indexOf(scan);
                if (indx != -1) {
                    if (current_scan.get(indx).timestamp == scan.timestamp)
                        return;
                    scan.value = (scan.value * alpha) + (1 - alpha)*current_scan.get(indx).value;
                    current_scan.set(indx, scan);
                }
                else
                    current_scan.add(scan);

                //max beacon
                Scan max_scan = new Scan();
                max_scan.value = -1000;

                for (Scan s: current_scan) {
                    if (s.value > max_scan.value)
                        max_scan = s;
                }

                map_view.invalidate();
//                Log.i(TAG,"Current scan: " + current_scan.size());

                for (Beacon b_max : beacons) {
                    if (b_max.mac_address.equals(max_scan.mac_address)) {
                        current_estimate.translation[0] = b_max.x;
                        current_estimate.translation[1] = b_max.y;
                        current_estimate.value = max_scan.value;
                    }
                }

                float value_threshold = -75;

                //average position
                float total_value = 0;

                for (Scan s: current_scan) {
                    if (s.value > value_threshold)
                        total_value += s.value;
                }

                current_estimate.value = max_scan.value;
                current_estimate.translation[0] = 0;
                current_estimate.translation[1] = 0;

                for (Scan s: current_scan) {
                    for (Beacon bb : beacons) {
                        if (bb.mac_address.equals(s.mac_address)) {
                            if (s.value > value_threshold) {
                                current_estimate.translation[0] += bb.x*s.value/total_value;
                                current_estimate.translation[1] += bb.y*s.value/total_value;
                            }
                        }
                    }
                }
            }
        }
    }

    class MapView extends View {

        public MapView(Context context) {
            super(context);
        }

        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();
            Paint paint = new Paint();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            paint.setColor(Color.argb(255, 128, 128, 128));

            float x_min = Float.MAX_VALUE;
            float x_max = Float.MIN_VALUE;
            float y_min = Float.MAX_VALUE;
            float y_max = Float.MIN_VALUE;

            for (Beacon b : beacons) {
                if (b.x < x_min)
                    x_min = (float)b.x;
                else if (b.x > x_max)
                    x_max = (float)b.x;
                if (b.y < y_min)
                    y_min = (float)b.y;
                else if (b.y > y_max)
                    y_max = (float)b.y;
            }

            float b_width = x_max - x_min;
            float b_height = y_max - y_min;

            x_min -= b_width*0.1;
            x_max += b_width*0.1;
            y_min -= b_height*0.1;
            y_max += b_height*0.1;
            b_width = x_max - x_min;
            b_height = y_max - y_min;
            float ratio = b_width;
            if (b_height > ratio)
                ratio = b_height;

            float radius = 10;

            for (Beacon b : beacons) {
                float cx = width*((float)b.x-x_min)/ratio;
                float cy = height*((float)b.y-y_min)/ratio;
                canvas.drawCircle(cx, cy, radius, paint);
            }

            for (Scan s : current_scan) {
                for (Beacon b : beacons) {
                    if (b.mac_address.equals(s.mac_address)) {
                        float strength = Math.abs(-120-(float)s.value)/200;
                        radius = (1-strength)*150;
                        int alpha = (int)(strength*255);
                        paint.setColor(Color.argb(alpha, 255, 0, 0));
                        float cx = width*((float)b.x-x_min)/ratio;
                        float cy = height*((float)b.y-y_min)/ratio;
                        canvas.drawCircle(cx, cy, radius, paint);
                    }
                }
            }

            if (current_estimate.value > -75) {
                radius = 40;
                paint.setColor(Color.argb(255, 0, 255, 0));
                float cx = width*((float)current_estimate.translation[0]-x_min)/ratio;
                float cy = height*((float)current_estimate.translation[1]-y_min)/ratio;
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }
    }
}
