package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;

public class ScoreActor extends FreetypeActor {

    public final static Color COLOR_SCORE_FORE = new Color(0.7f, 0.7f, 0.7f, 1.0f);
    public final static String SCORE_CHARACTERS = "0123456789";
    public final static String SCORE_FONT = "KiteOne-Regular";
    public final static Alignment SCORE_ALIGN = Alignment.MIDDLE;
    public final static float SCORE_PADDING = 0.125f;
    public final static boolean SCORE_MONOSPACE = true;

    public ScoreActor() {
        super(SCORE_FONT, SCORE_ALIGN, SCORE_CHARACTERS, SCORE_MONOSPACE);
        setColor(COLOR_SCORE_FORE);
    }

    public void resize(float x, float y, float width, float height) {
        setSize(width, height);
        setPosition(x, y);
        setFontSize((int) (height - (2 * height * SCORE_PADDING)));
    }

    public void setScore(int score) {
        setText(Integer.toString(score));
    }

}
