package com.klauncher.theme.ziptheme;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.klauncher.theme.ITheme;
import com.klauncher.theme.ThemeController;
import com.klauncher.theme.ThemeUtils;
import com.klauncher.theme.util.ThemeLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ZipTheme implements ITheme {

	public static final String TAG = "ZipTheme";
    Context mContext; // The context refs to launcher activity.
    String mZipThemeFilePath = "";
	
	public ZipTheme(Context context, String zipThemeFilePath) {
		super();
		this.mContext = context;
		this.mZipThemeFilePath = zipThemeFilePath;
	}

	@Override
	public boolean hasBgBitmap() {
		return false;
	}

	@Override
	public InputStream getDefaultWallpaper() {
        InputStream is = null;
        String absPath = mContext.getApplicationContext().getFilesDir() + "/themefolder/res/drawable-xxhdpi/wallpaper_grass.png";
        File file = new File(absPath);
        if(!file.exists()) {
            absPath = mContext.getApplicationContext().getFilesDir() + "/themefolder/res/drawable-xxhdpi/wallpaper_grass.jpg";
            file = new File(absPath);
            if(!file.exists()){
            	ThemeLog.i(TAG, "can not find the wallpaper");
                return null;
            }
        }
		try{
			is = new FileInputStream(file);
			return is;
		}catch(Exception e){
			ThemeLog.i(TAG, "getDefaultWallpaper exception happened!");
			return null;
		}
	}

    @Override
	public Bitmap getIconBitmap(ActivityInfo info) {
        Bitmap bitmap = null;
        /* Lenovo-SW 20150413 zhaoxin5 fix bug that get activity icon is null START */
        bitmap = ZipThemeUtils.retrieveCustomIconFromZipFile(info.getIconResource(), info.packageName, mContext);
        /* Lenovo-SW 20150413 zhaoxin5 fix bug that get activity icon is null END */
        return bitmap;
	}

    @Override
    public Bitmap getIconFromTheme(ActivityInfo info) {
        Bitmap bitmap = null;
        /* Lenovo-SW 20150413 zhaoxin5 fix bug that get activity icon is null START */
        bitmap = ZipThemeUtils.retrieveCustomIconFromZipFileOnly(info.getIconResource(), info.packageName, mContext);
        /* Lenovo-SW 20150413 zhaoxin5 fix bug that get activity icon is null END */
        return bitmap;
    }

    @Override
	public void handleTheme(boolean justReloadLauncher, boolean enableThemeMask) {
		if(justReloadLauncher){
            ThemeUtils.sendBroadcastToForceReloadLauncher(mContext, mZipThemeFilePath);
		} else {
			new ApplyZipThemeTask().execute(new Object[] { "", mZipThemeFilePath, "true"});
		}
	}

    private void exportResource() {
        File folder = new File(ThemeController.DEFAULT_ZIP_THEME_FOLDER);
        if (!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(mZipThemeFilePath);
        if (!file.exists()) {
            InputStream myInput = null;
            OutputStream myOutput = null;
            try {
                String themeFile = mZipThemeFilePath.substring(mZipThemeFilePath.lastIndexOf(File.separator) + 1);
                myOutput = new FileOutputStream(mZipThemeFilePath);
                myInput = mContext.getAssets().open(themeFile);
                byte[] buffer = new byte[1024];
                int length = myInput.read(buffer);
                while (length > 0) {
                    myOutput.write(buffer, 0, length);
                    length = myInput.read(buffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (myOutput != null) {
                        myOutput.flush();
                        myOutput.close();
                    }
                    if (myInput != null) {
                        myInput.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void cleanResource() {
        File file = new File(mZipThemeFilePath);
        if (file.exists()) {
            file.delete();
        }
    }
	
    /**
     * async task for apply zip theme 
     * @author zhangsp4
     *
     */
    private class ApplyZipThemeTask extends AsyncTask<Object, Void, Void> {
        @SuppressWarnings("unused")
        private String mFlag = null;
        
        @Override
        protected void onPreExecute() {
        }
        
        @Override
        protected Void doInBackground(Object... arg0) {
            final String themePath = arg0[1] == null ? null : arg0[1].toString();
            final boolean needCopy = arg0[2] == null ? true : Boolean.parseBoolean(arg0[2].toString());
            boolean result = true;
            if (themePath == null) {
                result = false;
                ZipThemeUtils.sendZipThemeApplyFailed();
                return null;
            }
            if (needCopy) {
                exportResource();
                result = ZipThemeUtils.copyThemePkgToLocal(mContext.getFilesDir().getAbsoluteFile(), themePath);
            }
            if (!result) {
            	ZipThemeUtils.sendZipThemeApplyFailed();
                cleanResource();
                return null; 
            }
            try {
            	ZipThemeUtils.unZipFileResDotZip(mContext.getFilesDir().getAbsoluteFile());
            } catch (Exception e) {
            	ThemeLog.i(TAG, "ApplyZipThemeTask error happened!", e);
            	ZipThemeUtils.sendZipThemeApplyFailed();
                cleanResource();
                return null;
            }
            // mark and preparations.
            //markThemeType(themeType);
            //checkCanShowActiveIcon();
            //setThemeIconBg(false);
            ZipThemeUtils.applyWallpaper(mContext, getDefaultWallpaper(), mZipThemeFilePath);
            ZipThemeUtils.setThemeIconBg(mContext);
            /*int color = getColorFromZipPkg(R.color.apps_icon_text_color, R.color.apps_icon_text_color);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mLa).edit();
            editor.putInt(PREF_ICON_TEXT_STYLE, color);
            editor.commit();
            mCurrentColor = color;*/
            
            if (arg0.length > 2) {
                mFlag = arg0[2] == null ? null : arg0[2].toString();
            }
            /*boolean rst = */
            //mContext.getModel().forceReloadForThemeApply();
            /*LauncherAppState.getInstance().getModel().changeThemeIcon(mLa, mFlag, true);*/
            ThemeUtils.sendBroadcastToForceReloadLauncher(mContext, mZipThemeFilePath);
            /*if(!rst){
            	ZipThemeUtils.sendZipThemeApplyFailed();
            }else{
            	ZipThemeUtils.sendZipThemeApplySuccess();
            }*/
            cleanResource();
            return null;
        }
   
    }
}
