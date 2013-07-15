package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.HashSet;
import java.util.Set;

public class GameBoard extends DebuggableGroup {

    static {
        CLASS_NAME = "GameBoard";
        DEBUG_MODE = false;
    }

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

    public GameBoard(int colCount, int rowCount, int maxWidth, int maxHeight) {
        super();
        debug("rows:%d cols:%d w:%d h:%d", rowCount, colCount, maxWidth, maxHeight);
        init(colCount, rowCount, maxWidth, maxHeight);
    }

    private void init(int colCount, int rowCount, int maxWidth, int maxHeight) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        board = new BaseTile[rowCount][colCount];
        scoreBoard = new ScoreActor();
        resizeToMax(maxWidth, maxHeight);
        addActor(scoreBoard);
        settled = false;
        score = 0;
        setTransform(false);
        sweeper = new BoardSweeper(rowCount * colCount);
    }

    public void randomizeTiles() {
        debug("randomizing tiles");
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
                        Actor target = event.getTarget();
                        debug("touchDown x:%.0f y:%.0f target:%s", x, y, target == null ? "null" : target);
                        BaseTile tile = self.tileAt(x, y);
                        if ((tile != null) && (tile instanceof TubeTile)) {
                            self.notifyListeners(EVENT_TYPE.TILE_SPIN, tile);
                            ((TubeTile) tile).onTouchDown();
                        }
                    }
                }
                catch (Exception e) {
                    error("touch exception: %s", e.toString());
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
        scoreHeight = maxHeight * SCORE_HEIGHT;
        float availableHeight = maxHeight - scoreHeight;
        tileSize = Math.min(maxWidth / colCount, availableHeight / rowCount);
        float w = Math.round(colCount * tileSize);
        float h = Math.round((rowCount * tileSize) + scoreHeight);
        setSize(w, h);
        float x = (maxWidth - w) / 2f;
        float y =  (maxHeight - h) / 2f;
        setPosition(x, y);
        scoreBoard.resize(0, 0, w, scoreHeight);
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
        debug("powerSweep begin");
        if (!awaitingSweep) {
            debug("powerSweep not awaitingSweep");
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
            debug("%s power %s -> %s", tilePower.tile.toString(), tilePower.tile.power, tilePower.power);
            tilePower.tile.setPower(tilePower.power);
        }
        // gather any connected tiles
        if (sweeper.connected.isEmpty()) {
            debug("ready");
            ready = true;
            if (!settled) {
                notifyListeners(EVENT_TYPE.BOARD_SETTLE);
            }
            settled = true;
        } else {
            sweeper.trackDrops(this);
            debug("vanishing %d", sweeper.vanished.size());
            if (sweeper.vanished.size() == (colCount - 2) * rowCount) {
                notifyListeners(EVENT_TYPE.BOARD_VANISH);
            } else {
                notifyListeners(EVENT_TYPE.TILES_VANISH);
            }
            for (TubeTile vanishTile : new HashSet<TubeTile>(sweeper.vanished)) {
                if (vanishTile != null) {
                    debug("vanishing %s", vanishTile);
                    vanishTile.vanish();
                }
            }
            if (settled) {
                debug("score %d + %d", score, sweeper.vanished.size());
                score += sweeper.vanished.size();
                scoreBoard.setScore(score);
            } else {
                debug("not settled");
                readyForSweep();
            }
        }
        debug("powerSweep done");
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
            debug("drop fell:%s remain:%d", tile.toString(), sweeper.fell.size());
            setTile(destinationColNum, destinationRowNum, tile);
            sweeper.fell.remove(tile);
            if (sweeper.fell.isEmpty()) {
                debug("drop complete");
                readyForSweep();
                debug("memory %d %d", Gdx.app.getNativeHeap(), Gdx.app.getJavaHeap());
            }
        } else {
            error("drop MISSING:%s remain:%d", tile.toString(), sweeper.fell.size());
        }
    }

    public void tileVanishComplete(TubeTile vanishedTile) {
        if (sweeper.vanished.contains(vanishedTile)) {
            Gdx.app.log("GameBoard", String.format("vanish removed:%s remain:%d", vanishedTile.toString(), sweeper.vanished.size()));
            setTile(vanishedTile.colNum, vanishedTile.rowNum, null);
            removeActor(vanishedTile);
            sweeper.vanished.remove(vanishedTile);
            if (sweeper.vanished.isEmpty()) {
                debug("fall begin drop:%d add:%d", sweeper.dropped.size(), sweeper.added.size());
                notifyListeners(EVENT_TYPE.TILES_DROP);
                for (BoardSweeper.DroppedTile droppedTile : sweeper.dropped) {
                    debug("dropping %s to c:%d r:%d x:%.0f y:%.0f", droppedTile.tile, droppedTile.colNum, droppedTile.rowNum, droppedTile.colX, droppedTile.rowY);
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
            error("vanish MISSING:%s remain:%d", vanishedTile.toString(), sweeper.vanished.size());
        }
    }

    public void interruptSweep() {
        debug("interruptSweep %b -> false, actions:%d", awaitingSweep, getActions().size);
        awaitingSweep = false;
        clearActions();
    }

    public void readyForSweep() {
        debug("readyForSweep %b -> true, actions:%d", awaitingSweep, getActions().size);
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
        if ((scoreKeeper != null) && (score > 0)) {
            scoreKeeper.addScore(score);
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

    private void notifyListeners(EVENT_TYPE type) {
        for (GameEventListener listener : eventListeners) {
            switch (type) {
                case BOARD_RANDOM:
                    listener.onRandomizeBoard(this);
                    break;
                case BOARD_SETTLE:
                    listener.onSettleBoard(this);
                    break;
                case BOARD_VANISH:
                    listener.onVanishBoard(this);
                    break;
                case TILES_DROP:
                    listener.onDropTiles(sweeper.dropped);
                    break;
                case TILES_VANISH:
                    listener.onVanishTiles(sweeper.vanished);
                    break;
            }
        }
    }

    private void notifyListeners(EVENT_TYPE type, BaseTile tile) {
        for (GameEventListener listener : eventListeners) {
            switch (type) {
                case TILE_SPIN:
                    listener.onSpinTile(tile);
                    break;
            }
            listener.onDropTiles(sweeper.dropped);
        }
    }

}
