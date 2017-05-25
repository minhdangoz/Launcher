package com.kapp.kinflow.business.util;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * description：View辅助类
 * <br>author：caowugao
 * <br>time： 2017/04/20 14:42
 */

public class ViewUtil {
    private ViewUtil() {
    }

    public static void setBackgroundDrawable(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }
}
