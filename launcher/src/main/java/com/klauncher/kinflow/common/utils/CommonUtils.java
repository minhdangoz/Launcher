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
import com.klauncher.launcher.BuildConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
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
        synchronized (this) {
            Intent defaultBrower = new Intent(context, KinflowBrower.class);
            defaultBrower.putExtra(KinflowBrower.KEY_EXTRA_URL, url);
            context.startActivity(defaultBrower);
        }
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
        synchronized (this) {
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
    }

    public boolean isInstalledAPK(Context context, String packageName, String mainActivity) {
        synchronized (this) {
            ComponentName componentName = new ComponentName(packageName, mainActivity);
            if (allInstallAPK(context).contains(componentName)) return true;
            return false;
        }
    }

    public boolean isInstalledAPK(Context context, ComponentName componentName) {
        synchronized (this) {
            if (allInstallAPK(context).contains(componentName)) return true;
            return false;
        }
    }

    public boolean isInstalledAPK(Context context, String component) {
        synchronized (this) {
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
    }

    public void openApp(Context context,String component){
        synchronized (this) {
            String[] cns = component.split("/");
            ComponentName componentName = new ComponentName(cns[0], cns[1]);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param content PackageName 和 MainActivity组合，例如：com.android.chrome/com.google.android.apps.chrome.Main
     * @param url     打开指定内容的url
     * @return
     */
    public boolean openDetalWithSpecialMode(Context context, String content, String url) {
        synchronized (this) {
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
    }

    /**
     * @param context
     * @param contents app的包名、类名
     * @param url      打开指定内容的url
     */
    public void openDetail(Context context, List<String> contents, String url) {
        synchronized (this) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < contents.size(); i++) {
                stringBuilder.append(contents.get(i)).append("  ,  ");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

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
    }

    public String openHotWord(Context context, String url) {
        synchronized (this) {
        String pullUpPackageName ="com.klauncher.launcher";//定义拉起APP的包名
            try {
                //uc
                openBrowerUrl(context, url, Const.UC_packageName, Const.UC_mainActivity);
                pullUpPackageName = Const.UC_packageName;
            } catch (Exception e) {
                try {
                    openBrowerUrl(context, url, Const.QQ_packageName, Const.QQ_mainActivity);
                    pullUpPackageName = Const.QQ_packageName;
                } catch (Exception e1) {
                    openDefaultBrowserUrl(context, url);
//                    pullUpPackageName = "com.klauncher.launcher";//使用这个,在正式app上无法获取src32,正式app的包名
                    pullUpPackageName = BuildConfig.APPLICATION_ID;
                }
            } finally {
                return pullUpPackageName;
            }
        }
    }

    void openBrowerUrl(Context context, String url, String packageName, String className) {
        synchronized (this) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri data = Uri.parse(url);
            intent.setData(data);
            intent.setClassName(packageName, className);
            context.startActivity(intent);
        }
    }

    public String getRandomString(int length) { //length表示生成字符串的长度
        synchronized (this) {
            String base = "abcdefghijklmnopqrstuvwxyz0123456789";
            Random random = new Random();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < length; i++) {
                int number = random.nextInt(base.length());
                sb.append(base.charAt(number));
            }
            return sb.toString();
        }
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

    /**
     * 字典排序
     * @param strArray
     * @return
     */
    public static String orderLexicographical(String[] strArray) {
        String t = null;
//        System.out.println("排序前");
//        for (String s : strArray)
//            System.out.print(s + "\t");
        int i, j, k;
        for (i = 0; i < strArray.length - 1; i++) {
            k = i;
            for (j = i + 1; j < strArray.length; j++) {
                Character c1 = Character.valueOf(strArray[j].charAt(0));
                Character c2 = Character.valueOf(strArray[k].charAt(0));
                if (c1.compareTo(c2) < 0)
                    k = j;
            }
            if (i != k) {
                t = strArray[i];
                strArray[i] = strArray[k];
                strArray[k] = t;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String str :
                strArray) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    /**
     * 信息流界面是否允许跳转
     * @return
     */
    public boolean allowActive2345() {
        if (!CommonShareData.getBoolean(CommonShareData.KEY_ACTIVE_2345, false)) {//如果后台配置不打开2345跳转,则返回false
//            Log.e("Kinflow", "后台配置不允许跳转,返回false");
            return false;
        }
        if (!canOperateNow()) {
//            Log.e("Kinflow", "首次激活一定时间内不可以运营,返回false");
            return false;
        }
        if (isSkipedInterval()) {
//            Log.e("Kinflow", "24h以内已经跳转过,所以不在跳转,返回false");
            return false;
        }
        else {//超过24小时还没有自动跳转过
            if (random()) {//随机为跳转
//                Log.e("Kinflow", "24h以内没有跳转过,随机:跳转,返回true");
                CommonShareData.putLong(Const.KEY_LAST_SKIP_TIME, Calendar.getInstance().getTimeInMillis());
//                Log.i("Kinflow", "24h以内没有跳转过,随机:跳转");
                return  true;
            } else {//随机为不跳转
//                Log.e("Kinflow", "24h以内没有跳转过,随机:不跳转,返回false");
//                Log.i("Kinflow","24h以内没有跳转过,随机:不跳转");
                return false;
            }
        }
    }

    /**
     * 首次激活一定时间内不可以运营，默认${CommonShareData.DEFAULT_OPERATOR_DELAY}小时
     * @return
     */
    public boolean canOperateNow() {
        //1.获取config的最后更新时间
        long configFirstUpdateMillis = CommonShareData.getLong(CommonShareData.KEY_CONFIG_FIRST_UPDATE, -1);//config最新更新时间,精确到毫秒
        if (configFirstUpdateMillis == -1) {//如果尚未更新config,则将最后更新config时间修改为当前时间.
            configFirstUpdateMillis = System.currentTimeMillis();
            CommonShareData.putLong(CommonShareData.KEY_CONFIG_FIRST_UPDATE, System.currentTimeMillis());
        }
        //2.通过config的最后更新时间计算开启运营跳转网页的时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(configFirstUpdateMillis);
        calendar.add(Calendar.HOUR_OF_DAY, CommonShareData.getInt(
                CommonShareData.KEY_OPERATOR_DELAY, CommonShareData.DEFAULT_OPERATOR_DELAY));
        //3.如果当前时间已经超过运营跳转网页的时间,则返回true.否则返回false
        if (Calendar.getInstance().after(calendar)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ${CommonShareData.DEFAULT_ACTIVE_INTERVAL_2345}小时以内是否跳转过
     * @return
     */
    private boolean isSkipedInterval() {
        long lastSkipTime = CommonShareData.getLong(Const.KEY_LAST_SKIP_TIME, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastSkipTime);
        calendar.add(Calendar.HOUR_OF_DAY, CommonShareData.getInt(
                CommonShareData.KEY_ACTIVE_INTERVAL_2345, CommonShareData.DEFAULT_ACTIVE_INTERVAL_2345));
        if (calendar.after(Calendar.getInstance())) {//DEFAULT_ACTIVE_INTERVAL_2345 以内,已经跳转过
            return true;
        } else {//DEFAULT_ACTIVE_INTERVAL_2345 以内没有跳转过
            return false;
        }
    }

    private boolean random () {
        int i = (int) (1 + Math.random() * (2 - 1 + 1));
        switch (i) {
            case 1:
                return true;
            case 2:
                return false;
            default:
                return false;
        }
    }


    public String nextSkipUrl() {
        String urlAddress = Const.URL_2345_HOMEPAGE;
        try {
        String urlList = CommonShareData.getString(CommonShareData.KEY_WEBPAGE_SKIP_URL_LIST,Const.URL_2345_HOMEPAGE);
//            String urlList = "http://m.2345.com/?sc_delong,http://m.baidu.com/s?from=1010445i,https://sina.cn/,https://m.hao123.com/";
            String[] urls = urlList.split(",");
            if (urlList.length()==0) return urlAddress;
            int cursor = CommonShareData.getInt(CommonShareData.KEY__URL_LIST_CURSOR,0);
            urlAddress = urls[cursor];
            //修改cursor
            if (cursor==urls.length) {
                cursor = 0;
            }else {
                cursor++;
            }
            CommonShareData.putInt(CommonShareData.KEY__URL_LIST_CURSOR,cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlAddress;
    }

    //====
    //=============

    public boolean allowUrlSkip() {
        try {
            if (!CommonShareData.getBoolean(CommonShareData.KEY_ACTIVE_2345, false)) {//如果后台配置不打开2345跳转,则返回false
    //            Log.e("Kinflow", "后台配置不允许跳转,返回false");
                return false;
            }
            if (!canOperateNow()) {
    //            Log.e("Kinflow", "首次激活一定时间内不可以运营,返回false");
                return false;
            }

        /*
        //总时间限制是12小时，，在12小时内所有的都跳转一个遍:规定时间段内，如果运营次数已经达到列表长度，则不再运营。
        if (inOperationDuration()) {//运营时间段内
            //获取最大允许运营次数
            String urlList = CommonShareData.getString(CommonShareData.KEY_WEBPAGE_SKIP_URL_LIST,Const.URL_2345_HOMEPAGE);
            String[] urls = urlList.split(",");
            //获取已经运营的次数
            int skipTimes = CommonShareData.getInt(CommonShareData.KEY_ACTIVE_2345_SKIP_TIMES,0);
            //如果已经达到最大运营次数,则不允许运营.否则可以运营
            if (skipTimes>=urls.length) {
                return false;
            }else {
//                return true;
                return random();
            }
        }else {//进入下一个运营阶段
            CommonShareData.putInt(CommonShareData.KEY_ACTIVE_2345_SKIP_TIMES,0);
//            return true;
            return random();
        }
        */

            //所有的跳转间隔是12小时。没有总时间限制：如果超过了规定时间段，则依次循环运营。
            if (isOverInterval()) {//是否超过了后台控制设置的时间间隔
              return true;
            }else {//没有超过后台控制设置的时间间隔
                return false;
            }
        } catch (Exception e) {
           return false;
        }

    }

    public String currentSkipUrl() {
        try {
            //声明跳转网页
            String skipUrl  = Const.URL_2345_HOMEPAGE;
            //获取url跳转列表
            String urlList = CommonShareData.getString(CommonShareData.KEY_WEBPAGE_SKIP_URL_LIST,Const.URL_2345_HOMEPAGE);
            String[] urls = urlList.split(",");
            //获取当前cursor,默认为0
            int cursor = CommonShareData.getInt(CommonShareData.KEY__URL_LIST_CURSOR,0);
//        if (cursor>=urls.length) cursor = 0;
            //计算本次跳转的url
            skipUrl = urls[cursor];
            //计算下一次跳转的cursor
            cursor++;
            //判断指针是否>=长度,当==长度时必须置为0
            if (cursor>=urls.length) cursor = 0;
            //存储下次跳转的cursor:PutInt
            CommonShareData.putInt(CommonShareData.KEY__URL_LIST_CURSOR,cursor);
            return skipUrl;
        } catch (Exception e) {
            return Const.URL_2345_HOMEPAGE;
        }

    }

    public void saveOnceSkip() {
        //当次运营是第几次跳转
        int currentSkipTimes = CommonShareData.getInt(CommonShareData.KEY_ACTIVE_2345_SKIP_TIMES,0)+1;
        //如果是新一轮的跳转则保存时间
        if (currentSkipTimes==1)
            CommonShareData.putLong(Const.KEY_LAST_SKIP_TIME,Calendar.getInstance().getTimeInMillis());
        //保存一次跳转运营次数
        CommonShareData.putInt(CommonShareData.KEY_ACTIVE_2345_SKIP_TIMES,currentSkipTimes);
    }

    public void saveSkipTime(){
        try {
            CommonShareData.putLong(Const.KEY_LAST_SKIP_TIME,Calendar.getInstance().getTimeInMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 在运营时长内
     */
    private boolean inOperationDuration() {
        //获取上一次运营时间
        long lastSkipTime = CommonShareData.getLong(Const.KEY_LAST_SKIP_TIME, 0);
        //计算下次运营时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastSkipTime);
        calendar.add(Calendar.HOUR_OF_DAY, CommonShareData.getInt(
                CommonShareData.KEY_ACTIVE_INTERVAL_2345, CommonShareData.DEFAULT_ACTIVE_INTERVAL_2345));
        //如果当前时间在下次运营时间之前则证明在运营时间段内.否则证明在运营时间段之外
        if (Calendar.getInstance().before(calendar)) {
            return true;
        }else {
            return false;
        }
    }

    /**
     * 是否超过了后台控制配置的时间间隔interval
     */
    private boolean isOverInterval() {
        //获取上一次运营时间
        long lastSkipTime = CommonShareData.getLong(Const.KEY_LAST_SKIP_TIME, 0);
        //计算下次运营时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastSkipTime);
        calendar.add(Calendar.HOUR_OF_DAY, CommonShareData.getInt(
                CommonShareData.KEY_ACTIVE_INTERVAL_2345, CommonShareData.DEFAULT_ACTIVE_INTERVAL_2345));
        //如果当前时间在下次运营时间之后则证明超过了时间间隔.
        if (Calendar.getInstance().after(calendar)) {
            return true;
        }else {
            return false;
        }
    }
}
