package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public enum ShapeRendererSingleton {
    INSTANCE;

    private ShapeRenderer shape = new ShapeRenderer();

    public ShapeRenderer getShape() {
        return shape;
    }

}
