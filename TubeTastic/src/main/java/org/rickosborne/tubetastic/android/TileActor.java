package org.rickosborne.tubetastic.android;

import android.util.Log;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class TileActor extends Actor implements TileWatcher {

    public static final float SIZE_PADDING = 1f / 16f;
    public static final float SIZE_ARCWIDTH = 1f / 8f;
    public static final Color COLOR_ARC = new Color(0.933333f, 0.933333f, 0.933333f, 1.0f);
    public static final Color COLOR_POWER_NONE    = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    public static final Color COLOR_POWER_SUNK    = new Color(1.0f, 0.6f, 0f, 1.0f);
    public static final Color COLOR_POWER_SOURCED = new Color(0f, 0.6f, 1.0f, 1.0f);
    public static final Color COLOR_SOURCE = new Color(0.25f, 0.5f, 1.0f, 1.0f);
    public static final Color COLOR_SINK = new Color(1.0f, 0.5f, 0.25f, 1.0f);
    public static final float DURATION_VANISH = 0.500f;
    public static final float DURATION_DROP   = 0.250f;
    public static final float DURATION_APPEAR = DURATION_VANISH + DURATION_DROP;
    public static final float DURATION_SPIN   = 0.150f;
    public static final float DEGREES_SPIN    = -90f;
    public static final float DEGREES_CIRCLE  = -360f;
    public static final float OPACITY_VANISH  = 0f;
    public static final float OPACITY_APPEAR  = 1f;
    public static final float SCALE_VANISH    = 0f;
    public static final float SCALE_APPEAR    = 1f;

    protected float padding = 0;
    protected BaseTile tile;
    protected TileRenderer renderer;
    protected float midpoint = 0f;
    protected float size = 0f;
    protected int spinRemain = 0;
    protected boolean isSpinning = false;
    protected boolean isVanishing = false;
    protected boolean isDropping = false;
    protected boolean isAppearing = false;
    protected TileActorWatcher watcher;

    public TileActor(BaseTile tile, float x, float y, float size) {
        this.tile = tile;
        tile.setWatcher(this);
        init(x, y, size);
    }

    protected void init(float x, float y, float size) {
        resize(x, y, size);
    }

    protected void resize(float x, float y, float size) {
        resize(size);
        setPosition((int) x, (int) y);
    }

    protected void resize(float size) {
        this.size = size;
        midpoint = MathUtils.floor(size * 0.5f);
        setOrigin(midpoint, midpoint);
        setSize(midpoint * 2, midpoint * 2);
        padding = MathUtils.floor(size * SIZE_PADDING);
    }

    public String toString() {
        return String.format("%s (%.0f,%.0f/%.0fx%.0f) %s", getClass().getSimpleName(), getX(), getY(), getWidth(), getHeight(), tile);
    }

    public void setRenderer(TileRenderer renderer) { this.renderer = renderer; }

    public void onTouchDown() {
        if (!getBoardActor().isWorking() && !isVanishing && !isDropping && !isAppearing) {
            if (isSpinning) {
                spinRemain++;
                // Log.d("TileActor", String.format("onTouchDown %s remain:%d", this, spinRemain));
            } else {
                spinRemain = 1;
                if (watcher == null) {
                    Log.e("TileActor", String.format("onTouchDown missing watcher %s", this));
                } else {
                    watcher.onTileActorSpinStart(this);
                }
            }
            tile.spin();
        } else {
            // Log.d("TileActor", String.format("onTouchDown %s vanishing:%b dropping:%b appearing:%b", this, isVanishing, isDropping, isAppearing));
        }
    }

    public void spin() {
        // Log.d("TileActor", String.format("spin %s remain:%d", this, spinRemain));
        final TileActor self = this;
        BoardActor board = getBoardActor();
        board.interruptSweep();
        if (spinRemain > 0) {
            isSpinning = true;
            tile.setPower(Power.NONE);
//            tile.spin();
            spinRemain--;
            // Log.d("TileActor", String.format("spin %s going remain:%d", this, spinRemain));
            addAction(Actions.sequence(
                    Actions.rotateBy(DEGREES_SPIN, DURATION_SPIN),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
//                            Gdx.app.log(self.toString(), String.format("spunTo r:%.0f x:%.0f y:%.0f w:%.0f h:%.0f", self.getRotation(), self.getX(), self.getY(), self.getWidth(), self.getHeight()));
                            self.spin();
                        }
                    })
            ));
        }
        else {
            int newRotation = MathUtils.round(getRotation()) % 360;
            if (newRotation <= -360) {
                newRotation += 360;
            }
//            Gdx.app.log(toString(), String.format("done spinning to:%d/%d", outletRotation, newRotation));
            if (newRotation != getRotation()) {
//                Gdx.app.log(toString(), String.format("rotate reset %d -> %d", outletRotation, newRotation));
                setRotation(newRotation);
            }
            isSpinning = false;
            if (watcher == null) {
                Log.e("TileActor", String.format("spin complete missing watcher %s", this));
            } else {
                // Log.d("TileActor", String.format("spin complete %s rotation:%d", this, newRotation));
                watcher.onTileActorSpinFinish(this);
            }
            board.readyForSweep();
        }
    }

    private BoardActor getBoardActor() {
        return (BoardActor) getParent();
    }

    public void vanish() {
        isVanishing = true;
        tile.setPower(Power.NONE);
        final TileActor self = this;
        if (watcher == null) {
            Log.e("TileActor", String.format("vanish missing watcher %s", this));
        } else {
            watcher.onTileActorVanishStart(this);
        }
        addAction(Actions.sequence(
                Actions.parallel(
                        Actions.alpha(OPACITY_VANISH, DURATION_VANISH),
                        Actions.rotateBy(DEGREES_CIRCLE, DURATION_VANISH),
                        Actions.scaleTo(SCALE_VANISH, SCALE_VANISH, DURATION_VANISH)
                ),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
//                            Gdx.app.log(self.toString(), "vanished");
                        self.onVanishComplete();
                    }
                })
        ));
    }

    public void dropTo(int colNum, int rowNum) {
        BoardActor board = getBoardActor();
        dropTo(colNum, rowNum, board.xForColNum(colNum), board.yForRowNum(rowNum));
    }

    public void dropTo(final int colNum, final int rowNum, final float x, final float y) {
        isDropping = true;
        tile.setPower(Power.NONE);
        if (watcher == null) {
            Log.e("TileActor", String.format("dropTo missing watcher %s", this));
        } else {
            watcher.onTileActorMoveStart(this);
        }
        final TileActor self = this;
        addAction(Actions.sequence(
                Actions.delay(DURATION_VANISH),
                Actions.moveTo((int) x, (int) y, DURATION_DROP),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
//                            Gdx.app.log(self.toString(), String.format("droppedTo col:%d row:%d x:%.0f y:%.0f", colNum, rowNum, x, y));
                        self.onDropComplete(colNum, rowNum);
                    }
                })
        ));
    }

    public void setAlpha(float alpha) {
        Color color = getColor();
        color.a = alpha;
        setColor(color);
    }

    public void appear(float x, float y) {
        isAppearing = true;
        if (watcher == null) {
            Log.e("TileActor", String.format("appear missing watcher %s", this));
        } else {
            watcher.onTileActorAppearStart(this);
        }
        setScale(SCALE_VANISH);
        setAlpha(OPACITY_VANISH);
        final TileActor self = this;
        addAction(Actions.sequence(
                Actions.delay(DURATION_APPEAR),
                Actions.parallel(
                        Actions.alpha(OPACITY_APPEAR, DURATION_VANISH),
                        Actions.rotateBy(DEGREES_CIRCLE, DURATION_VANISH),
                        Actions.scaleTo(SCALE_APPEAR, SCALE_APPEAR, DURATION_VANISH)
                ),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        self.onAppearComplete();
                    }
                })
        ));
    }

    public void onAppearComplete() {
        setRotation(0);
        // Log.d("TileActor", String.format("onAppearComplete %s", this));
        if (watcher == null) {
            Log.e("TileActor", String.format("onAppearComplete no watcher %s", this));
        } else {
            watcher.onTileActorAppearFinish(this, tile.colNum, tile.rowNum);
        }
        isAppearing = false;
    }

    public void onDropComplete(int colNum, int rowNum) {
        // Log.d("TileActor", String.format("onDropComplete %s col:%d row:%d", this, colNum, rowNum));
        if (watcher == null) {
            Log.e("TileActor", String.format("onDropComplete no watcher %s", this));
        } else {
            watcher.onTileActorMoveFinish(this, colNum, rowNum);
        }
        isDropping = false;
    }

    public void onVanishComplete() {
        // Log.d("TileActor", String.format("onVanishComplete %s", this));
        if (watcher == null) {
            Log.e("TileActor", String.format("onVanishComplete no watcher %s", this));
        } else {
            watcher.onTileActorVanishFinish(this);
        }
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        if (renderer == null) {
            Log.e("TileActor", String.format("renderer missing for %s for %s", this, this.tile));
            return;
        } else if (tile == null) {
            Log.e("TileActor", String.format("tile missing for %s", this));
            return;
        }
        Color tileColor = getColor();
        Color batchColor = batch.getColor();
        Color newColor = batchColor.cpy();
        newColor.a *= tileColor.a * parentAlpha;
        batch.setColor(newColor);
        batch.draw(renderer.getTextureRegionForTile(this.tile, (int) size), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        batch.setColor(batchColor);
    }

    @Override
    public void onTileSpin(BaseTile tile) {
        spin();
    }

    @Override
    public void onTilePower(BaseTile tile, Power fromPower, Power toPower) {

    }

    @Override
    public void onTileVanish(BaseTile tile) {
        vanish();
    }

    @Override
    public void onTileMove(BaseTile tile, int fromColNum, int fromRowNum, int toColNum, int toRowNum) {
        dropTo(toColNum, toRowNum);
    }

    public void setWatcher(TileActorWatcher watcher) { this.watcher = watcher; }

}
