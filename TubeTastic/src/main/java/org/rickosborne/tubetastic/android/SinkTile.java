package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class SinkTile extends BaseTile {

    public static final Color COLOR_SINK = new Color(1.0f, 0.5f, 0.25f, 1.0f);

    static {
        CACHE_KEY = "k";
    }

    public SinkTile(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super(colNum, rowNum, x, y, size, board);
        init(colNum, rowNum, x, y, size, board);
    }

    @Override
    public void init(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super.init(colNum, rowNum, x, y, size, board);
        super.setPower(Power.SUNK);
        outlets.setBits(Outlets.BIT_WEST);
        resize(x, y, size);
    }

    @Override
    public void resize(float x, float y, float size) {
        super.resize(x, y, size);
    }

    @Override
    public void setPower(Power power) {}

    @Override
    public void setBits(int bits) {}

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.draw(renderer.getTextureForTile(this), getX(), getY(), getWidth(), getHeight());
//        batch.end();
//        ShapeRenderer shape = new ShapeRenderer();
//        ShapeRenderer shape = ShapeRendererSingleton.INSTANCE.getShape();
//        float x = getX();
//        float y = getY();
//        float degrees = getRotation();
//        ShapeDrawer.circle(shape, midpoint, midpoint, midpoint - (padding * 2), COLOR_SINK, degrees, x, y);
//        ShapeDrawer.line(shape, 0, midpoint, midpoint, midpoint, arcWidth, COLOR_ARC, degrees, x, y);
//        batch.begin();
    }

}
