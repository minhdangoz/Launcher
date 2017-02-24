package com.alpsdroid.ads.commons;

/**
 * Created by：lizw on 2016/1/6 11:58
 */
public class AdConfigType {
    public static final int BUSINESS_TYPE_AD=1;//0001 投放
    public static final int BUSINESS_TYPE_FLOW=1<<1;//0010 流量
    public static final int BUSINESS_TYPE_BANNER=1<<2;//0100 banner
    public static final int BUSINESS_TYPE_FEEDS=1<<3;//1000 feeds
    public static final int BUSINESS_TYPE_INTERSTITIAL=1<<4;//10000 interstitial
}
