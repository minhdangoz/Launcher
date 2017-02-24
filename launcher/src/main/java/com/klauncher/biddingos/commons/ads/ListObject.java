package com.klauncher.biddingos.commons.ads;


import com.klauncher.biddingos.commons.analytics.EventHelper;
import com.klauncher.biddingos.commons.analytics.UserEvent;
import com.klauncher.biddingos.commons.utils.AppUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Edward on 2015/11/30.
 */
public class ListObject {

    private static final String TAG = "ListObject";

    //列表标识
    public String listId = "";

    //列表广告位list
    //private List<Placement> placementList = new ArrayList<>();

    //列表广告对象list
    private Map<String,AdInfo> appId_AdInfos = new ConcurrentHashMap<>();

    //列表请求网络状态
    public boolean bRequestStatus = false;

    //列表是否通过listId请求得到的
    private boolean isUsingListId = false;


    public ListObject(String listId, boolean bRequestStatus, boolean isUsingListId) {
        this.listId = listId;
        this.bRequestStatus = bRequestStatus;
        this.isUsingListId = isUsingListId;
    }

    public void updateAdCaches(Map<String,AdInfo> appId_AdInfos) {
        if(null!=appId_AdInfos) {
            this.appId_AdInfos.clear();
            this.appId_AdInfos.putAll(appId_AdInfos);
        }
    }

    public ParamAdInfoData getAppId_Assets() {
        Map<String, String> appId_assets = new HashMap<>();
        Map<Integer, String> pos_appId = new HashMap<>();

        List<String> appList = AppUtils.getAllPackage();
        for(Map.Entry<String, AdInfo> entry : appId_AdInfos.entrySet()) {
            String appId = entry.getKey();
            AdInfo adInfo = entry.getValue();

            //本地去重
            String pkgName = adInfo.getPackageName();
            if(appList.contains(pkgName)) {
                LogUtils.i(TAG, "remove app: " + appId + " pkgName:" + pkgName);
                continue;
            }

            String asset = adInfo.getAssets();
            if(null!=asset && ""!=asset && 0<=adInfo.getPosition()) {
                appId_assets.put(appId, asset);
                pos_appId.put(adInfo.getPosition(), appId);
            }
        }
        return new ParamAdInfoData(appId_assets, pos_appId);
    }

    //上报请求事件
    public void notifyAdsRequestEvent() {

        String target="";
        String zones="";

        boolean bRun = false;
        
        for(Map.Entry<String,AdInfo> entry : appId_AdInfos.entrySet()) {
            AdInfo adInfo = entry.getValue();
            if(null==adInfo) continue;
            target += (!bRun?"":"|")+listId+"."+adInfo.getPosition();
            zones += (!bRun?"":"|")+adInfo.getZoneid();
            bRun = true;
        }

        EventHelper.postAdsEvent(UserEvent.EVENT_ADS_REQUEST, target, zones);
    }


    public boolean isUsingListId() {
        return isUsingListId;
    }

    public void setRequestStatus(boolean bRequestStatus) {
        this.bRequestStatus = bRequestStatus;
    }

    public String getListId() {
        return listId;
    }

    public boolean isbRequestOK() {
        return bRequestStatus;
    }

    public AdInfo searchAdInfoByAppId(String creativeId) {
        return appId_AdInfos.get(creativeId);
    }


    public class ParamAdInfoData {
        Map<String, String> appId_assets;
        Map<Integer, String> pos_appId;

        public ParamAdInfoData(Map<String, String> appId_assets, Map<Integer, String> pos_appId) {
            this.appId_assets = appId_assets;
            this.pos_appId = pos_appId;
        }

        public Map<String, String> getAppId_assets() {
            return appId_assets;
        }

        public Map<Integer, String> getPos_appId() {
            return pos_appId;
        }
    }

}
