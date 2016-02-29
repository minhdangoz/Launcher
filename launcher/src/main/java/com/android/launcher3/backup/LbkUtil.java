package com.android.launcher3.backup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.ModeSwitchHelper.Mode;
import com.android.launcher3.backup.LbkLoader.LbkFileFilter;
import com.webeye.launcher.ext.LauncherLog;
import com.webeye.launcher.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.ContextThemeWrapper;

public class LbkUtil {
	
	private final static String TAG = "LbkUtil";
	public final static String DEFAULT_WALLPAPER = "wallpaper.png";
    public final static String DESC_FILE = "desc.xml";
    public final static String VIBEUI_BACKUP_LBK = "vibeui_backup.lbk";
    public final static String ANDROID_BACKUP_LBK = "android_backup.lbk";
    public final static String getBackupLBKName() {
    	if(LauncherAppState.getInstance().getCurrentLayoutMode() == Mode.ANDROID) {
    		return ANDROID_BACKUP_LBK;
    	}
    	return VIBEUI_BACKUP_LBK;
    }
	public static final String INPUT_ENCODING = "UTF-8";
	public final static String getXLauncherLbkBackupPath() {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +  
    			".IdeaDesktop" + File.separator + ".backup" + File.separator + ".localbackup";
		File f = new File(path);
		if(!f.exists()) {
			LauncherLog.i(TAG, "getXLauncherLbkBackupPath path not exists");
			f.mkdirs();
			LauncherLog.i(TAG, "getXLauncherLbkBackupPath : " + f.getAbsolutePath());
		}
		return f.getAbsolutePath();   
    }
	
	public final static String getPreloadLbkFilePath() {
		/**
		 * private static String DEFAULT_BACKUP_FILE_PATH = "/preload/etc/lenovo_profile.lbk";
		 * public static final String DEFAULT_BACKUP_FILE = new File(DEFAULT_BACKUP_FILE_PATH).exists() ? "//preload//etc//lenovo_profile.lbk" : "//system//etc//lenovo_profile.lbk"; 
		 */
		// 先加载preload/etc下的LBK，否则加载system/etc下的LBK
		String preloadLbk = File.separator + "preload" + File.separator + "etc" + File.separator + "lenovo_profile.lbk";
		String systemLbk  = File.separator + "system"  + File.separator + "etc" + File.separator + "lenovo_profile.lbk";
		if(new File(preloadLbk).exists()) {
			LauncherLog.i(TAG, "preload lbk exists!");
			return preloadLbk;
		} else {
			LauncherLog.i(TAG, "system lbk exists!");
			return systemLbk;
		}
	}
	
	public final static String saveShortcutBitmap(byte[] data) {
		// 保存成功返回当前保存的shortcut的名字
		Bitmap shortcut = BitmapFactory.decodeByteArray(data, 0, data.length);
		String shortcutFileName = System.currentTimeMillis() + ".png";
		String shortcutFilePath = getXLauncherLbkBackupTempPath() + shortcutFileName;
		
		File f = new File(getXLauncherLbkBackupTempPath(), shortcutFileName);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			if (null != fos) {
				shortcut.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			}
			LauncherLog.i(LbkPackager.TAG, shortcutFileName + " save success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
		return shortcutFileName;
	}
	
	
	
	/**
	 * 进行备份操作过程中的临时路径
	 * @return
	 */
	public final static String getXLauncherLbkBackupTempPath() {
		String path = getXLauncherLbkBackupPath() + File.separator + ".temp";
		File f = new File(path);
		if(!f.exists()) {
			LauncherLog.i(TAG, "getXLauncherLbkBackupTempPath path not exists");
			f.mkdirs();
			LauncherLog.i(TAG, "getXLauncherLbkBackupTempPath : " + f.getAbsolutePath());
		}
		return f.getAbsolutePath();
	}
	
	public final static void clearLbkBackupTempPath() {
		File f = new File(getXLauncherLbkBackupTempPath());
		if(f.isDirectory()) {
			File[] files = f.listFiles();
			LauncherLog.i(TAG, "lbk temp directory exists " + files.length + " files!");
			for(int i=0; i<files.length; i++) {
				files[i].delete();
			}
		}
	}
	
	public static boolean isPreloadLbkFileExist() {
		// 判断预置的LBK文件是否存在
		// 从XLauncher的备份路径中读取LBK文件
		return true;
//		File preloadLbkFile = new File(getPreloadLbkFilePath());
//		if (preloadLbkFile.exists()) {
//			return true;
//		} else {
//			return false;
//		}
	}
	
	public static byte[] inputStream2ByteArray(InputStream inStream) {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100]; // buff用于存放循环读取的临时数据
		int rc = 0;
		try {
			while ((rc = inStream.read(buff, 0, 100)) > 0) {
				swapStream.write(buff, 0, rc);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		byte[] data = swapStream.toByteArray(); // in_b为转换之后的结果
		
		if(null != inStream) {
			try {
				inStream.close();
				inStream = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(null != swapStream) {
			try {
				swapStream.close();
				swapStream = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return data;
	}
	
	public static File getLbkFileFromPreloadDirectory() {
		LauncherLog.i(TAG, "getLbkFileFromPreloadDirectory start!!!");
		Context context = LauncherAppState.getInstance().getContext();
		//String model = Build.MODEL.replace(" ", "-");
		//LauncherLog.i("wcrow", "model = " + model);
		String name = "default.lbk";
		float rows = LauncherAppState.getInstance().getDynamicGrid().getNumRows();
		if (rows > 5) {
			name = "default2.lbk";
		}
		File cacheFile = new File(context.getCacheDir(), name);
		if (cacheFile.exists()) {
			return cacheFile;
		}
		try {
			/*String[] files = context.getAssets().list("");
			for (int i = 0; i < files.length; i++) {
				if (files[i].equals(model + ".lbk")) {
					name = model + ".lbk";
					break;
				}
			}*/
			InputStream inputStream = context.getAssets().open(name);
			try {
				FileOutputStream outputStream = new FileOutputStream(cacheFile);
				try {
					byte[] buf = new byte[1024];
					int len;
					while ((len = inputStream.read(buf)) > 0) {
						outputStream.write(buf, 0, len);
					}
				} finally {
					outputStream.close();
				}
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			return null;
		}
		return cacheFile;
//		if(!isPreloadLbkFileExist()) {
//			return null;
//		}
//		return new File(getPreloadLbkFilePath());
	}

    public static boolean isXLauncherBackupLbkFileExist() {
    	// 判断在原来的XLauncher的备份路径下的LBK文件是否存在
    	// 从XLauncher的备份路径中读取LBK文件
    	File xlauncherBackupDirectory = new File(getXLauncherLbkBackupPath());
    	if(!xlauncherBackupDirectory.isDirectory()) {
    		return false;
    	}
    	LauncherLog.i(TAG, "XLauncher backuped directory is : " + xlauncherBackupDirectory.getAbsolutePath());
    	String[] lbkFiles = xlauncherBackupDirectory.list(new LbkFileFilter());
    	if(null != lbkFiles && lbkFiles.length > 0) {
    		for(int i=0; i<lbkFiles.length; i++) {
    			if(lbkFiles[i].equals(getBackupLBKName())) {
    				// 如果存在和当前模式匹配的LBK文件
    				return true;
    			}
    			if( !(lbkFiles[i].equals(ANDROID_BACKUP_LBK)) && !(lbkFiles[i].equals(VIBEUI_BACKUP_LBK))) {
    				// 如果存在一个Android还有Vibe都不匹配的LBK文件，肯定是老的备份文件
    				return true;
    			}
    		}
    	}
    	return false;
    }
	
    public static File getLbkFileFromXLauncherBackupDirectory() {
    	// 从XLauncher的备份路径中读取LBK文件
    	File xlauncherBackupDirectory = new File(getXLauncherLbkBackupPath());
    	if(!xlauncherBackupDirectory.isDirectory()) {
    		return null;
    	}
    	LauncherLog.i(TAG, "XLauncher backuped directory is : " + xlauncherBackupDirectory.getAbsolutePath());
    	String[] lbkFiles = xlauncherBackupDirectory.list(new LbkFileFilter());
    	String oldXLauncherLbkFileName = null;
    	if(null != lbkFiles && lbkFiles.length > 0) {
    		for(int i=0; i<lbkFiles.length; i++) {
        		LauncherLog.i(TAG, "XLauncher backuped Lbk file name is : " + lbkFiles[i]);
	    		if(lbkFiles[i].equals(getBackupLBKName())) {
					// 如果存在和当前模式匹配的LBK文件
					return new File(xlauncherBackupDirectory, lbkFiles[i]);
				}
				if( !(lbkFiles[i].equals(ANDROID_BACKUP_LBK)) && !(lbkFiles[i].equals(VIBEUI_BACKUP_LBK))) {
					// 如果存在一个Android还有Vibe都不匹配的LBK文件，肯定是老的备份文件
					oldXLauncherLbkFileName = lbkFiles[i];
				}
    		}
    		if(null != oldXLauncherLbkFileName) {
				return new File(xlauncherBackupDirectory, oldXLauncherLbkFileName);
    		}
    	}
    	return null;
    }
    
    /**
     * 功能:压缩多个文件成一个zip文件
     * <p>作者 陈亚标 Jul 16, 2010 10:59:40 AM
     * @param srcfile：源文件列表
     * @param zipfile：压缩后的文件
     */
    public static void zipFiles(File[] srcfile, File zipfile){
        byte[] buf=new byte[1024];
        try {
            //ZipOutputStream类：完成文件或文件夹的压缩
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            for(int i=0;i<srcfile.length;i++){
                LauncherLog.i(TAG, "zip " + srcfile[i]);
                FileInputStream in = new FileInputStream(srcfile[i]);
                out.putNextEntry(new ZipEntry(srcfile[i].getName()));
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
            LauncherLog.i(TAG, "zip success!");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	private static ZipFile mZipFile;
    /**   
     * return the InputStream of file in the ZIP  
     * @param zipFileString  name of ZIP   
     * @param fileString     name of file in the ZIP   
     * @return InputStream   
     * @throws Exception   
     */    
    public static InputStream unZip(String zipFileString, String fileString) throws Exception {    
        mZipFile = new ZipFile(zipFileString);
        ZipEntry zipEntry = mZipFile.getEntry(fileString);    
        if(zipEntry != null){
        	LauncherLog.i(TAG, "open zip stream");
            return mZipFile.getInputStream(zipEntry);
        } else {
            return null;
        }
    }
	
    public static void closeUnZipFile() {
    	if(mZipFile != null) {
    		try {
            	LauncherLog.i(TAG, "close zip stream");
				mZipFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public interface backupDialogClickListener {
    	public void onClick();
    }
    
    public static void restoreStartDialog(Context c, 
    		android.content.DialogInterface.OnClickListener positiveClickListener,
    		android.content.DialogInterface.OnClickListener negativeClickListener) {
    	AlertDialog.Builder builder = new Builder(new ContextThemeWrapper(c, R.style.AlertDialogCustom));
    	builder.setMessage(c.getResources().getString(R.string.local_restore_msg));
        builder.setTitle(c.getString(R.string.local_restore));
    	builder.setPositiveButton(c.getString(R.string.cling_dismiss), positiveClickListener);
    	builder.setNegativeButton(c.getString(R.string.cancel_action), 
    			(negativeClickListener == null) ? 
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				} : negativeClickListener);
    	builder.create().show();
    }
    
    public static void backupStartDialog(Context c, 
    		android.content.DialogInterface.OnClickListener positiveClickListener,
    		android.content.DialogInterface.OnClickListener negativeClickListener) {
    	AlertDialog.Builder builder = new Builder(new ContextThemeWrapper(c, R.style.AlertDialogCustom));
    	builder.setMessage(c.getResources().getString(R.string.local_backup_msg_first));
        builder.setTitle(c.getString(R.string.local_backup));
    	builder.setPositiveButton(c.getString(R.string.cling_dismiss), positiveClickListener);
    	builder.setNegativeButton(c.getString(R.string.cancel_action), 
    			(negativeClickListener == null) ? 
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				} : negativeClickListener);
    	builder.create().show();
    }
    
    public static void backupEndDialog(Context c, boolean success, 
    		android.content.DialogInterface.OnClickListener clickListener) {
    	AlertDialog.Builder builder = new Builder(new ContextThemeWrapper(c, R.style.AlertDialogCustom));
    	
        String msg = success ? c.getResources().getString(R.string.local_backup_toast_ok) :
    		c.getResources().getString(R.string.backup_toast_fail);
    	
    	builder.setMessage(msg);
        builder.setTitle(c.getString(R.string.local_backup));
    	builder.setPositiveButton(c.getString(R.string.cling_dismiss), 
    			(clickListener == null) ? new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				} : clickListener);
    	builder.create().show();
    }
    
    public static String getString(String str) {
    	return (null == str) ? "" : str;
    }
    
	public interface Version { public static final String ATTR_VERSION = "version"; }
    
    public interface KeyName {
    	public static final String ATTR_NAME = "name";
    	public static final String ATTR_KEY = "key";
    }
    
    public interface CellXCellYScreen {
    	public static final String ATTR_CELL_X = "cellX";
    	public static final String ATTR_CELL_Y = "cellY";
    	public static final String ATTR_SCREEN = "screen";
    }
    
    public interface PackageNameClassName {
    	public static final String ATTR_PACKAGE_NAME = "packagename";
    	public static final String ATTR_CLASS_NAME = "classname";
    }
    
    public interface PackageNameClassNameLeWidget {
    	public static final String ATTR_PACKAGE_NAME = "packageName";
    	public static final String ATTR_CLASS_NAME = "className";
    }
    
    public interface SpanXSpanY {
    	public static final String ATTR_SPAN_X = "spanX";
    	public static final String ATTR_SPAN_Y = "sapnY";
    }
    
    public interface ActionIconPackageIconResource {
    	public static final String ATTR_ACTION = "action";
    	public static final String ATTR_ICON_PACKAGE = "icon_package";
    	public static final String ATTR_ICON_RESOURCE = "icon_resource";
    }
    
    public interface Title { public static final String ATTR_TITLE = "title"; }
    public interface Container { public static final String ATTR_CONTAINER = "container"; }
    public interface Icon { public static final String ATTR_ICON = "icon"; }
    public interface Label { public static final String ATTR_LABEL = "label"; }
    public interface AppWidgetId { public static final String ATTR_APPWIDGET_ID = "appWidgetId"; }
    
    public interface CountryCodeValue {
    	public static final String ATTR_COUNTRYCODE = "countrycode";
    	public static final String ATTR_VALUE = "value";
    }
    
    public static class Profiles implements Version {
    	public static final String TAG = "Profiles";
    }
    
    public static class Profile implements KeyName {
    	public static final String TAG = "Profile";
    }
    
    public static class Folders {
    	public static final String TAG = "Folders";
    }
    
    public static class Folder implements KeyName {
    	public static final String TAG = "folder";
    }
    
    public static class Config implements CellXCellYScreen, Title, Container, Icon {
    	public static final String TAG = "config";
    }
    
    public static class AppList {
    	public static final String TAG = "applist";
    }
	
    public static class App implements PackageNameClassName, CellXCellYScreen, Title, Icon, Container, ActionIconPackageIconResource {
    	public static final String TAG = "app";
    }

    public static class Widgets {
    	public static final String TAG = "Widgets";
    }
    
    public static class Widget implements CellXCellYScreen, PackageNameClassName, SpanXSpanY, Label, AppWidgetId {
    	public static final String TAG = "widget";
    }
	
    public static class LEOSE2EWidgets {
    	public static final String TAG = "LEOSE2EWidgets";
    }
    
    public static class LEOSE2EWidget implements CellXCellYScreen, SpanXSpanY, Container, PackageNameClassNameLeWidget {
    	public static final String TAG = "LEOSE2EWidget";
    	
    	public static final String OLD_WEATHER_WIDGET_CLASS_NAME = "com.lenovo.laweather.activity.WeatherWidgetDefaultView";
    	public static final String OLD_WEATHER_WIDGET_PACKAGE_NAME = "com.lenovo.launcher";
    	public static final String NEW_WEATHER_WIDGET_CLASS_NAME = "com.lenovo.lewea.widget.WeatherWidget";
    	public static final String NEW_WEATHER_WIDGET_PACKAGE_NAME = "com.lenovo.lewea";
    }
    
    public static class ShortCuts {
    	public static final String TAG = "ShortCuts";
    }
    
    public static class ShortCut implements PackageNameClassName, CellXCellYScreen, Title, ActionIconPackageIconResource, Container, Icon {
    	public static final String TAG = "shortcut";
    }
    
    public static class Screens {
    	public static final String TAG = "Screens";
    }
    
    public static class Screen {
    	public static final String TAG = "screen";
    	
    	public static final String ATTR_SCREEN_ID = "screenId";	
    	public static final String ATTR_RANK = "rank";
    }
    
    public static class LabelList {
    	public static final String TAG = "labellist";
    }
    
    public static class LabelTag implements CountryCodeValue {
    	public static final String TAG = "label";
    }

    /** Lenovo-SW zhaoxin5 20150813 add for KOLEOSROW-580 START */
    public static class Settings {
    	public static final String TAG = "Settings";
    }
    
    public static class Setting {
    	public static final String TAG = "Setting";
    	
    	public static final String ATTR_TYPE = "type";
    	public static final String ATTR_CATEGORY = "category";
    	public static final String ATTR_NAME = "name";
    	public static final String ATTR_VALUE = "value";
    }
    /** Lenovo-SW zhaoxin5 20150813 add for KOLEOSROW-580 END */
}
