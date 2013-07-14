package org.rickosborne.tubetastic.android;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class SplashActivity extends Activity {

    public final static int REQUEST_GAME = 1;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);
        score = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScore();
        toggleResumeButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SplashActivity", String.format("onActivityResult %d %d", requestCode, resultCode));
        switch (requestCode) {
            case REQUEST_GAME:
                if (data != null) {
                    int newScore = data.getIntExtra(GameActivity.ARG_SCORE, 0);
                    if (newScore > score) {
                        score = newScore;
                        setScore();
                    }
                }
                break;
            default:
                Log.e("SplashActivity", String.format("onActivityResult unknown request:%d", requestCode));
        }
    }

    public void onNewGame (View sender) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.ARG_RESUME, false);
        startActivityForResult(i, REQUEST_GAME);
    }

    public void onResumeGame (View sender) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.ARG_RESUME, true);
        startActivityForResult(i, REQUEST_GAME);
    }

    private void setScore() {
        FontableTextView scoreView = (FontableTextView) findViewById(R.id.splash_score);
        String scoreTemplate = getString(R.string.high_score);
        if ((score > 0) && (scoreView != null) && (scoreTemplate != null)) {
            scoreView.setText(String.format(scoreTemplate, score));
            scoreView.setVisibility(View.VISIBLE);
        } else if (scoreView != null) {
            scoreView.setVisibility(View.INVISIBLE);
        }
    }

    private void updateScore() {
        int highScore = (new ScoreKeeper(getApplicationContext())).getHighScore();
        if (highScore > score) {
            score = highScore;
        }
        setScore();
    }

    private void toggleResumeButton() {
        BoardKeeper boardKeeper = new BoardKeeper(getApplicationContext());
        Button resumeButton = (Button) findViewById(R.id.splash_resumegame);
        if (resumeButton != null) {
            resumeButton.setEnabled(boardKeeper.couldResumeGame());
        }
    }
    
}
