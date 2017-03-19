package com.klauncher.ext;

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher3.Launcher;
import com.klauncher.launcher.R;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hw on 16-8-12.
 */
public class ClockWidgetView extends FrameLayout implements ClockWidgetService.UpdateClockWeatherListener{

    private final static String TAG = "ClockWidgetView";

    private Launcher mContext;

    @Override
    public void updatetClock() {
        updateViews();
    }

    @Override
    public void updateWeather(String weatherInfo) {
        if (TextUtils.isEmpty(weatherInfo)) {
            return;
        }
        try {
            JSONObject obj = new JSONObject(weatherInfo);
            JSONObject contentObj = new JSONObject(obj.getString("content"));
            String cityName = contentObj.getString("cityName");
            String pubDate = contentObj.getString("pubDate");
            JSONObject weatherObj = new JSONObject(contentObj.getString("weather"));
            int nowTemp = weatherObj.getInt("nowTemp");
            String climate = weatherObj.getString("climate");
            String icon = weatherObj.getString("icon");
            int highTemp = weatherObj.getInt("highTemp");
            int lowTemp = weatherObj.getInt("lowTemp");
            int humidity = contentObj.getInt("humidity");
            String pm = contentObj.getString("pm");
            Picasso.with(mContext)
                    .load(icon).fit().into(weatherIcon);
            weatherTmp.setText(climate + "  " + nowTemp+"℃");
            weatherCity.setText(cityName);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"onPostExecute Exception ");
        }
    }


    private final int[] CLOCK_IMAGES = new int[]{
            R.drawable.clock_0, R.drawable.clock_1, R.drawable.clock_2, R.drawable.clock_3,
            R.drawable.clock_4, R.drawable.clock_5, R.drawable.clock_6, R.drawable.clock_7,
            R.drawable.clock_8, R.drawable.clock_9
    };

    private RelativeLayout mRootView;
    private ImageView hour0, hour1, minute0, minute1;
    private ImageView weatherIcon;
    private TextView weatherTmp, weatherCity;

    private TextView dateView;


    public ClockWidgetView(Context context) {
        super(context);
        mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.clock_widget, this, false);
        addView(mRootView);
        mContext = (Launcher) context;
        init();
    }

    public ClockWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRootView = (RelativeLayout)LayoutInflater.from(context).inflate(R.layout.clock_widget, this, false);
        addView(mRootView);
        mContext = (Launcher) context;
        init();
    }

    public ClockWidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRootView = (RelativeLayout)LayoutInflater.from(context).inflate(R.layout.clock_widget, this, false);
        addView(mRootView);
        mContext = (Launcher) context;
        init();
    }

    private void init() {
        mContext.addUpdateClockWeatherListener(this);
        hour0 = (ImageView) mRootView.findViewById(R.id.img_clock_hour_0);
        hour1 = (ImageView) mRootView.findViewById(R.id.img_clock_hour_1);
        minute0 = (ImageView) mRootView.findViewById(R.id.img_clock_minute_0);
        minute1 = (ImageView) mRootView.findViewById(R.id.img_clock_minute_1);
        weatherIcon = (ImageView) mRootView.findViewById(R.id.weather_icon);
        weatherTmp = (TextView) mRootView.findViewById(R.id.weather_tmp);
        weatherCity = (TextView) mRootView.findViewById(R.id.weather_city);
        dateView = (TextView) mRootView.findViewById(R.id.clock_date);
        updateViews();

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
//        if (isChina(mContext)) {
//            updateDate4China("12".equals(timeFormat));
//        } else {
//            updateDate4English("12".equals(timeFormat));
//        }
        updateDate();

        Calendar c = Calendar.getInstance();
        int hour, minute;
        minute = c.get(Calendar.MINUTE);
        if (is12hour) {
            hour = c.get(Calendar.HOUR);
        } else {
            hour = c.get(Calendar.HOUR_OF_DAY);
        }
//        if (isHasUpdateWeather) {
//            if (DeviceInfoUtils.isWifiConnected(mContext)) {
//                if (hour % 2 == 0 && minute == 0) {
//                    getLocation();
//                }
//            } else if (DeviceInfoUtils.isMobileDataConnected(mContext)) {
//                if (hour % 4 == 0 && minute == 0) {
//                    getLocation();
//                }
//            }
//        } else {
//            if (DeviceInfoUtils.isWifiConnected(mContext)) {
//                if (isFirstReq) {
//                    getLocation();
//                    isFirstReq = false;
//                } else {
//                    if (minute % 10 == 0) {
//                        getLocation();
//                    }
//                }
//            } else if (DeviceInfoUtils.isMobileDataConnected(mContext)) {
//                if (isFirstReq) {
//                    getLocation();
//                    isFirstReq = false;
//                } else {
//                    if (minute % 15 == 0) {
//                        getLocation();
//                    }
//                }
//            }
//        }

        hour0.setImageResource(CLOCK_IMAGES[hour / 10]);
        hour1.setImageResource(CLOCK_IMAGES[hour % 10]);
        minute0.setImageResource(CLOCK_IMAGES[minute / 10]);
        minute1.setImageResource(CLOCK_IMAGES[minute % 10]);
    }

    private void updateDate(){
        Date now = new Date();
        SimpleDateFormat sdf;
        if (android.text.format.DateFormat.is24HourFormat(mContext)) {
            sdf = new SimpleDateFormat("MM/dd E");
        } else {
            sdf = new SimpleDateFormat("MM/dd E a");
        }
        dateView.setText(sdf.format(now));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.removeUpdateClockWeatherListener(this);
    }


}
