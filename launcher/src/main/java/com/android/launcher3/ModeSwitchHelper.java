package com.android.launcher3;

import java.util.List;

import com.android.launcher3.settings.SettingsProvider;
import com.android.launcher3.settings.SettingsValue;
import com.klauncher.ext.LauncherLog;
import com.klauncher.launcher.R;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ModeSwitchHelper {
	private static final String TAG = "ModeSwitchHelper";
	public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
	public static final String SETTINGS_CURRENT_MODE = "Settings_Current_Mode";
	public static final String SETTINGS_FIRST_LOAD_MODE = "settings_first_load_mode";
	
	public enum Mode{
		VIBEUI,     // 单层,0 
		ANDROID     // 双层,1
	}
	
	public static final String getSettingsFirstLoadString(){
		return SETTINGS_FIRST_LOAD_MODE + "_" + LauncherAppState.getInstance().getCurrentLayoutString();
	}

	public static String getShortCutName(Context c){
		Resources rs = c.getResources();
		return LauncherAppState.isDisableAllApps() ? 
				rs.getString(R.string.switch_2_double_layer_shortcut) : 
				rs.getString(R.string.switch_2_single_layer_shortcut);
	}
		
	public static void onLauncherAfterBinding(Context c) {
		
		if(!isFirstLoad(c)){
			return;
		}
		setNotFirstLoad(c);
		LauncherLog.i("xixia", "add mode switch shortcut for " + LauncherAppState.getInstance().getCurrentLayoutString());
		
       /* Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);

        // 不允许重复创建
        addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
        // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
        // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
        // 屏幕上没有空间时会提示
        // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式

        // 名字
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getShortCutName(c));

        // 图标
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(c, R.mipmap.ic_launcher_home));

        // 设置关联程序
        Intent launcherIntent = new Intent(*//*Intent.ACTION_MAIN*//*); // 如果加上这个会在InstallShortcutReceiver中被拦截
        launcherIntent.setClass(c, Launcher.class);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        addShortcutIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        
        // 发送广播
        c.sendBroadcast(addShortcutIntent);*/
    }
	
	public static boolean isFirstLoad(Context c){
		boolean current = SettingsProvider.getBooleanCustomDefault(
        		c, getSettingsFirstLoadString(),
        		true);
		return current;
	}
	
	public static void setNotFirstLoad(Context c){
		SharedPreferences sharedPref = SettingsProvider.get(c);
        sharedPref.edit().putBoolean(getSettingsFirstLoadString(), false).commit();
	}
		
	public static void onModeSwitchClick(Context c){
		/** Lenovo-SW zhaoxin5 20150703 LANCHROW-201 START */
		/*Toast.makeText(c, LauncherAppState.isDisableAllApps() ? 
				c.getResources().getString(R.string.switch_2_double_layer_toast): 
				c.getResources().getString(R.string.switch_2_single_layer_toast), 
				Toast.LENGTH_LONG).show();*/
		/** Lenovo-SW zhaoxin5 20150703 LANCHROW-201 END */
		onLayerStateChanged(c);
		checkAndMigrateLenovoLauncherDesktop(c);
	}
	
	public static void loadDefaultDesktop(Context c){
		if(isFirstLoad(c)){
			LauncherLog.i(TAG, LauncherAppState.getInstance().getCurrentLayoutString() + " need load default desktop layout from XML");
			LauncherAppState.getLauncherProvider().setFlagEmptyDbCreated();
		}
		LauncherAppState.getInstance().reloadWorkspaceForcibly(LauncherModel.LOADER_FLAG_NONE);
	}
	
	private static void migrateLenovoLauncherDesktop(){
		LauncherLog.i(TAG, LauncherAppState.getInstance().getCurrentLayoutString() + " need load desktop layout from old Lenovo Launcher");
		LauncherAppState.getInstance().reloadWorkspaceForcibly(LauncherModel.LOADER_FLAG_MIGRATE_LENOVO_LAUNCHER);
	}
	
    private static void onLayerStateChanged(Context c) {
        boolean current = LauncherAppState.isDisableAllApps();
        SettingsValue.setDisableAllApps(c, !current);
        LauncherAppState.getInstance().setCurrentLayoutMode(!current ? Mode.VIBEUI : Mode.ANDROID);
        // Set new state
        /* SharedPreferences sharedPref = SettingsProvider.get(c);
        sharedPref.edit().putBoolean(Launcher.DISABLE_ALL_APPS_PROPERTY, !current).commit(); */
    }
	
	private static boolean isModeSwitchShortcut(Context c, Object o){
		
		if(o instanceof ShortcutInfo){
			Intent intent = ((ShortcutInfo)o).intent;
			if(null != intent){
				if(intent.getComponent().getPackageName().equals(c.getPackageName()) &&
	            		intent.getComponent().getClassName().equals(Launcher.class.getName())){
					return true;
				}
			}
		}
		
		if(o instanceof ComponentName){
			ComponentName cn = (ComponentName)o;
			if(cn.getPackageName().equals(c.getPackageName()) &&
            		cn.getClassName().equals(Launcher.class.getName())){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * 即使存在老的LenovoLauncher,也不加载其桌面数据
	 */
	/** Lenovo-SW zhaoxin5 20150707 add for LANCHROW-206 START */
	private static boolean sNotMigrateFormOldLLenovoLauncher = true;
	/** Lenovo-SW zhaoxin5 20150707 add for LANCHROW-206 END */
	public static int getLauncherModelLoaderTaskLoaderFlag(Context context){
		/** Lenovo-SW zhaoxin5 20150707 add for LANCHROW-206 START */
		if(sNotMigrateFormOldLLenovoLauncher) {
			return LauncherModel.LOADER_FLAG_NONE;
		}
		/** Lenovo-SW zhaoxin5 20150707 add for LANCHROW-206 END */
		// 如果存在老版本的LenovoLauncher,
		// 则要迁移老版本的数据,
		// 否则不迁移
		LauncherLog.i("xixia", "getLauncherModelLoaderTaskLoaderFlag for : " + LauncherAppState.getInstance().getCurrentLayoutString());
		if(LauncherAppState.getInstance().getCurrentLayoutMode() == Mode.VIBEUI && isFirstLoad(context)) {
			if(oldLenovoLauncherExist(context)){
				LauncherLog.i("xixia", "Need migrate from old Lenovo Launcher");
				return LauncherModel.LOADER_FLAG_MIGRATE_LENOVO_LAUNCHER;
			}
		}
		LauncherLog.i("xixia", "Do not need migrate from old Lenovo Launcher");
		return LauncherModel.LOADER_FLAG_NONE;
	}
	
	public static void checkAndMigrateLenovoLauncherDesktop(final Context context){
		if(LauncherAppState.getInstance().getCurrentLayoutMode() != Mode.VIBEUI || !isFirstLoad(context)) {
			loadDefaultDesktop(context);
			return;
		}
		if(!oldLenovoLauncherExist(context)){
			loadDefaultDesktop(context);
			return;
		}
		new AlertDialog.Builder(context).setTitle("检测到旧版本的LenovoLauncher,是否需要导入旧版本的数据？")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						migrateLenovoLauncherDesktop();
						return;
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						loadDefaultDesktop(context);
						return;
					}
				}).show();
	}
	
	public static String findLenovoLauncherProviderAuthority(Context context) {
	    // Gets PackageInfo about all installed apps
	    final List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
	    if (packs == null) {
	        return null;
	    }

	    for (PackageInfo pack : packs) {
	        // This gets the ProviderInfo of every ContentProvider in that app
	        final ProviderInfo[] providers = pack.providers;
	        if (providers == null) {
	            continue;
	        }

	        // This loops through the ContentProviders
	        for (ProviderInfo provider : providers) {

	            // And finally we look for the one with the correct permissions
	            // We use `startsWith()` and `endsWith()` since only the middle 
	            // part might change
	            final String readPermission = provider.readPermission;
	            final String writePermission = provider.writePermission;
	            if(readPermission != null && writePermission != null) {
	                final boolean readPermissionMatches = readPermission.startsWith("com.lenovo.") && readPermission.endsWith(".permission.READ_SETTINGS");
	                final boolean writePermissionMatches = writePermission.startsWith("com.lenovo.") && writePermission.endsWith(".permission.WRITE_SETTINGS");
	                if(readPermissionMatches && writePermissionMatches) {

	                    // And if we found the right one we return the authority
	                    return provider.authority;
	                }
	            }
	        }
	    }
	    return null;
	}

	/**
	 * error attempt to write a readonly database
	 * @param mode
	 * @param db
     */
	private static void clearDesktopDataForSpecificMode(Mode mode, SQLiteDatabase db) {
		String args = String.valueOf(mode.ordinal());
		// 先删除favorites表中的数据
		String table = "favorites";
		String whereClause = "for2layerWorkspace=?";
		
		db.beginTransaction();
        try {
        	db.delete(table, whereClause, new String[]{args});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        
        // 再删除workspaceScreens表中的数据
        table = "workspaceScreens";
		whereClause = "for2layerWorkspace=?";
 		
 		db.beginTransaction();
 		try {
			db.delete(table, whereClause, new String[]{args});
			db.setTransactionSuccessful();
		} finally {
 			db.endTransaction();
 		}
	}
	
	public static void clearDesktopDataForCurrentMode(SQLiteDatabase db) {
		clearDesktopDataForSpecificMode(LauncherAppState.getInstance().getCurrentLayoutMode(), db);
	}
	
	public static void clearVIBEUIModeData(SQLiteDatabase db) {
		clearDesktopDataForSpecificMode(Mode.VIBEUI, db);
	}
	
	static Uri mOldLenovoLauncherUri = null;
	public static boolean oldLenovoLauncherExist(Context context){
		String authority = findLenovoLauncherProviderAuthority(context); 
		if(authority != null){
			LauncherLog.i(TAG, "findLenovoLauncherProviderAuthority : " + authority);
			mOldLenovoLauncherUri = new Uri.Builder().scheme("content").authority(authority).appendPath("favorites").build();
			return true;
		}else{
			LauncherLog.i(TAG, "findLenovoLauncherProviderAuthority returns null!");
			return false;
		}
	}
	
	public static Uri getOldLenovoLauncherUri(Context context){
		if(oldLenovoLauncherExist(context)) return mOldLenovoLauncherUri;
		return null;
	}
}
