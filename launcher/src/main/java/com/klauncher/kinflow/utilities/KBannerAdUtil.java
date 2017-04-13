package com.klauncher.kinflow.utilities;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.AdView;
import com.klauncher.kinflow.views.recyclerView.data.AdxSdkBanner;
import com.klauncher.utilities.DeviceInfoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * description：ADX-SDK辅助类
 * <br>author：caowugao
 * <br>time： 2017/01/06 11:31
 */

public class KBannerAdUtil {

    private static boolean isLunbo = true;

    private static int type = 0;

    private static String[] msspMsgFlow = {
            "3410166",
            "3799287",
            "3799288",
            "3803123",
            "3803141",
            "3803145"
    };

    private KBannerAdUtil() {
    }

    private static final String TAG = KBannerAdUtil.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static void logDebug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }




//    /**
//     * 获取单个广告的View
//     *
//     * @param context
//     * @param onFailView
//     * @param parent
//     * @return
//     */
//    public static KBannerAd addKBannerAdView(Context context, final View onFailView, final ViewGroup parent) {
//        final KBannerAd ad = new KBannerAd(context, "1016", "10013", "5");
//        ad.setLisetner(new KBannerAdListener() {
//            @Override
//            public void onAdSwitch() {
//                logDebug("addKBannerAdView() onAdSwitch...");
//            }
//
//            @Override
//            public void onAdShow(String s) {
//                logDebug("addKBannerAdView() onAdShow..." + s);
//            }
//
//            @Override
//            public void onAdReady() {
//                logDebug("addKBannerAdView() onAdReady...");
//            }
//
//            @Override
//            public void onAdFailed(String s) {
//                logDebug("addKBannerAdView() onAdFailed..." + s);
////                2017-1-11 去掉，改为没有默认广告
////                ad.addView(onFailView, new KBannerAd.LayoutParams(KBannerAd.LayoutParams.MATCH_PARENT, KBannerAd
////                        .LayoutParams.MATCH_PARENT));
//
//                parent.removeView(ad);
//                ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
//                layoutParams.height=0;
//                parent.setLayoutParams(layoutParams);
//            }
//
//            @Override
//            public void onAdClick() {
//                logDebug("addKBannerAdView() onAdClick...");
//            }
//
//            @Override
//            public void onAdClose() {
//                logDebug("addKBannerAdView() onAdClose...");
//            }
//        });
//        parent.addView(ad, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
//                .WRAP_CONTENT));
//        ad.loadAd();
//        return ad;
//    }

    /**
     * 获取单个广告的View
     * @param context
     * @param onFailView
     * @param root
     * @param parent
     * @return
     */
    public static void addKBannerAdView(final Context context, final View onFailView, final View root, final ViewGroup parent) {
        Log.d("hw","addKBannerAdView 1 ");
        final int rootWidth = DeviceInfoUtils.getSCreenWidthInt(context) -2 * Dips.dipsToIntPixels(16, context);
        final int rootHeight = rootWidth / 2 + Dips.dipsToIntPixels(12, context);
        Object cache = root.getTag();
        if(null!=cache&&cache instanceof WebView){
            Log.d("hw","cache ");
            logDebug("addKBannerAdView() 有缓存");
            WebView cacheAd= (WebView) cache;
            setItemHeight(context, root, rootWidth, rootHeight);
            parent.addView(cacheAd, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                    .WRAP_CONTENT));
            return;
        }

        final ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();

        /**
         * Step 1. 创建BaiduNative对象，参数分别为：
         * 上下文context，广告位ID, BaiduNativeNetworkListener监听（监听广告请求的成功与失败）
         * 注意：请将YOUR_AD_PALCE_ID替换为自己的广告位ID
         */

        //klauncher2 申请test appid 和 placeid
//        AdView.setAppSid(context, "a7482f81");
//        String placeId = "3420062";
//        if (isLunbo) {
//            placeId = "3420062";
//        } else {
//            placeId = "3433910";
//        }

        //正式上线前用 fcda70a4 appid 以及 3410166 的轮播信息流placeid, 3418992 橱窗信息信息流placeid
        AdView.setAppSid(context, "fcda70a4");
        String placeId = msspMsgFlow[type];
        type++;
        if (type == msspMsgFlow.length) {
            type = 0;
        }
        Log.d("hw","placeId : " + placeId);
        BaiduNative baidu = new BaiduNative(context, placeId,
                new BaiduNative.BaiduNativeNetworkListener() {

                    @Override
                    public void onNativeFail(NativeErrorCode arg0) {
                        layoutParams.height=0;
                        parent.setLayoutParams(layoutParams);
                    }

                    @Override
                    public void onNativeLoad(List<NativeResponse> arg0) {
                        if (arg0 != null && arg0.size() > 0) {
//                            nrAdList = arg0;
                            NativeResponse mNrAd = arg0.get(0);
                            if (mNrAd.getMaterialType() == NativeResponse.MaterialType.HTML) {
                                WebView webView = mNrAd.getWebView();
                                root.setTag(webView);
                                setItemHeight(context, root, rootWidth, rootHeight);
                                parent.addView(webView, new ViewGroup.LayoutParams(rootWidth, rootHeight));
                            } else {
                                layoutParams.height=0;
                                parent.setLayoutParams(layoutParams);
                            }
                        }
                    }

                });
        RequestParameters requestParameters = new RequestParameters.Builder()
                .setWidth(rootWidth)
                .setHeight(rootHeight)
                .downloadAppConfirmPolicy(
                        RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE) // 用户点击下载类广告时，是否弹出提示框让用户选择下载与否
                .build();

        baidu.makeRequest(requestParameters);
    }


    private static void setItemHeight(Context context, View root, int w, int h) {
//        int rootHeight = Dips.asDp(context, 160);
        ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
//        int rootWidth = layoutParams.width;
//        logDebug("setItemHeight() rootHeight=" + rootHeight);
        layoutParams.width = w;
        layoutParams.height=h;
        root.setLayoutParams(layoutParams);
    }

    /**
     * 返回占位的单个广告
     * @return
     */
    public static AdxSdkBanner getKBannerAdForPlaceholder() {
        return new AdxSdkBanner(null);
    }
    /**
     * 返回占位的指定数量的广告
     * @param size
     * @return
     */
    public static List<AdxSdkBanner> getKBannerAdsForPlaceholder(int size){
        List<AdxSdkBanner> results = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            results.add(getKBannerAdForPlaceholder());
        }
        return results;
    }

}
