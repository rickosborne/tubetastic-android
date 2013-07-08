package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;
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
    public void draw(ShapeRenderer shape) {
        shape.begin(ShapeRenderer.ShapeType.FilledCircle);
        shape.setColor(COLOR_SOURCE);
        shape.filledCircle(x + midpoint, y + midpoint, midpoint - padding);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.FilledRectangle);
        shape.setColor(COLOR_ARC);
        shape.filledRect(x + midpoint, y + midpoint - padding, midpoint, padding * 2);
        shape.end();
    }

}
