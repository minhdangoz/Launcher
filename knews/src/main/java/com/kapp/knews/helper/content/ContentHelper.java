package com.kapp.knews.helper.content;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.kapp.knews.helper.Utils;


/**
 * 作者:  android001
 * 创建时间:   16/10/31  上午11:50
 * 版本号:
 * 功能描述:
 */
public class ContentHelper {
    

    public static Resources getResource(){
        return Utils.getContext().getResources();
    }

    public static AssetManager getAssets(){
        return Utils.getContext().getAssets();
    }

    public static PackageManager getPackageManager() {
        return Utils.getContext().getPackageManager();
    }

    public static ContentResolver getContentResolver(){
        return Utils.getContext().getContentResolver();
    }
}
