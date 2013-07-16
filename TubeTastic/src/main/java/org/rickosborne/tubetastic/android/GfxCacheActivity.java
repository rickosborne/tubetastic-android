package org.rickosborne.tubetastic.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class GfxCacheActivity extends AndroidApplication {

    private GfxCacheListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = true;
        cfg.numSamples = 4;
        cfg.useAccelerometer = false;
        cfg.useCompass = false;
        cfg.useWakelock = false;
        final AndroidApplication self = this;
        listener = new GfxCacheListener();
        listener.setOnCompleteCallback(new Runnable() {
            @Override
            public void run() {
                self.finish();
            }
        });
        initialize(listener, cfg);
    }

}