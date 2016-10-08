package com.klauncher.biddingos.commons.analytics;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.cache.SharedPreferencesUtils;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpResponseStatus;
import com.klauncher.biddingos.commons.net.HttpUtils;
import com.klauncher.biddingos.commons.utils.AESUtils;
import com.klauncher.biddingos.commons.utils.AppUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.commons.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 事件辅助类，用于上报相关事件
 */
public class EventHelper {
    private static final String TAG = "EventHelper";
    //线程锁
    private static Object lockUpload = new Object();
    public static UserEvent localEvent(String role, String type, String target, long duration, String extra) {

        PackageInfo packageInfo = AppUtils.getPackageInfoByPackageName(Setting.context.getPackageName());
        String versionCode = null!=packageInfo? String.valueOf(packageInfo.versionCode):"";
        String versionName = null!=packageInfo? String.valueOf(packageInfo.versionName):"";

        UserContext userContext = new UserContext(
                Setting.context.getPackageName(),
                role,
                AppUtils.getTopActivity(),
                versionName,
                versionCode
        );
        UserEvent event = new UserEvent(
                userContext,
                type,
                target,
                duration,
                extra,
                new Date()
        );

        return event;
    }

    /**
     * *全量上报
     * 异步处理
     */
    public static void postEventFullPkg() {
        List<String> listPackage = AppUtils.getAllPackage();
        String extra = StringUtils.join(listPackage, "|");
        UserEvent userEvent = EventHelper.localEvent("", UserEvent.EVENT_APPSCAN_APPLIST, "", 0, extra);


        SharedPreferences.Editor editor = SharedPreferencesUtils.pkg.edit();
        editor.putString(SharedPreferencesUtils.KEY_PACKAGE_LIST, extra);
        editor.commit();

        tryUploadEvent(userEvent, UserEvent.EVENT_APPSCAN_APPLIST);

    }

    /**
     * *对比上次全量上报列表，增量上报
     * 异步处理
     */
    public static void postEventIncrementalPkg() {
        String spStrPackageList = SharedPreferencesUtils.pkg.getString(SharedPreferencesUtils.KEY_PACKAGE_LIST, "");
        if (""==spStrPackageList) return;
        List<String> lastPackageList = StringUtils.StringToList(spStrPackageList, "\\|");
        List<String> localPackageList = AppUtils.getAllPackage();

        List<String> newInstalledList = new ArrayList<>();//新安装列表
        for (String strNode:localPackageList) {
            if(lastPackageList.indexOf(strNode) >= 0) {
            }else {
                newInstalledList.add(strNode);
            }
        }

        List<String> newUninstalledList = new ArrayList<>();//卸载列表
        for (String strNode:lastPackageList) {
            if(localPackageList.indexOf(strNode) < 0) {
                newUninstalledList.add(strNode);
            }
        }
        LogUtils.d(TAG, "new installed app size:" + String.valueOf(newInstalledList.size()));
        LogUtils.d(TAG, "new uninstalled app size:" + String.valueOf(newUninstalledList.size()));
        postEventIncrementalPkg(newInstalledList, UserEvent.EVENT_APPSCAN_INSTALLED);
        postEventIncrementalPkg(newUninstalledList, UserEvent.EVENT_APPSCAN_UNINSTALLED);
    }


    /**
     * *增量安装应用上报
     * 异步处理
     */
    public static void postEventIncrementalPkg(final List<String> listPackage, final String type) {
        if(listPackage.isEmpty()) return;
        UserEvent userEvent = EventHelper.localEvent("", type, "", 0, StringUtils.join(listPackage, "|"));


        if(!SharedPreferencesUtils.isPackageListEmpty()) {
            if(type.equals(UserEvent.EVENT_APPSCAN_INSTALLED)) {
                SharedPreferencesUtils.addPackageList(listPackage);
            }
            else if(type.equals(UserEvent.EVENT_APPSCAN_UNINSTALLED)) {
                SharedPreferencesUtils.removePackageList(listPackage);
            }
        }
        tryUploadEvent(userEvent, type);
    }


    /**
     * 上报投放事件
     */
    public static void postAdsEvent(final String type, String target, String extra) {

        UserEvent userEvent = EventHelper.localEvent("", type, target, 0, extra);
        LogUtils.i(TAG,"postAdsEvent to " + target+" extra="+extra);
        tryUploadEvent(userEvent, type);
    }


    /**
     * 发送事件消息
     */
    public static void tryUploadEvent(final UserEvent userEvent, final String type) {

        new Thread() {
            @Override
            public void run() {

                //保存发送的事件消息
                if(null!=userEvent)
                    saveMessageToPost(userEvent);

                //判断是否wifi环境，如是 发送消息
                //否则判断是否超过24小时，如是则强制发送
                //发送消息成功则清空消息
                boolean bPostMsg = false;
                if(AppUtils.checkWifiOpened()) {
                    bPostMsg = true;
                }else {
                    if(24<=getLastTimePost()) {
                        bPostMsg = true;
                    }
                }
                if(!bPostMsg) {
                    LogUtils.d(TAG, "Not to post msg...");
                    return;
                }

                synchronized (lockUpload) {
                    String spEvents = SharedPreferencesUtils.events.getString(SharedPreferencesUtils.KEY_EVENTS, "");
                    LogUtils.i(TAG, "==========thread sync run====>"+type+" with data:"+spEvents);
                    if("".equals(spEvents)) return;
                    //加密，上传
                    spEvents = AESUtils.encode(Setting.SECRET, spEvents);
                    HttpResponse httpResponse = null;
                    try {
                        httpResponse = HttpUtils.execute(new HttpRequest(HttpRequest.Method.POST, Setting.getBiddingOS_USER_INFO_TRACK_URL(), spEvents));
                    } catch (Exception e) {
                        LogUtils.e(TAG, "", e);
                    }
                    if (null!=httpResponse && HttpResponseStatus.Code.OK == httpResponse.getRespCode()) {
                        LogUtils.d(TAG, "Post [" + type + "] Event Successfully.");
                        //清空 && 更新上次发送时间
                        SharedPreferences.Editor editor = SharedPreferencesUtils.events.edit();
                        editor.putString(SharedPreferencesUtils.KEY_EVENTS, "");
                        editor.putLong(SharedPreferencesUtils.KEY_LAST_TIME_POST, new Date().getTime());
                        editor.commit();
                    } else {
                        LogUtils.d(TAG, "Post [" + type + "] Event Failed...");
                    }
                }
            }
        }.start();

    }


    /**
     * 消息合并及保存:生成最终要发送的消息
     */
    public static void saveMessageToPost(UserEvent userEvent) {
        JSONObject jsonEvent = userEvent.toJson();
        JSONArray jsonEventArray = new JSONArray();
        jsonEventArray.put(jsonEvent);

        //生成事件消息
        JSONObject jsonMessage = messageGenerate(jsonEventArray);


        synchronized (lockUpload) {
            //读取历史事件
            String spEvents = SharedPreferencesUtils.events.getString(SharedPreferencesUtils.KEY_EVENTS, "");
            LogUtils.d(TAG, "messageToPost[spEvnts]:" + spEvents);
            if ("" != spEvents) {
                try {
                    JSONObject jsonSpEventObject = new JSONObject(spEvents);
                    JSONArray ja = jsonSpEventObject.getJSONArray("events");
                    if (null != ja) {
                        ja.put(jsonEvent);
                        jsonMessage = messageGenerate(ja);
                    }
                } catch (JSONException e) {
                    LogUtils.i(TAG, "", e);
                }
            }
            {
                LogUtils.i(TAG, "update sp with msg:" + jsonMessage.toString());
                //更新历史消息
                SharedPreferences.Editor editor = SharedPreferencesUtils.events.edit();
                editor.putString(SharedPreferencesUtils.KEY_EVENTS, jsonMessage.toString());
                editor.commit();
            }
        }

    }

    /**
     * 消息生成
     */
    private static JSONObject messageGenerate(JSONArray jsonEventArray) {
        JSONObject jsonMessage = new JSONObject();
        JSONArray uids = AppUtils.getUserUniqueID();
        try {
            jsonMessage.put("uids", uids);
            jsonMessage.put("events", jsonEventArray);
        }catch (JSONException e) {
            LogUtils.e(TAG, e.toString());
        }
        return jsonMessage;
    }

    public static double getLastTimePost() {
        Long lastTimestamp = SharedPreferencesUtils.events.getLong(SharedPreferencesUtils.KEY_LAST_TIME_POST, 0L);
        Date date = new Date();
        Long nowTimestamp = date.getTime();
        double hour = (double)(nowTimestamp - lastTimestamp)/3600/1000;
        LogUtils.d(TAG, "Time Span for Hour: "+ String.valueOf(hour));
        return hour;
    }

    /**
     * 直接发送消息
     */
    public static void sendSpMessage() {
        LogUtils.d(TAG, "sendSpMessage directly");
        tryUploadEvent(null, "sendSpMessage");
    }


}
