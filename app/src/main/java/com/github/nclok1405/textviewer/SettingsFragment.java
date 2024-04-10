package com.github.nclok1405.textviewer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.preference.Preference;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Settings Screen Fragment
 */
public class SettingsFragment extends PreferenceFragment {
    static private final String TAG = SettingsFragment.class.getSimpleName();

    static public SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() { super(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

        // "Hide Toolbar when Scrolling" is unavailable before Android 5.0
        if (Build.VERSION.SDK_INT < 21) {
            final PreferenceCategory pCategory = (PreferenceCategory)findPreference("pref_textviewer");
            final Preference pHideToolbarWhenScrolling = findPreference("pref_textviewer_hide_toolbar_when_scrolling");
            if (pCategory != null && pHideToolbarWhenScrolling != null) {
                pCategory.removePreference(pHideToolbarWhenScrolling);
            }
        }

        // Update Version Info
        final Preference pAboutVersion = findPreference("about_version");
        if (pAboutVersion != null) {
            final PackageInfo pInfo = U.getSelfPackageInfo(getActivity());
            if (pInfo != null) pAboutVersion.setSummary(String.format(getText(R.string.pref_about_version).toString(), pInfo.versionName));

            pAboutVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException fallback1) {
                        try {
                            final Intent fallbackIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS);
                            fallbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(fallbackIntent);
                        } catch (ActivityNotFoundException fallback2) {
                            try {
                                final Intent fallbackIntent2 = new Intent(android.provider.Settings.ACTION_SETTINGS);
                                fallbackIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(fallbackIntent2);
                            } catch (ActivityNotFoundException e) {
                                Log.e(TAG, "This device has no settings screen", e);
                            }
                        }
                    }
                    return true;
                }
            });
        } else {
            Log.e(TAG, "Cannot find Preference: about_version");
        }

        // License
        final Preference pAboutLicense = findPreference("about_license");
        if (pAboutLicense != null) {
            pAboutLicense.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://opensource.org/license/mit"));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Cannot launch a Web Browser", e);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set padding for Preference view.
        // The ActionBar is in Overlay mode, and it'll overlap with top of the settings.
        final TypedValue tv = new TypedValue();
        if( getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true) ) {
            final int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
            view.setPadding(0, actionBarHeight, 0, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getActivity().getActionBar().setTitle(R.string.textviewer_action_settings);
        }
    }
}
