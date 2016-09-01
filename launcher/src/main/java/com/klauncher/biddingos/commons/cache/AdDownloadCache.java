package com.klauncher.biddingos.commons.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class AdDownloadCache {
    private static final String TAG = "AdDownloadCache";
    private static AdDownloadCache adDownloadCache = null;
    private static Map<String, HashMap<String, String>> appId_Ti = new ConcurrentHashMap<>();

    public static synchronized AdDownloadCache shareInstance() {
        if (null == adDownloadCache) {
            adDownloadCache = new AdDownloadCache();
        }
        return adDownloadCache;
    }

    public synchronized void setAppTiInfo(String creativeId, HashMap<String, String> tiMap) {
        appId_Ti.put(creativeId, tiMap);
    }

    public synchronized void clearAll() {
        appId_Ti.clear();
    }

    public synchronized void deleteAdDownloadCacheByAppId(String appId) {
        appId_Ti.remove(appId);
    }

    public synchronized HashMap<String, String> getAdDownloadCacheByAppId(String appId) {
        return appId_Ti.get(appId);
    }


}
