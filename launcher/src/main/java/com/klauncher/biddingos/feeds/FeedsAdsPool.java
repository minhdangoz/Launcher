package com.klauncher.biddingos.feeds;


import com.klauncher.biddingos.commons.ads.AdInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by：lizw on 2015/12/24 12:21
 */
public class FeedsAdsPool {
    private static final String TAG = "FeedsAdsPool";
    private static FeedsAdsPool feedsAdsPool = null;
    public static Map<Integer, AdInfo> feedsAd_Info = null;

    public static FeedsAdsPool getInstance() {
        if (null == feedsAdsPool) {
            synchronized (FeedsAdsPool.class) {
                if (null == feedsAdsPool) {
                    feedsAdsPool = new FeedsAdsPool();
                }
            }
        }
        return feedsAdsPool;
    }

    public void addFeedsAdsPool(AdInfo adInfo) {
        if (null != adInfo) {
            if (null == feedsAd_Info) {
                feedsAd_Info = new HashMap<Integer, AdInfo>();
            }
            feedsAd_Info.put(adInfo.getZoneid(), adInfo);
        } else {
            return;
        }
    }

    public void removeFeedsAdsPool() {
        if (null != this.feedsAd_Info) {
            this.feedsAd_Info = null;
        }
    }

    public boolean isEmpty() {
        if (null != feedsAd_Info && feedsAd_Info.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

}
