package com.klauncher.kinflow.common.utils;

import com.klauncher.launcher.BuildConfig;

/**
 * Created by xixionghui on 2016/3/17.
 */
public class Const {

    /**
     * 百度
     */
    public static final String BAIDU_APIKEY = "2f08b705803233298c235eedb74b84ab";//百度apiKey:应该没用了
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
    public static final String SHEN_MA_SITE = "http://m.yz2.sm.cn/";
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
    public static final String TOUTIAO_SECURE_KEY = "cde8fee9e391062045d05c7848859464";//partner与client_id相同，都是delong；secure_key与client_secret相同都是cde8fee9e391062045d05c7848859464
    public static final String TOUTIAO_PARTNER = "delong";//partner与client_id相同，都是delong；secure_key与client_secret相同都是cde8fee9e391062045d05c7848859464
    public static final String TOUTIAO_URL_ACCESS_TOKEN = "http://open.snssdk.com/auth/access/web/";
    public static final String URL_JINRITOUTIAO_GET_CUSTOM_ARTICLE = "http://open.snssdk.com/data/stream/v3/";
    public static final String TOUTIAO_COMMON_PARTNER = "delong";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_TUIJIAN = "__all__";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_REDIAN = "news_hot";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_SHEHUI = "news_society";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_YULE = "news_entertainment";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_KEJI = "news_tech";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_QICHE = "news_car";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_CARJING = "news_finance";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_JUNSHI = "news_military";
    public static final String TOUTIAO_CUSTOM_ARTICLE_CATEGORY_TIYU = "news_sports";

    /*
    *server
    */
    public static final String REALEASE_HOST = "http://api.klauncher.com";
    public static final String TEST_HOST = "http://test.api.klauncher.com";
    public static final String TEST_CID = "1001_2101_00100000000";
    public static final String REALEASE_CID = BuildConfig.CHANNEL_ID;
//    public static final String REALEASE_CID = LauncherApplication.cid;
    public static final String CARD_GET = REALEASE_HOST+"/v1/card?cid="+REALEASE_CID;
//    public static final String NAVIGATION_GET = REALEASE_HOST+"/v1/nav?cid="+REALEASE_CID;
    public static final String NAVIGATION_GET = REALEASE_HOST+"/v1/nav/mobile?cid="+REALEASE_CID;
    public static final String NAVIGATION_LOCAL_FIRST_INIT = "local_init";
    public static final String NAVIGATION_LOCAL_LAST_MODIFIED = "local_lastModified";
    public static final String NAVIGATION_LOCAL_SERVER_MODIFIED = "lastModified";
    public static final String NAVIGATION_LOCAL_UPDATE_INTERVAL = "local_updateInterval";
    public static final String NAVIGATION_SERVER_UPDATE_INTERVAL = "updateInterval";
//    public static final String CONFIG_SETTINGS_SWITCH = TEST_HOST+"/v1/config/switch?cid="+TEST_CID;
//    public static final String CONFIG_SETTINGS_SWITCH = REALEASE_HOST+"/v1/config/switch?cid="+REALEASE_CID;
//    public static final String CONFIG_FUNCTION_LIST = TEST_HOST+"/v1/config/funclist?cid="+TEST_CID;
//    public static final String CONFIG_FUNCTION_LIST = REALEASE_HOST+"/v1/config/funclist?cid="+REALEASE_CID;
//    public static final String CONFIG = REALEASE_HOST+"/v2/config?cid="+REALEASE_CID;
    public static final String CONFIG = "http://test.api.klauncher.com/kplatform/v2/config?cid=1002_1201_00100000000";

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
    public static final String YI_DIAN_CHANNEL_MORE_JingXuan = "http://www.yidianzixun.com/home";//一点资讯精选
    public static final String YI_DIAN_CHANNEL_MORE_ReDian = "http://www.yidianzixun.com/home?page=channel&id=hot";//一点资讯热点
    public static final String YI_DIAN_CHANNEL_MORE_SheHui = "http://www.yidianzixun.com/home?page=channel&keyword=%E7%A4%BE%E4%BC%9A";//一点资讯社会
    public static final String YI_DIAN_CHANNEL_MORE_YuLe = "http://www.yidianzixun.com/home?page=channel&keyword=%E5%A8%B1%E4%B9%90";//一点资讯娱乐
    public static final String YI_DIAN_CHANNEL_MORE_CarJing = "http://www.yidianzixun.com/home?page=channel&keyword=%E8%B4%A2%E7%BB%8F";//一点资讯财经
    public static final String YI_DIAN_CHANNEL_MORE_TiYu = "http://www.yidianzixun.com/home?page=channel&keyword=%E4%BD%93%E8%82%B2";//一点资讯体育
    public static final String YI_DIAN_CHANNEL_MORE_KeJi = "http://www.yidianzixun.com/home?page=channel&keyword=%E7%A7%91%E6%8A%80";//一点资讯科技
    public static final String YI_DIAN_CHANNEL_MORE_JunShi = "http://www.yidianzixun.com/home?page=channel&keyword=%E5%86%9B%E4%BA%8B";//一点资讯军事
    public static final String YI_DIAN_CHANNEL_MORE_MinSheng = "http://www.yidianzixun.com/home?page=channel&keyword=%E6%B0%91%E7%94%9F";//一点资讯民生
    public static final String YI_DIAN_CHANNEL_MORE_MeiNv = "http://www.yidianzixun.com/home?page=channel&keyword=%E7%BE%8E%E5%A5%B3";//一点资讯美女
    public static final String YI_DIAN_CHANNEL_MORE_DuanZi = "http://www.yidianzixun.com/home?page=channel&keyword=%E6%AE%B5%E5%AD%90";//一点资讯段子
    public static final String YI_DIAN_CHANNEL_MORE_JianKang = "http://www.yidianzixun.com/home?page=channel&keyword=%E5%81%A5%E5%BA%B7";//一点资讯健康
    public static final String YI_DIAN_CHANNEL_MORE_ShiShang = "http://www.yidianzixun.com/home?page=channel&keyword=%E6%97%B6%E5%B0%9A";//一点资讯时尚
    public static final String YI_DIAN_CHANNEL_MORE_QiChe = "http://www.yidianzixun.com/home?page=channel&keyword=%E6%B1%BD%E8%BD%A6";//一点资讯汽车
    public static final String YI_DIAN_CHANNEL_MORE_GaoXiao = "http://www.yidianzixun.com/home?page=channel&keyword=%E6%90%9E%E7%AC%91";//一点资讯搞笑
    public static final String YI_DIAN_CHANNEL_MORE_ShiPin = "http://www.yidianzixun.com/home?page=channel&keyword=%E8%A7%86%E9%A2%91";//一点资讯视频
    public static final String YI_DIAN_CHANNEL_MORE_DianYing = "http://www.yidianzixun.com/home?page=channel&keyword=%E7%94%B5%E5%BD%B1";//一点资讯电影
    public static final String YI_DIAN_CHANNEL_MORE_JianShen = "http://www.yidianzixun.com/home?page=channel&keyword=%E5%81%A5%E8%BA%AB";//一点资讯健身
    public static final String YI_DIAN_CHANNEL_MORE_LvYou = "http://www.yidianzixun.com/home?page=channel&keyword=%E6%97%85%E6%B8%B8";//一点资讯旅游
    public static final String URL_TIMESTAMP = "http://api.klauncher.com/v1/card/gettime";

    /**
     * SharedPreference---key
     */
    public static final String KEY_CARD_CLEAR_OFFSET = "card_clear_offset";
    public static final String KEY_LAST_SKIP_TIME = "last_skip_time";//在信息里流界面一天以内,随机跳转到2345界面

    //other
    public static final String URL_2345_HOMEPAGE = "http://m.2345.com/?sc_delong";
}
