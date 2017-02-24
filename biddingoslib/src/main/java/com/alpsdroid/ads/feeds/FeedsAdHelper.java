package com.alpsdroid.ads.feeds;

import android.content.Context;
import android.widget.FrameLayout;

/**
 * Created by：lizw on 2015/12/22 14:40
 */
public interface FeedsAdHelper {
//    void init(Context var1, String var2, String var3, String var4, String var5, boolean var6);
    void builder(int zoneId, LoadListener listener);
    void boundView(FrameLayout rl, FeedsAdView adView, FeedsAppInstallAd ad, ActionCallBack callBack);
//    void destroy();
}
