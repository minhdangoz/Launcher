package com.klauncher.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by hw on 16-9-26.
 */
public class DeviceInfoUtils {

    private static final String TAG = "DeviceInfoUtils";

/* 手机基础信息 start*/
    //获取device_id
    public static String getDeviceId(Context ctx) {
        String deviceId = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d(TAG,"getDeviceId : " + deviceId);
        return deviceId;
    }

    //获取device_imei
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        Log.d(TAG,"getIMEI : " + imei);
        return imei == null ? "" : imei;
    }

    //获取系统软件版本号
    public static String getSoftwareVersion(Context ctx) {

        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG,"getSoftwareVersion : " + tm.getDeviceSoftwareVersion());
        return tm.getDeviceSoftwareVersion() == null ? "":tm.getDeviceSoftwareVersion();
    }

    //获取系统版本号
    public static String getOsVersion() {
        Log.d(TAG,"getOsVersion : " + Build.VERSION.RELEASE);
        return Build.VERSION.RELEASE;
    }

    //获取 MANUFACTURER 生产厂家
    public static String getDV() {
        Log.d(TAG,"getDV : " + Build.MANUFACTURER);
        return Build.MANUFACTURER;
    }

    //获取 MODEL 手机型号
    public static String getDM() {
        Log.d(TAG,"getDM : " + Build.MODEL);
        return Build.MODEL;
    }

    //获取手机SDK版本号
    public static int getAndroidSDKVersion() {
        Log.d(TAG,"getDM : " + Build.VERSION.SDK_INT);
        return Build.VERSION.SDK_INT;
    }


    public static String getOPID(Context ctx) {
        String imsi = getIMEI(ctx);
        if (imsi != null && imsi.length() > 6) {
            Log.d(TAG,"getOPID : " + imsi.substring(0, 5));
            return imsi.substring(0, 5);
        }
        Log.d(TAG,"getOPID : " + "");
        return "";
    }

    //获取屏幕宽度
    public static String getSCreenWidth(Context ctx) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        Log.d(TAG,"getSCreenWidth : " +String.valueOf(width));
        return String.valueOf(width);
    }

    //获取屏幕高度
    public static String getScreenHeight(Context ctx) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        Log.d(TAG,"getScreenHeight : " +String.valueOf(height));
        return String.valueOf(height);
    }

    /**
     * 获取设备信息,可根据需要自行截取要使用的
     *
     * @return
     */
    private String getDeviceInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("主板：" + Build.BOARD);
        sb.append(
                "系统启动程序版本号：" + Build.BOOTLOADER);
        sb.append(
                "系统定制商：" + Build.BRAND);
        sb.append(
                "cpu指令集：" + Build.CPU_ABI);
        sb.append(
                "cpu指令集2 " + Build.CPU_ABI2);
        sb.append(
                "设置参数：" + Build.DEVICE);
        sb.append(
                "显示屏参数：" + Build.DISPLAY);
        sb.append("无线电固件版本：" + Build.getRadioVersion());
        sb.append(
                "硬件识别码：" + Build.FINGERPRINT);
        sb.append(
                "硬件名称：" + Build.HARDWARE);
        sb.append(
                "HOST: " + Build.HOST);
        sb.append(
                "修订版本列表：" + Build.ID);
        sb.append(
                "硬件制造商：" + Build.MANUFACTURER);
        sb.append(
                "版本：" + Build.MODEL);
        sb.append(
                "硬件序列号： " + Build.SERIAL);
        sb.append(
                "手机制造商：" + Build.PRODUCT);
        sb.append(
                "描述Build的标签：" + Build.TAGS);
        sb.append(
                "时间:" + Build.TIME);
        sb.append(
                "builder类型：" + Build.TYPE);
        sb.append(
                "USER: " + Build.USER);
        return sb.toString();
    }
/* 手机基础信息 end*/

/* 手机网络信息 start*/
    //获取网络连接类型
    public static String getNT(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                Log.d(TAG,"getNT 1: " + "2");
                return "2";
            } else if (type.equalsIgnoreCase("MOBILE")) {
                String proxyHost = android.net.Proxy.getDefaultHost();
                int nt = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? 5 : 4) : 4;
                Log.d(TAG,"getNT 2: " + String.valueOf(nt));
                return String.valueOf(nt);
            }
            Log.d(TAG,"getNT 3: " + "0");
            return "0";
        } else {
            Log.d(TAG,"getNT 4: " + "0");
            return "0";
        }
    }

    //获取网络移动数据连接类型
    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true; // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true; // ~ 10+ Mbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }

    //获取MAC地址
    public static String getMac(Context ctx){
        WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();
        Log.d(TAG,"getMac : " +info.getMacAddress());
        return info.getMacAddress();
    }

    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {
        try {
            // for (Enumeration<NetworkInterface> en = NetworkInterface
            // .getNetworkInterfaces(); en.hasMoreElements();) {
            // NetworkInterface intf = en.nextElement();
            // for (Enumeration<InetAddress> enumIpAddr = intf
            // .getInetAddresses(); enumIpAddr.hasMoreElements();) {
            // InetAddress inetAddress = enumIpAddr.nextElement();
            // if (!inetAddress.isLoopbackAddress()) {
            // return inetAddress.getHostAddress().toString();
            // }
            // }
            // }
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return "";
        }
        // return null;
    }

    //WIFI是否连接
    public static boolean isWifiConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetInfo.isConnected();
    }

    //移动网络是否连接
    public static boolean isMobileDataConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobNetInfo.isConnected();
    }

    //获取网络附加信息
    public static String getNetWorkExtraInfo(Context context){
        ConnectivityManager connectionManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取网络的状态信息，有下面三种方式
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        if (!TextUtils.isEmpty(networkInfo.getExtraInfo())) {
            return networkInfo.getExtraInfo();
        }

        return "";
    }

    //获取连接状态
    public static NetworkInfo.State getNetWorStates(Context context){
        ConnectivityManager connectionManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取网络的状态信息，有下面三种方式
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        return networkInfo.getState();

    }

    //Return a human-readable name describing the subtype of the network.
    public static String getNetWorkSubtypeName(Context context){
        ConnectivityManager connectionManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取网络的状态信息，有下面三种方式
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        if (!TextUtils.isEmpty(networkInfo.getSubtypeName())) {
            return networkInfo.getSubtypeName();
        }

        return "";
    }

    //获取详细状态。
    public static NetworkInfo.DetailedState getNetWorkDetailedState(Context context){
        ConnectivityManager connectionManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取网络的状态信息，有下面三种方式
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

        return networkInfo.getDetailedState();
    }

    //获取BSSID属性 也就是路由器的mac
    public static String getNetWorkBSSID(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (!TextUtils.isEmpty(wifiInfo.getBSSID())) {
            return wifiInfo.getBSSID();
        }

        return "";
    }

    //获取SSID 也就是wifi名称
    public static String getNetWorkSSID(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (!TextUtils.isEmpty(wifiInfo.getSSID())) {
            return wifiInfo.getSSID();
        }

        return "";
    }
/* 手机网络信息 end*/

/* 手机电话信息 start*/

    /**
     * 获取手机号 取出MSISDN，很可能为空
     * @return
     */
    public static String getPhoneNumber(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

    /**
     * ICCID:ICC identity集成电路卡标识，这个是唯一标识一张卡片物理号码的
     * @return
     */
    public static String getIccid(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (isSimReady(ctx)) {
            return tm.getSimSerialNumber();
        }
        return "";
    }

    /**
     * IMSI 全称为 International Mobile Subscriber Identity，中文翻译为国际移动用户识别码。
     * 它是在公众陆地移动电话网（PLMN）中用于唯一识别移动用户的一个号码。在GSM网络，这个号码通常被存放在SIM卡中
     * @return
     */
    public static String getSubscriberId(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (isSimReady(ctx)) {
            return tm.getSubscriberId();
        }
        return "";
    }

    //获取MCC 移动国家号码，由3位数字组成
    public static String getMCC(Context ctx){
        String imsi = getSubscriberId(ctx);
        if (imsi!=null && !imsi.equals("")) {
            return imsi.substring(0,2);
        }
        return "";
    }

    //获取MCC 移动国家号码，由3位数字组成
    public static String getMSN(Context ctx){
        String imsi = getSubscriberId(ctx);
        if (imsi!=null && !imsi.equals("")) {
            return imsi.substring(3,4);
        }
        return "";
    }

    /**
     * 判断SIM卡是否准备好
     *
     * @param context
     * @return
     */
    public static boolean isSimReady(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            int simState = tm.getSimState();
            if (simState == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
        } catch (Exception e) {
            Log.w("PhoneHelper", "021:" + e.toString());
        }
        return false;
    }

    /**
     * 返回ISO标准的国家码，即国际长途区号
     * @return
     */
    public static String getNetworkCountrylso(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getNetworkCountryIso())) {
            return tm.getNetworkCountryIso();
        }
        return "";
    }

    /**
     * 返回MCC+MNC代码 (网络运营商国家代码和运营商网络代码)
     * @return
     */
    public static String getNetworkOperator(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getNetworkOperator())) {
            return tm.getNetworkOperator();
        }
        return "";
    }

    /**
     * 返回移动网络运营商的名字(SPN)
     * @return
     */
    public static String getNetworkOperatorName(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getNetworkOperatorName())) {
            return tm.getNetworkOperatorName();
        }
        return "";
    }

    /**
     * 返回SIM卡提供商的国家代码
     * @return
     */
    public static String getSimCountryIso(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getSimCountryIso())) {
            return tm.getSimCountryIso();
        }
        return "";
    }

    /**
     * 返回MCC+MNC代码 (SIM卡运营商国家代码和运营商网络代码)(IMSI)
     * @return
     */
    public static String getSimOperator(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getSimOperator())) {
            return tm.getSimOperator();
        }
        return "";
    }

    /**
     * 返回SIM卡网络运营商的名字(SPN)
     * @return
     */
    public static String getSimOperatorName(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getSimOperatorName())) {
            return tm.getSimOperatorName();
        }
        return "";
    }

    /**
     * 返回SIM卡的序列号(IMEI)
     * @return
     */
    public static String getSimSerialNumber(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getSimSerialNumber())) {
            return tm.getSimSerialNumber();
        }
        return "";
    }

    /**
     * 返回SIM卡的序列号(IMEI)
     * @return
     */
    public static String getVoiceMailNumber(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getVoiceMailNumber())) {
            return tm.getVoiceMailNumber();
        }
        return "";
    }

    /**
     * 返回语音邮件号码
     * @return
     */
    public static String getVoiceMailAlphaTag(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getVoiceMailAlphaTag())) {
            return tm.getVoiceMailAlphaTag();
        }
        return "";
    }

    /**
     * 返回移动终端的软件版本，例如：GSM手机的IMEI/SV码。
     * @return
     */
    public static String getDeviceSoftwareVersion(Context ctx){
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (!TextUtils.isEmpty(tm.getDeviceSoftwareVersion())) {
            return tm.getDeviceSoftwareVersion();
        }
        return "";
    }

/* 手机电话信息 end*/

    //Unicode转码
    public static String decodeUnicode(String theString) {

        char aChar;

        int len = theString.length();

        StringBuffer outBuffer = new StringBuffer(len);

        for (int x = 0; x < len;) {

            aChar = theString.charAt(x++);

            if (aChar == '\\') {

                aChar = theString.charAt(x++);

                if (aChar == 'u') {

                    // Read the xxxx

                    int value = 0;

                    for (int i = 0; i < 4; i++) {

                        aChar = theString.charAt(x++);

                        switch (aChar) {

                            case '0':

                            case '1':

                            case '2':

                            case '3':

                            case '4':

                            case '5':

                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';

                    else if (aChar == 'n')

                        aChar = '\n';

                    else if (aChar == 'f')

                        aChar = '\f';

                    outBuffer.append(aChar);

                }

            } else

                outBuffer.append(aChar);

        }

        return outBuffer.toString();

    }

    //MD5加密
    public static String Md5Encode(String string)
    {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F' };
        MessageDigest sha;
        try
        {
            sha = MessageDigest.getInstance("MD5");
            sha.reset();
            sha.update(string.getBytes());

            byte[] md = sha.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++)
            {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            String code = new String(str).toLowerCase();
            Log.d(TAG,"Md5Encode : " + code);
            return code;
        }
        catch (NoSuchAlgorithmException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }


}
