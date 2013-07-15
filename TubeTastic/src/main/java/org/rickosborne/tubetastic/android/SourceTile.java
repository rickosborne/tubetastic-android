package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;

public class SourceTile extends BaseTile {

    static {
        CLASS_NAME = "SourceTile";
        DEBUG_MODE = false;
    }
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
    public void setPower(Power power) {}

    @Override
    public void setBits(int bits) {}

}
