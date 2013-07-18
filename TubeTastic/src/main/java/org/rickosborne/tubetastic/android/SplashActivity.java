package org.rickosborne.tubetastic.android;

import android.content.Intent;
import android.util.Log;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import java.util.Set;

public class SplashActivity extends GdxActivity {

    protected static String FONT_TITLE = "Satisfy-Regular";
    protected static String TEXT_TITLE1 = "Tube";
    protected static String TEXT_TITLE2 = "Tastic";
    protected static String TEXT_TITLE3 = "!";
    protected static Color COLOR_TITLE1 = SourceTile.COLOR_POWER_SOURCED;
    protected static Color COLOR_TITLE2 = SinkTile.COLOR_POWER_SUNK;
    protected static Color COLOR_TITLE3 = BaseTile.COLOR_ARC;
    protected static String UNIQUE_TITLE = FreetypeActor.uniqueCharacters(TEXT_TITLE1 + TEXT_TITLE2 + TEXT_TITLE3);
    protected static String FONT_SCOREINST = "KiteOne-Regular";
    protected static String TEXT_SCORE = "High Score: %d";
    protected static String DIGITS_SCORE = "0123456789";
    protected static Color COLOR_SCORE = new Color(BaseTile.COLOR_ARC.r, BaseTile.COLOR_ARC.g, BaseTile.COLOR_ARC.b, BaseTile.COLOR_ARC.a * 0.4f);
    protected static String TEXT_INST1 = "Tap the squares to complete the";
    protected static String TEXT_INST2 = "connection and start a new game.";
    protected static String UNIQUE_SCOREINST = FreetypeActor.uniqueCharacters(TEXT_SCORE + DIGITS_SCORE + TEXT_INST1 + TEXT_INST2);
    protected static Color COLOR_INST = BaseTile.COLOR_ARC;
    public final static int REQUEST_GAME = 1;

    protected FreetypeActor titleActor1 = new FreetypeActor(FONT_TITLE, FreetypeActor.Alignment.WEST, UNIQUE_TITLE, false, COLOR_TITLE1, TEXT_TITLE1);
    protected FreetypeActor titleActor2 = new FreetypeActor(FONT_TITLE, FreetypeActor.Alignment.WEST, UNIQUE_TITLE, false, COLOR_TITLE2, TEXT_TITLE2);
    protected FreetypeActor titleActor3 = new FreetypeActor(FONT_TITLE, FreetypeActor.Alignment.WEST, UNIQUE_TITLE, false, COLOR_TITLE3, TEXT_TITLE3);
    protected FreetypeActor scoreActor = new FreetypeActor(FONT_SCOREINST, FreetypeActor.Alignment.MIDDLE, UNIQUE_SCOREINST, false);
    protected FreetypeActor instActor1 = new FreetypeActor(FONT_SCOREINST, FreetypeActor.Alignment.MIDDLE, UNIQUE_SCOREINST, false, COLOR_INST, TEXT_INST1);
    protected FreetypeActor instActor2 = new FreetypeActor(FONT_SCOREINST, FreetypeActor.Alignment.MIDDLE, UNIQUE_SCOREINST, false, COLOR_INST, TEXT_INST2);
    protected GameBoard gameBoard;
    protected InputListener onClickListener = new InputListener(){
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            launchAbout();
            return super.touchDown(event, x, y, pointer, button);
        }
    };
    private int score;

    @Override
    public void create() {
        super.create();
        stage.addActor(titleActor1);
        stage.addActor(titleActor2);
        stage.addActor(titleActor3);
        stage.addActor(scoreActor);
        stage.addActor(instActor1);
        stage.addActor(instActor2);
        titleActor1.setTouchable(Touchable.enabled);
        titleActor2.setTouchable(Touchable.enabled);
        titleActor3.setTouchable(Touchable.enabled);
        titleActor1.addListener(onClickListener);
        titleActor2.addListener(onClickListener);
        titleActor3.addListener(onClickListener);
        score = 0;
        updateScore();
        scoreActor.setColor(COLOR_SCORE);
        FreetypeActor.flushCache();
    }

    @Override
    public void resize(int width, int height) {
        if ((this.width == width) && (this.height == height)) {
            return;
        }
        super.resize(width, height);
        FreetypeActor.flushCache();
        float titleY = 5 * height / 6;
        int titleHeight = height / 9;
        titleActor1.setSize(width, titleHeight);
        titleActor1.setFontSize(titleHeight);
        titleActor2.setSize(width, titleHeight);
        titleActor2.setFontSize(titleHeight);
        titleActor3.setSize(width, titleHeight);
        titleActor3.setFontSize(titleHeight);
        FreetypeActor.TextBounds bounds1 = titleActor1.getBounds();
        FreetypeActor.TextBounds bounds2 = titleActor2.getBounds();
        FreetypeActor.TextBounds bounds3 = titleActor3.getBounds();
        float titleWidth = bounds1.width + bounds2.width + bounds3.width;
        float leftX = (width - titleWidth) / 2;
        titleActor1.setBounds(leftX, titleY, bounds1.width, titleHeight);
        titleActor2.setBounds(leftX + bounds1.width, titleY, bounds2.width, titleHeight);
        titleActor3.setBounds(leftX + bounds1.width + bounds2.width, titleY, bounds3.width, titleHeight);
        int scoreHeight = height / 20;
        scoreActor.setFontSize(scoreHeight);
        scoreActor.setBounds(0, height / 12, width, scoreHeight);
        int instHeight = height / 28;
        instActor1.setFontSize(instHeight);
        instActor1.setBounds(0, height / 4, width, instHeight * 2);
        instActor2.setFontSize(instHeight);
        instActor2.setBounds(0, height / 4 - instHeight * 1.3f, width, instHeight * 2);
        resetBoard();
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
                    }
                    setScore();
                    resetBoard();
                }
                break;
            default:
                Log.e("SplashActivity", String.format("onActivityResult unknown request:%d", requestCode));
        }
        FreetypeActor.flushCache();
    }

    private void updateScore() {
        int highScore = (new ScoreKeeper(getApplicationContext())).getHighScore();
        if (highScore > score) {
            score = highScore;
        }
        setScore();
    }

    private void setScore() {
        if (scoreActor != null) {
            if (score > 0) {
                scoreActor.setText(String.format(TEXT_SCORE, score));
            } else {
                scoreActor.setText(null);
            }
        }
    }

    private void clearBoard() {
        if (gameBoard != null) {
            gameBoard.removeAllGameEventListeners();
            gameBoard.remove();
            gameBoard = null;
        }
    }

    private void resetBoard() {
        clearBoard();
        if (stage == null) {
            return;
        }
        int boardHeight = height / 3;
        gameBoard = BoardKeeper.loadFixedBoard(width, boardHeight, new BoardKeeper.SaveGameData(4, 1, "4AB1", 0));
        gameBoard.setRenderControls(this);
        gameBoard.setY(gameBoard.getY() + height / 3);
        stage.addActor(gameBoard);
        final SplashActivity self = this;
        final GameBoard board = this.gameBoard;
        gameBoard.addGameEventListener(new GameSound() {
            @Override
            public boolean onDropTiles(Set<BoardSweeper.DroppedTile> tiles) {
                clearBoard();
                Intent i = new Intent(self, GameActivity.class);
                i.putExtra(GameActivity.ARG_RESUME, true);
                startActivityForResult(i, SplashActivity.REQUEST_GAME);
                return INTERRUPT_YES;
            }

            @Override
            public boolean onVanishBoard(GameBoard board) {
                return super.onVanishTiles(null);
            }
        });
        gameBoard.begin();
    }

    @Override
    public void resume() {
        super.resume();
        resetBoard();
    }

    @Override
    public void pause() {
        super.pause();
        clearBoard();
    }

    protected void launchAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }

}
