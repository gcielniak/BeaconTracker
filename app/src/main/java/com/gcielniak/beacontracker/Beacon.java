package com.gcielniak.beacontracker;

/**
 * Created by gcielniak on 11/10/2015.
 */
public class Beacon {
    String mac_address;
    double x, y, z;
    Scan.UUID uuid;

    @Override
    public String toString() {
        return new String("a=" + mac_address + " x=" + x + " y=" + y + " z=" + z + " u=" + uuid);
    }
}
