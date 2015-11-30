package com.android.launcher3.reaper;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.android.launcher3.backup.BackupProtos.Resource;
import com.android.launcher3.settings.SettingsValue;
import com.lenovo.launcher.ext.SystemProperties;
import com.lenovo.lps.reaper.sdk.AnalyticsTracker;
//import com.mobclick.android.MobclickAgent;
import com.webeye.launcher.R;


/* RK_ID: RK_LELAUNCHER_REAPER. AUT: zhangdxa DATE: 2012-03-01  */

public class Reaper {
    private static final String TAG ="Reaper";
    public static final String PREF_REAPER = "pref_reaper";
    
	public static final String REAPER_CATEGORY = "LeLauncher";
	public static final String REAPER_EVNET_TYPE = "ReaperEventType";
	public static final String REAPER_EVNET_VALUE = "ReaperEventValue";
	public static final String REAPER_EVNET_VALUE_TWO = "ReaperEventValueTwo";
	public static final String ACTION_REAPER = "com.lenovo.action.ACTION_REAPER"; 
	public static final String ACTION_REAPER_MAP = "com.lenovo.action.ACTION_REAPER_MAP"; 
	public static final String ACTION_REAPER_INIT = "com.lenovo.action.ACTION_REAPER_INIT";
	public static final String ACTION_REAPER_INIT_AGAIN = "com.lenovo.action.ACTION_REAPER_INIT_AGAIN";
	public static final String ACTION_REAPER_INIT_FORCE = "com.lenovo.action.ACTION_REAPER_INIT_FORCE";
	public static final String ACTION_REAPER_INIT_CMCC_FORCE = "com.lenovo.action.ACTION_REAPER_INIT_CMCC_FORCE";
	public static final long REAPER_INIT_AGAIN_INTERVAL =  24 * 60 * 60 * 1000L;
	public static final long REAPER_INIT_INTERVAL =  5 * 1000L;
	public static boolean bReaperInitForce = false;
	public static boolean bReaperInitCMCC = false;

	/*public static final String REAPER_EVENT_USE_LAUNCHER = "L00";
	public static final String REAPER_EVENT_INSTALL_PACKAGE = "L01";
	public static final String REAPER_EVENT_REMOVE_PACKAGE = "L02";
	public static final String REAPER_EVENT_APPLY_PROFILE = "L03";
	public static final String REAPER_EVENT_APPLY_THEME = "L04";
	public static final String REAPER_EVENT_PERSONAL_SETTING = "L05";
	public static final String REAPER_EVENT_ADD_WIDGET = "L06";
	public static final String REAPER_EVENT_REMOVE_WIDGET = "L07";
	public static final String REAPER_EVENT_GADGET_LOTUS= "L08";
	public static final String REAPER_EVENT_WIDGET_SPACE = "L09";
	public static final String REAPER_EVENT_ADD = "L10";
	public static final String REAPER_EVENT_WALLPAPER = "L11";
	public static final String REAPER_EVENT_THEME = "L12";
	public static final String REAPER_EVENT_EFFECTS = "L13";
	public static final String REAPER_EVENT_DESKTOP_SETTINGS = "L14";
	public static final String REAPER_EVENT_APPS_SETTINGS = "L15";
	public static final String REAPER_EVENT_SETTINGS = "L16";
	public static final String REAPER_EVENT_SECNE = "L17";
	public static final String REAPER_EVENT_ICON_STYLE = "L18";
	public static final String REAPER_EVENT_MAGIC_ONLINE = "L19";
	public static final String REAPER_EVENT_DESKTOP_CLEANUP = "L20";
	public static final String REAPER_EVENT_APPS_MANAGER = "L21";
	public static final String REAPER_EVENT_DOWNLOAD_MANAGER = "L22";
	public static final String REAPER_EVENT_FEEDBACK = "L23";
	public static final String REAPER_EVENT_VERSION_UPGRADE = "L24";
	public static final String REAPER_EVENT_ABOUT_US = "L25";
	public static final String REAPER_EVENT_LOCAL_SEARCH_NUM = "L26";
	public static final String REAPER_EVENT_NETWORK_SEARCH_NUM = "L27";
	public static final String REAPER_EVENT_DESKTOP_SETTING_ITEM = "L28";
	public static final String REAPER_EVENT_EFFECT_SETTING_ITEM = "L29";
	public static final String REAPER_EVENT_ICON_STYLE_ITEM = "L30";
	public static final String REAPER_EVENT_ADD_OWN_WIDGET = "L31";
	public static final String REAPER_EVENT_REMOVE_OWN_WIDGET = "L32";*/
	
	public static final String  REAPER_NO_LABEL_VALUE = "";
	public static final int REAPER_NO_INT_VALUE = -1;
	
	public static final int REAPER_PARAM_BASE = 1;
	public static final int REAPER_PARAM_EXTRA = 2;
	
	public static final String REAPER_EVENT_CATEGORY_DESKTOPADD = "DesktopAdd";
    //CHANGE MENU LAYOUT zhanglz1 20130116
	public static final String REAPER_EVENT_CATEGORY_APPPROMOTE = "Apppromote";
	public static final String REAPER_EVENT_CATEGORY_WALLPAPER = "WallPaper";
	public static final String REAPER_EVENT_CATEGORY_THEME = "Theme";
	public static final String REAPER_EVENT_CATEGORY_SCENE = "Scene";
	public static final String REAPER_EVENT_CATEGORY_WIDGET = "Widget";
	public static final String REAPER_EVENT_CATEGORY_EFFECT = "Effect";
	public static final String REAPER_EVENT_CATEGORY_SCREEN = "Screen";
	public static final String REAPER_EVENT_CATEGORY_DESKTOPSETTING = "DesktopSetting";
	public static final String REAPER_EVENT_CATEGORY_APPLIST = "Applist";
	public static final String REAPER_EVENT_CATEGORY_GESTURE = "Gesture";
	public static final String REAPER_EVENT_CATEGORY_SHARE = "Share";
	public static final String REAPER_EVENT_CATEGORY_STATISTICS = "Statistics";
	
	//yangmao add for Recommend 0106 start
	public static final String REAPER_EVENT_CATEGORY_LEJINGPIN = "LeJingpin";
		
	
	//REAPER_EVENT_CATEGORY_DESKTOPADD
	public static final String REAPER_EVENT_ACTION_DESKTOPADD_FROMMENU = "DesktopAddFromMenu";
	public static final String REAPER_EVENT_ACTION_DESKTOPADD_FROMLONG = "DesktopAddFromLong";
	public static final String REAPER_EVENT_ACTION_DESKTOPADD_APPLICATION = "DesktopAddApplication";
	public static final String REAPER_EVENT_ACTION_DESKTOPADD_SHORTCUT = "DesktopAddShortCut";
	public static final String REAPER_EVENT_ACTION_DESKTOPADD_FOLDER = "DesktopAddFolder";

	//REAPER_EVENT_CATEGORY_WALLPAPER
	public static final String REAPER_EVENT_ACTION_WALLPAPER_FROMADD = "WallPaperFromAdd";
	public static final String REAPER_EVENT_ACTION_WALLPAPER_FROMPERSONALIZED = "WallPaperFromPersonalized";
	public static final String REAPER_EVENT_ACTION_WALLPAPER_APPLY = "WallPaperApply";

   //CHANGE MENU LAYOUT zhanglz1 20130116
    public static final String REAPER_EVENT_ACTION_WALLPAPER_FROMMENU = "WallPaperFromMenu";
	//REAPER_EVENT_CATEGORY_APPPROMOTE
    public static final String REAPER_EVENT_ACTION_APPPROMOTE_ENTER = "ApppromoteEnter";

	//REAPER_EVENT_CATEGORY_THEME
	public static final String REAPER_EVENT_ACTION_THEME_ENTER = "ThemeEnter";
	public static final String REAPER_EVENT_ACTION_THEME_APPLAY = "ThemeApply";
	
	//REAPER_EVENT_CATEGORY_SCENE
	public static final String REAPER_EVENT_ACTION_SCENE_ENTER = "SceneEnter";
	public static final String REAPER_EVENT_ACTION_SCENE_APPLY = "SceneApply";
	
	//REAPER_EVENT_CATEGORY_WIDGET
	public static final String REAPER_EVENT_ACTION_WIDGET_ADD = "WidgetAdd";
	public static final String REAPER_EVENT_ACTION_WIDGET_IDEAADD = "IdeaWidgetAdd";
	public static final String REAPER_EVENT_ACTION_WIDGET_LONGDRAGDELETE = "WidgetLongDragDelete";
	public static final String REAPER_EVENT_ACTION_WIDGET_LONGCLICKDELETE = "WidgetLongClickDelete";
	public static final String REAPER_EVENT_ACTION_WIDGET_LONGDRAGDELETEIDEA = "WidgetLongDragDeleteIdea";
	public static final String REAPER_EVENT_ACTION_WIDGET_LONGCLICKDELETEIDEA = "WidgetLongClickDeleteIdea";
	public static final String REAPER_EVENT_ACTION_WIDGET_LOTUS = "WidgetLotus";
	public static final String REAPER_EVENT_ACTION_WIDGET_LOTUSWEATHER = "WidgetLotusWeather";
	public static final String REAPER_EVENT_ACTION_WIDGET_MAGICWEATHER = "WidgetMagicWeather";
	public static final String REAPER_EVENT_ACTION_WIDGET_TOGGLE = "WidgetToggle";
	public static final String REAPER_EVENT_ACTION_WIDGET_CLEANER = "WidgetCleaner";
	public static final String REAPER_EVENT_ACTION_WIDGET_SHORTCUT = "WidgetShortcut";
	
	//REAPER_EVENT_CATEGORY_EFFECT
	public static final String REAPER_EVENT_ACTION_EFFECT_DESKTOPPAGE = "EffectDesktopPage";
	public static final String REAPER_EVENT_ACTION_EFFECT_DESKTOPCYCLE = "EffectDesktopCycle";
	public static final String REAPER_EVENT_ACTION_EFFECT_APPLISTENTER = "EffectApplistEnter";
	public static final String REAPER_EVENT_ACTION_EFFECT_APPLISTFLIP = "EffectApplistFlip";
	public static final String REAPER_EVENT_ACTION_EFFECT_APPLISTENTER2 = "EffectApplistEnter2";
	public static final String REAPER_EVENT_ACTION_EFFECT_APPLISTFLIP2 = "EffectApplistFLIP2";
	public static final String REAPER_EVENT_ACTION_EFFECT_APPLY = "EffectApply";
	
	//REAPER_EVENT_CATEGORY_SCREEN
	public static final String REAPER_EVENT_ACTION_SCREEN_SCREENENTER = "ScreenEnter";
	public static final String REAPER_EVENT_ACTION_SCREEN_SCREENADD = "ScreenAdd";
	public static final String REAPER_EVENT_ACTION_SCREEN_SCREENCOUNT = "ScreenCount";
	public static final String REAPER_EVENT_ACTION_SCREEN_SCREENADD2 = "ScreenAdd2";
	
	//REAPER_EVENT_CATEGORY_DESKTOPSETTING
	public static final String REAPER_EVENT_ACTION_DESKTOPSETTING_ENTER = "DesktopSettingEnter";
	public static final String REAPER_EVENT_ACTION_DESKTOPSETTING_WEATHER = "WeatherSetting";
	public static final String REAPER_EVENT_ACTION_DESKTOPSETTING_LOCKSCREEN = "LockScreenSetting";
	public static final String REAPER_EVENT_ACTION_DESKTOPSETTING_CLEANUP = "CleanUpSetting";
	public static final String REAPER_EVENT_ACTION_DESKTOPSETTING_GESTURE = "GestureSetting";
	
	//REAPER_EVENT_CATEGORY_APPLIST
	public static final String REAPER_EVENT_ACTION_APPLIST_RECENT = "ApplistRecent";
	public static final String REAPER_EVENT_ACTION_APPLIST_RUNNING = "ApplistRunning";
	public static final String REAPER_EVENT_ACTION_APPLIST_SEARCH = "ApplistSearch";
	public static final String REAPER_EVENT_ACTION_APPLIST_SORT = "ApplistSort";
	public static final String REAPER_EVENT_ACTION_APPLIST_LIST = "ApplistList";
	public static final String REAPER_EVENT_ACTION_APPLIST_GRID = "ApplistGrid";
	public static final String REAPER_EVENT_ACTION_APPLIST_SEARCH_DOWNLOAD = "ApplistSearchfDownload";
	
	//REAPER_EVENT_CATEGORY_GESTURE
	public static final String REAPER_EVENT_ACTION_GESTURE_DOUBLECLICK = "DoubleClick";
	public static final String REAPER_EVENT_ACTION_GESTURE_WORKSPACESLIPUP = "WorkspaceSlipUp";
	public static final String REAPER_EVENT_ACTION_GESTURE_WORKSPACESLIPDOWN = "WorkspaceSlipDown";
	public static final String REAPER_EVENT_ACTION_GESTURE_WORKSPACETWOSLIPDOWN = "WorkspaceTwoSlipDown";
	public static final String REAPER_EVENT_ACTION_GESTURE_WORKSPACETWOSLIPOUT = "WorkspaceTwoSlipOut";
	public static final String REAPER_EVENT_ACTION_GESTURE_WORKSPACETWOSLIPIN = "WorkspaceTwoSlipIn";
	public static final String REAPER_EVENT_ACTION_GESTURE_APPLISTSLIPUP = "ApplistSlipUp";
	public static final String REAPER_EVENT_ACTION_GESTURE_APPLISTSLIPDOWN = "ApplistSlipDown";
	
	//yangmao add  for lejingpin_recommend start
	// REAPER_EVENT_CATEGORY_RECOMMEND 
	public static final String REAPER_EVENT_ACTION_LEJINGPIN_RECOMMAPPENTRY = "RecommAppEntry";
	public static final String REAPER_EVENT_ACTION_LEJINGPIN_APPRECOMPRENUM = "AppRecomPreNum";
	public static final String REAPER_EVENT_ACTION_LEJINGPIN_APPRECOMNUM = "AppRecomNum";
	public static final String REAPER_EVENT_ACTION_LEJINGPIN_APPRECOMDOWN = "AppRecomDown";
	public static final String REAPER_EVENT_ACTION_LEJINGPIN_APPRECOMDETAIL = "AppRecomDetail";
	public static final String REAPER_EVENT_ACTION_LEJINGPIN_APPRECOMREMARKL = "AppRecomRemarkl";
	//yangmao add end
	
	public static final String REAPER_EVENT_LOCAL_LOCK_NAME = "LocalLKScreenEntry";
	public static final String REAPER_EVENT_NET_LOCK_NAME = "RecommLKScreenEntry";
	public static final String REAPER_EVENT_LOCK_DOWNLOAD_NAME = "RecommLKScreenDown";
	public static final String REAPER_EVENT_LOCK_INSTALL_NAME = "RecommLKScreenInstall";
	public static final String REAPER_EVENT_LOCK_APPLY_NAME = "LocalLKScreenApply";
	
	
	//REAPER_EVENT_CATEGORY_SHARE
	public static final String REAPER_EVENT_ACTION_SHARE_ENTER= "ShareEnter";
	
	//REAPER_EVENT_CATEGORY_STATISTICS
	public static final String REAPER_EVENT_ACTION_STATISTICS_APPCLICKNAMEANDCOUNT = "AppClickNameAndCount";
	public static final String REAPER_EVENT_ACTION_STATISTICS_APPALL = "AppAll";
	
	public static final String LENOVO_BACKGROUND_DATA_ENABLE = "persist.backgrounddata.enable";
	
	 //add by yuanyc REAPER_EVENT_CATEGORY T9_SEARCH begin
    public static final String REAPER_EVENT_CATEGORY_SEARCH = "SearchApp";
    public static final String REAPER_EVENT_ACTION_SEARCH_LOCATEAPP = "SearchLocateApp";
    public static final String REAPER_EVENT_ACTION_SEARCH_OPENAPP = "SearchOpenApp";
    //add by yuanyc REAPER_EVENT_CATEGORY T9_SEARCH end
	
	//zhangqb3 add begin 
	//REAPER_EVENT_BIG_DATA_APPSCLASSFIY
	public static final String REAPER_EVENT_CATEGORY_BIGDATA_APPCLASSIFY = "AppClassify";	
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_FOLDERENTERY ="FolderEntery";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_APPINFOLDERRUN ="AppInFolderRun";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_APPOUTOFFOLDERRUN ="AppOutOfFolderRun";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_FOLDERRENAME ="FolderRename";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_FOLDERDELAPP ="FolderDelApp";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_FOLDERADDAPP ="FolderAddApp";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_FOLDERNEW ="FolderNew";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_FOLDERDEL ="FolderDel";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_APPSUM ="AppSum";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_APPSTARTSUM ="AppsStartSum";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_ATUOCLASSOFF ="AutoClassOff";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_ATUOCLASSON ="AutoClassOn";
    public static final String REAPER_EVENT_ACTCION_APPSCLASSIFY_ATUOCLASSINSTANT ="AutoClassInstant";
    //zhangqb3 add end
    static AnalyticsTracker sTracker = null;
    //private static final boolean TAG_REAPER_UMENG = false;
    
    //add by zhangdy3  start
    public static final String REAPER_EVENT_ACTION_OPEN_LAUNCHER_LOCK = "OpenLauncherLock";
    public static final String REAPER_EVENT_ACTION_OPEN_WALLPAPER_SCROLL = "OpenWallpaperScroll";
    public static final String REAPER_EVENT_ACTION_OPEN_ICON_AUTOSORT = "OenIconAutoSort";
    
    public static final String REAPER_EVENT_ACTION_SCROLL_UP_GESTURE = "ScrollUpGuesture";
    public static final String REAPER_EVENT_ACTION_SCROLL_DOWN_GESTURE = "ScrollDownGuesture";
    public static final String REAPER_EVENT_ACTION_DOUBLE_CLICK_GESTURE = "DoubleClickGuesture";
    //add by zhangdy3  end
    
    // add by zhaoxin5 20151118 for OLauncher
	public static final String REAPER_EVENT_CATEGORY_OLAUNCHER_STYLE_SWITCH = "OLauncherStyleSwitch";
	public static final String REAPER_EVENT_CATEGORY_OLAUNCHER_FIRST_MENU = "OLauncherFirstMenu";
	public static final String REAPER_EVENT_CATEGORY_OLAUNCHER_DESKTOP_SETTINGS = "OLauncherDesktopSettings";
	public static final String REAPER_EVENT_CATEGORY_OLAUNCHER_ALLAPPS_SETTINGS = "OLauncherAllAppsSettings";
	public static final String REAPER_EVENT_CATEGORY_OLAUNCHER_BACKUP_RECOVERY = "OLauncherBackupRecovery";
	
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_STYLE_SWITCH_ANDROID = "OLauncherStyleSwitchAndroid";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_STYLE_SWITCH_VIBEUI = "OLauncherStyleSwitchVibeui";

	public static final String REAPER_EVENT_ACTION_OLAUNCHER_FIRST_MENU_WALLPAPER = "OLauncherFirstMenuWallpaper";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_FIRST_MENU_SETTINGS = "OLauncherFirstMenuSettings";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_FIRST_MENU_DESKTOP_SETTINGS = "OLauncherFirstMenuDesktopSettings";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_FIRST_MENU_ADD_WIDGET = "OLauncherFirstMenuAddWidget";
	
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_DESKTOP_SCROLL_OPEN = "OLauncherDesktopSettingsDesktopScrollOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_DESKTOP_SCROLL_NOT_OPEN = "OLauncherDesktopSettingsDesktopScrollNotOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_WALLPAPER_SCROLL_OPEN = "OLauncherDesktopSettingsWallpaperScrollOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_WALLPAPER_SCROLL_NOT_OPEN = "OLauncherDesktopSettingsWallpaperScrollNotOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_DESKTOP_LOOP_OPEN = "OLauncherDesktopSettingsDesktopLoopOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_DESKTOP_LOOP_NOT_OPEN = "OLauncherDesktopSettingsDesktopLoopNotOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_AUTO_SORT_OPEN = "OLauncherDesktopSettingsAutoSortOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_AUTO_SORT_NOT_OPEN = "OLauncherDesktopSettingsAutoSortNotOpen";
	
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_ALLAPPS_SETTINGS_ALLAPPS_SCROLL_OPEN = "OLauncherAllAppsSettingsAllAppsScrollOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_ALLAPPS_SETTINGS_ALLAPPS_SCROLL_NOT_OPEN = "OLauncherAllAppsSettingsAllAppsScrollNotOpen";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_ALLAPPS_SETTINGS_ALLAPPS_SORT_TITLE = "OLauncherAllAppsSettingsAllAppsSortTitle";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_ALLAPPS_SETTINGS_ALLAPPS_SORT_LAUNCH_COUNT = "OLauncherAllAppsSettingsAllAppsSortLaunchCount";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_ALLAPPS_SETTINGS_ALLAPPS_SORT_INSTALL_TIME = "OLauncherAllAppsSettingsAllAppsSortInstallTime";
    
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_BACKUP_RECOVERY_BACKUP = "OLauncherBackupRecoveryBackup";
	public static final String REAPER_EVENT_ACTION_OLAUNCHER_BACKUP_RECOVERY_RECOVERY = "OLauncherBackupRecoveryRecovery";
    // add by zhaoxin5 20151118 for OLauncher
	
    public static int testNumber = 0;
	
	public static void reaperOn( final Context context)	{
		if( context == null )
			return;        
		if( sTracker == null){
		    sTracker = AnalyticsTracker.getInstance();
		}
		testNumber++;
		new Thread(new Runnable() {
			public void run() {
			    if( sTracker != null )  {
			    	setReportFlag(sTracker, context);
			        Log.i(TAG,"Reaper.reaperOn, call sTracker.initialize " + testNumber);
			        sTracker.initialize( context );
			    }

			   /* if( TAG_REAPER_UMENG ){
			        MobclickAgent.setSessionContinueMillis(1000); 
			        if( context != null)
			            MobclickAgent.updateOnlineConfig(contxt);
				    processReaperUmeng(context, Reaper.REAPER_EVENT_USE_LAUNCHER );
			    }*/
			}

		}).start();
	}
	
	 private static boolean getLenovoExperienceSwithValue(Context context) {
         boolean isEnable;
         
         String property = null;
         //String deviceModel = android.os.Build.MODEL;
         property = SystemProperties.get(LENOVO_BACKGROUND_DATA_ENABLE, "true");
         
         if(SettingsValue.getVerisonWWConfiguration(context)) {
        	 return false;
         }
         
         Log.i(TAG, "Lenovo User Experience Switch is == " + property);
         if (property.equals("false")) {
             isEnable = false;
         } else {
             isEnable = true;
         }
         return isEnable;
    }
	 
	 private static void setReportFlag(AnalyticsTracker tracker, Context context){
		 boolean isEnable = getLenovoExperienceSwithValue(context);
         if (isEnable) {
             tracker.enableReport();
         } else {
             tracker.disableReport();
         }
	 }

	//modify by zhangdxa 20121130
	/*public static void reaperOff() {
		if( !Launcher.bTagReaper)
			return;
		new Thread(new Runnable() {
			    public void run() {
			    	if (sTracker == null) {
				        sTracker = AnalyticsTracker.getInstance();
			        }
			        if( sTracker != null) {
                        sTracker.shutdown();	
                        sTracker = null;
		            }
			    }
		     }).start();
		
	}*/
	public static void reaperResume(final Context context) {
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
		if( bTagReaper  && 
	    		( context != null) ){
			if( reaperNetworkEnable(context)){
		        if (sTracker == null) {
			        sTracker = AnalyticsTracker.getInstance();
			    }
			    if(sTracker != null) {
			   	    Log.i(TAG,"Reaper.reaperResume");
			   	    sTracker.trackResume(context);
			    }
			    //if( TAG_REAPER_UMENG )
		        //    MobclickAgent.onResume(context);
			}
		}
	}
	public static void reaperPause(final Context context) {
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
		if( bTagReaper  && 
	    		( context != null) ){
			if( reaperNetworkEnable(context)){
		        if (sTracker == null) {
			        sTracker = AnalyticsTracker.getInstance();
			    }
		        if(sTracker != null) {
		    	    Log.i(TAG,"Reaper.reaperPause");
			        sTracker.trackPause(context);
		        }
		        //if( TAG_REAPER_UMENG )
		        //    MobclickAgent.onPause(context);
			}
		}
	}
	
	public static void processReaperLe(final Context context, String action, 
			String label, int value )	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
		if( bTagReaper  && 
			( context != null) &&
			( action != null )){
	        if( reaperNetworkEnable(context)){
	    	    if (sTracker == null) {
	    		    sTracker = AnalyticsTracker.getInstance();
	    	    }
	    	    if( sTracker != null ) {
	    		    Log.i(TAG,"Call LeLauncher Data Reaper, action:"+ action+", label:"+ label);
	    		    if( value == REAPER_NO_INT_VALUE ) {
	    			    sTracker.trackEvent(REAPER_CATEGORY, action, label, 1);
	    		    }else {
	    			    sTracker.trackEvent(REAPER_CATEGORY, action, label, value);
	    		    }
	    	    }
	        }
		}
	}
	/*public static void processReaperUmeng(final Context context, String reaperEventType ){
		if( Launcher.bTagReaper  && TAG_REAPER_UMENG && mReaperOn && ( context != null)){
	    	if( !ISWifiAvailable(context) && !getLauncherNetState(context)){
	    	    return;
	    	}
	    	Log.i("zdx","Launcher.processReaperUmeng, reaperEventType:"+reaperEventType );
	    	MobclickAgent.onEvent( context, reaperEventType );
	    }
	}*/

	//yangmao add start
	/*public static void reportSearch_Hawaii_DownlodEvent(final Context context,String lcaId ,
			String nettype,String packageName, String versionCode) {
        if (sTracker == null) {
			sTracker = AnalyticsTracker.getInstance();
		}
        if( Launcher.bTagReaper  && (sTracker != null ) && ( context != null)){
        	if( reaperNetworkEnable(context)){
		        sTracker.setParam(1, "appid", lcaId);
		        sTracker.setParam(2, "userid", null);
		        sTracker.setParam(3, "nettype", nettype);
		        sTracker.setParam(4, "packagename", packageName);
		        sTracker.setParam(5, "packageversion", versionCode);
		        new Thread(new Runnable() {
					@Override
					public void run() {
						Log.i("yangmao", "start trackEvent the hawaii download ");
						sTracker.trackEvent("Hawaii", "download_launcher", "" , 1);
					}
				}).start();
	        }
        }
    }*/
	//yangmao add end
	
	private static boolean getLauncherReaperState(final Context context) {
	    if( context == null ){
	    	return false;
	    }
       	if(  SettingsValue.isNetworkEnabled(context) ){
            return true;
       	}
       	return false;
	}
	
	private static boolean ISWifiAvailable(final Context context) {
        if( context == null) {
            return false;
        }
		ConnectivityManager connectivityManager = (ConnectivityManager) 
			context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo == null)
			return false;
		if ( ( activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI ) &&
			 activeNetInfo.isAvailable()) 
			return true;
		return false;
	}
	
	public static boolean ISNetworkAvailable(final Context context) {
        if( context == null) {
            return false;
        }
		ConnectivityManager connectivityManager = (ConnectivityManager) 
			context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo == null)
			return false;
		if ( activeNetInfo.isAvailable()) 
			return true;
		return false;
	}
	
    public static void scheduleReaperInit(Context context) {
    	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (am != null) {
	    	Intent intent = new Intent(ACTION_REAPER_INIT); 
	    	//Log.i("Reaper","*********************scheduleReaperInit ACTION_REAPER_INIT");
	    	PendingIntent sender = PendingIntent.getBroadcast(
	    			context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	    	am.cancel(sender);
			am.set( AlarmManager.RTC, System.currentTimeMillis()+REAPER_INIT_INTERVAL, sender);
		}    	
    }
    
    public static void scheduleReaperInitAgain(Context context) {
    	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (am != null) {
	    	Intent intent = new Intent(ACTION_REAPER_INIT_AGAIN); 
	    	//Log.i("Reaper","*********************scheduleReaperInitAgain");
	    	PendingIntent sender = PendingIntent.getBroadcast(
	    			context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	    	am.cancel(sender);
	    	
	        long t = System.currentTimeMillis();
	        Time time = new Time();
	        time.set(t);
	        long interval = (20-time.hour+24)* 60 * 60 * 1000L;
	        if(interval <=0 || interval > (44 * 60 * 60 * 1000)){
	        	interval = REAPER_INIT_AGAIN_INTERVAL;
	        }
			am.set( AlarmManager.RTC, System.currentTimeMillis()+interval, sender);
		}    	
    }
    
    public static void reaperTrackInit(Context context){
    	if (sTracker == null) {
			sTracker = AnalyticsTracker.getInstance();
		}
		if( sTracker != null ) {
			setReportFlag(sTracker, context);
			sTracker.trackEvent("__INITIAL__", "upload", "test", 0);
		}
    }
    
    public static boolean reaperNetworkEnable(Context context ){
        if (SettingsValue.getVerisonWWConfiguration(context)) {
            Log.i("Reaper", "Close reaper for ROW");
            return false;
        }
        if( ISWifiAvailable(context) || getLauncherReaperState(context)){
	        return true;
	    }
        return false;
    }
    
    public static void processReaper(final Context context, String category, String action, 
    		String label, int value )
	{
    	Log.i("Reaper","Reaper.processReaper, category:"+ category +", action:"+ action +", label:"+ label);
    	if( context == null)
    		return;
    	SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    	if( mSharedPreferences == null)
    		return;
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
		if( !bTagReaper ){
			return;
		}
    	if( ( category != null ) &&
    		( action != null )){
            sendDeepIntent(category,action,label,value,context);
        }
	}
    private static void sendDeepIntent(String category,String action,String label,int value,Context context){
        if( reaperNetworkEnable(context)){
    	    Intent reaperIntent = new Intent(Reaper.ACTION_REAPER);
    	    reaperIntent.putExtra("category", category);
    	    reaperIntent.putExtra("action", action);
    	    reaperIntent.putExtra("label" , label);
    	    reaperIntent.putExtra("value" , value);
    	    context.sendBroadcast(reaperIntent);
    	}
    }
    public static void processReaper(final Context context, String category, String action, 
    		HashMap<String, String> map, int value )
	{
    	Log.i("Reaper","Reaper.processReaper, category:"+ category +", action:"+ action +", map:"+ map);
    	SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
		if( !bTagReaper ){
			return;
		}
    	if( ( context != null) &&
    		( category != null ) &&
    		( action != null )){
    	        if( reaperNetworkEnable(context)){
    	        	//Log.i("Reaper","Reaper.processReaper, send Reaper.ACTION_REAPER_MAP");
    	        	Intent reaperIntent = new Intent(Reaper.ACTION_REAPER_MAP);
    	            reaperIntent.putExtra("category", category);
    	            reaperIntent.putExtra("action", action);
    	            reaperIntent.putExtra("map" , map);
    	            reaperIntent.putExtra("value" , value);
    	            context.sendBroadcast(reaperIntent);
    	        }
    	}
	}
    
    public static void processReaperEvent(final Context context, String category, String action, 
    		String label, int value )
	{
    	//Log.i(TAG,"***********************Reaper.processReaperEvent(), category:"+ category+", action:"+ action+", sTracker:"+ sTracker);
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
    	if( bTagReaper  && 
    			( context != null) &&
    			( category != null ) &&
    			( action != null )){
    	        if( reaperNetworkEnable(context)){
    	    	    if (sTracker == null) {
    	    	    	//Log.i(TAG,"***********************Reaper.processReaperEvent(),call AnalyticsTracker.getInstance()");
    	    		    sTracker = AnalyticsTracker.getInstance();
    	    	    }
    	    	    if( sTracker != null ) {
    	    		    Log.i(TAG,"Call LeLauncher Data Reaper, category:"+ category+", action:"+ action+", label:"+ label);
    	    		    if( label == null){
    	    		    	label = "";
    	    		    }
    	    		    
    	    		    setReportFlag(sTracker, context);
    	    		    if( value == REAPER_NO_INT_VALUE ) {
    	    			    sTracker.trackEvent(category, action, label, 1);
    	    		    }else {
    	    			    sTracker.trackEvent(category, action, label, value);
    	    		    }
    	    	    }
    	        }
    		}
		//Reaper.processReaperUmeng(context, reaperType);
	}
	 public static void processReaperEventMap(final Context context, String category, 
	    		String action, HashMap<String, String> map, int value )
		{
			SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
	    	if( bTagReaper  && 
	    			( context != null) &&
	    			( category != null ) &&
	    			( action != null )){
	    	        if( reaperNetworkEnable(context)){
	    	    	    if (sTracker == null) {
	    	    		    sTracker = AnalyticsTracker.getInstance();
	    	    	    }
	    	    	    if( sTracker != null ) {
	    	    		    Log.i(TAG,"Call LeLauncher Data Reaper, category:"+ category+", action:"+ action +", map:"+ map);
	    	    		    
	    	    		    setReportFlag(sTracker, context);
	    	    		    
	    	    		    if( ( map != null )&& (!map.isEmpty())){
	    	    		    	int count = 1;
	    	    				for(Entry<String, String> entry : map.entrySet()){
	    	    					String key = entry.getKey();
	    	    					String val = entry.getValue();
	    	    					if( (key != null) && (val != null)){
	    	    					    sTracker.setParam(count, key, val);
	    	    					}
	    	    		    	    count++;
	    	    				}
	    	    			}

	    	    		    if( value == REAPER_NO_INT_VALUE ) {
	    	    			    sTracker.trackEvent(category, action, "", 1);
	    	    		    }else {
	    	    			    sTracker.trackEvent(category, action, "", value);
	    	    		    }
	    	    	    }
	    	        }
	    		}
			//Reaper.processReaperUmeng(context, reaperType);
		}
    
    public static void reaperPageResume(final Context context, final String pageName) {
    	SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
    	if( bTagReaper  && 
    		( context != null) && 
    		( pageName != null)){
    		if( reaperNetworkEnable(context)){
	    	    if (sTracker == null) {
				    sTracker = AnalyticsTracker.getInstance();
			    }
			    if(sTracker != null) {
		    	    Log.i(TAG,"Reaper.trackPageResume, page name:"+pageName);
				    sTracker.trackPageResume(pageName);
			    }
    		}
		}
	}
    
    public static void reaperPagePause(final Context context,final String pageName) {
    	SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
    	if( bTagReaper  && 
        		( context != null) && 
        		( pageName != null)){
    		if( reaperNetworkEnable(context)){
                if (sTracker == null) {
    				sTracker = AnalyticsTracker.getInstance();
    			}
    			if(sTracker != null) {
    				Log.i(TAG,"Reaper.reaperPagePause, page name:"+pageName);
    				sTracker.trackPagePause(pageName);
    			}
    		}
    	}
	}
    
	public static void reaperUserAction(final Context context, 
			final String userActionName, final String pageName) 
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
		if( bTagReaper  && 
			( context != null) &&
			( userActionName != null) &&
			( pageName != null)){
			if( reaperNetworkEnable(context)){
	    	    if (sTracker == null) {
				    sTracker = AnalyticsTracker.getInstance();
			    }
			    if(sTracker != null) {
			    	Log.i(TAG,"Reaper.processReaperUserAction, user action:"+userActionName+", page name:"+pageName);
			    	sTracker.trackUserAction(userActionName,pageName);
			    }
			}
		}
	}
	
    public static boolean reaperNeedReport(final Context context, final String category, final String action) {
    	boolean bResult = false;
    	SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(PREF_REAPER, false);
		if( bTagReaper ){
			if (sTracker == null) {
				sTracker = AnalyticsTracker.getInstance();
			}
			if(sTracker != null) {
			    bResult = sTracker.needReport(category, action);
		    }
		}
		Log.i(TAG,"Reaper.reaperNeedReport, category:"+ category+", action:"+ action +", result:"+ bResult);
		return bResult;
	}
    
    public static void processReaperInitForce(final Context context )
	{
    	Log.i("Reaper","Reaper.processReaperInitForce");
    	bReaperInitForce = true;
    	Intent reaperIntent = new Intent(Reaper.ACTION_REAPER_INIT_FORCE);
    	context.sendBroadcast(reaperIntent);
    }
    public static void processReaperInitCmccForce(final Context context )
	{
    	Log.i("Reaper","Reaper.processReaperInitCmccForce");
    	bReaperInitCMCC = true;
    	Intent reaperIntent = new Intent(Reaper.ACTION_REAPER_INIT_CMCC_FORCE);
    	context.sendBroadcast(reaperIntent);
    }
    private Reaper() {
		// TODO Auto-generated constructor stub
	}

	public static void setReaperInitForce(boolean b) {
		// TODO Auto-generated method stub
		bReaperInitForce = b;
	}

	public static void setReaperInitCMCC(boolean b) {
		// TODO Auto-generated method stub
		bReaperInitCMCC = b;
	}

	/** Lenovo-SW zhaoxin5 20151118 add for Reaper support START */
	public static String getAllAppsSortAction(Resources r, Object o) {
		String str = o.toString();
		if (str.equals(r.getString(R.string.sort_mode_title))) {
			return REAPER_EVENT_ACTION_OLAUNCHER_ALLAPPS_SETTINGS_ALLAPPS_SORT_TITLE;
		} else if (str.equals(r.getString(R.string.sort_mode_launch_count))) {
			return REAPER_EVENT_ACTION_OLAUNCHER_ALLAPPS_SETTINGS_ALLAPPS_SORT_LAUNCH_COUNT;
		} else {
			return REAPER_EVENT_ACTION_OLAUNCHER_ALLAPPS_SETTINGS_ALLAPPS_SORT_INSTALL_TIME;
		}
	}
	/** Lenovo-SW zhaoxin5 20151118 add for Reaper support END */
}
