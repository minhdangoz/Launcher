package com.klauncher.ext;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by yanni on 16/5/26.
 */
public class DefaultLauncherReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent paramIntent = new Intent("android.intent.action.MAIN");
        paramIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        paramIntent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
        paramIntent.addCategory("android.intent.category.DEFAULT");
        paramIntent.addCategory("android.intent.category.HOME");
        context.startActivity(paramIntent);
    }
}
