package com.klauncher.ping;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Created by yanni on 16/3/27.
 */
public class PingManager {

    public static final String KEY_PING_TYPE = "pty";
    private static final int MAX_BUFFER_SIZE = 20;
    private static final String SP_PING = "ping";
    private static final String KEY_PING_APPLIST_LAST_REPORT = "lst_rpt_app";
    private static final int MIN_REPORT_INTERVAL = 12 * 3600 * 1000; //12 hours

    private static PingManager sPingManager = new PingManager();
    public static PingManager getInstance() {
        return sPingManager;
    }

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private PingHandler.PingFailCallback mPingFailCallback = new PingHandler.PingFailCallback() {
        @Override
        public void onFailed(final List<Map<String, String>> dataMap) {
            synchronized (mPingDataList) {
                mPingDataList.addAll(dataMap);
            }
        }
    };

    private List<Map<String, String>> mPingDataList = new ArrayList<>();

    private PingManager() {}

    public void init(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(SP_PING, Context.MODE_PRIVATE);
    }

    public void ping(int action, Map<String, String> value) {
        if (value == null) {
            value = new HashMap<>();
        }
        value.put(KEY_PING_TYPE, String.valueOf(action));
        synchronized (mPingDataList) {
            mPingDataList.add(value);
            if (needSend() && isNetworkAvailable()) {
                sendPingRequest();
            }
        }
    }

    private boolean needSend() {
        return mPingDataList.size() > MAX_BUFFER_SIZE;
    }

    private void sendPingRequest() {
        PingHandler handler = new PingHandler(mContext, mPingFailCallback);
        handler.requestPing(mPingDataList);
        mPingDataList.clear();
    }

    private boolean isNetworkAvailable() {
         ConnectivityManager manager= (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo= manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         if (networkInfo != null && networkInfo.isAvailable()) {
            return true;
         } else {
             return false;
         }
    }

    private String getApkFileSFCrc32(String sourceDir) {
        long crc = 0xffffffff;

        try {
            File f = new File(sourceDir);
            ZipFile z = new ZipFile(f);
            Enumeration<? extends ZipEntry> zList = z.entries();
            ZipEntry ze;
            while (zList.hasMoreElements()) {
                ze = zList.nextElement();
                if (ze.isDirectory()
                        || ze.getName().toString().indexOf("META-INF") == -1
                        || ze.getName().toString().indexOf(".SF") == -1) {
                    continue;
                } else {
                    crc = ze.getCrc();
                    break;
                }
            }
            z.close();
        } catch (Exception e) {
        }
        return Long.toHexString(crc);
    }

    public static String getFileMD5(String sourceDir) {
        File file = new File(sourceDir);
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public void reportUserAction4App(String action, String packageName) {
        Map<String, String> pingMap = new HashMap<>();
        pingMap.put("action", action);
        pingMap.put("source", "1");
        pingMap.put("pkg_name", packageName);
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES);
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);
            pingMap.put("version_code", String.valueOf(packageInfo.versionCode));
            pingMap.put("crc", getApkFileSFCrc32(applicationInfo.sourceDir));
            pingMap.put("md5", getFileMD5(applicationInfo.sourceDir));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ping(1, pingMap);
    }

    public void reportLauncherAppList(String xml) {
        Map<String, String> pingMap = new HashMap<>();
        pingMap.put("launcher_info", xml);
        ping(2, pingMap);
        mSharedPreferences.edit().putLong(KEY_PING_APPLIST_LAST_REPORT, System.currentTimeMillis()).commit();
    }

    public boolean needReportLauncherAppList() {
        return System.currentTimeMillis() - mSharedPreferences.getLong(
                KEY_PING_APPLIST_LAST_REPORT, 0) > MIN_REPORT_INTERVAL;
    }
}
