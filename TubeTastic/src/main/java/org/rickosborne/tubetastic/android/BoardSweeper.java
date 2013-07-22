package org.rickosborne.tubetastic.android;

import android.util.Log;

import java.util.*;

public class BoardSweeper {

    public static class DroppedTile {
        public TubeTile tile;
        public int colNum;
        public int rowNum;
        public DroppedTile(TubeTile tile, int colNum, int rowNum) {
            this.tile  = tile;
            this.colNum = colNum;
            this.rowNum = rowNum;
        }
    }

    public HashSet<BaseTile> sourced;
    public HashSet<BaseTile> sunk;
    public HashSet<BaseTile> neither;
    public HashSet<BaseTile> connected;
    public int maxTileCount;

    public BoardSweeper(int maxTileCount) {
        // Log.d("BoardSweeper", String.format("maxTileCount:%d", maxTileCount));
        this.maxTileCount = maxTileCount;
        sourced   = new HashSet<BaseTile>(maxTileCount);
        sunk      = new HashSet<BaseTile>(maxTileCount);
        neither   = new HashSet<BaseTile>(maxTileCount);
        connected = new HashSet<BaseTile>(maxTileCount);
        reset();
    }

    public GameBoard.TileChangeSet sweep(GameBoard board) {
        // Log.d("BoardSweeper", "sweep");
        GameBoard.TileChangeSet changes = new GameBoard.TileChangeSet(maxTileCount);
        reset();
        resetNeither(board, changes);
        trackSourced(board, changes);
        trackSunk(board, changes);
        trackUnpowered(board, changes);
        trackVanishes(board, changes);
        if (!connected.isEmpty()) {
            trackDrops(board, changes);
        }
        return changes;
    }

    protected void resetNeither(GameBoard board, GameBoard.TileChangeSet changes) {
        int colCount = board.getColCount();
        int rowCount = board.getRowCount();
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 0; colNum < colCount; colNum++) {
                neither.add(board.getTile(colNum, rowNum));
            }
        }
    }
    protected void trackSourced(GameBoard board, GameBoard.TileChangeSet changes) {
        int lastRowNum = board.getRowCount() - 1;
        int sourceColNum = 0;
        ArrayDeque<BaseTile> sourcedList = new ArrayDeque<BaseTile>(maxTileCount);
        for (int rowNum = lastRowNum; rowNum >= 0; rowNum--) {
            sourcedList.add(board.getTile(sourceColNum, rowNum));
        }
        int points = 0;
        // first, find all of the sourced tiles
        while (sourcedList.size() > 0) {
            BaseTile tile = sourcedList.pop();
            if (tile == null) {
                continue;
            }
            sourced.add(tile);
            neither.remove(tile);
            if (tile instanceof SinkTile) {
                connected.add(tile);
            } else if ((tile instanceof TubeTile) && !tile.isSourced()) {
                changes.powered.add(new GameBoard.TileChangePower((TubeTile) tile, Power.SOURCED));
            }
            for (BaseTile neighbor : tile.getConnectedNeighbors()) {
                if ((neighbor != null) && !sourced.contains(neighbor) && !sourcedList.contains(neighbor)) {
                    sourcedList.add(neighbor);
                }
            }
        }
    }
    protected void trackSunk(GameBoard board, GameBoard.TileChangeSet changes) {
        int lastRowNum = board.getRowCount() - 1;
        int sinkColNum = board.getColCount() - 1;
        ArrayDeque<BaseTile> sunkList = new ArrayDeque<BaseTile>(maxTileCount);
        // then, find all of the sunk tiles that are not sourced
        for (int rowNum = lastRowNum; rowNum >= 0; rowNum--) {
            BaseTile tile = board.getTile(sinkColNum, rowNum);
            if ((tile != null) && !sourced.contains(tile) && !sunkList.contains(tile)) {
                sunkList.add(tile);
            }
        }
        while (sunkList.size() > 0) {
            BaseTile tile = sunkList.pop();
            if (tile == null) {
                continue;
            }
            if (!tile.isSunk()) {
                changes.powered.add(new GameBoard.TileChangePower((TubeTile) tile, Power.SUNK));
//                    tile.setPower(BaseTile.Power.SUNK);
            }
            sunk.add(tile);
            neither.remove(tile);
            for (BaseTile neighbor : tile.getConnectedNeighbors()) {
                if ((neighbor != null) && !sourced.contains(neighbor) && !sunk.contains(neighbor) && !sunkList.contains(neighbor)) {
                    sunkList.add(neighbor);
                }
            }
        }
    }
    protected void trackUnpowered(GameBoard board, GameBoard.TileChangeSet changes) {
        // reset any leftover tiles
        for (BaseTile tile : neither) {
            if ((tile != null) && !tile.isUnpowered()) {
                changes.powered.add(new GameBoard.TileChangePower((TubeTile) tile, Power.NONE));
//                    tile.setPower(BaseTile.Power.NONE);
            }
        }
    }
    protected void trackVanishes(GameBoard board, GameBoard.TileChangeSet changes) {
        ArrayDeque<BaseTile> vanishList = new ArrayDeque<BaseTile>(maxTileCount);
        vanishList.addAll(connected);
        while (vanishList.size() > 0) {
            BaseTile tile = vanishList.pop();
            if (tile == null) {
                continue;
            }
            connected.add(tile);
            for (BaseTile neighbor : tile.getConnectedNeighbors()) {
                if ((neighbor != null) && !connected.contains(neighbor) && !vanishList.contains(neighbor)) {
                    vanishList.add(neighbor);
                }
            }
            if ((tile instanceof TubeTile)) {
                changes.vanished.add((TubeTile) tile);
            }
        }
    }
    protected void trackDrops(GameBoard board, GameBoard.TileChangeSet changes) {
        int rowCount = board.getRowCount();
        int colCount = board.getColCount();
        for (int colNum = 1; colNum <= colCount - 2; colNum++) {
            int destRowNum = rowCount;
            for (int rowNum = rowCount - 1; rowNum >= 0; rowNum--) {
                TubeTile tile = (TubeTile) board.getTile(colNum, rowNum);
                if ((tile != null) && !changes.vanished.contains(tile)) {
                    destRowNum--;
                    if (destRowNum > rowNum) {
                        changes.moved.add(new GameBoard.TileChangeMove(tile, colNum, rowNum, colNum, destRowNum));
                    }
                }
            }
            for (int rowNum = destRowNum - 1; rowNum >= 0; rowNum--) {
                changes.appeared.add(new GameBoard.TileChangeAppear(colNum, rowNum));
            }
        }
    }

    protected void reset() {
        sourced.clear();
        sunk.clear();
        neither.clear();
        connected.clear();
    }
}

