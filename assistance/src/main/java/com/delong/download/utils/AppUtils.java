package com.delong.download.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by alex on 15/10/29.
 */
public class AppUtils {


    public static Drawable getAppIconDrawable(Context ctx, String pkgName) {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
            return pi.applicationInfo.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    public static Drawable getAppIconDrawable(Context ctx, PackageInfo pi) {
        PackageManager pm = ctx.getPackageManager();
        if (pi != null) {
            return pi.applicationInfo.loadIcon(pm);
        }
        return null;
    }

    public static List<PackageInfo> getAllPackageInfo(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        return pm.getInstalledPackages(0);
    }

    public static boolean isAppInstalled(Context ctx, String pkg, int versionCode) {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(pkg, 0);
            if (pi != null && pi.versionCode == versionCode) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    public static boolean isAppInstalled(Context ctx, String pkg) {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(pkg, 0);
            if (pi != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    public static List<PackageInfo> getSystemPackageInfo(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        List<PackageInfo> allApps = pm.getInstalledPackages(0);
        List<PackageInfo> sysAppList = new ArrayList<>();

        for (PackageInfo pi : allApps) {
            if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                sysAppList.add(pi);
            }
        }

        return sysAppList;
    }

    public static List<PackageInfo> getInstallPackageInfo(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        List<PackageInfo> allApps = pm.getInstalledPackages(0);
        List<PackageInfo> installAppList = new ArrayList<>();

        for (PackageInfo pi : allApps) {
            if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                installAppList.add(pi);
            }
        }

        return installAppList;
    }


    public static Drawable getAPKIconDrawable(Context ctx, String filePath) {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            appInfo.sourceDir = filePath;
            appInfo.publicSourceDir = filePath;
            return appInfo.loadIcon(pm);
        }
        return null;
    }

    public static PackageInfo getPackageInfo(Context ctx, String pkgName) {
        PackageManager pm = ctx.getPackageManager();
        try {
            return pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    public static ApplicationInfo getApplicationInfo(Context ctx, String pkgName) {
        PackageManager pm = ctx.getPackageManager();
        try {
            return pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES).applicationInfo;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    public static PackageInfo getAPKPackageInfo(Context ctx, String filePath) {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (pi != null) {
            pi.applicationInfo.sourceDir = filePath;
            pi.applicationInfo.publicSourceDir = filePath;
        }
        return pi;
    }

    public static String getAppName(Context ctx, PackageInfo pi) {
        return pi.applicationInfo.loadLabel(ctx.getPackageManager()).toString();
    }

    public static String getAppName(Context ctx, String pkg) {
        PackageManager pm = ctx.getPackageManager();
        try {
            ApplicationInfo ai = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES).applicationInfo;
            return ai.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    public static String getAppSignature(Context ctx, String pkgName) {
        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
            if (pi != null) {
                Signature[] signatures = pi.signatures;
                StringBuilder builder = new StringBuilder();
                for (Signature signature : signatures) {
                    builder.append(signature.toCharsString());
                }
                return builder.toString();
            }
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isActivityLaunched(Context context, Class activityClass) {
        Intent mainIntent = new Intent(context, activityClass);
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> appTask = am.getRunningTasks(1);
        if (appTask.size() > 0
                && appTask.get(0).baseActivity
                .equals(mainIntent.getComponent())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean openApp(Context context, String packageName) {
        Intent mainIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        if (mainIntent == null) {
            return false;
        }
        context.startActivity(mainIntent);
        return true;
    }

    public static boolean isCanLaunchApp(Context context, String packageName) {
        Intent mainIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        return mainIntent != null;
    }

    public static boolean existLaunchActivity(Context context, String packageName) {
        Intent mainIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        return mainIntent != null;
    }
    public  static List<ResolveInfo> getAllApp(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);

        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> ril = pm
                .queryIntentActivities(mainIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

        Collections.sort(ril, new ResolveInfo.DisplayNameComparator(pm));

        return ril;
    }
    public static boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    /**
     * install an app on user's mobile phone by the path of the apk.
     *
     * @param apkFilePath the path of the apk file;
     */
    public static void installApk(Context context, String apkFilePath) {
        File apkFile = new File(apkFilePath);
        if (!apkFile.exists()) {
            Toast.makeText(context,"apk file is not exist",Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.fromFile(apkFile);
        installApk(context, uri);
    }
    public static void installApk(Context context, Uri uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(i);
    }
}
