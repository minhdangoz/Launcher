package com.klauncher.biddingos.commons.analytics;


import com.klauncher.biddingos.commons.cache.SharedPreferencesUtils;
import com.klauncher.biddingos.commons.receiver.AppInstallReceiver;

public class AppScanner {
    private static final String TAG = "AppScanner";
    public static void init() {
        SharedPreferencesUtils.init();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (SharedPreferencesUtils.isPackageListEmpty())
                    EventHelper.postEventFullPkg();
                else {
                    //增量上报
                    EventHelper.postEventIncrementalPkg();
                }
                //注册监听器
                AppInstallReceiver.register();
            }
        }).start();



    }
}
