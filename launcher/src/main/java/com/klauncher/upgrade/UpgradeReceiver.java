package com.klauncher.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Created by Administrator on 2017/1/17.
 */

public class UpgradeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), UpgradeHelper.ACTION_UPGRADE)) {
            UpgradeHelper.getInstance(context).checkImmediately();
        } else {
            UpgradeHelper.getInstance(context).startUpgradeMission();
        }
    }
}
