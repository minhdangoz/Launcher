// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-

package com.klauncher.utilities;

import android.content.Context;

public class AppContextUtils {
    private static Context mAppContext;

    public static void setAppContext(Context appContext) {
        mAppContext = appContext;
    }

    public static Context getAppContext(){
        return mAppContext;
    }
}
