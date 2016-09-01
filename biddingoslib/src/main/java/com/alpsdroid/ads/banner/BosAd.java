package com.alpsdroid.ads.banner;

import android.view.View;
import android.content.Context;

public interface BosAd {

    public static final int MODE_ONLINE = 3;
    public static final int MODE_DEBUG_PUBLIC = 2;
    public static final int MODE_DEBUG_PRIVATE = 1;

    /**
     * 创建Banner对象 
     *
     * @param mid    媒体商代理ID
     * @param afid   媒体商ID
     * @param secret 密钥
     * @param mode SDK 环境模式
     */
//    public void init(Context context, String mid, String afid, String secret, int mode);

    /**
     * 创建Banner对象 
     *
     * @param context    设备上下文
     * @param placeMentId 广告位ID
     * @param adSize     广告位大小对象
     */
    public View createBanner(Context context, int placeMentId, AdSize adSize);

    /**
     * 设置广告监听器
     *
     * @param placeMentId 广告位ID
     * @param adListener 广告监听器
     */
    public void setAdListener(int placeMentId, AdListener adListener);
}