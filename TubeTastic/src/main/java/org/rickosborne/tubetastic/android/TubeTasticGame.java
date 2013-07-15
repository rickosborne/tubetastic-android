package org.rickosborne.tubetastic.android;

import android.content.Context;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class TubeTasticGame implements ApplicationListener {

    public static final int COUNT_COLS = 7;
    public static final int COUNT_ROWS = 9;

    private GameBoard board;
    private Stage stage;
    private Context appContext;
    private int width;
    private int height;
    private boolean resume = true;

    @Override
    public void create() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        stage = new Stage(width, height, true);
        loadOrCreateBoard();
        Gdx.input.setInputProcessor(stage);
        configureGL();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float delta = Gdx.graphics.getDeltaTime();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
//        Gdx.app.log("TubeTasticGame", String.format("resize w:%d h:%d", width, height));
        this.width = width;
        this.height = height;
        board.resizeToMax(width, height);
        stage.setViewport(width, height, true);
        configureGL();
    }

    @Override
    public void pause() {
        BoardKeeper boardKeeper = new BoardKeeper(appContext);
        boardKeeper.saveBoard(board);
    }

    @Override
    public void resume() {
//        Gdx.app.log("TubeTasticGame", "resume");
        GameBoard newBoard = loadBoard();
        if (newBoard != null) {
            stage.clear();
            board = newBoard;
            stage.addActor(board);
            board.resizeToMax(width, height);
            board.begin();
        }
        configureGL();
    }

    private GameBoard loadBoard() {
        BoardKeeper boardKeeper = new BoardKeeper(appContext);
        GameBoard newBoard = boardKeeper.loadBoard(width, height);
        if (newBoard != null) {
            newBoard.setScoreKeeper(new ScoreKeeper(appContext));
            newBoard.resizeToMax(width, height);
        }
        return newBoard;
    }

    private GameBoard createBoard() {
        GameBoard newBoard = new GameBoard(COUNT_COLS, COUNT_ROWS, width, height);
        newBoard.setScoreKeeper(new ScoreKeeper(appContext));
        newBoard.randomizeTiles();
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

    private void configureGL() {
        // oooh look, it's a magic incantation!
        Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
        Gdx.gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        Gdx.gl.glEnable(GL10.GL_POINT_SMOOTH);
        Gdx.gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
        Gdx.gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
        Gdx.gl.glEnable(GL11.GL_LINE_SMOOTH);
        Gdx.gl.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        Gdx.gl.glEnable(GL11.GL_POINT_SMOOTH);
        Gdx.gl.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);
        Gdx.gl.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setAppContext(Context context) { this.appContext = context; }
    public void setResume(Boolean resume) { this.resume = resume; }
    public int getScore() { return board.getScore(); }

}
