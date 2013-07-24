package org.rickosborne.tubetastic.android;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewConfiguration;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class GdxActivity extends AndroidApplication implements ApplicationListener, RenderControls {

    protected int width = 0;
    protected int height = 0;
    protected Stage stage = null;
    protected Color clearColor = new Color(0, 0, 0, 1);
    protected float delta;
    protected boolean continuousRendering = true;
    protected boolean deltaNeedsReset = false;
    protected boolean hasMenuButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(this, getConfig());
        hasMenuButton = Build.VERSION.SDK_INT <= 10 || (Build.VERSION.SDK_INT >= 14 && ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey());
    }

    @Override
    public void create() {
        stage = new Stage(0, 0, true);
//        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(stage);
        configureGL();
    }

    @Override
    public void resize(int width, int height) {
        if ((this.width == width) && (this.height == height)) {
            return;
        }
        this.width = width;
        this.height = height;
        stage.setViewport(width, height, true);
        configureGL();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        delta = Gdx.graphics.getDeltaTime();
        if (deltaNeedsReset) {
            delta = 0;
            deltaNeedsReset = false;
        }
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
        Gdx.input.setInputProcessor(stage);
        configureGL();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    protected void configureGL() {
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

    protected AndroidApplicationConfiguration getConfig() {
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = true;
        cfg.numSamples = 2;
        cfg.useAccelerometer = false;
        cfg.useCompass = false;
        cfg.useWakelock = false;
        return cfg;
    }

    @Override
    public void startRendering() {
        if (continuousRendering) {
            return;
        }
        // Log.d("GdxActivity", "startRendering");
        continuousRendering = true;
        deltaNeedsReset = true;
    }

    @Override
    public void stopRendering() {
        if (!continuousRendering) {
            return;
        }
        // Log.d("GdxActivity", "stopRendering");
        continuousRendering = false;
        Gdx.graphics.requestRendering();
    }

    @Override
    public boolean isContinuousRendering() {
        return continuousRendering;
    }

    @Override
    public void requestRender() {
        // Log.d("GdxActivity", "requestRender");
        Gdx.graphics.requestRendering();
    }

}
