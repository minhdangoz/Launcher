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
import com.klauncher.utilities.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Search widget
 */
public class SearchWidgetProvider extends AppWidgetProvider {
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
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

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.search_widget);
            views.setOnClickPendingIntent(R.id.search_widget_bar, pendingIntent);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
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
}