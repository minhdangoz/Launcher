package com.webeye.theme;

import com.webeye.theme.apktheme.ApkTheme;
import com.webeye.theme.settings.SettingsProvider;
import com.webeye.theme.util.ThemeLog;
import com.webeye.theme.ziptheme.ZipTheme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ThemeController extends BroadcastReceiver{
	public static final String LOGTAG = "ThemeController";
	public static final String ACTION_LAUNCHER_THEME_FORCE_RELOAD_LAUNCHER = "action_themecenter_themechange_lelauncher_force_reload_launcher";
	public static final String ACTION_LAUNCHER_THEME_ZIP = "action_themecenter_themechange_lelauncher_zip";
	public static final String ACTION_LAUNCHER_THEME_APK = "action_themecenter_themechange_lelauncher_apk";
	public static final String ACTION_LAUNCHER_THEME_TYPE = "ThemeType";
    public static final String ACTION_LAUNCHER_THEME_PATH = "ThemePath";
    public static final String ACTION_LAUNCHER_THEME_PACKAGE = "ThemePackage";
    public static final String EXTRA_LAUNCHER_THEME_ENABLE_THEME_MASK = "EnableThemeMask";
    public static final String EXTRA_THEME_TYPE_ZIP = "ThemeTypeZip";
    public static final String EXTRA_THEME_TYPE_APK = "ThemeTypeApk";
    public static final String EXTRA_THEME_APP_ICON_SIZE = "AppIconSize";
    public static final String EXTRA_THEME_APP_ICON_TEXTURE_SIZE = "AppIconTextureSize";
	
    private ITheme mTheme = null;
    private Context mContext = null;
    
    public ITheme getTheme(){
    	return mTheme;
    }
    
	public ThemeController(Context context) {
		super();
		mContext = context.getApplicationContext();
		
		// zip theme
		IntentFilter filter = new IntentFilter(ACTION_LAUNCHER_THEME_ZIP);
		mContext.registerReceiver(this, filter);
		
		// apk theme
		filter = new IntentFilter(ACTION_LAUNCHER_THEME_APK);
		mContext.registerReceiver(this, filter);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
        ThemeLog.d(LOGTAG, "onReceive intent=" + action);
        String themeInfo = "";
        boolean enableThemeMask = true;
        try{
	        //add for receive zip theme apply broadcast start
	        if(ACTION_LAUNCHER_THEME_ZIP.equals(action)){
	        	/* adb shell am broadcast -a action_themecenter_themechange_lelauncher_zip --es ThemePath data/data/com.lenovo.xlauncher/res.zip */
	        	themeInfo = intent.getStringExtra(ACTION_LAUNCHER_THEME_PATH);
	        	enableThemeMask = intent.getBooleanExtra(EXTRA_LAUNCHER_THEME_ENABLE_THEME_MASK, true);
	            mTheme = new ZipTheme(mContext, themeInfo);
	        }else if(ACTION_LAUNCHER_THEME_APK.equals(action)){
	        	/* adb shell am broadcast -a action_themecenter_themechange_lelauncher_apk --es ThemePackage com.lenovo.launcher.theme.lovecorner */
	        	themeInfo = intent.getStringExtra(ACTION_LAUNCHER_THEME_PACKAGE);
	        	enableThemeMask = intent.getBooleanExtra(EXTRA_LAUNCHER_THEME_ENABLE_THEME_MASK, true);
	        	mTheme = new ApkTheme(mContext, themeInfo);
	        }
        } catch (Exception e) {
        	ThemeLog.i(LOGTAG, "Error happened in init Theme!");
        	mTheme = null;
        }
        String currentTheme = SettingsProvider.getStringCustomDefault(mContext, SettingsProvider.SETTINGS_CURRENT_THEME, "");
        ThemeLog.i(LOGTAG, "currentTheme:" + currentTheme + ",themeInfo:" + themeInfo + ", enableThemeMask:" + enableThemeMask);
        if(null != mTheme){
        	// true表明已经加载过此主题,仅仅需要重新retart Launcher即可
        	boolean justRestartLauncher = currentTheme.equals(themeInfo);
        	if(!justRestartLauncher){
            	// false表明第一次加载此主题,需要记录当前主题信息,同时需要重启Launcher
            	SettingsProvider.putString(mContext, SettingsProvider.SETTINGS_CURRENT_THEME, themeInfo);
        	}
            mTheme.handleTheme(justRestartLauncher, enableThemeMask);	
        }
	}

}
