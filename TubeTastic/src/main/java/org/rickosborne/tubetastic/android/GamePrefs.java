package org.rickosborne.tubetastic.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.badlogic.gdx.graphics.Color;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class GamePrefs {

    public static Color COLOR_ARC;
    public static Color COLOR_POWER_NONE;
    public static Color COLOR_POWER_SUNK;
    public static Color COLOR_POWER_SOURCED;
    public static Color COLOR_BACK;
    public static Color COLOR_SCORE;
    public static boolean SOUND_GLOBAL;
    public static boolean ACCEL_ENABLED;

    protected static String KEY_COLOR_CUSTOM = "color:custom";
    protected static String KEY_COLOR_ARC    = "color:arc";
    protected static String KEY_COLOR_SOURCE = "color:source";
    protected static String KEY_COLOR_SINK   = "color:sink";
    protected static String KEY_COLOR_NONE   = "color:none";
    protected static String KEY_COLOR_BACK   = "color:back";
    protected static String KEY_COLOR_SCORE  = "color:score";
    protected static String KEY_SOUND_GLOBAL = "sound:global";
    protected static String KEY_ACCEL_ENABLED = "accel:enabled";

    static {
        setDefaults();
    }

    protected static void loadFromContext(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.prefs_name), Context.MODE_PRIVATE);
        setDefaults();
        if (getBoolean(preferences, KEY_COLOR_CUSTOM, false)) {
            COLOR_ARC           = getColor(preferences, KEY_COLOR_ARC,    COLOR_ARC);
            COLOR_POWER_SOURCED = getColor(preferences, KEY_COLOR_SOURCE, COLOR_POWER_SOURCED);
            COLOR_POWER_SUNK    = getColor(preferences, KEY_COLOR_SINK,   COLOR_POWER_SUNK);
            COLOR_POWER_NONE    = getColor(preferences, KEY_COLOR_NONE,   COLOR_POWER_NONE);
            COLOR_BACK          = getColor(preferences, KEY_COLOR_BACK,   COLOR_BACK);
            COLOR_SCORE         = getColor(preferences, KEY_COLOR_SCORE,  COLOR_SCORE);
        }
        SOUND_GLOBAL = getBoolean(preferences, KEY_SOUND_GLOBAL, SOUND_GLOBAL);
        ACCEL_ENABLED = getBoolean(preferences, KEY_ACCEL_ENABLED, ACCEL_ENABLED);
    }

    protected static void setDefaults() {
        COLOR_ARC           = new Color(0.933333f, 0.933333f, 0.933333f, 1.0f);
        COLOR_POWER_NONE    = new Color(0.5f, 0.5f, 0.5f, 1.0f);
        COLOR_POWER_SUNK    = new Color(1.0f, 0.6f, 0f, 1.0f);
        COLOR_POWER_SOURCED = new Color(0f, 0.6f, 1.0f, 1.0f);
        COLOR_SCORE         = new Color(0.625f, 0.625f, 0.625f, 1.0f);
        COLOR_BACK          = Color.BLACK;
        SOUND_GLOBAL        = true;
        ACCEL_ENABLED       = true;
    }

    protected static boolean getBoolean(SharedPreferences preferences, String prefName, boolean defaultValue) {
        return preferences.getBoolean(prefName, defaultValue);
    }

    protected static Color getColor(SharedPreferences preferences, String prefName, Color defaultValue) {
        Color color = defaultValue;
        if (preferences.contains(prefName)) {
            String hexDefault = defaultValue.toString();
            int intDefault = ColorPickerPreference.convertToColorInt(hexDefault);
            int iColor = preferences.getInt(prefName, intDefault);
            String hexColor = ColorPickerPreference.convertToRGB(iColor).replace("#", "");
            color = Color.valueOf(hexColor);
        }
        return color;
    }

}
