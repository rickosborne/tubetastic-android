package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class GfxCacheListener implements ApplicationListener {

    public static String CLASS_NAME = "GfxCacheListener";
    public static int RENDER_COUNT = (16 * 3) + 2; // 16 outlets, 3 colors, 2 extra

    private static class RenderItem {
        public String key;
        public BaseTile tile;
        public RenderItem(String key, BaseTile tile) {
            this.key = key;
            this.tile = tile;
        }
    }

    private Runnable onCompleteCallback;
    private OrthographicCamera camera;
    private Stage stage;
    private int width;
    private int height;
    private int tileSize;
    private int tileX;
    private int tileY;
    private ArrayDeque<BaseTile> renderQueue;
    private Actor lastTile;

    @Override
    public void create() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        stage = new Stage(width, height, true);
        tileSize = getTileSize(TubeTasticGame.COUNT_COLS, TubeTasticGame.COUNT_ROWS, width, height);
        tileX = (width - tileSize) / 2;
        tileY = (height - tileSize) / 2;
        Gdx.app.log(CLASS_NAME, String.format("create tileSize:%d", tileSize));
        renderQueue = new ArrayDeque<BaseTile>(RENDER_COUNT);
//        renderQueue.add(new SourceTile(0, 0, tileX, tileY, tileSize, null));
//        renderQueue.add(new SinkTile(0, 0, tileX, tileY, tileSize, null));
        renderQueue.add(new TubeTile(0, 0, tileX, tileY, tileSize, 15, null));
//        for (BaseTile.Power power : BaseTile.Power.values()) {
//            for (int bits = 1; bits <= 16; bits++) {
//                TubeTile tile = new TubeTile(0, 0, tileX, tileY, tileSize, bits, null);
//                tile.setPower(power);
//                renderQueue.add(tile);
//            }
//        }
        lastTile = null;
        configureGL();
        Gdx.graphics.setContinuousRendering(false);
//        Gdx.graphics.requestRendering();
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
            if (lastTile != null) {
                lastTile.remove();
            }
        if (lastTile == null) {
            lastTile = renderQueue.pop();
            stage.addActor(lastTile);
        }
        Gdx.app.log(CLASS_NAME, "render");
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