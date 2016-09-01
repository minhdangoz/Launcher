package com.klauncher.biddingos.interstitial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.commons.web.BaseWebInterface;

import org.json.JSONException;
import org.json.JSONObject;


public class InterstitialWebInterface extends BaseWebInterface {
    private static final String TAG = "InterstitialWebInterface";
    private InterstitialWebView interstitialWebView = null;
    /**
     * Instantiate the interface and set the context
     */
    @SuppressLint("HandlerLeak")
    InterstitialWebInterface(Context c, InterstitialWebView wv, int placeMentId, Handler mBosHandler) {
        super(c, placeMentId, mBosHandler);
        this.interstitialWebView = wv;
        if (null == mHandler) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    InterstitialWebInterface.super.handleMsg(msg.what);
                    InterstitialWebInterface.this.handleMsg(msg.what);
                }
            };
        }
    }

    public void handleMsg(int nType) {
        switch (nType) {
            case MSG_TYPE_REQUEST_FAILED:
                if (null != interstitialWebView ) {
                    interstitialWebView.isLoading=false;
                    if(interstitialWebView.getVisibility() == View.VISIBLE) {
                        LogUtils.w(TAG, "setVisibility(View.GONE)");
                        interstitialWebView.setVisibility(View.GONE);
                    }
                }
                break;
            case MSG_TYPE_REQUEST_SUCCESS:
                if (null != interstitialWebView) {
                    interstitialWebView.isAdload = true;
                    interstitialWebView.isLoading=false;
                    if (interstitialWebView.getVisibility() == View.GONE) {
                        LogUtils.i(TAG, "setVisibility(View.VISIBLE)");
                        interstitialWebView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 得到上报log参数和显示参数
     * @param jsonInfo
     */
    @JavascriptInterface
    public void setAdInfo(String jsonInfo){
        LogUtils.i(TAG,"jsonInfo="+jsonInfo);
        if(!TextUtils.isEmpty(jsonInfo)) {
            try {
                JSONObject json = new JSONObject(jsonInfo);
                interstitialWebView.logUrl=json.getString("logUrl");
                interstitialWebView.img_with=json.getInt("imageWidth");
                interstitialWebView.img_height=json.getInt("imageHeight");
                interstitialWebView.adType=json.getInt("adType");
                interstitialWebView.st=json.getString("st");
                interstitialWebView.transactionid=json.getString("transactionid");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @JavascriptInterface
    public void setRefresh(String strTime) {
    }

    @JavascriptInterface
    public void notifyLoadFailed() {
        LogUtils.w(TAG, "failed to load page: notifyLoadFailed");
        Message msg = mHandler.obtainMessage(MSG_TYPE_REQUEST_FAILED);
        mHandler.sendMessageDelayed(msg, 1);
    }

    @JavascriptInterface
    public void notifyLoadSuccess() {
        LogUtils.i(TAG, "notifyLoadSuccess");
        Message msg = mHandler.obtainMessage(MSG_TYPE_REQUEST_SUCCESS);
        mHandler.sendMessageDelayed(msg, 1);

    }
}