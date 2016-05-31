package com.gcielniak.beacontracker;

import com.gcielniak.scannerlib.UUID;

/**
 * Beacon class including its identity (MAC address and UUID) and location (x, y, z)
 *
 */
public class Beacon {
    String mac_address;
    double x, y, z;
    UUID uuid;

    Beacon() {
        mac_address = "";
        uuid = new UUID("");
    }

    @Override
    public String toString() {
        return new String("a=" + mac_address + " x=" + x + " y=" + y + " z=" + z + " u=" + uuid);
    }
}
