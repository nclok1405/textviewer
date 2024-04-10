package com.github.nclok1405.textviewer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

/**
 * Main TextViewer Fragment
 */
public class TextViewerFragment extends Fragment {
    static private final String TAG = TextViewerFragment.class.getSimpleName();
    static private final int REQUEST_OPEN = 1;

    /** Create new instance */
    static public TextViewerFragment newInstance() {
        return new TextViewerFragment();
    }
    static public TextViewerFragment newInstance(Uri uri) {
        final Bundle b = new Bundle();
        b.putString("strUri", uri.toString());
        final TextViewerFragment f = new TextViewerFragment();
        f.setArguments(b);
        return f;
    }

    private Uri mUri;
    private String mText = "";
    private int mScrollY;

    private ScrollView mScrollView;
    private TextView mTextViewTextViewer;
    private Button mButtonFileOpen;
    private MenuItem mMenuItemShare;
    private MenuItem mMenuItemView;
    private MenuItem mMenuItemEdit;
    private MenuItem mMenuItemScrollToTop;
    private MenuItem mMenuItemScrollToBottom;
    private MenuItem mMenuItemClose;
    private View mLinearLayoutTextViewer;

    /** Required empty public constructor */
    public TextViewerFragment() { super(); }

    /**
     * Get a SharedPreferences
     * @return SharedPreferences
     */
    private SharedPreferences pref() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_textviewer, menu);

        mMenuItemShare = menu.findItem(R.id.textviewer_action_share);
        mMenuItemView = menu.findItem(R.id.textviewer_action_view);
        mMenuItemEdit = menu.findItem(R.id.textviewer_action_edit);
        mMenuItemScrollToTop = menu.findItem(R.id.textviewer_action_scroll_to_top);
        mMenuItemScrollToBottom = menu.findItem(R.id.textviewer_action_scroll_to_bottom);
        mMenuItemClose = menu.findItem(R.id.textviewer_action_close);

        if (mUri == null) {
            setToolbarItemsVisibility(false);
        }
    }

    /**
     * Set toolbar items visible or invisible
     * @param visible Visibility
     */
    private void setToolbarItemsVisibility(boolean visible) {
        if (mMenuItemShare != null) mMenuItemShare.setVisible(visible);
        if (mMenuItemView != null) mMenuItemView.setVisible(visible);
        if (mMenuItemEdit != null) mMenuItemEdit.setVisible(visible);
        if (mMenuItemScrollToTop != null) mMenuItemScrollToTop.setVisible(visible);
        if (mMenuItemScrollToBottom != null) mMenuItemScrollToBottom.setVisible(visible);
        if (mMenuItemClose != null) mMenuItemClose.setVisible(visible);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        updateHideToolbarWhenScrolling();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_textviewer, container, false);

        mScrollView = (ScrollView)v.findViewById(R.id.scrollViewTextViewer);
        if (Build.VERSION.SDK_INT >= 21) {
            mScrollView.setNestedScrollingEnabled(true);
        }
        mScrollView.setVisibility(View.GONE);

        mTextViewTextViewer = (TextView)v.findViewById(R.id.textViewTextViewer);

        mButtonFileOpen = (Button)v.findViewById(R.id.buttonFileOpen);
        mButtonFileOpen.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               openFileSelector();
           }
        });

        mLinearLayoutTextViewer = v.findViewById(R.id.linearLayoutTextViewer);

        if (mUri == null) {
            // Load from Intent
            final Bundle b = getArguments();
            if (b != null) {
                final String strUri = b.getString("strUri");
                if (strUri != null) {
                    final Uri uri = Uri.parse(strUri);
                    mUri = uri;
                    loadText(uri);
                }
            } else {
                // Autoload
                final SharedPreferences pref = pref();
                final String strLastUri = pref.getString("lastUri", "");
                final boolean bAutoLoadOnLaunch = pref.getBoolean("pref_textviewer_auto_load_last_file_on_launch", true);
                final boolean bActivityBeingRecreated = pref.getBoolean("activityBeingRecreated", false);
                if (bActivityBeingRecreated) {
                    Log.i(TAG, "Activity is being recreated");
                }
                if ((bAutoLoadOnLaunch || bActivityBeingRecreated) && (strLastUri.length() > 0)) {
                    pref.edit().putBoolean("activityBeingRecreated", false).apply();
                    final Uri uri = Uri.parse(strLastUri);
                    mUri = uri;
                    loadText(uri, true);
                }
            }
        }

        return v;
    }

    /**
     * Open the "Select a File" screen of AndroidOS
     */
    private void openFileSelector() {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, REQUEST_OPEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Open
        if (item.getItemId() == R.id.textviewer_action_open) {
            openFileSelector();
            return true;
        }
        // Settings
        if (item.getItemId() == R.id.textviewer_action_settings) {
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            ft.replace(R.id.container, SettingsFragment.newInstance(), "main");
            ft.commit();
            return true;
        }
        // Share
        if (item.getItemId() == R.id.textviewer_action_share) {
            if (mText != null && mText.length() > 0) {
                try {
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, mText);
                    startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                    Toast.makeText(getActivity(), R.string.textviewer_error_nowhere_to_share, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Share Failed", e);
                }
            }
            return true;
        }
        // View
        if (item.getItemId() == R.id.textviewer_action_view) {
            if (mUri != null) {
                try {
                    final Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                    Toast.makeText(getActivity(), R.string.textviewer_error_no_viewer, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "View Failed", e);
                }
            }
            return true;
        }
        // Edit
        if (item.getItemId() == R.id.textviewer_action_edit) {
            if (mUri != null) {
                try {
                    final Intent intent = new Intent(Intent.ACTION_EDIT, mUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                    Toast.makeText(getActivity(), R.string.textviewer_error_no_editor, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Edit Failed", e);
                }
            }
            return true;
        }
        // Scroll to Top
        if (item.getItemId() == R.id.textviewer_action_scroll_to_top) {
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(ScrollView.FOCUS_UP);
                }
            });
            return true;
        }
        // Scroll to Bottom
        if (item.getItemId() == R.id.textviewer_action_scroll_to_bottom) {
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            return true;
        }
        // Close
        if (item.getItemId() == R.id.textviewer_action_close) {
            mText = "";
            mTextViewTextViewer.setText(mText);
            updateTextSize();
            mScrollView.setScrollY(0);
            mScrollY = 0;
            mUri = null;
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setTitle(R.string.app_name);
            }
            mButtonFileOpen.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
            setToolbarItemsVisibility(false);

            final SharedPreferences pref = pref();
            pref.edit().putString("lastUri", "").putInt("mScrollY", 0).apply();
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OPEN && resultCode == Activity.RESULT_OK && data != null) {
            mUri = data.getData();
            loadText(mUri);
        }
    }

    /**
     * Load a text file from the Uri
     * @param uri Uri of the text file
     */
    private void loadText(Uri uri) {
        loadText(uri, false);
    }

    /**
     * Load a text file from the Uri
     * @param uri Uri of the text file
     * @param isAutoLoad true if automatic load on launch (Ignore error, restore scroll Y position)
     */
    private void loadText(Uri uri, boolean isAutoLoad) {
        if (uri == null || getActivity() == null) return;
        final SharedPreferences pref = pref();
        InputStream in = null;
        try {
            final ContentResolver cr = getActivity().getContentResolver();
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setTitle(U.getUriFilename(uri, cr));
            }
            in = cr.openInputStream(uri);
            final byte[] by = U.readBytes(in);
            mText = new String(by, U.UTF_8);
            mTextViewTextViewer.setText(mText);
            updateTextSize();
            mButtonFileOpen.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            if (!isAutoLoad) {
                mScrollY = 0;
                mScrollView.setScrollY(0);
            } else {
                mScrollY = pref.getInt("mScrollY", 0);
                Log.d(TAG, "loadText autoload mScrollY:" + mScrollY);
                //mScrollView.setScrollY(mScrollY);
                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.scrollTo(0, mScrollY);
                    }
                });
            }
            setToolbarItemsVisibility(true);

            // Try to Persist Permission
            try {
                cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (Exception ignore_1) {
                try {
                    cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception persist_failure) {
                    Log.w(TAG, "Cannot Persist Permission", persist_failure);
                }
            }

            pref.edit().putString("lastUri", uri.toString()).apply();
        } catch (Exception e) {
            if (!isAutoLoad) {
                Log.e(TAG, "Could not load the text file", e);
                mText = getText(R.string.textviewer_error_load_failed) + "\n" + U.getStackTrace(e);
                mTextViewTextViewer.setText(mText);
                mScrollView.setScrollY(0);
                mScrollView.setVisibility(View.VISIBLE);
            } else {
                Log.w(TAG, "Could not autoload the text file", e);
            }
            mUri = null;
            pref.edit().putString("lastUri", "").apply();
        } finally {
            U.closeQuietly(in);
        }
    }

    /**
     * Load Text Size setting and apply it to text viewer
     */
    private void updateTextSize() {
        if (getActivity() != null && mTextViewTextViewer != null) {
            final SharedPreferences pref = pref();
            final String str_pref_textviewer_font_size = pref.getString("pref_textviewer_font_size", "18");
            float txtSize = 18f;
            try {
                txtSize = Float.parseFloat(str_pref_textviewer_font_size);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Invalid Text Size:" + str_pref_textviewer_font_size, nfe);
            }
            mTextViewTextViewer.setTextSize(txtSize);

            // Allow or disallow selection
            final boolean allowSelection = pref.getBoolean("pref_textviewer_allow_selection_and_copy", false);
            mTextViewTextViewer.setTextIsSelectable(allowSelection);
        }
    }

    /**
     * Read the Preferences and enable/disable the "toolbar hides when scrolling" feature.
     * Also updates the padding because the ActionBar gets in the way.
     */
    private void updateHideToolbarWhenScrolling() {
        final SharedPreferences pref = pref();
        final boolean enabled = Build.VERSION.SDK_INT >= 21 && pref.getBoolean("pref_textviewer_hide_toolbar_when_scrolling", true);

        if (Build.VERSION.SDK_INT >= 21 && (getActivity() != null) && (getActivity().getActionBar() != null)) {
            try {
                getActivity().getActionBar().setHideOnContentScrollEnabled(enabled);
            } catch (Exception e) {
                Log.w(TAG, "Failed to setHideOnContentScrollEnabled", e);
            }
        }

        // Update padding
        if (getActivity() != null && mScrollView != null && mLinearLayoutTextViewer != null) {
            final TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if( getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true) ) {
                actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
            }
            final int top = getResources().getDimensionPixelOffset(R.dimen.textviewer_top_padding);
            final int virt = getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
            final int horz = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);

            if (Build.VERSION.SDK_INT >= 21 && getActivity().getActionBar() !=null && getActivity().getActionBar().isHideOnContentScrollEnabled()) {
                mScrollView.setPadding(0, 0, 0, 0);
                mLinearLayoutTextViewer.setPadding(horz, top, horz, virt);
            } else {
                mScrollView.setPadding(0, actionBarHeight, 0, 0);
                mLinearLayoutTextViewer.setPadding(horz, virt, horz, virt);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mScrollView != null) {
            mScrollY = mScrollView.getScrollY();
            Log.d(TAG, "onDestroyView get mScrollY:" + mScrollY);

            if (getActivity() != null) pref().edit().putInt("mScrollY", mScrollY).apply();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mScrollView != null) {
            mScrollY = mScrollView.getScrollY();
            Log.d(TAG, "onPause get mScrollY:" + mScrollY);
            if (getActivity() != null) pref().edit().putInt("mScrollY", mScrollY).apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
            if (mUri != null) {
                getActivity().getActionBar().setTitle(U.getUriFilename(mUri, getActivity().getContentResolver()));
            } else {
                getActivity().getActionBar().setTitle(R.string.app_name);
            }
        }

        updateHideToolbarWhenScrolling();
        updateTextSize();

        if (mText.length() > 0) {
            if (mTextViewTextViewer != null) mTextViewTextViewer.setText(mText);
            if (mScrollView != null) {
                Log.d(TAG, "onResume mScrollY:" + mScrollY);
                mScrollView.setScrollY(mScrollY);
                mScrollView.setVisibility(View.VISIBLE);
            }
            if (mButtonFileOpen != null) mButtonFileOpen.setVisibility(View.GONE);
            setToolbarItemsVisibility(true);
        } else {
            if (mScrollView != null) {
                mScrollView.setVisibility(View.GONE);
            }
            if (mButtonFileOpen != null) mButtonFileOpen.setVisibility(View.VISIBLE);
            setToolbarItemsVisibility(false);
        }
    }
}
