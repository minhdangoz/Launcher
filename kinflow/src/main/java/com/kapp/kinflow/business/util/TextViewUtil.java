package com.kapp.kinflow.business.util;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

/**
 * description：TextView辅助类
 * <br>author：caowugao
 * <br>time： 2017/04/21 16:41
 */

public class TextViewUtil {
    private TextViewUtil() {
    }

    public static void setDrawableLeft(TextView textView, int resId, int drawableWidth, int drawableHeight) {
        Drawable drawable = textView.getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawableWidth, drawableHeight);
        textView.setCompoundDrawables(drawable, null, null, null);//只放左边
    }

    public static void setDrawableLeft(TextView textView, int resId) {
        Drawable drawable = textView.getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(drawable, null, null, null);//只放左边
    }

    /**
     * @param textView
     * @param drawable
     */
    public static void setDrawableTop(TextView textView, Drawable drawable) {
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, drawable, null, null);

    }
}
