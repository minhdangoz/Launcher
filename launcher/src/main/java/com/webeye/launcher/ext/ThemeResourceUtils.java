package com.webeye.launcher.ext;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManager;

import com.android.launcher3.FolderIcon;
import com.android.launcher3.FolderIcon.FolderRingAnimator;
import com.android.launcher3.Launcher;
import com.android.launcher3.PageIndicator.PageMarkerResources;
import com.android.launcher3.backup.LbkUtil;
import com.webeye.theme.Utilities;
import com.webeye.theme.apktheme.ApkThemeUtils;
import com.webeye.theme.util.ThemeLog;
import com.webeye.launcher.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ThemeResourceUtils {
    static final String TAG = "ThemeUtils";
    private static final String KEY_THEME_PATH = "LENOVO_LAUNCHER_THEME_PATH";
    private static final String KEY_THEME_ID = "LENOVO_LAUNCHER_THEME_ID";
    private static Bitmap[] mThemeBgBitmap = new Bitmap[]{null, null, null,
            null, null, null, null};
    private static final String[] THEME_ICON_NAME = new String[]{
            "portal_ring_outer_holo", "portal_ring_rest",
            "portal_ring_inner_holo", "blackpoint", "whitepoint",
            "blackpoint.9", "whitepoint.9"};
    private static final String LENOVO_THEME_CONFIG_PATH = "etc/lenovo_theme_config.xml";// 默认主题配置文件
    private static final String PNG = ".png";
    private static final String THEME_DEF_PATH = "res/drawable-xxhdpi/";
    static final boolean DEBUG_LOADERS = true;

    private static Launcher mLauncher;
    private static String mSysDpi;
    private static String mThemePath;

    /**
     * 从Setting数据库内获取当前主题路径
     *
     * @return
     */
    private static String getThemePathFromSetting() {
        return Settings.System.getString(mLauncher.getContentResolver(), KEY_THEME_PATH);
    }

    /**
     * 获取默认主题配置文件
     *
     * @return
     */
    private final static File getLenovoThemeConfigFile() {
        File file = new File(Environment.getRootDirectory(),
                LENOVO_THEME_CONFIG_PATH);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * 解析默认主题配置文件
     */
    private static String parseLenovoThemeConfig() {
        File inFile = getLenovoThemeConfigFile();
        if (inFile == null) {
            return null;
        }

        InputStream fileInputStream = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            fileInputStream = new FileInputStream(inFile);
            parser.setInput(fileInputStream, null);

            int type;
            // 过滤packages
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
            }

            int outerDepth = parser.getDepth();
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {

                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }

                String tagName = parser.getName();
                if (tagName.equals("category")) {
                    String name = parser.getAttributeValue(null, "name");
                    if (name != null && name.equals("launcher")) {
                        String path = parser.getAttributeValue(null, "file");
                        // String defResId = parser.getAttributeValue(null,
                        // "defResId");
                        // setThemeValueToSetting(path, defResId);
                        return path;
                    } // end if (name)
                } // end if (tagName)
            } // end while
        } catch (Exception e) {
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Drawable resizeImage(Bitmap bitmap, int newWidth, int newHeight) {
        if (bitmap == null) {
            return null;
        }
        Bitmap BitmapOrg = bitmap;
        Resources r = mLauncher.getResources();
        int imageWidth = BitmapOrg.getWidth();
        int imageHeight = BitmapOrg.getHeight();

        newWidth = newWidth - newHeight;

        float scaleWidth = ((float) newWidth) / imageWidth;
        float scaleHeight = ((float) newHeight) / imageHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, imageWidth,
                imageHeight, matrix, true);
        return new BitmapDrawable(r, resizedBitmap);
    }

    private static void initDpi() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mLauncher.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_XXXHIGH:
                mSysDpi = "-xxxhdpi";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                mSysDpi = "-xxhdpi";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                mSysDpi = "-xhdpi";
                break;
            default:
                mSysDpi = "-hdpi";
                break;
        }
    }

    private static Bitmap findIconBitmapByIdName(String fileName) {
        InputStream bitmapStream;
        Bitmap newBitmap;
        String name = "res/drawable" + mSysDpi + "/" + fileName + PNG;
        try {
            bitmapStream = LbkUtil.unZip(mThemePath, name);
            if (bitmapStream == null) {
                LbkUtil.closeUnZipFile();
                name = THEME_DEF_PATH + fileName + PNG;
                bitmapStream = LbkUtil.unZip(mThemePath, name);
            }
            if (bitmapStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                newBitmap = BitmapFactory.decodeStream(bitmapStream, null, options);
                if(null != bitmapStream) {
                    try {
                        bitmapStream.close();
                        bitmapStream = null;
                        LbkUtil.closeUnZipFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return newBitmap;
            } else {
                return null;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void loadThemeResource(Launcher launcher) {
        FolderRingAnimator.sSharedOuterRingDrawable = null;
        FolderRingAnimator.sSharedInnerRingDrawable = null;
        FolderIcon.sSharedFolderLeaveBehind = null;
        PageMarkerResources.mActiveDrawable = null;
        PageMarkerResources.mInactiveDrawable = null;
        if (launcher == null) {
            return;
        }
        mLauncher = launcher;
        final long loadTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
        mThemePath = getThemePathFromSetting();
        if (mThemePath == null) {
            mThemePath = parseLenovoThemeConfig();
        }
        if (mThemePath == null) {
            return;
        }
        initDpi();
        for (int i = 0; i < THEME_ICON_NAME.length; i++) {
            String name = THEME_ICON_NAME[i];
            mThemeBgBitmap[i] = findIconBitmapByIdName(name);
            LbkUtil.closeUnZipFile();
        }
        setResource();
        if (DEBUG_LOADERS) {
            Log.d(TAG, "theme Resource in "
                    + (SystemClock.uptimeMillis() - loadTime) + "ms");
        }
    }

    private static void setResource() {
        Resources r = mLauncher.getResources();
        int newWidth = (int) r.getDimension(R.dimen.page_indicator_size);
        int newHeight = (int) r.getDimension(R.dimen.def_home_point_height);
        if(mThemeBgBitmap[0] != null){
            FolderRingAnimator.sSharedOuterRingDrawable = new BitmapDrawable(r, mThemeBgBitmap[0]);
        }
        if(mThemeBgBitmap[1] != null){
            FolderRingAnimator.sSharedInnerRingDrawable = new BitmapDrawable(r, mThemeBgBitmap[1]);
        }
        if(mThemeBgBitmap[2] != null){
            FolderIcon.sSharedFolderLeaveBehind = new BitmapDrawable(r, mThemeBgBitmap[2]);
        }
        if (mThemeBgBitmap[3] != null) {
            PageMarkerResources.mInactiveDrawable = new BitmapDrawable(r, mThemeBgBitmap[3]);
        } else {
            if(mThemeBgBitmap[5] != null){
                PageMarkerResources.mInactiveDrawable = resizeImage(mThemeBgBitmap[5], newWidth, newHeight);
            }
        }
        if (mThemeBgBitmap[4] != null) {
            PageMarkerResources.mActiveDrawable = new BitmapDrawable(r, mThemeBgBitmap[4]);
        } else {
            if(mThemeBgBitmap[6] != null){
                PageMarkerResources.mActiveDrawable = resizeImage(mThemeBgBitmap[6], newWidth, newHeight);
            }
        }
    }

    /**
     * 从APK theme 中读取Launcher 文件夹样式及page indicator 样式
     * @param launcher
     * @param apkThemeName
     */
    public static void loadThemeResource(Launcher launcher, String apkThemeName) {
        if (launcher == null) {
            return;
        }
        mLauncher = launcher;

        Context themeContext;
        try {
            themeContext = launcher.createPackageContext(apkThemeName, Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            ThemeLog.e(TAG, "theme " + apkThemeName + " not found! ", e);
            return;
        }

        ThemeLog.i(TAG, "find theme " + apkThemeName);
        int i = 0;
        for (String resName : THEME_ICON_NAME) {
            Drawable drawable = ApkThemeUtils.findDrawableByResourceName(resName, themeContext);
            if (null == drawable) {
                mThemeBgBitmap[i] = null;
            } else {
                mThemeBgBitmap[i] = Utilities.createIconBitmap(drawable, launcher);
            }
            i++;
        }
        setResource();
    }
}
