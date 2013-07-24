package org.rickosborne.tubetastic.android;

import android.util.Log;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.HashSet;
import java.util.Set;

public class GameBoard {

    public static enum TILE_TYPE {
        SOURCE,
        TUBE,
        SINK
    }

    public interface Watcher {
//        public void onBoardChanged(TileChangeSet changes);
        public void onScoreChanged(int fromScore, int toScore);
    }

    public static class TileChangeSet {
        public Set<TileChangeMove> moved;
        public Set<TileChangePower> powered;
        public Set<TubeTile> vanished;
        public Set<TileChangeAppear> appeared;
        public boolean isRandom = false;
        public TileChangeSet(int maxTileCount) {
            moved = new HashSet<TileChangeMove>(maxTileCount);
            powered = new HashSet<TileChangePower>(maxTileCount);
            vanished = new HashSet<TubeTile>(maxTileCount);
            appeared = new HashSet<TileChangeAppear>(maxTileCount);
        }
    }
    public static class TileChange {
        public BaseTile tile;
        public TileChange(BaseTile tile) {
            this.tile = tile;
        }
    }
    public static class TileChangePower extends TileChange {
        public Power power;
        public TileChangePower(TubeTile tile, Power power) {
            super(tile);
            this.power = power;
        }
    }
    public static class TileChangeMove extends TileChange {
        public int fromColNum;
        public int fromRowNum;
        public int toColNum;
        public int toRowNum;
        public TileChangeMove(TubeTile tile, int fromColNum, int fromRowNum, int toColNum, int toRowNum) {
            super(tile);
            this.fromColNum = fromColNum;
            this.fromRowNum = fromRowNum;
            this.toColNum = toColNum;
            this.toRowNum = toRowNum;
        }
    }
    public static class TileChangeAppear {
        public int colNum;
        public int rowNum;
        public TileChangeAppear(int colNum, int rowNum) {
            this.colNum = colNum;
            this.rowNum = rowNum;
        }
    }

    private int rowCount = 0;
    private int colCount = 0;
    private BaseTile[][] board;
    private int score = 0;
    private BoardSweeper sweeper;
    protected Watcher watcher;
    protected boolean settled = false;

    public GameBoard(int colCount, int rowCount) {
        super();
        init(colCount, rowCount);
    }

    private void init(int colCount, int rowCount) {
        // Log.d("GameBoard", String.format("init cols:%d rows:%d", colCount, rowCount));
        this.rowCount = rowCount;
        this.colCount = colCount;
        board = new BaseTile[rowCount][colCount];
        score = 0;
        sweeper = new BoardSweeper(rowCount * colCount);
        settled = false;
    }

    public void randomizeTiles() {
        Log.d("GameBoard", "randomizeTiles");
        settled = false;
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 1; colNum < colCount - 1; colNum++) {
                TubeTile tile = (TubeTile) getTile(colNum, rowNum);
                if (tile != null) {
//                    tile.vanish();
                    setTile(colNum, rowNum, null);
                }
                setTile(colNum, rowNum, TILE_TYPE.TUBE, 0);
            }
        }
        sweepUntilSettled();
    }

    public BaseTile getTile(int colNum, int rowNum) {
        if ((colNum >= 0) && (rowNum >= 0) && (rowNum < board.length) && (colNum < board[rowNum].length)) {
            return board[rowNum][colNum];
        }
        return null;
    }

    public BaseTile setTile(int colNum, int rowNum, BaseTile tile) {
        if ((colNum >= 0) && (colNum < colCount) && (rowNum >= 0) && (rowNum < rowCount)) {
            board[rowNum][colNum] = tile;
            if ((tile != null) && ((tile.colNum != colNum) || (tile.rowNum != rowNum))) {
                // Log.d("GameBoard", String.format("setting %s to col:%d row:%d", tile, colNum, rowNum));
                tile.setColRow(colNum, rowNum);
            }
        }
        return tile;
    }

    public BaseTile setTile(int colNum, int rowNum, TILE_TYPE type, int bits) {
        BaseTile tile;
        switch (type) {
            case SOURCE:
                tile = new SourceTile(colNum, rowNum, this);
                break;
            case SINK:
                tile = new SinkTile(colNum, rowNum, this);
                break;
            default:
                tile = new TubeTile(colNum, rowNum, bits, this);
                break;
        }
        return setTile(colNum, rowNum, tile);
    }

    public int getRowCount() { return rowCount; }
    public int getColCount() { return colCount; }
    public int getScore() { return score; }
    public void setScore(int score) {
        int oldScore = this.score;
        if (score == oldScore) {
            return;
        }
        this.score = score;
        // Log.d("GameBoard", String.format("setScore old:%d new:%d", oldScore, score));
        if (watcher != null) {
            watcher.onScoreChanged(oldScore, score);
        }
    }
    public void addScore(int score) { setScore(this.score + score); }

    protected void sweepUntilSettled() {
        // Log.d("GameBoard", String.format("sweepUntilSettled cc:%d rc:%d", colCount, rowCount));
        while (!isSettled()) {
            powerSweep();
        }
    }

    public TileChangeSet powerSweep() {
        // Log.d("GameBoard", "powerSweep");
        TileChangeSet changes = sweeper.sweep(this);
        for (TileChangePower tilePower : changes.powered) {
            if ((tilePower == null) || (tilePower.tile == null)) {
                continue;
            }
            tilePower.tile.setPower(tilePower.power);
        }
        for (TubeTile tile : changes.vanished) {
            if (tile != null) {
                // Log.d("GameBoard", String.format("powerSweep vanishing %s", tile));
                setTile(tile.colNum, tile.rowNum, null);
                tile.vanish();
            } else {
                Log.e("GameBoard", "powerSweep vanished null tile");
            }
        }
        for (TileChangeMove move : changes.moved) {
            if ((move != null) && (move.tile != null)) {
                // Log.d("GameBoard", String.format("powerSweep moving %s from %d,%d to %d,%d", move.tile, move.fromColNum, move.fromRowNum, move.toColNum, move.toRowNum));
//                setTile(move.fromColNum, move.fromRowNum, null);
                setTile(move.toColNum, move.toRowNum, move.tile);
                move.tile.setColRow(move.toColNum, move.toRowNum);
            } else {
                Log.e("GameBoard", "powerSweep moved null tile");
            }
        }
        for (TileChangeAppear appear : changes.appeared) {
            // Log.d("GameBoard", String.format("powerSweep appearing new tile at %d,%d", appear.colNum, appear.rowNum));
            setTile(appear.colNum, appear.rowNum, new TubeTile(appear.colNum, appear.rowNum, this));
        }
        if (settled) {
            addScore(changes.vanished.size());
//            if (watcher != null) {
//                watcher.onBoardChanged(changes);
//            }
        } else if ((changes.appeared.size()) == 0 && (changes.vanished.size()) == 0 && (changes.moved.size() == 0)) {
            settled = true;
            return null;
        }
        return changes;
    }

    public boolean isSettled() { return settled; }

    public void setWatcher (GameBoard.Watcher watcher) { this.watcher = watcher; }

}
