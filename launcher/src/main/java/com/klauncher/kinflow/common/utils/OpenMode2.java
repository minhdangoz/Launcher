package com.klauncher.kinflow.common.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.klauncher.kinflow.browser.KinflowBrower;
import com.klauncher.kinflow.cards.model.sougou.SougouSearchArticle;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.views.recyclerView.data.BaseRecyclerViewAdapterData;

import java.util.List;

/**
 * Created by xixionghui on 16/4/6.
 */
public class OpenMode2 {
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

    private static final String INNER_BROWSER_PACKAGENAME = "com.klauncher.launcher/com.klauncher.kinflow.browser.KinflowBrower";
    private static final String CLIENT_SCHEMA = "xxx/www";

    public Intent setFirstIntent(String packNameAndClassName, String openUrl) {
        firstIntent.setAction(Intent.ACTION_VIEW);
        Uri uri = null;
        if (TextUtils.isEmpty(packNameAndClassName)) return firstIntent;
        if (packNameAndClassName.equals(INNER_BROWSER_PACKAGENAME)) {//自身的kinflowBrowser
            //设置Component
            firstIntent.setClass(mContext,KinflowBrower.class);
            firstIntent.putExtra(KinflowBrower.KEY_EXTRA_URL,openUrl);
            //设置uri
            uri = Uri.parse(openUrl);
        }else if (packNameAndClassName.equals(CLIENT_SCHEMA)) {//对应新闻客户端
            //设置Component
            if (mBaseData.getType() == BaseRecyclerViewAdapterData.TYPE_NEWS_JINRITOUTIAO) {
                packNameAndClassName = COMPONENT_NAME_JINRI_TOUTIAO;
                JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle)mBaseData;
                uri = Uri.parse(Const.URI_TOUTIAO_ARTICLE_DETAIL+jinRiTouTiaoArticle.getGroup_id());
            } else if (mBaseData.getType() == BaseRecyclerViewAdapterData.TYPE_NEWS_SOUGOU) {
                packNameAndClassName = COMPONENT_NAME_SOUGOU_SOUSUO;
                SougouSearchArticle sougouSearchArticle = (SougouSearchArticle) mBaseData;
                uri = Uri.parse(sougouSearchArticle.getSchema());
            }
            String[] cn = packNameAndClassName.split("/");
            if (cn.length == 2) {
                ComponentName componentName = new ComponentName(cn[0], cn[1]);
                firstIntent.setComponent(componentName);
            }
        } else {//包名类名
            //设置Component
            String[] cn = packNameAndClassName.split("/");
            if (cn.length == 2) {
                ComponentName componentName = new ComponentName(cn[0], cn[1]);
                firstIntent.setComponent(componentName);
            }
            //设置uri
            uri = Uri.parse(openUrl);
        }
        if (openUrl != null) {
            firstIntent.setData(uri);
        }
        return firstIntent;
    }

    /*
    thirdIntent.setAction(Intent.ACTION_VIEW);
                        thirdIntent.setClass(context, KinflowBrower.class);
                        thirdIntent.putExtra(KinflowBrower.KEY_EXTRA_URL, openUrl);
                        thirdIntent.setData(Uri.parse(openUrl));
     */
    public Intent setSecondIntent(String packNameAndClassName, String openUrl) {
        secondIntent.setAction(Intent.ACTION_VIEW);
        Uri uri = null;
        if (TextUtils.isEmpty(packNameAndClassName)) return secondIntent;
        if (packNameAndClassName.equals(INNER_BROWSER_PACKAGENAME)) {//自身的kinflowBrowser
            //设置Component
            secondIntent.setClass(mContext,KinflowBrower.class);
            secondIntent.putExtra(KinflowBrower.KEY_EXTRA_URL, openUrl);
            //设置uri
            uri = Uri.parse(openUrl);
        }else if (packNameAndClassName.equals(CLIENT_SCHEMA)) {//对应新闻客户端
            //设置Component
            if (mBaseData.getType() == BaseRecyclerViewAdapterData.TYPE_NEWS_JINRITOUTIAO) {
                packNameAndClassName = COMPONENT_NAME_JINRI_TOUTIAO;
                JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle)mBaseData;
                uri = Uri.parse(Const.URI_TOUTIAO_ARTICLE_DETAIL+jinRiTouTiaoArticle.getGroup_id());
            } else if (mBaseData.getType() == BaseRecyclerViewAdapterData.TYPE_NEWS_SOUGOU) {
                packNameAndClassName = COMPONENT_NAME_SOUGOU_SOUSUO;
                SougouSearchArticle sougouSearchArticle = (SougouSearchArticle) mBaseData;
                uri = Uri.parse(sougouSearchArticle.getSchema());
            }
            String[] cn = packNameAndClassName.split("/");
            if (cn.length == 2) {
                ComponentName componentName = new ComponentName(cn[0], cn[1]);
                secondIntent.setComponent(componentName);
            }
        } else {//包名类名
            //设置Component
            String[] cn = packNameAndClassName.split("/");
            if (cn.length == 2) {
                ComponentName componentName = new ComponentName(cn[0], cn[1]);
                secondIntent.setComponent(componentName);
            }
            //设置uri
            uri = Uri.parse(openUrl);
        }
        if (openUrl != null) {
            secondIntent.setData(uri);
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

    private BaseRecyclerViewAdapterData mBaseData;
    public OpenMode2(Context context, List<String> cardOpenOptionList, String openUrl, BaseRecyclerViewAdapterData baseData) {
        this.mContext = context;
        this.mOpenUrl = openUrl;
        this.mBaseData = baseData;
        int size = cardOpenOptionList.size();
        for (int i = 0; i < size; i++) {
            switch (i) {
                case 0:
//                    if (CommonUtils.isInteger(cardOpenOptionList.get(i)))
//                    setFirstIntent(Integer.valueOf(cardOpenOptionList.get(i)));
//                    else {
//                        //
//                        firstIntent.setAction(Intent.ACTION_VIEW);
//                        firstIntent.setClass(context, KinflowBrower.class);
//                        firstIntent.putExtra(KinflowBrower.KEY_EXTRA_URL, openUrl);
//                        firstIntent.setData(Uri.parse(openUrl));
//                    }
                    setFirstIntent(cardOpenOptionList.get(i), openUrl);//信息流第二版使用
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
