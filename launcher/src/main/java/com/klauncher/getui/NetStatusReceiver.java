package com.klauncher.getui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.klauncher.ext.KLauncherApplication;

/**
 * Created by wangqinghao on 2016/5/13.
 */
public class NetStatusReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        KLauncherApplication.mKLauncherApplication.initGeitui();
    }

}
