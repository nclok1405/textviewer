package com.github.nclok1405.textviewer;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Custom Application class
 */
public class MyApplication extends Application {
    static private final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d(TAG, "Reset the activityBeingRecreated flag");
        pref.edit().putBoolean("activityBeingRecreated", false).apply();

        final boolean bAutoLoadOnLaunch = pref.getBoolean("pref_textviewer_auto_load_last_file_on_launch", true);
        if (!bAutoLoadOnLaunch) {
            Log.d(TAG, "Auto load disabled by settings so cleanup the last uri");
            pref.edit().putString("lastUri", "").putInt("mScrollY", 0).apply();
        }

        // Set Defaults
        final String pref_textviewer_font_size = pref.getString("pref_textviewer_font_size", "");
        try {
            Integer.parseInt(pref_textviewer_font_size);
        } catch (NumberFormatException nfe) {
            pref.edit().putString("pref_textviewer_font_size", "18").apply();
        }
        final String pref_textviewer_theme = pref.getString("pref_textviewer_theme", "");
        if ("".equals(pref_textviewer_theme)) {
            pref.edit().putString("pref_textviewer_theme", "dark").apply();
        }
    }
}
