package com.klauncher.ext;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;

import com.klauncher.utilities.LogUtil;

/**
 * Created by yanni on 16/2/26.
 */
public class KLauncherDeviceAdminReceiver extends DeviceAdminReceiver {
    public void lockScreen(Context context) {
        LogUtil.e("DeviceAdmin", "lockScreen");
        Intent intent1 = context.getPackageManager().getLaunchIntentForPackage("com.android.settings");
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
        final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        dpm.lockNow();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < 15) {
                    dpm.lockNow();
                    try {
                        Thread.sleep(500);
                        i++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        LogUtil.e("DeviceAdmin", "onReceive");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
//        LogUtil.e("DeviceAdmin", "onDisableRequested");
//        long delta = System.currentTimeMillis() - CommonShareData.getLong(
//                Const.NAVIGATION_LOCAL_FIRST_INIT, System.currentTimeMillis());
//        if (delta > 3600 * 24 * 1000) {
//            lockScreen(context);
//        }
        return "You have no need to cancel this policy manager...";
    }

}
