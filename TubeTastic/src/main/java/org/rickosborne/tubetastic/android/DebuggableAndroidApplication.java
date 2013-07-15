package org.rickosborne.tubetastic.android;

import android.util.Log;
import com.badlogic.gdx.backends.android.AndroidApplication;

public class DebuggableAndroidApplication extends AndroidApplication {

    protected static String CLASS_NAME;
    protected static boolean DEBUG_MODE;

    static {
        CLASS_NAME = "DebuggableAndroidApplication";
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
