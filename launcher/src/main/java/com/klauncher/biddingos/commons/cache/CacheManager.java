package com.klauncher.biddingos.commons.cache;


import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.ads.AdInfo;
import com.klauncher.biddingos.commons.ads.ListObject;
import com.klauncher.biddingos.commons.db.BiddingOSDBHelper;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.util.Map;


public class CacheManager {
    private static final String TAG = "CacheManager";
    private static CacheManager mCacheManager = null;
    private static BiddingOSDBHelper mBiddingOSDBHelper;

    public static synchronized CacheManager getInstance() {
        if (null == mCacheManager) {
            mCacheManager = new CacheManager();
        }
        return mCacheManager;
    }

    private CacheManager() {
        mBiddingOSDBHelper = BiddingOSDBHelper.getInstance(Setting.context);

    }

    //*************AdInfo*****************************

    public void updateDBCache(String listId, String cipherText) {
        try {
            mBiddingOSDBHelper.insterOrUpdateAdCaches(listId, cipherText);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
    }

    //刷新内存listId_ListObject
    public void updateRamCache(String listId, Map<String, AdInfo> appId_AdInfo, boolean bRequestStatus, boolean isUsingListId) {

        ListObject listObject = new ListObject(listId, bRequestStatus, isUsingListId);
        if (0 < appId_AdInfo.size()) {
            listObject.updateAdCaches(appId_AdInfo);
        }
        ListObjectCache.shareInstance().setListObject(listObject);
    }

    public String getDataFromDBCache(String listId) {
        try {
            String delivery = mBiddingOSDBHelper.getDeliveryDataByListId(listId);
            if (null!=delivery) {
                return delivery;
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "getDataFromDBCache", e);
        }
        return "";
    }



}
