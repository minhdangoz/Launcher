
package com.webeye.theme.apktheme;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.webeye.theme.ThemeUtils;
import com.webeye.theme.Utilities;
import com.webeye.theme.util.ThemeLog;

import java.io.InputStream;

public class ApkThemeUtils {
	private static final String TAG = "ApkThemeUtils";
	private static final boolean debug = true;
    
	public static final String LENOVO_THEME_AGE = "com.lenovo.launcher.theme.age";
	public static final String LENOVO_THEME_DAVINCI = "com.lenovo.launcher.theme.davinci";
	public static final String LENOVO_THEME_IDEAWORLD = "com.lenovo.launcher.theme.ideaworld";
	public static final String LENOVO_THEME_LESSISMORE = "com.lenovo.launcher.theme.lessismore";
	public static final String LENOVO_THEME_LOOP = "com.lenovo.launcher.theme.loop";
	public static final String LENOVO_THEME_LOVECORNER = "com.lenovo.launcher.theme.lovecorner";
	public static final String LENOVO_THEME_RIBBON = "com.lenovo.launcher.theme.ribbon";
	public static final String LENOVO_THEME_SUMMERHURBS = "com.lenovo.launcher.theme.summerherbs";
	
	public static final String DEFAULT_WALLPAPER_NAME = "default_wallpaper";
	
	public static void applyWallpaper(Context context, InputStream is, String tag){
		ThemeUtils.applyWallpaper(context, is, tag);
	}
	
	/**
	 * 
	 * @param res                      当前包名 pkg 应用的资源
	 * @param iconIdInFindingIconPkg   当前包名 pkg 的图标ID
	 * @param themePackageContext      主题包的上下文
	 * @param launcherContext          Launcher的上下文
	 * @param findingIconPkg           当前正在查找图标的包
	 * @param hasBgBitmap              当前主题是否有背板 (在当前包名内部资源的ID)
	 * @return
	 */
    public static Bitmap findCustomIconById(int iconIdInFindingIconPkg,
            Context themePackageContext, Context launcherContext, String findingIconPkg, boolean hasBgBitmap) {
        if (iconIdInFindingIconPkg == 0) {
            return null;
        }
        try {
        	Resources res = ThemeUtils.getResourcesForApplication(launcherContext, findingIconPkg);
            // get icon id name
            String iconName = res.getResourceName(iconIdInFindingIconPkg);
            // parse the id name
            if (iconName != null) {
            	String[] result = new String[2];
            	ThemeUtils.parseResourceName(iconName, result, findingIconPkg);
                return findIconBitmapByIdName(result[0], themePackageContext, 
                		launcherContext, findingIconPkg, hasBgBitmap, iconIdInFindingIconPkg);
            } 
        } catch (NotFoundException e) {
            ThemeLog.e(TAG, "findCustomIconById  .");
        } catch (OutOfMemoryError e) {
            ThemeLog.e(TAG, "findCustomIconById OutOfMemoryError.");
        }
        return null;
    }
    
	private static int sIconDpi = 0;
    private static void updateIconDpi(Context context){
    	sIconDpi = ThemeUtils.updateIconDpi(context);
    }
    
    /**
     * 
     * @param iconNameInThemeAPK      当前包名 pkg 的图标在主题包中的名字
     * @param themePackageContext     主题包的上下文
     * @param launcherContext         Launcher上下文
     * @param findingIconPkg          当前正在查找图标的包名
     * @param hasBgBitmap             当前主题是否提供图标背板
     * @param iconIdInFindingIconPkg  当前包名 pkg 的图标ID (在当前包名内部资源的ID)
     * @return
     */
    public static Bitmap findIconBitmapByIdName(String iconNameInThemeAPK, Context themePackageContext, 
    		Context launcherContext, String findingIconPkg, boolean hasBgBitmap, int iconIdInFindingIconPkg) {
        if (themePackageContext == null){
            return null;
        }
        updateIconDpi(themePackageContext);
        int iconIdInThemeAPK = ThemeUtils.getDrawableId(themePackageContext, iconNameInThemeAPK);
        if (ThemeUtils.isDefalutApp(iconNameInThemeAPK) && iconIdInThemeAPK == 0){
            iconIdInThemeAPK = getAnotherResIdInThemeAPK(iconNameInThemeAPK, themePackageContext);
        }       
        Drawable drawable = null;
        if (iconIdInThemeAPK == 0) {
        	// 在Theme APK中找不到当前包名对应的ICON
        	// 需要确认是否需要为当前包名的ICON创建背板图片ICON
        	if(!hasBgBitmap){
        		// 不使用背板, 默认返回null
        		return null;
        	}
        	if(debug){
        		ThemeLog.i(TAG, "can't find the icon from apk theme for package : " + findingIconPkg + ", we need to make a mask bitmap for it!");
        	}
        	// can't find the icon for the given name,
        	// we need to make a bg mask for this icon
        	Bitmap bg = ThemeUtils.getThemeIconBg()[0];
        	Bitmap fg = ThemeUtils.getThemeIconBg()[1];
        	Bitmap mask = ThemeUtils.getThemeIconBg()[2];
        	Resources res;
			try {
				res = launcherContext.getPackageManager().getResourcesForApplication(findingIconPkg);
	        	drawable = res.getDrawableForDensity(iconIdInFindingIconPkg, sIconDpi);
			} catch (NameNotFoundException e) {
				ThemeLog.i(TAG, "", e);
			}
        	return Utilities.createIconBitmap(drawable, bg, fg, mask, launcherContext);
        }
        drawable = themePackageContext.getResources().getDrawableForDensity(iconIdInThemeAPK, sIconDpi); 
        if (drawable == null) {
        	return null;
        }
        return Utilities.createIconBitmap(drawable, launcherContext);
    }

    /**
     * 
     * @param name 
     * @param context
     * @return
     */
    public static Drawable findDrawableByResourceName(String name, Context context) {
        if (context == null) {
            return null;
        }
        try {
	        int resID = ThemeUtils.getDrawableId(context, name);
	        if (resID == 0) {
	        	return null;
	        }
	        updateIconDpi(context);
	        Drawable drawable = context.getResources().getDrawableForDensity(resID, sIconDpi);
	        return drawable;
        } catch (NotFoundException e) {
        	ThemeLog.i(TAG, "findDrawableByResourceName error", e);
        } catch (OutOfMemoryError e) {
        	ThemeLog.i(TAG, "findDrawableByResourceName error", e);
        }
        return null;
    }
    
	public static int getAnotherResIdInThemeAPK(String resName, Context themePackageContext){
	    int resID = 0;
	    if (resName.contains(                          "com_android_contacts__ic_launcher_phone")){
	        resID = ThemeUtils.getDrawableId(themePackageContext, "com_lenovo_ideafriend__ic_launcher_phone");
	    } 
	    else if (resName.contains(                     "com_android_contacts__ic_launcher_contacts")){
	        resID = ThemeUtils.getDrawableId(themePackageContext, "com_lenovo_ideafriend__ic_launcher_contacts");
	    } 
	    else if (resName.contains(                     "com_android_mms__ic_launcher_smsmms")){
            resID = ThemeUtils.getDrawableId(themePackageContext, "com_lenovo_mms__ic_launcher_smsmms");
            if (resID == 0){
                resID = ThemeUtils.getDrawableId(themePackageContext, "com_lenovo_ideafriend__ic_launcher_smsmms");    	
            }
        } 
	    else if (resName.contains(                     "com_android_browser__ic_launcher_browser")){
            resID = ThemeUtils.getDrawableId(themePackageContext, "com_android_browser__ic_launcher_browser_evdo");
        } 
	    else if (resName.contains("com_android_camera__ic_launcher_camera")){
            //do nothing
        }
	    return resID;
    }
    


    public static boolean setThemeIconBg(Context themePackageContext) {
    	
    	ThemeUtils.sThemeBgBitmap[0] = null;
		ThemeUtils.sThemeBgBitmap[1] = null;
		ThemeUtils.sThemeBgBitmap[2] = null;
		
    	if (themePackageContext == null) {
    		ThemeLog.i(ApkTheme.TAG, "themePackageContext == null, hasBgTheme is false");
    		return false;
    	}
    	
    	String themeIconBgName = ThemeUtils.THEME_ICON_BG_NAME;
    	String themeIconFgName = ThemeUtils.THEME_ICON_FG_NAME;
    	String themeIconMaskName = ThemeUtils.THEME_ICON_MASK_NAME;
    	
    	Drawable drawable = ApkThemeUtils.findDrawableByResourceName(themeIconBgName, themePackageContext);
    	if (drawable == null) {
    		ThemeUtils.sThemeBgBitmap[0] = null;
    		ThemeUtils.sThemeBgBitmap[1] = null;
    		ThemeUtils.sThemeBgBitmap[2] = null;
    	} else {
    		ThemeUtils.sThemeBgBitmap[0] = Utilities.createBitmap(drawable, 0, 0, themePackageContext);		
    	    drawable = ApkThemeUtils.findDrawableByResourceName(themeIconFgName, themePackageContext);
    	    ThemeUtils.sThemeBgBitmap[1] = Utilities.createBitmap(drawable, 0, 0, themePackageContext); 
    	    drawable = ApkThemeUtils.findDrawableByResourceName(themeIconMaskName, themePackageContext);
    	    ThemeUtils.sThemeBgBitmap[2] = Utilities.createBitmap(drawable, 0, 0, themePackageContext);    
    	    ThemeLog.i(ApkTheme.TAG, "hasBgTheme is true");
    	    return true;
    	}
    	ThemeLog.i(ApkTheme.TAG, "hasBgTheme is false");
    	return false;
    }
}
