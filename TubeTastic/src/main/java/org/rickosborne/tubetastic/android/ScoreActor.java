package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;

public class ScoreActor extends Actor {

    public final static Color COLOR_SCORE_BACK = new Color(0.3f, 0.3f, 0.3f, 1.0f);
    public final static Color COLOR_SCORE_FORE = new Color(0.7f, 0.7f, 0.7f, 1.0f);
    public final static float CORNER_RADIUS = 0.25f;
    public final static String SCORE_CHARACTERS = "0123456789";
    public final static String SCORE_FONT = "fonts/KiteOne-Regular.ttf";
    private BitmapFont font;

    public ScoreActor() {
        init();
    }

    public void init() {
//        font = TrueTypeFontFactory.
    }

    public void resize(float x, float y, float width, float height) {
        setPosition(x, y);
        setSize(width, height);
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.end();
        ShapeRenderer shape = new ShapeRenderer();
        shape.begin(ShapeRenderer.ShapeType.FilledRectangle);
        shape.identity();
        shape.setColor(COLOR_SCORE_BACK);
        shape.filledRect(getX(), getY(), getWidth(), getHeight());
        shape.end();
        batch.begin();
//        super.draw(batch, parentAlpha);
    }

}
