package com.klauncher.utilities;

import android.util.Log;

/**
 * Created by wangqinghao on 2016/5/27.
 * 日志工具
 */

public class LogUtil {

    public enum LogModel {
        RELEASE, DEBUG
    }


    public static LogModel mLogModel = LogModel.RELEASE;

    /**
     * 打印日志-INFO
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void i(String tag, String msg) {
        if (mLogModel == LogModel.DEBUG) {
            Log.i(tag, msg);
        }
    }

    /**
     * 打印日志-DEBUG
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void d(String tag, String msg) {
        if (mLogModel == LogModel.DEBUG) {
            Log.d(tag, msg);
        }
    }

    /**
     * 打印日志-ERROR
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void e(String tag, String msg) {
        if (mLogModel == LogModel.DEBUG) {
            Log.e(tag, msg);
        }
    }

    /**
     * 打印日志-VERBOSE
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void v(String tag, String msg) {
        if (mLogModel == LogModel.DEBUG) {
            Log.v(tag, msg);
        }
    }

    /**
     * 打印日志-WARN
     *
     * @param tag 标签
     * @param msg 信息
     */
    public static void w(String tag, String msg) {
        if (mLogModel == LogModel.DEBUG) {
            Log.w(tag, msg);
        }
    }

    /**
     * 打印日志-自定义tag和传操作对象
     *
     * @param tag tag
     * @param o   操作对象
     * @param msg 信息
     */
    public static void log(String tag, Object o, String msg) {
        e(tag, o.getClass().getName() + "->" + msg);
    }


}
