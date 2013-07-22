package org.rickosborne.tubetastic.android;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;
import java.util.List;

public class BaseTile {

    private static final class OutletOffset {
        public int col = 0;
        public int row = 0;
    }

    public static final String DIRECTION_NORTH = "N";
    public static final String DIRECTION_SOUTH = "S";
    public static final String DIRECTION_EAST = "E";
    public static final String DIRECTION_WEST = "W";
    public static final int DEGREES_NORTH =   0;
    public static final int DEGREES_EAST  =  90;
    public static final int DEGREES_SOUTH = 180;
    public static final int DEGREES_WEST  = 270;
    public static final int directionCount = 4;
    public static final int[] outletDegrees = new int[]{DEGREES_NORTH, DEGREES_EAST, DEGREES_SOUTH, DEGREES_WEST};
    public static final String[] outletDirections = new String[]{DIRECTION_NORTH, DIRECTION_EAST, DIRECTION_SOUTH, DIRECTION_WEST};
    public static final SparseArray<String> directionFromDegrees;
    public static final SparseArray<OutletOffset> outletOffsets;
    public static final SparseArray<SparseIntArray> outletRotationsReverse;
    public static final SparseIntArray directionReverse;

    static {
        // sweet baby Jesus, Java needs more literals
        directionFromDegrees = new SparseArray<String>(directionCount);
        for (int i = 0; i < directionCount; i++) {
            directionFromDegrees.put(outletDegrees[i], outletDirections[i]);
        }
        outletRotationsReverse = new SparseArray<SparseIntArray>(directionCount);
        outletOffsets = new SparseArray<OutletOffset>(directionCount);
        for (int degrees : outletDegrees) {
            SparseIntArray submap = new SparseIntArray(directionCount);
            for (int rotated : outletDegrees) {
                int offset = (rotated + degrees) % 360;
                submap.put(offset, rotated);
            }
            outletRotationsReverse.put(degrees, submap);
            OutletOffset offset = new OutletOffset();
            switch (degrees) {
                case DEGREES_NORTH : offset.col =  0; offset.row = -1; break;
                case DEGREES_EAST  : offset.col =  1; offset.row =  0; break;
                case DEGREES_SOUTH : offset.col =  0; offset.row =  1; break;
                case DEGREES_WEST  : offset.col = -1; offset.row =  0; break;
            }
            outletOffsets.put(degrees, offset);
        }
        directionReverse = new SparseIntArray(directionCount);
        directionReverse.put(DEGREES_NORTH, DEGREES_SOUTH);
        directionReverse.put(DEGREES_EAST,  DEGREES_WEST);
        directionReverse.put(DEGREES_SOUTH, DEGREES_NORTH);
        directionReverse.put(DEGREES_WEST,  DEGREES_EAST);
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
    protected GameBoard board = null;
    protected Power power = Power.NONE;
    protected int id = makeId(0, 0);
    protected Outlets outlets = new Outlets();
    protected int outletRotation = 0;
    protected TileWatcher watcher;

    public BaseTile(int colNum, int rowNum, GameBoard board) {
        super();
        init(colNum, rowNum, board);
    }

    protected void init(int colNum, int rowNum, GameBoard board) {
        this.colNum = colNum;
        this.rowNum = rowNum;
        this.id = makeId(colNum, rowNum);
        this.board = board;
    }

    public String toString() {
        StringBuilder connected = new StringBuilder();
        for (int degrees : outletDegrees) {
            if (hasOutletTo(degrees)) {
                BaseTile neighbor = neighborAt(degrees);
                if ((neighbor != null) && neighbor.hasOutletTo(directionReverse.get(degrees, -1))) {
                    connected.append(String.format(" %s:%d:%d,%d", neighbor.power, degrees, neighbor.colNum, neighbor.rowNum));
                }
            }
        }
        return String.format("%s %d,%d %s %s", getClass().getSimpleName(), colNum, rowNum, outlets, connected);
    }

    public boolean hasOutletTo(int degrees) {
        boolean ret = false;
        SparseIntArray originalMap = outletRotationsReverse.get(outletRotation);
        if (originalMap != null) {
            int originalDegrees = originalMap.get(degrees, -1);
            if (originalDegrees > -1) {
                ret = this.outlets.hasOutlet(originalDegrees);
            } else {
                Log.e("baseTile", String.format("hasOutletTo(orig:? + rot:%d = %d) missing original degrees", outletRotation, degrees));
            }
        } else {
            Log.e("baseTile", String.format("hasOutletTo(orig:? + rot:%d = %d) missing reverse", outletRotation, degrees));
        }
        return ret;
    }

    public boolean hasOutletTo(String direction) {
        return hasOutletTo(degreesFromDirection(direction));
    }

    public List<BaseTile> getConnectedNeighbors() {
        ArrayList<BaseTile> connected = new ArrayList<BaseTile>(directionCount);
        for (int degrees : outletDegrees) {
            if (hasOutletTo(degrees)) {
                BaseTile neighbor = neighborAt(degrees);
                if ((neighbor != null) && neighbor.hasOutletTo(directionReverse.get(degrees, -1))) {
                    connected.add(neighbor);
                }
            }
        }
        return connected;
    }

    public BaseTile neighborAt(int degrees) {
        OutletOffset offset = outletOffsets.get(degrees);
        return board.getTile(colNum + offset.col, rowNum + offset.row);
    }

    public void setPower(Power power) {
        Power fromPower = this.power;
        if (fromPower != power) {
            this.power = power;
            if (watcher != null) {
                watcher.onTilePower(this, fromPower, power);
            }
        }
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

    public int getBits() { return outlets.getBits(); }
    public void setBits(int bits) { outlets.setBits(bits); }
    public void setColRow(int colNum, int rowNum) {
        int fromColNum = this.colNum;
        int fromRowNum = this.rowNum;
        if ((fromColNum != colNum) || (fromRowNum != rowNum)) {
            // // Log.d("BaseTile", String.format("setColRow %s to %d,%d", this, colNum, rowNum));
            this.colNum = colNum;
            this.rowNum = rowNum;
            this.id = makeId(colNum, rowNum);
            if (watcher != null) {
                watcher.onTileMove(this, fromColNum, fromRowNum, colNum, rowNum);
            }
        }
    }

    public void spin() {
        outletRotation += 90;
        outletRotation %= 360;
        // // Log.d("BaseTile", String.format("%s spin to %d", this, outletRotation));
        if (watcher != null) {
            watcher.onTileSpin(this);
        }
    }

    public void vanish() {
        if (watcher != null) {
            watcher.onTileVanish(this);
        }
    }

    public void setWatcher(TileWatcher watcher) {
        this.watcher = watcher;
    }

}











