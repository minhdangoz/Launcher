package com.android.launcher3;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.android.launcher3.list.PinnedHeaderListView;
import com.android.launcher3.settings.LauncherSettingActivity;
import com.android.launcher3.settings.SettingsPinnedHeaderAdapter;
import com.webeye.launcher.R;

public class OverviewSettingsPanel {
    public static final String ANDROID_SETTINGS = "com.android.settings";
    public static final String ANDROID_PROTECTED_APPS =
            "com.android.settings.applications.ProtectedAppsActivity";
    public static final int HOME_SETTINGS_POSITION = 0;
    public static final int DRAWER_SETTINGS_POSITION = 1;
    public static final int APP_SETTINGS_POSITION = 2;

    private Launcher mLauncher;
    private View mOverviewPanel;
    private SettingsPinnedHeaderAdapter mSettingsAdapter;
    private PinnedHeaderListView mListView;

    OverviewSettingsPanel(Launcher launcher, View overviewPanel) {
        mLauncher = launcher;
        mOverviewPanel = overviewPanel;
    }

    // One time initialization of the SettingsPinnedHeaderAdapter
    public void initializeAdapter() {
        // Settings pane Listview
        mListView = (PinnedHeaderListView) mLauncher
                .findViewById(R.id.settings_home_screen_listview);
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        Resources res = mLauncher.getResources();
        String[] headers = new String[] {
                res.getString(R.string.home_screen_settings),
                res.getString(R.string.drawer_settings),
                res.getString(R.string.app_settings)};

        String[] values = new String[]{
                res.getString(R.string.home_screen_search_text),
                res.getString(R.string.scroll_effect_text),
                res.getString(R.string.icon_labels),
                res.getString(R.string.scrolling_wallpaper),
                res.getString(R.string.grid_size_text)};

        String[] valuesDrawer = new String[] {
                res.getString(R.string.drawer_type),
                res.getString(R.string.scroll_effect_text),
                res.getString(R.string.drawer_sorting_text),
                res.getString(R.string.icon_labels)};

        String[] valuesApp = new String[] {
                res.getString(R.string.larger_icons_text),
                res.getString(R.string.protected_app_settings)};


        mSettingsAdapter = new SettingsPinnedHeaderAdapter(mLauncher);
        mSettingsAdapter.setHeaders(headers);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.mPinnedHeaderCount = headers.length;

        mSettingsAdapter.changeCursor(HOME_SETTINGS_POSITION, createCursor(headers[0], values));
        mSettingsAdapter.changeCursor(DRAWER_SETTINGS_POSITION, createCursor(headers[1],
                valuesDrawer));
        mSettingsAdapter.changeCursor(APP_SETTINGS_POSITION, createCursor(headers[2], valuesApp));
        mListView.setAdapter(mSettingsAdapter);
    }

    private Cursor createCursor(String header, String[] values) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", header});
        int count = values.length;
        for (int i = 0; i < count; i++) {
            cursor.addRow(new Object[]{i, values[i]});
        }
        return cursor;
    }

    // One time View setup
    public void initializeViews() {
        mOverviewPanel.setAlpha(0f);
        mOverviewPanel
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        ((SlidingUpPanelLayout) mOverviewPanel)
                .setPanelSlideListener(new SettingsSimplePanelSlideListener());

        //Quick Settings Buttons
        View widgetButton = mOverviewPanel.findViewById(R.id.widget_button_android);
        widgetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mLauncher.showAllApps(true, AppsCustomizePagedView.ContentType.Widgets, true);
            }
        });
        widgetButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        View wallpaperButton = mOverviewPanel.findViewById(R.id.wallpaper_button_android);
        wallpaperButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mLauncher.setFullScreen(false);
                if (mLauncher.getWorkspace().isInOverviewMode()) {
                    mLauncher.getWorkspace().exitOverviewMode(true);
                }
                mLauncher.onClickWallpaperPicker(arg0);
            }
        });
        wallpaperButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        View settingsButton = mOverviewPanel.findViewById(R.id.settings_button_android);
        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mLauncher.getWorkspace().isSwitchingState()) {
                    mLauncher.startSettings();
                }
            }
        });
        settingsButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
        View preferenceButton = mOverviewPanel.findViewById(R.id.preference_button_android);
        preferenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLauncher.closeFolder();
                mLauncher.setFullScreen(false);
                if (mLauncher.getWorkspace().isInOverviewMode()) {
                    mLauncher.getWorkspace().exitOverviewMode(true);
                }
                /*Lenovo-sw zhangyj19 20150806 add setting function begin*/
                //mLauncher.showSettingsPanel();
                Intent intent = new Intent(mLauncher, LauncherSettingActivity.class);
                mLauncher.startActivity(intent);
                /*Lenovo-sw zhangyj19 20150806 add setting function end*/
            }
        });
        preferenceButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */

        View defaultScreenButton = mOverviewPanel.findViewById(R.id.default_screen_button);
        defaultScreenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mLauncher.getWorkspace().isSwitchingState()) {
                    mLauncher.getWorkspace().onClickDefaultScreenButton();
                }
            }
        });

        defaultScreenButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        /* Lenovo-SW zhaoxin5 20150119 change the menu style */
        //Handle
        /*View v = mOverviewPanel.findViewById(R.id.settings_pane_header);
        ((SlidingUpPanelLayout) mOverviewPanel).setEnableDragViewTouchEvents(true);
        ((SlidingUpPanelLayout) mOverviewPanel).setDragView(v);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SlidingUpPanelLayout) mOverviewPanel).isExpanded()) {
                    ((SlidingUpPanelLayout) mOverviewPanel).collapsePane();
                } else {
                    ((SlidingUpPanelLayout) mOverviewPanel).expandPane();
                }
            }
        });*/
        /* Lenovo-SW zhaoxin5 20150119 change the menu style */
    }

    @SuppressWarnings("unused")
    public void update() {
        Resources res = mLauncher.getResources();
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
        View widgetButton = mOverviewPanel.findViewById(R.id.widget_button_android);
        View wallpaperButton = mOverviewPanel
                .findViewById(R.id.wallpaper_button_android);
        View settingsButton = mOverviewPanel.findViewById(R.id.settings_button_android);
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
        View defaultHomePanel = mOverviewPanel.findViewById(R.id.default_screen_button);

        boolean isAllAppsVisible = mLauncher.isAllAppsVisible();

        PagedView pagedView = !isAllAppsVisible ? mLauncher.getWorkspace()
                : mLauncher.getAppsCustomizeContent();

        defaultHomePanel.setVisibility((pagedView.getPageCount() > 1) ?
                View.VISIBLE : View.GONE);

        if (mLauncher.isAllAppsVisible()) {
            mSettingsAdapter.changeCursor(0, createCursor(res
                    .getString(R.string.home_screen_settings), new String[]{}));
        } else {
            String[] values = new String[] {
                    res.getString(R.string.home_screen_search_text),
                    res.getString(R.string.scroll_effect_text),
                    res.getString(R.string.icon_labels),
                    res.getString(R.string.scrolling_wallpaper),
                    res.getString(R.string.grid_size_text)};
            mSettingsAdapter.changeCursor(0, createCursor(res
                    .getString(R.string.home_screen_settings), values));
        }

        // Make sure overview panel is drawn above apps customize and collapsed
        mOverviewPanel.bringToFront();
        mOverviewPanel.invalidate();

        ((SlidingUpPanelLayout) mOverviewPanel).setPanelHeight(isAllAppsVisible ?
                res.getDimensionPixelSize(R.dimen.settings_pane_handle)
                : res.getDimensionPixelSize(R.dimen.sliding_panel_padding));
    }

    public void notifyDataSetInvalidated() {
        mSettingsAdapter.notifyDataSetInvalidated();
    }


    class SettingsSimplePanelSlideListener extends SlidingUpPanelLayout.SimplePanelSlideListener {
    	/* Lenovo-SW zhaoxin5 20150119 change the menu style */
        /*ImageView mAnimatedArrow;*/
        /* Lenovo-SW zhaoxin5 20150119 change the menu style */

        public SettingsSimplePanelSlideListener() {
            super();
            /* Lenovo-SW zhaoxin5 20150119 change the menu style */
            /*mAnimatedArrow = (ImageView) mOverviewPanel.findViewById(R.id.settings_drag_arrow);*/
            /* Lenovo-SW zhaoxin5 20150119 change the menu style */
        }

        @Override
        public void onPanelCollapsed(View panel) {
        	/* Lenovo-SW zhaoxin5 20150119 change the menu style */
            /*mAnimatedArrow.setBackgroundResource(R.drawable.transition_arrow_reverse);
            AnimationDrawable frameAnimation = (AnimationDrawable) mAnimatedArrow.getBackground();
            frameAnimation.start();*/
            /* Lenovo-SW zhaoxin5 20150119 change the menu style */

            if (mLauncher.updateGridIfNeeded()) {
                Workspace workspace = mLauncher.getWorkspace();
                if (workspace.isInOverviewMode()) {
                    workspace.setChildrenOutlineAlpha(1.0f);
                    mLauncher.mSearchDropTargetBar.hideSearchBar(false);
                }
            }
        }

        @Override
        public void onPanelExpanded(View panel) {
        	/* Lenovo-SW zhaoxin5 20150119 change the menu style */
            /*mAnimatedArrow.setBackgroundResource(R.drawable.transition_arrow);

            AnimationDrawable frameAnimation = (AnimationDrawable) mAnimatedArrow.getBackground();
            frameAnimation.start();*/
        	/* Lenovo-SW zhaoxin5 20150119 change the menu style */
        }
    }
}
