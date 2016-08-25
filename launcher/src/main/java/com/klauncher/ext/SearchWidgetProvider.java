package com.klauncher.ext;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.klauncher.utilities.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Search widget
 */
public class SearchWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "wqh_SearchWidgetProvider";
    private static final String SEARCH_WIDGET_CLICK = "klauncher_search_widget_click";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "onReceive");
        String action = intent.getAction();
        LogUtil.d(TAG, action);
        if (SEARCH_WIDGET_CLICK.equals(action)) {
            //点击Intent跳转
            setSearchIntent(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //wqh add
        //wqh  广播处理 发送广播 埋点 统计点击事件 在广播接收中处理
        LogUtil.i(TAG, "onupdated");
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.search_widget);
        Intent intentStart = new Intent();
        intentStart.setAction(SEARCH_WIDGET_CLICK);
        // 使用getBroadcast方法，得到一个PendingIntent对象，当该对象执行时，会发送一个广播
        PendingIntent pendingIntentStart = PendingIntent.getBroadcast(context,
                0, intentStart, 0);
        remoteViews.setOnClickPendingIntent(R.id.search_widget_bar,
                pendingIntentStart);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        //final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        /*for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            //Intent intent = new Intent(context, SearchActivity.class);
            //搜狗1 百度2
            Intent intent = new Intent();
            if ( isAppInstalled(context, "com.sogou.activity.src")) {
                intent.setAction(Intent.ACTION_MAIN);
                intent.setClassName("com.sogou.activity.src","com.sogou.activity.src.SplashActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }else if(!isAppInstalled(context, "com.sogou.activity.src")&& isAppInstalled(context,"com.baidu.searchbox")){
                intent.setAction(Intent.ACTION_MAIN);
                intent.setClassName("com.baidu.searchbox","com.baidu.searchbox.SplashActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }else {
                intent = new Intent(context, SearchActivity.class);
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.search_widget);
           *//* Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(SEARCH_WIDGET_CLICK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, broadcastIntent, 0);*//*
            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            views.setOnClickPendingIntent(R.id.search_widget_bar, pendingIntent);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }*/
    }

    public boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    /**
     * 处理 搜索Intent跳转
     *
     * @param context
     */
    private void setSearchIntent(Context context) {
        int searchType = 0;//搜索 类型 1 搜狗搜索 2  百度搜索
        Intent intent = new Intent();
        if (isAppInstalled(context, "com.sogou.activity.src")) {
            intent.setAction(Intent.ACTION_MAIN);
            intent.setClassName("com.sogou.activity.src", "com.sogou.activity.src.SplashActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LogUtil.d(TAG, "intent SOUGOU ");
            searchType = 1;
        } else if (!isAppInstalled(context, "com.sogou.activity.src") && isAppInstalled(context, "com.baidu.searchbox")) {
            intent.setAction(Intent.ACTION_MAIN);
            intent.setClassName("com.baidu.searchbox", "com.baidu.searchbox.SplashActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LogUtil.d(TAG, "intent BAIDU ");
            searchType = 2;
        } else {
            intent = new Intent(context, SearchActivity.class);
        }
        if (searchType == 1) {
            PingManager.getInstance().reportUserAction4App(
                    PingManager.KLAUNCHER_WIDGET_SOUGOU_SEARCH, context.getPackageName());
            LogUtil.d(TAG, "PingManager SOUGOU ");
        } else if (searchType == 2) {
            PingManager.getInstance().reportUserAction4App(
                    PingManager.KLAUNCHER_WIDGET_BAIDU_SEARCH, context.getPackageName());
            LogUtil.d(TAG, "PingManager BAIDU ");
        }
        context.startActivity(intent);
    }

}