package org.rickosborne.tubetastic.android;

import android.util.SparseArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;
import java.util.List;

public class BaseTile extends Actor {

    private static final class OutletOffset {
        public int col = 0;
        public int row = 0;
    }

    public static enum Power {
        NONE,
        SOURCED,
        SUNK
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
    public static final SparseArray< SparseArray< Integer > > outletRotationsReverse;
    public static final SparseArray<Integer> directionReverse;
    public static final Color COLOR_ARC = new Color(0.933333f, 0.933333f, 0.933333f, 1.0f);
    public static final Color COLOR_POWER_NONE    = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    public static final Color COLOR_POWER_SUNK    = new Color(1.0f, 0.6f, 0f, 1.0f);
    public static final Color COLOR_POWER_SOURCED = new Color(0f, 0.6f, 1.0f, 1.0f);
    public static final float SIZE_PADDING = 1f / 16f;
    public static final float SIZE_ARCWIDTH = 1f / 12f;
    private TileRenderer renderer;

    static {
        // sweet baby Jesus, Java needs more literals
        directionFromDegrees = new SparseArray<String>(directionCount);
        for (int i = 0; i < directionCount; i++) {
            directionFromDegrees.put(outletDegrees[i], outletDirections[i]);
        }
        outletRotationsReverse = new SparseArray<SparseArray<Integer>>(directionCount);
        outletOffsets = new SparseArray<OutletOffset>(directionCount);
        for (int degrees : outletDegrees) {
            SparseArray<Integer> submap = new SparseArray<Integer>(directionCount);
            for (int rotated : outletDegrees) {
                int offset = (rotated + degrees) % 360;
                submap.put(offset, rotated);
            }
            outletRotationsReverse.put(-degrees, submap);
            OutletOffset offset = new OutletOffset();
            switch (degrees) {
                case DEGREES_NORTH : offset.col =  0; offset.row = -1; break;
                case DEGREES_EAST  : offset.col =  1; offset.row =  0; break;
                case DEGREES_SOUTH : offset.col =  0; offset.row =  1; break;
                case DEGREES_WEST  : offset.col = -1; offset.row =  0; break;
            }
            outletOffsets.put(degrees, offset);
        }
        directionReverse = new SparseArray<Integer>(directionCount);
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

    public static Color arcShadow(Power power) {
        switch (power) {
            case NONE:    return COLOR_POWER_NONE;
            case SOURCED: return COLOR_POWER_SOURCED;
            case SUNK:    return COLOR_POWER_SUNK;
        }
        return null;
    }

    protected int colNum = 0;
    protected int rowNum = 0;
    protected GameBoard board = null;
    protected Power power = Power.NONE;
    protected int id = makeId(0, 0);
    protected Outlets outlets = new Outlets();
    protected int outletRotation = 0;
    protected float midpoint = 0f;
    protected float alpha = 0f;
    protected float padding = 0;

    public BaseTile(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super();
        init(colNum, rowNum, x, y, size, board);
    }

    protected void init(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        this.colNum = colNum;
        this.rowNum = rowNum;
        this.id = makeId(colNum, rowNum);
        this.board = board;
        this.resize(x, y, size);
    }

    public String toString() {
        return String.format("%s %d,%d (%.0f,%.0f/%.0fx%.0f) %s", getClass().getSimpleName(), colNum, rowNum, getX(), getY(), getWidth(), getHeight(), outlets);
    }

    public boolean hasOutletTo(int degrees) {
        boolean ret = false;
        SparseArray<Integer> originalMap = outletRotationsReverse.get(outletRotation);
        if (originalMap != null) {
            Integer originalDegrees = originalMap.get(degrees);
            if (originalDegrees != null) {
                ret = this.outlets.hasOutlet(originalDegrees);
//                Gdx.app.log(toString(), String.format("hasOutletTo(orig:%d + rot:%d = %d) = %b", originalDegrees, outletRotation, degrees, ret));
            } else {
                Gdx.app.error(toString(), String.format("hasOutletTo(orig:? + rot:%d = %d) missing original degrees", outletRotation, degrees));
            }
        } else {
            Gdx.app.error(toString(), String.format("hasOutletTo(orig:? + rot:%d = %d) missing reverse", outletRotation, degrees));
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
        return board.tileAt(colNum + offset.col, rowNum + offset.row);
    }

    public void setPower(Power power) {
//        Gdx.app.log(toString(), String.format("power %s -> %s", this.power, power));
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
        midpoint = MathUtils.floor(size * 0.5f);
        setBounds((int) x, (int) y, midpoint * 2, midpoint * 2);
        setOrigin(midpoint, midpoint);
        padding = MathUtils.floor(size * SIZE_PADDING);
    }

    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = alpha; }
    public int getBits() { return outlets.getBits(); }
    public void setBits(int bits) { outlets.setBits(bits); }
    public void setColRow(int colNum, int rowNum) {
        this.colNum = colNum;
        this.rowNum = rowNum;
        this.id = makeId(colNum, rowNum);
    }

    public void setRenderer(TileRenderer renderer) { this.renderer = renderer; }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.draw(renderer.getTextureRegionForTile(this), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}











