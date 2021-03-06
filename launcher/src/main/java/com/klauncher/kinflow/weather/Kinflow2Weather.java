package com.klauncher.kinflow.weather;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.common.utils.DeviceState;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.launcher.R;
import com.klauncher.utilities.DeviceInfoUtils;
import com.klauncher.utilities.WeakAsyncTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by hw on 16-8-12.
 */
public class Kinflow2Weather extends FrameLayout implements View.OnClickListener{

    private final static String TAG = "Kinflow2Weather";

    private Context mContext;

    private String mLocationJson;

    AMapLocationClientOption mLocationOption = null;
    AMapLocation aMapLocation;
    private boolean isHasUpdateWeather = false;
    private boolean isFirstReq = true;

    private static final String WEATHER_LOCATE_SERVER = "http://tq.91.com/api/";
    private static final String WEATHER_INFO_SERVER = "http://tq.91.com/foreignApi/";

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    if (Kinflow2Weather.this.aMapLocation != null) {
                        double moveDistance = gps2m(Kinflow2Weather.this.aMapLocation.getLatitude(), Kinflow2Weather.this.aMapLocation.getLongitude(), aMapLocation.getLatitude(), aMapLocation.getLongitude());
                        if(moveDistance < 5*1000){
                            return;
                        }
                    }
                    Kinflow2Weather.this.aMapLocation = aMapLocation;
//可在其中解析amapLocation获取相应内容。
                    Log.d(TAG,
                            aMapLocation.getLocationType() + " " +//获取当前定位结果来源，如网络定位结果，详见定位类型表
                                    aMapLocation.getLatitude() + " " +//获取纬度
                                    aMapLocation.getLongitude() + " " +//获取经度
                                    aMapLocation.getAccuracy() + " " +//获取精度信息
                                    aMapLocation.getAddress() + " " +//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                                    aMapLocation.getCountry() + " " +//国家信息
                                    aMapLocation.getProvince() + " " +//省信息
                                    aMapLocation.getCity() + " " +//城市信息
                                    aMapLocation.getDistrict() + " " +//城区信息
                                    aMapLocation.getStreet() + " " +//街道信息
                                    aMapLocation.getStreetNum() + " " +//街道门牌号信息
                                    aMapLocation.getCityCode() + " " +//城市编码
                                    aMapLocation.getAdCode() + " " +//地区编码
                                    aMapLocation.getAoiName() + " " +//获取当前定位点的AOI信息
                                    aMapLocation.getTime());
//获取定位时间
//                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date date = new Date(aMapLocation.getTime());
//                    df.format(date);


                    mLocationJson = "{\"address\":{\"SubLocality\":\""
                            + aMapLocation.getDistrict() + "\",\"CountryCode\":\"" + aMapLocation.getCountry()
                            + "\",\"Name\":\"" + aMapLocation.getAddress() + "\",\"State\":\""
                            + aMapLocation.getProvince() + "\",\"FormattedAddressLines\":[\"" + aMapLocation.getAddress()
                            + "\"],\"Country\":\"" + aMapLocation.getCountry()
                            + "\",\"City\":\"" + aMapLocation.getCity() + "\"},\"ip\"：\"" + DeviceInfoUtils.getLocalIpAddress(getMContext()) + "\"}";

//                    myHandler.post(sRunnable);
//                    Log.d(TAG," mLocationJson : " + mLocationJson);
                    myHandler.sendEmptyMessage(0);

                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e(TAG, "AmapError location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    private final double EARTH_RADIUS = 6378137.0;

    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private double gps2d(double lat_a, double lng_a, double lat_b, double lng_b) {
        double d = 0;
        lat_a = lat_a * Math.PI / 180;
        lng_a = lng_a * Math.PI / 180;
        lat_b = lat_b * Math.PI / 180;
        lng_b = lng_b * Math.PI / 180;

        d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a) * Math.cos(lat_b) * Math.cos(lng_b - lng_a);
        d = Math.sqrt(1 - d * d);
        d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
        d = Math.asin(d) * 180 / Math.PI;
        // d = Math.round(d*10000);
        return d;
    }

    MyHandler myHandler;

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

    private static class MyHandler extends Handler {
        WeakReference mClockWidget;

        MyHandler(Kinflow2Weather cwv) {
            mClockWidget = new WeakReference(cwv);
        }

        @Override
        public void handleMessage(Message msg) {
            Kinflow2Weather cwv = (Kinflow2Weather) mClockWidget.get();
            if (cwv != null) {
                switch (msg.what) {
                    case 0:
                        new MyTask(cwv).execute();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class MyTask extends WeakAsyncTask<Void, Void, String, Kinflow2Weather> {
        public MyTask(Kinflow2Weather target) {
            super(target);
        }

        @Override
        protected String doInBackground(Kinflow2Weather target, Void... params) { // 获取context,
            // 执行一些操作
            try {
                return target.postGetWeather(target.mLocationJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Kinflow2Weather target, String resultInfo) {
            Log.d(TAG, "onPostExecute : " + resultInfo);
            try {
                if (TextUtils.isEmpty(resultInfo)) {
                    return;
                } else {
                    JSONObject obj = new JSONObject(resultInfo);
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
                    Picasso.with(target.getMContext())
                            .load(icon).fit().into(target.weatherIcon);

                    String[] airInfos = pm.split(" ");
                    String airExplainStr = "空气" + airInfos[0];
                    String airIndexStr = airInfos[1];
                    target.weatherTmp.setText(nowTemp + "\u00B0");//温度
                    target.weatherCity.setText(cityName);//城市
                    target.weatherExplain.setText(climate);//天气说明
                    target.airIndex.setText(airIndexStr);//空气指数
                    target.airExplain.setText(airExplainStr);//空气说明
                    target.isHasUpdateWeather = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "onPostExecute JSONException ");
            }
        }
    }

    private final int[] CLOCK_IMAGES = new int[]{
            R.drawable.clock_0, R.drawable.clock_1, R.drawable.clock_2, R.drawable.clock_3,
            R.drawable.clock_4, R.drawable.clock_5, R.drawable.clock_6, R.drawable.clock_7,
            R.drawable.clock_8, R.drawable.clock_9
    };

    public Context getMContext(){
        if (null==mContext)
            mContext = KLauncherApplication.mKLauncherApplication;
        return mContext;
    }

    private RelativeLayout mRootView;
    //    private ImageView hour0, hour1, minute0, minute1;
    private ImageView weatherIcon;
    private TextView weatherTmp, weatherCity, weatherExplain;
    private TextView airIndex, airExplain;

//    private TextView dateView;

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

    public Kinflow2Weather(Context context) {
        super(context);
        try {
            mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_kinflow2_weather, this, false);
            addView(mRootView);
            mContext = context;
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
            mContext = context;
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
            mContext = context;
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
            initLocation();
            myHandler = new MyHandler(this);
//            hour0 = (ImageView) mRootView.findViewById(R.id.img_clock_hour_0);
//            hour1 = (ImageView) mRootView.findViewById(R.id.img_clock_hour_1);
//            minute0 = (ImageView) mRootView.findViewById(R.id.img_clock_minute_0);
//            minute1 = (ImageView) mRootView.findViewById(R.id.img_clock_minute_1);
            weatherIcon = (ImageView) mRootView.findViewById(R.id.weather_icon);
            weatherTmp = (TextView) mRootView.findViewById(R.id.weather_tmp);
            weatherCity = (TextView) mRootView.findViewById(R.id.weather_city);
            weatherExplain = (TextView) mRootView.findViewById(R.id.weather_explain);
            airIndex = (TextView) mRootView.findViewById(R.id.air_index);
            airExplain = (TextView) mRootView.findViewById(R.id.air_explain);
//            dateView = (TextView) mRootView.findViewById(R.id.clock_date);
//            dateView.setVisibility(View.GONE);
            updateViews();

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_DATE_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_TIME_TICK);
            getMContext().registerReceiver(receiver, filter);
            Log.d(TAG, "Kinflow2Weather: onEnabled...");

            setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initLocation() {
        try {
            Log.d(TAG, "initLocation");
            //初始化定位
            mLocationClient = new AMapLocationClient(getMContext());
//设置定位回调监听
            mLocationClient.setLocationListener(mLocationListener);

            if (mLocationOption == null) {
                mLocationOption = new AMapLocationClientOption();
            }
            //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
            mLocationOption.setOnceLocation(true);

//获取最近3s内精度最高的一次定位结果：
//设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);
            mLocationOption.setNeedAddress(true);
            mLocationOption.setWifiActiveScan(false);
            //设置是否允许模拟位置,默认为false，不允许模拟位置
            mLocationOption.setMockEnable(false);
//        mLocationOption.setProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getLocation() {
        if (mLocationClient != null) {
            Log.d(TAG, "getWeather Location");
            //启动定位
            mLocationClient.startLocation();
        }
    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

    }

    /**
     * 更新时间和请求位置,当获取到位置变更后会请求天气
     */
    private void updateViews() {
        try {
            boolean is12hour = DeviceState.get12HourMode(getMContext());
            Calendar c = Calendar.getInstance();
            int hour, minute;
            minute = c.get(Calendar.MINUTE);
            if (is12hour) {
                hour = c.get(Calendar.HOUR);
            } else {
                hour = c.get(Calendar.HOUR_OF_DAY);
            }
            if (isHasUpdateWeather) {
                if (DeviceInfoUtils.isWifiConnected(getMContext())) {
                    if (hour % 2 == 0 && minute == 0) {
                        getLocation();
                    }
                } else if (DeviceInfoUtils.isMobileDataConnected(getMContext())) {
                    if (hour % 4 == 0 && minute == 0) {
                        getLocation();
                    }
                }
            } else {
                if (DeviceInfoUtils.isWifiConnected(getMContext())) {
                    if (isFirstReq) {
                        getLocation();
                        isFirstReq = false;
                    } else {
                        if (minute % 10 == 0) {
                            getLocation();
                        }
                    }
                } else if (DeviceInfoUtils.isMobileDataConnected(getMContext())) {
                    if (isFirstReq) {
                        getLocation();
                        isFirstReq = false;
                    } else {
                        if (minute % 15 == 0) {
                            getLocation();
                        }
                    }
                }
            }

//            hour0.setImageResource(CLOCK_IMAGES[hour / 10]);
//            hour1.setImageResource(CLOCK_IMAGES[hour % 10]);
//            minute0.setImageResource(CLOCK_IMAGES[minute / 10]);
//            minute1.setImageResource(CLOCK_IMAGES[minute % 10]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void updateDate4English(boolean is12hour) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT);
        String dateString = df.format(new java.util.Date());
        int lastIndex = dateString.lastIndexOf(",");
        String dateValue = dateString.substring(0, lastIndex);
        if (is12hour) {
            dateValue += " " + dateString.substring(dateString.length() - 3);
        }
//        dateView.setText(dateValue);
    }

    ;

    private void updateDate4China(boolean is12hour) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT);
        String[] dateSplit = df.format(new java.util.Date()).split(" ");
        if (dateSplit != null && dateSplit.length == 2) {
            String dateValue = dateSplit[0].substring(5);
            if (is12hour) {
                dateValue += " " + dateSplit[1].substring(0, 2);
            }
//            dateView.setText(dateValue);
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
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            getMContext().unregisterReceiver(receiver);
            if (mLocationClient != null) {
                mLocationClient.stopLocation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    OkHttpClient client = new OkHttpClient();
    private static final String appEncryptKey = "online:hltq+(^g&%~vd+10003";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    String postGetWeather(String json) throws IOException {
        RequestBody jbody = RequestBody.create(JSON, json);
        Request requestLocate = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(makeWeatherLocateUrl())
                .addHeader("Content-Type", "application/json")
                .post(jbody)
                .build();
//        Log.d(TAG, "post : request :  " + requestLocate.toString());
        Response response = client.newCall(requestLocate).execute();
        if (response.isSuccessful()) {
            String result = DeviceInfoUtils.decodeUnicode(response.body().string());
//            Log.d(TAG, "post : requestLocate.body().string() :  " + result);
            try {
                JSONObject objLocate = new JSONObject(result);
                JSONObject cityObj = new JSONObject(objLocate.getString("city"));
                String id = cityObj.getString("id");
                String name = cityObj.getString("name");
                String from = cityObj.getString("from");
                Request requestInfo = new Request.Builder()
                        .url(makeWeatherInfoUrl(id, name))
                        .addHeader("Content-Type", "application/json")
                        .post(jbody)
                        .build();
                Response response1 = client.newCall(requestInfo).execute();
                if (response1.isSuccessful()) {
                    String resultInfo = DeviceInfoUtils.decodeUnicode(response1.body().string());
//                    Log.d(TAG, "post : requestInfo.body().string() :  " + resultInfo);
                    return resultInfo;

                } else {
                    Log.d(TAG, "post response1 failed");
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.d(TAG, "post response failed");
            return null;
        }

    }

    private String makeWeatherLocateUrl() {

        StringBuilder urlBuild = new StringBuilder(WEATHER_LOCATE_SERVER);
        urlBuild.append("?");
        urlBuild.append("act=");
        urlBuild.append("106");
        urlBuild.append("&");
        urlBuild.append("mt=");
        urlBuild.append("1");
        urlBuild.append("&");
        urlBuild.append("idfa=");
        urlBuild.append(DeviceInfoUtils.getDeviceId(getMContext()));
        urlBuild.append("&");
        urlBuild.append("imei=");
        urlBuild.append(DeviceInfoUtils.getIMEI(getMContext()));
        urlBuild.append("&");
        urlBuild.append("dm=");
        urlBuild.append(DeviceInfoUtils.getDM().replace(" ", ""));
        urlBuild.append("&");
        urlBuild.append("nt=");
        urlBuild.append(DeviceInfoUtils.getNT(getMContext()));
        urlBuild.append("&");
        urlBuild.append("pid=");
        urlBuild.append("10003");
        urlBuild.append("&");
        urlBuild.append("sv=");
        urlBuild.append(DeviceInfoUtils.getSoftwareVersion(getMContext()));
        urlBuild.append("&");
        urlBuild.append("osv=");
        urlBuild.append(DeviceInfoUtils.getOsVersion());
        urlBuild.append("&");
        urlBuild.append("marsLat=");
        urlBuild.append(aMapLocation.getLatitude());
        urlBuild.append("&");
        urlBuild.append("marsLng=");
        urlBuild.append(aMapLocation.getLongitude());
        urlBuild.append("&");
        long unixTime = System.currentTimeMillis() / 1000L;
        urlBuild.append("time=");
        urlBuild.append(unixTime);
        urlBuild.append("&");
        urlBuild.append("sign=");
        urlBuild.append(DeviceInfoUtils.Md5Encode(unixTime + appEncryptKey));
        Log.d(TAG, "post : makeWeatherUrl :  " + urlBuild.toString());
        return urlBuild.toString();
    }

    private String makeWeatherInfoUrl(String cityCode, String cityName) {

        StringBuilder urlBuild = new StringBuilder(WEATHER_INFO_SERVER);
        urlBuild.append("?");
        urlBuild.append("city=");
        urlBuild.append(cityCode);
        urlBuild.append("&");
        urlBuild.append("cityName=");
        urlBuild.append(cityName);
        urlBuild.append("&");
        urlBuild.append("act=");
        urlBuild.append("106");
        urlBuild.append("&");
        urlBuild.append("mt=");
        urlBuild.append("1");
        urlBuild.append("&");
        urlBuild.append("idfa=");
        urlBuild.append(DeviceInfoUtils.getDeviceId(getMContext()));
        urlBuild.append("&");
        urlBuild.append("imei=");
        urlBuild.append(DeviceInfoUtils.getIMEI(getMContext()));
        urlBuild.append("&");
        urlBuild.append("dm=");
        urlBuild.append(DeviceInfoUtils.getDM().replace(" ", ""));
        urlBuild.append("&");
        urlBuild.append("nt=");
        urlBuild.append(DeviceInfoUtils.getNT(getMContext()));
        urlBuild.append("&");
        urlBuild.append("pid=");
        urlBuild.append("10003");
        urlBuild.append("&");
        urlBuild.append("sv=");
        urlBuild.append(DeviceInfoUtils.getSoftwareVersion(getMContext()));
        urlBuild.append("&");
        urlBuild.append("osv=");
        urlBuild.append(DeviceInfoUtils.getOsVersion());
        urlBuild.append("&");
        urlBuild.append("marsLat=");
        urlBuild.append(aMapLocation.getLatitude());
        urlBuild.append("&");
        urlBuild.append("marsLng=");
        urlBuild.append(aMapLocation.getLongitude());
        urlBuild.append("&");
        long unixTime = System.currentTimeMillis() / 1000L;
        urlBuild.append("time=");
        urlBuild.append(unixTime);
        urlBuild.append("&");
        urlBuild.append("sign=");
        urlBuild.append(DeviceInfoUtils.Md5Encode(unixTime + appEncryptKey));
        Log.d(TAG, "post : makeWeatherUrl :  " + urlBuild.toString());
        return urlBuild.toString();
    }

}
