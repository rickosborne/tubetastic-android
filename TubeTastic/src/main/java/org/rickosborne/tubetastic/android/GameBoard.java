package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

import java.util.HashSet;

public class GameBoard extends Group {

    public static final float DELAY_SWEEP = 0.125f;
    public static final String CLASS_NAME = "GameBoard";

    public static enum TILE_TYPE {
        SOURCE,
        TUBE,
        SINK
    }

    private int rowCount = 0;
    private int colCount = 0;
    private BaseTile[][] board;
    private boolean settled = false;
    private int score = 0;
    private float tileSize = 0;
    private boolean ready = false;
//    private boolean needPowerSweep = false;
//    private boolean interruptPowerSweep = false;
    private boolean awaitingSweep = false;
    private BoardSweeper sweeper;
    private final float SCORE_HEIGHT = 0.08f;
    private float scoreHeight = 0;
    private ScoreActor scoreBoard;
    private ScoreKeeper scoreKeeper;
//    private HashSet<Actor> childActors = new HashSet<Actor>();
    protected final TileRenderer renderer = new TileRenderer();

    public GameBoard(int colCount, int rowCount, int maxWidth, int maxHeight) {
        super();
//        Gdx.app.log("GameBoard", String.format("rows:%d cols:%d w:%d h:%d", rowCount, colCount, maxWidth, maxHeight));
        init(colCount, rowCount, maxWidth, maxHeight);
//        Gdx.graphics.setContinuousRendering(true);
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
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 0; colNum < colCount; colNum++) {
                BaseTile tile;
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
    }

    public void begin() {
        final GameBoard self = this;
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                try {
                    if (!event.isHandled() && self.isReady() && self.isSettled()) {
                        Actor target = event.getTarget();
//                        Gdx.app.log("GameBoard;" + event.getTarget().getClass().getSimpleName(), String.format("touchDown x:%.0f y:%.0f", x, y));
                        BaseTile tile = self.tileAt(x, y);
                        if ((tile != null) && (tile instanceof TubeTile)) {
//                            toggleContinuousRendering(true);
                            ((TubeTile) tile).onTouchDown();
                        }
                    }
                }
                catch (Exception e) {
                    Gdx.app.error("GameBoard;" + event.getTarget().getClass().getSimpleName(), "touch exception:" + e.toString());
                }
                return true;
            }
        });
        setTouchable(Touchable.enabled);
        awaitingSweep = true;
//        readyForSweep();
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
//        Gdx.app.log("GameBoard", String.format("tileAt c:%d r:%d x:%.0f y:%.0f", colNum, rowNum, x, y));
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
            BaseTile existing = board[rowNum][colNum];
//            if (existing != null) {
//                removeActor(existing);
//            }
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
        Gdx.app.log(CLASS_NAME, "powerSweep begin");
        if (!awaitingSweep) {
            Gdx.app.log(CLASS_NAME, "powerSweep not awaitingSweep");
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
            Gdx.app.log(tilePower.tile.toString(), String.format("power %s -> %s", tilePower.tile.power, tilePower.power));
            tilePower.tile.setPower(tilePower.power);
        }
        // gather any connected tiles
        if (sweeper.connected.isEmpty()) {
            Gdx.app.log(CLASS_NAME, "ready");
            ready = true;
            settled = true;
        } else {
            sweeper.trackDrops(this);
            Gdx.app.log(CLASS_NAME, String.format("vanishing %d", sweeper.vanished.size()));
            for (TubeTile vanishTile : (HashSet<TubeTile>) sweeper.vanished.clone()) {
                if (vanishTile != null) {
                    Gdx.app.log(CLASS_NAME, String.format("vanishing %s", vanishTile));
                    vanishTile.vanish();
                }
            }
            if (settled) {
                Gdx.app.log(CLASS_NAME, String.format("score %d + %d", score, sweeper.vanished.size()));
                score += sweeper.vanished.size();
                scoreBoard.setScore(score);
            }
        }
        Gdx.app.log(CLASS_NAME, "powerSweep done");
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
//            Gdx.app.log("GameBoard", String.format("drop fell:%s remain:%d", tile.toString(), sweeper.fell.size()));
            setTile(destinationColNum, destinationRowNum, tile);
            sweeper.fell.remove(tile);
            if (sweeper.fell.isEmpty()) {
//                Gdx.app.log("GameBoard", "drop complete");
                readyForSweep();
                Gdx.app.log("GameBoard", String.format("memory %d %d", Gdx.app.getNativeHeap(), Gdx.app.getJavaHeap()));
            }
        } else {
            Gdx.app.error("GameBoard", String.format("drop MISSING:%s remain:%d", tile.toString(), sweeper.fell.size()));
        }
    }

    public void tileVanishComplete(TubeTile vanishedTile) {
        if (sweeper.vanished.contains(vanishedTile)) {
            Gdx.app.log("GameBoard", String.format("vanish removed:%s remain:%d", vanishedTile.toString(), sweeper.vanished.size()));
            setTile(vanishedTile.colNum, vanishedTile.rowNum, null);
            removeActor(vanishedTile);
            sweeper.vanished.remove(vanishedTile);
            if (sweeper.vanished.isEmpty()) {
//                Gdx.app.log("GameBoard", String.format("fall begin drop:%d add:%d", sweeper.dropped.size(), sweeper.added.size()));
                for (BoardSweeper.DroppedTile droppedTile : sweeper.dropped) {
//                    Gdx.app.log("GameBoard", String.format("dropping %s to c:%d r:%d x:%.0f y:%.0f", droppedTile.tile, droppedTile.colNum, droppedTile.rowNum, droppedTile.colX, droppedTile.rowY));
                    setTile(droppedTile.tile.colNum, droppedTile.tile.rowNum, null);
                    sweeper.fell.add(droppedTile.tile);
                }
                for (BoardSweeper.DroppedTile addedTile : sweeper.added) {
                    addedTile.tile = setTile(addedTile.colX, addedTile.rowY);
//                    TubeTile tile = new TubeTile(-2, -2, addedTile.colX, addedTile.rowY, tileSize, this);
//                    addActor(tile);
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
            Gdx.app.error("GameBoard", String.format("vanish MISSING:%s remain:%d", vanishedTile.toString(), sweeper.vanished.size()));
        }
    }

    public void interruptSweep() {
        Gdx.app.log(CLASS_NAME, String.format("interruptSweep %b -> false, actions:%d", awaitingSweep, getActions().size));
        awaitingSweep = false;
        clearActions();
    }

    public void readyForSweep() {
        Gdx.app.log(CLASS_NAME, String.format("readyForSweep %b -> true, actions:%d", awaitingSweep, getActions().size));
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

//    private void toggleContinuousRendering(boolean continuous) {
//        Gdx.app.log(CLASS_NAME, String.format("toggleContinuousRendering %b", continuous));
//        if (continuous) {
//            Gdx.graphics.requestRendering();
//        }
//        Gdx.graphics.setContinuousRendering(continuous);
//        if (!continuous) {
//            Gdx.graphics.requestRendering();
//        }
//    }

//    public void draw(SpriteBatch batch, float parentAlpha) {
//        batch.end();
//        ShapeRenderer shape = new ShapeRenderer();
//        ShapeRenderer shape = ShapeRendererSingleton.INSTANCE.getShape();
//        shape.begin(ShapeRenderer.ShapeType.FilledRectangle);
//        shape.identity();
//        shape.setColor(0.2f, 0.2f, 0.2f, 1.0f);
//        shape.rotate(0f, 0f, 1f, getRotation());
//        shape.filledRect(getX(), getY(), getWidth(), getHeight());
//        shape.end();
//        batch.begin();
//        super.draw(batch, parentAlpha);
//    }

}
