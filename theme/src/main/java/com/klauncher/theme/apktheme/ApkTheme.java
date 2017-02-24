package com.klauncher.theme.apktheme;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.klauncher.theme.ITheme;
import com.klauncher.theme.ThemeUtils;
import com.klauncher.theme.util.ThemeLog;

import java.io.IOException;
import java.io.InputStream;

public class ApkTheme implements ITheme {
    public static final String TAG = "ApkTheme";
    Context mContext; // The context refs to theme package.
    Context mLauncherContext; // The context refs to launcher activity.
    private static boolean sHasBgBitmap = false;
    
    /**
     * Build a new ApkTheme
     * @param launcherContext the context refs to LauncherActivity.
     * @param themePackageName the theme package name.
     */
    public ApkTheme(Context launcherContext, String themePackageName) throws Exception{
    	mLauncherContext = launcherContext;
    	try {
			mContext = mLauncherContext.createPackageContext(themePackageName, Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			ThemeLog.i(TAG, "theme " + themePackageName + " not found! ", e);
			mContext = null;
			throw e;
		}
    	ThemeLog.i(TAG, "find theme " + themePackageName);
    }
    
    @Override
    public InputStream getDefaultWallpaper() {
    	if(mContext == null) {
    		ThemeLog.i(TAG, "getDefaultWallpaper failed, because mContext is null");
    		return null;
    	}
    	InputStream retIS = null;
    	try {
			int wallpaperId = ThemeUtils.getDrawableId(mContext, ApkThemeUtils.DEFAULT_WALLPAPER_NAME);
			if (wallpaperId == 0) {
				AssetManager assetManager = mContext.getAssets();					
				retIS = assetManager.open(ApkThemeUtils.DEFAULT_WALLPAPER_NAME + ".png");
			} else {
				retIS = mContext.getResources().openRawResource(wallpaperId);
			}
    	} catch (IOException e) {
    		ThemeLog.i(TAG, "get wallpaper from theme: " + mContext.getPackageName() + " error! ", e);
    		return null;
		}
        return retIS;
    }

    @Override
    public Bitmap getIconBitmap(ActivityInfo activityInfo) {
        Bitmap iconBitmap = null;
        
        if (null == mLauncherContext || null == activityInfo) {
            return null;
        }
        /* Lenovo-SW 20150413 zhaoxin5 fix bug that get activity icon is null START */
        int iconId = activityInfo.getIconResource();
        /* Lenovo-SW 20150413 zhaoxin5 fix bug that get activity icon is null END */
        String pkgName = activityInfo.packageName;
        try {
            iconBitmap = ApkThemeUtils.findCustomIconById(iconId, mContext, mLauncherContext, pkgName, hasBgBitmap());
        } catch (Exception e) {
            ThemeLog.e(TAG, "getIconBitmap Failed", e);
        } 
        
        return iconBitmap;
    }

	@Override
	public Bitmap getIconFromTheme(ActivityInfo activityInfo) {
		Bitmap iconBitmap = null;

		if (null == mLauncherContext || null == activityInfo) {
			return null;
		}
        /* Lenovo-SW 20150413 zhaoxin5 fix bug that get activity icon is null START */
		int iconId = activityInfo.getIconResource();
        /* Lenovo-SW 20150413 zhaoxin5 fix bug that get activity icon is null END */
		String pkgName = activityInfo.packageName;
		try {
			iconBitmap = ApkThemeUtils.findCustomIconById(iconId, mContext, mLauncherContext, pkgName, hasBgBitmap());
		} catch (Exception e) {
			ThemeLog.e(TAG, "getIconBitmap Failed", e);
		}

		return iconBitmap;
	}

	@Override
    public boolean hasBgBitmap() {
    	return sHasBgBitmap;
    }

	@Override
	public void handleTheme(boolean justReloadLauncher, boolean enableThemeMask) {
    	sHasBgBitmap = ApkThemeUtils.setThemeIconBg(mContext) && enableThemeMask;
		if(!justReloadLauncher){
	    	ApkThemeUtils.applyWallpaper(mLauncherContext, getDefaultWallpaper(), mContext.getPackageName());
		}
        ThemeUtils.sendBroadcastToForceReloadLauncher(mLauncherContext, mContext.getPackageName());
    	//mLauncherContext.getModel().forceReloadForThemeApply();
	}
}
