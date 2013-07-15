package org.rickosborne.tubetastic.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class GameActivity extends DebuggableAndroidApplication {

    public final static String ARG_RESUME = "resume";
    public final static String ARG_SCORE  = "score";
    static {
        CLASS_NAME = "GameActivity";
        DEBUG_MODE = false;
    }

    private TubeTasticGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = true;
        cfg.numSamples = 2;
        cfg.useAccelerometer = true;
        cfg.useCompass = false;
        cfg.useWakelock = false;
        game = new TubeTasticGame();
        game.setAppContext(getApplicationContext());
        boolean wantResume = getIntent().getBooleanExtra(ARG_RESUME, true);
        debug("onCreate resume:%b", wantResume);
        game.setResume(wantResume);
        initialize(game, cfg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.setAppContext(getApplicationContext());
        boolean wantResume = getIntent().getBooleanExtra(ARG_RESUME, true);
        debug("onResume resume:%b", wantResume);
        game.setResume(wantResume);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //set result and finish()
            updateScore();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setScore(int score) {
        Intent result = new Intent();
        result.putExtra(ARG_SCORE, score);
        setResult(RESULT_OK, result);
        debug("setScore score:%d", score);
    }

    private void updateScore() {
        int score = game.getScore();
        debug("updateScore score:%d", score);
        setScore(score);
    }

}
