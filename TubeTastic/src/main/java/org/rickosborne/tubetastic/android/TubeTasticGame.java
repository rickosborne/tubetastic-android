package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TubeTasticGame implements ApplicationListener {

    private static final int COUNT_COLS = 8;
    private static final int COUNT_ROWS = 7;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture texture;
    private Sprite sprite;
    private GameBoard board;

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        board = new GameBoard(COUNT_COLS, COUNT_ROWS, (int) w, (int) h);

        camera = new OrthographicCamera(1, h/w);
        batch = new SpriteBatch();

//        texture = new Texture(Gdx.files.internal("data/libgdx.png"));
//        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
//        TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);
//        sprite = new Sprite(region);
//        sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
//        sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
//        sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);
    }

    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        // Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // sprite.draw(batch);
        board.draw(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        board.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
