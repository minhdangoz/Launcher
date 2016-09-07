package com.klauncher.kinflow.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.klauncher.ext.KLauncherApplication;

/**
 * Created by xixionghui on 16/8/24.
 */
public class ResourceUtils {
//    Resources res = getResources();
//    Drawable drawable = res.getDrawable(R.drawable.myimage);

    public static ResourceUtils instance = new ResourceUtils();
    public Context mContext;

    private ResourceUtils(){}

    public void init(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        if (null == mContext) mContext = KLauncherApplication.mKLauncherApplication;
        return mContext;
    }

    public Resources getResource() {
        return getContext().getResources();
    }

    public Drawable resId2Drawable (int resId) {
       return getResource().getDrawable(resId);
    }
}
