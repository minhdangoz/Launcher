package com.kapp.knews.common.utils;

import android.content.res.AssetManager;

import com.apkfuns.logutils.LogUtils;
import com.kapp.knews.helper.Utils;
import com.kapp.knews.helper.content.ContentHelper;
import com.kapp.knews.helper.view.DialogUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;


public class FileUtils {
    private static final int BUFFER_SIZE = 512;

    public static boolean isFileExist(String path) {
        return new File(path).exists();
    }

    public static String loadStringFromStream(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(new BufferedInputStream(is));
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        while (isr.read(buffer) > 0) {
            builder.append(buffer);
        }
        return builder.toString();
    }

    public static boolean safeClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
                return true;
            } catch (Exception e) {

            }
        }
        return false;
    }

    public static boolean write(CharSequence from, File to, Charset charset) {
        if (from == null || to == null || charset == null) {
            throw new NullPointerException();
        }

        FileOutputStream out = null;
        Writer writer = null;
        boolean result = true;

        try {
            out = new FileOutputStream(to);
            writer = new OutputStreamWriter(out, charset).append(from);
        } catch (IOException e) {
            return false;
        } finally {
            if (writer == null) {
                safeClose(out);
            }
            result &= safeClose(writer);
        }

        return result;
    }

    public static boolean deleteDir(File dir, boolean deleteSelf) {
        boolean success = false;
        if (dir.isDirectory()) {
            File files[] = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        success = deleteDir(f, true);
                    } else {
                        success = f.delete();
                    }
                    if (!success) {
                        break;
                    }
                }
            }
        }
        if (deleteSelf) {
            success = dir.delete();
        }
        return success;
    }

    /**
     * inputStream转String
     * @param is
     * @return
     */
    public static String inputStream2String(InputStream is) {
        if (null == is) {
            return null;
        }
        StringBuilder resultSb = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            resultSb = new StringBuilder();
            String line;
            while (null != (line = br.readLine())) {
                resultSb.append(line + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                safeClose(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null == resultSb ? null : resultSb.toString();
    }

    public static String FormatFileNameLength(String fileName, int maxLength) {

        if (fileName.length() <= maxLength)
            return fileName;

        String result = null;

        final int headLength = 3;
        final int tailLength = 3;
        final String omitStr = "...";
        int dot = fileName.lastIndexOf(".");
        final int extLength = fileName.length() - dot;
        final int minLength = headLength + tailLength + omitStr.length();

        if (maxLength < minLength)
            return fileName;

        if (dot >= 0) {
            result = fileName.substring(0, maxLength - tailLength - extLength - omitStr.length())
                    + omitStr + fileName.substring(dot - tailLength);
        } else {
            result = fileName.substring(0, maxLength);
        }

        return result;
    }


    public static final String SDCARD_KNEWS_ASSETS_NAVSITE = "/knews/assets/navsite";
//    public static final String SDCARD_KNEWS_ASSETS_LOCALSERVERNAV = "/knews/assets/"+ Navigation.NAV_ICON_LOCALSERVERNAV;


    public static File createAppCacheDir(){
        return Utils.getContext().getExternalCacheDir();
    }

    public static void createNavsiteDir() {
        StringBuilder navsiteDirPathSB = new StringBuilder(createAppCacheDir().getAbsolutePath());

        File assetsFile = new File(navsiteDirPathSB.append(SDCARD_KNEWS_ASSETS_NAVSITE).toString());
        boolean createSuccress = assetsFile.mkdirs();
        LogUtils.e("创建 ：  "+ assetsFile.getAbsolutePath()+"   是否成功："+createSuccress);
    }
//    public static void createNavsiteDir2() {
//        StringBuilder navsiteDirPathSB = new StringBuilder(createAppCacheDir().getAbsolutePath());
//
//        File assetsFile = new File(navsiteDirPathSB.append(SDCARD_KNEWS_ASSETS_LOCALSERVERNAV).toString());
//        boolean createSuccress = assetsFile.mkdirs();
//        LogUtils.e("创建 ：  "+ assetsFile.getAbsolutePath()+"   是否成功："+createSuccress);
//    }

    public static File getNavsiteDir() {
        File file = new File(createAppCacheDir().getAbsolutePath()+ SDCARD_KNEWS_ASSETS_NAVSITE);
        return file;
    }
//    public static File getNavsiteDir2() {
//        File file = new File(createAppCacheDir().getAbsolutePath()+ SDCARD_KNEWS_ASSETS_LOCALSERVERNAV);
//        return file;
//    }

    public static boolean sdCardCanUse() {
        String sdCardState = android.os.Environment.getExternalStorageState();
        if (sdCardState.equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    public static void initSdCardAssets() {
        if (!sdCardCanUse()) {
            DialogUtils.make("sdcard不可使用").show();
            LogUtils.e("sdcard不可使用");
            return;
        }
        File navsiteFile = FileUtils.getNavsiteDir();
        if (null == navsiteFile || !navsiteFile.exists() || navsiteFile.list().length == 0) {
            FileUtils.createNavsiteDir();
//            boolean copyAssets2sdCardIsSuccess = FileUtils.copyFiles(Utils.getContext(), "navsite", SDCARD_KNEWS_ASSETS_NAVSITE);
//            LogUtils.e("从assetscopy到sdcard是否成功："+copyAssets2sdCardIsSuccess);

//            FileUtils.copyFilesFassets(Utils.getContext(), "navsite", SDCARD_KNEWS_ASSETS_NAVSITE);
            copyAssetsFile2Sdcard("navsite", SDCARD_KNEWS_ASSETS_NAVSITE);

        }
    }
//    public static void initSdCardAssets2() {
//        if (!sdCardCanUse()) {
//            DialogUtils.make("sdcard不可使用").show();
//            LogUtils.e("sdcard不可使用");
//            return;
//        }
//        File navsiteFile = FileUtils.getNavsiteDir2();
//        if (null == navsiteFile || !navsiteFile.exists() || navsiteFile.list().length == 0) {
//            FileUtils.createNavsiteDir2();
//            copyAssetsFile2Sdcard2(Navigation.NAV_ICON_LOCALSERVERNAV, SDCARD_KNEWS_ASSETS_LOCALSERVERNAV);
//        }
//    }



    public static void copyAssetsFile2Sdcard(String assetsDirName, String sdcardDirPath) {


        try {
            //获取或者创建sdcard上得assets目录
            File navsiteDir = getNavsiteDir();
            if (!navsiteDir.exists()) navsiteDir.mkdirs();
            //获取所有要copy的文件名称
            AssetManager assetManager = ContentHelper.getAssets();
            String[] assetsDirInnerFiles = assetManager.list(assetsDirName);
            //执行copy
            for (String assetsFileName :
                    assetsDirInnerFiles) {
                LogUtils.e("获取到的assetsFile分别为：" + assetsFileName);

                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = assetManager.open(assetsDirName + "/" + assetsFileName);//获取输入流
                    File save2sdcardFile = new File(navsiteDir + "/" + assetsFileName);//创建保存文件
                    outputStream = new FileOutputStream(save2sdcardFile);
                    byte[] buf = new byte[512];
                    int byteRead;
                    while ((byteRead = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, byteRead);
                    }
                } finally {//从assets中copy文件到sdcard时出错：Attempt to invoke virtual method 'void java.io.OutputStream.close()' on a null object reference
                    inputStream.close();
                    outputStream.close();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("从assets中copy文件到sdcard时出错：" + e.getMessage());
        }

    }
//    public static void copyAssetsFile2Sdcard2(String assetsDirName, String sdcardDirPath) {
//
//
//        try {
//            //获取或者创建sdcard上得assets目录
//            File navsiteDir = getNavsiteDir2();
//            if (!navsiteDir.exists()) navsiteDir.mkdirs();
//            //获取所有要copy的文件名称
//            AssetManager assetManager = ContentHelper.getAssets();
//            String[] assetsDirInnerFiles = assetManager.list(assetsDirName);
//            //执行copy
//            for (String assetsFileName :
//                    assetsDirInnerFiles) {
//                LogUtils.e("获取到的assetsFile分别为：" + assetsFileName);
//
//                InputStream inputStream = null;
//                OutputStream outputStream = null;
//                try {
//                    inputStream = assetManager.open(assetsDirName + "/" + assetsFileName);//获取输入流
//                    File save2sdcardFile = new File(navsiteDir + "/" + assetsFileName);//创建保存文件
//                    outputStream = new FileOutputStream(save2sdcardFile);
//                    byte[] buf = new byte[512];
//                    int byteRead;
//                    while ((byteRead = inputStream.read(buf)) > 0) {
//                        outputStream.write(buf, 0, byteRead);
//                    }
//                } finally {//从assets中copy文件到sdcard时出错：Attempt to invoke virtual method 'void java.io.OutputStream.close()' on a null object reference
//                    inputStream.close();
//                    outputStream.close();
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            LogUtils.e("从assets中copy文件到sdcard时出错：" + e.getMessage());
//        }
//
//    }


//    public static Bitmap readAssetsBitmap(String fileName) {
//
//        try {
//            AssetManager assetManager = ContentHelper.getAssets();
//            InputStream is = assetManager.open("navsite/" + fileName);
//            return BitmapFactory.decodeStream(is);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return BitmapFactory.decodeResource(ContentHelper.getResource(), R.mipmap.ic_launcher);
//        }
//
//    }


}
