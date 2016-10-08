package com.klauncher.biddingos.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alpsdroid.ads.flow.FlowAdHelper;
import com.alpsdroid.ads.flow.FlowAdInfo;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.ads.AdInfo;
import com.klauncher.biddingos.commons.ads.AdsRequestObject;
import com.klauncher.biddingos.commons.ads.CachedRemoteCreativeMgr;
import com.klauncher.biddingos.commons.analytics.EventHelper;
import com.klauncher.biddingos.commons.analytics.UserEvent;
import com.klauncher.biddingos.commons.cache.FlowAdsPool;
import com.klauncher.biddingos.commons.cache.SharedPreferencesUtils;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpResponseStatus;
import com.klauncher.biddingos.commons.net.HttpUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Edward on 2015/11/26.
 */
public class FlowAdHelplerImpl implements FlowAdHelper {

    private static final String TAG = "FlowAdHelplerImpl";
    private final int TIMENUMBER=1000;
    private Timer timer;
    private SingleTimerTask timerTask;

    @Override
    public void init(Context context, String mid, String afid, String secret, String kid, boolean debuggable) {

        Log.e("TEST", "123123");

        LogUtils.d(TAG, "call init debuggable: " + debuggable);

        if(Setting.isInitDone()) {
            Log.i(TAG, "init has been luanch");
            return;
        }
        com.klauncher.biddingos.AdHelper.init(context, mid, afid, secret, kid, debuggable);

        Log.i(TAG, "FlowAdHelpler init done.");
        //异步刷新广告池
        refleshAdsPool();
        //加载广告
        loadAds();
    }

//    /**
//     * 初始化SDK
//     * 2345已经接入，不能去掉
//     * @param context    设备上下文
//     * @param mid        媒体商代理ID
//     * @param afid       媒体商ID
//     * @param secret     密钥
//     * @param kid        秘钥ID
//     * @param debuggable 是否开启调试模式（开启后会输出全部日志且上报地址为测试地址，不会扣费）
//     */
//    public  void init(Context context, String mid, String afid, String secret, String kid, boolean debuggable) {
//        Setting.init(context, mid, afid, secret, kid,debuggable);
//        if(isInitDone()) {
//            Log.i(TAG, "FeedsAdHelper init has been luanch");
//            return;
//        }
//        Log.i(TAG, "FlowAdHelpler init done.");
//        //异步刷新广告池
//        refleshAdsPool();
//        //加载广告
//        loadAds();
//        //设置初始化成功
//        setInitDone();
//    }
    /**
     * 是否已经初始化
     */
    public static boolean bInit = false;

    public static synchronized boolean isInitDone() {
        return bInit;
    }
    public static synchronized void setInitDone() {
        bInit = true;
    }

    public void loadAds() {
        //判断广告池空，则读取文件加载广告到广告池(若文件广告的刷新时间需小于广告池的刷新时间，则不读取)

        if(!FlowAdsPool.shareInstance().isFlowAdsPoolEmpty()) {
            LogUtils.w(TAG, "FlowAdsPool is not empty.");
            return;
        }

        //若文件不存在数据（第一次打开），或者文件数据刷新时间早于广告池的刷新时间，则返回
        long lastTimeforSp = SharedPreferencesUtils.getOrganicAdsLastTimeRefresh();
        if(0==lastTimeforSp || lastTimeforSp<=FlowAdsPool.shareInstance().getLastRefreshTime()) {
            LogUtils.w(TAG, "Delivery data is empty or is earlier than FlowAdsPool's");
            return;
        }

        String delivery = SharedPreferencesUtils.getOrganicAds();

        LogUtils.w(TAG, delivery);

        //解析投放数据，然后刷新内存缓存
        Map<String, AdInfo> pkg_AdInfo = AdInfo.parseJSONString(delivery, true, 1, null, true);
        FlowAdsPool.shareInstance().clearAndUpdateAdCaches(pkg_AdInfo, lastTimeforSp);

    }


    /**
     * 刷新广告池
     */
    public void refleshAdsPool() {
        //开启定时器
        timer = new Timer();
        timerTask = new SingleTimerTask(handler);
        timer.schedule(timerTask, 0, Setting.getREFLESH_FLOWADS_TIME() *1000);
    }


    /**
     * 开线程获取所有广告
     */
    private void  refleshThread(){
        //TODO::开线程获取所有广告
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(TAG, "refresh");

                //取得媒体投放中的广告包名列表
                List<String> listPackages = CachedRemoteCreativeMgr.fetchAllCampaigns(2, true);
                if(null!=listPackages && listPackages.size()>0) {
                    //通过包名列表请求，取得对应的应用投放数据
                    AdsRequestObject adsRequest = new AdsRequestObject(AdsRequestObject.FLOWAD, "", null, "", listPackages, false);
                    requstAdInfos(adsRequest);
                    notifyAdsRequestEvent();
                }
            }
        }).start();
    }

    /**
     * 响应定时
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMENUMBER:
                    refleshThread();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 定时task
     */
    public class SingleTimerTask extends TimerTask {
        private Handler handler;

        public SingleTimerTask(Handler mHandler) {
            // TODO Auto-generated constructor stub
            this.handler = mHandler;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            /**
             * 定时时间到
             */
            handler.sendEmptyMessage(TIMENUMBER);
        }
    }


    /**
     * 通过广告请求对象，请求广告数据，缓存下来
     * @param adsRequest
     */
    private void requstAdInfos(AdsRequestObject adsRequest) {
        boolean bRequestSuccess;
        HttpResponse httpResponse = null;
        try {
            //POST 请求
            String body = adsRequest.getSignedAndEncryptString();
            httpResponse = HttpUtils.execute(new HttpRequest(HttpRequest.Method.POST, adsRequest.getUrl(), body));

            if (null != httpResponse && HttpResponseStatus.Code.OK == httpResponse.getRespCode()) {
                bRequestSuccess = true;
            }else {
                bRequestSuccess = false;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
            bRequestSuccess = false;
        }

        long updateTime = System.currentTimeMillis();

        String delivery = "";
        if(bRequestSuccess) {
            //以密文形式保存到文件
            LogUtils.d(TAG, "load ad Succeed.");
            SharedPreferencesUtils.updateFlowAdsPool(httpResponse.getRespBody(false), updateTime);
            delivery = httpResponse.getRespBody(false);
        }else {
            LogUtils.d(TAG, "load ad failed.");
            delivery = SharedPreferencesUtils.getOrganicAds();
        }

        LogUtils.w(TAG, delivery);

        //解析投放数据，然后刷新内存缓存
        Map<String, AdInfo> pkg_AdInfo = AdInfo.parseJSONString(delivery, true, 1, null, true);
        FlowAdsPool.shareInstance().clearAndUpdateAdCaches(pkg_AdInfo, updateTime);


    }

    private void notifyAdsRequestEvent() {
        String target = "pkgName:"+Setting.context.getPackageName();
        String zones = "";
        EventHelper.postAdsEvent(UserEvent.EVENT_ADS_REQUEST, target, zones);
    }


    public FlowAdInfo getDownloadInfo(String listId, int position, String packageName, int versionCode) {
        LogUtils.v(TAG, "call getDownloadInfo with listId:" + listId + " position:" + position + " pkgName:" + packageName);

        AdInfo ad = FlowAdsPool.shareInstance().getAdInfoByPkgname(packageName);
        if (null != ad) {
            if(versionCode>ad.getAppVersionCode()) {
                Log.w(TAG, "ad's versionCode is older than media");
                return null;
            }
            Log.d(TAG, "return flowAd");
            FlowAdInfo flowAdInfo = new FlowAdInfo(ad.getPackageName(), ad.getAppVersionName(), ad.getAppVersionCode(), ad.getAppDownloadUrl(), ad.getAppChecksum(), ad.getAppSize());
            return flowAdInfo;
        }

        return null;
    }

    /**
     *
     * @param listId
     * @param position
     * @param packageName
     */
    public void notifyImpression(String listId, int position, String packageName) {
        LogUtils.d(TAG, "call notifyImpression with listId:" + listId + " position:" + position + " pkgName:" + packageName);

        AdInfo ad = FlowAdsPool.shareInstance().getAdInfoByPkgname(packageName);
        if (null != ad) {

            if (ad.isNotifyimpression()) {
                LogUtils.d(TAG, "已经通知过有效展示 => " + ad.getPackageName());
            } else {
                ad.notifyImpression();
            }

            //上报展示事件,展示在不同的列表才需要上报
            if(listId != ad.getLastDisplayListId()) {
                EventHelper.postAdsEvent(UserEvent.EVENT_ADS_IMPRESSION, packageName, listId + "." + position);
                ad.setLastDisplayListId(listId);
            }
        }
    }

    public void notifyClick(String listId, int position, String packageName) {
        LogUtils.d(TAG, "call notifyClick with listId:" + listId + " position:" + position + " pkgName:" + packageName);

        AdInfo ad = FlowAdsPool.shareInstance().getAdInfoByPkgname(packageName);
        if (null != ad) {
            if(ad.isNotifyclick()) {
                LogUtils.d(TAG, "已经通知过有效点击 => " + packageName);
            }else  {
                ad.notifyClick(listId, position, true);
                //上报点击事件
                EventHelper.postAdsEvent(UserEvent.EVENT_ADS_DOWNLOAD_START, packageName, listId + "." + position);
            }
        }


    }

    public void notifyConversion(String packageName) {
        LogUtils.d(TAG, "call notifyConversion with pkgName:" + packageName);
        AdInfo.notifyConversion(packageName, true);

    }

    /**
     * 2345已经接入，不能取消
     */
    @Override
    public void destroy() {
        Log.i(TAG, "FlowAdHelper destroy done.");
    }
}
