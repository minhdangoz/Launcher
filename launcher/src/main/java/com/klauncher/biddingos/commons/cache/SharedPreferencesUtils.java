package com.klauncher.biddingos.commons.cache;

import android.content.Context;
import android.content.SharedPreferences;

import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.utils.AESUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.commons.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class SharedPreferencesUtils {
    private static final String TAG = "SharedPreferencesUtils";
    public static final String KEY_PACKAGE_LIST = "packageslist";
    public static final String KEY_EVENTS        = "events";
    public static final String KEY_LAST_TIME_POST = "lasttimepost";
    public static final String KEY_FLOWADS = "organicAds";
    public static final String KEY_LAST_TIME_REFRESH = "lasttime";
    public static final String KEY_LAST_CHECK_TIME = "last_check_time";
    public static SharedPreferences pkg = null;
    public static SharedPreferences events = null;
    public static SharedPreferences download = null;
    public static SharedPreferences flowAds = null;


    public static void init() {
        if(null==pkg) {
            pkg = Setting.context.getSharedPreferences("biddingos.pkg", Context.MODE_PRIVATE);
        }
        if(null==events) {
            events = Setting.context.getSharedPreferences("biddingos.events", Context.MODE_PRIVATE);
        }
        if(null==download) {
            download = Setting.context.getSharedPreferences("biddingos.dl", Context.MODE_PRIVATE);
        }
        if(null==flowAds) {
            flowAds = Setting.context.getSharedPreferences("Organic.Ads", Context.MODE_PRIVATE);
        }
    }

    public static void updateFlowAdsPool(String ciphertext, long updateTime) {
        if(null==ciphertext || ""==ciphertext ) {
            return;
        }

        SharedPreferences.Editor editor = flowAds.edit();
        editor.putString(KEY_FLOWADS, ciphertext);
        editor.putLong(KEY_LAST_TIME_REFRESH, updateTime);
        editor.commit();
    }

    public static void clearFlowAdsPool() {
        SharedPreferences.Editor editor = flowAds.edit();
        editor.remove(KEY_FLOWADS);
        editor.commit();
    }

    public static String getOrganicAds() {
        return flowAds.getString(KEY_FLOWADS, "");
    }

    public static long getOrganicAdsLastTimeRefresh() {
        return flowAds.getLong(KEY_LAST_TIME_REFRESH, 0);
    }

    //添加应用标识，下载开始的应用信息（加密形式）
    public static void setKeyAdAppDownload(String key, String value) {
        if(null==key || ""==key ) {
            return;
        }

        SharedPreferences.Editor editor = download.edit();
        editor.putString(key, AESUtils.encode(Setting.SECRET, value));
        editor.commit();
    }

    public static long getAdsCheckLastTime() {
        return flowAds.getLong(KEY_LAST_CHECK_TIME, -1);
    }

    public static void setAdsCheckTime(long time) {
        SharedPreferences.Editor editor = flowAds.edit();
        editor.putLong(KEY_LAST_CHECK_TIME, time);
        editor.commit();
    }

    public static void deleteAdAppDownload(String key) {
        SharedPreferences.Editor editor = download.edit();
        editor.remove(key);
        editor.commit();
    }

    public static JSONObject getKeyAdAppDownload(String key) {
        if(null==key || ""==key ) {
            return null;
        }

        String dl = download.getString(key, "");
        if(""!=dl) {
            try {
                JSONObject jsonObject = new JSONObject(AESUtils.decode(Setting.SECRET, dl));
                return jsonObject;
            }catch (JSONException e) {
                LogUtils.e(TAG, "", e);
            }
        }

        return null;
    }

    public static boolean isPackageListEmpty() {
        if( null == pkg.getString(KEY_PACKAGE_LIST, null) ) {
            return true;
        }
        return false;
    }

    public static void addPackageList(List<String> addList) {
        if(addList.isEmpty()) return;
        String spStrPackageList = pkg.getString(KEY_PACKAGE_LIST, "");
        if (""==spStrPackageList) return;
        List<String> packageList = StringUtils.StringToList(spStrPackageList, "\\|");
        for(int i=0; i<addList.size(); ++i) {
            if(packageList.indexOf(addList.get(i)) <0 ) {
                packageList.add(addList.get(i));
            }
        }
        SharedPreferences.Editor editor = pkg.edit();
        editor.putString(KEY_PACKAGE_LIST, StringUtils.join(packageList, "|"));
        editor.commit();
    }

    public static void removePackageList(List<String> removeList) {
        if(removeList.isEmpty()) return;
        String spStrPackageList = pkg.getString(KEY_PACKAGE_LIST, "");
        if (""==spStrPackageList) return;
        List<String> packageList = StringUtils.StringToList(spStrPackageList, "\\|");

        for(String rmName:removeList) {
            packageList.remove(rmName);
        }
        SharedPreferences.Editor editor = pkg.edit();
        editor.putString(KEY_PACKAGE_LIST, StringUtils.join(packageList, "|"));
        editor.commit();
    }

}
