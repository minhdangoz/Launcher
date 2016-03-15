package com.klauncher.ext;

import android.content.Context;
import android.content.pm.PackageManager;

import com.klauncher.launcher.R;

import java.util.HashMap;

/**
 * Created by yanni on 16/3/15.
 */
public class KLauncherAppDisguise {
    private static final String DISGUISE_CONFIG_PREFIX = "config_";

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

    public String getFrontPackageName(String pkgName) {
        if (mContext == null) {
            return null;
        }
        String pkgString = mDisguiseAppMap.get(pkgName);
        if (pkgString != null && pkgString.contains(",")) {
            String[] apps = pkgString.split(",");
            for (String pkg : apps) {
                try {
                    if (mContext.getPackageManager().getApplicationIcon(pkg) != null) {
                        return pkg;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    continue;
                }
            }
            return null;
        } else {
            return pkgString;
        }
    }
}
