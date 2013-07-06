package org.rickosborne.tubetastic.android;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);
        int highScore = ScoreKeeper.getHighScore();
        if (highScore > 0) {
            String scoreTemplate = getString(R.string.high_score);
            String scoreText = String.format(scoreTemplate, highScore);
            FontableTextView scoreView = (FontableTextView) findViewById(R.id.splash_score);
            scoreView.setText(scoreText);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

    public void onNewGame (View sender) {
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }
    
}
