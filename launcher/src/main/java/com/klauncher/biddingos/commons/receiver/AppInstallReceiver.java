package com.klauncher.biddingos.commons.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.analytics.EventHelper;
import com.klauncher.biddingos.commons.analytics.UserEvent;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class AppInstallReceiver extends BroadcastReceiver {
    private static final String TAG = "AppInstallReceiver";
    public static AppInstallReceiver appInstallReceiver=null;

    public static synchronized void register () {

        if(appInstallReceiver==null) {
            appInstallReceiver = new AppInstallReceiver();
        }else {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        filter.setPriority(Integer.MAX_VALUE);
        Setting.context.registerReceiver(appInstallReceiver, filter);

        IntentFilter filterNet = new IntentFilter();
        filterNet.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filterNet.setPriority(Integer.MAX_VALUE);
        Setting.context.registerReceiver(appInstallReceiver, filterNet);

        LogUtils.i(TAG, "Register Receiver Done");
    }

    public static synchronized void unregister() {
        if(appInstallReceiver!=null) {
            Setting.context.unregisterReceiver(appInstallReceiver);
            appInstallReceiver = null;
        }
        LogUtils.i(TAG, "unregister Receiver Done");
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogUtils.d(TAG, action);
        if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(null!=networkInfo && networkInfo.isConnected()) {
                Log.d(TAG, "Wifi connected: " + String.valueOf(networkInfo));
                //发送历史事件
                EventHelper.sendSpMessage();
            }
        }else if (Intent.ACTION_PACKAGE_REPLACED.equals(action) ||
                Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
                Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            LogUtils.d(TAG, "receiver : " + action + "; pkgName:" + packageName + "; replacing:" + replacing);
            if (packageName == null || packageName.length() == 0) {
                return;
            }
            List<String> stringList = new ArrayList<>();
            stringList.add(packageName);
            if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                //应用被替换
                LogUtils.d(TAG, "app replaced");
                EventHelper.postEventIncrementalPkg(stringList, UserEvent.EVENT_APPSCAN_UPDATED);
            }
            else if (Intent.ACTION_PACKAGE_ADDED.equals(action) && !replacing) {
                //应用被添加
                LogUtils.d(TAG, "app added");
                EventHelper.postEventIncrementalPkg(stringList, UserEvent.EVENT_APPSCAN_INSTALLED);

            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action) && !replacing) {
                //应用被删除
                LogUtils.d(TAG, "app removed");
                EventHelper.postEventIncrementalPkg(stringList, UserEvent.EVENT_APPSCAN_UNINSTALLED);
            }
        }
    }



}