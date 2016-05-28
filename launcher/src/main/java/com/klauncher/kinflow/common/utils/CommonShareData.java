package com.klauncher.kinflow.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Set;

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
        editor.commit();
    }

    public static String getString(String key,String defaultStr) {
        if (TextUtils.isEmpty(key)) return defaultStr;
        return sharedPreferences.getString(key,defaultStr);
    }

    public static void putBoolean (String key,boolean value) {
        if (TextUtils.isEmpty(key)) return;
        editor.putBoolean(key,value);
        editor.commit();
    }

    public static boolean getBoolean (String key,boolean defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return sharedPreferences.getBoolean(key,defaultValue);
    }

    public static void putInt(String key,int value){
        if (TextUtils.isEmpty(key)) return;
        editor.putInt(key,value);
        editor.commit();
    }

    public static int getInt(String key,int defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return sharedPreferences.getInt(key,defaultValue);
    }

    public static void putLong (String key,long value) {
        if (TextUtils.isEmpty(key)) return;
        editor.putLong(key,value);
        editor.commit();
    }

    public static long getLong (String key,long defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return sharedPreferences.getLong(key,defaultValue);
    }

    public static void putSet (String key,Set<String> values) {
        if (TextUtils.isEmpty(key)) return;
        editor.putStringSet(key,values);
        editor.commit();
    }

    public static Set<String> getSet(String key,Set<String> defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return sharedPreferences.getStringSet(key,defaultValue);
    }

    public static void clearInt(String key){
        editor.putInt(key,0);
    }
}
