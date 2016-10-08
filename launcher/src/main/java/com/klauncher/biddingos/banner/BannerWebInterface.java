package com.klauncher.biddingos.banner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.klauncher.biddingos.commons.web.BaseWebInterface;

import java.util.Timer;
import java.util.TimerTask;

public class BannerWebInterface extends BaseWebInterface {
	private static final String TAG = "BannerWebInterface";
	BannerWebView bannerWebView = null;
	public Timer timer			= null;
	private int refreshSeconds = 0; // 刷新时间设置
	public boolean bIsRefreshing = false;

    /** Instantiate the interface and set the context */
    @SuppressLint("HandlerLeak")
	BannerWebInterface(Context c, BannerWebView wv, int placeMentId, Handler mBosHandler) {
		super(c, placeMentId, mBosHandler);
        this.bannerWebView = wv;
        if(null==mHandler) {
        	mHandler = new Handler() {
				@Override
				public void handleMessage (Message msg) {
					BannerWebInterface.super.handleMsg(msg.what);
					BannerWebInterface.this.handleMsg(msg.what);
				}
			};
		}
    }
    
    public void handleMsg(int nType) {
    	switch(nType) {
		case BaseWebInterface.MSG_TYPE_REQUEST_FAILED:
			 if(null!= bannerWebView && bannerWebView.getVisibility()== View.VISIBLE) {
		        Log.w(TAG, "setVisibility(View.GONE)");
				 bannerWebView.setVisibility(View.GONE);
			 }
			 break;
		case BaseWebInterface.MSG_TYPE_REQUEST_SUCCESS:
			if(null!= bannerWebView && bannerWebView.getVisibility()== View.GONE) {
				Log.i(TAG, "setVisibility(View.VISIBLE)");
				bannerWebView.setVisibility(View.VISIBLE);
			 }
			break;
		case BaseWebInterface.MSG_TYPE_REFRESH:
			if(null!= bannerWebView /*&& adView.hasWindowFocus()*/) {
				if(isConnect()) {
					Log.i(TAG, "refresh ad zone:" + placeMentId);
					bIsDownloading = false;
					bannerWebView.loadAd();
			    }
			}
			
			break;
		default:
			break;
		}
    }

	public void startRefresh() {
		if(bIsRefreshing || 0>=refreshSeconds) return;

		//if(null==this.timer) {
			timer = new Timer();
		//}
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(null!= bannerWebView) {
					Message msg = mHandler.obtainMessage(BaseWebInterface.MSG_TYPE_REFRESH);
					mHandler.sendMessageDelayed(msg, 1);
				}
			}
		}, refreshSeconds*1000, refreshSeconds*1000);
		Log.i(TAG, "start ad refresh with time:" + refreshSeconds);
		bIsRefreshing = true;
	}

	public void stopRefresh() {
		if(!bIsRefreshing) return;
		if(null!=this.timer) {
			this.timer.cancel();
		}
		Log.i(TAG, "stop ad refresh");
		bIsRefreshing = false;
	}
	@JavascriptInterface
	public void setRefresh(String strTime) {
		refreshSeconds = Integer.parseInt(strTime);
		startRefresh();
	}
	@JavascriptInterface
	public void notifyLoadFailed() {
		Log.w(TAG, "failed to load page: notifyLoadFailed");
		Message msg = mHandler.obtainMessage(MSG_TYPE_REQUEST_FAILED);
		mHandler.sendMessageDelayed(msg, 1);
	}

	@JavascriptInterface
	public void notifyLoadSuccess() {
		Log.i(TAG, "notifyLoadSuccess");
		Message msg = mHandler.obtainMessage(MSG_TYPE_REQUEST_SUCCESS);
		mHandler.sendMessageDelayed(msg, 1);

	}
}