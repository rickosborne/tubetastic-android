package org.rickosborne.tubetastic.android;

import android.util.SparseArray;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseTile {

    private static final class OutletOffset {
        public int col = 0;
        public int row = 0;
    }

    public static enum Power {
        NONE,
        SOURCED,
        SUNK
    }

    public static final String NORTH = "N";
    public static final String SOUTH = "S";
    public static final String EAST = "E";
    public static final String WEST = "W";
    public static final int directionCount = 4;
    public static final int[] outletDegrees = new int[]{0, 90, 180, 270};
    public static final String[] outletDirections = new String[]{
            BaseTile.NORTH,
            BaseTile.EAST,
            BaseTile.SOUTH,
            BaseTile.WEST
    };
    public static final SparseArray<String> directionFromDegrees;
    public static final SparseArray< HashMap< String, String > > outletRotationsReverse;
    public static final SparseArray<OutletOffset> outletOffsets;
    public static final double padding = 1 / 16;

    static {
        // sweet baby Jesus, Java needs more literals
        directionFromDegrees = new SparseArray<String>(directionCount);
        for (int i = 0; i < directionCount; i++) {
            directionFromDegrees.put(outletDegrees[i], outletDirections[i]);
        }
        outletRotationsReverse = new SparseArray<HashMap<String, String>>(directionCount);
        outletOffsets = new SparseArray<OutletOffset>(directionCount);
        for (int degrees : outletDegrees) {
            HashMap<String, String> submap = new HashMap<String, String>(directionCount);
            for (int rotated : outletDegrees) {
                String endDirection = directionFromDegrees.get(rotated);
                Integer offset = (rotated + degrees) % 360;
                String startDirection = directionFromDegrees.get(offset);
                submap.put(startDirection, endDirection);
            }
            outletRotationsReverse.put(degrees, submap);
            OutletOffset offset = new OutletOffset();
            switch (degrees) {
                case   0: offset.col =  0; offset.row = -1; break;
                case  90: offset.col =  1; offset.row =  0; break;
                case 180: offset.col =  0; offset.row =  1; break;
                case 270: offset.col = -1; offset.row =  0; break;
            }
            outletOffsets.put(degrees, offset);
        }
    }

    public static int makeId (int colNum, int rowNum) {
        return (colNum * 1000) + rowNum;
    }

    public static int degreesFromDirection (String direction) {
        for (int i = 0; i < directionCount; i++) {
            if (outletDirections[i].equals(direction)) {
                return outletDegrees[i];
            }
        }
        return 0;
    }

    protected int colNum = 0;
    protected int rowNum = 0;
    protected float x = 0;
    protected float y = 0;
    protected float size = 0;
    protected GameBoard board = null;
    protected Power power = Power.NONE;
    protected int id = makeId(0, 0);
    protected SparseBooleanArray outlets = new SparseBooleanArray(directionCount);
    protected float rotation = 0;
    protected int outletRotation = 0;
    protected float midpoint = 0;

    public BaseTile(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        init(colNum, rowNum, x, y, size, board);
    }

    protected void init(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        this.colNum = colNum;
        this.rowNum = rowNum;
        this.x = x;
        this.y = y;
        this.size = size;
        this.board = board;
        this.id = makeId(colNum, rowNum);
        for (int degrees : outletDegrees) {
            this.outlets.put(degrees, false);
        }
        this.resize(x, y, size);
    }

    public boolean hasOutletTo(int degrees) {
        return this.outlets.get(degrees, false);
    }

    public boolean hasOutletTo(String direction) {
        return hasOutletTo(degreesFromDirection(direction));
    }

    public List<BaseTile> getConnectedNeighbors() {
        ArrayList<BaseTile> connected = new ArrayList<BaseTile>(directionCount);
        for (int degrees : outletDegrees) {
            if (hasOutletTo(degrees)) {
                connected.add(neighborAt(degrees));
            }
        }
        return connected;
    }

    public BaseTile neighborAt(int degrees) {
        OutletOffset offset = outletOffsets.get(degrees);
        return board.tileAt(colNum + offset.col, rowNum + offset.row);
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public boolean isSourced() {
        return (this.power == Power.SOURCED);
    }

    public boolean isSunk() {
        return (this.power == Power.SUNK);
    }

    public boolean isUnpowered() {
        return (this.power == Power.NONE);
    }

    protected void resize(float x, float y, float size) {
        midpoint = size * 0.5f;
        this.x = x + midpoint;
        this.y = y + midpoint;
    }

}
