package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;

public class ScoreActor extends DebuggableActor {

    static {
        CLASS_NAME = "ScoreActor";
        DEBUG_MODE = false;
    }

    public final static Color COLOR_SCORE_FORE = new Color(0.7f, 0.7f, 0.7f, 1.0f);
    public final static String SCORE_CHARACTERS = "0123456789";
    public final static String SCORE_FONT = "fonts/KiteOne-Regular.ttf";
    public final static float SCORE_PADDING = 0.125f;

    private BitmapFont font = null;
    private int score;
    private float scoreY;
    private float scoreX;

    public ScoreActor() {
        init();
    }

    public void init() {
        score = 0;
    }

    public void resize(float x, float y, float width, float height) {
        setPosition(x, y);
        setSize(width, height);
        float padding = height * SCORE_PADDING;
        int fontSize = (int) (height - (2 * padding));
        Vector2 coords = localToStageCoordinates(new Vector2(x, y));
        scoreY = y + (height - padding) + coords.y;
        scoreX = x + coords.x;
        debug("resize x:%.0f y:%.0f w:%.0f h:%.0f fontSize:%d scoreY:%.0f sx:%.0f sy:%.0f", x, y, width, height, fontSize, scoreY, coords.x, coords.y);
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(SCORE_FONT));
        font = generator.generateFont(fontSize, SCORE_CHARACTERS, false);
        font.setColor(COLOR_SCORE_FORE);
        font.setFixedWidthGlyphs(SCORE_CHARACTERS);
        generator.dispose();
    }

    public void setScore(int score) { this.score = score; }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        String scoreText = ((Integer) score).toString();
        BitmapFont.TextBounds bounds = font.getBounds(scoreText);
        font.draw(batch, scoreText, scoreX + ((getWidth() - bounds.width) * 0.5f), scoreY);
    }

}
