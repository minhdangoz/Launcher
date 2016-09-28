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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * Created by hw on 16-9-26.
 */
public class DeviceInfoUtils {

    private static final String TAG = "DeviceInfoUtils";

    public static String getDeviceId(Context ctx) {
        String deviceId = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d(TAG,"getDeviceId : " + deviceId);
        return deviceId;
    }

    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        Log.d(TAG,"getIMEI : " + imei);
        return imei == null ? "" : imei;
    }

    public static String getSoftwareVersion(Context ctx) {

        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG,"getSoftwareVersion : " + tm.getDeviceSoftwareVersion());
        return tm.getDeviceSoftwareVersion() == null ? "":tm.getDeviceSoftwareVersion();
    }

    public static String getOsVersion() {
        Log.d(TAG,"getOsVersion : " + Build.VERSION.RELEASE);
        return Build.VERSION.RELEASE;
    }

    public static String getDV() {

        Log.d(TAG,"getDV : " + Build.MANUFACTURER);
        return Build.MANUFACTURER;
    }

    public static String getDM() {

        Log.d(TAG,"getDM : " + Build.MODEL);
        return Build.MODEL;
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

    public static String getSCreenWidth(Context ctx) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        Log.d(TAG,"getSCreenWidth : " +String.valueOf(width));
        return String.valueOf(width);
    }

    public static String getScreenHeight(Context ctx) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        Log.d(TAG,"getScreenHeight : " +String.valueOf(height));
        return String.valueOf(height);
    }

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

    public static String getLocalIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            Log.e(TAG,"getLocalIpAddress error");
        }
        return "";
    }

    public static boolean isWifiConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetInfo.isConnected();
    }

    public static boolean isMobileDataConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobNetInfo.isConnected();
    }

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
