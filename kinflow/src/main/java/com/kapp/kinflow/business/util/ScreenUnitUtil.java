package com.kapp.kinflow.business.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * description：屏幕单位辅助类
 * <br>author：caowugao
 * <br>time： 2017/04/20 15:05
 */

public class ScreenUnitUtil {
    private ScreenUnitUtil() {
    }

    public static int asDp(int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, Resources.getSystem()
                .getDisplayMetrics());
    }

    public static int asPx(int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, i, Resources.getSystem()
                .getDisplayMetrics());
    }

    public static int asPs(int i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, i, Resources.getSystem()
                .getDisplayMetrics());
    }

}
