package com.android.launcher3.settings;

import android.content.Context;

import com.klauncher.launcher.R;

/**
 * Created by LEI on 2015/6/30.
 */
public class SettingsValue {

    public static boolean isAutoReorderEnabled(Context c) {
        return SettingsProvider.getBoolean(c,
                    SettingsProvider.SETTINGS_UI_AUTO_REORDER,
                    R.bool.preferences_interface_auto_reorder_default);
    }
    
    public static void setAutoReorderState(Context c, boolean state) {
    	SettingsProvider.putBoolean(c, SettingsProvider.SETTINGS_UI_AUTO_REORDER, state);
    }

    public static boolean isScrollingWallpaper(Context c) {
        return SettingsProvider.getBoolean(c,
                    SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_WALLPAPER_SCROLL,
                    R.bool.preferences_interface_homescreen_scrolling_wallpaper_scroll_default);
    }
    
    public static void setScrollingWallpaperState(Context c, boolean state) {
       SettingsProvider.putBoolean(c, SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_WALLPAPER_SCROLL, state);
    }

    public static boolean isDisableAllApps(Context c) {
    	boolean current = SettingsProvider.getBoolean(
    			c, SettingsProvider.SETTINGS_UI_DISABLE_ALL_APPS,
        		R.bool.preferences_ui_disable_all_apps_default);
        return current;
    }
    
    public static void setDisableAllApps(Context c, boolean b) {
        SettingsProvider.putBoolean(c, SettingsProvider.SETTINGS_UI_DISABLE_ALL_APPS, b);
    }

    public static String getBackupPath(Context c) {
        return SettingsProvider.getString(c, SettingsProvider.SETTINGS_UI_BACKUP_PATH,
                R.string.preferences_interface_backup_path_default);
    }

    public static void setBackupPath(Context c, String path) {
        SettingsProvider.putString(c, SettingsProvider.SETTINGS_UI_BACKUP_PATH, path);
    }

    public static boolean isWorkspaceLoop(Context c) {
        return SettingsProvider.getBoolean(c, SettingsProvider.SETTINGS_UI_WORKSPACE_LOOP,
                R.bool.preferences_interface_workspace_loop_default);
    }

    public static void setWorkspaceLoop(Context c, boolean loop) {
        SettingsProvider.putBoolean(c, SettingsProvider.SETTINGS_UI_WORKSPACE_LOOP, loop);
    }
    
    /** Lenovo-SW zhaoxin5 20150813 add for KOLEOSROW-580 START */
    public static boolean isFirstLoadSettings(Context c) {
    	return SettingsProvider.getBooleanCustomDefault(c, SettingsProvider.SETTINGS_FIRST_LOAD_SETTING, true);
    }
    
    public static void setFirstLoadSettings(Context c, boolean b) {
    	SettingsProvider.putBoolean(c, SettingsProvider.SETTINGS_FIRST_LOAD_SETTING, b);
    }
    /** Lenovo-SW zhaoxin5 20150813 add for KOLEOSROW-580 END */
    
    /** Lenovo-SW zhaoxin5 20150814 KOLEOSROW-951 START */
    public static void setDefaultPage(Context c, int idx) {
    	SettingsProvider.putInt(c, SettingsProvider.SETTINGS_DEFAULT_PAGE, idx);
    }
    
    public static int getDefaultPage(Context c) {
    	return SettingsProvider.getIntCustomDefault(c, SettingsProvider.SETTINGS_DEFAULT_PAGE,
                c.getResources().getInteger(R.integer.config_workspaceDefaultScreen));
    }
    /** Lenovo-SW zhaoxin5 20150814 KOLEOSROW-951 END */
    /*Lenovo-sw zhangyj19 add 2015/09/09 add search app definition begin */
    public static boolean isSearchApp(Context c) {
        return SettingsProvider.getBoolean(c, SettingsProvider.SETTINGS_UI_SEARCH_APP,
                R.bool.preferences_search_app_default);
    }

    public static void setSearchApp(Context c, boolean loop) {
        SettingsProvider.putBoolean(c, SettingsProvider.SETTINGS_UI_SEARCH_APP, loop);
    }
    /*Lenovo-sw zhangyj19 add 2015/09/09 add search app definition end */

    /** Lenovo_SW zhaoxin5 20150924 add for migrate from xlauncher support START */
    public static boolean isFirstMigrateFromXLauncher(Context c) {
    	return SettingsProvider.getBoolean(c, 
    			SettingsProvider.SETTINGS_FIRST_MIGRATE_FROM_XLAUNCHER, R.bool.preferences_first_migrate_from_xlauncher);
    }
    
    public static void setIsFirstMigrateFromXLauncher(Context c, boolean value) {
    	SettingsProvider.putBoolean(c, SettingsProvider.SETTINGS_FIRST_MIGRATE_FROM_XLAUNCHER, value);
    }
    /** Lenovo_SW zhaoxin5 20150924 add for migrate from xlauncher support END */

}
