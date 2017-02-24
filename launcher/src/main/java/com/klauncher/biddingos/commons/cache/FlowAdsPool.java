package com.klauncher.biddingos.commons.cache;


import com.klauncher.biddingos.commons.ads.AdInfo;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class FlowAdsPool {
    private static final String TAG = "FlowAdsPool";
    private static FlowAdsPool flowAdsPool = null;
    private static Map<String, AdInfo> pkg_adInfo = new ConcurrentHashMap<>();
    //刷新时间
    private long lastRefreshTime = 0;

    public static synchronized FlowAdsPool shareInstance() {
        if (null == flowAdsPool) {
            flowAdsPool = new FlowAdsPool();
        }
        return flowAdsPool;
    }

    public synchronized void  clearAndUpdateAdCaches(Map<String, AdInfo> map_pkg_adInfo, long updateTime) {
        if(null!=map_pkg_adInfo) {
            pkg_adInfo.clear();
        }

        pkg_adInfo.putAll(map_pkg_adInfo);
        lastRefreshTime = updateTime;

        LogUtils.d(TAG, "Timestamp: [" + updateTime + "] clearAndUpdateAdCaches: " + pkg_adInfo.toString());
    }

    public synchronized AdInfo getAdInfoByPkgname(String pkgName) {
        return pkg_adInfo.get(pkgName);
    }

    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    public boolean isFlowAdsPoolEmpty() {
        return pkg_adInfo.isEmpty();
    }



}
