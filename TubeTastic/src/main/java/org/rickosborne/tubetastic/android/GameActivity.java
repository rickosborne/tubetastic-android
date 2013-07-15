package org.rickosborne.tubetastic.android;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class GameActivity extends AndroidApplication {

    public final static String ARG_RESUME = "resume";
    public final static String ARG_SCORE  = "score";

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
        Log.d("GameActivity", String.format("onCreate resume:%b", wantResume));
        game.setResume(wantResume);
        initialize(game, cfg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.setAppContext(getApplicationContext());
        boolean wantResume = getIntent().getBooleanExtra(ARG_RESUME, true);
        Log.d("GameActivity", String.format("onResume resume:%b", wantResume));
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
        Log.d("GameActivity", String.format("setScore score:%d", score));
    }

    private void updateScore() {
        int score = game.getScore();
        Log.d("GameActivity", String.format("updateScore score:%d", score));
        setScore(score);
    }

}
