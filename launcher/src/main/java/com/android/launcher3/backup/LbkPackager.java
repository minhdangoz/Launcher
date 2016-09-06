package com.android.launcher3.backup;


import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.ModeSwitchHelper.Mode;
import com.klauncher.ext.LauncherLog;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 原来的6.3格式的Launcher的备份文件LBK格式的备份类
 * @author zhaoxin5
 *
 */
public class LbkPackager {

	public interface BackupListener {
		public void onBackupStart();
		public void onBackupEnd(boolean success);
	}
	
	public final static String TAG = "LbkPackager";
	public XmlSerializer mSerializer;
	private final static boolean LOGD = true;
	private static Context mContext = null;
	private static PackageManager mPackageManager;
	private final static String LBK_VERSION = "2.0";
	private final static Uri mLauncherFavoriteUri = LauncherSettings.Favorites.CONTENT_URI;
	private final static Uri mLauncherWorkspaceScreensUri = LauncherSettings.WorkspaceScreens.CONTENT_URI;
	private static BackupListener mListener;
	private final static String getErrorHappenedIn(String funcName) {
		return "Error happened in " + funcName;
	}
	private final static void logErrorHappenedIn(String funcName) {
		LauncherLog.i(TAG, getErrorHappenedIn(funcName));
		isErrorHappened = true;
	}
	
	private static boolean isErrorHappened = false;
	private static void startErrorTracker() {
		LauncherLog.i(TAG, "startErrorTracker");
		isErrorHappened = false;
	}
	private static void endErrorTracker() {
		LauncherLog.i(TAG, "endErrorTracker");
		//处理错误发生以后的情况
		File targetZip = new File(LbkUtil.getXLauncherLbkBackupPath() + File.separator + LbkUtil.getBackupLBKName());
		if(isErrorHappened) {
			LauncherLog.i(TAG, "endErrorTracker error happened");
			// 有错误发生
			// 删除已经生成的备份文件
			if(targetZip.exists()) {
				targetZip.delete();
			}
		} else {
			scanLbkFile(targetZip);
		}
		
	}
	
	private static void scanLbkFile(File f) {
		// 通知文件管理系统当前文件已更新,这样备份结束即可在电脑上查看到备份完成的文件
		Intent scanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		scanIntent.setData(Uri.fromFile(f));
		mContext.sendBroadcast(scanIntent);
	}
	
	private File getBackup2DescFile() {
		String parentPath = LbkUtil.getXLauncherLbkBackupTempPath();
		File backupDescFile = new File(parentPath + File.separator + LbkUtil.DESC_FILE);
		if(backupDescFile.exists()) {
			backupDescFile.delete();
		}
		try {
			backupDescFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			logErrorHappenedIn("getBackup2DescFile");
		}
		LauncherLog.i(TAG, "backupDescFile : " + backupDescFile.getAbsolutePath());
		return backupDescFile;
	}
	
	static LbkPackager mInstance = null;
	public static LbkPackager getInstance(Context context, BackupListener l) {
		if(null == mInstance) {
			mInstance = new LbkPackager();
		}
		init(context,l);
		return mInstance;
	}

	private static void init(Context context,BackupListener l) {
		mContext = context;
		mPackageManager = mContext.getPackageManager();
		mListener = l;
	}
	
	private LbkPackager() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unused")
	public static void startBackup(Context c, BackupListener l) {
		try {
			getInstance(c, l).packager();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("startBackup");
		}
	}
	
    static String getContainerFormFavoritesItem(int i) {
    	
    	if(LauncherSettings.Favorites.CONTAINER_HOTSEAT == i) {
    		return "hotseat";
    	}
    	
    	if(LauncherSettings.Favorites.CONTAINER_DESKTOP == i) {
    		return "desktop";
    	}
    	
    	return "";
    }
	
    /**
     * 头部备份的类
     * @author zhaoxin5
     *
     */
    private class PackageHead {
    	
    	FileOutputStream mFileOutputStream;
    	
    	public PackageHead() {
    		try {
	    		XmlPullParserFactory factory;
	    		// 首先清空temp目录
	    		LbkUtil.clearLbkBackupTempPath();
	    		mFileOutputStream = new FileOutputStream(getBackup2DescFile());
				factory = XmlPullParserFactory.newInstance();
				mSerializer = factory.newSerializer();
				// 设置流输出对象
				mSerializer.setOutput(mFileOutputStream, LbkUtil.INPUT_ENCODING);
			} catch (Exception e) {
				e.printStackTrace();
				logErrorHappenedIn("PackageHead");
			}
		}
    	
    	public void startDocument() {
    		try {
				/* 
	             * startDocument(String encoding, Boolean standalone)encoding代表编码方式 
	             * standalone  用来表示该文件是否呼叫其它外部的文件。 
	             * 若值是 ”true” 表示没有呼叫外部规则文件，若值是 ”false” 则表示有呼叫外部规则文件。默认值是 “yes”。 
	             */  
				mSerializer.startDocument(LbkUtil.INPUT_ENCODING, true);
    		} catch (Exception e) {
    			e.printStackTrace();
    			logErrorHappenedIn("startDocument");
    		}
    	}
    	
    	public void endDocument() {
    		try {
				mSerializer.endDocument();
				if(null != mFileOutputStream) {
					mFileOutputStream.close();
				}
    		} catch (Exception e) {
    			e.printStackTrace();
    			logErrorHappenedIn("endDocument");
    		}
    	}
    }
    
	public abstract class PackagerBase {
		
		public final String mTag;
		PackagerBase(String tag) {
			mTag = tag;
		}
		
		private final void startTag() throws Exception {
			if(LOGD) {
				LauncherLog.i(TAG, "startTag : " + mTag);	
			}
			mSerializer.startTag(null, mTag);
		}
		
		private final void endTag() throws Exception {
			if(LOGD) {
				LauncherLog.i(TAG, "endTag : " + mTag);	
			}
			mSerializer.endTag(null, mTag);
		}

		protected void attribute() throws Exception {};
		
		public final void startPackage() throws Exception {
			startTag();
			attribute();
		}
		public final void endPackage() throws Exception {
			endTag();
		}
	}
	
	private class ProfilesPackager extends PackagerBase {
		
		final String version;
		private ProfilesPackager(String version) {
			super(LbkUtil.Profiles.TAG);
			this.version = version;
		}

		@Override
		protected void attribute() throws Exception {
			// TODO Auto-generated method stub
			
			if(LOGD) {
				StringBuilder sb = new StringBuilder();
				sb.append("{ ");
				sb.append(LbkUtil.Profiles.ATTR_VERSION + ":" + version);
				sb.append(" }");
				LauncherLog.i(TAG, sb.toString());
			}
			
			mSerializer.attribute(null, LbkUtil.Profiles.ATTR_VERSION, version);
		}
	}
	
	private class ProfilePackager extends PackagerBase {
		
		final String key;
		final String name;
		public ProfilePackager(String key, String name) {
			super(LbkUtil.Profile.TAG);
			this.key = key;
			this.name = name;
		}

		@Override
		protected void attribute() throws Exception {
			// TODO Auto-generated method stub
			
			if(LOGD) {
				StringBuilder sb = new StringBuilder();
				sb.append("{ ");
				sb.append(LbkUtil.Folder.ATTR_KEY + ":" + key + " , ");
				sb.append(LbkUtil.Folder.ATTR_NAME + ":" + name);
				sb.append(" }");
				LauncherLog.i(TAG, sb.toString());
			}
			
			mSerializer.attribute(null, LbkUtil.Profile.ATTR_KEY, key);
			mSerializer.attribute(null, LbkUtil.Profile.ATTR_NAME, name);
		}
	}
	
	private class FoldersPackager extends PackagerBase {
		public FoldersPackager() {
			super(LbkUtil.Folders.TAG);
		}
	}
	
	private class FolderPackager extends PackagerBase {
		
		final String key;
		final String name;
		public FolderPackager(String key, String name) {
			super(LbkUtil.Folder.TAG);
			this.key = key;
			this.name = name;
		}

		@Override
		protected void attribute() throws Exception {
			// TODO Auto-generated method stub
			
			if(LOGD) {
				StringBuilder sb = new StringBuilder();
				sb.append("{ ");
				sb.append(LbkUtil.Folder.ATTR_KEY + ":" + key + " , ");
				sb.append(LbkUtil.Folder.ATTR_NAME + ":" + name);
				sb.append(" }");
				LauncherLog.i(TAG, sb.toString());
			}
			
			mSerializer.attribute(null, LbkUtil.Folder.ATTR_KEY, key);
			mSerializer.attribute(null, LbkUtil.Folder.ATTR_NAME, name);
		}
	}
	
	private class ConfigPackager extends PackagerBase {
		
		final String cellX;
		final String cellY;
		final String title;
		final String screen;
		final String container;
		final String icon;
		
		public ConfigPackager(String cellX, String cellY, String title, String screen, String container, String icon) {
			super(LbkUtil.Config.TAG);
			
			this.cellX = cellX;
			this.cellY = cellY;
			this.title = title;
			this.screen = screen;
			this.container = container;
			this.icon = icon;
		}

		@Override
		protected void attribute() throws Exception {
			// TODO Auto-generated method stub
			
			if(LOGD) {
				StringBuilder sb = new StringBuilder();
				sb.append("{ ");
				sb.append(LbkUtil.Config.ATTR_CELL_X + ":" + cellX + " , ");
				sb.append(LbkUtil.Config.ATTR_CELL_Y + ":" + cellY + " , ");
				sb.append(LbkUtil.Config.ATTR_TITLE + ":" + title + " , ");
				sb.append(LbkUtil.Config.ATTR_SCREEN + ":" + screen + " , ");
				sb.append(LbkUtil.Config.ATTR_ICON + ":" + icon + " , ");
				sb.append(LbkUtil.Config.ATTR_CONTAINER + ":" + container);
				sb.append(" }");
				LauncherLog.i(TAG, sb.toString());
			}
			
			mSerializer.attribute(null, LbkUtil.Config.ATTR_CELL_X, cellX);
			mSerializer.attribute(null, LbkUtil.Config.ATTR_CELL_Y, cellY);
			mSerializer.attribute(null, LbkUtil.Config.ATTR_TITLE,	title);
			mSerializer.attribute(null, LbkUtil.Config.ATTR_SCREEN, screen);
			mSerializer.attribute(null, LbkUtil.Config.ATTR_CONTAINER, container);
			mSerializer.attribute(null, LbkUtil.Config.ATTR_ICON, icon);
		}
	}
	
	private class AppListPackager extends PackagerBase {
		public AppListPackager() {
			super(LbkUtil.AppList.TAG);
		}
	}
	
	private class AppPackager extends PackagerBase {
		
		final String cellX;
		final String cellY;
		final String title;
		final String screen;
		final String container;
		final String packageName;
		final String className;
		final String action;
		final String iconPackage;
		final String iconResource;
		final boolean isShortCut;
		final byte[] icon;
		
		public AppPackager(String cellX, String cellY, String title, String screen, String container, 
				String packageName, String className, String action, String iconPackage, String iconResource, byte[] icon) {
			super(LbkUtil.App.TAG);
			
			this.cellX = cellX;
			this.cellY = cellY;
			this.title = title;
			this.screen = screen;
			this.container = container;
			this.packageName = packageName;
			this.className = className;
			this.action = action;
			this.iconPackage = iconPackage;
			this.iconResource = iconResource;
			this.icon = icon;
			if(packageName == null && className == null && action != null) {
				isShortCut = true;
			} else {
				isShortCut = false;
			}
		}

		@Override
		protected void attribute() throws Exception {
			// TODO Auto-generated method stub
			
			if(LOGD) {
				StringBuilder sb = new StringBuilder();
				sb.append("{ ");
				sb.append(LbkUtil.App.ATTR_CELL_X + ":" + cellX + " , ");
				sb.append(LbkUtil.App.ATTR_CELL_Y + ":" + cellY + " , ");
				sb.append(LbkUtil.App.ATTR_TITLE + ":" + title + " , ");
				sb.append(LbkUtil.App.ATTR_SCREEN + ":" + screen + " , ");
				sb.append(LbkUtil.App.ATTR_CONTAINER + ":" + container + " , ");
				sb.append(LbkUtil.App.ATTR_PACKAGE_NAME + ":" + packageName + " , ");
				sb.append(LbkUtil.App.ATTR_ACTION + ":" + action + " , ");
				sb.append(LbkUtil.App.ATTR_ICON_PACKAGE + ":" + iconPackage + " , ");
				sb.append(LbkUtil.App.ATTR_ICON_RESOURCE + ":" + iconResource + " , ");
				sb.append(LbkUtil.App.ATTR_CLASS_NAME + ":" + className);
				sb.append(" }");
				LauncherLog.i(TAG, sb.toString());
			}
			
			mSerializer.attribute(null, LbkUtil.App.ATTR_CELL_X, LbkUtil.getString(cellX));
			mSerializer.attribute(null, LbkUtil.App.ATTR_CELL_Y, LbkUtil.getString(cellY));
			mSerializer.attribute(null, LbkUtil.App.ATTR_TITLE,	LbkUtil.getString(title));
			mSerializer.attribute(null, LbkUtil.App.ATTR_SCREEN, LbkUtil.getString(screen));
			mSerializer.attribute(null, LbkUtil.App.ATTR_CONTAINER, LbkUtil.getString(container));
			if(!isShortCut) {
				mSerializer.attribute(null, LbkUtil.App.ATTR_PACKAGE_NAME, LbkUtil.getString(packageName));
				mSerializer.attribute(null, LbkUtil.App.ATTR_CLASS_NAME, LbkUtil.getString(className));	
			} else {
				mSerializer.attribute(null, LbkUtil.App.ATTR_ACTION, LbkUtil.getString(action));
				mSerializer.attribute(null, LbkUtil.App.ATTR_ICON_PACKAGE, LbkUtil.getString(iconPackage));
				mSerializer.attribute(null, LbkUtil.App.ATTR_ICON_RESOURCE, LbkUtil.getString(iconResource));	
				mSerializer.attribute(null, LbkUtil.App.ATTR_ICON, packageIcon(icon));
			}
		}
	}
	
	private class ScreensPackager extends PackagerBase {
		public ScreensPackager() {
			super(LbkUtil.Screens.TAG);
		}
	}
	
	private class ScreenPackager extends PackagerBase {
		final String screenId;
		final String rank;
		
		public ScreenPackager(String screenId, String rank) {
			super(LbkUtil.Screen.TAG);
			
			this.screenId = screenId;
			this.rank = rank;
		}
		
		@Override
		protected void attribute() throws Exception {
			// TODO Auto-generated method stub
			
			if(LOGD) {
				StringBuilder sb = new StringBuilder();
				sb.append("{ ");
				sb.append(LbkUtil.Screen.ATTR_SCREEN_ID + ":" + screenId + " , ");
				sb.append(LbkUtil.Screen.ATTR_RANK + ":" + rank);
				sb.append(" }");
				LauncherLog.i(TAG, sb.toString());
			}
			
			mSerializer.attribute(null, LbkUtil.Screen.ATTR_SCREEN_ID, screenId);
			mSerializer.attribute(null, LbkUtil.Screen.ATTR_RANK, rank);
		}
	}
	
	private class WidgetsPackager extends PackagerBase {
		public WidgetsPackager() {
			super(LbkUtil.Widgets.TAG);
		}
	}	
	
	private class WidgetPackager extends PackagerBase {
		
		final String cellX;
		final String cellY;
		final String spanX;
		final String sapnY;
		final String screen;
		final String packageName;
		final String className;
		final String label;
		final String appWidgetId;
		
		public WidgetPackager(String cellX, String cellY, String spanX, String sapnY, String label, String screen,  
				String packageName, String className, String appWidgetId) {
			super(LbkUtil.Widget.TAG);
			
			this.cellX = cellX;
			this.cellY = cellY;
			this.spanX = spanX;
			this.sapnY = sapnY;
			this.label = label;
			this.screen = screen;
			this.packageName = packageName;
			this.className = className;
			this.appWidgetId = appWidgetId;
		}

		@Override
		protected void attribute() throws Exception {
			// TODO Auto-generated method stub
			
			if(LOGD) {
				StringBuilder sb = new StringBuilder();
				sb.append("{ ");
				sb.append(LbkUtil.Widget.ATTR_CELL_X + ":" + cellX + " , ");
				sb.append(LbkUtil.Widget.ATTR_CELL_Y + ":" + cellY + " , ");
				sb.append(LbkUtil.Widget.ATTR_SPAN_X + ":" + spanX + " , ");
				sb.append(LbkUtil.Widget.ATTR_SPAN_Y + ":" + sapnY + " , ");
				sb.append(LbkUtil.Widget.ATTR_LABEL + ":" + label + " , ");
				sb.append(LbkUtil.Widget.ATTR_SCREEN + ":" + screen + " , ");
				sb.append(LbkUtil.Widget.ATTR_APPWIDGET_ID + ":" + appWidgetId + " , ");
				sb.append(LbkUtil.Widget.ATTR_PACKAGE_NAME + ":" + packageName + " , ");
				sb.append(LbkUtil.Widget.ATTR_CLASS_NAME + ":" + className);
				sb.append(" }");
				LauncherLog.i(TAG, sb.toString());
			}
			
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_CELL_X, cellX);
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_CELL_Y, cellY);
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_SPAN_X, spanX);
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_SPAN_Y, sapnY);
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_LABEL, label);
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_SCREEN, screen);
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_PACKAGE_NAME, packageName);
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_CLASS_NAME, className);
			mSerializer.attribute(null, LbkUtil.Widget.ATTR_APPWIDGET_ID, appWidgetId);
		}
	}
	
	private class ShortcutsPackager extends PackagerBase {
		public ShortcutsPackager() {
			super(LbkUtil.ShortCuts.TAG);
		}
	}	
	
	private class ShortCutPackager extends PackagerBase {
		
		final String cellX;
		final String cellY;
		final String title;
		final String screen;
		final String container;
		final String packageName;
		final String className;
		final String action;
		final String iconPackage;
		final String iconResource;
		final boolean isShortCut;
		final byte[] icon;
		
		public ShortCutPackager(String cellX, String cellY, String title, String screen, String container, 
				String packageName, String className, String action, String iconPackage, String iconResource, byte[] icon) {
			super(LbkUtil.ShortCut.TAG);
			
			this.cellX = cellX;
			this.cellY = cellY;
			this.title = title;
			this.screen = screen;
			this.container = container;
			this.packageName = packageName;
			this.className = className;
			this.action = action;
			this.iconPackage = iconPackage;
			this.iconResource = iconResource;
			this.icon = icon;
			if(packageName == null && className == null && action != null) {
				isShortCut = true;
			} else {
				isShortCut = false;
			}
		}

		@Override
		protected void attribute() throws Exception {
			// TODO Auto-generated method stub
			
			if(LOGD) {
				StringBuilder sb = new StringBuilder();
				sb.append("{ ");
				sb.append(LbkUtil.ShortCut.ATTR_TITLE + ":" + title + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_CELL_X + ":" + cellX + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_CELL_Y + ":" + cellY + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_SCREEN + ":" + screen + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_PACKAGE_NAME + ":" + packageName + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_CLASS_NAME + ":" + className + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_ACTION + ":" + action + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_ICON_PACKAGE + ":" + iconPackage + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_ICON_RESOURCE + ":" + iconResource + " , ");
				sb.append(LbkUtil.ShortCut.ATTR_CONTAINER + ":" + container);
				sb.append(" }");
				LauncherLog.i(TAG, sb.toString());
			}
			
			mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_CELL_X, LbkUtil.getString(cellX));
			mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_CELL_Y, LbkUtil.getString(cellY));
			mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_TITLE, LbkUtil.getString(title));
			mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_SCREEN, LbkUtil.getString(screen));
			mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_CONTAINER, LbkUtil.getString(container));
			if(!isShortCut) {
				mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_PACKAGE_NAME, LbkUtil.getString(packageName));
				mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_CLASS_NAME, LbkUtil.getString(className));	
			} else {
				mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_ACTION, LbkUtil.getString(action));
				mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_ICON_PACKAGE, LbkUtil.getString(iconPackage));
				mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_ICON_RESOURCE, LbkUtil.getString(iconResource));	
				mSerializer.attribute(null, LbkUtil.ShortCut.ATTR_ICON, packageIcon(icon));
			}
		}
	}
	
	private String packageIcon(byte[] icon) {
		if(null != icon) {
			return LbkUtil.saveShortcutBitmap(icon);
		}
		return "";
	}
	
	private void packager() {
		startErrorTracker();
		if(mListener != null) {
			mListener.onBackupStart();
		}
		
		startPackager();
		
		endErrorTracker();
		if(mListener != null) {
			mListener.onBackupEnd(!isErrorHappened);
		}
	}
	
	/**
	 * 备份的准备工作
	 * 写XML的头部
	 */
	private void startPackager() {
		PackageHead head = new PackageHead();
		head.startDocument();
		
		packageProfiles();
		
		head.endDocument();
		
		// 已经备份到desc.xml中了
		// 需要将desc.xml文件给压缩成lbk文件
		zipLbkFile();
	}
	
	private void zipLbkFile() {
		// 检查将要压缩的目录下是否存在LBK文件,存在则删除
		File targetZip = new File(LbkUtil.getXLauncherLbkBackupPath() + File.separator + LbkUtil.getBackupLBKName());
		if(targetZip.exists()) {
			targetZip.delete();
		}
		try {
			File tempDirectory = new File(LbkUtil.getXLauncherLbkBackupTempPath());
			File[] files = tempDirectory.listFiles();
			LbkUtil.zipFiles(files, targetZip);
			// LbkUtil.zipFolder(LbkUtil.getXLauncherLbkBackupTempPath(), targetZip.getAbsolutePath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logErrorHappenedIn("zipLbkFile");
		}
	}
	
	/**
	 * 分发下去,进行各种字段的备份
	 * @throws Exception
	 */
	private void packageProfiles() {
		ProfilesPackager profilesPackager = new ProfilesPackager(LBK_VERSION);
		try {
			profilesPackager.startPackage(); // 最外层的Profiles
			
			packageProfile();
			
			profilesPackager.endPackage(); // 最外层的Profiles
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageProfiles");
		}
	}
	
	/**
	 * 压缩"Profile"标签
	 */
	private void packageProfile() {
		ProfilePackager profilePackager = new ProfilePackager("", "");
		try {
			profilePackager.startPackage();

			packageFolders();
			packageWidgets();
			packageShortCuts();
			packageScreens();
			
			profilePackager.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageProfile");
		}
	}
	
	/***
	 * 压缩文件夹外面最大的Foldes标签
	 */
	private void packageFolders() {
		FoldersPackager folders = new FoldersPackager();
		try {
			folders.startPackage();
			dispatchPackageFolder();
			folders.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageFolders");
		}
	}
	
	private void dispatchPackageFolder() {
		String[] projection = new String[] { "cellX", "cellY", "title", "screen", "container", "_id", "itemType" };
		String selection = "itemType=? and for2layerWorkspace=?";
		String[] selectionArgs = new String[] {  
				String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_FOLDER),  
				String.valueOf(LauncherAppState.getInstance().getCurrentLayoutInt()) };
		String sortOrder = "title ASC";
		final ContentResolver resolver = mContext.getContentResolver();
		Cursor c = null;
		try {
			c = resolver.query(mLauncherFavoriteUri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("dispatchPackageShortcut");
			return;
		}
		if(null != c) {
			if(c.getCount() > 0) {
				final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
				final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
				final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
				final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
				final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
				final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
				final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
				
				while(c.moveToNext()) {
                    final int itemType = c.getInt(itemTypeIndex);
                    int cellX = c.getInt(cellXIndex);
                    final int cellY = c.getInt(cellYIndex);
                    int screen = c.getInt(screenIndex);
                    int container = c.getInt(containerIndex);
                    final String title = c.getString(titleIndex);
                    final int id = c.getInt(idIndex);
                    
                    if (itemType == Favorites.ITEM_TYPE_FOLDER) {
                    	String icon = String.valueOf(id) + ".png";
                    	String containerStr = getContainerFormFavoritesItem(container);
                        if(container == LauncherSettings.Favorites.CONTAINER_HOTSEAT 
                        		&& LauncherAppState.getInstance().getCurrentLayoutMode() == Mode.ANDROID) {
                        	// android模式下,备份hotseat上的内容
                        	// 如果大于1则要将其往前挪一个,
                        	// 因为解析的地方会出错
                        	if(screen > 1) {
                        		screen = screen - 1;
                        	}
                        	if(cellX > 1) {
                        		cellX = cellX - 1;
                        	}
                        }
                    	packageFolder("", "", String.valueOf(cellX), String.valueOf(cellY), title, String.valueOf(screen), containerStr, String.valueOf(id), icon);
                    }
				}
			} // c.getCount() > 0
			c.close();
			c = null;
		} // null != c
	}
	
	/**
	 * 压缩folder标签
	 */
	private void packageFolder(String key, String name, String cellX, String cellY, 
			String title, String screen, String container, String id, String icon) {
		FolderPackager folder = new FolderPackager(key, name);
		try {
			folder.startPackage();
			
			// 首先压缩的是文件夹本身的属性信息
			packageConfig(cellX, cellY, title, screen, container, icon);
			// 然后压缩文件夹内部的应用的信息
			packageAppList(id);
			
			folder.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageFolder");
		}
	}
	
	private void packageConfig(String cellX, String cellY, String title, String screen, String container, String icon) {
		ConfigPackager config = new ConfigPackager(cellX, cellY, title, screen, container, icon);
		try {
			config.startPackage();
			config.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageConfig");
		}
	}
	
	private void packageAppList(String parentContainerId) {
		AppListPackager appList = new AppListPackager();
		try {
			appList.startPackage();
			
			// need to search the specific apps and package them
			// 需要检索出所有放置在桌面上的应用
			String[] projection = new String[] { "cellX", "cellY", "title", "screen", "intent", "container", "iconPackage", "iconResource", "itemType", "icon" };
			String selection = "container=? and for2layerWorkspace=?";
			String[] selectionArgs = new String[] { 
					parentContainerId, 
					String.valueOf(LauncherAppState.getInstance().getCurrentLayoutInt()) };
			String sortOrder = "title ASC";
			dispatchPackageShortcutAndApp(projection, selection, selectionArgs, sortOrder, true);
			
			appList.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageAppList");
		}
	}
	
	private void packageApp(String cellX, String cellY, String title, String screen, String container, 
			String packageName, String className, String action, String iconPackage, String iconResource, byte[] icon) {
		AppPackager app = new AppPackager(cellX, cellY, title, screen, container, packageName, className, action, iconPackage, iconResource, icon);
		try {
			app.startPackage();
			app.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageApp");
		}
	}
	
	/**
	 * 压缩widget
	 */
	private void packageWidgets() {
		WidgetsPackager widgets = new WidgetsPackager();
		try {
			widgets.startPackage();
			dispatchPackageWidget();
			widgets.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageWidgets");
		}
	}
	
	private void dispatchPackageWidget() {
		String[] projection = new String[] { "cellX", "cellY", "spanX", "spanY", "title", "screen", 
				"intent", "container", "itemType", "appWidgetProvider", "appWidgetId" };
		String selection = "container=? and itemType=? and for2layerWorkspace=?";
		String[] selectionArgs = new String[] { 
				String.valueOf(LauncherSettings.Favorites.CONTAINER_DESKTOP), 
				String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET),  
				String.valueOf(LauncherAppState.getInstance().getCurrentLayoutInt()) };
		String sortOrder = "title ASC";
		
		final ContentResolver resolver = mContext.getContentResolver();
		Cursor c = null;
		try {
			c = resolver.query(mLauncherFavoriteUri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("dispatchPackageShortcut");
			return;
		}
		if(null != c) {
			if(c.getCount() > 0) {
				final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
				final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
				final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
				final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);
				final int appWidgetProviderIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.APPWIDGET_PROVIDER);
				final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
				final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
				final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
				final int appWidgetIdIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.APPWIDGET_ID);
				
				while(c.moveToNext()) {
                    final int itemType = c.getInt(itemTypeIndex);
                    final int cellX = c.getInt(cellXIndex);
                    final int cellY = c.getInt(cellYIndex);
                    final int screen = c.getInt(screenIndex);
                    final String title = c.getString(titleIndex);
                    final int spanX = c.getInt(spanXIndex);
                    final int spanY = c.getInt(spanYIndex);
                    final int appWidgetId = c.getInt(appWidgetIdIndex);
                    
                    if (itemType == Favorites.ITEM_TYPE_APPWIDGET) {
                    	
                    	final String appWidgetProviderStr = c.getString(appWidgetProviderIndex);
                    	ComponentName cn = ComponentName.unflattenFromString(appWidgetProviderStr);
                    	boolean hasPackage = true;
                        try {
                            mPackageManager.getReceiverInfo(cn, 0);
                        } catch (Exception e) {
                            LauncherLog.i("xixia", "Can't find widget provider: " + appWidgetProviderStr);
                            hasPackage = false;
                        }

                        if (hasPackage) {
                            packageWidget(String.valueOf(cellX), String.valueOf(cellY), 
                            		String.valueOf(spanX), String.valueOf(spanY), String.valueOf(screen), cn.getPackageName(), cn.getClassName(), title,
                            			String.valueOf(appWidgetId));
                        }   	
                    	
                    }
				}
			} // c.getCount() > 0
			c.close();
			c = null;
		} // null != c
		
	}
	
	private void packageWidget(String cellX, String cellY, String spanX, String sapnY, String screen, 
			String packageName, String className, String label, String appWidgetId) {
		WidgetPackager widget = new WidgetPackager(cellX, cellY, spanX, sapnY, (label == null) ? "" : label, screen, packageName, className, appWidgetId);
		try {
			widget.startPackage();
			widget.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageWidget");
		}
		
	}
	
	/**
	 * 压缩"ShortCuts"标签
	 */
	private void packageShortCuts() {
		ShortcutsPackager shortcuts = new ShortcutsPackager();
		try {
			shortcuts.startPackage();
			
			// 需要检索出所有放置在桌面上的应用
			String[] projection = new String[] { "cellX", "cellY", "title", "screen", "intent", "container", "iconPackage", "iconResource", "itemType", "icon" };
			String selection = "(container=? or container=?) and (itemType=? or itemType=?) and for2layerWorkspace=?";
			String[] selectionArgs = new String[] { 
					String.valueOf(LauncherSettings.Favorites.CONTAINER_DESKTOP), 
					String.valueOf(LauncherSettings.Favorites.CONTAINER_HOTSEAT), 
					String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT), 
					String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_APPLICATION), 
					String.valueOf(LauncherAppState.getInstance().getCurrentLayoutInt()) };
			String sortOrder = "title ASC";
			dispatchPackageShortcutAndApp(projection, selection, selectionArgs, sortOrder, false);
			
			shortcuts.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageShortCuts");
		}
	}
	
	private void dispatchPackageShortcutAndApp(String[] projection, String selection, String[] selectionArgs, String sortOrder, boolean isTagIsApp) {
		final ContentResolver resolver = mContext.getContentResolver();
		Cursor c = null;
		try {
			c = resolver.query(mLauncherFavoriteUri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("dispatchPackageShortcut");
			return;
		}
		if(null != c) {
			if(c.getCount() > 0) {
				final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
				final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
				final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
				final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
				final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
				final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
				final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
				final int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
				final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
				final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
				
				while(c.moveToNext()) {
                    final int itemType = c.getInt(itemTypeIndex);
                    int cellX = c.getInt(cellXIndex);
                    final int cellY = c.getInt(cellYIndex);
                    int screen = c.getInt(screenIndex);
                    int container = c.getInt(containerIndex);
                    final String title = c.getString(titleIndex);
                    final String intentStr = c.getString(intentIndex);
                    final String iconPackage = c.getString(iconPackageIndex);
                    final String iconResource = c.getString(iconResourceIndex);
                    
                    final byte[] icon = c.getBlob(iconIndex);
                    
                    if (itemType != Favorites.ITEM_TYPE_FOLDER 
                    		&& itemType != LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET) {
                    	// 桌面上的APP或者是ShortCut,并解析得到他们的intent
                    	// 得到packagName和className
                        final Intent intent;
                        final ComponentName cn;

						LauncherLog.i(TAG, "dispatchPackageShortcutAndApp title "+ title +  "intentStr : " + intentStr);
                        try {
                            intent = Intent.parseUri(intentStr, 0);
                        } catch (Exception e) {
                            // bogus intent?
                            Launcher.addDumpLog(TAG,
                                    "skipping invalid intent uri", true);
                            continue;
                        }

                        String containerStr = getContainerFormFavoritesItem(container);
                        if(container == LauncherSettings.Favorites.CONTAINER_HOTSEAT 
                        		&& LauncherAppState.getInstance().getCurrentLayoutMode() == Mode.ANDROID) {
                        	// android模式下,备份hotseat上的内容
                        	// 如果大于1则要将其往前挪一个,
                        	// 因为解析的地方会出错
                        	if(screen > 1) {
                        		screen = screen - 1;
                        	}
                        	if(cellX > 1) {
                        		cellX = cellX - 1;
                        	}
                        }
                        if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                        	cn = intent.getComponent();
                            final String packageName = cn.getPackageName();
                            final String className = cn.getClassName();
                            if(!isTagIsApp) {
                            	packageShortcut(String.valueOf(cellX), String.valueOf(cellY), title, 
                            			String.valueOf(screen), containerStr, packageName, className, null, null, null, null);
                            } else {
                            	packageApp(String.valueOf(cellX), String.valueOf(cellY), title, 
                            			String.valueOf(screen), String.valueOf(container), packageName, className, null, null, null, null);
                            }
                        } else if (itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
                        	if(!isTagIsApp) {
                            	packageShortcut(String.valueOf(cellX), String.valueOf(cellY), title, 
                            			String.valueOf(screen), containerStr, null, null, intent.toUri(0), iconPackage, iconResource, icon);
                        	} else {
                            	packageApp(String.valueOf(cellX), String.valueOf(cellY), title, 
                            			String.valueOf(screen), String.valueOf(container), null, null, intent.toUri(0), iconPackage, iconResource, icon);
                        	}
                        }
                    }
				}
			} // c.getCount() > 0
			c.close();
			c = null;
		} // null != c
	}
	
	private void packageShortcut(String cellX, String cellY, String title, String screen, String container, 
			String packageName, String className, String action, String iconPackage, String iconResource, byte[] icon) {
		ShortCutPackager shortcut = 
				new ShortCutPackager(cellX, cellY, title, screen, container, packageName, className, action, iconPackage, iconResource, icon);
		try {
			shortcut.startPackage();
			shortcut.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageShortcut");
		}
	}
	
	private void packageScreens() {
		// 备份Screens
		ScreensPackager screensPackager = new ScreensPackager();
		try {
			screensPackager.startPackage();
			
			// 需要检索出所有放置在桌面上的应用
			String[] projection = new String[] { "_id", "screenRank" };
			String selection = "for2layerWorkspace=?";
			String[] selectionArgs = new String[] {  
					String.valueOf(LauncherAppState.getInstance().getCurrentLayoutInt()) };
			dispatchPackageScreen(projection, selection, selectionArgs, null);
			
			screensPackager.endPackage();
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("packageScreens");
		}
	}
	
	private void packageScreen(int screenId, int rank) {
		ScreenPackager screenPackager = new ScreenPackager(String.valueOf(screenId), String.valueOf(rank));
		try {
			screenPackager.startPackage();
			screenPackager.endPackage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logErrorHappenedIn("packageScreen");
		}
	}
	
	private void dispatchPackageScreen(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final ContentResolver resolver = mContext.getContentResolver();
		Cursor c = null;
		try {
			c = resolver.query(mLauncherWorkspaceScreensUri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			e.printStackTrace();
			logErrorHappenedIn("dispatchPackageScreen");
			return;
		}
		if(null != c) {
			if(c.getCount() > 0) {
				final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.WorkspaceScreens._ID);
				final int rankIndex = c.getColumnIndexOrThrow(LauncherSettings.WorkspaceScreens.SCREEN_RANK);
				
				while(c.moveToNext()) {
                    final int id = c.getInt(idIndex);
                    int rank = c.getInt(rankIndex);
                    
                    packageScreen(id, rank);
                    
				} // while
			} // c.getCount() > 0
			c.close();
			c = null;
		} // null != c
	}
}
