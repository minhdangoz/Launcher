package com.klauncher.biddingos.commons.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alpsdroid.ads.banner.AdSize;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;

@SuppressLint("SetJavaScriptEnabled")
public class BaseWebView extends WebView {
	private static final String TAG = "BaseWebView";
	protected Context mContext = null;
	public int nDpi = -1;
	public AdSize adSize = null;
	protected int placeMentId = 0;
	protected Handler mHandler;
	public boolean isLoading=true;//502没有失败和成功消息，在onPageFinished置为false

	
	public BaseWebView(Context context, AdSize adSize) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.adSize = adSize;
		this.nDpi = getDpi();
	}

	public boolean init(int placeMentId, final Handler mHandler){
		if(null==mContext) return false;
		
		if(-1==nDpi) {
			nDpi = getDpi();
		}

		this.placeMentId = placeMentId;
		this.mHandler = mHandler;

        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
		if(Build.VERSION.SDK_INT >= 11) {
			this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
        this.setBackgroundColor(0);
        WebSettings webSettings = this.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setBuiltInZoomControls(false);
        this.setWebChromeClient(new WebChromeClient());
        
        final int pMid = placeMentId;
        final BaseWebView webView = this;
        this.setWebViewClient(new WebViewClient(){

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
            	Log.i(TAG, "onReceivedError");
            	webView.setVisibility(View.GONE);
            	//通知失败
            	if(null!=mHandler) {

            		Message msg = mHandler.obtainMessage(BaseWebInterface.MSG_TYPE_REQUEST_FAILED);
            		Bundle data = new Bundle();
            		data.putInt("placeMentId", pMid);
            		data.putString("desc", description);
            		msg.setData(data);
                    mHandler.sendMessageDelayed(msg, 1);
            	}

				super.onReceivedError(view, errorCode, description, failingUrl);
			}

            @Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
            	Log.i(TAG, "shouldOverrideUrlLoading");
                view.loadUrl(url);
                return true;
            }

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onPageFinished");
				isLoading=false;
				super.onPageFinished(view, url);
			}
			
        });
		Log.i("TAG", "init done");
        return true;
	}
	

	protected int getDpi() {

		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		Log.i(TAG, "w:" + dm.widthPixels + " h:" + dm.heightPixels);
		return dm.densityDpi;
	}


	protected int getScreenDpWidth() {
		DisplayMetrics dm =mContext.getResources().getDisplayMetrics();
		int dp = (int)((dm.widthPixels*160)/dm.densityDpi);
		return dp;
	}

	protected int getScreenPixWidth() {
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}
	protected int getScreenPixHeight() {
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	protected int getAdSuitableWidth() {
		int nWidth = 0;
		if(-1==adSize.getWidth()) {
			nWidth = getScreenDpWidth();
		}else {
			nWidth = adSize.getWidth();
		}
		return nWidth;
	}
	public String getUrl(String url,Context context, int placeMentId) {
		String uids = "";
		JSONArray jsUids = AppUtils.getUserUniqueID();
		try {
			for (int i=0; i<jsUids.length(); ++i) {
				uids = uids + jsUids.getString(i) + ((jsUids.length()-1)==i?"":"|");
			}
		}catch (JSONException e) {
			Log.e(TAG, "cannot get uids", e);
		}
		url = url + "&channel=android." + Setting.SDK_VERSION + "&uids=" + uids + "&zones=" + placeMentId+"&platform="+ Setting.SYSTEM_PLATFORM;

		Log.i(TAG, "url:" + url);
		return url;
	}

}
