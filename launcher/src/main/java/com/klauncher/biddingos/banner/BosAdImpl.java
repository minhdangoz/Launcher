package com.klauncher.biddingos.banner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.alpsdroid.ads.banner.AdListener;
import com.alpsdroid.ads.banner.AdSize;
import com.alpsdroid.ads.banner.BosAd;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.commons.web.BaseWebInterface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BosAdImpl implements BosAd {
	private static final String TAG = "BosAdImpl";
	private static Map<Integer, BannerWebView> mZoneId_adView= new ConcurrentHashMap<Integer, BannerWebView>();
	private static Map<Integer, AdListener> mZoneId_adListener= new ConcurrentHashMap<Integer, AdListener>();
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
			@Override
			public void handleMessage (Message msg) {
				handleMsg(msg.what, msg.getData());	 
			}
		};
		
	private void handleMsg(int what, Bundle data) {
		int placeMentId = data.getInt("placeMentId");
		switch(what) {
		case BaseWebInterface.MSG_TYPE_REQUEST_FAILED:
			 String desc = data.getString("desc");
			 if(mZoneId_adListener.containsKey(placeMentId)) {
				 AdListener adListener = mZoneId_adListener.get(placeMentId);
				 if(null!=adListener) {
					 adListener.onAdFailed(desc);
				 }
			 }
			 break;
		case BaseWebInterface.MSG_TYPE_REQUEST_SUCCESS:
			 if(mZoneId_adListener.containsKey(placeMentId)) {
				 AdListener adListener = mZoneId_adListener.get(placeMentId);
				 if(null!=adListener) {
					 adListener.onAdLoaded();
				 }
			 }
			break;
		case BaseWebInterface.MSG_TYPE_CLICK:
			LogUtils.i("BaseWebInterface", 222 + "  " + (mZoneId_adListener.containsKey(placeMentId)));
			 if(mZoneId_adListener.containsKey(placeMentId)) {
				 AdListener adListener = mZoneId_adListener.get(placeMentId);
				 if(null!=adListener) {
					 adListener.onAdClicked();
				 }
			 }
			break;
		default:
			break;
		}	
	}
	
	@Override
	public View createBanner(Context context, int placeMentId, AdSize adSize) {

		if(mZoneId_adView.containsKey(placeMentId) && null!=mZoneId_adView.get(placeMentId)) {
			//Log.w(TAG, "return adview directly for :"+placeMentId);
			BannerWebView bannerWebView = mZoneId_adView.get(placeMentId);
			//adView.stopRefresh();
			if(!bannerWebView.isRefreshing()) {
				bannerWebView.loadAd();
			}
			ViewGroup vg = (ViewGroup) bannerWebView.getParent();
			if(vg!=null) {
				vg.removeView(bannerWebView);
			}
			return mZoneId_adView.get(placeMentId);
		}
		
		
		BannerWebView adview = new BannerWebView(context, adSize);
		if(!adview.init(placeMentId, mHandler)) {
			return null;
		}
		mZoneId_adView.put(placeMentId, adview);
		return adview;
	}
	
	@Override
	public void setAdListener(int placeMentId, AdListener adListener) {
		mZoneId_adListener.put(placeMentId, adListener);
	}

	
}
