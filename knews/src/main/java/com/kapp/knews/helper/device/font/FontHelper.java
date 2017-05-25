package com.kapp.knews.helper.device.font;

import android.content.res.ColorStateList;

import com.kapp.knews.helper.content.ContentHelper;
import com.kapp.knews.helper.device.DeviceInfo;


/**
 * 作者:  android001
 * 创建时间:   16/10/31  下午5:56
 * 版本号:
 * 功能描述:
 */
public class FontHelper {

    public static ColorStateList getColorStateList (int colorResId) {
        if (DeviceInfo.getSDKVersion()>=23) {
            return ContentHelper.getResource().getColorStateList(colorResId,null);
        } else {
//            ColorStateList colorStateList = new ColorStateList()
        }

        return null;
    }
}
