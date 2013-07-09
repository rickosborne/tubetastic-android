package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

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
        Gdx.app.log(String.format("SourceTile %d,%d", colNum, rowNum), String.format("draw @(%.0f,%.0f)", getX(), getY()));
        batch.end();
        ShapeRenderer shape = new ShapeRenderer();
        float x = getX();
        float y = getY();
        float width = getWidth();
        ShapeDrawer.circle(shape, x + midpoint, y + midpoint, midpoint - (padding * 2), COLOR_SOURCE);
        ShapeDrawer.line(shape, x + midpoint, y + midpoint, x + width, y + midpoint, arcWidth, COLOR_ARC);
        batch.begin();
    }

}
