package org.rickosborne.tubetastic.android;

import android.content.Context;
import android.content.SharedPreferences;

public class ScoreKeeper {

    private int highScore = 0;
    private String PREFS_SCORE_HIGH;
    private SharedPreferences prefs;

    public ScoreKeeper (Context context) {
        String prefsName = context.getString(R.string.prefs_name);
        PREFS_SCORE_HIGH = context.getString(R.string.prefs_score_high);
        prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        highScore = prefs.getInt(PREFS_SCORE_HIGH, 0);
    }

    public int getHighScore () {
        return highScore;
    }

    public void addScore (int newScore) {
        synchronized (ScoreKeeper.class) {
            if (newScore > highScore) {
                highScore = newScore;
                prefs.edit().putInt(PREFS_SCORE_HIGH, highScore).commit();
            }
        }
    }

}
