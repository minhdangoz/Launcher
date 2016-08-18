package com.klauncher.kinflow.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xixionghui on 16/4/6.
 */
public class CommonShareData {
    public static final String COMMON_SHAREDPREFERENCE = "com.klauncher.kinflow.common.utils.CommonShareData";

    public static final String KEY_CONFIG_FIRST_UPDATE = "config_last_update";
    public static final String KEY_OPERATOR_DELAY = "opt_delay";
    public static final String KEY_ACTIVE_2345 = "active_2345";
    public static final String KEY_ACTIVE_INTERVAL_2345 = "act_val_2345";
    public static final String KEY_APP_ACTIVE = "app_active";
    public static final String KEY_APP_PACKAGE_NAME = "app_package_name";
    public static final String KEY_JINRITOUTIAO_ACCESS_TOKEN = "jinritoutiao_access_token";
    public static final String KEY_IS_FIRST_USE_KINFLOW  = "is_first_use_kinflow";
    public static final String KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET = "user_allow_kinflow_use_net";
    public static final String FIRST_CONNECTED_NET = "first_connected_net";

    public static final int DEFAULT_OPERATOR_DELAY = 72;
    public static final int DEFAULT_ACTIVE_INTERVAL_2345 = 12;

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    public static void init(final Context context){
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(COMMON_SHAREDPREFERENCE,Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = getCurrentNetworkTime();
                if ( time != -1) {
                    putString(Const.NAVIGATION_LOCAL_LAST_MODIFIED, String.valueOf(0));
                    putString(Const.NAVIGATION_LOCAL_UPDATE_INTERVAL, String.valueOf(0));
                    if (getLong(Const.NAVIGATION_LOCAL_FIRST_INIT, -1) == -1) {
                        putLong(Const.NAVIGATION_LOCAL_FIRST_INIT, time);
                    }
                    //初始默认第一次
                    putBoolean(FIRST_CONNECTED_NET,true);
                }
            }
        }).start();

    }

    public static long getCurrentNetworkTime(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://api.klauncher.com/v1/card/gettime/").build();
        Response response = null;
        try {
            Log.d("CommonShareData", "getCurrentNetworkTime start");
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String str = response.body().string();
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(str);
                m.find();
                Log.d("CommonShareData", "getCurrentNetworkTime : " + m.group());
                return Long.parseLong(m.group());
            } else {
                Log.d("CommonShareData", "getCurrentNetworkTime failed  ");
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean containsKey(String key) {
        return sharedPreferences.contains(key);
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
