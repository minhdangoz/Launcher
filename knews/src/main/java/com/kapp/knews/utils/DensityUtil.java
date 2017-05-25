package com.kapp.knews.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/02/08 15:52
 */

public class DensityUtil {
    private DensityUtil(){}
    /**
     * 数字i返回idp
     * @param context
     * @param i
     * @return
     */
    public static int dp(Context context, int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, context.getResources().getDisplayMetrics());
    }
}
