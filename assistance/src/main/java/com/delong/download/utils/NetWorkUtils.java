package com.delong.download.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by alex on 15/11/21.
 */
public class NetWorkUtils {

    public static final int NET_TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
    public static final int NET_TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
    public static final int NET_TYPE_ETHERNET = ConnectivityManager.TYPE_ETHERNET;
    public static final int NET_TYPE_NONE = -1;

    public static int getNetworkType(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            return NET_TYPE_NONE;
        }
        return ni.getType();
    }

    public static boolean isNetworkAvailable(Context ctx) {
        return getNetworkType(ctx) != NET_TYPE_NONE;
    }

    public static void openNetworkSetting(Context ctx) {
       try{
           Intent intent = null;
           if (android.os.Build.VERSION.SDK_INT > 10) {
               intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
           } else {
               intent = new Intent();
               ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
               intent.setComponent(component);
               intent.setAction("android.intent.action.VIEW");
           }
           ctx.startActivity(intent);
       }catch (Exception e){
       }
    }
}
