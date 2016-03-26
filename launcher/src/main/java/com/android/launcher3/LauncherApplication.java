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

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.launcher.R;
import com.klauncher.theme.ThemeController;

public class LauncherApplication extends KLauncherApplication {
    public static boolean LAUNCHER_SHOW_UNREAD_NUMBER;
    public static boolean LAUNCHER_SHORTCUT_ENABLED;
    public static boolean SHOW_CTAPP_FEATURE;
    public static boolean mDebug = true;
    public static int mIndexLog = 0;
    public static String[] mLogArray = null;
    public static int mLoglength = 500;
    @Override
    public void onCreate() {
        super.onCreate();
        if(mLogArray == null)
        	mLogArray = new String[mLoglength];
        LAUNCHER_SHOW_UNREAD_NUMBER = getResources().getBoolean(
                R.bool.config_launcher_show_unread_number);
        LAUNCHER_SHORTCUT_ENABLED = getResources().getBoolean(
                R.bool.config_launcher_shortcut);
        SHOW_CTAPP_FEATURE = getResources().getBoolean(R.bool.config_launcher_page);
        LauncherAppState.setApplicationContext(this);
        LauncherAppState.getInstance();

        /* Lenovo-SW zhaoxin5 20150116 add Theme support */
        initTheme();
        /* Lenovo-SW zhaoxin5 20150116 add Theme support */
        //startService(new Intent(this, NotifierService.class));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LauncherAppState.getInstance().onTerminate();
    }

    /* Lenovo-SW zhaoxin5 20150116 add Theme support */
    ThemeController mThemeController = null;
    public void initTheme(){
        mThemeController = new ThemeController(getApplicationContext());
    }

    public ThemeController getThemeController(){
        return mThemeController;
    }
    /* Lenovo-SW zhaoxin5 20150116 add Theme support */
}
