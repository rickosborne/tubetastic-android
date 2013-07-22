package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.Set;

public class GameSound extends BaseGameEventListener {

    private static String PATH_BOOM = "audio/tube-boom.mp3";
    private static String PATH_SH   = "audio/tube-sh.mp3";
    private static String PATH_FRABJOUS = "audio/tube-frabjous.mp3";
    private static float VOLUME_SH  = 0.5f;
    private static float VOLUME_BOOM = 1.0f;
    private static float VOLUME_FRABJOUS = 1.0f;

    private class SoundEffect {
        protected Sound sound;
        protected long id;
        protected float volume;
        public SoundEffect(String path, float volume) {
            sound = Gdx.audio.newSound(Gdx.files.internal(path));
            id = -1;
            this.volume = volume;
        }
        public void stop() {
            if (id != -1) {
                sound.stop(id);
                id = -1;
            }
        }
        public void play() {
            stop();
            id = sound.play(volume);
        }

        @Override
        protected void finalize() throws Throwable {
            sound.dispose();
            super.finalize();
        }
    }

    protected SoundEffect boom;
    protected SoundEffect sh;
    protected SoundEffect frabjous;

    public GameSound() {
        init();
    }

    private void init() {
        boom = new SoundEffect(PATH_BOOM, VOLUME_BOOM);
        sh = new SoundEffect(PATH_SH, VOLUME_SH);
        frabjous = new SoundEffect(PATH_FRABJOUS, VOLUME_FRABJOUS);
    }

    @Override
    public boolean onSpinTile(BaseTile tile) {
        sh.play();
        return super.onSpinTile(tile);
    }

    @Override
    public boolean onVanishTilesStart() {
        boom.play();
        return super.onVanishTilesStart();
    }

    @Override
    public boolean onVanishBoard(GameBoard board) {
        frabjous.play();
        return super.onVanishBoard(board);
    }

    @Override
    public boolean onWakeBoard(GameBoard board) {
        init();
        return super.onWakeBoard(board);
    }
}
