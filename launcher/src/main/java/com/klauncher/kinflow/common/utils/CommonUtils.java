package com.klauncher.kinflow.common.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.klauncher.kinflow.browser.KinflowBrower;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by xixionghui on 2016/3/22.
 */
public class CommonUtils {

    public static CommonUtils instance  = new CommonUtils();

    private CommonUtils() {
    }

    public static CommonUtils getInstance() {
//        if (null == instance) instance = new CommonUtils();
        return instance;
    }

    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //获取一个圆形的Bitmap
    public Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    /**
     * 按照指定浏览器打开，如果没有指定浏览器则使用自带浏览器
     *
     * @param context
     * @param url
     * @param packageName
     * @param mainActivity
     * @throws ActivityNotFoundException
     */
    public void openBrowserUrl(Context context, String url, String packageName, String mainActivity) {
        if (packageName.equals("com.klauncher.kinflow")) {
            openDefaultBrowserUrl(context, url);
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        intent.setClassName(packageName, mainActivity);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent defaultBrower = new Intent(context, KinflowBrower.class);
            defaultBrower.putExtra(KinflowBrower.KEY_EXTRA_URL, url);
            context.startActivity(defaultBrower);
        }
    }

    /**
     * 使用内嵌默认浏览器打开
     *
     * @param context
     * @param url
     */
    public void openDefaultBrowserUrl(Context context, String url) {
        Intent defaultBrower = new Intent(context, KinflowBrower.class);
        defaultBrower.putExtra(KinflowBrower.KEY_EXTRA_URL, url);
        context.startActivity(defaultBrower);
    }


    /**
     * 将图片转换成Base64编码
     *
     * @param imgFile 待处理图片
     * @return
     */
    public static String getImgStr(String imgFile) {
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.encode(data, Base64.DEFAULT));
    }

    public static String getImagStr(InputStream in) {
        byte[] data = null;
        try {
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.encode(data, Base64.DEFAULT));
    }


    public List<ComponentName> allInstallAPK(Context context) {
        List<ComponentName> componentNameList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = packageManager.queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS);
        for (ResolveInfo resolveInfo : infos) {
            componentNameList.add(new ComponentName(
                    resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
        }
        return componentNameList;
    }

    public boolean isInstalledAPK(Context context, String packageName, String mainActivity) {
        ComponentName componentName = new ComponentName(packageName, mainActivity);
        if (allInstallAPK(context).contains(componentName)) return true;
        return false;
    }

    public boolean isInstalledAPK(Context context, ComponentName componentName) {
        if (allInstallAPK(context).contains(componentName)) return true;
        return false;
    }

    public boolean isInstalledAPK(Context context, String component) {
        String[] cns = component.split("/");
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(cns[0], PackageManager.GET_META_DATA);
            if (null != info)
                return true;
            else return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void openApp(Context context,String component){
        String[] cns = component.split("/");
        ComponentName componentName = new ComponentName(cns[0],cns[1]);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param content PackageName 和 MainActivity组合，例如：com.android.chrome/com.google.android.apps.chrome.Main
     * @param url     打开指定内容的url
     * @return
     */
    public boolean openDetalWithSpecialMode(Context context, String content, String url) {
        boolean isSuccess = false;
        String[] contents = null;

        try {
            contents = content.split("/");
            Log.d("CommonUtils", "try split context");
        } catch (Exception e) {//没有按照指定格式返回数据：....PackageName...../.....MainActivity......
            Log.d("CommonUtils", "没有按照指定格式返回数据：....PackageName...../.....MainActivity......");
            return false;
        }
        if (contents.length != 2) return false;
        Log.d("CommonUtils", "开始构建ComponentName");
        ComponentName componentName = new ComponentName(contents[0], contents[1]);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);

        try {
            context.startActivity(intent);
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            return isSuccess;
        }
    }

    /**
     * @param context
     * @param contents app的包名、类名
     * @param url      打开指定内容的url
     */
    public void openDetail(Context context, List<String> contents, String url) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            stringBuilder.append(contents.get(i)).append("  ,  ");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        Log.i("CommonUtils", "要打开的方式: " + stringBuilder.toString());

        if (null == contents || contents.size() == 0) {
            Toast.makeText(context, "没有指定打开方式,使用默认打开方式", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, KinflowBrower.class).putExtra(KinflowBrower.KEY_EXTRA_URL, url));
            return;
        }
        boolean isOpenSuccess = false;
        for (int i = 0; i < contents.size(); i++) {
            if (openDetalWithSpecialMode(context, contents.get(i), url)) {//如果打开成功
                isOpenSuccess = true;
                return;
            }
        }
        if (!isOpenSuccess) {//如果所有指定的方式都打开失败，则启动内嵌浏览器打开
            Toast.makeText(context, "没有指定打开方式,使用默认打开方式", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, KinflowBrower.class).putExtra(KinflowBrower.KEY_EXTRA_URL, url));
        }
    }

    public void openHotWord(Context context, String url) {
        try {
            //uc
            openBrowerUrl(context, url, Const.UC_packageName, Const.UC_mainActivity);
        } catch (Exception e) {
            try {
                openBrowerUrl(context, url, Const.QQ_packageName, Const.QQ_mainActivity);
            } catch (Exception e1) {
                openDefaultBrowserUrl(context, url);
            }
        }
    }

    void openBrowerUrl(Context context, String url, String packageName, String className) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri data = Uri.parse(url);
        intent.setData(data);
        intent.setClassName(packageName, className);
        context.startActivity(intent);
    }

    public String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    //secretkey ＝ sha1(md5(app_key) + nonce + timestamp)
    public static String getSecretkey(String appkey,String nonce,String timestamp){
        String md5AppKey = getMD5(appkey);
        return SHA1(md5AppKey+nonce+timestamp);
    }


    public static String SHA1(String decript) {
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    //-----
    public static String getMD5(String input) {
        if (TextUtils.isEmpty(input)) {
            return null;
        }
        try {
            return getDigest(input.getBytes("UTF-8"), "MD5");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String getDigest(byte[] bytes, String algorithm) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.reset();
            messageDigest.update(bytes);
        } catch (Exception e) {
            return null;
        }

        byte[] byteArray = messageDigest.digest();
        StringBuilder md5StrBuff = new StringBuilder(byteArray.length * 2);
        for (byte b : byteArray) {
            md5StrBuff.append(Integer.toHexString((0xFF & b) >> 4));
            md5StrBuff.append(Integer.toHexString(0x0F & b));
        }
        return md5StrBuff.toString();
    }
}
