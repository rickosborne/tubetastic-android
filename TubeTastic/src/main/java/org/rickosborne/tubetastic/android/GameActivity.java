package org.rickosborne.tubetastic.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class GameActivity extends GdxActivity implements ShakeListener.ShakeHandler {

    public final static String ARG_RESUME = "resume";
    public final static String ARG_SCORE  = "score";
    public static final int COUNT_COLS = 7;
    public static final int COUNT_ROWS = 9;

    protected GameBoard board;
    protected boolean resume = true;
    protected ShakeListener shakeListener = new ShakeListener(this);

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
        (new BoardKeeper(getApplicationContext())).saveBoard(board);
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
        if (board != null) {
            board.resizeToMax(width, height);
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

    private GameBoard loadBoard() {
        BoardKeeper boardKeeper = new BoardKeeper(getApplicationContext());
        GameBoard newBoard = boardKeeper.loadBoard(width, height);
        if (newBoard != null) {
            newBoard.setScoreKeeper(new ScoreKeeper(getApplicationContext()));
            newBoard.resizeToMax(width, height);
            newBoard.addGameEventListener(new GameSound());
            newBoard.setRenderControls(this);
            newBoard.loadTiles();
        }
        return newBoard;
    }

    private void saveBoard() {
        (new BoardKeeper(getApplicationContext())).saveBoard(board);
    }

    private GameBoard createBoard() {
        GameBoard newBoard = new GameBoard(COUNT_COLS, COUNT_ROWS, width, height, new ScoreKeeper(getApplicationContext()));
        newBoard.randomizeTiles();
        newBoard.addGameEventListener(new GameSound());
        newBoard.setRenderControls(this);
        newBoard.loadTiles();
        return newBoard;
    }

    private void loadOrCreateBoard() {
        if (resume) {
            board = loadBoard();
        }
        if (board == null) {
            board = createBoard();
        }
        stage.clear();
        stage.addActor(board);
        board.begin();
    }

    public void setResume(Boolean resume) { this.resume = resume; }
    public int getScore() { return board.getScore(); }

    @Override
    public void onShake() {
        if (board.isSettled() && board.isReady()) {
            board.randomizeTiles();
            board.readyForSweep();
        }
    }
}
