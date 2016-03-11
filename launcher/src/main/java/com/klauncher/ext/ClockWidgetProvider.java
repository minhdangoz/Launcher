package com.klauncher.ext;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.umeng.common.message.Log;
import com.klauncher.launcher.R;

import java.util.Calendar;
import java.text.DateFormat;
import java.util.Locale;

/**
 * Search widget
 */
public class ClockWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "ClockWidgetProvider";

    private final int[] CLOCK_IMAGES = new int[] {
            R.drawable.clock_0, R.drawable.clock_1, R.drawable.clock_2, R.drawable.clock_3,
            R.drawable.clock_4, R.drawable.clock_5, R.drawable.clock_6, R.drawable.clock_7,
            R.drawable.clock_8, R.drawable.clock_9
    };
    // Lazily creating this name to use with the AppWidgetManager
    private ComponentName mComponentName;

    private ComponentName getComponentName(Context context) {
        if (mComponentName == null) {
            mComponentName = new ComponentName(context, getClass());
        }
        return mComponentName;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_LOCALE_CHANGED.equals(action)
                || Intent.ACTION_SCREEN_ON.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)
                || ClockWidgetService.ACTION_CLOCK_UPDATE.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (appWidgetManager != null) {
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(getComponentName(context));
                for (int appWidgetId : appWidgetIds) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                            R.id.clock_widget_bar);
                    RemoteViews widget = new RemoteViews(context.getPackageName(),
                            R.layout.clock_widget);
                    updateViews(context, widget);
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, widget);
                }
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "ClockWidgetProvider: onEnabled...");
        Intent service = new Intent(context, ClockWidgetService.class);
        context.startService(service);

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG, "ClockWidgetProvider: onDisabled...");
        Intent service = new Intent(context, ClockWidgetService.class);
        context.stopService(service);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.deskclock",
                    "com.android.deskclock.DeskClockTabActivity"));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock_widget);
            views.setOnClickPendingIntent(R.id.clock_widget_bar, pendingIntent);
            updateViews(context, views);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void updateViews(Context context, RemoteViews views) {
        ContentResolver cv = context.getContentResolver();
        String timeFormat = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);
        boolean is12hour = "12".equals(timeFormat);
        if (isChina(context)) {
            updateDate4China(views, "12".equals(timeFormat));
        } else {
            updateDate4English(views, "12".equals(timeFormat));
        }

        Calendar c = Calendar.getInstance();
        int hour, minute;
        if (is12hour) {
            hour = c.get(Calendar.HOUR);
        } else {
            hour = c.get(Calendar.HOUR_OF_DAY);
        }
        minute = c.get(Calendar.MINUTE);
        views.setImageViewResource(R.id.img_clock_hour_0, CLOCK_IMAGES[hour / 10]);
        views.setImageViewResource(R.id.img_clock_hour_1, CLOCK_IMAGES[hour % 10]);
        views.setImageViewResource(R.id.img_clock_minute_0, CLOCK_IMAGES[minute / 10]);
        views.setImageViewResource(R.id.img_clock_minute_1, CLOCK_IMAGES[minute % 10]);
    }

    private void updateDate4English(RemoteViews views, boolean is12hour) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT);
        String dateString = df.format(new java.util.Date());
        int lastIndex = dateString.lastIndexOf(",");
        String dateValue = dateString.substring(0, lastIndex);
        if (is12hour) {
            dateValue += " " + dateString.substring(dateString.length() - 3);
        }
        views.setTextViewText(R.id.clock_date, dateValue);
    }

    private void updateDate4China(RemoteViews views, boolean is12hour) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT);
        String[] dateSplit = df.format(new java.util.Date()).split(" ");
        if (dateSplit != null && dateSplit.length == 2) {
            String dateValue = dateSplit[0].substring(5);
            if (is12hour) {
                dateValue += " " + dateSplit[1].substring(0, 2);
            }
            views.setTextViewText(R.id.clock_date, dateValue);
        }
    }

    private boolean isChina(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
}