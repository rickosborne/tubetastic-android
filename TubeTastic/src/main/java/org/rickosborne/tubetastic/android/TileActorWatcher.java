package org.rickosborne.tubetastic.android;

public interface TileActorWatcher {

    public void onTileActorSpinStart(TileActor tileActor);
    public void onTileActorSpinFinish(TileActor tileActor);

    public void onTileActorMoveStart(TileActor tileActor);
    public void onTileActorMoveFinish(TileActor tileActor, int colNum, int rowNum);

    public void onTileActorAppearStart(TileActor tileActor);
    public void onTileActorAppearFinish(TileActor tileActor, int colNum, int rowNum);

    public void onTileActorVanishStart(TileActor tileActor);
    public void onTileActorVanishFinish(TileActor tileActor);

}
