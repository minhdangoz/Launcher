package com.klauncher.biddingos.interstitial;


import android.content.Context;
import android.util.DisplayMetrics;

import com.alpsdroid.ads.banner.AdSize;
import com.alpsdroid.ads.interstitial.InterstitialAdListener;
import com.klauncher.biddingos.commons.Setting;

/**
 * Created by：lizw on 2016/3/31 11:37
 */
public class BiddingOsInterstitialAd {
    private int zoneId;
    private AdSize adSize;
    /**
     *
     * @param zoneId 广告位id
     * @param context 设备上下位
     */
    public BiddingOsInterstitialAd(int zoneId,Context context) {
        this.zoneId=zoneId;
        DisplayMetrics dm = Setting.context.getResources().getDisplayMetrics();
        this.adSize=new AdSize(dm.widthPixels,dm.heightPixels);
    }

    /**
     * 请求对应zonId的广告数据
     */
    public void load() {
        new InterstitialHelperImpl().load(zoneId,adSize);
    }

    /**
     * 展示Interstitial广告
     */
    public void show() {
        new InterstitialHelperImpl().show(zoneId);
    }

    /**
     * 客户端设置回调监听
     * @param listener 回调监听对象
     */
    public void setAdListener(InterstitialAdListener listener) {
        new InterstitialHelperImpl().setAdListener(zoneId, listener);
    }

    /**
     * 判断广告是否请求成功
     * @return
     */
    public boolean isAdReady() {
        return new InterstitialHelperImpl().isAdReady(zoneId);
    }
}
