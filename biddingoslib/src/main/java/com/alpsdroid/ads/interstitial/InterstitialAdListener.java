package com.alpsdroid.ads.interstitial;

/**
 * Created by：lizw on 2016/3/28 11:49
 */
public interface InterstitialAdListener {
    void onAdReady();
    void onAdImpression();
    void onAdClosed();
    void onLoadFailed(String errorInfo);
    void onAdClicked();
    void onConversion();
}
