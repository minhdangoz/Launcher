package com.klauncher.ext;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.klauncher.launcher.R;

/**
 * Created by wangqinghao on 2016/4/25.
 */
public class ShortCutManagerService extends Service {
    public static final String TAG = "ShortCutManagerService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Startup", TAG + "onCreate() executed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Startup", TAG + "onStartCommand() executed");
        if (intent != null && intent.getBooleanExtra("add_klaucher3_wifi", false)) {
            addWifiShortCut(true);
        }
        if (intent != null && intent.getBooleanExtra("add_laucher_wifi", false)) {
            // 创建默认桌面快捷方式 wifi设置快捷方式
            if (ShortCutManager.isPkgInstalled(this, KlauncherStartupReceiver.WIFISETTING_PACKAGENAME) &&
                    !ShortCutManager.hasShortcut(this, KlauncherStartupReceiver.WIFISETTING_PACKAGENAME)) {
//            ComponentName cn = new ComponentName("com.farproc.wifi.connecter", "TestWifiScan");
//            ShortCutManager.addShortcut(this,R.string.wifi_setting, R.drawable.wifi,cn);
                addWifiShortCut(false);
                Log.d("Startup", "Wifi add shortcut");
            }
            //klancher have lancher  删除默认桌面快捷方式  创建wifi设置快捷方式
            if (!ShortCutManager.hasShortcut(this, this.getPackageName())) {
//            ComponentName cn = new ComponentName("com.klauncher.launcher", "com.klauncher.ext.KLauncher");
//            ShortCutManager.addShortcut(this,R.string.application_name, R.mipmap.ic_launcher_home,cn);
                addKlaucher();
                Log.d("Startup", "klancher add shortcut");
            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Startup", TAG + "onDestroy() executed");
    }

    private void addWifiShortCut(boolean isKlaucher) {
        Intent shortcut;
        // 安装的Intent
        if (isKlaucher) {
            shortcut = new Intent(
                    "com.klauncher.launcher.permission.INSTALL_SHORTCUT");
        } else {
            shortcut = new Intent(
                    "com.android.launcher.action.INSTALL_SHORTCUT");
        }

        // 快捷名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, "无限设置");
        // 快捷图标是允许重复
        shortcut.putExtra("duplicate", false);

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName("com.farproc.wifi.connecter",
                "com.farproc.wifi.connecter.TestWifiScan");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        // 快捷图标
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(
                this, R.drawable.wifi);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        //shortcutIntent.setAction("android.intent.action.MAIN");
        shortcutIntent.addCategory("android.intent.category.LAUNCHER");
        // 发送广播
        this.sendBroadcast(shortcut);
        Log.d("Startup", "addWifiShortCut");
    }

    private void addKlaucher() {
        // 安装的Intent
        Intent shortcut2 = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");

        // 快捷名称
        shortcut2.putExtra(Intent.EXTRA_SHORTCUT_NAME, "默认桌面");
        // 快捷图标是允许重复
        shortcut2.putExtra("duplicate", false);

        Intent shortcutIntent2 = new Intent(Intent.ACTION_MAIN);
        shortcutIntent2.setClassName("com.klauncher.launcher",
                "com.klauncher.ext.KLauncher");
        shortcut2.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent2);

        // 快捷图标
        Intent.ShortcutIconResource iconRes2 = Intent.ShortcutIconResource
                .fromContext(this, R.mipmap.ic_launcher_home);
        shortcut2.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes2);

        // 发送广播
        sendBroadcast(shortcut2);
        Log.d("Startup", "addKlaucher");

    }

}
