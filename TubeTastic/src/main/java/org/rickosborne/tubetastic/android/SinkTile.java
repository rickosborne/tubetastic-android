package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;

public class SinkTile extends BaseTile {

    public SinkTile(int colNum, int rowNum, GameBoard board) {
        super(colNum, rowNum, board);
        init(colNum, rowNum, board);
    }

    @Override
    public void init(int colNum, int rowNum, GameBoard board) {
        super.init(colNum, rowNum, board);
        super.setPower(Power.SUNK);
        outlets.setBits(Outlets.BIT_WEST);
    }

    @Override
    public void setPower(Power power) {}

    @Override
    public void setBits(int bits) {}

}
