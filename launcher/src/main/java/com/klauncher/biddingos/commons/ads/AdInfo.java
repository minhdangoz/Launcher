package com.klauncher.biddingos.commons.ads;


import android.text.TextUtils;

import com.alpsdroid.ads.Placement;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.analytics.EventHelper;
import com.klauncher.biddingos.commons.analytics.UserEvent;
import com.klauncher.biddingos.commons.cache.SharedPreferencesUtils;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpTask;
import com.klauncher.biddingos.commons.task.TaskCallback;
import com.klauncher.biddingos.commons.task.TaskManager;
import com.klauncher.biddingos.commons.utils.AESUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.commons.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广告对象
 */
public class AdInfo {
    private static final String TAG = "AdInfo";
    public static final String MODE_CPC="cpc";
    public static final String MODE_CPD="cpd";
    public static final String MODE_CPM="cpm";

    private String transactionid = "";
    private String st = ""; //用于lg ck ti post body
    private int zoneid = 0;
    private int position = -1;
    private String creativeid = "";
    private JSONObject assets = null;
    private String logUrl = "";
    private String clickUrl = "";
    private String trackUrl = "";
    private String lastDisplayListId = "";
    private boolean notifyimpression = false;
    private boolean notifyclick = false;
    private boolean notifyconversion = false;
    private String app_package = "";
    private int app_size = 0;
    private String app_checksum = "";
    private int app_version_code = -1;
    private String app_version = "";
    private String app_download_url = "";
    private boolean bIsDownloading = false;
    private String action_text="";

    public String getPay_mode() {
        return pay_mode;
    }

    private String pay_mode="";//计费模式

    public boolean getbIsDownloading() {
        return bIsDownloading;
    }

    public void setbIsDownloading(boolean bIsDownloading) {
        this.bIsDownloading = bIsDownloading;
    }

    public String getLastDisplayListId() {
        return lastDisplayListId;
    }

    public void setLastDisplayListId(String lastDisplayListId) {
        this.lastDisplayListId = lastDisplayListId;
    }

    /**
     * 最后刷新时间
     */
    private Date timestamp = new Date();


    /**
     * 解析从网络/数据库取出的字符串成对象列表
     *
     * @param isUsingListId 若false，则需要更新adInfo的pos，并且placementList不能为空
     * @return type:1 返回apk_adInfo  type:2 返回appId_adInfo
     */
    public static Map<String, AdInfo> parseJSONString(String jsonString, boolean bNeedDecrypt, int type, List<Placement> placementList, boolean isUsingListId) {

        Map<String, AdInfo> key_AdInfo = new HashMap<>();
        if (null == jsonString || "" == jsonString) {
            LogUtils.w(TAG, "Delivery data is empty");
            return key_AdInfo;
        }
        if (bNeedDecrypt) {
            jsonString = AESUtils.decode(Setting.SECRET, jsonString);
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray deliveryArray = jsonObject.getJSONArray("delivery");
            if (null != deliveryArray) {
                for (int i = 0; i < deliveryArray.length(); i++) {
                    try {
                        AdInfo adInfo = new AdInfo(deliveryArray.getJSONObject(i));
                        if (null != adInfo && "" != adInfo.getPackageName() && "" != adInfo.getCreativeid()) {

                            //如果使用placement，需要更新adIsanfo zoneid对应的position
                            if (!isUsingListId && null != placementList) {
                                for (Placement placeMent : placementList) {
                                    if (placeMent.getZoneid() == adInfo.getZoneid()) {
                                        adInfo.setPosition(placeMent.getPos());
                                    }
                                }
                            }

                            if (1 == type) {
                                key_AdInfo.put(adInfo.getPackageName(), adInfo);
                            } else {
                                key_AdInfo.put(adInfo.getCreativeid(), adInfo);
                            }

                        }
                    } catch (Exception e) {
                        LogUtils.w(TAG, "parse AdInfo wrong", e);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "parseJSONString wrong", e);
        }


        return key_AdInfo;
    }

    private void setPosition(int position) {
        this.position = position;
    }


    //对外开放的素材信息
    public String getAssets() {
        String strAssets = "";
        if (null != assets) {
            strAssets = assets.toString();
        }
        return strAssets;
    }


    //解析属性，请勿使用CreativeName类（业务接口层），因为将来素材的增加以及自更新，会导致因
    // 已经接入的媒体商的CreativeName未带有新属性而报错
    public AdInfo(JSONObject jObject) throws IllegalArgumentException, JSONException {
        parsePaySame(jObject);
        if(!TextUtils.isEmpty(pay_mode)&&pay_mode.equals(MODE_CPC)) {
            //cpc计费
            this.action_text="详情";
        }else {
            this.action_text="下载";
            if (TextUtils.isEmpty(trackUrl)) {
                throw new JSONException("trackUrl is empty");
            }

            //因为下载地址从BiddingOS下载，所以必须保证下载正常，以下检查
            if (assets.isNull("app_download_url") || assets.isNull("app_package") || assets.isNull("app_version")
                    || assets.isNull("app_version_code") || assets.isNull("app_checksum") || assets.isNull("app_size")) {
                throw new JSONException("download_info is empty");
            }
            this.app_download_url = assets.getString("app_download_url");
            if (TextUtils.isEmpty(app_download_url)) {
                LogUtils.i("temp", "url is null");
                throw new JSONException("app_download_url is empty");
            }

            this.app_package = assets.getString("app_package");
            if (TextUtils.isEmpty(app_package)) {
                throw new JSONException("app_package is empty");
            }

            this.app_version = assets.getString("app_version");
            if (TextUtils.isEmpty(app_version)) {
                throw new JSONException("app_version is empty");
            }

            this.app_version_code = assets.getInt("app_version_code");
            if (0 > app_version_code) {
                throw new JSONException("app_version_code is wrong");
            }

            this.app_checksum = assets.getString("app_checksum");
            if (TextUtils.isEmpty(app_checksum)) {
                throw new JSONException("app_checksum is empty");
            }

            this.app_size = assets.getInt("app_size");
            if (0 >= app_size) {
                throw new JSONException("app_size is 0");
            }
        }
    }

    private void parsePaySame(JSONObject jObject) throws JSONException {
        this.transactionid = jObject.getString("transactionid");
        this.st = jObject.getString("st");
        this.zoneid = jObject.getInt("zoneid");
        this.position = jObject.getInt("position");
        this.assets = jObject.getJSONObject("assets");
        this.creativeid = assets.getString("app_id");
        this.logUrl = jObject.getString("logUrl");
        this.clickUrl = jObject.getString("clickUrl");
        this.trackUrl = jObject.getString("trackUrl");
        if(!(jObject.isNull("pay_mode"))){
            this.pay_mode=jObject.getString("pay_mode");
        }

        if (0 >= this.zoneid) {
            throw new JSONException("zoneid is negative");
        }
        if (0 > this.position) {
            throw new JSONException("position is negative");
        }
        if (TextUtils.isEmpty(creativeid)) {
            throw new JSONException("creativeid is empty");
        }
        if (TextUtils.isEmpty(logUrl)) {
            throw new JSONException("logUrl is empty");
        }
        if (TextUtils.isEmpty(clickUrl)) {
            throw new JSONException("clickUrl is empty");
        }
        this.timestamp = new Date();
    }

    public static String getStBody(String ts, String st, String transactionid) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("st", st);
            jsonObject.put("transactionid", transactionid);
        } catch (JSONException e) {
            LogUtils.w(TAG, "", e);
        }

        String signedString = StringUtils.signature(jsonObject.toString(), ts);
        String encryptString = AESUtils.encode(Setting.SECRET, signedString);
        return encryptString;
    }

    //通知展示
    public void notifyImpression() {

                LogUtils.d(TAG, "be going to notifyImpression ad pkgName: " + getPackageName());
                String ts = String.valueOf(System.currentTimeMillis() / 1000);

                HttpTask httpTask = new HttpTask(HttpRequest.Method.POST, AdInfo.this.logUrl + "&ts=" + ts, AdInfo.getStBody(ts, getSt(), getTransactionid()), new TaskCallback<HttpRequest, HttpResponse>() {
                    @Override
                    public void onSuccess(HttpRequest input, HttpResponse output) {
                        //设置已展示
                        setNotifyimpression(true);
                        LogUtils.i(TAG, "notifyImpression Success pkgName: " + getPackageName());
                    }

                    @Override
                    public void onFailure(HttpRequest input, Throwable th) {
                        LogUtils.w(TAG, "notifyImpression Failed pkgName: " + getPackageName());
                    }
                });

                TaskManager.getInstance().submitRealTimeTask(httpTask);
    }
//    //通知展示
//    public void notifyImpression(final boolean isFirst) {
//        AdAffairThreadPool.getAffairThreadPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                LogUtils.d(TAG, "be going to notifyImpression ad pkgName: " + getPackageName());
//                //先保存事务
//                if (isFirst) {
//                    BiddingOSDBHelper.getInstance(Setting.context).insterOrUpdateAdAffairCaches(getAdAffair(null));
//                }
//                String ts = String.valueOf(System.currentTimeMillis() / 1000);
//
//                HttpTask httpTask = new HttpTask(HttpRequest.Method.POST, AdInfo.this.logUrl + "&ts=" + ts, AdInfo.getStBody(ts, getSt(), getTransactionid()), new TaskCallback<HttpRequest, HttpResponse>() {
//                    @Override
//                    public void onSuccess(HttpRequest input, HttpResponse output) {
//                        //设置已展示
//                        setNotifyimpression(true);
//                        LogUtils.i(TAG, "notifyImpression Success pkgName: " + getPackageName());
//                    }
//
//                    @Override
//                    public void onFailure(HttpRequest input, Throwable th) {
//                        LogUtils.w(TAG, "notifyImpression Failed pkgName: " + getPackageName());
//                    }
//                });
//
//                TaskManager.getInstance().submitRealTimeTask(httpTask);
//            }
//
//        });
//
//    }
//
//    /**
//     * 获得初始事务对象
//     */
//    private AdAffairBean getAdAffair(String list) {
//        if(TextUtils.isEmpty(transactionid)) {
//            return null;
//        }
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("ti", getTrackUrl());
//            jsonObject.put("position", position);
//            jsonObject.put("st", st);
//            jsonObject.put("zoneid", getZoneid());
//            jsonObject.put("transactionid", getTransactionid());
//        } catch (JSONException e) {
//            LogUtils.e(TAG, "", e);
//        }
//        AdAffairBean adAffairBean=new AdAffairBean();
//        adAffairBean.setTransactionid(transactionid);
//        adAffairBean.setTimestamp(new Date().getTime());
//        adAffairBean.setcontent(jsonObject.toString());
//        adAffairBean.setImpressionhappen(Constants.AFFAIRHAPPEN);
//        adAffairBean.setImpressionstatus(Constants.REQUESTREADY);
//        adAffairBean.setImpressionurl(getLogUrl());
//        adAffairBean.setClickhappen(Constants.AFFAIRNOTHAPPEN);
//        adAffairBean.setClickstatus(Constants.REQUESTREADY);
//        adAffairBean.setClickurl(getClickUrl());
//        adAffairBean.setConversionhappen(Constants.AFFAIRNOTHAPPEN);
//        adAffairBean.setConversionstatus(Constants.REQUESTREADY);
//        adAffairBean.setConversionurl(getTrackUrl());
//        return adAffairBean;
//    }
    /**
     * 通知点击
     *
     * @param listId        列表ID
     * @param position      位置
     * @param bUsingPkgName 是否通过包名来通知点击，否则使用appId
     */
    public void notifyClick(final String listId, final int position, final boolean bUsingPkgName) {

        LogUtils.d(TAG, "be going to notifyClick ad pkgName: " + getPackageName());
        String ts = String.valueOf(System.currentTimeMillis() / 1000);

        HttpTask httpTask = new HttpTask(HttpRequest.Method.POST, this.clickUrl + "&ts=" + ts, AdInfo.getStBody(ts, getSt(), getTransactionid()), new TaskCallback<HttpRequest, HttpResponse>() {
            @Override
            public void onSuccess(HttpRequest input, HttpResponse output) {
                //设置已点击
                setNotifyclick(true);
                LogUtils.i(TAG, "notifyClick Success pkgName =" + getPackageName());

                //缓存ti信息，表明正在下载的应用(存入文件)
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("ti", getTrackUrl());
                    if (null != listId) {
                        jsonObject.put("listId", listId);
                    }
                    jsonObject.put("position", position);
                    jsonObject.put("st", st);
                    jsonObject.put("zoneid", getZoneid());
                    jsonObject.put("transactionid", getTransactionid());
                } catch (JSONException e) {
                    LogUtils.e(TAG, "", e);
                }
                if (bUsingPkgName) {
                    SharedPreferencesUtils.setKeyAdAppDownload(getPackageName(), jsonObject.toString());
                } else {
                    //若是appId 则加上前缀BOS
                    String key = "BOS." + getCreativeid();
                    SharedPreferencesUtils.setKeyAdAppDownload(key, jsonObject.toString());
                }

            }

            @Override
            public void onFailure(HttpRequest input, Throwable th) {
                LogUtils.w(TAG, "notifyClick Failed pkgName =" + getPackageName());
            }
        });

        TaskManager.getInstance().submitRealTimeTask(httpTask);

    }

//    /**
//     * 通知点击
//     *
//     * @param listId        列表ID
//     * @param position      位置
//     * @param isFrist
//     */
//    public void notifyClick(boolean isFrist,final String listId, final int position) {
//
//        LogUtils.d(TAG, "be going to notifyClick ad pkgName: " + getPackageName());
//        String ts = String.valueOf(System.currentTimeMillis() / 1000);
//
//        HttpTask httpTask = new HttpTask(HttpRequest.Method.POST, this.clickUrl + "&ts=" + ts, AdInfo.getStBody(ts, getSt(), getTransactionid()), new TaskCallback<HttpRequest, HttpResponse>() {
//            @Override
//            public void onSuccess(HttpRequest input, HttpResponse output) {
//                //设置已点击
//                setNotifyclick(true);
//                LogUtils.i(TAG, "notifyClick Success pkgName =" + getPackageName());
//
//                //缓存ti信息，表明正在下载的应用(存入文件)
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("ti", getTrackUrl());
//                    if (null != listId) {
//                        jsonObject.put("listId", listId);
//                    }
//                    jsonObject.put("position", position);
//                    jsonObject.put("st", st);
//                    jsonObject.put("zoneid", getZoneid());
//                    jsonObject.put("transactionid", getTransactionid());
//                } catch (JSONException e) {
//                    LogUtils.e(TAG, "", e);
//                }
//
//            }
//
//            @Override
//            public void onFailure(HttpRequest input, Throwable th) {
//                LogUtils.w(TAG, "notifyClick Failed pkgName =" + getPackageName());
//            }
//        });
//
//        TaskManager.getInstance().submitRealTimeTask(httpTask);
//
//    }


    /**
     * 通知转化
     *
     * @param key           标识应用
     * @param bUsingPkgName 是否通过包名来通知转化，否则使用appId
     */
    public static void notifyConversion(String key, boolean bUsingPkgName) {
        String keyStr = "";
        if (!bUsingPkgName) {//若是appId 则加上前缀BOS
            keyStr = "BOS." + key;
        } else {
            keyStr = key;
        }

        final String appKey = keyStr;

        JSONObject jsonObject = SharedPreferencesUtils.getKeyAdAppDownload(appKey);
        if (null == jsonObject) {
            LogUtils.d(TAG, "get adDownload info from file failed.");
            return;
        }
        String trackUrl = "";
        String st = "";
        String listId = "";
        int position = -1;
        int zoneid;
        String transactionid = "";
        try {
            trackUrl = jsonObject.getString("ti");
            st = jsonObject.getString("st");
            listId = jsonObject.getString("listId");
            position = jsonObject.getInt("position");
            zoneid = jsonObject.getInt("zoneid");
            transactionid = jsonObject.getString("transactionid");
        } catch (JSONException e) {
            LogUtils.e(TAG, "cannot parse track data", e);
            SharedPreferencesUtils.deleteAdAppDownload(appKey);
            return;
        }

        if (null == trackUrl || "" == trackUrl) {
            LogUtils.e(TAG, "cannot parse trackUrl data");
            SharedPreferencesUtils.deleteAdAppDownload(appKey);
            return;
        }

        LogUtils.d(TAG, "be going to notifyConversion ad key: " + key);

        String ts = String.valueOf(System.currentTimeMillis() / 1000);

        HttpTask httpTask = new HttpTask(HttpRequest.Method.POST, trackUrl + "&ts=" + ts, AdInfo.getStBody(ts, st, transactionid), new TaskCallback<HttpRequest, HttpResponse>() {
            @Override
            public void onSuccess(HttpRequest input, HttpResponse output) {

                //删除此缓存记录
                SharedPreferencesUtils.deleteAdAppDownload(appKey);
                LogUtils.i(TAG, "notifyConversion Success for app: " + appKey);
            }

            @Override
            public void onFailure(HttpRequest input, Throwable th) {
                LogUtils.w(TAG, "notifyConversion Failed for app: " + appKey);
            }
        });
        TaskManager.getInstance().submitRealTimeTask(httpTask);
        //上报转化事件
        if (bUsingPkgName) {
            EventHelper.postAdsEvent(UserEvent.EVENT_ADS_DOWNLOAD_END, appKey, listId + "." + position);
        } else {
            EventHelper.postAdsEvent(UserEvent.EVENT_ADS_DOWNLOAD_END, appKey, String.valueOf(zoneid));
        }

    }


    public String getPackageName() {
        return this.app_package;
    }

    public int getPosition() {
        return position;
    }

    public int getZoneid() {
        return zoneid;
    }


    public String getCreativeid() {
        return creativeid;
    }

    public void setCreativeid(String creativeid) {
        this.creativeid = creativeid;
    }


    public String getLogUrl() {
        return logUrl;
    }

    public String getClickUrl() {
        return clickUrl;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public String getSt() {
        return st;
    }

    public Date getTimestamp() {
        return timestamp;
    }


    public boolean isNotifyimpression() {
        return notifyimpression;
    }

    public void setNotifyimpression(boolean notifyimpression) {
        this.notifyimpression = notifyimpression;
    }

    public boolean isNotifyclick() {
        return notifyclick;
    }

    public void setNotifyclick(boolean notifyclick) {
        this.notifyclick = notifyclick;
    }

    public boolean isNotifyconversion() {
        return notifyconversion;
    }

    public void setNotifyconversion(boolean notifyconversion) {
        this.notifyconversion = notifyconversion;
    }

    public String getAppDownloadUrl() {
        return app_download_url;
    }

    public String getAppVersionName() {
        return app_version;
    }

    public int getAppVersionCode() {
        return app_version_code;
    }

    public String getAppChecksum() {
        return app_checksum;
    }

    public int getAppSize() {
        return app_size;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public String getAction_text() {
        return action_text;
    }
}
