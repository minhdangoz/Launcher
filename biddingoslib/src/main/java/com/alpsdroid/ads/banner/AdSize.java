package com.alpsdroid.ads.banner;

public final class AdSize {
	
    public static final AdSize BANNER_216_36 = new AdSize(216, 36);
    public static final AdSize BANNER_300_50 = new AdSize(300, 50);
    public static final AdSize BANNER_320_50 = new AdSize(320, 50);
    public static final AdSize BANNER_468_60 = new AdSize(468, 60);
    public static final AdSize BANNER_640_100 = new AdSize(640, 100);
    public static final AdSize BANNER_728_90 = new AdSize(728, 90);
    public static final AdSize BANNER_1280_200 = new AdSize(1280, 200);
//    public static final AdSize INTERSTITIAL_300_250 = new AdSize(300, 250);
//    public static final AdSize INTERSTITIAL_250_300 = new AdSize(250, 300);
//    public static final AdSize INTERSTITIAL_600_500 = new AdSize(600, 500);
//    public static final AdSize INTERSTITIAL_500_600 = new AdSize(500, 600);
//    public static final AdSize BANNER_HEIGHT_36 = new AdSize(-1, 36);
//    public static final AdSize BANNER_HEIGHT_50 = new AdSize(-1, 50);
//    public static final AdSize BANNER_HEIGHT_60 = new AdSize(-1, 60);
//    public static final AdSize BANNER_HEIGHT_90 = new AdSize(-1, 90);
//    public static final AdSize BANNER_HEIGHT_100 = new AdSize(-1, 100);
//    public static final AdSize BANNER_HEIGHT_200 = new AdSize(-1, 200);

    public int nWidth;
    public int nHeight;
    
    
    public AdSize(int width, int height) {
    	this.nWidth = width;
    	this.nHeight = height;
    }
    
    public int getWidth() {
    	return nWidth;
    }
    
    public int getHeight() {
    	return nHeight;
    }

    public void setWidth(int nWidth) {
        this.nWidth = nWidth;
    }

    public void setHeight(int nHeight) {
        this.nHeight = nHeight;
    }

}