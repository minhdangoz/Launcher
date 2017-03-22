package com.excelliance.open;

import com.excelliance.lbsdk.LBConfig;

public class GlobalSettings {
    public static int SELECTED_AREA = LBConfig.AREA_CN;

    public static boolean USE_LEBIAN = true;
    public static boolean DOWNLOAD_AFTER_QUIT = true;
    public static boolean AUTO_CHECK_NEWVER_ONSTART = true;
    public static boolean ENABLE_CRASH_REPORT = true;

    public static boolean USE_BWBX = false;
    public static boolean SHOW_LOADING_PROGRESS_BWBX = true;
    public static boolean SHOW_LOADING_PROGRESS_BWBX_IMMEDIATELY = false;
    public static boolean SHOW_FIRST_DIALOG_WITHOUT_WIFI_BWBX = true;
    public static boolean SHOW_FIRST_DIALOG_ALWAYS_BWBX = false;
    public static boolean CHECK_OLD_USER_AUTO = true;
    public static boolean CHOOSE_BY_USER_BWBX = false;
    public static boolean CHOOSE_BY_USER_BWBX_FORCE = false;
    public static boolean DOWNLOAD_FULL_AFTER_SECOND_DIALOG_BWBX = false;
    public static int SECOND_DIALOG_INTERVAL_BWBX = 0;

    public static void refreshState() {
        //USE_LEBIAN = !(new java.io.File("/sdcard/disable_LB_flag").exists());
        android.util.Log.d("GlobalSettings", "USE_LEBIAN=" + USE_LEBIAN);
    }
}

