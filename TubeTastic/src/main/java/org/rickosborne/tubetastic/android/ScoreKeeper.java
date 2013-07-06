package org.rickosborne.tubetastic.android;

public enum ScoreKeeper {
    INSTANCE;

    static private int highScore = 0;

    static public int getHighScore () {
        int score;
        synchronized (ScoreKeeper.class) {
            score = highScore;
        }
        return score;
    }

    static public void addScore (int newScore) {
        synchronized (ScoreKeeper.class) {
            if (newScore > highScore) {
                highScore = newScore;
            }
        }
    }

}
