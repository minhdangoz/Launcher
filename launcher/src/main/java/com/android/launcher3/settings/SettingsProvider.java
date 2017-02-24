/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.settings;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsProvider {
    public static final String SETTINGS_KEY = "trebuchet_preferences";

    public static final String SETTINGS_CHANGED = "settings_changed";

    public static final String SETTINGS_UI_HOMESCREEN_DEFAULT_SCREEN_ID_VIBE_UI = "ui_homescreen_default_screen_id_vibe_ui";
    public static final String SETTINGS_UI_HOMESCREEN_DEFAULT_SCREEN_ID_ANDROID = "ui_homescreen_default_screen_id_android";
    public static final String SETTINGS_UI_HOMESCREEN_SEARCH = "ui_homescreen_search";
    public static final String SETTINGS_UI_HOMESCREEN_HIDE_ICON_LABELS = "ui_homescreen_general_hide_icon_labels";
    public static final String SETTINGS_UI_HOMESCREEN_SCROLLING_TRANSITION_EFFECT = "ui_homescreen_scrolling_transition_effect";
    public static final String SETTINGS_UI_HOMESCREEN_SCROLLING_WALLPAPER_SCROLL = "ui_homescreen_scrolling_wallpaper_scroll";
    public static final String SETTINGS_UI_HOMESCREEN_SCROLLING_PAGE_OUTLINES = "ui_homescreen_scrolling_page_outlines";
    public static final String SETTINGS_UI_HOMESCREEN_SCROLLING_FADE_ADJACENT = "ui_homescreen_scrolling_fade_adjacent";
    public static final String SETTINGS_UI_DYNAMIC_GRID_SIZE = "ui_dynamic_grid_size";
    public static final String SETTINGS_UI_HOMESCREEN_ROWS = "ui_homescreen_rows";
    public static final String SETTINGS_UI_HOMESCREEN_COLUMNS = "ui_homescreen_columns";
    public static final String SETTINGS_UI_DRAWER_SCROLLING_TRANSITION_EFFECT = "ui_drawer_scrolling_transition_effect";
    public static final String SETTINGS_UI_DRAWER_SCROLLING_FADE_ADJACENT = "ui_drawer_scrolling_fade_adjacent";
    public static final String SETTINGS_UI_DRAWER_REMOVE_HIDDEN_APPS_SHORTCUTS = "ui_drawer_remove_hidden_apps_shortcuts";
    public static final String SETTINGS_UI_DRAWER_REMOVE_HIDDEN_APPS_WIDGETS = "ui_drawer_remove_hidden_apps_widgets";
    public static final String SETTINGS_UI_DRAWER_HIDE_ICON_LABELS = "ui_drawer_hide_icon_labels";
    public static final String SETTINGS_UI_GENERAL_ICONS_LARGE = "ui_general_icons_large";
    public static final String SETTINGS_UI_GENERAL_ICONS_TEXT_FONT_FAMILY = "ui_general_icons_text_font";
    public static final String SETTINGS_UI_GENERAL_ICONS_TEXT_FONT_STYLE = "ui_general_icons_text_font_style";
    public static final String SETTINGS_UI_DRAWER_SORT_MODE = "ui_drawer_sort_mode";
    public static final String SETTINGS_UI_DRAWER_TYPE = "ui_drawer_type";
    /*Lenovo-sw zhangyj19 add 2015/06/03 add folder sort type definition begin */
	public static final String SETTINGS_UI_FOLDER_SORT_TYPE = "ui_folder_sort_type";
    /*Lenovo-sw zhangyj19 add 2015/06/03 add folder sort type definition end */

	/** disable all apps */
	public static final String SETTINGS_UI_DISABLE_ALL_APPS = "ui_disable_all_apps";

    public static final String SETTINGS_UI_AUTO_REORDER = "ui_auto_reorder";
    public static final String SETTINGS_UI_BACKUP_PATH = "ui_backup_path";
    public static final String SETTINGS_UI_WORKSPACE_LOOP = "ui_workspace_loop";
    public static final String SETTINGS_UI_KINFLOW_SETON = "ui_kinflow_seton";
    public static final String KINFLOW_REPORT_SETOFF = "kinflow_report_setoff";
    public static final String KINFLOW_REPORT_SETON = "kinflow_report_seton";
    public static final String KINFLOW_USER_SWITCHED = "kinflow_user_switched";
    public static final String SHORTCUT_WIFI_SETTING = "shortcut_wifi_seting";
    public static final String SHORTCUT_ADD_KLANCHER = "shortcut_add_klancher";
    /*Lenovo-sw zhangyj19 add 2015/09/09 add search app definition begin */
	public static final String SETTINGS_UI_SEARCH_APP = "ui_search_app";
    /*Lenovo-sw zhangyj19 add 2015/09/09 add search app definition end */
    /** Lenovo-SW zhaoxin5 20150813 add for KOLEOSROW-580 START */
    /** 如果是第一次加载Launcher,则从LBK中加载桌面配置数据 */
    public static final String SETTINGS_FIRST_LOAD_SETTING = "first_load_setting";
    /** Lenovo-SW zhaoxin5 20150813 add for KOLEOSROW-580 END */
    
    /** Lenovo-SW zhaoxin5 20150814 KOLEOSROW-951 START */
    /** 从设置中读取的默认主页 */
    public static final String SETTINGS_DEFAULT_PAGE = "settings_default_page";
    /** Lenovo-SW zhaoxin5 20150814 KOLEOSROW-951 END */
    
    /** Lenovo_SW zhaoxin5 20150924 add for migrate from xlauncher support START */
    /** 是否是第一次从XLauncher升级上来的*/
    public static final String SETTINGS_FIRST_MIGRATE_FROM_XLAUNCHER = "settings_first_migrate_from_xlauncher";
    /** Lenovo_SW zhaoxin5 20150924 add for migrate from xlauncher support END */
    
    /*** Lenovo-SW zhaoxin5 20151117 add for Reaper Function START */
    public static final String EXTRA_NETWORK_ENABLED = "network_enabled";
    public static final String ACTION_NETWORK_ENABLER_CHANGED = "com.lenovo.action.ACTION_NETWORK_ENABLER_CHANGED";
    /*** Lenovo-SW zhaoxin5 20151117 add for Reaper Function END */
    
    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences(SETTINGS_KEY, Context.MODE_MULTI_PROCESS);
    }

    public static int getIntCustomDefault(Context context, String key, int def) {
        return get(context).getInt(key, def);
    }

    public static int getInt(Context context, String key, int resource) {
        return getIntCustomDefault(context, key, context.getResources().getInteger(resource));
    }

    public static long getLongCustomDefault(Context context, String key, long def) {
        return get(context).getLong(key, def);
    }

    public static long getLong(Context context, String key, int resource) {
        return getLongCustomDefault(context, key, context.getResources().getInteger(resource));
    }

    public static boolean getBooleanCustomDefault(Context context, String key, boolean def) {
        return get(context).getBoolean(key, def);
    }

    public static boolean getBoolean(Context context, String key, int resource) {
        return getBooleanCustomDefault(context, key, context.getResources().getBoolean(resource));
    }

    public static String getStringCustomDefault(Context context, String key, String def) {
        return get(context).getString(key, def);
    }

    public static String getString(Context context, String key, int resource) {
        return getStringCustomDefault(context, key, context.getResources().getString(resource));
    }

    public static void putString(Context context, String key, String value) {
        get(context).edit().putString(key, value).commit();
    }

    public static void putInt(Context context, String key, int value) {
        get(context).edit().putInt(key, value).commit();
    }

    public static void putBoolean(Context context, String key, boolean value) {
        get(context).edit().putBoolean(key, value).commit();
    }
}
