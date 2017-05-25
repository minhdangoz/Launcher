package com.kapp.knews.repository.utils;


import com.kapp.knews.BuildConfig;

/**
 * Created by xixionghui on 2016/11/24.
 */

public class Const {

    public static String DONGFANG_RANDOM_CHARS = "http://kdhcodeapi.dftoutiao.com/newskey_kdh/code";
    public static String DONGFANG_ARTICLE_URL = "http://kdhnewsapi.dftoutiao.com/newsapi_kdh/newspool";


    public static final String REALEASE_HOST = "http://api.klauncher.com";
    public static final String REALEASE_CID = BuildConfig.CHANNEL_ID;
    public static final String KINFLOW2_SERVER_CONTROL = REALEASE_HOST+"/v1/news/mobile?cid="+REALEASE_CID;

    public static final String NAVIGATION_WEB = REALEASE_HOST+"/v1/webnav/mobile?cid="+REALEASE_CID;
    public static final String NAVIGATION_WEB2 = REALEASE_HOST+"/v2/webnav/mobile?cid="+REALEASE_CID;
    public static final String AD_KLAUNCHER =REALEASE_HOST+"/v1/ad/mobile?cid="+REALEASE_CID;

    /**
     * kcontrol控制新闻广告间隔
     */
    public static final String AD_KCONTROL_INTERVAL="http://api.kxcontrol.com:666/v1/config/funclist?cid="+BuildConfig.KCONTROL_KNEWS_ID+"&param=interval";

    /**
     * 百度
     */
    public static final String BAIDU_APIKEY = "2f08b705803233298c235eedb74b84ab";//百度apiKey:应该没用了
    public static final String BAIDU_TRADE_ID_HOTWORD = "1019327a";//百度ID
    public static final String HOTWORD_TYPE_SHISHI = "shishi";
    public static final String HOTWORD_TYPE_DIANYING = "dianying";
    public static final String HOTWORD_TYPE_ZONGYI = "zongyi";
    public static final String HOTWORD_TYPE_DIANSHIJU = "dianshiju";
    public static final String HOTWORD_TYPE_DONGMAN = "dongman";
    public static final String HOTWORD_TYPE_TITAN = "titan";
    public static final String HOTWORD_TYPE_TIYU = "tiyu";
    public static final String HOTWORD_TYPE_SHUMA = "shuma";
    public static String URL_HOT_WORD = "http://api.m.baidu.com/?type=hot&c=" + HOTWORD_TYPE_SHISHI + "&from=" + BAIDU_TRADE_ID_HOTWORD;
    public static String URL_SEARCH_WITH_BAIDU = "http://m.baidu.com/s?from=" + BAIDU_TRADE_ID_HOTWORD + "&word=";//最后面需要拼接关键字


    /**
     * 神马搜索
     */
    public static final String SHEN_MA_SEARCH_KEY = "wm946679";
    public static final String SHEN_MA_HOTWORD_KEY = "wm848022";
    public static final String SHEN_MA_KEYW0RD = "";
    public static final String SHEN_MA_SITE = "http://m.yz2.sm.cn/";
    public static final String SHEN_MA_HOMEPAGE = "http://m.yz2.sm.cn/?from="+SHEN_MA_SEARCH_KEY;
    public static final String SHEN_MA_SEARCH_BOX = "http://m.yz2.sm.cn/s?from="+SHEN_MA_SEARCH_KEY+"&q=";//最后需要拼接关键字
    public static final String SHEN_MA_COMBOBOX_HINT = "http://sugs.m.sm.cn/api?wd="+SHEN_MA_KEYW0RD+"&vd"+SHEN_MA_SEARCH_KEY;//需要给keywork赋值,或者单独拼接此url
    public static final String SHEN_MA_COMBOBOX_ICON = SHEN_MA_HOMEPAGE;//神马icon与神马主页url相同
    public static final String SHEN_MA_HOTWORD_8 = "http://api.m.sm.cn/rest?method=tools.hot&start=1&from="+SHEN_MA_HOTWORD_KEY;
    public static final String SHEN_MA_HOTWORD_24 = "http://api.m.sm.cn/rest?method=tools.hot&size=0&from="+SHEN_MA_HOTWORD_KEY;


    //UC浏览器
    public static final String UC_packageName = "com.UCMobile";
    public static final String UC_mainActivity = "com.UCMobile.main.UCMobile";

    public static final String QQ_packageName = "com.tencent.mtt";
    public static final String QQ_mainActivity = "com.tencent.mtt.SplashActivity";
}
