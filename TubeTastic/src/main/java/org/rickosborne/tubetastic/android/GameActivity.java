package org.rickosborne.tubetastic.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Rectangle;

public class GameActivity extends GdxActivity implements ShakeListener.ShakeHandler {

    public final static String ARG_RESUME = "resume";
    public final static String ARG_SCORE  = "score";
    public static final int COUNT_COLS = 7;
    public static final int COUNT_ROWS = 9;

    protected BoardActor boardActor;
    protected boolean resume = true;
    protected ShakeListener shakeListener = new ShakeListener(this);
    protected Rectangle boardBounds;

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
    }

    public void setResume(Boolean resume) { this.resume = resume; }
    public int getScore() { return boardActor.getGameBoard().getScore(); }

    @Override
    public void onShake() {
        if (boardActor.getGameBoard().isSettled() && boardActor.isReady()) {
            boardActor.randomizeTiles();
            boardActor.readyForSweep();
        }
    }
}
