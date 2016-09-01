
package com.klauncher.biddingos.commons;

import android.content.Context;
import android.util.Log;

import com.klauncher.biddingos.commons.ads.AdsRequestObject;
import com.klauncher.biddingos.commons.analytics.AppScanner;


/**
 * 保存系统设置信息
 */
public class Setting  {

    private static final String TAG = "Setting";
    public static Context context;
//    public static int SDK_MODE = 1;
    public static final int SYSTEM_PLATFORM=3;
    /**
     * 是否已经初始化
     */
    public static boolean bInit = false;

    /**
     * 流量广告刷新时间设置（秒）
     */
    private static int REFLESH_FLOWADS_TIME = 3600 * 12;

    public static int getREFLESH_FLOWADS_TIME() {
        return Setting.REFLESH_FLOWADS_TIME;
    }

    public static final String SDK_VERSION = "3.7.9";
    //协议
    public static final String PROTOCOL = "http://";
    /**
     * biddingos服务器域名
     */
    public static final String BiddingOS_HOST = "sandbox-adn.biddingos.com";
    //    public static final String BiddingOS_HOST_DEBUG_PRIVATE = "192.168.1.222";
    public static final String BiddingOS_HOST_ONLINE = "dsp.biddingos.com";
    /**
     * 请求广告位对应的广告应用ID列表参数路径
     */
    public static final String BiddingOS_AD_CALL = "/up/www/delivery/v2/ajson?mid=";

    /**
     * 获取投放应用ID列表参数路径
     */
    public static final String BiddingOS_CAMPAIGN = "/bos/api/v1/campaigns?mid=";
    /**
     * 用户信息跟踪接口参数
     */
    public static final String USER_INFO_TRACK_URL = "/bos/api/v1/ai?from=v2&mid=";
    /**
     * Banner请求地址
     */
//    public static final String BANNER_DEBUG_URL_PRIVATE = "/ADN_up/www/delivery/aBanner.php?mid=";
    public static final String BANNER_URL = "/up/www/delivery/aBanner.php?mid=";
    public static final String INTERSTITIAL_URL = "/up/www/delivery/aInterstitial.php?mid=";
    /**
     * 网络连接超时设置
     */
    public static int HTTP_CONNECT_TIMEOUT = 10000;
    /**
     * 媒体商接入密钥
     */
    public static String SECRET = "BiddingOS";
    /**
     * 打印日志开关
     */
    public static boolean bWriteLog = true;
    /**
     * 媒体商标识，唯一标识一个媒体商
     */
    public static String MID = "";

    /**
     * 媒体ID
     */
    public static String AFID = "";


    /**
     * 秘钥ID
     */
    public static String KID = "BiddingOS";

    /**
     * 测试状态
     */
    public static boolean DEGUGGABLE = false;

    public static String getBiddingOS_CAMPAIGN_URL(int nType,boolean isFlow) {
        String type = nType == 1 ? "appid" : "package";
        return Setting.PROTOCOL + (Setting.DEGUGGABLE ? Setting.BiddingOS_HOST : BiddingOS_HOST_ONLINE) + Setting.BiddingOS_CAMPAIGN + Setting.MID + "&affiliateid=" + Setting.AFID + "&status=0&list=" + type+(isFlow?("&adtype=flow&platform=3"):"");
    }

    public static String getBiddingOS_AD_CALL_URL() {
        return Setting.PROTOCOL + (Setting.DEGUGGABLE ? Setting.BiddingOS_HOST : BiddingOS_HOST_ONLINE) + Setting.BiddingOS_AD_CALL + Setting.MID + "&afid=" + Setting.AFID;
//        return "http://192.168.1.229/ADN_up/www/delivery/v2/ajson.php?mid="+ Setting.MID + "&afid=" + Setting.AFID;
    }

    public static String getBiddingOS_USER_INFO_TRACK_URL() {
        return Setting.PROTOCOL + (Setting.DEGUGGABLE ? Setting.BiddingOS_HOST : BiddingOS_HOST_ONLINE) + Setting.USER_INFO_TRACK_URL + Setting.MID + "&afid=" + Setting.AFID;
    }

    public static synchronized boolean isInitDone() {
        return bInit;
    }

    public static synchronized void setInitDone() {
        bInit = true;
    }

    /**
     * 初始化SDK
     * 供已对接了的旧sdk使用，更新请使用最下面重写父类的init
     *
     * @param context    设备上下文
     * @param mid        媒体商代理ID
     * @param afid       媒体商ID
     * @param secret     密钥
     * @param kid        秘钥ID
     * @param debuggable 是否开启调试模式（开启后会输出全部日志且上报地址为测试地址，不会扣费）
     */
    public static void init(Context context, String mid, String afid, String secret, String kid, boolean debuggable) {

        if (Setting.isInitDone()) {
            Log.i(TAG, "init has been launch");
            return;
        }

        if (null == context) {
            Log.e(TAG, "BiddingOS sdk init failed, context is null");
            return;
        }

        if (null == mid || "" == mid) {
            Log.e(TAG, "BiddingOS sdk init failed, mid is empty");
            return;
        }

        if (null == afid || "" == afid) {
            Log.e(TAG, "afid is empty");
            afid = "0";
        }

        if (null == secret || "" == secret || 32 > secret.length()) {
            Log.e(TAG, "secret is wrong");
        }
        Setting.context = context.getApplicationContext();
        Setting.MID = mid;
        Setting.AFID = afid;
        Setting.SECRET = secret;
        Setting.DEGUGGABLE = debuggable;
        Setting.KID = kid;
        Setting.bWriteLog = debuggable;

        //生成SessionId
        AdsRequestObject.setSessionid();
        AppScanner.init();

        //设置初始化成功
        Setting.setInitDone();
    }

    public static String getBiddingOS_HOST() {
        return Setting.DEGUGGABLE ? Setting.BiddingOS_HOST : Setting.BiddingOS_HOST_ONLINE;
    }

    public static String getBiddingOS_BANNER_URL(int nWidth, int nHeight, int nDpi) {
        return Setting.PROTOCOL + Setting.getBiddingOS_HOST() +
                Setting.BANNER_URL + Setting.MID + "&afid=" + Setting.AFID + "&width=" + nWidth +
                "&height=" + nHeight + "&dpi=" + nDpi;
    }
    public static String getBiddingOS_INTERSTITIAIL_URL(int nWidth, int nHeight, int nDpi) {
        return Setting.PROTOCOL + Setting.getBiddingOS_HOST() +
                Setting.INTERSTITIAL_URL + Setting.MID + "&afid=" + Setting.AFID + "&width=" + nWidth +
                "&height=" + nHeight + "&dpi=" + nDpi;
    }


}
