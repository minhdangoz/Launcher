package com.klauncher.ext;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.klauncher.utilities.DeviceInfoUtils;
import com.klauncher.utilities.WeakAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yanni on 16/3/11.
 */
public class ClockWidgetService extends Service {

    private final static String TAG = "ClockWidgetService";

    public static final String ACTION_CLOCK_UPDATE = "com.klauncher.action.ACTION_CLOCK_UPDATE";

    private static final String SP_UPDATE_WEATHER_TIME = "update_weather_time";

    private static final String SP_UPDATE_WEATHER_INFO = "update_weather_info";

    private String mLocationJson;

    AMapLocationClientOption mLocationOption = null;
    AMapLocation aMapLocation;

    private UpdateClockWeatherListener listener;

    private SharedPreferences sharedPreferences;

    public void setUpdateClockWeatherListener(UpdateClockWeatherListener l)
    {
        this.listener=l;
    }

    public void checkLoadWeather(){
//        long curtime = System.currentTimeMillis();
//        long duration = curtime - sharedPreferences.getLong(SP_UPDATE_WEATHER_TIME, curtime);
//        if (duration < 6 * 60 * 60 * 1000) {
//            String weatherinfo = sharedPreferences.getString(SP_UPDATE_WEATHER_INFO, "");
//            if (!TextUtils.isEmpty(weatherinfo)) {
//                if (listener != null) {
//                    listener.updateWeather(weatherinfo);
//                }
//            } else {
//                getLocation();
//            }
//        } else {
            getLocation();
//        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public ClockWidgetService getService(){
            return ClockWidgetService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        initLocation();
        myHandler = new MyHandler(this);
        sharedPreferences = getApplicationContext().getSharedPreferences("weather_sp", Context.MODE_PRIVATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_TIME_TICK);
//        filter.addAction(ClockWidgetService.ACTION_CLOCK_UPDATE);
        registerReceiver(receiver, filter);
        Log.d(TAG, "ClockWidgetProvider: onEnabled...");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
    }

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
                    if (ClockWidgetService.this.aMapLocation != null) {
                        double moveDistance = gps2m(ClockWidgetService.this.aMapLocation.getLatitude(), ClockWidgetService.this.aMapLocation.getLongitude(), aMapLocation.getLatitude(), aMapLocation.getLongitude());
                        if(moveDistance < 20*1000){
                            long curtime = System.currentTimeMillis();
                            long duration = curtime - sharedPreferences.getLong(SP_UPDATE_WEATHER_TIME, curtime);
                            if (duration < 6 * 60 * 60 * 1000) {
                                String weatherinfo = sharedPreferences.getString(SP_UPDATE_WEATHER_INFO, "");
                                if (!TextUtils.isEmpty(weatherinfo)) {
                                    if (listener != null) {
                                        listener.updateWeather(weatherinfo);
                                    }
                                    myHandler.sendEmptyMessageDelayed(1, 30 * 60 * 1000);
                                    return;
                                }
                            }
                        }
                    }
                    ClockWidgetService.this.aMapLocation = aMapLocation;
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
                            +"\",\"City\":\""+aMapLocation.getCity()+"\"},\"ip\"：\""+ DeviceInfoUtils.getLocalIpAddress(getApplicationContext())+"\"}";

//                    myHandler.post(sRunnable);
//                    Log.d(TAG," mLocationJson : " + mLocationJson);
                    myHandler.sendEmptyMessage(0);
                    myHandler.sendEmptyMessageDelayed(1, 30 * 60 * 1000);
                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e(TAG,"AmapError location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                    if (!myHandler.hasMessages(1)) {
                        myHandler.sendEmptyMessageDelayed(1, 10 * 60 * 1000);
                    }
                }
            }
        }
    };

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
                if (listener != null) {
                    listener.updatetClock();
                }
            }
        }
    };


    MyHandler myHandler;
    private static class MyHandler extends Handler {
        WeakReference mClockWidgetService;
        MyHandler(ClockWidgetService cwv) {
            mClockWidgetService = new WeakReference(cwv);
        }

        @Override
        public void handleMessage(Message msg) {
            ClockWidgetService cwv = (ClockWidgetService) mClockWidgetService.get();
            if (cwv !=null) {
                switch (msg.what) {
                    case 0:
                        new MyTask(cwv).execute();
                        break;
                    case 1:
                        cwv.getLocation();
                        break;
                    default:
                        break;
                }
            }
        }
    }

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


    private void initLocation(){
        Log.d(TAG,"initLocation");
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
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


    private static class MyTask extends WeakAsyncTask<Void, Void, String, ClockWidgetService> {
        public MyTask(ClockWidgetService target) {
            super(target);
        }

        @Override
        protected String doInBackground(ClockWidgetService target, Void... params) { // 获取context,
            // 执行一些操作
            try {
                return target.postGetWeather(target.mLocationJson);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ClockWidgetService target, String resultInfo) {
            Log.d(TAG,"onPostExecute : " + resultInfo);
            if (TextUtils.isEmpty(resultInfo)) {
                if (!target.myHandler.hasMessages(1)) {
                    target.myHandler.sendEmptyMessageDelayed(1, 15 * 60 * 1000);
                }
                return;
            }
            if (target.listener != null) {
                target.listener.updateWeather(resultInfo);
            }
            target.saveUpdateWeatherTime(resultInfo);
        }
    }

    private void saveUpdateWeatherTime(String weatherInfo){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SP_UPDATE_WEATHER_TIME, System.currentTimeMillis());
        editor.putString(SP_UPDATE_WEATHER_INFO, weatherInfo);
        editor.apply();
        myHandler.sendEmptyMessageDelayed(1, 6 * 60 * 60 * 1000);
    }

    OkHttpClient client = new OkHttpClient();
    private static final String appEncryptKey= "online:hltq+(^g&%~vd+10003";
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
        urlBuild.append(DeviceInfoUtils.getDeviceId(getApplicationContext()));
        urlBuild.append("&");
        urlBuild.append("imei=");
        urlBuild.append(DeviceInfoUtils.getIMEI(getApplicationContext()));
        urlBuild.append("&");
        urlBuild.append("dm=");
        urlBuild.append(DeviceInfoUtils.getDM().replace(" ",""));
        urlBuild.append("&");
        urlBuild.append("nt=");
        urlBuild.append(DeviceInfoUtils.getNT(getApplicationContext()));
        urlBuild.append("&");
        urlBuild.append("pid=");
        urlBuild.append("10003");
        urlBuild.append("&");
        urlBuild.append("sv=");
        urlBuild.append(DeviceInfoUtils.getSoftwareVersion(getApplicationContext()));
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
        urlBuild.append(DeviceInfoUtils.getDeviceId(getApplicationContext()));
        urlBuild.append("&");
        urlBuild.append("imei=");
        urlBuild.append(DeviceInfoUtils.getIMEI(getApplicationContext()));
        urlBuild.append("&");
        urlBuild.append("dm=");
        urlBuild.append(DeviceInfoUtils.getDM().replace(" ",""));
        urlBuild.append("&");
        urlBuild.append("nt=");
        urlBuild.append(DeviceInfoUtils.getNT(getApplicationContext()));
        urlBuild.append("&");
        urlBuild.append("pid=");
        urlBuild.append("10003");
        urlBuild.append("&");
        urlBuild.append("sv=");
        urlBuild.append(DeviceInfoUtils.getSoftwareVersion(getApplicationContext()));
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
        Log.d(TAG,"post : makeWeatherUrl :  " + urlBuild.toString());
        return urlBuild.toString();
    }

    public interface UpdateClockWeatherListener{
        public void updatetClock();
        public void updateWeather(String weatherInfo);
    }

}
