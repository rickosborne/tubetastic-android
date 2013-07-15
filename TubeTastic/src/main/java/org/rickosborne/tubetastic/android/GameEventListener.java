package org.rickosborne.tubetastic.android;

import java.util.Set;

public interface GameEventListener {

    public void onSpinTile(BaseTile tile);

    public void onVanishTiles(Set<TubeTile> tiles);

    public void onDropTiles(Set<BoardSweeper.DroppedTile> tiles);

    public void onVanishBoard(GameBoard board);

    public void onRandomizeBoard(GameBoard board);

    public void onSettleBoard(GameBoard board);

}
