package org.rickosborne.tubetastic.android;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

public class GameBoard {

    private int rowCount = 0;
    private int colCount = 0;
    private BaseTile[][] board;
    private boolean settled = false;
    private int score = 0;
    private float tileSize = 0;
    private boolean ready = false;

    public GameBoard(int rowCount, int colCount) {
        init(rowCount, colCount);
    }

    private void init(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        board = new BaseTile[rowCount][colCount];
        BaseTile tile;
        resize();
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 0; colNum < colCount; colNum++) {
                if (colNum == 0) {
                    tile = new SourceTile(colNum, rowNum, xForColNum(colNum), yForRowNum(rowNum), tileSize, this);
                }
                else if (colNum == colCount - 1) {
                    tile = new SinkTile(colNum, rowNum, xForColNum(colNum), yForRowNum(rowNum), tileSize, this);
                }
                else {
                    tile = new TubeTile(colNum, rowNum, xForColNum(colNum), yForRowNum(rowNum), tileSize, this);
                }
                board[rowNum][colNum] = tile;
            }
        }
        settled = false;
        score = 0;
        powerSweep();
    }

    public void resize() {

    }

    public float xForColNum(int colNum) {
        return colNum * tileSize;
    }

    public float yForRowNum(int rowNum) {
        return rowNum * tileSize;
    }

    private void powerSweep() {
        BaseTile tile;
        int rowNum;
        int colNum;
        ArrayList<BaseTile> neighbors;
        int maxTileCount = rowCount * colCount;
        int sinkColNum = colCount - 1;
        int lastRowNum = rowCount - 1;
        ready = false;
        ArrayDeque<BaseTile> toCheck = new ArrayDeque<BaseTile>(maxTileCount);
        for (rowNum = lastRowNum; rowNum >= 0; rowNum--) {
            toCheck.add(board[rowNum][0]);
        }
        HashSet<BaseTile> sourced   = new HashSet<BaseTile>(maxTileCount);
        HashSet<BaseTile> sunk      = new HashSet<BaseTile>(maxTileCount);
        HashSet<BaseTile> neither   = new HashSet<BaseTile>(maxTileCount);
        HashSet<BaseTile> connected = new HashSet<BaseTile>(maxTileCount);
        int points = 0;
        for (rowNum = 0; rowNum < rowCount; rowNum++) {
            for (colNum = 0; colNum < colCount; colNum++) {
                neither.add(board[rowNum][colNum]);
            }
        }
        // first, find all of the sourced tiles
        while (toCheck.size() > 0) {
            tile = toCheck.pop();
            if (!tile.isSourced()) {
                tile.setPower(BaseTile.Power.SOURCED);
            }
            sourced.add(tile);
            neither.remove(tile);
            if (tile instanceof SinkTile) {
                connected.add(tile);
            }
            for (BaseTile neighbor : tile.getConnectedNeighbors()) {
                if (!sourced.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
        }
        // then, find all of the sunk tiles that are not sourced
        for (rowNum = lastRowNum; rowNum >= 0; rowNum--) {
            tile = board[sinkColNum][rowNum];
            if (!sourced.contains(tile)) {
                toCheck.add(tile);
            }
        }
        while (toCheck.size() > 0) {
            tile = toCheck.pop();
            if (!tile.isSunk()) {
                tile.setPower(BaseTile.Power.SUNK);
                sunk.add(tile);
                neither.remove(tile);
            }
            for (BaseTile neighbor : tile.getConnectedNeighbors()) {
                if (!sourced.contains(neighbor) && !sunk.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
        }
        // reset any leftover tiles
        for (BaseTile unpowered : neither) {
            if (!unpowered.isUnpowered()) {
                unpowered.setPower(BaseTile.Power.NONE);
            }
        }
        // gather any connected tiles
        toCheck.addAll(connected);
        if (toCheck.isEmpty()) {
            ready = true;
            settled = true;
        }
        ArrayList<TubeTile> toVanish = new ArrayList<TubeTile>((colCount - 2) * rowCount);
        int toVanishCount = 0;
        while (toCheck.size() > 0) {
            tile = toCheck.pop();
            connected.add(tile);
            for (BaseTile neighbor : tile.getConnectedNeighbors()) {
                if (!connected.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
            if (tile instanceof TubeTile) {
                toVanish.add((TubeTile) tile);
                toVanishCount++;
            }
        }
        points += toVanishCount;
        for (TubeTile vanishTile : toVanish) {
            // vanishTile.vanish();
        }
        if (settled) {
            score += points;
        }
    }

    public BaseTile tileAt(int colNum, int rowNum) {
        if ((colNum >= 0) && (rowNum >= 0) && (colNum < colCount) && (rowNum < rowCount)) {
            return board[rowNum][colNum];
        }
        return null;
    }

    public boolean isReady() { return ready; }

    public boolean isSettled() { return settled; }

}
