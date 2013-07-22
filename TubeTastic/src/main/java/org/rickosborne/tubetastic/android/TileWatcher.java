package org.rickosborne.tubetastic.android;

public interface TileWatcher {

    public void onTileSpin(BaseTile tile);

    public void onTilePower(BaseTile tile, Power fromPower, Power toPower);

    public void onTileVanish(BaseTile tile);

    public void onTileMove(BaseTile tile, int fromColNum, int fromRowNum, int toColNum, int toRowNum);

}
