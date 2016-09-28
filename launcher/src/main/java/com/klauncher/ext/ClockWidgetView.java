package com.klauncher.ext;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.klauncher.launcher.R;
import com.klauncher.utilities.DeviceInfoUtils;
import com.klauncher.utilities.WeakAsyncTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
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
public class ClockWidgetView extends FrameLayout {

    private final static String TAG = "ClockWidgetView";

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
                    ClockWidgetView.this.aMapLocation = aMapLocation;
//可在其中解析amapLocation获取相应内容。
                    Log.d(TAG,
                    aMapLocation.getLocationType()+" "+//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    aMapLocation.getLatitude()+" "+//获取纬度
                    aMapLocation.getLongitude()+" "+//获取经度
                    aMapLocation.getAccuracy()+" "+//获取精度信息
                    aMapLocation.getAddress()+" "+//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    aMapLocation.getCountry()+" "+//国家信息
                    aMapLocation.getProvince()+" "+//省信息
                    aMapLocation.getCity()+" "+//城市信息
                    aMapLocation.getDistrict()+" "+//城区信息
                    aMapLocation.getStreet()+" "+//街道信息
                    aMapLocation.getStreetNum()+" "+//街道门牌号信息
                    aMapLocation.getCityCode()+" "+//城市编码
                    aMapLocation.getAdCode()+" "+//地区编码
                    aMapLocation.getAoiName()+" "+//获取当前定位点的AOI信息
                    aMapLocation.getTime());
//获取定位时间
//                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date date = new Date(aMapLocation.getTime());
//                    df.format(date);


                    mLocationJson = "{\"address\":{\"SubLocality\":\""
                            +aMapLocation.getDistrict()+"\",\"CountryCode\":\""+aMapLocation.getCountry()
                            +"\",\"Name\":\""+aMapLocation.getAddress()+"\",\"State\":\""
                            +aMapLocation.getProvince()+"\",\"FormattedAddressLines\":[\""+aMapLocation.getAddress()
                            +"\"],\"Country\":\""+aMapLocation.getCountry()
                            +"\",\"City\":\""+aMapLocation.getCity()+"\"},\"ip\"：\""+DeviceInfoUtils.getLocalIpAddress(mContext)+"\"}";

//                    myHandler.post(sRunnable);
//                    Log.d(TAG," mLocationJson : " + mLocationJson);
                    myHandler.sendEmptyMessage(0);

                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e(TAG,"AmapError location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    MyHandler myHandler;
    private static class MyHandler extends Handler {
        WeakReference mClockWidget;
        MyHandler(ClockWidgetView cwv) {
            mClockWidget = new WeakReference(cwv);
        }

        @Override
        public void handleMessage(Message msg) {
            ClockWidgetView cwv = (ClockWidgetView) mClockWidget.get();
            if (cwv !=null) {
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

    private static class MyTask extends WeakAsyncTask<Void, Void, String, ClockWidgetView> {
        public MyTask(ClockWidgetView target) {
            super(target);
        }

        @Override
        protected String doInBackground(ClockWidgetView target, Void... params) { // 获取context,
            // 执行一些操作
            try {
                return target.postGetWeather(target.mLocationJson);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ClockWidgetView target, String resultInfo) {
            Log.d(TAG,"onPostExecute : " + resultInfo);
            try {
                JSONObject obj = new JSONObject(resultInfo);
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
                Picasso.with(target.mContext)
                        .load(icon).fit().into(target.weatherIcon);
                target.weatherTmp.setText(climate + "  " + nowTemp+"℃");
                target.weatherCity.setText(cityName);
                target.isHasUpdateWeather = true;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG,"onPostExecute JSONException ");
            }
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
        initLocation();
        myHandler = new MyHandler(this);
        hour0 = (ImageView) mRootView.findViewById(R.id.img_clock_hour_0);
        hour1 = (ImageView) mRootView.findViewById(R.id.img_clock_hour_1);
        minute0 = (ImageView) mRootView.findViewById(R.id.img_clock_minute_0);
        minute1 = (ImageView) mRootView.findViewById(R.id.img_clock_minute_1);
        weatherIcon = (ImageView) mRootView.findViewById(R.id.weather_icon);
        weatherTmp = (TextView) mRootView.findViewById(R.id.weather_tmp);
        weatherCity = (TextView) mRootView.findViewById(R.id.weather_city);
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

    private void initLocation(){
        Log.d(TAG,"initLocation");
        //初始化定位
        mLocationClient = new AMapLocationClient(mContext);
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

    }

    private void getLocation(){
        if (mLocationClient != null) {
            Log.d(TAG,"getWeather Location");
            //启动定位
            mLocationClient.startLocation();
        }
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
        minute = c.get(Calendar.MINUTE);
        if (is12hour) {
            hour = c.get(Calendar.HOUR);
        } else {
            hour = c.get(Calendar.HOUR_OF_DAY);
        }
        if (isHasUpdateWeather) {
            if (DeviceInfoUtils.isWifiConnected(mContext)) {
                if (hour % 2 == 0 && minute == 0) {
                    getLocation();
                }
            } else if (DeviceInfoUtils.isMobileDataConnected(mContext)) {
                if (hour % 4 == 0 && minute == 0) {
                    getLocation();
                }
            }
        } else {
            if (DeviceInfoUtils.isWifiConnected(mContext)) {
                if (isFirstReq) {
                    getLocation();
                    isFirstReq = false;
                } else {
                    if (minute % 10 == 0) {
                        getLocation();
                    }
                }
            } else if (DeviceInfoUtils.isMobileDataConnected(mContext)) {
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
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(receiver);
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }

//        Intent service = new Intent(mContext, ClockWidgetService.class);
//        mContext.stopService(service);
    }

    OkHttpClient client = new OkHttpClient();
    private static final String appEncryptKey= "(^g&vd+10001";
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
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.d(TAG, "post response failed");
            return null;
        }

    }

    private String makeWeatherLocateUrl(){

        StringBuilder urlBuild = new StringBuilder(WEATHER_LOCATE_SERVER);
        urlBuild.append("?");
        urlBuild.append("act=");
        urlBuild.append("106");
        urlBuild.append("&");
        urlBuild.append("mt=");
        urlBuild.append("1");
        urlBuild.append("&");
        urlBuild.append("idfa=");
        urlBuild.append(DeviceInfoUtils.getDeviceId(mContext));
        urlBuild.append("&");
        urlBuild.append("dm=");
        urlBuild.append(DeviceInfoUtils.getDM().replace(" ",""));
        urlBuild.append("&");
        urlBuild.append("nt=");
        urlBuild.append(DeviceInfoUtils.getNT(mContext));
        urlBuild.append("&");
        urlBuild.append("pid=");
        urlBuild.append("10001");
        urlBuild.append("&");
        urlBuild.append("sv=");
        urlBuild.append(DeviceInfoUtils.getSoftwareVersion(mContext));
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
        urlBuild.append("time=");
        urlBuild.append(SystemClock.currentThreadTimeMillis());
        urlBuild.append("&");
        urlBuild.append("sign=");
        urlBuild.append(DeviceInfoUtils.Md5Encode(SystemClock.currentThreadTimeMillis() + appEncryptKey));
        Log.d(TAG,"post : makeWeatherUrl :  " + urlBuild.toString());
        return urlBuild.toString();
    }

    private String makeWeatherInfoUrl(String cityCode, String cityName){

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
        urlBuild.append(DeviceInfoUtils.getDeviceId(mContext));
        urlBuild.append("&");
        urlBuild.append("dm=");
        urlBuild.append(DeviceInfoUtils.getDM().replace(" ",""));
        urlBuild.append("&");
        urlBuild.append("nt=");
        urlBuild.append(DeviceInfoUtils.getNT(mContext));
        urlBuild.append("&");
        urlBuild.append("pid=");
        urlBuild.append("10001");
        urlBuild.append("&");
        urlBuild.append("sv=");
        urlBuild.append(DeviceInfoUtils.getSoftwareVersion(mContext));
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
        urlBuild.append("time=");
        urlBuild.append(SystemClock.currentThreadTimeMillis());
        urlBuild.append("&");
        urlBuild.append("sign=");
        urlBuild.append(DeviceInfoUtils.Md5Encode(SystemClock.currentThreadTimeMillis() + appEncryptKey));
        Log.d(TAG,"post : makeWeatherUrl :  " + urlBuild.toString());
        return urlBuild.toString();
    }

}
