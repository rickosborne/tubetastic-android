package org.rickosborne.tubetastic.android;

import java.util.Set;

public class BaseGameEventListener extends Debuggable implements GameEventListener {

    static {
        CLASS_NAME = "BaseGameEventListener";
        DEBUG_MODE = false;
    }

    @Override
    public void onSpinTile(BaseTile tile) {

    }

    @Override
    public void onVanishTiles(Set<TubeTile> tiles) {

    }

    @Override
    public void onDropTiles(Set<BoardSweeper.DroppedTile> tiles) {

    }

    @Override
    public void onVanishBoard(GameBoard board) {

    }

    @Override
    public void onRandomizeBoard(GameBoard board) {

    }

    @Override
    public void onSettleBoard(GameBoard board) {

    }
}
