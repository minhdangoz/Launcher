package com.klauncher.kinflow.weather;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.klauncher.ext.ClockWidgetService;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.launcher.R;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

/**
 * Created by hw on 16-8-12.
 */
public class Kinflow2Weather extends FrameLayout implements View.OnClickListener, ClockWidgetService.UpdateClockWeatherListener{

    private final static String TAG = "Kinflow2Weather";

    private Launcher mContext;

    @Override
    public void onClick(View v) {
        try {
            ComponentName cn = new ComponentName("cn.etouch.ecalendar","cn.etouch.ecalendar.ECalendar");

            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setComponent(cn);
            getMContext().startActivity(target);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getMContext(),"请先下载中华万年历",Toast.LENGTH_LONG).show();
        }catch (Exception e) {
            KinflowLog.e("天气模块在启动调用中华万年历时，出现未知错误："+e.getMessage());
        }

    }

    @Override
    public void updatetClock() {
    }

    @Override
    public void updateWeather(String weatherInfo) {
        try {
            if (TextUtils.isEmpty(weatherInfo)) {
                return;
            } else {
                JSONObject obj = new JSONObject(weatherInfo);
                JSONObject contentObj = new JSONObject(obj.getString("content"));
                String cityName = contentObj.getString("cityName");
                String pubDate = contentObj.getString("pubDate");
                JSONObject weatherObj = new JSONObject(contentObj.getString("weather"));
                int nowTemp = weatherObj.getInt("nowTemp");
                String climate = weatherObj.getString("climate");//天气状况:阴,晴
                String icon = weatherObj.getString("icon");
                int highTemp = weatherObj.getInt("highTemp");
                int lowTemp = weatherObj.getInt("lowTemp");
                int humidity = contentObj.getInt("humidity");
//                String pm = contentObj.getString("pm");
                String pm = contentObj.optString("pm", "优 45");//天气质量
                Picasso.with(getMContext())
                        .load(icon).fit().into(weatherIcon);

                String[] airInfos = pm.split(" ");
                String airExplainStr = "空气" + airInfos[0];
                String airIndexStr = airInfos[1];
                weatherTmp.setText(nowTemp + "\u00B0");//温度
                weatherCity.setText(cityName);//城市
                weatherExplain.setText(climate);//天气说明
                airIndex.setText(airIndexStr);//空气指数
                airExplain.setText(airExplainStr);//空气说明
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onPostExecute JSONException ");
        }
    }


    private final int[] CLOCK_IMAGES = new int[]{
            R.drawable.clock_0, R.drawable.clock_1, R.drawable.clock_2, R.drawable.clock_3,
            R.drawable.clock_4, R.drawable.clock_5, R.drawable.clock_6, R.drawable.clock_7,
            R.drawable.clock_8, R.drawable.clock_9
    };

    public Context getMContext(){
//        if (null==mContext)
//            mContext = KLauncherApplication.mKLauncherApplication;
        return mContext;
    }

    private RelativeLayout mRootView;
    //    private ImageView hour0, hour1, minute0, minute1;
    private ImageView weatherIcon;
    private TextView weatherTmp, weatherCity, weatherExplain;
    private TextView airIndex, airExplain;

//    private TextView dateView;


    public Kinflow2Weather(Context context) {
        super(context);
        try {
            mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_kinflow2_weather, this, false);
            addView(mRootView);
            mContext = (Launcher) context;
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Kinflow2Weather(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_kinflow2_weather, this, false);
            addView(mRootView);
            mContext = (Launcher)context;
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Kinflow2Weather(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_kinflow2_weather, this, false);
            addView(mRootView);
            mContext = (Launcher) context;
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     */
    private void init() {
        try {
            mContext.addUpdateClockWeatherListener(this);
            weatherIcon = (ImageView) mRootView.findViewById(R.id.weather_icon);
            weatherTmp = (TextView) mRootView.findViewById(R.id.weather_tmp);
            weatherCity = (TextView) mRootView.findViewById(R.id.weather_city);
            weatherExplain = (TextView) mRootView.findViewById(R.id.weather_explain);
            airIndex = (TextView) mRootView.findViewById(R.id.air_index);
            airExplain = (TextView) mRootView.findViewById(R.id.air_explain);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_DATE_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_TIME_TICK);
            Log.d(TAG, "Kinflow2Weather: onEnabled...");

            setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

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
