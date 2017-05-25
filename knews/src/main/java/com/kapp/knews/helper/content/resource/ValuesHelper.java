package com.kapp.knews.helper.content.resource;

import android.content.res.Resources;
import android.support.annotation.StringRes;

import com.kapp.knews.helper.content.ContentHelper;


/**
 * 作者:  android001
 * 创建时间:   16/11/4  上午10:07
 * 版本号:
 * 功能描述:
 */
public class ValuesHelper {

    public static Resources resources = ContentHelper.getResource();

    public static String[] getStringArrays(int arraysStringId){
       return resources.getStringArray(arraysStringId);
    }

    public static String getString (int stringId) {
        return resources.getString(stringId);
    }

    public static String getString(@StringRes int resId, Object... formatArgs) {
        return resources.getString(resId, formatArgs);
    }
}
