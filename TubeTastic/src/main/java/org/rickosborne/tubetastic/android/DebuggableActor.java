package org.rickosborne.tubetastic.android;

import android.util.Log;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class DebuggableActor extends Actor {

    protected static String CLASS_NAME;
    protected static boolean DEBUG_MODE;
    static {
        CLASS_NAME = "DebuggableActor";
        DEBUG_MODE = false;
    }

    protected static void debug(String format, Object... params) {
        if (DEBUG_MODE) {
            Log.d(CLASS_NAME, String.format(format, params));
        }
    }

    protected static void error(String format, Object... params) {
        if (DEBUG_MODE) {
            Log.e(CLASS_NAME, String.format(format, params));
        }
    }

}
