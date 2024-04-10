package com.github.nclok1405.textviewer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * The main activity. This application relies on Fragments for most things.
 */
public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request the ActionBar Overlay Mode
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        // Preferences
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        // Set Theme
        final String pref_textviewer_theme = pref.getString("pref_textviewer_theme", "");
        if ("light".equals(pref_textviewer_theme)) {
            setTheme(R.style.Theme_TextViewer_Light);
        } else if ("black".equals(pref_textviewer_theme)) {
            setTheme(R.style.Theme_TextViewer_Black);
        } else {
            setTheme(R.style.Theme_TextViewer);
        }

        // Setup GUI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // Set Fragment
            final FragmentManager fm = getFragmentManager();
            final Intent intent = getIntent();
            final Uri uri = (intent == null) ? null : intent.getData();
            final String action = (intent == null) ? "" : intent.getAction();
            final Fragment f = (intent != null && uri != null && Intent.ACTION_VIEW.equals(action)) ? TextViewerFragment.newInstance(uri) : TextViewerFragment.newInstance();
            final FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, f, "main").commit();
        }
    }

    // Handles the Left Arrow button (officially and confusingly named "Up") of ActionBar
    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("pref_textviewer_theme".equals(key)) {
            // Restart Activity when theme is changed
            sharedPreferences.edit().putBoolean("activityBeingRecreated", true).apply();
            recreate();
        }
    }
}
