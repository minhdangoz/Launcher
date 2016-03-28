// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-

package com.klauncher.ping;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.klauncher.ext.LauncherLog;
import com.klauncher.launcher.BuildConfig;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

class PingHandler {

    private static final int MAX_PING_TIMES = 4; // retry 3 times after ping failed

    public interface PingFailCallback {
        void onFailed(List<Map<String, String>> dataMap);
    }

    class LaunchRequestHelper implements JReq.RequestHelper {
        @Override
        public String getRequestURL() {
            return BuildConfig.PING_DOMAIN + "/launcher_api/log/uploadlog";
        }

        @Override
        public String getRequestJsonData() {
            JSONArray postArray = new JSONArray();
            for (Map<String, String> map : mDataMap) {
                JSONObject postData = new JSONObject();
                putGeneralData(postData);
                for (String key : map.keySet()) {
                    try {
                        postData.put(key, map.get(key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                postArray.put(postData);
            }
            return postArray.toString();
        }

        @Override
        public void onRequestFinished(HttpResponse response, boolean succeeded) {
            mPingTimes++;
            if (succeeded) {
                mIsPinging = false;
                mDataMap.clear();
            } else {
                if (mPingTimes < MAX_PING_TIMES) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestPingByForce();
                        }
                    }, getDelayedTime());
                } else {
                    mIsPinging = false;
                    mPingTimes = 0;
                    if (mPingFailCallback != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mPingFailCallback.onFailed(mDataMap);
                            }
                        });
                    }
                }
            }
        }
    }

    private Handler mHandler = null;
    private Context mContext = null;
    private JReq jsonRequest = null;
    private PingFailCallback mPingFailCallback;

    private List<Map<String, String>> mDataMap = new ArrayList<>();
    private int mPingTimes = 0;
    private boolean mIsPinging = false;

    public PingHandler(Context context, PingFailCallback callback) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        jsonRequest = new JReq(context);
        jsonRequest.setRequestHelper(new LaunchRequestHelper(), "PingHandler");
        mPingFailCallback = callback;
    }

    public synchronized void requestPing(List<Map<String, String>> map) {
        LauncherLog.d("ping", "requestPing() Entry, " + "mIsPinging: " + mIsPinging);
        if (mContext != null && map != null && !mIsPinging) {
            mDataMap.addAll(map);
            mIsPinging = true;
            jsonRequest.requestPing();
        }
    }

    private void requestPingByForce() {
        mIsPinging = true;
        jsonRequest.requestPing();
    }

    private long getDelayedTime() {
        return (long)(1000 * Math.pow(2, mPingTimes) * 10);
    }

    private JSONObject putGeneralData(JSONObject postData) {
        String token = "klauncher";
        String clientChannel ="103";
        String productiId = "";
        try {
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(
                    mContext.getPackageName(), PackageManager.GET_META_DATA);
            productiId = applicationInfo.metaData.getString("KappChannel");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Map<String, String> map = com.android.system.ReporterApi.getCarryOnParams(token, clientChannel, productiId);
        for (String key : map.keySet()) {
            try {
                postData.put(key, map.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return postData;
    }
}
