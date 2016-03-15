package com.klauncher.theme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import com.klauncher.theme.util.ThemeLog;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;

public class ThemeUtils {

	private static final String TAG = "ThemeUtils";
	
	public static final String THEME_ICON_BG_NAME = "theme_appbg";
    public static final String THEME_ICON_FG_NAME = "theme_appfg";
    public static final String THEME_ICON_MASK_NAME = "theme_appmask";

	public static final String RESOURCE_DRAWABLE_TYPE = "drawable";
	
    public static Bitmap sThemeBgBitmap[] = new Bitmap[3];
    public static Bitmap[] getThemeIconBg() {
    	return sThemeBgBitmap;
    }
    
    // package name --> get resource.
    public static Resources getResourcesForApplication(Context context, String packageName) {
        Resources res = null;
        try {
            res = context.getPackageManager().getResourcesForApplication(packageName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }
	
	@SuppressLint("ServiceCast")
	public static void applyWallpaper(Context context, InputStream is, String tag){
		if (is != null) {
			WallpaperManager wm = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
			try {
				ThemeLog.i(TAG, "set wallpaper from " + tag + " !");
				wm.setStream(is);
				is.close();
			} catch (IOException e) {
				ThemeLog.i(TAG, "set wallpaper from " + tag + " failed ! ", e);
			} finally {
				is = null;	
			}
		}
	}
	
    public static boolean isDefalutApp(String iconName) {
        if (iconName.contains("com_android_contacts__ic_launcher_phone")    ||
            iconName.contains("com_android_contacts__ic_launcher_contacts") ||
            iconName.contains("com_android_mms__ic_launcher_smsmms")        ||
            iconName.contains("com_android_browser__ic_launcher_browser")   ||
            iconName.contains("com_android_camera__ic_launcher_camera")){
            return true;
        }
        return false;
    }
    
    private static float sDensity = Float.MIN_VALUE;
	private static int sIconDpi = 0;
    public static int updateIconDpi(Context context){
    	if(sIconDpi > 0) {
    		return sIconDpi;
    	}
    	
        if(sDensity == Float.MIN_VALUE) {
        	sDensity = context.getResources().getDisplayMetrics().density;
        }
        
    	if(sDensity <= 1) {
    		if(context != null) {
    			sIconDpi = 240;
    		} else {
    			sIconDpi = 160;
    		}
    	} else if(sDensity > 1.0 && sDensity < 2.0) {
    		sIconDpi = 240;
    	} else {
        	final ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    		sIconDpi = activityManager.getLauncherLargeIconDensity();
    	}
    	ThemeLog.i(TAG, "sIconDpi : " + sIconDpi + ", sDensity:" + sDensity);
    	return sIconDpi;
    }
    
    public static void parseResourceName(String iconName, String[] ret, String packageInfo) {
        if (iconName != null) {
        	/**
        	 * the iconName is look like below
        	 * 
        	 * com.android.settings:mipmap/ic_launcher_settings
			 * com.lenovo.browser:mipmap/ic_launcher_browser
			 * com.lenovo.calendar:mipmap/ic_launcher_calendar
			 * com.lenovo.ideafriend:drawable/ic_launcher_contacts_calllog
			 * com.lenovo.ideafriend:drawable/ic_launcher_smsmms
        	 */
            int indexColon = iconName.indexOf(":");
            int indexSeperator = iconName.indexOf(File.separator);
            
            if (indexColon != -1 && indexSeperator != -1) {
            	// get packageName "com.android.settings" from "com.android.settings:mipmap/ic_launcher_settings"
                String packageName = iconName.substring(0, indexColon);
                // get iconName "ic_launcher_settings" from "com.android.settings:mipmap/ic_launcher_settings"
                iconName = iconName.substring(indexSeperator + 1);
                // make the ret[0] like "com_android_settings__ic_launcher_settings"
                ret[0] = buildupResourceName(packageName, iconName);
                
                /**
                 * the icon name in the theme package is look like below:
                 * 
                 * com_android_settings__ic_launcher_settings
                 * com_lenovo_browser__ic_launcher_browser
                 * com_lenovo_calendar__ic_launcher_calendar
                 * com_lenovo_ideafriend__ic_launcher_contacts_calllog
                 * com_lenovo_ideafriend__ic_launcher_smsmms
                 */
                /**
                 * 如果当前正在查找图标的包packageInfo的包名和从iconName中解析得到的包名packageName
                 * 不一致的话，以packageInfo为准，再解析一次
                 */
                boolean anotherPkgName = packageName.equals(packageInfo);
                if (!anotherPkgName) {
                    ret[1] = buildupResourceName(packageInfo, iconName);
                }
            }
        }
    }
    
    private static String buildupResourceName(String packageName, String iconName) {
    	 // make the iconNameInThemeAPK like "com_android_settings__ic_launcher_settings"
        return (rebuildPackageName(packageName) + "__" + iconName).toLowerCase(Locale.getDefault());
    }

    private static String rebuildPackageName(String packageName) {
    	// change the packageName from "com.android.settings" to "com_android_settings"
        return packageName.replace(".", "_");
    }
    
    public static int getDrawableId(Context context, String name){
    	return context.getResources().getIdentifier(name, RESOURCE_DRAWABLE_TYPE, context.getPackageName());
    }
    
    public static void sendBroadcastToForceReloadLauncher(Context context, String tag){
    	ThemeLog.i(TAG, "sendBroadcastToForceReloadLauncher : " + tag);
    	Intent intent = new Intent(ThemeController.ACTION_LAUNCHER_THEME_FORCE_RELOAD_LAUNCHER);
    	context.sendBroadcast(intent);
    }
}
