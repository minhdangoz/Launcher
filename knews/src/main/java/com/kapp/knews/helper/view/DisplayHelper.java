package com.kapp.knews.helper.view;

import android.util.DisplayMetrics;

import com.kapp.knews.helper.content.ContentHelper;


/**
 * 作者:  android001
 * 创建时间:   16/10/31  下午2:12
 * 版本号:
 * 功能描述:
 */
public class DisplayHelper {

    public static DisplayMetrics getDisplayMetrics () {
        return ContentHelper.getResource().getDisplayMetrics();
    }

    public static int getWidthPixels(){
        return getDisplayMetrics().widthPixels;
    }

    public static int getHeightPixels(){
        return getDisplayMetrics().heightPixels;
    }

    public static int getDeviceDpi(){
        return getDisplayMetrics().densityDpi;
    }

}
