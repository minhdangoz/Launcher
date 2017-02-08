package com.klauncher.kinflow.utilities;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.kapp.mob.adx.KBannerAd;
import com.kapp.mob.adx.KBannerAdListener;
import com.klauncher.kinflow.views.recyclerView.data.AdxSdkBanner;

import java.util.ArrayList;
import java.util.List;

/**
 * description：ADX-SDK辅助类
 * <br>author：caowugao
 * <br>time： 2017/01/06 11:31
 */

public class KBannerAdUtil {
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
    public static KBannerAd addKBannerAdView(final Context context, final View onFailView, final View root, final ViewGroup parent) {

        Object cache = root.getTag();
        if(null!=cache&&cache instanceof KBannerAd){
            logDebug("addKBannerAdView() 有缓存");
            KBannerAd cacheAd=(KBannerAd)cache;
            setItemHeight(context, root);
            parent.addView(cacheAd, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                    .WRAP_CONTENT));
            return cacheAd;
        }

        final KBannerAd ad = new KBannerAd(context, "1016", "10013", "5");
        ad.setLisetner(new KBannerAdListener() {
            @Override
            public void onAdSwitch() {
                logDebug("getKBannerAdView() onAdSwitch...");
            }

            @Override
            public void onAdShow(String s) {
                logDebug("getKBannerAdView() onAdShow..." + s);
            }

            @Override
            public void onAdReady() {
                logDebug("getKBannerAdView() onAdReady...");
                root.setTag(ad);
                setItemHeight(context, root);
            }

            @Override
            public void onAdFailed(String s) {
                logDebug("getKBannerAdView() onAdFailed..." + s);
                ad.removeAllViews();
//                2017-1-11 去掉，改为没有默认广告
//                ad.addView(onFailView, new KBannerAd.LayoutParams(KBannerAd.LayoutParams.MATCH_PARENT, KBannerAd
//                        .LayoutParams.MATCH_PARENT));
                ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
                layoutParams.height=0;
                parent.setLayoutParams(layoutParams);
            }

            @Override
            public void onAdClick() {
                logDebug("getKBannerAdView() onAdClick...");
            }

            @Override
            public void onAdClose() {
                logDebug("getKBannerAdView() onAdClose...");
            }
        });
        parent.addView(ad, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                .WRAP_CONTENT));
        ad.loadAd();
        return ad;
    }
    private static void setItemHeight(Context context, View root) {
        int rootHeight = Dips.asDp(context, 172);
        logDebug("setItemHeight() rootHeight=" + rootHeight);
        ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
        layoutParams.height=rootHeight;
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
