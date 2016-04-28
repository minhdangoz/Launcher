package com.klauncher.kinflow.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by xixionghui on 2016/3/22.
 */
public class CacheLocation {

    private String fileName = "cacheWeather";
    Gson gson;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static String KEY_WEATHER = "latest_weather";
    public static String KEY_LAT_LNG = "latest_lat_lng";

    private static CacheLocation instance;

    private CacheLocation(Context context) {
        sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new GsonBuilder().create();
    }

    public static CacheLocation getInstance(Context context) {
        if (null == instance) instance = new CacheLocation(context);
        return instance;
    }

    public void putLatLng(String content) {
        synchronized (this) {
            editor.putString(KEY_LAT_LNG, content);
            editor.commit();
        }
    }

    public String getLatLng() {
        synchronized (this) {
            return sharedPreferences.getString(KEY_LAT_LNG, null);
        }
    }

    /**
     * 注册监听
     *
     * @param listener
     */
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * 取消监听
     *
     * @param listener
     */
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

}
