package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class TubeTasticGame implements ApplicationListener {

    private static final int COUNT_COLS = 8;
    private static final int COUNT_ROWS = 7;

    private OrthographicCamera camera;
    private GameBoard board;
    private ShapeRenderer shapeRenderer;
    private Stage stage;

    @Override
    public void create() {
        // oooh look, it's a magic incantation!
        Gdx.gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        Gdx.gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
        Gdx.gl.glEnable(GL11.GL_LINE_SMOOTH);
        Gdx.gl.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        Gdx.gl.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        Gdx.gl.glEnable(GL11.GL_LINE_SMOOTH);
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        board = new GameBoard(COUNT_COLS, COUNT_ROWS, (int) w, (int) h);
        stage = new Stage(w, h, true);
        Gdx.input.setInputProcessor(stage);
        stage.addActor(board);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        board.resizeToMax(width, height);
        stage.setViewport(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

}
