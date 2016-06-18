package com.gcielniak.beacontracker;

import com.gcielniak.scannerlib.BluetoothScanner;
import com.gcielniak.scannerlib.FileScanner;
import com.gcielniak.scannerlib.OnReadingListener;
import com.gcielniak.scannerlib.Reading;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnReadingListener, OnScanListener{

    String TAG = "MainActivity";
    BluetoothScanner bluetooth_scanner;//bt scanner
    FileScanner file_scanner;//file scanner
    NNTracker bluetooth_tracker;
    List<Reading> current_scan;
    Reading current_estimate;
    MapView map_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        map_view = new MapView(this);
        setContentView(map_view);

        bluetooth_tracker = new NNTracker(this, this);
        bluetooth_scanner = new BluetoothScanner(bluetooth_tracker);
        bluetooth_tracker.alpha = 0.5f;//take an average of the new and old reading
        file_scanner = new FileScanner(bluetooth_tracker);
        file_scanner.SetFile("test.txt");
    }

    @Override
    public void onResume() {
        super.onResume();
        map_view.onResume();
        if (!bluetooth_scanner.Start())
            Toast.makeText(getApplicationContext(), "Bluetooth not enabled!",
                    Toast.LENGTH_SHORT).show();

        file_scanner.Start();
    }

    @Override
    public void onPause() {
        super.onPause();
        bluetooth_scanner.Stop();
        file_scanner.Stop();
    }

    @Override
    public void onReading(Reading reading) {
        current_estimate = reading;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map_view.invalidate();
            }
        });
    }

    @Override
    public void onScan(List<Reading> scan) {
        current_scan = scan;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map_view.invalidate();
            }
        });
    }

    class MapView extends View {
        int width, height;
        float x_min, x_max, y_min, y_max;
        float b_width, b_height, ratio;
        Paint paint;

        public MapView(Context context) {

            super(context);
            paint = new Paint();
        }

        public void onResume() {
            //recalculate view coordinates

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

            double padd = b_width*0.05;
            if (b_height*0.05 > padd)
                padd = b_height*0.05;

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

        float getX(double x) {
            return (float)(width*(x-x_min-b_width/2)/ratio + width/2);
        }

        float getY(double y) { return (float)(height - (height*(y-y_min)/ratio)); }

        /**
         * Draw beacons, current scan (all beacons in the range) and current estimate of the position
         */
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            width = getWidth();
            height = getHeight();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            paint.setColor(Color.argb(255, 128, 128, 128));

            float radius = 5;

            List<Beacon> beacons = bluetooth_tracker.beacons;

            for (Beacon b : beacons) {
                canvas.drawCircle(getX(b.x), getY(b.y), radius, paint);
            }

            if (current_scan != null) {
                for (Reading s : current_scan) {
                    for (Beacon b : beacons) {
                        if ((b.mac_address != null) && b.mac_address.equals(s.getMacAddress())) {
                            double strength = Math.min(Math.abs(-120-s.value)/80, 1.0);
                            strength *= strength;
                            radius = (float)(1-strength)*50;
                            int alpha = (int)(strength*255*0.5);
                            paint.setColor(Color.argb(alpha, 255, 0, 0));
                            canvas.drawCircle(getX(b.x), getY(b.y), radius, paint);
                        }
                    }
                }
            }
            if (current_estimate != null) {
                radius = 20;
                paint.setColor(Color.argb(180, 0, 255, 0));
                canvas.drawCircle(getX(current_estimate.translation[0]), getY(current_estimate.translation[1]), radius, paint);
            }
        }
    }
}
