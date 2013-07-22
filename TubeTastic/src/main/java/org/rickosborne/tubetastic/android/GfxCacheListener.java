package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import java.util.ArrayDeque;

public class GfxCacheListener implements ApplicationListener {

    public static int RENDER_COUNT = (16 * 3) + 2; // 16 outlets, 3 colors, 2 extra

    private Runnable onCompleteCallback;
    private Stage stage;
    private int width;
    private int height;
    private int tileSize;
    private ArrayDeque<TileActor> renderQueue;
    private TileActor lastTile;
    private final TileRenderer renderer = new TileRenderer();

    @Override
    public void create() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        stage = new Stage(width, height, true);
        tileSize = getTileSize(GameActivity.COUNT_COLS, GameActivity.COUNT_ROWS, width, height);
        renderQueue = new ArrayDeque<TileActor>(RENDER_COUNT);
        renderQueue.add(new TileActor(new SourceTile(0, 0, null), 0, 0, tileSize));
        renderQueue.add(new TileActor(new SinkTile(0, 0, null), 0, 0, tileSize));
        for (Power power : Power.values()) {
            for (int bits = 1; bits < 16; bits++) {
                TubeTile tile = new TubeTile(0, 0, bits, null);
                tile.setPower(power);
                renderQueue.add(new TileActor(tile, 0, 0, tileSize));
            }
        }
        lastTile = null;
        lastTile = renderQueue.pop();
        lastTile.setRenderer(renderer);
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
                                    lastTile.setRenderer(renderer);
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
