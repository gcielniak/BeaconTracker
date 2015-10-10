package com.gcielniak.beacontracker;

/**
 * Created by gcielniak on 10/10/2015.
 */
public class Pose {
    double translation[];
    double rotation[];

    Pose() {
        translation = new double[3];
        rotation = new double[4];
        rotation[3] = 1.0;
    }
}
