package com.alpsdroid.ads.banner;

//Banner广告监听器定义
public interface AdListener {
	//广告加载成功
	public void onAdLoaded();
	//广告加载失败
	public void onAdFailed(String errorMsg);
	//广告被点击
	public void onAdClicked();
}