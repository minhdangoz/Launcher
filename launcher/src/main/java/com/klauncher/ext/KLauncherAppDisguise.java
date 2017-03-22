package com.klauncher.ext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.klauncher.launcher.BuildConfig;
import com.klauncher.launcher.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yanni on 16/3/15.
 */
public class KLauncherAppDisguise {

    public static class DisguiseIconInfo {
        public ComponentName componentName;
        public boolean readFromApplication;
    }

    private static final String DISGUISE_CONFIG_PREFIX = "config_icon_";
    private static final String DISGUISE_SHAREDPREFERENCES_NANME = "disguise_packages_sp";
    private static final String DISGUISE_SHAREDPREFERENCES_KEY = "disguise_packages";
    private static final long GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_ONE = 10000;
    private static final long GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_TWO = 30000;
    private static final long GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_THREE = 60000;
    private int mRequestCount = 0;

    private static final String DISGUISE_APPS_SERVER = "http://api.klauncher.com/v2/config/funclist?cid=";

    private static KLauncherAppDisguise sInstance = new KLauncherAppDisguise();
    public static KLauncherAppDisguise getInstance() {
        return sInstance;
    }

    private final HashMap<String, String> mDisguiseAppMap = new HashMap<>();
    private Context mContext = null;
    MyHandler myHandler;

    static class MyHandler extends Handler {
        WeakReference mKap;
        MyHandler(KLauncherAppDisguise kap) {
            mKap = new WeakReference(kap);
        }

        @Override
        public void handleMessage(Message msg) {
            KLauncherAppDisguise kap = (KLauncherAppDisguise) mKap.get();
            if (kap !=null) {
                new Thread(kap.mGetDisguisePackagesRunbale).start();
            }
        }
    }

    private Runnable mGetDisguisePackagesRunbale = new Runnable(){
        @Override
        public synchronized void run() {
            getDisguiseAppsOnline();
        }
    };

    private KLauncherAppDisguise() {}

    public void init(Context context) {
        if (context == null) {
            return;
        }
        mContext = context;
        myHandler = new MyHandler(this);
        mDisguiseAppMap.clear();
        getLocalDisguisePackages(mContext, DISGUISE_SHAREDPREFERENCES_KEY);
        if (mDisguiseAppMap.size() == 0) {
            String[] appList = mContext.getResources().getStringArray(R.array.app_disguise_list);
            for (String pkg : appList) {
                int id = mContext.getResources().getIdentifier(
                        DISGUISE_CONFIG_PREFIX + pkg, "string", mContext.getPackageName());
                String frontPackageName = id == 0 ? null : mContext.getResources().getString(id);
                mDisguiseAppMap.put(pkg, frontPackageName);
            }
        }
        new Thread(mGetDisguisePackagesRunbale).start();

    }

    private void getDisguiseAppsOnline() {
        OkHttpClient client = new OkHttpClient();
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(DISGUISE_APPS_SERVER);
        sbUrl.append(BuildConfig.CHANNEL_ID);
        Log.d("KLauncherAppDisguise", "getDisguiseAppsOnline url : " + sbUrl.toString());
        Request request = new Request.Builder().url(sbUrl.toString()).build();
        Response response = null;
        try {
            mRequestCount++;
            Log.d("KLauncherAppDisguise", "getDisguiseAppsOnline start");
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String str = response.body().string();
                Log.d("KLauncherAppDisguise", "getDisguiseAppsOnline Successful : " + str);
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    JSONArray jsonArray = new JSONArray(jsonObject.getString("cw"));
                    HashMap<String, String> olDisguiseAppMap = new HashMap<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject contentJson = jsonArray.getJSONObject(i);
                        if (contentJson.toString().contains("notuninstall_pkg")) {
                            JSONArray pkgJsonArray = new JSONArray(contentJson.getString("notuninstall_pkg"));
                            for (int j = 0; j < pkgJsonArray.length(); j++) {
                                JSONObject pkgJson = pkgJsonArray.getJSONObject(j);
                                String pkg = pkgJson.getString("pkg");
                                Log.d("KLauncherAppDisguise", "getDisguiseAppsOnline Successful pkg : " + pkg);
                                int id = mContext.getResources().getIdentifier(
                                        DISGUISE_CONFIG_PREFIX + pkg, "string", mContext.getPackageName());
                                String frontPackageName = id == 0 ? null : mContext.getResources().getString(id);
                                olDisguiseAppMap.put(pkg, frontPackageName);
                            }
                        }
                    }
                    if (olDisguiseAppMap != null && olDisguiseAppMap.size() > 0) {
                        mDisguiseAppMap.clear();
                        mDisguiseAppMap.putAll(olDisguiseAppMap);
                    }
                    Log.d("KLauncherAppDisguise", "getDisguiseAppsOnline saveOnlineDisguisePackages ");
                    saveOnlineDisguisePackages(mContext, DISGUISE_SHAREDPREFERENCES_KEY, mDisguiseAppMap);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.d("KLauncherAppDisguise", "getDisguiseAppsOnline failed parse json ");
                    e.printStackTrace();
                }

            } else {
                Log.d("KLauncherAppDisguise", "getDisguiseAppsOnline failed responses ");
                if (mRequestCount == 1) {
                    myHandler.sendEmptyMessageDelayed(0, GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_ONE);
                } else if (mRequestCount == 2) {
                    myHandler.sendEmptyMessageDelayed(0, GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_TWO);
                } else if (mRequestCount == 3) {
                    myHandler.sendEmptyMessageDelayed(0, GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_THREE);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("KLauncherAppDisguise", "getDisguiseAppsOnline failed IOException");
            if (mRequestCount == 1) {
                myHandler.sendEmptyMessageDelayed(0, GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_ONE);
            } else if (mRequestCount == 2) {
                myHandler.sendEmptyMessageDelayed(0, GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_TWO);
            } else if (mRequestCount == 3) {
                myHandler.sendEmptyMessageDelayed(0, GET_ONLINE_DISGUISE_PACKAGE_INTERVALS_THREE);
            }
        }
    }

    void saveOnlineDisguisePackages(Context context, String key, HashMap<String, String> maps) {
        Iterator<Map.Entry<String, String>> iterator = maps.entrySet().iterator();

        JSONObject object = new JSONObject();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            try {
                object.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {

            }
        }
        SharedPreferences sp = context.getSharedPreferences(DISGUISE_SHAREDPREFERENCES_NANME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, object.toString());
        editor.commit();
    }

    void getLocalDisguisePackages(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(DISGUISE_SHAREDPREFERENCES_NANME, Context.MODE_PRIVATE);
        String result = sp.getString(key, "");
        if (TextUtils.isEmpty(result)) {
            try {
                JSONObject object = new JSONObject(result);
                JSONArray names = object.names();
                if (names != null) {
                    for (int j = 0; j < names.length(); j++) {
                        String name = names.getString(j);
                        String value = object.getString(name);
                        mDisguiseAppMap.put(name, value);
                    }
                }
            } catch (JSONException e) {
                Log.d("KLauncherAppDisguise", "getLocalDisguisePackages error");
            }
        }

    }

    public boolean inPrivateAppList(String pkgName) {
        if (mContext == null) {
            return false;
        }
        return mDisguiseAppMap.containsKey(pkgName);
    }

    public DisguiseIconInfo getDisguiseIconInfo(ActivityInfo info) {
        if (info == null) {
            return null;
        }
        ComponentName componentName = getFrontComponent(info.packageName);
        DisguiseIconInfo iconInfo = new DisguiseIconInfo();
        if (componentName != null) {
            iconInfo.componentName = componentName;
            iconInfo.readFromApplication = info.targetActivity == null ? true :
                    componentName.getClassName().equals(info.targetActivity);
        }
        return iconInfo;
    }

    private ComponentName getFrontComponent(String backendPackageName) {
        if (mContext == null) {
            return null;
        }
        String frontPackageName = mDisguiseAppMap.get(backendPackageName);
        if (frontPackageName == null) {
            return null;
        }
        if (frontPackageName.contains(",")) {
            String[] apps = frontPackageName.split(",");
            for (String pkg : apps) {
                ComponentName componentName = getComponent(pkg);
                if (componentName != null) {
                    return componentName;
                }
            }
            return null;
        } else {
            return getComponent(frontPackageName);
        }
    }

    private ComponentName getComponent(String action) {
        ComponentName component = null;
        if (action.contains("/")) {
            String[] cns = action.split("/");
            if (cns != null && cns.length == 2) {
                component = new ComponentName(cns[0], cns[1]);
            }
        } else {
            Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(action);
            if (launchIntent != null) {
                component = launchIntent.getComponent();
            }
        }
        if (component != null) {
            try {
                ActivityInfo activityInfo = mContext.getPackageManager().getActivityInfo(
                        component, PackageManager.GET_META_DATA);
                if (activityInfo != null) {
                    return component;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return null;
    }
}
