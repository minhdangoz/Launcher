package com.android.launcher3.settings;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.view.View;
import android.widget.ListView;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.ModeSwitchHelper;
import com.android.launcher3.list.PinnedHeaderListView;
import com.webeye.launcher.R;

/**
 * Created by LEI on 2015/6/10.
 * Bind settings data to setting list.
 */
public class SettingsController {

    public static final int APP_SETTINGS_POSITION = 0;
    public static final int HOME_SETTINGS_POSITION = 1;
    public static final int DRAWER_SETTINGS_POSITION = 2;
    public static final int BACKUP_SETTINGS_POSITION = 3;

    private Launcher mLauncher;
    private SettingsPinnedHeaderAdapter mSettingsAdapter;
    private PinnedHeaderListView mListView;

    public SettingsController(Launcher launcher, View settingsPanel) {
        mLauncher = launcher;
        PinnedHeaderListView settingList = (PinnedHeaderListView) settingsPanel.findViewById(
                R.id.settings_home_screen_listview);
    }

    // One time initialization of the SettingsPinnedHeaderAdapter
    public void initializeAdapter() {
        // Settings pane Listview
        mListView = (PinnedHeaderListView) mLauncher.findViewById(R.id.settings_home_screen_listview);
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        Resources res = mLauncher.getResources();
        String[] headers = new String[]{
                res.getString(R.string.app_settings),
                res.getString(R.string.home_screen_settings),
                res.getString(R.string.drawer_settings),
                res.getString(R.string.menu_sense_settings)};

        String[] valuesApp = new String[]{
                res.getString(R.string.desktop_style),
                res.getString(R.string.auto_reorder_title)};

        String[] values = new String[]{
                res.getString(R.string.scroll_effect_text),
                res.getString(R.string.scrolling_wallpaper)};

        String[] valuesDrawer = new String[]{
            	/** Lenovo-SW zhaoxin5 20150722 LANCHROW-240 START */
                /* res.getString(R.string.drawer_type), */
            	/** Lenovo-SW zhaoxin5 20150722 LANCHROW-240 END */
                res.getString(R.string.scroll_effect_text),
                res.getString(R.string.drawer_sorting_text)};

        String[] valuesBackup = new String[] {
            res.getString(R.string.local_backup),
            res.getString(R.string.local_restore)
        };

        mSettingsAdapter = new SettingsPinnedHeaderAdapter(mLauncher);
        mSettingsAdapter.setHeaders(headers);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.mPinnedHeaderCount = headers.length;

        mSettingsAdapter.changeCursor(APP_SETTINGS_POSITION, createCursor(headers[0], valuesApp));
        mSettingsAdapter.changeCursor(HOME_SETTINGS_POSITION, createCursor(headers[1], values));
        if (LauncherAppState.getInstance().getCurrentLayoutMode() == ModeSwitchHelper.Mode.VIBEUI) {
            mSettingsAdapter.changeCursor(DRAWER_SETTINGS_POSITION, createCursor(res
                    .getString(R.string.drawer_settings), new String[]{}));
        } else {
            mSettingsAdapter.changeCursor(DRAWER_SETTINGS_POSITION, createCursor(headers[2],
                    valuesDrawer));
        }
        mSettingsAdapter.changeCursor(BACKUP_SETTINGS_POSITION, createCursor(headers[3], valuesBackup));
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

    public void notifyDataSetInvalidated() {
        mSettingsAdapter.notifyDataSetInvalidated();
    }

    public void update() {
        Resources res = mLauncher.getResources();
        if (LauncherAppState.getInstance().getCurrentLayoutMode() == ModeSwitchHelper.Mode.VIBEUI) {
            mSettingsAdapter.changeCursor(DRAWER_SETTINGS_POSITION, createCursor(res
                    .getString(R.string.drawer_settings), new String[]{}));
        } else {
            String[] valuesDrawer = new String[]{
                	/** Lenovo-SW zhaoxin5 20150722 LANCHROW-240 START */
                    /* res.getString(R.string.drawer_type), */
                	/** Lenovo-SW zhaoxin5 20150722 LANCHROW-240 END */
                    res.getString(R.string.scroll_effect_text),
                    res.getString(R.string.drawer_sorting_text)};
            mSettingsAdapter.changeCursor(DRAWER_SETTINGS_POSITION, createCursor(res
                    .getString(R.string.drawer_settings), valuesDrawer));
        }
    }
}
