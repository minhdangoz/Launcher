package com.klauncher.biddingos.impl;

import android.content.Context;
import android.util.Log;

import com.alpsdroid.ads.AdHelper;
import com.alpsdroid.ads.AppCreative;
import com.alpsdroid.ads.CachedRemoteCreative;
import com.alpsdroid.ads.Placement;

import java.util.List;
import java.util.Map;

/**
 * Created by Edward on 2015/11/26.
 */
public class AdHelplerImpl implements AdHelper {

    private static final String TAG = "AdHelplerImpl";
    /**
     * 91已经接入，不能去掉
     */
    @Override
    public void init(Context context, String mid, String afid, String secret, String kid, boolean debuggable) {
        com.klauncher.biddingos.AdHelper.init(context, mid, afid, secret, kid, debuggable);
        Log.i(TAG, "AdHelper init done.");
    }

    public <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId, CachedRemoteCreative<T> manager) {
        return com.klauncher.biddingos.AdHelper.getAds(pageOffset, listId, manager);
    }
    public <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId,String kw, CachedRemoteCreative<T> manager) {
        return com.klauncher.biddingos.AdHelper.getAds(pageOffset, listId,kw, manager);
    }

    public <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId, List<Placement> placementList, String kw, CachedRemoteCreative<T> manager) {
        return com.klauncher.biddingos.AdHelper.getAds(pageOffset, listId, placementList, kw, manager);
    }

    public <T extends AppCreative> List<T> placeAds(List<T> originList, String listId, boolean isOnlyOnePage, int pageOffset, CachedRemoteCreative<T> manager) {
        return com.klauncher.biddingos.AdHelper.placeAds(originList, listId, isOnlyOnePage, pageOffset, manager);
    }

    public <T extends AppCreative> List<T> placeAds(List<T> originList, String listId, boolean isOnlyOnePage, int pageOffset, List<Placement> placementList, String kw, CachedRemoteCreative<T> manager) {
        return com.klauncher.biddingos.AdHelper.placeAds(originList, listId, isOnlyOnePage, pageOffset, placementList, kw, manager);
    }

    public void notifyImpression(String listId, String creativeId) {
        com.klauncher.biddingos.AdHelper.notifyImpression(listId, creativeId);
    }

    public void notifyClick(String listId, String creativeId) {
        com.klauncher.biddingos.AdHelper.notifyClick(listId, creativeId);
    }

    public void notifyConversion(String creativeId) {
        com.klauncher.biddingos.AdHelper.notifyConversion(creativeId);
    }
    /**
     * 91已经接入，不能去掉
     */
    @Override
    public void destroy() {
        com.klauncher.biddingos.AdHelper.destroy();
        Log.i(TAG, "AdHelper destroy done.");
    }
}
