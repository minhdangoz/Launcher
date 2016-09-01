package com.klauncher.biddingos.commons.ads;


import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpResponseStatus;
import com.klauncher.biddingos.commons.net.HttpUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端缓存远程素材管理器
 * <br><br>
 * 实现客户端缓存，定期访问远程素材库更新本地素材。<br>
 * <font color='red'>子类必须继承 fetchAllCreatives（方式一）
 * 或 fetchCreativesByIdList（方式二） 其中之一</font>
 */
public class CachedRemoteCreativeMgr {
    private static final String TAG = "CachedRemoteCreativeMgr";


    /*
    private CachedRemoteCreative cachedRemoteCreative = null;


    public CachedRemoteCreativeMgr(CachedRemoteCreative cachedRemoteCreative) {
        this.cachedRemoteCreative = cachedRemoteCreative;
    }*/

//    public final <T extends Creative> T getCreative(String s_creativeId, String assets, boolean bRequestAd) {
//        LogUtils.i(TAG, "getCreative:"+s_creativeId+" assets:"+assets);
//
//        if (null == s_creativeId || "".equals(s_creativeId)) {
//            return null;
//        }
//        T t_creative = null;
//        Class<T> realCreativeClass = getRealCreativeClass();
//        CacheManager cacheManager = CacheManager.getInstance();
//
//        /* 因鼎开在fetchCreativesByIdList里使用了AdHelper.getDownloadUrl方法，导致
//        fetchAllCreatives 无法获取还没请求过广告的下载地址、包名、包大小等信息，
//        所以无法实现：一次性获得全部的素材。
//        处理方案：不异步去获得全部素材
//        --------------------------------------------
//        //如果缓存是空则更新全部素材
//        if (0 >= cachedCreativeMap.size()) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Map<String, T> map_creativeid_T = null;
//                    try {
//                        LogUtils.i(TAG, "new thread to fetchAllCreatives");
//                        map_creativeid_T = fetchAllCreatives();
//                        LogUtils.i(TAG, "fetchAllCreatives:"+map_creativeid_T.toString());
//                    } catch (Exception e) {
//                        LogUtils.e(TAG, "", e);
//                    }
//                    if (null != map_creativeid_T && 0 < map_creativeid_T.size()) {
//                        insertCachesAndDatebase(map_creativeid_T);
//                    }
//                }
//            }).start();
//        } else {
//            t_creative = (T)cachedCreativeMap.get(s_creativeId);
//        }
//        --------------------------------------------
//        */
//
//        t_creative = (T)cachedCreativeMap.get(s_creativeId);
//
//        if(null!=t_creative) {
//            LogUtils.i(TAG, "return t_creative:"+t_creative.toString());
//            return t_creative;
//        }
//
//        List<String> list_creativeIds = new ArrayList<>();
//        list_creativeIds.add(s_creativeId);
//
//        if(!bRequestAd) {
//            //从数据库中读取
//            BiddingOSDBHelper.DataObj dataObj_creative = cacheManager.searchCreativeCachesByCreative(s_creativeId);
//            if (null != dataObj_creative) {
//                try {
//                    t_creative = realCreativeClass.newInstance();
//                    LogUtils.i(TAG, "realCreativeClass:"+t_creative.toString());
//                    t_creative.setContent(dataObj_creative.getAssets());
//                    cachedCreativeMap.put(s_creativeId, t_creative);
//                } catch (Exception e) {
//                    LogUtils.e(TAG, "", e);
//                    cacheManager.deleteCreativeCachesByCreative(s_creativeId);
//                    return null;
//                }
//            }
//        }
//        else {
//            //请求资源并返回
//            Map<String, T> map_creativeid_T = null;
//            try {
//                //map_creativeid_T = fetchCreativesByIdList(list_creativeIds);
//                map_creativeid_T = cachedRemoteCreative.fetchCreativesByIdList(list_creativeIds);
//                LogUtils.i(TAG, "fetchCreativesByIdList1:"+list_creativeIds.toString());
//            } catch (Exception e) {
//                LogUtils.e(TAG, "", e);
//                return null;
//            }
//            if (null != map_creativeid_T && 0 < map_creativeid_T.size() && null != map_creativeid_T.get(s_creativeId)) {
//                t_creative = map_creativeid_T.get(s_creativeId);
//                //传入广告素材，更新app对象
//                t_creative.setExtra(assets);
//                insertCachesAndDatebase(map_creativeid_T);
//            }
//        }
//        return t_creative;
//    }


    /**
     * 取得所有的广告素材ID
     * <font color='red'>注意：由于是网络请求请在线程中调用！</font>
     * @param nType 1:appid 2:package
     * @return 素材ID列表
     */
    public static List<String> fetchAllCampaigns(int nType,boolean isFlow) {
        List<String> list_creativeids = new ArrayList<>();
        String url = Setting.getBiddingOS_CAMPAIGN_URL(nType, isFlow);
        // 先调用BiddingOS_CAMPAIGN_URL获取投放应用ID列表
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtils.execute(new HttpRequest(url));
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
        }

        if (null != httpResponse && HttpResponseStatus.Code.OK == httpResponse.getRespCode()) {
            JSONArray jsonArray_campaigns;
            try {
                jsonArray_campaigns = new JSONArray(httpResponse.getRespBody(false));
                if (null != jsonArray_campaigns) {
                    for (int i = 0; i < jsonArray_campaigns.length(); i++) {
                        list_creativeids.add(jsonArray_campaigns.get(i).toString());
                    }
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "", e);
            }
        }
        return list_creativeids;
    }

}
