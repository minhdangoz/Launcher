package com.klauncher.kinflow.common.utils;

import com.klauncher.launcher.BuildConfig;

/**
 * Created by xixionghui on 2016/3/17.
 */
public class Const {

    /**
     * 百度
     */
    public static final String BAIDU_APIKEY = "2f08b705803233298c235eedb74b84ab";//百度apiKey
    public static final String BAIDU_TRADE_ID_HOTWORD = "1010445i";//百度ID
    public static final String HOTWORD_TYPE_SHISHI ="shishi";
    public static final String HOTWORD_TYPE_DIANYING ="dianying";
    public static final String HOTWORD_TYPE_ZONGYI ="zongyi";
    public static final String HOTWORD_TYPE_DIANSHIJU ="dianshiju";
    public static final String HOTWORD_TYPE_DONGMAN ="dongman";
    public static final String HOTWORD_TYPE_TITAN ="titan";
    public static final String HOTWORD_TYPE_TIYU ="tiyu";
    public static final String HOTWORD_TYPE_SHUMA ="shuma";
    public static String URL_HOT_WORD = "http://api.m.baidu.com/?type=hot&c="+HOTWORD_TYPE_SHISHI+"&from="+ BAIDU_TRADE_ID_HOTWORD;
    public static String URL_SEARCH_WITH_BAIDU = "http://m.baidu.com/s?from"+ BAIDU_TRADE_ID_HOTWORD +"&word=";//最后面需要拼接关键字

    /**
     * 神马搜索
     */
    public static final String SHEN_MA_SEARCH_KEY = "wm946679";
    public static final String SHEN_MA_HOTWORD_KEY = "wm848022";
    public static final String SHEN_MA_KEYW0RD = "";
    public static final String SHEN_MA_HOMEPAGE = "http://m.yz2.sm.cn/?from="+SHEN_MA_SEARCH_KEY;
    public static final String SHEN_MA_SEARCH_BOX = "http://m.yz2.sm.cn/s?from="+SHEN_MA_SEARCH_KEY+"&q=";//最后需要拼接关键字
    public static final String SHEN_MA_COMBOBOX_HINT = "http://sugs.m.sm.cn/api?wd="+SHEN_MA_KEYW0RD+"&vd"+SHEN_MA_SEARCH_KEY;//需要给keywork赋值,或者单独拼接此url
    public static final String SHEN_MA_COMBOBOX_ICON = SHEN_MA_HOMEPAGE;//神马icon与神马主页url相同
    public static final String SHEN_MA_HOTWORD_8 = "http://api.m.sm.cn/rest?method=tools.hot&start=1&from="+SHEN_MA_HOTWORD_KEY;
    public static final String SHEN_MA_HOTWORD_24 = "http://api.m.sm.cn/rest?method=tools.hot&size=0&from="+SHEN_MA_HOTWORD_KEY;


    /**
     * 今日头条Uri
     */
//    public static final String URI_TOUTIAO_ARTICLE_DETAIL = "snssdk143://detail?gd_label=delong&groupid=";//第一版
    public static final String URI_TOUTIAO_ARTICLE_DETAIL = "snssdk143://detail?gd_label=click_wap_delong_detail&groupid=";//第二版

    /*
    *server
    */
    public static final String REALEASE_HOST = "http://api.klauncher.com";
    public static final String TEST_HOST = "http://test.api.klauncher.com";
//    public static final String TEST_CID = "10033";
    public static final String REALEASE_CID = BuildConfig.CHANNEL_ID;
//    public static final String REALEASE_CID = LauncherApplication.cid;
    public static final String CARD_GET = REALEASE_HOST+"/v1/card?cid="+REALEASE_CID;
//    public static final String NAVIGATION_GET = REALEASE_HOST+"/v1/nav?cid="+REALEASE_CID;
    public static final String NAVIGATION_GET = REALEASE_HOST+"/v1/nav/mobile?cid="+REALEASE_CID;
    public static final String NAVIGATION_LOCAL_LAST_MODIFIED = "local_lastModified";
    public static final String NAVIGATION_LOCAL_SERVER_MODIFIED = "lastModified";
    public static final String NAVIGATION_LOCAL_UPDATE_INTERVAL = "local_updateInterval";
    public static final String NAVIGATION_SERVER_UPDATE_INTERVAL = "updateInterval";

    //Android默认浏览器（Android新版本默认是chrome）
    public static final String DEFAULT_BROWSER_packageName = "com.android.browser";
    public final String DEFAULT_BROWSER_mainActivity = "com.android.browser.BrowserActivity";


    //Chrome浏览器
    public static final String CHROME_packageName = "com.android.chrome";
    public static final String CHROME_mainActivity = "com.google.android.apps.chrome.Main";

    //UC浏览器
    public static final String UC_packageName = "com.UCMobile";
    public static final String UC_mainActivity = "com.UCMobile.main.UCMobile";

    public static final String QQ_packageName = "com.tencent.mtt";
    public static final String QQ_mainActivity = "com.tencent.mtt.SplashActivity";

    //今日头条
    public static final String TOUTIAO_packageName = "com.ss.android.article.news";
    public static final String TOUTIAO_mainActivity = "com.ss.android.article.news.activity.SplashActivity";

    //地理位置逆解析
    public static final String URL_GECODER_LOCATION ="http://api.map.baidu.com/geocoder?location=";//需要拼接三个参数:lat,lng,key

    //时长
    public static final long DURATION_ONE_MILLISECOND =1;//毫秒
    public static final long DURATION_SECOND = DURATION_ONE_MILLISECOND*1000;
    public static final long DURATION_ONE_MINUTE = DURATION_SECOND * 60;
    public static final long DURATION_ONE_HOUR = DURATION_ONE_MINUTE*60;
    public static final long DURATION_FOUR_HOUR = DURATION_ONE_HOUR*4;


    //距离
    public static final long DISTANCE_IN_METER = 1;//米
    public static final long DISTANCE_IN_KM = DISTANCE_IN_METER*1000;//米
    public static final long DISTANCE_LOCATION_UPDATE = DISTANCE_IN_KM*5;//5千米更新一次

    //获取当前经纬度所在城市
    //http://api.map.baidu.com/geocoder?location=30.990998,103.645966&output=json&key=28bcdd84fae25699606ffad27f8da77b
    //
    public static final String OBTAIN_CITY_NAME = "http://api.map.baidu.com/geocoder";
    //获取城市天气，根据城市名称
    public static String OBTAION_WEATHER_BY_CITY_NAME = "http://apis.baidu.com/apistore/weatherservice/cityname";

    //一点资讯
    public static final String YIDIAN_APPID = "17Mgz4F2WfWvrWQ9z8J6iAfi";
    public static final String YIDIAN_APPKEY = "lxRgj53VtpL6wL0t1NkX1IxxRvlZtf0j";
    public static final int YI_DIAN_CHANNEL_JingXuan = 0;
    public static final int YI_DIAN_CHANNEL_ReMen = 1;
    public static final int YI_DIAN_CHANNEL_YuLe = 2;
    public static final int YI_DIAN_CHANNEL_QiChe = 3;
    public static final int YI_DIAN_CHANNEL_JianKang = 4;
    public static final int YI_DIAN_CHANNEL_LvYou = 5;
    public static final int YI_DIAN_CHANNEL_ID = YI_DIAN_CHANNEL_JingXuan;
    public static final String  URL_YI_DIAN_ZI_XUN_HOUT_DEBUG ="http://o.go2yd.com/oapi/sample/channel?";//测试
    public static final String  URL_YI_DIAN_ZI_XUN_HOUT_RELEASE ="http://o.go2yd.com/open-api/delong/channel";
    public static final String YI_DIAN_CHANNEL_MORE_JingXuan = "http://www.yidianzixun.com/home";
    public static final String YI_DIAN_CHANNEL_MORE_ReDian = "http://www.yidianzixun.com/home?page=channel&id=hot";
    public static final String YI_DIAN_CHANNEL_MORE_YuLe = "http://www.yidianzixun.com/home?page=channel&keyword=%E5%A8%B1%E4%B9%90";
    public static final String YI_DIAN_CHANNEL_MORE_QiChe = "http://www.yidianzixun.com/home?page=channel&keyword=%E6%B1%BD%E8%BD%A6";
    public static final String YI_DIAN_CHANNEL_MORE_JianKang = "http://www.yidianzixun.com/home?page=channel&keyword=%E5%81%A5%E5%BA%B7";
    public static final String YI_DIAN_CHANNEL_MORE_LvYou = "http://www.yidianzixun.com/home?page=channel&keyword=%E6%97%85%E6%B8%B8";
    public static final String URL_TIMESTAMP = "http://api.klauncher.com/v1/card/gettime";
}
