package com.alpsdroid.ads;

import android.content.Context;

import java.util.List;
import java.util.Map;

/**
 * Created by Edward on 2015/11/26.
 */
public interface AdHelper {

    public void init(Context context, String mid, String afid, String secret, String kid, boolean debuggable);

    public <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId, CachedRemoteCreative<T> manager);
    public <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId, String kw, CachedRemoteCreative<T> manager);

    public <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId, List<Placement> placementList, String kw, CachedRemoteCreative<T> manager);

    public <T extends AppCreative> List<T> placeAds(List<T> originList, String listId, boolean isOnlyOnePage, int pageOffset, CachedRemoteCreative<T> manager);

    public <T extends AppCreative> List<T> placeAds(List<T> originList, String listId, boolean isOnlyOnePage, int pageOffset, List<Placement> placementList, String kw, CachedRemoteCreative<T> manager);

    public void notifyImpression(String listId, String creativeId);

    public void notifyClick(String listId, String creativeId);

    public void notifyConversion(String creativeId);

    public void destroy();
    
}
