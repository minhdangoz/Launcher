package com.klauncher.kinflow.utilities;

import android.util.Log;

import com.klauncher.launcher.BuildConfig;

/**
 * Created by yanni on 15/10/30.
 */
public class KinflowLog {

    private static boolean enable = BuildConfig.DEBUG;
    private static final String APPLICATION_ID = "Kinflow";

    public static boolean isDebug() {
        return enable;
    }

    public static void d(String msg) {
        if (isDebug()) {
            Log.d(APPLICATION_ID, msg);
        }
    }

    public static void i(String msg) {
        if (isDebug()) {
            Log.i(APPLICATION_ID, msg);
        }
    }

    public static void e(String msg) {
        if (isDebug()) {
            Log.e(APPLICATION_ID, msg);
        }
    }

    public static void w(String msg) {
        if (isDebug()) {
            Log.w(APPLICATION_ID, msg);
        }
    }
}
