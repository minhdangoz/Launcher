package com.klauncher.ext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import com.klauncher.launcher.R;

import java.util.HashMap;

/**
 * Created by yanni on 16/3/15.
 */
public class KLauncherAppDisguise {

    public static class DisguiseIconInfo {
        public ComponentName componentName;
        public boolean readFromApplication;
    }

    private static final String DISGUISE_CONFIG_PREFIX = "config_icon_";

    private static KLauncherAppDisguise sInstance = new KLauncherAppDisguise();
    public static KLauncherAppDisguise getInstance() {
        return sInstance;
    }

    private final HashMap<String, String> mDisguiseAppMap = new HashMap<>();
    private Context mContext = null;

    private KLauncherAppDisguise() {}

    public void init(Context context) {
        if (context == null) {
            return;
        }
        mContext = context;
        mDisguiseAppMap.clear();
        String[] appList = mContext.getResources().getStringArray(R.array.app_disguise_list);
        for (String pkg : appList) {
            int id = mContext.getResources().getIdentifier(
                        DISGUISE_CONFIG_PREFIX + pkg, "string", mContext.getPackageName());
            String frontPackageName = id == 0 ? null : mContext.getResources().getString(id);
            mDisguiseAppMap.put(pkg, frontPackageName);
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
