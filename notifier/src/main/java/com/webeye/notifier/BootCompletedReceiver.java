package com.webeye.notifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {    
    static final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BOOT_COMPLETED_ACTION)) {
            Log.i(Notifier.TAG, "boot completed action");
            Intent unreadService = new Intent(context, Notifier.class);
            context.startService(unreadService);
        }
    }

}
