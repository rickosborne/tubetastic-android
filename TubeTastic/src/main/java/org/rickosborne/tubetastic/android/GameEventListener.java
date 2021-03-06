package org.rickosborne.tubetastic.android;

import java.util.Set;

public interface GameEventListener {

    public static boolean INTERRUPT_NO  = false;
    public static boolean INTERRUPT_YES = true;

    public boolean onSpinTile(BaseTile tile);

    public boolean onAppearTiles();

    public boolean onVanishTilesStart();
    public boolean onVanishTilesFinish();

    public boolean onDropTiles(Set<GameBoard.TileChangeMove> tiles);

    public boolean onVanishBoard(GameBoard board);

    public boolean onRandomizeBoard(GameBoard board);

    public boolean onSettleBoard(GameBoard board);

    public boolean onWakeBoard(GameBoard board);

}
