package org.rickosborne.tubetastic.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class SplashActivity extends GdxActivity {

    protected static String FONT_TITLE = "Satisfy-Regular";
    protected static String TEXT_TITLE1 = "Tube";
    protected static String TEXT_TITLE2 = "Tastic";
    protected static String TEXT_TITLE3 = "!";
    protected static String UNIQUE_TITLE = FreetypeActor.uniqueCharacters(TEXT_TITLE1 + TEXT_TITLE2 + TEXT_TITLE3);
    protected static String FONT_SCOREINST = "KiteOne-Regular";
    protected static String TEXT_SCORE = "High Score: %d";
    protected static String DIGITS_SCORE = "0123456789";
    protected static String TEXT_INST1 = "Tap squares to rotate them.";
    protected static String TEXT_INST2 = "Connect the left & right sides";
    protected static String TEXT_INST3_NEW = "to begin a new game.";
    protected static String TEXT_INST3_RES = "to resume your last game.";
    protected static String TEXT_INST3;
    protected static String TEXT_VER = "v. %s";
    protected static String UNIQUE_SCOREINST = FreetypeActor.uniqueCharacters(TEXT_SCORE + DIGITS_SCORE + TEXT_INST1 + TEXT_INST2 + TEXT_INST3_NEW + TEXT_INST3_RES);
    protected static String UNIQUE_VER = FreetypeActor.uniqueCharacters(TEXT_VER + DIGITS_SCORE);
    protected static Color COLOR_INST = GamePrefs.COLOR_ARC;
    protected static Color COLOR_VER = new Color(0.25f, 0.25f, 0.25f, 1.0f);
    public final static int REQUEST_GAME = 1;
    public final static int REQUEST_PREFS = 2;

    protected FreetypeActor titleActor1;
    protected FreetypeActor titleActor2;
    protected FreetypeActor titleActor3;
    protected FreetypeActor scoreActor;
    protected FreetypeActor instActor1;
    protected FreetypeActor instActor2;
    protected FreetypeActor instActor3;
    protected FreetypeActor verActor;
    protected BoardActor boardActor;
    protected OptionsButtonActor optionsButtonActor;
    protected InputListener onClickAboutListener = new InputListener(){
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            launchAbout();
            return super.touchDown(event, x, y, pointer, button);
        }
    };
    protected boolean hasMenuButton = false;

    protected InputListener onClickOptionsListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            optionsButtonActor.onTouch();
            showPopup(null);
            return super.touchDown(event, x, y, pointer, button);
        }
    };
    private int score;
    protected Rectangle boardBounds = new Rectangle(0, 0, 0, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerForContextMenu(graphics.getView());
        hasMenuButton = Build.VERSION.SDK_INT <= 10 || (Build.VERSION.SDK_INT >= 14 && ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey());
    }

    protected void removeListeners(Actor actor) {
        if (actor != null) {
            actor.removeListener(onClickAboutListener);
        }
    }

    protected void init() {
        GamePrefs.loadFromContext(getApplicationContext());
        stage.clear();
        if ((new BoardKeeper(getApplicationContext())).couldResumeGame()) {
            TEXT_INST3 = TEXT_INST3_RES;
        } else {
            TEXT_INST3 = TEXT_INST3_NEW;
        }
        removeListeners(titleActor1);
        removeListeners(titleActor2);
        removeListeners(titleActor3);
        titleActor1 = new FreetypeActor(FONT_TITLE, FreetypeActor.Alignment.WEST, UNIQUE_TITLE, false, GamePrefs.COLOR_POWER_SOURCED, TEXT_TITLE1);
        titleActor2 = new FreetypeActor(FONT_TITLE, FreetypeActor.Alignment.WEST, UNIQUE_TITLE, false, GamePrefs.COLOR_POWER_SUNK, TEXT_TITLE2);
        titleActor3 = new FreetypeActor(FONT_TITLE, FreetypeActor.Alignment.WEST, UNIQUE_TITLE, false, GamePrefs.COLOR_ARC, TEXT_TITLE3);
        scoreActor = new FreetypeActor(FONT_SCOREINST, FreetypeActor.Alignment.MIDDLE, UNIQUE_SCOREINST, false);
        instActor1 = new FreetypeActor(FONT_SCOREINST, FreetypeActor.Alignment.MIDDLE, UNIQUE_SCOREINST, false, COLOR_INST, TEXT_INST1);
        instActor2 = new FreetypeActor(FONT_SCOREINST, FreetypeActor.Alignment.MIDDLE, UNIQUE_SCOREINST, false, COLOR_INST, TEXT_INST2);
        instActor3 = new FreetypeActor(FONT_SCOREINST, FreetypeActor.Alignment.MIDDLE, UNIQUE_SCOREINST, false, COLOR_INST, TEXT_INST3);
        verActor = new FreetypeActor(FONT_SCOREINST, FreetypeActor.Alignment.WEST, UNIQUE_VER, false, COLOR_VER, getAppVersion());
        stage.addActor(titleActor1);
        stage.addActor(titleActor2);
        stage.addActor(titleActor3);
        stage.addActor(scoreActor);
        stage.addActor(instActor1);
        stage.addActor(instActor2);
        stage.addActor(instActor3);
        stage.addActor(verActor);
        titleActor1.setTouchable(Touchable.enabled);
        titleActor2.setTouchable(Touchable.enabled);
        titleActor3.setTouchable(Touchable.enabled);
        verActor.setTouchable(Touchable.enabled);
        titleActor1.addListener(onClickAboutListener);
        titleActor2.addListener(onClickAboutListener);
        titleActor3.addListener(onClickAboutListener);
        verActor.addListener(onClickAboutListener);
        scoreActor.setColor(GamePrefs.COLOR_ARC.r, GamePrefs.COLOR_ARC.g, GamePrefs.COLOR_ARC.b, GamePrefs.COLOR_ARC.a * 0.4f);
        if (!hasMenuButton) {
            optionsButtonActor = new OptionsButtonActor();
            optionsButtonActor.setColor(0.5f, 0.5f, 0.5f, 1.0f);
            optionsButtonActor.addListener(onClickOptionsListener);
            optionsButtonActor.setTouchable(Touchable.enabled);
            stage.addActor(optionsButtonActor);
        }
        FreetypeActor.flushCache();
    }

    @Override
    public void create() {
        super.create();
        init();
    }

    @Override
    public void resize(int width, int height) {
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
        scoreActor.setBounds(0, height / 10, width, scoreHeight);
        int instHeight = height / 27;
        float instY = height / 3;
        float instLineHeight = instHeight * 1.3f;
        instActor1.setFontSize(instHeight);
        instActor1.setBounds(0, instY + instLineHeight, width, instHeight * 2);
        instActor2.setFontSize(instHeight);
        instActor2.setBounds(0, instY, width, instHeight * 2);
        instActor3.setFontSize(instHeight);
        instActor3.setBounds(0, instY - instLineHeight, width, instHeight * 2);
        float optionsSize = height / 28;
        verActor.setFontSize((int) (optionsSize * 0.75f));
        verActor.setBounds(optionsSize * 0.5f, 0, width / 2, optionsSize);
        if (!hasMenuButton) {
            optionsButtonActor.setBounds(width - optionsSize, 0, optionsSize, optionsSize);
        }
        score = 0;
        updateScore();
        resetBoard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Log.d("SplashActivity", String.format("onActivityResult %d %d", requestCode, resultCode));
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
            case REQUEST_PREFS:
                init();
                resize(width, height);
                break;
            default:
                Log.e("SplashActivity", String.format("onActivityResult unknown request:%d", requestCode));
        }
        FreetypeActor.flushCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.splash, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onMenuItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.splash, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return onMenuItemSelected(item) || super.onContextItemSelected(item);
    }

    protected boolean onMenuItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                launchSettings();
                return true;
            case R.id.action_about:
                launchAbout();
                return true;
            case R.id.action_newgame:
                launchNew();
                return true;
        }
        return false;
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
        if (boardActor != null) {
            boardActor.removeAllGameEventListeners();
            boardActor.remove();
            boardActor = null;
        }
    }

    private void resetBoard() {
        clearBoard();
        if (stage == null) {
            return;
        }
        int boardHeight = height / 3;
        boardBounds.set(0, 4 * height / 9, width, boardHeight);
        final GameBoard gameBoard = BoardKeeper.loadFixedBoard(new BoardKeeper.SaveGameData(5, 2, "4A91146CB1", 0));
        boardActor = new BoardActor(gameBoard, boardBounds);
        boardActor.setRenderControls(this);
        stage.addActor(boardActor);
        boardActor.loadTiles(BoardKeeper.getBitsForBoard(gameBoard));
        if (GamePrefs.SOUND_GLOBAL) {
            boardActor.addGameEventListener(new GameSound());
        }
        boardActor.addGameEventListener(new BaseGameEventListener() {

            @Override
            public boolean onVanishBoard(GameBoard board) {
                return INTERRUPT_YES;
            }

            @Override
            public boolean onVanishTilesFinish() {
                clearBoard();
                launchResume();
                return INTERRUPT_YES;
            }

            @Override
            public boolean onAppearTiles() {
                return INTERRUPT_YES;
            }

        });
        boardActor.begin();
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

    protected void launchSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_PREFS);
    }

    protected void launchResume() {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.ARG_RESUME, true);
        startActivityForResult(i, SplashActivity.REQUEST_GAME);
    }

    protected void launchNew() {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.ARG_RESUME, false);
        startActivityForResult(i, SplashActivity.REQUEST_GAME);
    }

    protected String getAppVersion() {
        try {
            return String.format(TEXT_VER, getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }
        catch (PackageManager.NameNotFoundException e) {
            // don't care
        }
        catch (NullPointerException e) {
            // still don't care
        }
        return "";
    }

    protected void showPopup(View v) {
        if (hasMenuButton) {
            return;
        }
        final SplashActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setItems(new CharSequence[]{
                        "Settings",
                        "About",
                        "New Game From Zero"
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0: launchSettings(); break;
                            case 1: launchAbout(); break;
                            case 2: launchNew(); break;
                        }
                    }
                });
                dialog.show();
            }
        });
    }

}
