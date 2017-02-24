package com.klauncher.biddingos.commons.cache;


import com.klauncher.biddingos.commons.ads.AdInfo;
import com.klauncher.biddingos.commons.ads.ListObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Edward on 2015/12/1.
 */
public class ListObjectCache {
    private static final String TAG = "ListObjectCache";
    private static ListObjectCache listObjectCache = null;
    private static Map<String, ListObject> listId_listObject = new ConcurrentHashMap<>();

    public static synchronized ListObjectCache shareInstance() {
        if (null == listObjectCache) {
            listObjectCache = new ListObjectCache();
        }
        return listObjectCache;
    }


    public synchronized void clearAll()
    {
        listId_listObject.clear();
    }

    public synchronized void setListObject(ListObject listObject) {
        if(null!=listObject) {
            listId_listObject.put(listObject.getListId(), listObject);
        }
    }

    public synchronized ListObject getListObjectFromList(String listId) {
        return listId_listObject.get(listId);
    }

    public AdInfo searchAdInfoInListByAppId(String listId, String creativeId)
    {
        ListObject listObject = listId_listObject.get(listId);
        if(null!=listObject){
            AdInfo adInfo = listObject.searchAdInfoByAppId(creativeId);
            if(null!=adInfo && null!=adInfo.getCreativeid()) {
                return adInfo;
            }
        }
        return null;
    }

    public void setNotifyImpressionToAppInList(String listId, String appId, boolean bStatus) {
        AdInfo adInfo = this.searchAdInfoInListByAppId(listId, appId);
        if(null!=adInfo) {
            adInfo.setNotifyimpression(bStatus);
        }
    }

    public void setNotifyClickToAppInList(String listId, String appId, boolean bStatus) {
        AdInfo adInfo = this.searchAdInfoInListByAppId(listId, appId);
        if(null!=adInfo) {
            adInfo.setNotifyclick(bStatus);
        }
    }

}
