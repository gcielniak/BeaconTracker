package com.gcielniak.beacontracker;

/**
 * Type of device: currently Bluetooth beacons and Wifi
 * NO_DEVICE can be used for logging location data only (e.g with Tango)
 *
 */
enum DeviceType {
    BT_BEACON,
    WIFI_AP,
    NO_DEVICE
};

/**
 * A single scan (perhaps should be called Reading) which includes identity and location information.
 *
 */
public class Scan {
    public DeviceType device_type;
    public String name;
    public String mac_address;
    public long timestamp;
    public double value;
    public double[] translation;
    public double[] rotation;
    public UUID uuid;

    Scan() {
        translation = new double[3];
        rotation = new double[4];
        uuid = new UUID(new byte[20]);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Scan && this.mac_address.equals(((Scan) o).mac_address);
    }

    @Override
    public String toString() {

        String _name;
        if (name != null)
            _name = name;
        else
            _name = "";

        if (device_type == DeviceType.BT_BEACON) {
            return "BT: t=" + timestamp + " n=\"" + _name + "\" a=" + mac_address + " v=" + value +
                    " p=" + translation[0] + " " + translation[1] + " " + translation[2] +
                    " r=" + rotation[0] + " " + rotation[1] + " " + rotation[2] + " " + rotation[3] + " u=" + uuid;

        }
        else if (device_type == DeviceType.WIFI_AP) {
            return "WF: t=" + timestamp + " n=\"" + _name + "\" a=" + mac_address + " v=" + value +
                    " p=" + translation[0] + " " + translation[1] + " " + translation[2] +
                    " r=" + rotation[0] + " " + rotation[1] + " " + rotation[2] + " " + rotation[3];

        }
        else {
            return "ND: t=" + timestamp + " n=\"" + _name + "\" a=" + mac_address + " v=" + value +
                    " p=" + translation[0] + " " + translation[1] + " " + translation[2] +
                    " r=" + rotation[0] + " " + rotation[1] + " " + rotation[2] + " " + rotation[3];
        }
    }
};


