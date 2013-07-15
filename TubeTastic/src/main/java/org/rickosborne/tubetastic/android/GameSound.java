package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.Set;

public class GameSound extends BaseGameEventListener {

    private static String PATH_BOOM = "audio/tube-boom.mp3";
    private static String PATH_SH   = "audio/tube-sh.mp3";
    private static float VOLUME_SH  = 0.5f;
    private static float VOLUME_BOOM = 1.0f;

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
    }

    protected SoundEffect boom;
    protected SoundEffect sh;

    public GameSound() {
        boom = new SoundEffect(PATH_BOOM, VOLUME_BOOM);
        sh = new SoundEffect(PATH_SH, VOLUME_SH);
    }

    @Override
    public void onSpinTile(BaseTile tile) {
        sh.play();
    }

    @Override
    public void onVanishTiles(Set<TubeTile> tiles) {
        boom.play();
    }
}
