package com.klauncher.ext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by wangqinghao on 2016/4/25.
 * 监听开机启动  app 卸载安装
 */
public class KlauncherStartupReceiver extends BroadcastReceiver {
    // 系统启动完成
    static final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
    // 设备上新安装了一个应用程序包
    static final String PACKAGE_ADDED_ACTION = "android.intent.action.PACKAGE_ADDED";
    // 设备上删除了一个应用程序包
    static final String PACKAGE_REMOVED_ACTION = "android.intent.action.PACKAGE_REMOVED";
    static final String WIFISETTING_PACKAGENAME = "com.farproc.wifi.connecter";
    static final String WIFISETTING_ACTIVITY = "com.farproc.wifi.connecter.TestWifiScan";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BOOT_COMPLETED_ACTION)) {
            // start addShortCut service
            Intent startIntent = new Intent(context, ShortCutManagerService.class);
            startIntent.putExtra("add_laucher_wifi",true);
            context.startService(startIntent);
            Log.d("Startup","BroadcastReceiver BOOT_COMPLETED_ACTION onResceive");
        }else if (intent.getAction().equals(PACKAGE_ADDED_ACTION)) {
            // //安装apk 获取应用程序包名
            String packageName = intent.getDataString();
            if (WIFISETTING_PACKAGENAME.equals(packageName)) {
                //add shortcut
            }
        } else if (intent.getAction().equals(PACKAGE_REMOVED_ACTION)) {
            String packageName = intent.getDataString();
            if (WIFISETTING_PACKAGENAME.equals(packageName)) {
                //remove shortcut

            }
        }

    }
}
