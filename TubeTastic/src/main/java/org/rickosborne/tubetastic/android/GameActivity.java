package org.rickosborne.tubetastic.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class GameActivity extends GdxActivity implements ShakeListener.ShakeHandler {

    public final static String ARG_RESUME = "resume";
    public final static String ARG_SCORE  = "score";
    public static final int COUNT_COLS = 7;
    public static final int COUNT_ROWS = 9;

    protected BoardActor boardActor;
    protected boolean resume = true;
    protected ShakeListener shakeListener = new ShakeListener(this);
    protected Rectangle boardBounds;
    protected OptionsButtonActor optionsButtonActor;
    protected InputListener onClickOptionsListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            optionsButtonActor.onTouch();
            showPopup(null);
            return super.touchDown(event, x, y, pointer, button);
        }
    };

    @Override
    protected AndroidApplicationConfiguration getConfig() {
        AndroidApplicationConfiguration cfg = super.getConfig();
        cfg.useAccelerometer = true;
        return cfg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean wantResume = getIntent().getBooleanExtra(ARG_RESUME, true);
        setResume(wantResume);
        FreetypeActor.flushCache();
        boardBounds = new Rectangle(0, 0, 0, 0);
        clearColor = GamePrefs.COLOR_BACK;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_randomize_tiles:
                onShake();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean wantResume = getIntent().getBooleanExtra(ARG_RESUME, true);
        setResume(wantResume);
        FreetypeActor.flushCache();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            updateScore();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setScore(int score) {
        Intent result = new Intent();
        result.putExtra(ARG_SCORE, score);
        setResult(RESULT_OK, result);
    }

    private void updateScore() {
        int score = getScore();
        setScore(score);
    }

    @Override
    public void create() {
        super.create();
        loadOrCreateBoard();
    }

    @Override
    public void dispose() {
        saveBoard();
        super.dispose();
    }

    @Override
    public void render() {
        super.render();
        shakeListener.update(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        boardBounds.width = width;
        boardBounds.height = height;
        if (boardActor == null) {
            loadOrCreateBoard();
        }
        if (boardActor != null) {
            boardActor.resizeToFit(boardBounds);
        }
        if (!hasMenuButton) {
            float optionsSize = height / 28;
            optionsButtonActor.setBounds(width - optionsSize, 0, optionsSize, optionsSize);
        }
    }

    @Override
    public void pause() {
        super.pause();
        saveBoard();
    }

    @Override
    public void resume() {
        super.resume();
        loadOrCreateBoard();
    }

    private BoardActor loadBoard() {
        BoardKeeper boardKeeper = new BoardKeeper(getApplicationContext());
        GameBoard newBoard = boardKeeper.loadSavedBoard();
        if (newBoard != null) {
            boardActor = new BoardActor(newBoard, boardBounds);
            return boardActor;
        }
        return null;
    }

    private void saveBoard() {
        (new BoardKeeper(getApplicationContext())).saveBoard(boardActor.getGameBoard());
    }

    private BoardActor createBoard() {
        GameBoard newBoard = new GameBoard(COUNT_COLS, COUNT_ROWS);
        newBoard.randomizeTiles();
        boardActor = new BoardActor(newBoard, boardBounds);
        return boardActor;
    }

    private void loadOrCreateBoard() {
        if ((width == 0) || (height == 0)) {
            return;
        }
        if (resume) {
            loadBoard();
        }
        if ((boardActor == null) || !resume) {
            createBoard();
        }
        stage.clear();
        boardActor.setRenderControls(this);
        stage.addActor(boardActor);
        boardActor.setScoreKeeper(new ScoreKeeper(getApplicationContext()));
        boardActor.resizeToFit(boardBounds);
        if (GamePrefs.SOUND_GLOBAL) {
            boardActor.addGameEventListener(new GameSound());
        }
        boardActor.loadTiles();
        boardActor.begin();
        if (!hasMenuButton) {
            optionsButtonActor = new OptionsButtonActor();
            optionsButtonActor.setColor(0.5f, 0.5f, 0.5f, 1.0f);
            optionsButtonActor.addListener(onClickOptionsListener);
            optionsButtonActor.setTouchable(Touchable.enabled);
            stage.addActor(optionsButtonActor);
        }
    }

    public void setResume(Boolean resume) { this.resume = resume; }
    public int getScore() { return boardActor.getGameBoard().getScore(); }

    @Override
    public void onShake() {
        if (boardActor.getGameBoard().isSettled() && boardActor.isReady()) {
            boardActor.needsRandomizing = true;
//            boardActor.randomizeTiles();
//            boardActor.readyForSweep();
        }
    }

    protected void showPopup(View v) {
        if (hasMenuButton) {
            return;
        }
        final GameActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setItems(new CharSequence[]{
                        "Randomize Tiles",
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0: onShake(); break;
                        }
                    }
                });
                dialog.show();
            }
        });
    }



}
