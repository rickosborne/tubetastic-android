package org.rickosborne.tubetastic.android;

public interface RenderControls {

    public void startRendering();

    public void stopRendering();

    public boolean isContinuousRendering();

    public void requestRender();

}

