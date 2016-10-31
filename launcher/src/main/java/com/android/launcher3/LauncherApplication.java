/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.launcher3;

import android.os.Build;
import android.util.Log;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.launcher.R;
import com.klauncher.theme.ThemeController;

public class LauncherApplication extends KLauncherApplication {
    private static final String TAG = "LauncherApplication";
    public static boolean LAUNCHER_SHOW_UNREAD_NUMBER;
    public static boolean LAUNCHER_SHORTCUT_ENABLED;
    public static boolean SHOW_CTAPP_FEATURE;
    public static boolean mDebug = true;
    public static int mIndexLog = 0;
    public static String[] mLogArray = null;
    public static int mLoglength = 500;
    public static String mModellbkValue = "";

    @Override
    public void onCreate() {
        super.onCreate();
        if (isMultiProgressInit()) {
            return;
        }
        if(mLogArray == null)
        	mLogArray = new String[mLoglength];
        LAUNCHER_SHOW_UNREAD_NUMBER = getResources().getBoolean(
                R.bool.config_launcher_show_unread_number);
        LAUNCHER_SHORTCUT_ENABLED = getResources().getBoolean(
                R.bool.config_launcher_shortcut);
        SHOW_CTAPP_FEATURE = getResources().getBoolean(R.bool.config_launcher_page);
        LauncherAppState.setApplicationContext(this);
        LauncherAppState.getInstance();

        String[] keywords = getResources().getStringArray(R.array.config_lbk_load_keywords);
        for (String keyword : keywords) {
            String[] modelTheme = keyword.split(",");
            if (Build.MODEL.replaceAll(" ","").contains(modelTheme[0])) {
                mModellbkValue = modelTheme[1];
                break;
            }
        }
        /* Lenovo-SW zhaoxin5 20150116 add Theme support */
        initTheme();
        /* Lenovo-SW zhaoxin5 20150116 add Theme support */
        //startService(new Intent(this, NotifierService.class));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LauncherAppState.getInstance().onTerminate();
        unRegistBrocaster();
    }

    /* Lenovo-SW zhaoxin5 20150116 add Theme support */
    ThemeController mThemeController = null;
    public void initTheme() {
        String[] keywords = getResources().getStringArray(R.array.config_theme_load_keywords);
        String themeName = "default";
        for (String keyword : keywords) {
            String[] modelTheme = keyword.split(",");
            if (Build.MODEL.contains(modelTheme[0])) {
                themeName = modelTheme[1];
                break;
            }
        }
        mThemeController = new ThemeController(getApplicationContext(), themeName);
        applyDefaultTheme();

    }

    private void applyDefaultTheme(){
    	/*String[] parameters = Placement.getDefaultThemeParameters(this);*/
        //String[] parameters = new String[]{"apk", ThemeController.DEFAULT_APK_THEME};

        String[] keywords = getResources().getStringArray(R.array.config_theme_load_keywords);
        String themeName = "default";
        for (String keyword : keywords) {
            String[] modelTheme = keyword.split(",");
            if (Build.MODEL.contains(modelTheme[0])) {
                themeName = modelTheme[1];
                break;
            }
        }
        String[] parameters = new String[]{"zip", ThemeController.UI_ZIP_THEME + themeName + ".ktm"};
        boolean enableThemeMask = !Utilities.isRowProduct();
        if (null != parameters) {
//            Intent intent = new Intent();
            if(parameters[0].equals("apk")){
                Log.i(TAG, "apply default apk theme : " + parameters[1]);
//                intent.setAction(ThemeController.ACTION_LAUNCHER_THEME_APK);
//                intent.putExtra(ThemeController.ACTION_LAUNCHER_THEME_PACKAGE, parameters[1]);
                mThemeController.handleTheme(ThemeController.ACTION_LAUNCHER_THEME_APK, parameters[1], enableThemeMask);
            }else if(parameters[0].equals("zip")){
                Log.i(TAG, "apply default zip theme : " + parameters[1]);
//                intent.setAction(ThemeController.ACTION_LAUNCHER_THEME_ZIP);
//                intent.putExtra(ThemeController.ACTION_LAUNCHER_THEME_PATH, parameters[1]);
                mThemeController.handleTheme(ThemeController.ACTION_LAUNCHER_THEME_ZIP, parameters[1], enableThemeMask);

            }
//            intent.putExtra(ThemeController.EXTRA_LAUNCHER_THEME_ENABLE_THEME_MASK, enableThemeMask);
//            this.sendBroadcast(intent);
        }
    }

    public ThemeController getThemeController() {
        return mThemeController;
    }
    /* Lenovo-SW zhaoxin5 20150116 add Theme support */
}
