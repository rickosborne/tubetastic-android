package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class OptionsButtonActor extends Actor {

    protected Color backgroundColor;
    protected float touchDelay = 0.25f;

    public void onTouch() {
        backgroundColor = Color.LIGHT_GRAY;
        addAction(Actions.sequence(
                Actions.delay(touchDelay),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        backgroundColor = null;
                    }
                })
        ));
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.end();
        ShapeRenderer shape = new ShapeRenderer();
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();
        float seventh = h / 7f;
        float left = x + ((w - seventh) * 0.5f);
        Color color = getColor();
        Color back = backgroundColor;
        shape.begin(ShapeRenderer.ShapeType.FilledRectangle);
        if (back != null) {
            shape.setColor(back);
            shape.filledRect(x, y, w, h);
        }
        shape.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        shape.filledRect(left, y + seventh, seventh, seventh);
        shape.filledRect(left, y + seventh * 3, seventh, seventh);
        shape.filledRect(left, y + seventh * 5, seventh, seventh);
        shape.end();
        batch.begin();
    }
}
