package com.klauncher.theme.ziptheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.klauncher.theme.Utilities;
import com.klauncher.theme.ThemeUtils;
import com.klauncher.theme.util.ThemeLog;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ZipThemeUtils {
	private static final String TAG = "ZipThemeUtils";
	public static final String THEME_FOLDER = "themefolder";
	public static final String RES_ZIP_FILE = "res.zip";
	public static final String RES = "res";
	public static final String DRAWABLE_XXHDPI = "drawable-xxhdpi";
	public static final String POSTFIX = ".png";
	public static final String ZIP_FILE_THEME_FOLDER_PATH = File.separator + THEME_FOLDER + File.separator + RES + File.separator + DRAWABLE_XXHDPI + File.separator;
	
	public static void sendZipThemeApplyFailed(){
		ThemeLog.i(TAG, "sendZipThemeApplyFailed");
	}
	
	public static void sendZipThemeApplySuccess(){
		ThemeLog.i(TAG, "sendZipThemeApplySuccess");
	}
	private static int sIconDpi = 0;	
    @SuppressWarnings("unused")
    private Drawable findDrawableByResourceName(String name, Context context) {
        if (context == null)
            return null;
        try {
            int resID = ThemeUtils.getDrawableId(context, name);
            if (resID == 0) {
                return null;
            }
            sIconDpi = ThemeUtils.updateIconDpi(context);
            Drawable drawable = context.getResources().getDrawableForDensity(resID, sIconDpi);
            return drawable;
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }
	
    public static void setThemeIconBg(Context launcherApplication) {
    	
    	ThemeUtils.sThemeBgBitmap[0] = null;
    	ThemeUtils.sThemeBgBitmap[1] = null;
    	ThemeUtils.sThemeBgBitmap[2] = null;

    	int width = Utilities.getIconWidth(launcherApplication);
        Drawable drawable = loadDrawable(ThemeUtils.THEME_ICON_BG_NAME, launcherApplication);
        if (drawable == null) {
        	ThemeUtils.sThemeBgBitmap[0] = null;
        	ThemeUtils.sThemeBgBitmap[1] = null;
        	ThemeUtils.sThemeBgBitmap[2] = null;
        } else {
        	ThemeLog.i("xixia", "setThemeIconBg not null");
        	ThemeUtils.sThemeBgBitmap[0] = resizeDrawable(drawable, width, launcherApplication);
            drawable = loadDrawable(ThemeUtils.THEME_ICON_FG_NAME, launcherApplication);
            ThemeUtils.sThemeBgBitmap[1] = resizeDrawable(drawable, width, launcherApplication);
            drawable = loadDrawable(ThemeUtils.THEME_ICON_MASK_NAME, launcherApplication);
            ThemeUtils.sThemeBgBitmap[2] = resizeDrawable(drawable, width, launcherApplication);
        }
        
    }
	
    private static Bitmap resizeDrawable(Drawable drawable, int width, Context launcherApplication) {
        if (drawable.getIntrinsicWidth() != width) {
            return Utilities.createIconBitmapForZipTheme(drawable, launcherApplication);
        } else {
            return ((BitmapDrawable) drawable).getBitmap();
        }
    }
    
    private static Bitmap findIconBitmapByIdNameFromZip(String iconName, Context launcherApplication, String pkg, int iconId) {
        if (launcherApplication == null){
            return null;
        }
        Drawable drawable = loadDrawable(iconName, launcherApplication);
        if (drawable == null) {
        	Bitmap bg = ThemeUtils.getThemeIconBg()[0];
        	Bitmap fg = ThemeUtils.getThemeIconBg()[1];
        	Bitmap mask = ThemeUtils.getThemeIconBg()[2];
        	Resources res = null;
			try {
				//ThemeLog.i("xixia", "pkg:"+pkg+",iconId:"+iconId);
				res = launcherApplication.getPackageManager().getResourcesForApplication(pkg);
				sIconDpi = ThemeUtils.updateIconDpi(launcherApplication);
	        	drawable = res.getDrawableForDensity(iconId, sIconDpi);
			} catch (NameNotFoundException e) {
				ThemeLog.i(TAG, "findIconBitmapByIdNameFromZip error", e);
			}
            return Utilities.composeIcon(drawable, bg, fg, mask, launcherApplication);
        }
        return Utilities.createIconBitmapForZipTheme(drawable, launcherApplication);
    }
	
    @SuppressWarnings("deprecation")
    public static Drawable loadDrawable(String iconName, Context launcherApplication) {
        InputStream is = null;
        String absPath = launcherApplication.getApplicationContext().getFilesDir() +"/themefolder/res/drawable-xxhdpi/"+iconName + ".png";
        try {
            File file = new File(absPath);
            if(file.exists()){
                is = new FileInputStream(new File(absPath));
            }else{
                ThemeLog.i(TAG, "Load Drawable "+ iconName + " file not found...Failed!");
            }
            if(is != null){
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap= BitmapFactory.decodeStream(is, null, options);
                return new BitmapDrawable(bitmap);
            }
            else{
                return null;
            }
        } catch (Exception e) {
            ThemeLog.i(TAG, "Load Drawable "+ iconName + " ...Failed!", e);
        } finally {
            try {
                if(is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
	public static Bitmap retrieveCustomIconFromZipFile(int iconId, String packageName,
	            Context context) {
        if (context == null || packageName == null || iconId == 0) {
            ThemeLog.i(TAG, "launcherApplication == null || packageName == null || iconId == 0");
            return null;
        }
        Resources res = ThemeUtils.getResourcesForApplication(context, packageName);
        if (res != null) {
            try {
                // get icon id name
                String iconName = res.getResourceName(iconId);
                // parse the icon name
                if (iconName != null) {
                    if (ThemeUtils.isDefalutApp(iconName)) {
                        String resName = iconName.substring(iconName.indexOf(File.separator) + 1);
                        return findIconBitmapByIdNameFromZip(resName, context, packageName, iconId);
                    } else {
                        String[] resName = new String[2];
                        // parse the id name
                        ThemeUtils.parseResourceName(iconName, resName, packageName);
                        // return retrieveIcon(resName, mLapp, mLapp,
                        // packageName);
                        Bitmap ret = null;
                        if (resName != null) {
                            ret = findIconBitmapByIdNameFromZip(resName[0], context, packageName, iconId);

                            if (ret == null && resName[1] != null && !resName[1].equals(resName[0])) {
                                ret = findIconBitmapByIdNameFromZip(resName[1], context, packageName, iconId);
                            }
                        }
                        return ret;
                    }
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }
	
	public static void applyWallpaper(Context context, InputStream is, String tag){
		ThemeUtils.applyWallpaper(context, is, tag);
	}
	
    public static boolean copyThemePkgToLocal(File dest, String themePath) {
        //File dest = mLa.getApplicationContext().getFilesDir().getAbsoluteFile();
        File themeFolder = new File(dest.getAbsolutePath() + File.separator + THEME_FOLDER);
        ThemeLog.i(TAG, "themeFolder  "+themeFolder.getAbsoluteFile());
        if(themeFolder.exists()){
        	ThemeLog.i(TAG, "--> theme folder exists");
        }else{
        	ThemeLog.i(TAG, "--> theme folder not exists");
            themeFolder.mkdir();
        }
        File zipFile = new File(themeFolder.getAbsolutePath()+ File.separator + RES_ZIP_FILE);
        if(zipFile.exists()){
        	ThemeLog.d(TAG, "--> zipFile exists, delete it");
            zipFile.delete();
        }
        ThemeLog.i(TAG, "zipFile  "+zipFile.getAbsoluteFile());
        return copyFile(themePath, zipFile.getAbsolutePath());
    }
    
    private static boolean copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                // 文件存在时
                InputStream inStream = new FileInputStream(oldPath);
                // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                inStream = null;
                fs.close();
                fs = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static void unZipFileResDotZip(File dest) throws Exception{
        /*File dest = mLa.getApplicationContext().getFilesDir().getAbsoluteFile();*/
        String themeFolderPath = dest.getAbsolutePath()+ File.separator + THEME_FOLDER;
        File themeFolder = new File(themeFolderPath);
        if(!themeFolder.exists()){
            throw new Exception("Error when unZip res.zip, caused by file copy error");
        }
        try {
            unzip(themeFolderPath+"/res.zip" ,themeFolderPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error when unZip res.zip, caused by file unzip error");
        }
    }
    
    public static void unzip(String zipFilePath, String unzipDirectory)
            throws Exception {
        //Lenovo.sw xinggy1 20140417 modify
        //new File(zipFilePath) also throw exception just in FHD device,
        //use try & catch to avoid exception
        //File file = new File(zipFilePath);
        //ZipFile zipFile = new ZipFile(file);
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFilePath);
        } catch(Exception e){
        	ThemeLog.i(TAG, "new zip file error, try redo it");
            zipFile = new ZipFile(zipFilePath);
        }
        //End Lenovo.sw xinggy1 20140417 modify
        File unzipFile = new File(unzipDirectory + "/res");
        if (unzipFile.exists()){
            deleteFile(unzipFile);
        }
        Enumeration<?> zipEnum = zipFile.entries();
        InputStream input = null;
        OutputStream output = null;
        while (zipEnum.hasMoreElements()) {
            // 得到当前条目
            ZipEntry entry = (ZipEntry) zipEnum.nextElement();
            String entryName = new String(entry.getName().getBytes("ISO8859_1"));

            // 若当前条目为目录则创建
            if (entry.isDirectory())
                new File(unzipDirectory + "/" + entryName).mkdir();
            else { // 若当前条目为文件则解压到相应目录
                input = zipFile.getInputStream(entry);
                output = new FileOutputStream(new File(unzipDirectory + "/" + entryName));
                byte[] buffer = new byte[1024 * 8];
                int readLen = 0;
                while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1)
                    output.write(buffer, 0, readLen);
                input.close();
                output.flush();
                output.close();
            }
        }
        zipFile.close();
    }
    
    private static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                if(files == null){
                    file.delete();
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        }
    }
}