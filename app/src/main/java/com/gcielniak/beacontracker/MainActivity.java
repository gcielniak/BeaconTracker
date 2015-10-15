package com.gcielniak.beacontracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnScanListener, OnScanListListener{

    String TAG = "MainActivity";
    BluetoothScanner bluetooth_scanner;
    NNTracker bluetooth_tracker;
    List<Scan> current_scan;
    Scan current_estimate;
    MapView map_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        map_view = new MapView(this);
        setContentView(map_view);

        bluetooth_tracker = new NNTracker(this, this);
        bluetooth_scanner = new BluetoothScanner(bluetooth_tracker);
    }

    @Override
    public void onResume() {
        super.onResume();
        map_view.onResume();
        bluetooth_scanner.Start();
    }

    @Override
    public void onPause() {
        super.onPause();
        bluetooth_scanner.Stop();
    }

    @Override
    public void onScan(Scan scan) {
        current_estimate = scan;
        map_view.invalidate();
    }

    @Override
    public void onScanList(List<Scan> scan_list) {
        current_scan = scan_list;
        map_view.invalidate();
    }

    class MapView extends View {
        int width, height;
        float x_min, x_max, y_min, y_max;
        float b_width, b_height, ratio;

        public MapView(Context context) {
            super(context);
        }

        public void onResume() {
            x_min = Float.POSITIVE_INFINITY;
            x_max = Float.NEGATIVE_INFINITY;
            y_min = Float.POSITIVE_INFINITY;
            y_max = Float.NEGATIVE_INFINITY;

            List<Beacon> beacons = bluetooth_tracker.beacons;

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

            b_width = x_max - x_min;
            b_height = y_max - y_min;
            double padd = b_width*0.1;
            if (b_height*0.1 > padd)
                padd = b_height*0.1;

            x_min -= padd;
            x_max += padd;
            y_min -= padd;
            y_max += padd;
            b_width = x_max - x_min;
            b_height = y_max - y_min;
            ratio = b_width;
            if (b_height > ratio) {
                ratio = b_height;
            }

        }

        float getX(float x) {
            return width*(x-x_min-b_width/2)/ratio + width/2;
        }

        float getY(float y) {
            return height*(y-y_min)/ratio;
        }

        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            width = getWidth();
            height = getHeight();
            Paint paint = new Paint();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            paint.setColor(Color.argb(255, 128, 128, 128));

            float radius = 10;

            List<Beacon> beacons = bluetooth_tracker.beacons;

            for (Beacon b : beacons) {
                canvas.drawCircle(getX((float)b.x), getY((float)b.y), radius, paint);
            }

            if (current_scan != null) {
                for (Scan s : current_scan) {
                    for (Beacon b : beacons) {
                        if (b.mac_address.equals(s.mac_address)) {
                            float strength = Math.abs(-120-(float)s.value)/200;
                            radius = (1-strength)*150;
                            int alpha = (int)(strength*255);
                            paint.setColor(Color.argb(alpha, 255, 0, 0));
                            float cx = width*((float)b.x-x_min)/ratio;
                            float cy = height*((float)b.y-y_min)/ratio;
                            canvas.drawCircle(getX((float)b.x), getY((float)b.y), radius, paint);
                        }
                    }
                }
            }

            if (current_estimate != null) {
                radius = 40;
                paint.setColor(Color.argb(255, 0, 255, 0));
                canvas.drawCircle(getX((float)current_estimate.translation[0]), getY((float)current_estimate.translation[1]), radius, paint);
            }
        }
    }
}
