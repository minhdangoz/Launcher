package com.alpsdroid.ads.feeds;

import android.view.View;

/**
 * Created by：lizw on 2016/3/8 12:01
 */
public interface ActionCallBack {
    void onAdImpression(int zoneId, FeedsAdView feedsAdView);
    void onAdClick(int zoneId, FeedsAdView feedsAdView, String payMode);
    void onConversion(int zoneId, FeedsAdView feedsAdView, String payMode);
}
