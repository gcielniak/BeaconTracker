package com.gcielniak.beacontracker;

/**
 * Created by gcielniak on 10/10/2015.
 */
enum DeviceType {
    BT_BEACON,
    WIFI_AP
};

public class Scan {
    public DeviceType device_type;
    public String name;
    public String mac_address;
    public long timestamp;
    public double value;
    public double[] translation;
    public double[] rotation;

    Scan() {
        translation = new double[3];
        rotation = new double[4];
    }

    public boolean equals(Object o) {
        if (o instanceof Scan)
            return this.mac_address.equals(((Scan) o).mac_address);

        return false;
    }

    @Override
    public String toString() {

        String _name = new String();
        if (name != null)
            _name = name;

        if (device_type == DeviceType.BT_BEACON)
            return new String("BT: t=" + timestamp + " n=\"" + _name + "\" a=" + mac_address + " v=" + value +
                    " p=" + translation[0] + " " + translation[1] + " " + translation[2] +
                    " r=" + rotation[0] + " " + rotation[1] + " " + rotation[2] + " " + rotation[3]);
        else
            return new String("WF: t=" + timestamp + " n=\"" + _name + "\" a=" + mac_address + " v=" + value +
                    " p=" + translation[0] + " " + translation[1] + " " + translation[2] +
                    " r=" + rotation[0] + " " + rotation[1] + " " + rotation[2] + " " + rotation[3]);
    }
};


