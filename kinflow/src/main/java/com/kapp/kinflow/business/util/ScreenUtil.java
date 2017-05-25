package com.kapp.kinflow.business.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * description：屏幕辅助类
 * <br>author：caowugao
 * <br>time： 2017/04/28 11:31
 */

public class ScreenUtil {
    private ScreenUtil() {
    }

    /**
     * 获取屏幕宽度
     *
     * @param ctx
     * @return
     */
    public static int getScreenW(Context ctx) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param ctx
     * @return
     */
    public static int getScreenH(Context ctx) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    /**
     * 获取屏幕密度
     *
     * @param context
     * @return float
     */
    public static float getScreenDensity(Context context) {
        DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
        return metrics.density;
    }
}
