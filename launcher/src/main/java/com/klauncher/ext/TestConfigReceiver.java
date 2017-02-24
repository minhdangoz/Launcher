package com.klauncher.ext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hw on 16-10-27.
 */
public class TestConfigReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        if (arg1.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClass(arg0, TestConfigActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            arg0.startActivity(i);
        }
    }
}
