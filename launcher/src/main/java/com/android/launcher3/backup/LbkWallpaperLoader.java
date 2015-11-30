package com.android.launcher3.backup;

import java.io.File;
import java.io.InputStream;

import android.app.WallpaperManager;
import android.content.Context;

import com.lenovo.launcher.ext.LauncherLog;

public class LbkWallpaperLoader {
	
	private final static String TAG = "LbkWallpaperLoader";
    private static final boolean LOGD = true;
	final Context mContext;
	final WallpaperManager mWallpaperManager;
	
    public static LbkWallpaperLoader get(Context context) {
		if(LbkUtil.isPreloadLbkFileExist()) {
			return new LbkWallpaperLoader(context);			
		}
		return null;
	}
    
	private LbkWallpaperLoader(Context context) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
		mWallpaperManager = WallpaperManager.getInstance(mContext);
	}
	
	public void applyDefaultWallpaper() {
		InputStream is = getDefaultWallpaperInputStream();
		if(null != is) {
			try {
				mWallpaperManager.setStream(is);
				LauncherLog.i(TAG, "applyDefaultWallpaper successful");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
					is = null;
					LbkUtil.closeUnZipFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			LauncherLog.i(TAG, "applyDefaultWallpaper unzip lbk file failed");
		}
	}
	
	private InputStream getDefaultWallpaperInputStream() {
		File lbkFile = LbkUtil.getLbkFileFromPreloadDirectory();
		if( null == lbkFile ) {
    		LauncherLog.i(TAG, "not find lbk file in the root directory");
    		return null;
		}
		
		try {
			LauncherLog.i(TAG, "getDefaultWallpaperInputStream : " + lbkFile.getAbsolutePath());
			return LbkUtil.unZip(lbkFile.getAbsolutePath(), LbkUtil.DEFAULT_WALLPAPER);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LauncherLog.i(TAG, "getDefaultWallpaperInputStream error happened in upzip file");
			return null;
		}
	}
}
