package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class SourceTile extends BaseTile {

    public static final Color COLOR_SOURCE = new Color(0.25f, 0.5f, 1.0f, 1.0f);

    public SourceTile(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super(colNum, rowNum, x, y, size, board);
        init(colNum, rowNum, x, y, size, board);
    }

    @Override
    public void init(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super.init(colNum, rowNum, x, y, size, board);
        super.setPower(Power.SOURCED);
        outlets.setBits(Outlets.BIT_EAST);
        resize(x, y, size);
    }

    @Override
    public void resize(float x, float y, float size) {
        super.resize(x, y, size);
    }

    @Override
    public void setPower(Power power) {}

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.end();
        ShapeRenderer shape = new ShapeRenderer();
        float x = getX();
        float y = getY();
        float width = getWidth();
        float degrees = getRotation();
        ShapeDrawer.circle(shape, midpoint, midpoint, midpoint - (padding * 2), COLOR_SOURCE, degrees, x, y);
        ShapeDrawer.line(shape, midpoint, midpoint, width, midpoint, arcWidth, COLOR_ARC, degrees, x, y);
        batch.begin();
    }

}
