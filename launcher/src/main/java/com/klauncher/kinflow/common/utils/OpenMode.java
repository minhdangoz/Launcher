package com.klauncher.kinflow.common.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.klauncher.kinflow.browser.KinflowBrower;

import java.util.List;

/**
 * Created by xixionghui on 16/4/6.
 */
public class OpenMode {
    private Context mContext;

    public static final int ID_INTERNAL_BROWSER = 0;//内置浏览器
    public static final int ID_JIN_RI_TOU_TIAO = 2;//对应打开方式:Uri
    public static final int ID_QQ_BROWSER = 23;//对应打开方式:component
    public static final int ID_BAIDU_BROWSER = 9;//对应打开方式:component
    public static final int ID_UC_BROWSER = 19; // UC浏览器
    public static final int ID_SYSTEM_SETTING = 37; // 系统设置
    public static final int ID_SOUGOU_SOUSUO = 30; // 搜狗搜索
    public static final int ID_MOJI_TIANQI = 4; // 搜狗搜索
    public static final int ID_TAOBAO = 6; // 淘宝
    public static final int ID_TENGXUN_SHIPIN = 14; // 腾讯视频
    public static final int ID_TENGXUN_XINWEN = 32; // 腾讯新闻---手机腾讯网
    public static final int ID_BAIDU_SOUSUO = 10; // 百度搜索----手机百度

    public static final String COMPONENT_NAME_QQ_BROWSER = "com.tencent.mtt/com.tencent.mtt.SplashActivity";
    public static final String COMPONENT_NAME_UC_BROWSER = "com.UCMobile/com.UCMobile.main.UCMobile";
    public static final String COMPONENT_NAME_BAIDU_BROWSER_1 = "com.baidu.browser.apps_sj/com.baidu.browser.framework.BdBrowserActivity";
    public static final String COMPONENT_NAME_BAIDU_BROWSER_2 = "com.baidu.browser.apps/com.baidu.browser.framework.BdBrowserActivity";
    public static final String COMPONENT_NAME_JINRI_TOUTIAO = "com.ss.android.article.news/com.ss.android.article.news.activity.SplashActivity";
    public static final String COMPONENT_NAME_MOJI_TIANQI = "com.moji.mjweather/com.moji.mjweather.CSplashScreen";
    public static final String COMPONENT_NAME_SOUGOU_SOUSUO = "com.sogou.activity.src/com.sogou.activity.src.SplashActivity";
    public static final String COMPONENT_NAME_TAOBAO = "com.taobao.com/com.taobao.tao.welcome.Welcome";
    public static final String COMPONENT_NAME_TENGXUN_SHIPIN = "com.tencent.qqlive/com.tencent.qqlive.ona.activity.WelcomeActivity";
    public static final String COMPONENT_NAME_TENGXUN_XINWEN = "com.tencent.news/com.tencent.news.activity.SplashActivity";
    public static final String COMPONENT_NAME_BAIDU_SOUSUO = "com.baidu.searchbox/com.baidu.searchbox.MainActivity";



    private Intent firstIntent = new Intent();//已知应用
    private Intent secondIntent = new Intent();//指定应用
    private Intent thirdIntent = new Intent();//内嵌浏览器

    //已知应用全部按照id返回,但是对应的打开方式任意一种都存在,下面是打开方式

    public static final String OPEN_URL_KEY = "open_url_key";//第二打开方式,第三打开方式都会用到url

    public static final String FIRST_OPEN_MODE_TYPE_DEFAULT = "firt_open_mode_type_default";
    public static final String FIRST_OPEN_MODE_TYPE_URI = "first_open_mode_type_uri";
    public static final String FIRST_OPEN_MODE_TYPE_COMPONENT = "first_open_mode_type_component";
    public static final String FIRST_OPEN_MODE_TYPE_KEY = "open_mode_type_key";
    public static final String FIRST_OPEN_MODE_TYPE_ACTION = "first_open_mode_type_action";

    private String currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_DEFAULT;
    private String mOpenUrl;

    public Intent setFirstIntent(int id) {//ID决定第一打开方式
        firstIntent.setAction(Intent.ACTION_VIEW);
        switch (id) {
            case ID_INTERNAL_BROWSER:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_DEFAULT;
                firstIntent.setClass(mContext, KinflowBrower.class);
                break;
            case ID_JIN_RI_TOU_TIAO:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_URI;//设置当前打开方式
                break;
            case ID_QQ_BROWSER:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_COMPONENT;//设置当前打开方式
                setComponent(COMPONENT_NAME_QQ_BROWSER);
                break;
            case ID_UC_BROWSER:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_COMPONENT;//设置第一打开方式为component
                setComponent(COMPONENT_NAME_UC_BROWSER);
                break;
            case ID_SOUGOU_SOUSUO:
                setComponent(COMPONENT_NAME_SOUGOU_SOUSUO);
                break;
            case ID_MOJI_TIANQI:
                setComponent(COMPONENT_NAME_MOJI_TIANQI);
                break;
            case ID_TAOBAO:
                setComponent(COMPONENT_NAME_TAOBAO);
                break;
            case ID_TENGXUN_SHIPIN:
                setComponent(COMPONENT_NAME_TENGXUN_SHIPIN);
                break;
            case ID_TENGXUN_XINWEN:
                setComponent(COMPONENT_NAME_TENGXUN_XINWEN);
                break;
            case ID_BAIDU_SOUSUO:
                setComponent(COMPONENT_NAME_BAIDU_SOUSUO);
                break;

            case ID_SYSTEM_SETTING:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_ACTION;//设置当前打开方式
                if (android.os.Build.VERSION.SDK_INT > 10) {
                    firstIntent.setAction(android.provider.Settings.ACTION_SETTINGS);
                } else {
                    firstIntent.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                }
                break;
            case ID_BAIDU_BROWSER:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_COMPONENT;//设置当前打开方式
                setComponentBaidu();
                break;
            default:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_ACTION;//设置当前打开方式
                firstIntent.setAction(Intent.ACTION_VIEW);
                firstIntent.setClass(mContext, KinflowBrower.class);
                firstIntent.putExtra(KinflowBrower.KEY_EXTRA_URL, this.mOpenUrl);
                firstIntent.setData(Uri.parse(this.mOpenUrl));

        }
        return firstIntent;
    }

    private void setComponent(String component){
        String[] cn = component.split("/");
        ComponentName componentName = new ComponentName(cn[0], cn[1]);
        firstIntent.setComponent(componentName);
    }

    private void setComponentBaidu(){
        if (CommonUtils.getInstance().isInstalledAPK(mContext,COMPONENT_NAME_BAIDU_BROWSER_1)) {//用户安装BAIDU_BROWSER_1浏览器
            String[] cns1= COMPONENT_NAME_BAIDU_BROWSER_1.split("/");
            ComponentName componentName = new ComponentName(cns1[0], cns1[1]);
            firstIntent.setComponent(componentName);
        }else {//用户安装BAIDU_BROWSER_2浏览器||都没安装
            String[] cns2= COMPONENT_NAME_BAIDU_BROWSER_2.split("/");
            ComponentName componentName = new ComponentName(cns2[0], cns2[1]);
            firstIntent.setComponent(componentName);
        }
    }

    public Intent setFirstIntent(String packNameAndClassName, String openUrl) {
        firstIntent.setAction(Intent.ACTION_VIEW);
        if (TextUtils.isEmpty(packNameAndClassName)) return firstIntent;
        String[] cn = packNameAndClassName.split("/");
        if (cn.length == 2) {
            ComponentName componentName = new ComponentName(cn[0], cn[1]);
            firstIntent.setComponent(componentName);
        }
        if (openUrl != null) {
            firstIntent.setData(Uri.parse(openUrl));
        }
        return firstIntent;
    }

    public Intent setSecondIntent(String packNameAndClassName, String openUrl) {
        secondIntent.setAction(Intent.ACTION_VIEW);
        if (TextUtils.isEmpty(packNameAndClassName)) return secondIntent;
        String[] cn = packNameAndClassName.split("/");
        if (cn.length == 2) {
            ComponentName componentName = new ComponentName(cn[0], cn[1]);
            secondIntent.setComponent(componentName);
        }
        if (openUrl != null) {
            secondIntent.setData(Uri.parse(openUrl));
        }
        return secondIntent;
    }

    public Intent getFirstIntent() {
        return firstIntent;
    }

    public Intent getSecondIntent() {
        return secondIntent;
    }

    public Intent getThirdIntent() {
        return thirdIntent;
    }

    public String getCurrentFirstOpenMode() {
        return currentFirstOpenMode;
    }

    public OpenMode(Context context, List<String> cardOpenOptionList, String openUrl) {
        this.mContext = context;
        this.mOpenUrl = openUrl;
        int size = cardOpenOptionList.size();
        for (int i = 0; i < size; i++) {
            switch (i) {
                case 0:
                    if (CommonUtils.isInteger(cardOpenOptionList.get(i)))
                    setFirstIntent(Integer.valueOf(cardOpenOptionList.get(i)));
                    else {
                        //
                        firstIntent.setAction(Intent.ACTION_VIEW);
                        firstIntent.setClass(context, KinflowBrower.class);
                        firstIntent.putExtra(KinflowBrower.KEY_EXTRA_URL, openUrl);
                        firstIntent.setData(Uri.parse(openUrl));
                    }
//                    setFirstIntent(cardOpenOptionList.get(i), openUrl);//信息流第二版使用
                    break;
                case 1:
                    setSecondIntent(cardOpenOptionList.get(i), openUrl);
                    break;
                case 2:
                    if (openUrl != null) {
                        thirdIntent.setAction(Intent.ACTION_VIEW);
                        thirdIntent.setClass(context, KinflowBrower.class);
                        thirdIntent.putExtra(KinflowBrower.KEY_EXTRA_URL, openUrl);
                        thirdIntent.setData(Uri.parse(openUrl));
                    }
                    break;

            }
        }
    }

}
