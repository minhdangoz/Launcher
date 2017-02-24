package com.klauncher.biddingos.commons.utils;

import android.util.Log;

import com.klauncher.biddingos.commons.Setting;

public class LogUtils {
    /**
     * 日志开关
     * package com.delonghutong.www.pingxiaotongsdk_test.biddingos.interstitial;
     */
    public static boolean bWriteLog = Setting.bWriteLog;
    public static void d(String Tag, String msg) {
        if(bWriteLog)
            Log.d(Tag, msg);
    }

    public static void i(String Tag, String msg) {
        if(bWriteLog)
            Log.i(Tag, msg);
    }

    public static void e(String Tag, String msg) {
        if(bWriteLog)
            Log.e(Tag, msg);
    }

    public static void v(String Tag, String msg) {
        if(bWriteLog)
            Log.v(Tag, msg);
    }

    public static void w(String Tag, String msg) {
        if(bWriteLog)
            Log.w(Tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if(bWriteLog)
            Log.e(tag, msg, tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if(bWriteLog)
            Log.w(tag, msg, tr);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if(bWriteLog)
            Log.i(tag, msg, tr);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if(bWriteLog)
            Log.v(tag, msg, tr);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if(bWriteLog)
            Log.d(tag, msg, tr);
    }

}
