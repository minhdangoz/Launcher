package com.klauncher.biddingos.feeds;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.alpsdroid.ads.Placement;
import com.alpsdroid.ads.feeds.ActionCallBack;
import com.alpsdroid.ads.feeds.FeedsAdHelper;
import com.alpsdroid.ads.feeds.FeedsAdView;
import com.alpsdroid.ads.feeds.FeedsAppInstallAd;
import com.alpsdroid.ads.feeds.LoadListener;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.ads.AdInfo;
import com.klauncher.biddingos.commons.ads.AdsRequestObject;
import com.klauncher.biddingos.commons.analytics.EventHelper;
import com.klauncher.biddingos.commons.analytics.UserEvent;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpResponseStatus;
import com.klauncher.biddingos.commons.net.HttpUtils;
import com.klauncher.biddingos.commons.utils.AESUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FeedsAdHelplerImpl implements FeedsAdHelper {

    private static final String TAG = "FeedsAdHelplerImpl";
    private static final int LOADSUCCESS = 0x0001;
    private static final int LOADFAIL = 0x0002;
    private LoadListener listener;


    /**
     * 获取广告
     *
     * @param zoneId   广告位
     * @param listener 获取广告素材监听
     */
    @Override
    public void builder(int zoneId, LoadListener listener) {
        this.listener = listener;
        if(null!= FeedsAdsPool.feedsAd_Info&& FeedsAdsPool.feedsAd_Info.containsKey(zoneId)
                && FeedsAdsPool.feedsAd_Info.get(zoneId).getbIsDownloading()) {//广告原本存在并且在下载中
            Message message = new Message();
            message = failedMsg(message, ErrorCode.HASADDOWNLOADING.getValue());
            handler.sendMessage(message);
        }else {
            getAds(zoneId);
        }
    }

    /**
     * 绑定布局
     *
     * @param rl     传入的Fragment
     * @param adView 广告素材对象
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void boundView(FrameLayout rl, final FeedsAdView adView, final FeedsAppInstallAd feedsAppInstallAd,
                          ActionCallBack callBack) {
        final int zoneId = feedsAppInstallAd.getZoneId();
        adView.setZoneId(zoneId);
        adView.setActionListener(callBack);
        adView.setWindowVisibilityChangedListener(new MyWindowVisibilityChangedListener());
        FeedsAction feedsAction = new FeedsAction();
        feedsAction.setActionListener(callBack);
        feedsAction.setOnClick(adView, zoneId);
        if (AdInfo.MODE_CPC.equals(feedsAppInstallAd.getPay_mode())) {
            adView.getStarRatingView().setVisibility(View.GONE);
        } else {
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        try {
            rl.removeAllViews();
            rl.addView(adView);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class MyWindowVisibilityChangedListener implements FeedsAdView.WindowVisibilityChangedListener {

        @Override
        public void visibilityHasChanged(int visibility, int zoneId, ActionCallBack actionListener, FeedsAdView feedsAdView) {
            switch (visibility) {
                case View.VISIBLE:
//                    LogUtils.i(TAG, "visibility=" + visibility+" feedsAdView.isShown()="+feedsAdView.isShown()+" getVisibility="+feedsAdView.getVisibility());
                    if (feedsAdView.getVisibility()== View.VISIBLE) {
                        FeedsAction.notifyImpression(zoneId);
                        if (null != actionListener) {
                            actionListener.onAdImpression(zoneId, feedsAdView);
                        }
                    }
                    break;
            }
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (null == listener) {
                return;
            }
            switch (msg.what) {
                case LOADFAIL:
                    int errorCode = (int) msg.obj;
                    listener.onAdFailedToLoad(errorCode);
                    break;
                case LOADSUCCESS:
                    FeedsAppInstallAd ad = (FeedsAppInstallAd) msg.obj;
                    listener.onAppInstallAdLoaded(ad);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void getAds(final int zoneId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(TAG, "getAds");
                Placement placement = new Placement(0, zoneId);
                List<Placement> placementList = new ArrayList<Placement>();
                placementList.add(placement);
                AdsRequestObject adsRequest = new AdsRequestObject(AdsRequestObject.FEEDSAD, "", placementList, "", null, false);
                //通过广告位id获取广告
                requstAdInfos(adsRequest);
                notifyAdsRequestEvent(zoneId);
            }
        }).start();
    }

    private void notifyAdsRequestEvent(int zoneId) {
        String target = "pkgName:" + Setting.context.getPackageName();
        String zones = "" + zoneId;
        EventHelper.postAdsEvent(UserEvent.EVENT_ADS_REQUEST, target, zones);
    }

    /**
     * 通过广告请求对象，请求广告数据
     *
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
            } else {
                bRequestSuccess = false;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
            bRequestSuccess = false;
        }
        if (null == httpResponse) {
            LogUtils.w(TAG, "get adInfo fail-httpResponse is null");
            return;
        }
        String delivery = httpResponse.getRespBody(false);

//        LogUtils.w(TAG, "re="+delivery);
        LogUtils.w(TAG, AESUtils.decode(Setting.SECRET, delivery));
        AdInfo adInfo = null;
        Message message = new Message();
        if (bRequestSuccess && !TextUtils.isEmpty(delivery)) {
            LogUtils.d(TAG, "load ad Succeed.");
            //解析feeds数据
            String jsonString = AESUtils.decode(Setting.SECRET, delivery);
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(jsonString);
                JSONArray deliveryArray = jsonObject.getJSONArray("delivery");
                if (null != deliveryArray) {
                    adInfo = new AdInfo(deliveryArray.getJSONObject(0));
                }
                if (null != adInfo) {
                    FeedsAppInstallAd feedsAppInstallAd = new FeedsAppInstallAd(adInfo.getZoneid(), adInfo.getPay_mode(), adInfo.getAction_text(), new JSONObject(adInfo.getAssets()));
                    if (checkAssets(feedsAppInstallAd)) {
                        message.what = LOADSUCCESS;
                        message.obj = feedsAppInstallAd;
                        FeedsAdsPool.getInstance().addFeedsAdsPool(adInfo);
                    } else {
                        if (null != feedsAppInstallAd) {//不展示素材回收
                            if (null != feedsAppInstallAd.getIcon()) {
                                feedsAppInstallAd.getIcon().recycle();
                            }
                            if (null != feedsAppInstallAd.getImage()) {
                                feedsAppInstallAd.getImage().recycle();
                            }
                        }
                        message = failedMsg( message, ErrorCode.ASSETSNOTCOMPLETE.getValue());
                    }
                } else {
                    message = failedMsg( message, ErrorCode.ASSETSNOTCOMPLETE.getValue());
                }
            } catch (JSONException e) {
                message = failedMsg( message, ErrorCode.REQUESTFAILED.getValue());
                LogUtils.d(TAG, "load ad failed on get feedsAd");
                e.printStackTrace();
            }
        } else {
            message = failedMsg( message, ErrorCode.REQUESTFAILED.getValue());
            LogUtils.d(TAG, "load ad failed.");
        }
        handler.sendMessage(message);

    }

    private Message failedMsg( Message message, int errorCode) {
        message.what = LOADFAIL;
        message.obj = errorCode;
        return message;
    }


    private boolean checkAssets(FeedsAppInstallAd ad) {
        //TODO 界面有修改需要做调整
        if (TextUtils.isEmpty(ad.getHeadline())) {
            //无应用名
            LogUtils.i(TAG, "无应用名");
            return false;
        } else if (TextUtils.isEmpty(ad.getApp_star()) && !AdInfo.MODE_CPC.equals(ad.getPay_mode())) {
            LogUtils.i(TAG, "无星级");
            //无星级
            return false;
        } else if (TextUtils.isEmpty(ad.getStore())) {
            LogUtils.i(TAG, "无一句话描述");
            //无一句话描述
            return false;
        } else if (null == ad.getImage()) {
            LogUtils.i(TAG, "无素材图片");
            //无素材图片
            return false;
        } else if (null == ad.getIcon()) {
            LogUtils.i(TAG, "无logo");
            //无logo
            return false;
        } else {
            return true;
        }
    }

}
