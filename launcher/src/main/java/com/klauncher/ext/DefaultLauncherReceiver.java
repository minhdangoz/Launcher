package com.klauncher.ext;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.klauncher.getui.HomeSelector;
import com.klauncher.utilities.LogUtil;

/**
 * Created by yanni on 16/5/26.
 */
public class DefaultLauncherReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //0613
        /*Intent paramIntent = new Intent("android.intent.action.MAIN");
        paramIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        paramIntent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
        paramIntent.addCategory("android.intent.category.DEFAULT");
        paramIntent.addCategory("android.intent.category.HOME");
        context.startActivity(paramIntent);*/
        /*//获取跳转页面
        String pkgstr = null;
        String actStr = null;
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.addCategory("android.intent.category.HOME");
        ResolveInfo localResolveInfo = context.getPackageManager().resolveActivity(localIntent, 0);
        if (localResolveInfo == null || localResolveInfo.activityInfo == null) {
            pkgstr = "";
        } else if (!"android".equals(localResolveInfo.activityInfo.packageName)) {
            pkgstr = localResolveInfo.activityInfo.packageName;
            actStr = localResolveInfo.activityInfo.name;

        } else {
            pkgstr = localResolveInfo.activityInfo.packageName;
            actStr = localResolveInfo.activityInfo.name;
        }
        if (TextUtils.isEmpty(pkgstr) || TextUtils.isEmpty(actStr)) {
            return;
        }

        Intent paramIntent = new Intent("android.intent.action.MAIN");
        paramIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        paramIntent.setComponent(new ComponentName(pkgstr, actStr));
        paramIntent.addCategory("android.intent.category.DEFAULT");
        paramIntent.addCategory("android.intent.category.HOME");
        context.startActivity(paramIntent);
    */
        //清除默认设置
        Intent paramIntent = new Intent("android.intent.action.MAIN", null);
        paramIntent.addCategory("android.intent.category.HOME");
        ResolveInfo info = context.getPackageManager().resolveActivity(paramIntent, PackageManager.MATCH_DEFAULT_ONLY);
        LogUtil.e("DefaultLauncherReceiver",info.activityInfo.packageName);
        if ("android".equalsIgnoreCase(info.activityInfo.packageName)) {
            //android  没有设置默认桌面  直接设置
            Intent localIntent = new Intent("android.intent.action.MAIN", null);
            localIntent.addCategory("android.intent.category.HOME");
            //localIntent.addFlags(270532608);
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(localIntent);
            LogUtil.e("DefaultLauncherReceiver","android end");
        } else {
            //有默认程序 将现在的默认的程序的默认设置清除
            Intent settIntent = new Intent(context,HomeSelector.class);
            settIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settIntent);
            LogUtil.e("DefaultLauncherReceiver","other end");
        }
    }
}
