package com.alpsdroid.ads.flow;

public class FlowAdInfo {

	private String downloadUrl;
	private int versionCode;
	private String versionName;
	private String md5;
	private String packageName;
	private int size;
	
	public FlowAdInfo(String packageName, String versionName, int versionCode, String downloadUrl, String md5, int size) {
		this.packageName = packageName;
		this.versionName = versionName;
		this.versionCode = versionCode;
		this.downloadUrl = downloadUrl;
		this.md5		 = md5;
		this.size		 = size;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getMd5() {
		return md5;
	}

	public String getPackageName() {
		return packageName;
	}

	public int getSize() {
		return size;
	}
	
}
