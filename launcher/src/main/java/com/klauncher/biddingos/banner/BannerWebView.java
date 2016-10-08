package com.klauncher.biddingos.banner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.alpsdroid.ads.banner.AdSize;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.web.BaseWebView;


@SuppressLint("SetJavaScriptEnabled")
public class BannerWebView extends BaseWebView {
	private static final String TAG = "BannerWebView";
	private BannerWebInterface bannerWebInterface = null;

	
	public BannerWebView(Context context, AdSize adSize) {
		super(context,adSize);
		// TODO Auto-generated constructor stub
	}

	public boolean isRefreshing() {
		if(null!= bannerWebInterface) {
			return bannerWebInterface.bIsRefreshing;
		}
		return false;
	}

	private void startRefresh() {
		if(null!= bannerWebInterface) {
			bannerWebInterface.startRefresh();
		}
	}

	private void stopRefresh() {
		if(null!= bannerWebInterface) {
			bannerWebInterface.stopRefresh();
		}
	}

	public void loadAd() {
		this.loadUrl(getBannerUrl(mContext, placeMentId));
	}

	private String getBannerUrl(Context mContext, int placeMentId) {
		return getUrl(Setting.getBiddingOS_BANNER_URL(getAdSuitableWidth(), adSize.getHeight(), nDpi),mContext,placeMentId);
	}

	public boolean init(int placeMentId, final Handler mHandler){
		super.init(placeMentId,mHandler);
		loadAd();
        bannerWebInterface = new BannerWebInterface(mContext, this, placeMentId, mHandler);
		this.addJavascriptInterface(bannerWebInterface, "BosAd");
        return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int nWidth = (int) (getAdSuitableWidth()*((float)nDpi/160));
		int nHeight = (int) (adSize.getHeight()*((float)nDpi/160));
		setMeasuredDimension(nWidth, nHeight);

	}



	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(View.GONE == visibility) {
			this.stopRefresh();
		}else if(View.VISIBLE == visibility) {
			this.startRefresh();
		}
		super.onWindowVisibilityChanged(visibility);
	}


}
