package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;

public class SinkTile extends BaseTile {

    static {
        CLASS_NAME = "SinkTile";
        DEBUG_MODE = false;
    }
    public static final Color COLOR_SINK = new Color(1.0f, 0.5f, 0.25f, 1.0f);

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
    public void setPower(Power power) {}

    @Override
    public void setBits(int bits) {}

}
