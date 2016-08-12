package com.klauncher.ext;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.klauncher.launcher.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by hw on 16-8-12.
 */
public class ClockWidgetView extends FrameLayout {

    private final static String TAG = "ClockWidgetView";

    private Context mContext;

    private final int[] CLOCK_IMAGES = new int[]{
            R.drawable.clock_0, R.drawable.clock_1, R.drawable.clock_2, R.drawable.clock_3,
            R.drawable.clock_4, R.drawable.clock_5, R.drawable.clock_6, R.drawable.clock_7,
            R.drawable.clock_8, R.drawable.clock_9
    };

    private RelativeLayout mRootView;
    private ImageView hour0, hour1, minute0, minute1;

    private TextView dateView;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_DATE_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_LOCALE_CHANGED.equals(action)
                    || Intent.ACTION_SCREEN_ON.equals(action)
                    || Intent.ACTION_USER_PRESENT.equals(action)
                    || Intent.ACTION_TIME_TICK.equals(action)) {
                updateViews();
            }
        }
    };

    public ClockWidgetView(Context context) {
        super(context);
        mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.clock_widget, this, false);
        addView(mRootView);
        mContext = context;
        init();
    }

    public ClockWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRootView = (RelativeLayout)LayoutInflater.from(context).inflate(R.layout.clock_widget, this, false);
        addView(mRootView);
        mContext = context;
        init();
    }

    public ClockWidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRootView = (RelativeLayout)LayoutInflater.from(context).inflate(R.layout.clock_widget, this, false);
        addView(mRootView);
        mContext = context;
        init();
    }

    private void init() {
//        mRootView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PackageManager packageManager = mContext.getPackageManager();
//                if (packageManager != null) {
//                    Intent AlarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(
//                            Intent.CATEGORY_LAUNCHER).setComponent(
//                            new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClock"));
//                    ResolveInfo resolved = packageManager.resolveActivity(AlarmClockIntent,
//                            PackageManager.MATCH_DEFAULT_ONLY);
//                    if (resolved != null) {
//                        mContext.startActivity(AlarmClockIntent);
//                    }
//                }
//            }
//        });
        hour0 = (ImageView) mRootView.findViewById(R.id.img_clock_hour_0);
        hour1 = (ImageView) mRootView.findViewById(R.id.img_clock_hour_1);
        minute0 = (ImageView) mRootView.findViewById(R.id.img_clock_minute_0);
        minute1 = (ImageView) mRootView.findViewById(R.id.img_clock_minute_1);

        dateView = (TextView) mRootView.findViewById(R.id.clock_date);
        updateViews();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_TIME_TICK);
//        filter.addAction(ClockWidgetService.ACTION_CLOCK_UPDATE);
        mContext.registerReceiver(receiver, filter);
        Log.d(TAG, "ClockWidgetProvider: onEnabled...");
//        Intent service = new Intent(mContext, ClockWidgetService.class);
//        mContext.startService(service);

    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

    }

    private void updateViews() {

        ContentResolver cv = mContext.getContentResolver();
        String timeFormat = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);
        boolean is12hour = "12".equals(timeFormat);
        if (isChina(mContext)) {
            updateDate4China("12".equals(timeFormat));
        } else {
            updateDate4English("12".equals(timeFormat));
        }

        Calendar c = Calendar.getInstance();
        int hour, minute;
        if (is12hour) {
            hour = c.get(Calendar.HOUR);
        } else {
            hour = c.get(Calendar.HOUR_OF_DAY);
        }
        minute = c.get(Calendar.MINUTE);

        hour0.setImageResource(CLOCK_IMAGES[hour / 10]);
        hour1.setImageResource(CLOCK_IMAGES[hour % 10]);
        minute0.setImageResource(CLOCK_IMAGES[minute / 10]);
        minute1.setImageResource(CLOCK_IMAGES[minute % 10]);
    }

    private void updateDate4English(boolean is12hour) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT);
        String dateString = df.format(new java.util.Date());
        int lastIndex = dateString.lastIndexOf(",");
        String dateValue = dateString.substring(0, lastIndex);
        if (is12hour) {
            dateValue += " " + dateString.substring(dateString.length() - 3);
        }
        dateView.setText(dateValue);
    }

    private void updateDate4China(boolean is12hour) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT);
        String[] dateSplit = df.format(new java.util.Date()).split(" ");
        if (dateSplit != null && dateSplit.length == 2) {
            String dateValue = dateSplit[0].substring(5);
            if (is12hour) {
                dateValue += " " + dateSplit[1].substring(0, 2);
            }
            dateView.setText(dateValue);
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("hw","hour : onAttachedToWindow ");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("hw","hour : onDetachedFromWindow ");
//        Intent service = new Intent(mContext, ClockWidgetService.class);
//        mContext.stopService(service);
    }
}
