package com.klauncher.biddingos.commons.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.klauncher.biddingos.commons.Setting;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;


public class AppUtils {
    private static final String TAG = "AppUtils";

    /**
     * 获取Activity堆栈顶层的名字
     * @return
     */
    public static String getTopActivity() {

        boolean IS_LOL_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        if(IS_LOL_OR_LATER) {
        }else if(!checkPermission("android.permission.GET_TASKS")) {
            return null;
        }
        ActivityManager mAm = (ActivityManager) Setting.context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(1);
        String topStr = "";
        if(taskList != null&&taskList.size()>0) {
            topStr = taskList.get(0).topActivity.toString();
        }
        return topStr;
    }

    /**
     * 获取设备所有已安装应用的包名
     * @return
     */
    public static List<String> getAllPackage() {
        List<String> list=new ArrayList<>();
        try {
            List<PackageInfo> packs = Setting.context.getPackageManager().getInstalledPackages(0);
            for(int i=0; i<packs.size(); i++) {
                PackageInfo p = packs.get(i);
                list.add(p.packageName);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 通过包名获取应用信息
     * @return null 表示找不到
     */
    public static PackageInfo getPackageInfoByPackageName(String packageName) {
        try {
            PackageInfo packageInfo = Setting.context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo;

        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    /**
     * 获取设备权限列表
     */
    public static List<String> getDevicePermissionList() {
        String[] permissionStrings = null;
        try {
            PackageManager pm = Setting.context.getPackageManager();
            PackageInfo pack = pm.getPackageInfo(Setting.context.getPackageName(), PackageManager.GET_PERMISSIONS);
            permissionStrings = pack.requestedPermissions;

        } catch (PackageManager.NameNotFoundException e) {
        }

        List<String> lstPermission = new ArrayList<>();
        if(permissionStrings!=null) {
            for(int i=0; i<permissionStrings.length; i++) {
                lstPermission.add(permissionStrings[i]);
            }
        }

        return lstPermission;
    }

    /**
     * 判断设备是否具有某权限
     */
    public static boolean checkPermission(String permName) {
        PackageManager pm = Setting.context.getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission(permName, Setting.context.getPackageName());
    }


    /**
     * 获取用户唯一标识
     */
    public static JSONArray getUserUniqueID() {

        String android_id="",device_id="",wifi_mac="";
        String device_serial = "";
        String simSerialNumber = "";
        String imsi = "";

        try {
            //GET ANDROID_ID
            android_id = Settings.Secure.getString(Setting.context.getContentResolver(), Settings.Secure.ANDROID_ID);

            //GET DEVICE_ID
            if (checkPermission("android.permission.READ_PHONE_STATE")) {
                TelephonyManager tm = (TelephonyManager) Setting.context.getSystemService(Context.TELEPHONY_SERVICE);
                device_id = tm.getDeviceId();
                simSerialNumber = tm.getSimSerialNumber();
                imsi = tm.getSubscriberId();
            }

            //GET WIFI MAC
            if (checkPermission("android.permission.ACCESS_WIFI_STATE")) {
                WifiManager wifiManager = (WifiManager) Setting.context.getSystemService(Context.WIFI_SERVICE);
                wifi_mac = wifiManager.getConnectionInfo().getMacAddress();
            }

            //device_serial
            device_serial = Build.SERIAL;


        }catch (Exception e) {
        }

        JSONArray jsonArray = new JSONArray();
        if(!TextUtils.isEmpty(android_id))    jsonArray.put("device.adid."+android_id);
        if(!TextUtils.isEmpty(device_id))     jsonArray.put("phone.deviceid."+device_id);
        if(!TextUtils.isEmpty(wifi_mac))      jsonArray.put("device.mac."+wifi_mac);
        if(!TextUtils.isEmpty(device_serial)) jsonArray.put("device.serial."+device_serial);
        if(!TextUtils.isEmpty(simSerialNumber)) jsonArray.put("sim.serial."+simSerialNumber);
        if(!TextUtils.isEmpty(imsi)) jsonArray.put("sim.imsi."+imsi);
        return  jsonArray;

    }


    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkDebuggable() {
        try {
            ApplicationInfo info = Setting.context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断设备是否启用WIFI
     */
    public static boolean checkWifiOpened() {
        if (checkPermission("android.permission.ACCESS_WIFI_STATE")) {
            WifiManager wifiManager = (WifiManager) Setting.context.getSystemService(Context.WIFI_SERVICE);
            return wifiManager.isWifiEnabled();
        }
        return false;
    }

    /**
     * 获取网络状态，wifi,MOBILE
     *
     * @return int -1 获取失败 ; 0 MOBILE; 1 WIFI
     */
    public static int getNetWorkType() {

        int nType = -1;
        ConnectivityManager manager = (ConnectivityManager) Setting.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (null!=networkInfo && networkInfo.isConnected()) {
            nType = networkInfo.getType();
        }

        return nType;
    }

    /**
     * 判断设备是否启用WIFI
     */
    public static String getNetProvider() {
        String imsi = "";
        try {
            //GET IMSI
            if (checkPermission("android.permission.READ_PHONE_STATE")) {
                TelephonyManager tm = (TelephonyManager) Setting.context.getSystemService(Context.TELEPHONY_SERVICE);
                imsi = tm.getSubscriberId();
            }
        }catch (Exception e) {
        }
        if(null!=imsi && imsi.length()>=5) {
            return imsi.substring(0, 5);
        }

        return "";
    }

    /**
     * 通过获取包信息判断文件是否可以安装
     * @param apkPath
     * @return
     */
    public static boolean isApkCanInstall(Context context,String apkPath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


}
