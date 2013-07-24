package org.rickosborne.tubetastic.android;

import android.util.Log;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.HashSet;
import java.util.Set;

public class BoardActor extends Group implements RenderController, TileLoaderActor.TileLoaderWatcher, GameBoard.Watcher, TileActorWatcher {

    public static final float DELAY_SWEEP = 0.125f;

    public static enum EVENT_TYPE {
        TILE_SPIN,
        TILES_VANISH,
        TILES_DROP,
        TILES_WILL_APPEAR,
        BOARD_WILL_VANISH,
        BOARD_RANDOM,
        BOARD_SETTLE,
        TILES_VANISH_START,
        TILES_VANISH_FINISH
    }

    public static Color COLOR_SOURCE = GamePrefs.COLOR_POWER_SOURCED;
    public static Color COLOR_SINK   = GamePrefs.COLOR_POWER_SUNK;
    public static Color COLOR_ARC    = GamePrefs.COLOR_ARC;
    public static Color COLOR_NONE   = GamePrefs.COLOR_POWER_NONE;

    private GameBoard gameBoard;
    private float tileSize = 0;
    private boolean ready = false;
    private final float SCORE_HEIGHT = 0.08f;
    private float scoreHeight = 0;
    private ScoreActor scoreBoard;
    protected final TileRenderer renderer = new TileRenderer();
    protected HashSet<GameEventListener> eventListeners = new HashSet<GameEventListener>();
    protected RenderControls renderControls;
    protected TileLoaderActor tileLoader;
    protected boolean isLoading = false;
    protected boolean awaitingSweep = false;
    protected int[] tileBits;
    protected ScoreKeeper scoreKeeper;
    protected TileActor[][] tileActors;
    protected GameBoard.TileChangeSet tileChanges;
    protected Set<TileActor> dropping;
    protected Set<TileActor> spinning;
    protected Set<TileActor> appearing;
    protected Set<TileActor> vanishing;
    protected int colCount;
    protected int rowCount;
    protected int animatableCount;
    protected Rectangle bounds;

    public BoardActor(GameBoard gameBoard, Rectangle bounds) {
        this.gameBoard = gameBoard;
        colCount = gameBoard.getColCount();
        rowCount = gameBoard.getRowCount();
        animatableCount = (colCount - 2) * rowCount;
        dropping = new HashSet<TileActor>(animatableCount);
        spinning = new HashSet<TileActor>(animatableCount);
        appearing = new HashSet<TileActor>(animatableCount);
        vanishing = new HashSet<TileActor>(animatableCount);
        // // Log.d("BoardActor", String.format("cols:%d rows:%d bounds:%s", colCount, rowCount, bounds));
        tileActors = new TileActor[rowCount][colCount];
        gameBoard.setWatcher(this);
        init(bounds);
    }

    protected void init(Rectangle bounds) {
        FreetypeActor.flushCache();
//        setSize(maxWidth, maxHeight);
        resizeToFit(bounds);
        setTransform(false);
        COLOR_SOURCE = GamePrefs.COLOR_POWER_SOURCED;
        COLOR_SINK   = GamePrefs.COLOR_POWER_SUNK;
        COLOR_ARC    = GamePrefs.COLOR_ARC;
        COLOR_NONE   = GamePrefs.COLOR_POWER_NONE;
    }

    public void loadTiles(int... bits) {
        clear();
        isLoading = true;
        tileBits = bits;
        tileLoader = new TileLoaderActor((int) tileSize, renderer, this);
        if ((tileBits != null) && (tileBits.length > 0)) {
            tileLoader.setBits(tileBits);
        }
        float loaderX = 0;
        float loaderY = 0;
        float loaderW = getWidth();
        float loaderH = getHeight();
        float loaderRatio = loaderW / loaderH;
        if (loaderRatio > 8f) {
            loaderX = (loaderW - (loaderH * 8f)) / 2f;
            loaderW = loaderH * 8f;
        }
        else {
            loaderY = (loaderH - (loaderW / 8f)) / 2f;
            loaderH = loaderW / 8f;
        }
        tileLoader.setBounds(loaderX, loaderY, loaderW, loaderH);
        addActor(tileLoader);
        // // Log.d("BoardActor", String.format("loadTiles n:%d x:%.0f y:%.0f w:%.0f h:%.0f", bits.length, loaderX, loaderY, loaderW, loaderH));
    }

    public void randomizeTiles() {
        Log.d("BoardActor", "randomizeTiles");
        ready = false;
        gameBoard.randomizeTiles();
        addGameActors();
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 1; colNum < colCount - 1; colNum++) {
                getTileActor(colNum, rowNum).appear();
            }
        }
        notifyListeners(EVENT_TYPE.BOARD_RANDOM);
        ready = true;
    }

    public void begin() {
        // // Log.d("BoardActor", "begin");
        if (isLoading) {
            Log.d("BoardActor", "begin isLoading return");
            return;
        }
        final BoardActor self = this;
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Log.d("BoardActor", String.format("touchDown handled:%b ready:%b x:%.0f y:%.0f", event.isHandled(), self.isReady(), x, y));
                try {
                    if (!event.isHandled() && !self.isWorking()) {
                        TileActor tileActor = self.tileAt(x, y);
                        if ((tileActor != null) && (tileActor.tile instanceof TubeTile)) {
                            self.startRendering();
                            if (!self.notifyListeners(EVENT_TYPE.TILE_SPIN, tileActor.tile)) {
                                tileActor.onTouchDown();
                            } else {
                                Log.d("BoardActor", "touchDown notify returned INTERRUPT_YES");
                            }
                        } else {
                            Log.d("BoardActor", String.format("touchDown tileActor:%s", tileActor));
                        }
                    } else {
                        Log.d("BoardActor", String.format("touchDown handled:%b working:%b", event.isHandled(), self.isWorking()));
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
        notifyListeners(EVENT_TYPE.BOARD_SETTLE);
        stopRendering();
    }

    protected TileActor getActorForTile(BaseTile tile) {
        if (tile != null) {
            return getTileActor(tile.colNum, tile.rowNum);
        }
        return null;
    }

    protected boolean closeEnough(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    protected void checkBoard() {
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 0; colNum < colCount; colNum++) {
                TileActor tileActor = getTileActor(colNum, rowNum);
                if (tileActor == null) {
                    Log.e("checkBoard", String.format("TileActor missing for col:%d row:%d", colNum, rowNum));
                } else {
                    if (!closeEnough(tileActor.getX(), xForColNum(colNum), 1) || !closeEnough(tileActor.getY(), yForRowNum(rowNum), 1)) {
                        Log.e("checkBoard", String.format("TileActor %s at (%.0f,%.0f) should be at (%.0f,%.0f)", tileActor, tileActor.getX(), tileActor.getY(), xForColNum(colNum), yForRowNum(rowNum)));
                    }
                    if (tileActor.tile == null) {
                        Log.e("checkBoard", String.format("TileActor %s at %d,%d missing tile", tileActor, colNum, rowNum));
                    } else {
                        if ((colNum == 0) && !(tileActor.tile instanceof SourceTile)) {
                            Log.e("checkBoard", String.format("Tile %s at %d,%d should be Source", tileActor.tile, colNum, rowNum));
                        } else if ((colNum == colCount - 1) && !(tileActor.tile instanceof SinkTile)) {
                            Log.e("checkBoard", String.format("Tile %s at %d,%d should be Sink", tileActor.tile, colNum, rowNum));
                        } else if ((colNum > 0) && (colNum < colCount - 1) && !(tileActor.tile instanceof TubeTile)) {
                            Log.e("checkBoard", String.format("Tile %s at %d,%d should be Tube", tileActor.tile, colNum, rowNum));
                        }
                        if (!closeEnough(tileActor.getRotation(), -tileActor.tile.outletRotation, 1)) {
                            Log.e("checkBoard", String.format("TileActor %s rotation:%.0f != %d for Tile %s", tileActor, tileActor.getRotation(), tileActor.tile.outletRotation, tileActor.tile));
                        }
                        if ((tileActor.tile.colNum != colNum) || (tileActor.tile.rowNum != rowNum)) {
                            Log.e("checkBoard", String.format("position %s at %d,%d != %d,%d for %s", tileActor, colNum, rowNum, tileActor.tile.colNum, tileActor.tile.rowNum, tileActor.tile));
                        }
                        if (tileActor.tile.board.getTile(colNum, rowNum) != tileActor.tile) {
                            Log.e("checkBoard", String.format("board is missing %s at %d,%d", tileActor.tile, colNum, rowNum));
                        }
                    }
                }
            }
        }
    }

    protected void powerSweep() {
        // // Log.d("BoardActor", "powerSweep");
//        checkBoard();
        if (awaitingSweep && !isWorking()) {
            awaitingSweep = false;
            tileChanges = gameBoard.powerSweep();
            if (tileChanges != null) {
                int vanishCount = tileChanges.vanished.size();
                if (vanishCount > 0) {
                    boolean addTiles;
                    if (vanishCount == animatableCount) {
                        // // Log.d("BoardActor", "powerSweep board vanish");
                        notifyListeners(EVENT_TYPE.BOARD_WILL_VANISH);
                    }
                    if (!notifyListeners(EVENT_TYPE.TILES_WILL_APPEAR)) {
                        ready = false;
                        for (GameBoard.TileChangeAppear change : tileChanges.appeared) {
                            BaseTile tile = gameBoard.getTile(change.colNum, change.rowNum);
                            if (tile == null) {
                                Log.e("BoardActor", String.format("tileVanishComplete null appeared col:%d row:%d", change.colNum, change.rowNum));
                            } else {
                                // // Log.d("BoardActor", String.format("Appearing %s at col:%d row:%s", tile, change.colNum, change.rowNum));
                                TileActor tileActor = new TileActor(tile, xForColNum(change.colNum), yForRowNum(change.rowNum), tileSize);
                                tile.setWatcher(tileActor);
                                tileActor.setWatcher(this);
                                tileActor.setRenderer(renderer);
                                setTileActor(change.colNum, change.rowNum, tileActor);
                                addActor(tileActor);
                                tileActor.appear();
                            }
                        }
                    }
                } else {
                    Log.d("BoardActor", "powerSweep nothing vanished");
                    ready = true;
                }
            } else {
                Log.d("BoardActor", "powerSweep unchanged");
                ready = true;
            }
        } else {
            Log.d("BoardActor", "powerSweep NOT awaitingSweep");
        }
    }

    public void resizeToFit(Rectangle bounds) {
        if ((getWidth() == bounds.width) && (getHeight() == bounds.height)) {
            return;
        }
        this.bounds = bounds;
        // // Log.d("BoardActor", String.format("resizeToFit %s", bounds));
        if (scoreKeeper == null) {
            scoreHeight = 0;
            scoreBoard = null;
        } else {
            scoreHeight = bounds.height * SCORE_HEIGHT;
            if (scoreBoard == null) {
                scoreBoard = new ScoreActor();
            }
        }
        float availableHeight = bounds.height - scoreHeight;
        tileSize = Math.min(bounds.width / colCount, availableHeight / rowCount);
        float w = Math.round(colCount * tileSize);
        float h = Math.round((rowCount * tileSize) + scoreHeight);
        float x = bounds.x + (bounds.width - w) / 2f;
        float y = bounds.y + (bounds.height - h) / 2f;
        setBounds(x, y, w, h);
//        // // Log.d("GameBoard", String.format("resizeToFit mw:%d mh:%d ah:%.0f sh:%.0f sz:%.0f w:%.0f h:%.0f x:%.0f y:%.0f", maxWidth, maxHeight, availableHeight, scoreHeight, tileSize, w, h, x, y));
        if (scoreBoard != null) {
            scoreBoard.resize(0, 0, w, scoreHeight);
        }
        for (int colNum = 0; colNum < colCount; colNum++) {
            float colX = xForColNum(colNum);
            for (int rowNum = 0; rowNum < rowCount; rowNum++) {
                TileActor tile = getTileActor(colNum, rowNum);
                if (tile != null) {
                    tile.resize(colX, yForRowNum(rowNum), tileSize);
                }
            }
        }
    }

    public float xForColNum(int colNum) {
        return (colNum * tileSize);
    }

    public float yForRowNum(int rowNum) {
        return ((rowCount - 1 - rowNum) * tileSize) + scoreHeight;
    }

    public TileActor tileAt(float x, float y) {
        int colNum = (int) (x / tileSize);
        int rowNum = rowCount - 1 - ((int) ((y - scoreHeight) / tileSize));
        return getTileActor(colNum, rowNum);
    }

    public TileActor getTileActor(int colNum, int rowNum) {
        if ((colNum >= 0) && (rowNum >= 0) && (rowNum < tileActors.length) && (colNum < tileActors[0].length)) {
            return tileActors[rowNum][colNum];
        }
        return null;
    }

    public void setTileActor(int colNum, int rowNum, TileActor tileActor) {
        // // Log.d("BoardActor", String.format("setTileActor col:%d row:%d ta:%s", colNum, rowNum, tileActor));
        if ((colNum >= 0) && (rowNum >= 0) && (rowNum < tileActors.length) && (colNum < tileActors[rowNum].length)) {
//            if (tileActors[rowNum][colNum] != null) {
//                tileActors[rowNum][colNum].remove();
//            }
            tileActors[rowNum][colNum] = tileActor;
//            if (tileActor != null) {
//                tileActor.setRenderer(renderer);
//                tileActor.remove();
//                addActor(tileActor);
//            }
        }
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isAnimating() {
        return !(dropping.isEmpty() && spinning.isEmpty() && vanishing.isEmpty() && appearing.isEmpty());
    }

    public boolean isWorking() {
        return !(dropping.isEmpty() && vanishing.isEmpty() && appearing.isEmpty());
    }

    public void interruptSweep() {
        // // Log.d("BoardActor", "interruptSweep");
        awaitingSweep = false;
        clearActions();
    }

    public void readyForSweep() {
        // // Log.d("BoardActor", "readyForSweep");
        final BoardActor self = this;
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
            scoreBoard = null;
        } else {
            if (!isLoading) {
                scoreBoard = new ScoreActor();
                addActor(scoreBoard);
            }
            int score = gameBoard.getScore();
            if (score > 0) {
                scoreKeeper.addScore(score);
                scoreBoard.setScore(score);
            }
        }
        requestRender();
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
                    interrupt = interrupt || listener.onRandomizeBoard(gameBoard);
                    break;
                case BOARD_SETTLE:
                    interrupt = interrupt || listener.onSettleBoard(gameBoard);
                    break;
                case BOARD_WILL_VANISH:
                    interrupt = interrupt || listener.onVanishBoard(gameBoard);
                    break;
                case TILES_DROP:
                    interrupt = interrupt || listener.onDropTiles(tileChanges.moved);
                    break;
                case TILES_WILL_APPEAR:
                    interrupt = interrupt || listener.onAppearTiles();
                    break;
                case TILES_VANISH_START:
                    interrupt = interrupt || listener.onVanishTilesStart();
                    break;
                case TILES_VANISH_FINISH:
                    interrupt = interrupt || listener.onVanishTilesFinish();
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

    private boolean isRendering() {
        return (renderControls == null) || renderControls.isContinuousRendering();
    }

    private void addGameActors() {
        // // Log.d("BoardActor", "addGameActors");
        clear();
        if (scoreKeeper != null) {
            if (scoreBoard == null) {
                scoreBoard = new ScoreActor();
            }
            addActor(scoreBoard);
        }
        for (int rowNum = 0; rowNum < tileActors.length; rowNum++) {
            for (int colNum = 0; colNum < tileActors[rowNum].length; colNum++) {
                TileActor tileActor = getTileActor(colNum, rowNum);
                if ((tileActor != null) && (tileActor.tile == gameBoard.getTile(colNum, rowNum))) {
                    addActor(tileActor);
                } else {
                    BaseTile tile = gameBoard.getTile(colNum, rowNum);
                    if (tile != null) {
                        tileActor = new TileActor(tile, xForColNum(colNum), yForRowNum(rowNum), tileSize);
                        tile.setWatcher(tileActor);
                        tileActor.setWatcher(this);
                        tileActor.setRenderer(renderer);
                        setTileActor(colNum, rowNum, tileActor);
                        addActor(tileActor);
//                        tileActor.appear(xForColNum(colNum), yForRowNum(rowNum));
                    } else {
                        // // Log.d("BoardActor", String.format("addGameActors empty tile col:%d row:%d", colNum, rowNum));
                    }
                }
            }
        }
        requestRender();
    }

    public GameBoard getGameBoard() { return gameBoard; }

    @Override
    public void onScoreChanged(int fromScore, int toScore) {
        // // Log.d("BoardActor", String.format("onScoreChanged from:%d to:%d", fromScore, toScore));
        if (scoreKeeper != null) {
            scoreKeeper.addScore(toScore);
        }
        if (scoreBoard != null) {
            scoreBoard.setScore(toScore);
        }
    }

    @Override
    public void onTileLoadComplete() {
        // // Log.d("BoardActor", "onTileLoadComplete");
        if (tileLoader != null) {
            tileLoader.remove();
            tileLoader = null;
            addGameActors();
        }
        isLoading = false;
        begin();
    }

    @Override
    public boolean removeActor(Actor actor) {
        // // Log.d("BoardActor", String.format("removeActor %s", actor));
        return super.removeActor(actor);
    }

    @Override
    public void onTileActorSpinStart(TileActor tileActor) {
        ready = false;
        spinning.add(tileActor);
    }

    @Override
    public void onTileActorSpinFinish(TileActor tileActor) {
        spinning.remove(tileActor);
        ready = !isAnimating();
    }

    @Override
    public void onTileActorMoveStart(TileActor tileActor) {
        ready = false;
        dropping.add(tileActor);
    }

    @Override
    public void onTileActorMoveFinish(TileActor tileActor, int colNum, int rowNum) {
        dropping.remove(tileActor);
        ready = !isAnimating();
        setTileActor(colNum, rowNum, tileActor);
    }

    @Override
    public void onTileActorAppearStart(TileActor tileActor) {
        ready = false;
        appearing.add(tileActor);
    }

    @Override
    public void onTileActorAppearFinish(TileActor tileActor, int colNum, int rowNum) {
        appearing.remove(tileActor);
        ready = !isAnimating();
        setTileActor(colNum, rowNum, tileActor);
        if (appearing.isEmpty()) {
            readyForSweep();
        }
    }

    @Override
    public void onTileActorVanishStart(TileActor tileActor) {
        if (vanishing.isEmpty()) {
            notifyListeners(EVENT_TYPE.TILES_VANISH_START);
        }
        ready = false;
        vanishing.add(tileActor);
    }

    @Override
    public void onTileActorVanishFinish(TileActor tileActor) {
        vanishing.remove(tileActor);
        ready = !isAnimating();
        removeActor(tileActor);
        if (vanishing.isEmpty()) {
            notifyListeners(EVENT_TYPE.TILES_VANISH_FINISH);
        }
    }
}
