
package com.klauncher.biddingos;

import android.content.Context;
import android.util.Log;

import com.alpsdroid.ads.AppCreative;
import com.alpsdroid.ads.CachedRemoteCreative;
import com.alpsdroid.ads.Placement;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.ads.AdInfo;
import com.klauncher.biddingos.commons.ads.AdsRequestObject;
import com.klauncher.biddingos.commons.ads.ListObject;
import com.klauncher.biddingos.commons.analytics.EventHelper;
import com.klauncher.biddingos.commons.analytics.UserEvent;
import com.klauncher.biddingos.commons.cache.CacheManager;
import com.klauncher.biddingos.commons.cache.ListObjectCache;
import com.klauncher.biddingos.commons.cache.OriginCache;
import com.klauncher.biddingos.commons.cache.ZoneCache;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpResponseStatus;
import com.klauncher.biddingos.commons.net.HttpUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 提供接入原生广告的相关辅助方法
 */
public class AdHelper {

    private static final String TAG = "AdHelper";

    /**
     * 初始化SDK
     * 91已经接入，不能去掉
     * @param context    设备上下文
     * @param mid        媒体商代理ID
     * @param afid       媒体商ID
     * @param secret     密钥
     * @param kid        秘钥ID
     * @param debuggable 是否开启调试模式（开启后会输出全部日志且上报地址为测试地址，不会扣费）
     */
    public static void init(Context context, String mid, String afid, String secret, String kid, boolean debuggable) {
        Setting.init(context, mid, afid, secret, kid, debuggable);
        if(isInitDone()) {
            Log.i(TAG, "AdHelper init has been luanch");
            return;
        }
        //设置初始化成功
        setInitDone();
    }
    /**
     * 是否已经初始化
     */
    public static boolean bInit = false;

    public static synchronized boolean isInitDone() {
        return bInit;
    }
    public static synchronized void setInitDone() {
        bInit = true;
    }

    /**
     * 请求ListId对应的广告
     * <br><br>
     * <font color='red'>注意：该方法不能在主线程中执行！</font>
     * @param pageOffset    原始内容列表在展示区域内的偏移，即分页的起始位置对应的绝对位置从0开始
     * @param listId 列表id-唯一标识, 在BiddingOS后台建立的的榜单Id
     * @param manager       媒体商实现的素材管理器
     * @param <T>  继承AppCreative的应用信息类
     * @return 广告展示的内容列表《广告位在列表中的绝对位置,广告》
     */
    public static <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId, CachedRemoteCreative<T> manager) {
        LogUtils.d(TAG, "call getAds: listId " + listId);
        return getAds(pageOffset, listId, null, "", manager, true);
    }
    public static <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId,String kw, CachedRemoteCreative<T> manager) {
        LogUtils.d(TAG, "call getAds: listId "+listId);
        return getAds(pageOffset, listId, null, kw, manager, true);
    }

    /**
     * 请求placementList对应的广告
     * <br><br>
     * <font color='red'>注意：该方法不能在主线程中执行！</font>
     * @param pageOffset    原始内容列表在展示区域内的偏移，即分页的起始位置对应的绝对位置从0开始
     * @param listId 列表id-唯一标识
     * @param placementList 广告位置列表
     * @param kw            搜索关键词，用于搜索广告位
     * @param manager       媒体商实现的素材管理器
     * @param <T>  继承AppCreative的应用信息类
     * @return 广告展示的内容列表《广告位在列表中的绝对位置,广告》
     */
    public static <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId, List<Placement> placementList, String kw, CachedRemoteCreative<T> manager) {
        LogUtils.d(TAG, "call getAds: listId "+listId);
        return getAds(pageOffset, listId, placementList, kw, manager, false);
    }

    /**
     * 请求placementList对应的广告
     * <br><br>
     * <font color='red'>注意：该方法不能在主线程中执行！</font>
     * @param pageOffset    原始内容列表在展示区域内的偏移，即分页的起始位置对应的绝对位置从0开始
     * @param listId 列表id-唯一标识
     * @param placementList 广告位置列表
     * @param kw            搜索关键词，用于搜索广告位
     * @param manager       媒体商实现的素材管理器
     * @param <T>  继承AppCreative的应用信息类
     * @param bUsingListId  表示是否使用ListType/ListId 来请求广告
     * @return 广告展示的内容列表《广告位在列表中的绝对位置,广告》
     */
     private static <T extends AppCreative> Map<Integer, T> getAds(int pageOffset, String listId, List<Placement> placementList, String kw, CachedRemoteCreative<T> manager, Boolean bUsingListId) {
        Map<Integer, T> listAd = new HashMap<>();

         if(!bUsingListId) {//直接使用zoneid，需要检查参数
             if (null == placementList) {
                 LogUtils.e(TAG, "getAds() placementList is null");
                 return listAd;
             }

             if (0 >= placementList.size()) {
                 LogUtils.e(TAG, "getAds() placementList size is empty");
                 return listAd;
             }
         }

        if (null == manager) {
            LogUtils.e(TAG, "getAds() manager is null");
            return listAd;
        }
        //位置为页面起始位置
        if (0 == pageOffset) {
            LogUtils.i(TAG, "getAds() request new adinfo.");

            //请求广告
            AdsRequestObject adsRequest = new AdsRequestObject(AdsRequestObject.ADHELPER, listId, placementList, kw, null, bUsingListId);
            requstAdInfos(listId, adsRequest); //从网络请求某列表的广告，生成关于这份列表的对象

            //上报请求事件
            ListObject listObject = ListObjectCache.shareInstance().getListObjectFromList(listId);
            if(null!=listObject) {
                listObject.notifyAdsRequestEvent();
            }

        }

        return getAdInfosFromMedia(listId, manager);
    }

    /**
     * 根据地址请求广告并添加进CacheManager中缓存
     *
     * @param adsRequest 广告请求对象
     * @return ListObject
     */
    private static void requstAdInfos(String listId, AdsRequestObject adsRequest) {
        boolean bRequestSuccess;
        HttpResponse httpResponse = null;
        try {
            //POST 请求
            String body = adsRequest.getSignedAndEncryptString();
            httpResponse = HttpUtils.execute(new HttpRequest(HttpRequest.Method.POST, adsRequest.getUrl(), body));
            if (null!=httpResponse && HttpResponseStatus.Code.OK==httpResponse.getRespCode()) {
                bRequestSuccess = true;
            }else {
                bRequestSuccess = false;
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "", e);
            bRequestSuccess = false;
        }

        //如果网络请求成功，先刷新数据库,再解析，否则从数据库取数据解析； 以上步骤的到解析的数据存入内存缓存。
        String delivery = "";
        if(bRequestSuccess) {
            //以密文形式保存到数据库
            LogUtils.d(TAG, "load ad Succeed.");
//            CacheManager.getInstance().updateDBCache(listId, httpResponse.getRespBody(false));
            delivery = httpResponse.getRespBody(false);
        }else {
            LogUtils.d(TAG, "load ad failed.");
//            delivery = CacheManager.getInstance().getDataFromDBCache(listId);
        }

        LogUtils.e(TAG, delivery);
        Log.e("wqh_adinfo_demo", "delivery:\n" + delivery);

        //解析投放数据，然后刷新内存缓存
        Map<String, AdInfo> appId_AdInfo = AdInfo.parseJSONString(delivery, true, 2, adsRequest.placementList, adsRequest.isUsingListId());
        CacheManager.getInstance().updateRamCache(listId, appId_AdInfo, bRequestSuccess, adsRequest.isUsingListId());

    }


    /**
     * 从CacheManager中读取对应广告位的广告
     *
     * @param manager       媒体商实现的素材管理器
     * @param <T> 继承AppCreative的应用信息类
     * @return
     */
    private static <T extends AppCreative> Map<Integer, T> getAdInfosFromMedia(String listId, CachedRemoteCreative<T> manager ) {

        Map<Integer, T> pos_T = new HashMap<>();
        ListObject listObject = ListObjectCache.shareInstance().getListObjectFromList(listId);
        if(null==listObject) {
            return pos_T;
        }

        ListObject.ParamAdInfoData paramAdInfoData = listObject.getAppId_Assets();
        Map<String, T> appId_T = manager.generateAppInfo(paramAdInfoData.getAppId_assets());
        Map<Integer, String> pos_appId = paramAdInfoData.getPos_appId();

        for(Map.Entry<Integer, String> entry:pos_appId.entrySet()) {
            int pos = entry.getKey();
            String appId = entry.getValue();
            T t = appId_T.get(appId);
            if(null!=t) {
                pos_T.put(pos, t);
            }
        }

        return pos_T;
    }


    /**
     * 找到listId对应的广告，替换相同位置的应用，并去掉重复包名的应用
     * <br><br>
     * <font color='red'>注意：该方法不能在主线程中执行！</font>
     *
     * @param originList    原始内容列表
     * @param listId 列表id-唯一标识
     * @param isOnlyOnePage 是否只有一页，无需翻页
     * @param pageOffset    原始内容列表在展示区域内的偏移，即分页的起始位置对应的绝对位置从0开始
     * @param manager       媒体商实现的素材管理器
     * @param <T> 继承AppCreative的应用信息类
     * @return 处理去重后的带广告展示的内容列表并保证顺序不会变化，,广告将替换相同位置的对象
     */
    public static <T extends AppCreative> List<T> placeAds(List<T> originList, String listId, boolean isOnlyOnePage, int pageOffset, CachedRemoteCreative<T> manager) {
        LogUtils.d(TAG, "call placeAds 1 listId: "+listId + " beOnlyOnePage:"+isOnlyOnePage);
        return placeAds(originList, listId, isOnlyOnePage, pageOffset, null, "", manager, true);
    }

    /**
     * 找到placementList对应的广告，替换相同位置的应用，并去掉重复包名的应用
     * <br><br>
     * <font color='red'>注意：该方法不能在主线程中执行！</font>
     *
     * @param originList    原始内容列表
     * @param listId 列表id-唯一标识
     * @param isOnlyOnePage 是否只有一页，无需翻页
     * @param pageOffset    原始内容列表在展示区域内的偏移，即分页的起始位置对应的绝对位置从0开始
     * @param placementList 广告位置列表
     * @param kw             搜索关键词，用于搜索广告位
     * @param manager       媒体商实现的素材管理器
     * @param <T> 继承AppCreative的应用信息类
     * @return 处理去重后的带广告展示的内容列表并保证顺序不会变化，,广告将替换相同位置的对象
     */
    public static <T extends AppCreative> List<T> placeAds(List<T> originList, String listId, boolean isOnlyOnePage, int pageOffset, List<Placement> placementList, String kw, CachedRemoteCreative<T> manager) {
        LogUtils.d(TAG, "call placeAds 2 listId: "+listId + " beOnlyOnePage:"+isOnlyOnePage);
        return placeAds(originList, listId, isOnlyOnePage, pageOffset, placementList, kw, manager, false);
    }

    private static <T extends AppCreative> List<T> placeAds(List<T> originList, String listId, boolean isOnlyOnePage, int pageOffset, List<Placement> placementList, String kw, CachedRemoteCreative<T> manager, Boolean bUsingListId) {


        if (null == originList) {
            LogUtils.e(TAG, "placeAds() originList is null");
            return originList;
        }

        if (0 == originList.size()) {
            LogUtils.e(TAG, "placeAds() originList is empty");
            return originList;
        }

        Map<Integer, T> appAdList = null;

        //使用listId
        if(bUsingListId) {
            appAdList = getAds(pageOffset, listId, manager);
        }else {
            appAdList = getAds(pageOffset, listId, placementList, kw, manager);
        }

        if (0 >= appAdList.size()) {
            LogUtils.e(TAG, "placeAds() appAdList is empty");
            return originList;
        }

        List<T> targetList = null;

        if (isOnlyOnePage) {
            targetList = onePageDeal(originList, appAdList, pageOffset);
        }else  {
            targetList = multiPagesDeal(originList, listId, appAdList, pageOffset);
        }

        if(null==targetList || 0>=targetList.size()) {
            return originList;
        }

        return targetList;

    }


    private static <T extends AppCreative> List<T> onePageDeal(List<T> originList, Map<Integer, T> appAdList, int pageOffset) {

        Object[] targetArry = new Object[originList.size()];

        ArrayList tempOrigin = new ArrayList(); //保存广告位置对应的自然应用（用于填补）

        Iterator<Integer> i_Pos = appAdList.keySet().iterator();
        while (i_Pos.hasNext()) {
            int pos = i_Pos.next();

            //分页的位置
            int posOffset = pos - pageOffset;
            //插入广告
            if (null != appAdList.get(pos) && 0 <= posOffset && originList.size() > posOffset) {
                targetArry[posOffset] = appAdList.get(pos);

                //判断是否需要保存，保存的不能和广告重复
                boolean bConflict = false;
                for (Map.Entry<Integer, T> entry : appAdList.entrySet()) {
                    if(null != entry.getValue()) {
                        T t = entry.getValue();
                        if (null != originList.get(posOffset).getPackageName() && null != t.getPackageName()) {
                            if (originList.get(posOffset).getPackageName().equals(t.getPackageName())) {
                                bConflict = true;
                                break;
                            }
                        }
                    }
                }
                if(bConflict==false) {
                    //保存到临时数组
                    tempOrigin.add(originList.get(posOffset));
                }

            }
        }

        for (int i = 0; i < originList.size(); i++) {
            //过滤掉位置与广告冲突的应用
            if (null != targetArry[i]) {
                continue;
            }

            //查找是否已存在相同的包名 ，如已存在则不插入
            boolean bConflict = false;
            for (Map.Entry<Integer, T> entry : appAdList.entrySet()) {
                if(null != entry.getValue()) {
                    T t = entry.getValue();
                    if (null != originList.get(i).getPackageName() && null != t.getPackageName()) {
                        if (originList.get(i).getPackageName().equals(t.getPackageName())) {
                            bConflict = true;
                            break;
                        }
                    }
                }
            }

            if (false == bConflict) {
                targetArry[i] = originList.get(i);
            }
        }

        int tempOrigin_pos = 0;

        List<T> targetList = new ArrayList<>();
        for (int i = 0; i < targetArry.length; i++) {
            if (null != targetArry[i]) {
                targetList.add((T) targetArry[i]);
            }else {
                if(tempOrigin_pos < tempOrigin.size()) {
                    targetList.add((T) tempOrigin.get(tempOrigin_pos++));
                }
            }
        }

        return targetList;

    }


    private static <T extends AppCreative> List<T> multiPagesDeal(List<T> originList, String listId, Map<Integer, T> appAdList, int pageOffset) {

        if(pageOffset==0) {
            ZoneCache.shareInstance().clearAll(listId);
            OriginCache.shareInstance().clearAll(listId);
        }

        Object[] targetArry = new Object[originList.size()];

        Iterator<Integer> i_Pos = appAdList.keySet().iterator();
        while (i_Pos.hasNext()) {
            int pos = i_Pos.next();

            //分页的位置
            int posOffset = pos - pageOffset;
            //插入广告
            if (null != appAdList.get(pos) && 0 <= posOffset && originList.size() > posOffset) {
                T t  = appAdList.get(pos);

                //判断是否此应用pkg是否已经插入了
                if(ZoneCache.shareInstance().isIncludedWithPkg(t.getPackageName(), listId)) {
                    //不插入广告，插入自然列表项
                    targetArry[posOffset] = originList.get(posOffset);
                }else {
                    //插入广告
                    targetArry[posOffset] = appAdList.get(pos);

                    //判断是否需要缓存，缓存的不能和广告重复
                    boolean bConflict = false;
                    for (Map.Entry<Integer, T> entry : appAdList.entrySet()) {
                        if(null != entry.getValue()) {
                            T t2 = entry.getValue();
                            if (null != originList.get(posOffset).getPackageName() && null != t.getPackageName()) {
                                if (originList.get(posOffset).getPackageName().equals(t.getPackageName())) {
                                    bConflict = true;
                                    break;
                                }
                            }
                        }
                    }
                    if(bConflict==false) {
                        //缓存被替换的自然应用
                        OriginCache.shareInstance().insertOriginInfo(originList.get(posOffset), listId);
                    }
                }
            }
        }

        for (int i=0; i<originList.size(); i++) {
            //过滤掉位置与广告冲突的应用
            if(null != targetArry[i]) {
                continue;
            }

            //查找是否已存在相同的包名 ，如已存在则不插入
            boolean bConflict = false;
            for (Map.Entry<Integer, T> entry : appAdList.entrySet()) {
                if(null != entry.getValue()) {
                    T t = entry.getValue();
                    if (null != originList.get(i).getPackageName() && null != t.getPackageName()) {
                        if (originList.get(i).getPackageName().equals(t.getPackageName())) {
                            bConflict = true;
                            //从缓存里读取一个自然应用
                            Object obj = OriginCache.shareInstance().getOneOriginInList(listId);
                            if (null!=obj) {
                                targetArry[i] = obj;
                            }else  {//读取失败，使用原始自然列表
                                targetArry[i] = originList.get(i);

                                //添加此列表位置pkg到ZoneCache
                                ZoneCache.shareInstance().insertPkg(t.getPackageName(), listId);

                            }
                            break;
                        }
                    }
                }
            }


            if(false==bConflict) {
                targetArry[i] = originList.get(i);
            }

        }


        List<T> targetList = new ArrayList<>();
        for (int i = 0; i < targetArry.length; i++) {
            if (null != targetArry[i]) {
                targetList.add((T) targetArry[i]);
            }
        }
        return targetList;

    }



    /**
     * 通知有效展示
     *
     * @param listId 列表Id
     * @param creativeId 素材ID（appID）
     */
    public static void notifyImpression(String listId, String creativeId) {
        LogUtils.d(TAG, "call notifyImpression with listId:"+listId+" appId:"+creativeId);

        AdInfo ad = ListObjectCache.shareInstance().searchAdInfoInListByAppId(listId, creativeId);
        if (null != ad) {
            if (ad.isNotifyimpression()) {
                LogUtils.d(TAG, "已经通知过有效展示 => " + ad.getPackageName());
                return;
            }
            ad.notifyImpression();
            //上报展示事件
            EventHelper.postAdsEvent(UserEvent.EVENT_ADS_IMPRESSION, creativeId, String.valueOf(ad.getZoneid()));
        }

    }

    /**
     * 通知有效点击
     *
     * @param listId 列表Id
     * @param creativeId 素材ID（appID）
     */
    public static void notifyClick(String listId, String creativeId) {
        LogUtils.d(TAG, "call notifyClick with listId:"+listId+" appId:"+creativeId);

        AdInfo ad = ListObjectCache.shareInstance().searchAdInfoInListByAppId(listId, creativeId);
        if (null != ad) {
            if(ad.isNotifyclick()) {
                LogUtils.d(TAG, "已经通知过有效点击 => " + ad.getPackageName());
            }else  {
                ad.notifyClick(listId, -1, false);
                //上报点击事件
                EventHelper.postAdsEvent(UserEvent.EVENT_ADS_DOWNLOAD_START, creativeId, String.valueOf(ad.getZoneid()));
            }
        }

    }

    /**
     * 通知有效转化
     *
     * @param creativeId 素材ID（appID）
     */
    public static void notifyConversion(String creativeId) {
        LogUtils.d(TAG, "call notifyConversion with appId:"+creativeId);
        AdInfo.notifyConversion(creativeId, false);

    }


    /**
     * 回收资源
     */
    public static void destroy() {
        //AppInstallReceiver.unregister();
    }

    //-----以下为我司在其源码基础添加的方法

    /**
     * 读取缓存的AppInfo集合
     * @param listId
     * @return
     */
//    public static List<AppInfo> getCacheAppInfo (String listId) {
//        List<AppInfo> appList = new ArrayList<>();
//        try {
//            // 读取内存缓存
//            Map<Integer,AppInfo> appInfoMap = AdHelper.getAdInfosFromMedia(listId, new CreativeCacheManager());
//            //判断内存缓存是否非为空
//            if (null==appInfoMap||appInfoMap.size()==0) {//内存缓存为空---->尝试读取数据库中的缓存
//                String delivery = CacheManager.getInstance().getDataFromDBCache(listId);
//                Map<String, AdInfo> appId_AdInfo = AdInfo.parseJSONString(delivery, true, 2, null, true);
//                CacheManager.getInstance().updateRamCache(listId, appId_AdInfo, false, true);
//                appList.addAll(AdHelper.getAdInfosFromMedia(listId, new CreativeCacheManager()).values());
//            } else {//内存缓存非空
//                appList.addAll(appInfoMap.values());
//            }
//        } catch (Exception e) {
//            appList.clear();
//            Log.e("Kinflow", "读取缓存AppInfo时,出错");
//        }
//
//        return appList;
//    }
}
