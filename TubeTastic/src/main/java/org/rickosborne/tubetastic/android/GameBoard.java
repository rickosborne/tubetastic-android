package org.rickosborne.tubetastic.android;

import android.util.Log;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.HashSet;

public class GameBoard extends Group implements RenderController {

    public static final float DELAY_SWEEP = 0.125f;

    public static enum TILE_TYPE {
        SOURCE,
        TUBE,
        SINK
    }

    public static enum EVENT_TYPE {
        TILE_SPIN,
        TILES_VANISH,
        TILES_DROP,
        BOARD_VANISH,
        BOARD_RANDOM,
        BOARD_SETTLE
    }

    private int rowCount = 0;
    private int colCount = 0;
    private BaseTile[][] board;
    private boolean settled = false;
    private int score = 0;
    private float tileSize = 0;
    private boolean ready = false;
    private boolean awaitingSweep = false;
    private BoardSweeper sweeper;
    private final float SCORE_HEIGHT = 0.08f;
    private float scoreHeight = 0;
    private ScoreActor scoreBoard;
    private ScoreKeeper scoreKeeper;
    protected final TileRenderer renderer = new TileRenderer();
    protected HashSet<GameEventListener> eventListeners = new HashSet<GameEventListener>();
    protected RenderControls renderControls;

    public GameBoard(int colCount, int rowCount, int maxWidth, int maxHeight) {
        super();
        init(colCount, rowCount, maxWidth, maxHeight);
    }

    public GameBoard(int colCount, int rowCount, int maxWidth, int maxHeight, ScoreKeeper scoreKeeper) {
        this(colCount, rowCount, maxWidth, maxHeight);
        this.scoreKeeper = scoreKeeper;
    }

    private void init(int colCount, int rowCount, int maxWidth, int maxHeight) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        FreetypeActor.flushCache();
        board = new BaseTile[rowCount][colCount];
        scoreBoard = new ScoreActor();
        resizeToMax(maxWidth, maxHeight);
        settled = false;
        score = 0;
        setTransform(false);
        sweeper = new BoardSweeper(rowCount * colCount);
    }

    public void randomizeTiles() {
        ready = false;
        settled = false;
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 0; colNum < colCount; colNum++) {
                BaseTile tile = tileAt(colNum, rowNum);
                if (tile != null) {
                    removeActor(tile);
                    setTile(colNum, rowNum, null);
                }
                TILE_TYPE type;
                if (colNum == 0) {
                    type = TILE_TYPE.SOURCE;
                }
                else if (colNum == colCount - 1) {
                    type = TILE_TYPE.SINK;
                }
                else {
                    type = TILE_TYPE.TUBE;
                }
                setTile(colNum, rowNum, type, 0);
            }
        }
        notifyListeners(EVENT_TYPE.BOARD_RANDOM);
    }

    public void begin() {
        final GameBoard self = this;
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                try {
                    if (!event.isHandled() && self.isReady() && self.isSettled()) {
                        BaseTile tile = self.tileAt(x, y);
                        if ((tile != null) && (tile instanceof TubeTile)) {
                            self.startRendering();
                            if (!self.notifyListeners(EVENT_TYPE.TILE_SPIN, tile)) {
                                ((TubeTile) tile).onTouchDown();
                            }
                        }
                    }
                }
                catch (Exception e) {
                    Log.e("GameBoard", String.format("touch exception: %s", e.toString()));
                }
                return true;
            }
        });
        setTouchable(Touchable.enabled);
        awaitingSweep = true;
        powerSweep();
    }

    public void resizeToMax(int maxWidth, int maxHeight) {
        if ((getWidth() == maxWidth) && (getHeight() == maxHeight)) {
            return;
        }
        if (scoreKeeper == null) {
            scoreHeight = 0;
        } else {
            scoreHeight = maxHeight * SCORE_HEIGHT;
        }
        float availableHeight = maxHeight - scoreHeight;
        tileSize = Math.min(maxWidth / colCount, availableHeight / rowCount);
        float w = Math.round(colCount * tileSize);
        float h = Math.round((rowCount * tileSize) + scoreHeight);
        setSize(w, h);
        float x = (maxWidth - w) / 2f;
        float y =  (maxHeight - h) / 2f;
        setPosition(x, y);
        Log.d("GameBoard", String.format("resizeToMax mw:%d mh:%d ah:%.0f sz:%.0f w:%.0f h:%.0f x:%.0f y:%.0f", maxWidth, maxHeight, availableHeight, tileSize, w, h, x, y));
        if (scoreKeeper != null) {
            scoreBoard.resize(0, 0, w, scoreHeight);
        }
        for (int colNum = 0; colNum < colCount; colNum++) {
            float colX = xForColNum(colNum);
            for (int rowNum = 0; rowNum < rowCount; rowNum++) {
                BaseTile tile = getTile(colNum, rowNum);
                if (tile != null) {
                    tile.resize(colX, yForRowNum(rowNum), tileSize);
                }
            }
        }
        requestRender();
    }

    public float xForColNum(int colNum) {
        return (colNum * tileSize);
    }

    public float yForRowNum(int rowNum) {
        return ((rowCount - 1 - rowNum) * tileSize) + scoreHeight;
    }

    public BaseTile tileAt(float x, float y) {
        int colNum = (int) (x / tileSize);
        int rowNum = rowCount - 1 - ((int) ((y - scoreHeight) / tileSize));
        return getTile(colNum, rowNum);
    }

    public BaseTile getTile(int colNum, int rowNum) {
        if ((colNum >= 0) && (colNum < colCount) && (rowNum >= 0) && (rowNum < rowCount)) {
            return board[rowNum][colNum];
        }
        return null;
    }

    public BaseTile setTile(int colNum, int rowNum, BaseTile tile) {
        if ((colNum >= 0) && (colNum < colCount) && (rowNum >= 0) && (rowNum < rowCount)) {
            board[rowNum][colNum] = tile;
            if (tile != null) {
                addActor(tile);
            }
        }
        return tile;
    }

    public BaseTile setTile(int colNum, int rowNum, TILE_TYPE type, int bits) {
        BaseTile tile;
        switch (type) {
            case SOURCE:
                tile = new SourceTile(colNum, rowNum, xForColNum(colNum), yForRowNum(rowNum), tileSize, this);
                break;
            case SINK:
                tile = new SinkTile(colNum, rowNum, xForColNum(colNum), yForRowNum(rowNum), tileSize, this);
                break;
            default:
                tile = new TubeTile(colNum, rowNum, xForColNum(colNum), yForRowNum(rowNum), tileSize, bits, this);
                break;
        }
        tile.setRenderer(renderer);
        return setTile(colNum, rowNum, tile);
    }

    public TubeTile setTile(float x, float y) {
        TubeTile tile = (TubeTile) setTile(-2, -2, TILE_TYPE.TUBE, 0);
        tile.setPosition(x, y);
        addActor(tile);
        return tile;
    }

    public int getRowCount() { return rowCount; }
    public int getColCount() { return colCount; }
    public int getScore() { return score; }
    public void setScore(int score) {
        this.score = score;
        if (scoreKeeper != null) {
            scoreKeeper.addScore(score);
        }
        if (scoreBoard != null) {
            scoreBoard.setScore(score);
        }
    }
    public void addScore(int score) { setScore(this.score + score); }

    private void powerSweep() {
        if (!awaitingSweep) {
            return;
        }
        awaitingSweep = false;
        ready = false;
        sweeper.reset();
        sweeper.resetNeither(this);
        sweeper.trackSourced(this);
        sweeper.trackSunk(this);
        sweeper.trackUnpowered(this);
        sweeper.trackVanishes(this);
        for (BoardSweeper.TileChangePower tilePower : sweeper.powered) {
            if ((tilePower == null) || (tilePower.tile == null)) {
                continue;
            }
            tilePower.tile.setPower(tilePower.power);
        }
        if (sweeper.powered.size() > 0) {
            requestRender();
        }
        // gather any connected tiles
        if (sweeper.connected.isEmpty()) {
            ready = true;
            if (!settled) {
                notifyListeners(EVENT_TYPE.BOARD_SETTLE);
            }
            settled = true;
            stopRendering();
        } else {
            sweeper.trackDrops(this);
            if (sweeper.vanished.size() == (colCount - 2) * rowCount) {
                notifyListeners(EVENT_TYPE.BOARD_VANISH);
            } else {
                notifyListeners(EVENT_TYPE.TILES_VANISH);
            }
            for (TubeTile vanishTile : new HashSet<TubeTile>(sweeper.vanished)) {
                if (vanishTile != null) {
                    vanishTile.vanish();
                }
            }
            if (settled) {
                score += sweeper.vanished.size();
                scoreBoard.setScore(score);
            } else {
                readyForSweep();
            }
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

    public void tileDropComplete(TubeTile tile, int destinationColNum, int destinationRowNum) {
        if (sweeper.fell.contains(tile)) {
            setTile(destinationColNum, destinationRowNum, tile);
            sweeper.fell.remove(tile);
            if (sweeper.fell.isEmpty()) {
                readyForSweep();
            }
        } else {
            Log.e("GameBoard", String.format("drop MISSING:%s remain:%d", tile.toString(), sweeper.fell.size()));
        }
    }

    public void tileVanishComplete(TubeTile vanishedTile) {
        if (sweeper.vanished.contains(vanishedTile)) {
            Gdx.app.log("GameBoard", String.format("vanish removed:%s remain:%d", vanishedTile.toString(), sweeper.vanished.size()));
            setTile(vanishedTile.colNum, vanishedTile.rowNum, null);
            removeActor(vanishedTile);
            sweeper.vanished.remove(vanishedTile);
            if (sweeper.vanished.isEmpty()) {
                if (notifyListeners(EVENT_TYPE.TILES_DROP)) {
                    return;
                }
                for (BoardSweeper.DroppedTile droppedTile : sweeper.dropped) {
                    setTile(droppedTile.tile.colNum, droppedTile.tile.rowNum, null);
                    sweeper.fell.add(droppedTile.tile);
                }
                for (BoardSweeper.DroppedTile addedTile : sweeper.added) {
                    addedTile.tile = setTile(addedTile.colX, addedTile.rowY);
                    sweeper.fell.add(addedTile.tile);
                }
                for (BoardSweeper.DroppedTile droppedTile : sweeper.dropped) {
                    droppedTile.tile.dropTo(droppedTile.colNum, droppedTile.rowNum, droppedTile.colX, droppedTile.rowY);
                }
                for (BoardSweeper.DroppedTile addedTile : sweeper.added) {
                    addedTile.tile.dropTo(addedTile.colNum, addedTile.rowNum, addedTile.colX, yForRowNum(addedTile.rowNum));
                }
            }
        } else {
            Log.e("GameBoard", String.format("vanish MISSING:%s remain:%d", vanishedTile.toString(), sweeper.vanished.size()));
        }
    }

    public void interruptSweep() {
        awaitingSweep = false;
        clearActions();
    }

    public void readyForSweep() {
        final GameBoard self = this;
        awaitingSweep = true;
        clearActions();
        addAction(Actions.sequence(
                Actions.delay(DELAY_SWEEP),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        self.powerSweep();
                    }
                })
        ));
    }

    public void setScoreKeeper(ScoreKeeper keeper) {
        scoreKeeper = keeper;
        if (scoreKeeper == null) {
            removeActor(scoreBoard);
        } else {
            addActor(scoreBoard);
            if (score > 0) {
                scoreKeeper.addScore(score);
            }
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    public void removeGameEventListener(GameEventListener listener) {
        if (eventListeners.contains(listener)) {
            eventListeners.remove(listener);
        }
    }

    public void removeAllGameEventListeners() {
        eventListeners.clear();
    }

    private boolean notifyListeners(EVENT_TYPE type) {
        boolean interrupt = false;
        for (GameEventListener listener : eventListeners) {
            switch (type) {
                case BOARD_RANDOM:
                    interrupt = interrupt || listener.onRandomizeBoard(this);
                    break;
                case BOARD_SETTLE:
                    interrupt = interrupt || listener.onSettleBoard(this);
                    break;
                case BOARD_VANISH:
                    interrupt = interrupt || listener.onVanishBoard(this);
                    break;
                case TILES_DROP:
                    interrupt = interrupt || listener.onDropTiles(sweeper.dropped);
                    break;
                case TILES_VANISH:
                    interrupt = interrupt || listener.onVanishTiles(sweeper.vanished);
                    break;
            }
        }
        return interrupt;
    }

    private boolean notifyListeners(EVENT_TYPE type, BaseTile tile) {
        boolean interrupt = false;
        for (GameEventListener listener : eventListeners) {
            switch (type) {
                case TILE_SPIN:
                    interrupt = interrupt || listener.onSpinTile(tile);
                    break;
            }
        }
        return interrupt;
    }

    public void setRenderControls(RenderControls renderControls) {
        this.renderControls = renderControls;
    }

    private void startRendering() {
        if (renderControls != null) {
            renderControls.startRendering();
        }
    }

    private void stopRendering() {
        if (renderControls != null) {
            renderControls.stopRendering();
        }
    }

    private void requestRender() {
        if (renderControls != null) {
            renderControls.requestRender();
        }
    }

}
