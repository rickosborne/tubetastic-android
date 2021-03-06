package org.rickosborne.tubetastic.android;

import android.util.Log;
import com.badlogic.gdx.graphics.Color;

public class ScoreActor extends FreetypeActor {

    public final static String SCORE_CHARACTERS = "0123456789";
    public final static String SCORE_FONT = "KiteOne-Regular";
    public final static Alignment SCORE_ALIGN = Alignment.MIDDLE;
    public final static float SCORE_PADDING = 0.125f;
    public final static boolean SCORE_MONOSPACE = true;

    public ScoreActor() {
        super(SCORE_FONT, SCORE_ALIGN, SCORE_CHARACTERS, SCORE_MONOSPACE);
        setColor(GamePrefs.COLOR_SCORE);
    }

    public void resize(float x, float y, float width, float height) {
        setSize(width, height);
        setPosition(x, y);
    }

    public void setScore(int score) {
        setText(Integer.toString(score));
    }

    @Override
    public void setHeight(float height) {
        if (getHeight() != height) {
            super.setHeight(height);
            setFontSize((int) (height - (2 * height * SCORE_PADDING)));
            // Log.d("ScoreActor", String.format("setHeight h:%.0f fsz:%d", height, fontSize));
        }
    }
}
