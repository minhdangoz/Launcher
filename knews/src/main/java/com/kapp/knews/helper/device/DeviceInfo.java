package com.kapp.knews.helper.device;

import android.os.Build;

/**
 * 作者:  android001
 * 创建时间:   16/10/31  下午6:02
 * 版本号:
 * 功能描述:
 */
public class DeviceInfo {

    public static int getSDKVersion () {
        return Build.VERSION.SDK_INT;
    }
}
