package org.rickosborne.tubetastic.android;

import android.util.SparseBooleanArray;

public class Outlets {

    public static final int BIT_NORTH = 8;
    public static final int BIT_EAST  = 4;
    public static final int BIT_SOUTH = 2;
    public static final int BIT_WEST  = 1;

    SparseBooleanArray outlets = new SparseBooleanArray(BaseTile.directionCount);
    private int bits;
    public Outlets() {
        setBits(0);
    }
    public Outlets(int bits) {
        setBits(bits);
    }
    public int getBits() { return bits; }
    public void setBits(int bits) {
        this.bits = bits;
        outlets.put(BaseTile.DEGREES_NORTH, (bits & BIT_NORTH) != 0);
        outlets.put(BaseTile.DEGREES_EAST , (bits & BIT_EAST ) != 0);
        outlets.put(BaseTile.DEGREES_SOUTH, (bits & BIT_SOUTH) != 0);
        outlets.put(BaseTile.DEGREES_WEST , (bits & BIT_WEST)  != 0);
    }
    public void setOutlet(int degrees, boolean hasOutlet) {
        outlets.put(degrees, hasOutlet);
        int bit = 0;
        switch (degrees) {
            case BaseTile.DEGREES_NORTH: bit = BIT_NORTH; break;
            case BaseTile.DEGREES_EAST : bit = BIT_EAST ; break;
            case BaseTile.DEGREES_SOUTH: bit = BIT_SOUTH; break;
            case BaseTile.DEGREES_WEST : bit = BIT_WEST ; break;
        }
        if (hasOutlet) {
            bits |= bit;
        }
        else if ((bits & bit) != 0) {
            bits -= bit;
        }
    }
    public boolean hasOutlet(int degrees) {
        return outlets.get(degrees, false);
    }
}
