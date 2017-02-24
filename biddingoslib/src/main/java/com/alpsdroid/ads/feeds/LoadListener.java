package com.alpsdroid.ads.feeds;


/**
 * Created by：lizw on 2015/12/22 14:49
 */
public interface LoadListener {
    void onAppInstallAdLoaded(FeedsAppInstallAd ad);
    void onAdFailedToLoad(int errorCode);
}
