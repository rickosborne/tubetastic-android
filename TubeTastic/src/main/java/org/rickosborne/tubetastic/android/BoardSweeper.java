package org.rickosborne.tubetastic.android;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BoardSweeper {

    public static class DroppedTile {
        public TubeTile tile;
        public int colNum;
        public int rowNum;
        public float colX;
        public float rowY;
        public DroppedTile(TubeTile tile, int colNum, int rowNum, float colX, float rowY) {
            this.tile  = tile;
            this.colNum = colNum;
            this.rowNum = rowNum;
            this.colX   = colX;
            this.rowY   = rowY;
        }
    }

    public HashSet<BaseTile> sourced;
    public HashSet<BaseTile> sunk;
    public HashSet<BaseTile> neither;
    public HashSet<BaseTile> connected;
    public HashSet<TubeTile> vanished;
    public HashSet<DroppedTile> dropped;
    public HashSet<DroppedTile> added;
    public HashSet<TubeTile> fell;
    public List<TileChangePower> powered;
    public int maxTileCount;

    public static class TileChangePower {
        public BaseTile tile;
        public BaseTile.Power power;
        public TileChangePower(BaseTile tile, BaseTile.Power power) {
            this.tile  = tile;
            this.power = power;
        }
    }
//        private static class TileChangeMove extends TileChange {
//            public int colNum;
//            public int rowNum;
//            public TileChangeMove(BaseTile tile, int colNum, int rowNum) {
//                super(tile);
//                this.colNum = colNum;
//                this.rowNum = rowNum;
//            }
//        }
//        private static class TileChangeVanish extends TileChange {
//            public TileChangeVanish(BaseTile tile) {
//                super(tile);
//            }
//        }
    public BoardSweeper(int maxTileCount) {
        sourced   = new HashSet<BaseTile>(maxTileCount);
        sunk      = new HashSet<BaseTile>(maxTileCount);
        neither   = new HashSet<BaseTile>(maxTileCount);
        connected = new HashSet<BaseTile>(maxTileCount);
        vanished  = new HashSet<TubeTile>(maxTileCount);
        dropped   = new HashSet<DroppedTile>(maxTileCount);
        added     = new HashSet<DroppedTile>(maxTileCount);
        fell      = new HashSet<TubeTile>(maxTileCount);
        powered   = new ArrayList<TileChangePower>(maxTileCount);
        this.maxTileCount = maxTileCount;
        reset();
    }
    public void resetNeither(GameBoard board) {
        int colCount = board.getColCount();
        int rowCount = board.getRowCount();
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 0; colNum < colCount; colNum++) {
                neither.add(board.getTile(colNum, rowNum));
            }
        }
    }
    public void trackSourced(GameBoard board) {
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
//                Gdx.app.log("BoardSweeper", String.format("connected row:%d col:%d", tile.rowNum, tile.colNum));
                connected.add(tile);
            } else if ((tile instanceof TubeTile) && !tile.isSourced()) {
                powered.add(new TileChangePower(tile, BaseTile.Power.SOURCED));
            }
            for (BaseTile neighbor : tile.getConnectedNeighbors()) {
                if ((neighbor != null) && !sourced.contains(neighbor) && !sourcedList.contains(neighbor)) {
                    sourcedList.add(neighbor);
                }
            }
        }
    }
    public void trackSunk(GameBoard board) {
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
                powered.add(new TileChangePower(tile, BaseTile.Power.SUNK));
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
    public void trackUnpowered(GameBoard board) {
        // reset any leftover tiles
        for (BaseTile tile : neither) {
            if ((tile != null) && !tile.isUnpowered()) {
                powered.add(new TileChangePower(tile, BaseTile.Power.NONE));
//                    tile.setPower(BaseTile.Power.NONE);
            }
        }
    }
    public void trackVanishes(GameBoard board) {
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
                vanished.add((TubeTile) tile);
            }
        }
    }
    public void trackDrops(GameBoard board) {
        int rowCount = board.getRowCount();
        int colCount = board.getColCount();
        for (int colNum = 1; colNum <= colCount - 2; colNum++) {
            int destRowNum = rowCount;
            float colX = board.xForColNum(colNum);
            for (int rowNum = rowCount - 1; rowNum >= 0; rowNum--) {
                TubeTile tile = (TubeTile) board.getTile(colNum, rowNum);
                if ((tile != null) && !vanished.contains(tile)) {
                    destRowNum--;
                    if (destRowNum > rowNum) {
                        dropped.add(new DroppedTile(tile, colNum, destRowNum, colX, board.yForRowNum(destRowNum)));
                    }
                }
            }
//            Gdx.app.log("BoardSweeper", String.format("trackDrops col:%d fillrows:%d", colNum, destRowNum));
            for (int rowNum = destRowNum - 1; rowNum >= 0; rowNum--) {
//                    toDropCount++;
//                    TubeTile tile = new TubeTile(-2, -2, colX, yForRowNum(rowNum - destRowNum), tileSize, this);
                added.add(new DroppedTile(null, colNum, rowNum, colX, board.yForRowNum(rowNum - destRowNum)));
//                    addActor(tile);
            }
        }
//        Gdx.app.log("BoardSweeper", String.format("trackDrops drop:%d add:%d", dropped.size(), added.size()));
    }

    public void reset() {
        sourced.clear();
        sunk.clear();
        neither.clear();
        connected.clear();
        powered.clear();
        dropped.clear();
        added.clear();
        vanished.clear();
        fell.clear();
    }
}

