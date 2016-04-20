package com.klauncher.kinflow.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by xixionghui on 16/4/6.
 */
public class CommonShareData {
    public static String COMMON_SHAREDPREFERENCE = "com.klauncher.kinflow.common.utils.CommonShareData";

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    public static void init(Context context){
        sharedPreferences = context.getSharedPreferences(COMMON_SHAREDPREFERENCE,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        putString(Const.NAVIGATION_LOCAL_LAST_MODIFIED,String.valueOf(0));
        putString(Const.NAVIGATION_LOCAL_UPDATE_INTERVAL,String.valueOf(0));
    }


    public static void putString(String key,String value){
        if (TextUtils.isEmpty(key)||TextUtils.isEmpty(value)) return;
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key,String defaultStr) {
        if (TextUtils.isEmpty(key)) return defaultStr;
        return sharedPreferences.getString(key,defaultStr);
    }

    public static void putInt(String key,int value){
        if (TextUtils.isEmpty(key)) return;
        editor.putInt(key,value);
        editor.apply();
    }

    public static int getInt(String key,int defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return sharedPreferences.getInt(key,defaultValue);
    }

    public static void clearInt(String key){
        editor.putInt(key,0);
    }
}
