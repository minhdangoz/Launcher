package com.alpsdroid.ads.interstitial;

import android.app.Activity;

import com.alpsdroid.ads.banner.AdSize;

/**
 * Created by：lizw on 2016/3/28 11:51
 */
public interface InterstitialAdHelper {
	void setAdListener(int zoneId, InterstitialAdListener listener);

	boolean isAdReady(int zoneId);

	void show(int zoneId);

	void load(int zoneId, AdSize adSize);

	// activity
	void onCreate(Activity activity, int zoneId);

	void onDestroy(int zoneId);
}
