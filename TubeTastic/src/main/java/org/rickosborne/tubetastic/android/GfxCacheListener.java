package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import java.util.ArrayDeque;

public class GfxCacheListener extends Debuggable implements ApplicationListener {

    static {
        CLASS_NAME = "GfxCacheListener";
        DEBUG_MODE = false;
    }

    public static int RENDER_COUNT = (16 * 3) + 2; // 16 outlets, 3 colors, 2 extra

    private Runnable onCompleteCallback;
    private Stage stage;
    private int width;
    private int height;
    private int tileSize;
    private int tileX;
    private int tileY;
    private ArrayDeque<BaseTile> renderQueue;
    private Actor lastTile;
    private final TileRenderer renderer = new TileRenderer();

    @Override
    public void create() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        stage = new Stage(width, height, true);
        tileSize = getTileSize(TubeTasticGame.COUNT_COLS, TubeTasticGame.COUNT_ROWS, width, height);
        tileX = (width - tileSize) / 2;
        tileY = (height - tileSize) / 2;
        debug("create tileSize:%d", tileSize);
        renderQueue = new ArrayDeque<BaseTile>(RENDER_COUNT);
        renderQueue.add(new SourceTile(0, 0, tileX, tileY, tileSize, null));
        renderQueue.add(new SinkTile(0, 0, tileX, tileY, tileSize, null));
        for (BaseTile.Power power : BaseTile.Power.values()) {
            for (int bits = 1; bits < 16; bits++) {
                TubeTile tile = new TubeTile(0, 0, tileX, tileY, tileSize, bits, null);
                tile.setPower(power);
                renderQueue.add(tile);
            }
        }
        lastTile = null;
        lastTile = renderQueue.pop();
        ((BaseTile) lastTile).setRenderer(renderer);
        stage.addActor(lastTile);
        configureGL();
        Gdx.input.setInputProcessor(stage);
        stage.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                lastTile.addAction(Actions.sequence(
                        Actions.rotateBy(-360, 1f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                lastTile.remove();
                                stage.clear();
                                lastTile = renderQueue.pop();
                                if (lastTile == null) {
                                    onCompleteCallback.run();
                                }
                                else {
                                    ((BaseTile) lastTile).setRenderer(renderer);
                                    stage.addActor(lastTile);
                                }
                            }
                        })
                ));
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        stage.setViewport(width, height, true);
        configureGL();
    }

    @Override
    public void render() {
//        if (renderQueue.size() > 0) {
//            if (lastTile != null) {
//                stage.clear();
//                lastTile.remove();
//            }
//        Gdx.app.log(CLASS_NAME, "render");
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            float delta = Gdx.graphics.getDeltaTime();
            stage.act(delta);
            stage.draw();
//            TextureRegion tex = ScreenUtils.getFrameBufferTexture(tileX, tileY, tileSize, tileSize);
//            Sprite sprite = new Sprite(tex);

//        } else {
//            onCompleteCallback.run();
//        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    public void setOnCompleteCallback(Runnable onCompleteCallback) {
        this.onCompleteCallback = onCompleteCallback;
    }

    private void configureGL() {
        // oooh look, it's a magic incantation!
        Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
        Gdx.gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        Gdx.gl.glEnable(GL10.GL_POINT_SMOOTH);
        Gdx.gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
        Gdx.gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
        Gdx.gl.glEnable(GL11.GL_LINE_SMOOTH);
        Gdx.gl.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        Gdx.gl.glEnable(GL11.GL_POINT_SMOOTH);
        Gdx.gl.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);
        Gdx.gl.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private int getTileSize(int colCount, int rowCount, int width, int height) {
        float cr = Math.min(colCount, rowCount);
        float wh = Math.max(width, height);
        return MathUtils.nextPowerOfTwo(MathUtils.ceil(wh / cr));
    }
}
