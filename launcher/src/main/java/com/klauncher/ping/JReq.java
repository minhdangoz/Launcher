// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-

package com.klauncher.ping;

import android.content.Context;

import com.klauncher.ext.LauncherLog;
import com.klauncher.utilities.ProxyUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.text.DateFormat;
import java.util.Date;

class JReq {

    interface RequestHelper {
        String getRequestURL();
        String getRequestJsonData();
        void onRequestFinished(HttpResponse response, boolean succeeded);
    }

    private final Context mContext;

    protected JReq(Context context) {
        mContext = context;
    }

    private static final String TAG = "JsonRequest";
    private static final int TIMEOUT_INTERVAL = 60 * 1000;
    private RequestHelper mRequestHelper = null;
    private String mRequestHelperName = null;

    void setRequestHelper(RequestHelper requestHelper, String name) {
        mRequestHelper = requestHelper;
        mRequestHelperName = name;
    }

    void requestPing() {
        LauncherLog.d("ping", mRequestHelperName + " requestPing() Entry");

        new Thread(new Runnable() {
            @Override
            public void run() {
                postData();
            }
        }).start();
    }

    private void postData() {
        HttpPost hp = new HttpPost(mRequestHelper.getRequestURL());
        LauncherLog.d("ping", "current time:" + DateFormat.getTimeInstance().format(new Date()));
        LauncherLog.d("ping", mRequestHelperName + " ping url:" + mRequestHelper.getRequestURL());
        HttpParams params = hp.getParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_INTERVAL);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT_INTERVAL);
        hp.setParams(params);
        HttpClient hc = null;
        try {
            String data = mRequestHelper.getRequestJsonData();
            LauncherLog.d("ping", mRequestHelperName + " ping request data:" + data);
            StringEntity se = new StringEntity(data, HTTP.UTF_8);
            se.setContentType("application/json; charset=UTF-8");
            hp.setEntity(se);
            hc = new DefaultHttpClient();
            ProxyUtils.configHttpClientProxy(hc, mContext, hp.getURI());
            HttpResponse response = hc.execute(hp);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                LauncherLog.d("ping", mRequestHelperName + " Ping succeeded.");
                onRequestDone(response);
            } else {
                LauncherLog.e("ping", mRequestHelperName + " Ping failed, status code: " + statusCode);
                onRequestFailed();
            }
        } catch (Exception e) {
            LauncherLog.e("ping", mRequestHelperName + " Ping error: " + e);
            onRequestFailed();
        } finally {
            if (hc != null) {
                hc.getConnectionManager().shutdown();
            }
        }
    }

    private void onRequestDone(HttpResponse response) {
        mRequestHelper.onRequestFinished(response, true);
    }

    private void onRequestFailed() {
        mRequestHelper.onRequestFinished(null, false);
    }
}
