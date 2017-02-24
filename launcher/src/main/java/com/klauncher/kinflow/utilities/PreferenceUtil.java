package com.klauncher.kinflow.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * description：SharePrefence辅助类
 * <br>author：caowugao
 * <br>time： 2016/12/30 17:52
 */

public class PreferenceUtil {
    private PreferenceUtil(){}
    public static void write(Context context, String fileName, String k, String v) {
        SharedPreferences preference = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(k, v);
        editor.commit();
    }
    public static String readString(Context context, String fileName, String k, String defV) {
        SharedPreferences preference = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return preference.getString(k, defV);
    }
    public static void write(Context context, String fileName, String k, int v) {
        SharedPreferences preference = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt(k, v);
        editor.commit();
    }
    public static int readInt(Context context, String fileName, String k, int defV) {
        SharedPreferences preference = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return preference.getInt(k, defV);
    }
}
