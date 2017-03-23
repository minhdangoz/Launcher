package com.klauncher.setupwizard.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

/**
 * description：Wifi的监听和连接
 * <br>author：caowugao
 * <br>time： 2017/03/23 14:36
 */
public class WifiInfoManager extends BroadcastReceiver {

    private Context context;
    private static final String TAG = WifiInfoManager.class.getSimpleName();
    private static final boolean DEBUG = true;
    private WifiManager wifiManager;
    //  网络加密模式  OPEN, WEP, WPA, WPA2
    public static final int OPEN = 1;
    public static final int WEP = 2;
    public static final int WPA = 3;
    public static final int WPA2 = 4;
    private int mNetworkID;


    public WifiInfoManager(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        context.registerReceiver(this, filter);
    }

    public void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        } else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {//正在打开
            logDebug("openWifi() Wifi正在打开...");
            if (null != listener) {
                listener.onWifiEnabling();
            }
        } else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {//已经打开
            logDebug("openWifi() Wifi已经打开!!!");
            if (null != listener) {
                listener.onWifiEnabled();
            }
        }
    }

    public void closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        } else if ((wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING)) {//正在关闭
            logDebug("closeWifi() Wifi正在关闭...");
            if (null != listener) {
                listener.onWifiDisabling();
            }
        } else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {//已经关闭
            logDebug("closeWifi() Wifi已经关闭!!!");
            if (null != listener) {
                listener.onWifiDisEnabled();
            }
        }

    }

    public void onDestory() {
        context.unregisterReceiver(this);
    }

    public boolean isWifiEnabled(){
        return wifiManager.isWifiEnabled();
    }

    public interface OnWifiListener {
        void onWifiEnabled();

        void onWifiDisabling();

        void onWifiDisEnabled();

        void onWifiEnabling();

        void onWifiUnknown();

        void onWifiConnected(WifiInfo info);

        void onWifiDisconnected();

        void onWifiScanSuccess(List<ScanResult> results);

        void onWifiScanNone();
    }

    private OnWifiListener listener;

    public void setOnWifiListener(OnWifiListener listener) {
        this.listener = listener;
    }

    public void connect(String ssid, String password, int securityMode) {
        //添加新的网络配置
        WifiConfiguration cfg = new WifiConfiguration();
        cfg.SSID = "\"" + ssid + "\"";
        if (password != null && !"".equals(password)) {
            //这里比较关键，如果是WEP加密方式的网络，密码需要放到cfg.wepKeys[0]里面
            if (securityMode == WEP) {
                cfg.wepKeys[0] = "\"" + password + "\"";
                cfg.wepTxKeyIndex = 0;
            } else {
                cfg.preSharedKey = "\"" + password + "\"";
            }
        }
        cfg.status = WifiConfiguration.Status.ENABLED;

        //添加网络配置
        mNetworkID = wifiManager.addNetwork(cfg);
        boolean enableNetwork = wifiManager.enableNetwork(mNetworkID, true);//连接Wifi
        if (!enableNetwork) {//
            logDebug("connect() Wifi已连接,ssid=" + ssid);
            if (null != listener) {
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                listener.onWifiConnected(connectionInfo);
            }
        }
    }

    public void disconnect() {
        wifiManager.disableNetwork(mNetworkID);
        wifiManager.disconnect();
    }


    private void logDebug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (null == action || "".equals(action)) {
            return;
        }

        if (null == listener) {
            return;
        }

        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {//WiFi状态改变
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLING:
                    logDebug("onReceive() WiFi状态  WIFI_STATE_DISABLING");
                    listener.onWifiDisabling();
                    break;
                case WifiManager.WIFI_STATE_DISABLED://不可用
                    logDebug("onReceive() WiFi状态 WIFI_STATE_DISABLED");
                    listener.onWifiDisEnabled();
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    logDebug("onReceive() WiFi状态 WIFI_STATE_ENABLING");
                    listener.onWifiEnabling();
                    break;
                case WifiManager.WIFI_STATE_ENABLED://可用
                    logDebug("onReceive() WiFi状态 WIFI_STATE_ENABLED,开始扫描...");
                    wifiManager.startScan();//开启扫描
                    listener.onWifiEnabled();
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    logDebug("onReceive() WiFi状态 WIFI_STATE_UNKNOWN");
                    listener.onWifiUnknown();
                    break;
            }
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {//WiFi网络状态改变,WifiManager
            // .WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候,WifiManager.NETWORK_STATE_CHANGED_ACTION不会接收到
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;//当然，这边可以更精确的确定状态
                    logDebug("onReceive() WiFi网络状态 isConnected=" + isConnected);
                    if (isConnected) {//连接
                        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                        listener.onWifiConnected(connectionInfo);
                    } else {
                        listener.onWifiDisconnected();
                    }
                }
            }
        } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {//WiFi扫描完成,
            // 通过wifimanager的startScan主动发起扫描,扫描完成后会发送这个广播
            List<ScanResult> list = wifiManager.getScanResults();//获取到可用的WiFi列表
            // 此处循环遍历，可以根据SSID 或者MAC，与某个指定wifi进行对比，如果相等可以认为进入了该wifi的范围。
            if (null == list || list.isEmpty()) {
                logDebug("onReceive() WiFi扫描完成，数据为空！！！");
                listener.onWifiScanNone();
            } else {
                listener.onWifiScanSuccess(list);
                logDebug("onReceive() WiFi扫描完成，共扫描到"+list.size()+"条可用Wifi");
            }
        } else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {//Wifi连接
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info.getNetworkId() == mNetworkID && info.getSupplicantState() == SupplicantState.COMPLETED) {
                logDebug("onReceive() 连接Wifi成功");
                listener.onWifiConnected(info);
            }
        }
    }
}
