package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

public class GameBoard extends Group {

    private static class DroppedTile {
        public TubeTile tile;
        public int colNum;
        public int rowNum;
        public float colX;
        public DroppedTile(TubeTile tile, int colNum, int rowNum, float colX) {
            this.tile = tile;
            this.colNum = colNum;
            this.rowNum = rowNum;
            this.colX = colX;
        }
    }

    private int rowCount = 0;
    private int colCount = 0;
    private BaseTile[][] board;
    private boolean settled = false;
    private int score = 0;
    private float tileSize = 0;
    private boolean ready = false;
    private int toVanishCount = 0;
    private int toDropCount = 0;
    private boolean needPowerSweep = false;
    private ArrayList<TubeTile> toVanish = new ArrayList<TubeTile>();
    private ArrayList<DroppedTile> toDrop = new ArrayList<DroppedTile>();

    public GameBoard(int rowCount, int colCount, int maxWidth, int maxHeight) {
        super();
        Gdx.app.log("GameBoard", String.format("rows:%d cols:%d w:%d h:%d", rowCount, colCount, maxWidth, maxHeight));
        init(rowCount, colCount, maxWidth, maxHeight);
    }

    private void init(int rowCount, int colCount, int maxWidth, int maxHeight) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        board = new BaseTile[rowCount][colCount];
        BaseTile tile;
        resizeToMax(maxWidth, maxHeight);
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
                addActor(tile);
            }
        }
        settled = false;
        score = 0;
        setTransform(false);
        final GameBoard self = this;
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                try {
                    if (!event.isHandled()) {
                        Actor target = event.getTarget();
                        Gdx.app.log("GameBoard;" + event.getTarget().getClass().getSimpleName(), String.format("touchDown x:%.0f y:%.0f", x, y));
                        BaseTile tile = self.tileAt(x, y);
                        if ((tile != null) && (tile instanceof TubeTile)) {
                            ((TubeTile) tile).onTouchDown();
                        }
                    }
                }
                catch (Exception e) {
                    Gdx.app.log("GameBoard;" + event.getTarget().getClass().getSimpleName(), "touch exception:" + e.toString());
                }
                return true;
            }
        });
        setTouchable(Touchable.enabled);
        powerSweep();
    }

    public void resizeToMax(int maxWidth, int maxHeight) {
        if ((getWidth() == maxWidth) && (getHeight() == maxHeight)) {
            return;
        }
        tileSize = Math.min(maxWidth / colCount, maxHeight / (rowCount + 1));
        setSize(Math.round(colCount * tileSize), Math.round((rowCount + 1) * tileSize));
        setPosition((maxWidth - (tileSize * colCount)) / 2, (maxHeight - (tileSize * (rowCount + 1))) / 2);
    }

    public float xForColNum(int colNum) {
        return (colNum * tileSize);
    }

    public float yForRowNum(int rowNum) {
        return ((rowCount - rowNum) * tileSize);
    }

    public BaseTile tileAt(float x, float y) {
        int colNum = (int) (x / tileSize);
        int rowNum = rowCount - ((int) (y / tileSize));
        Gdx.app.log("GameBoard", String.format("tileAt c:%d r:%d x:%.0f y:%.0f", colNum, rowNum, x, y));
        if ((colNum >= 0) && (colNum < colCount) && (rowNum >= 0) && (rowNum < rowCount)) {
            return board[rowNum][colNum];
        }
        return null;
    }

    private void powerSweep() {
        needPowerSweep = false;
        BaseTile tile;
        int rowNum;
        int colNum;
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
                if ((neighbor != null) && !sourced.contains(neighbor) && !toCheck.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
        }
        // then, find all of the sunk tiles that are not sourced
        for (rowNum = lastRowNum; rowNum >= 0; rowNum--) {
            tile = board[rowNum][sinkColNum];
            if ((tile != null) && !sourced.contains(tile) && !toCheck.contains(tile)) {
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
                if ((neighbor != null) && !sourced.contains(neighbor) && !sunk.contains(neighbor) && !toCheck.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
        }
        // reset any leftover tiles
        for (BaseTile unpowered : neither) {
            if ((unpowered != null) && !unpowered.isUnpowered()) {
                unpowered.setPower(BaseTile.Power.NONE);
            }
        }
        // gather any connected tiles
//        toCheck.addAll(connected);
        if (toCheck.isEmpty()) {
            Gdx.app.log("GameBoard", "ready");
            ready = true;
            settled = true;
        }
        while (toCheck.size() > 0) {
            tile = toCheck.pop();
            connected.add(tile);
            for (BaseTile neighbor : tile.getConnectedNeighbors()) {
                if ((neighbor != null) && !connected.contains(neighbor) && !toCheck.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
            if ((tile != null) && (tile instanceof TubeTile)) {
//                toVanish.add((TubeTile) tile);
                toVanishCount++;
            }
        }
        points += toVanishCount;
        Gdx.app.log(toString(), String.format("want to vanish %d", toVanishCount));
        for (TubeTile vanishTile : toVanish) {
            if (vanishTile != null) {
                vanishTile.vanish();
            }
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

    public void tileDropComplete(BaseTile tile, int destinationColNum, int destinationRowNum) {
        toDropCount--;
        Gdx.app.log("GameBoard", String.format("tileDropComplete col:%d/%d row:%d/%d remain:%d", destinationColNum, colCount - 1, destinationRowNum, rowCount - 1, toDropCount));
        board[destinationRowNum][destinationColNum] = tile;
        if (toDropCount > 0) {
            return;
        } else if (toDropCount < 0) {
            toDropCount = 0;
            return;
        }
//        final GameBoard self = this;
        readyForSweep();
    }

    public void tileVanishComplete() {
        toVanishCount--;
        Gdx.app.log("GameBoard", String.format("vanish:%d", toVanishCount));
        if (toVanishCount > 0) {
            return;
        }
        for (int colNum = 1; colNum < colCount - 2; colNum++) {
            int destRowNum = rowCount;
            float colX = xForColNum(colNum);
            for (int rowNum = rowCount - 1; rowNum >= 0; rowNum--) {
                TubeTile tile = (TubeTile) board[rowNum][colNum];
                if (toVanish.contains(tile)) {
                    board[rowNum][colNum] = null;
                    removeActor(tile);
                }
                else {
                    destRowNum--;
                    if (destRowNum > rowNum) {
                        toDropCount++;
                        toDrop.add(new DroppedTile(tile, colNum, rowNum, colX));
                        board[rowNum][colNum] = null;
                    }
                }
            }
            for (int rowNum = destRowNum - 1; rowNum >= 0; rowNum--) {
                toDropCount++;
                TubeTile tile = new TubeTile(-2, -2, colX, yForRowNum(rowNum - destRowNum), tileSize, this);
                toDrop.add(new DroppedTile(tile, colNum, rowNum, colX));
                addActor(tile);
            }
        }
        for (DroppedTile drop : toDrop) {
            drop.tile.dropTo(drop.colNum, drop.rowNum, drop.colX, yForRowNum(drop.rowNum));
        }
    }

    public void interruptSweep() {
        needPowerSweep = false;
    }

    public void readyForSweep() {
        needPowerSweep = true;
    }

    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.end();
        ShapeRenderer shape = new ShapeRenderer();
        shape.begin(ShapeRenderer.ShapeType.FilledRectangle);
        shape.identity();
        shape.setColor(0.2f, 0.2f, 0.2f, 1.0f);
        shape.rotate(0f, 0f, 1f, getRotation());
        shape.filledRect(getX(), getY(), getWidth(), getHeight());
        shape.end();
        batch.begin();
        super.draw(batch, parentAlpha);
    }

    public void act (float delta) {
        if (needPowerSweep) {
            powerSweep();
        }
        super.act(delta);
    }

}
