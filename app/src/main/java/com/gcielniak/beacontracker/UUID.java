package com.gcielniak.beacontracker;

import java.util.Arrays;

/**
 * Proximity UUID structure
 *
 */
public class UUID {
    byte[] data;

    UUID(byte[] data) { this.data = data; }

    UUID(byte[] data, int major, int minor) {
        byte[] major_minor = new byte[4];
        major_minor[0] = (byte)(major & 0xFF00 >> 8);
        major_minor[1] = (byte)(major & 0xFF);
        major_minor[2] = (byte)(minor & 0xFF00 >> 8);
        major_minor[3] = (byte)(minor & 0xFF);

        this.data = new byte[data.length + major_minor.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
        System.arraycopy(major_minor, 0, this.data, data.length, major_minor.length);
    }

    UUID(String input) {
        this.data = parseHexBinary(input);
    }

    public byte[] parseHexBinary(String s) {
        final int len = s.length();

        // "111" is not a valid hex encoding.
        if( len%2 != 0 )
            throw new IllegalArgumentException("hexBinary needs to be even-length: "+s);

        byte[] out = new byte[len/2];

        for( int i=0; i<len; i+=2 ) {
            int h = hexToBin(s.charAt(i  ));
            int l = hexToBin(s.charAt(i+1));
            if( h==-1 || l==-1 )
                throw new IllegalArgumentException("contains illegal character for hexBinary: "+s);

            out[i/2] = (byte)(h*16+l);
        }

        return out;
    }

    private static int hexToBin( char ch ) {
        if( '0'<=ch && ch<='9' )    return ch-'0';
        if( 'A'<=ch && ch<='F' )    return ch-'A'+10;
        if( 'a'<=ch && ch<='f' )    return ch-'a'+10;
        return -1;
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02X", b & 0xff));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Scan) {
            UUID uuid = (UUID)o;

            if ((this.data == null) || (uuid.data == null) || (this.data.length != uuid.data.length))
                return false;

            for (int i = 0; i < this.data.length; i++)
                if (this.data[i] != uuid.data[i])
                    return false;

            return true;
        }

        return false;
    }

    public String toString() {
        if (data != null)
            return byteArrayToHex(this.data);
        else
            return "";
    }

    public String toStringUUID() {
        if (data != null)
            return byteArrayToHex(Arrays.copyOf(this.data, 16));
        else
            return "";
    }

    public String toStringMajor() {
        if (data != null) {
            int major = ((this.data[16] << 8) & 0xFF00) | (this.data[17] & 0xFF);
            return Integer.toString(major);
        }
        else
            return "";
    }

    public String toStringMinor() {
        if (data != null) {
            int major = ((this.data[18] << 8) & 0xFF00) | (this.data[19] & 0xFF);
            return Integer.toString(major);
        }
        else
            return "";
    }
}
