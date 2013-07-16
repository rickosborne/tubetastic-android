package org.rickosborne.tubetastic.android;

import java.util.Set;

public class BaseGameEventListener implements GameEventListener {

    @Override
    public boolean onSpinTile(BaseTile tile) {
        return INTERRUPT_NO;
    }

    @Override
    public boolean onVanishTiles(Set<TubeTile> tiles) {
        return INTERRUPT_NO;
    }

    @Override
    public boolean onDropTiles(Set<BoardSweeper.DroppedTile> tiles) {
        return INTERRUPT_NO;
    }

    @Override
    public boolean onVanishBoard(GameBoard board) {
        return INTERRUPT_NO;
    }

    @Override
    public boolean onRandomizeBoard(GameBoard board) {
        return INTERRUPT_NO;
    }

    @Override
    public boolean onSettleBoard(GameBoard board) {
        return INTERRUPT_NO;
    }

    @Override
    public boolean onWakeBoard(GameBoard board) {
        return INTERRUPT_NO;
    }
}
