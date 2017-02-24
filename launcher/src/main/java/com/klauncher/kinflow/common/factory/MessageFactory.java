package com.klauncher.kinflow.common.factory;

import android.os.Message;

/**
 * Created by xixionghui on 2016/3/17.
 */
public class MessageFactory {
    public static final int MESSAGE_WHAT_ORROR = -999;//当请求数据如果发生错误,发送此message给MainControl.以便停止刷新转圈
    public static final int MESSAGE_WHAT_OBTAION_CARD = 1;//获取Card的MESSAGE_WHAT
    public static final int MESSAGE_WHAT_OBTAION_HOTWORD = 2;//获取百度热词的MESSAGE_WHAT
    public static final int MESSAGE_WHAT_OBTAION_NAVIGATION = 3;//获取Navigation的MESSAGE_WHAT
    public static final int MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY = 31;//获取Navigation的MESSAGE_WHAT
    public static final int MESSAGE_WHAT_OBTAION_CITY_NAME = 4;//获取当前位置的城市名称
    public static final int MESSAGE_WHAT_OBTAION_CITY_WEATHER = 5;//获取天气
    public static final int MESSAGE_WHAT_OBTAION_NEWS_YIDIAN = 6;//获取一点资讯
    public static final int MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO_SDK = 7;//获取头条,通过sdk
    public static final int MESSAGE_WHAT_OBTAION_NEWS_YOKMOB = 8;//获取YOKMOB
    public static final int MESSAGE_WHAT_OBTAION_NEWS_ADVIEW = 9;//获取ADVIEW
    public static final int MESSAGE_WHAT_TIMESTAMP = 10;//获取时间戳
    public static final int MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH = 11;//获取配置信息
    public static final int MESSAGE_WHAT_OBTAIN_FUNCTION_LIST = 12;//获取FUNCTION_LIST
    public static final int MESSAGE_WHAT_OBTAIN_CONFIG = 13;//获取所有config
    public static final int MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN = 14;//获取头条TOKEN,通过api
    public static final int MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE = 15;//获取头条ARTICLE,通过api
    public static final int MESSAGE_WHAT_OBTAIN_SOUGOU_SEARCH_ARTICLE = 16;//获取到搜狗搜索新闻
    public static final int MESSAGE_WHAT_OBTAIN_KINFLOW2_SERVER_CONTROLLER = 17;//信息流第二版,后台控制:数据类型:新闻1,广告23,打开方式,广告信息,广告位置.新闻数据具体类型不再指定(搜狗搜索新闻和今日头条新闻交替显示).
    public static final int MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_DIANYING = 18;//获取到amap的电影背景图的url-----可能用不到
    public static final int MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_YULE = 19;//获取到amap的娱乐(周末去哪儿)背景图的url------可能用不到


    public static Message createMessage(int what) {
        Message msg = Message.obtain();
        switch (what) {
            case MESSAGE_WHAT_OBTAION_CARD:
                msg.what = MESSAGE_WHAT_OBTAION_CARD;
                break;
            case MESSAGE_WHAT_OBTAION_HOTWORD:
                msg.what = MESSAGE_WHAT_OBTAION_HOTWORD;
                break;
            case MESSAGE_WHAT_OBTAION_NAVIGATION:
                msg.what = MESSAGE_WHAT_OBTAION_NAVIGATION;
                break;
            case MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY:
                msg.what =MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY;
                break;
            case MESSAGE_WHAT_OBTAION_CITY_NAME:
                msg.what = MESSAGE_WHAT_OBTAION_CITY_NAME;
                break;
            case MESSAGE_WHAT_OBTAION_CITY_WEATHER:
                msg.what = MESSAGE_WHAT_OBTAION_CITY_WEATHER;
                break;
            case MESSAGE_WHAT_OBTAION_NEWS_YIDIAN:
                msg.what = MESSAGE_WHAT_OBTAION_NEWS_YIDIAN;
                break;
            case MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO_SDK:
                msg.what = MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO_SDK;
                break;
            case MESSAGE_WHAT_OBTAION_NEWS_YOKMOB:
                msg.what = MESSAGE_WHAT_OBTAION_NEWS_YOKMOB;
                break;
            case MESSAGE_WHAT_OBTAION_NEWS_ADVIEW:
                msg.what = MESSAGE_WHAT_OBTAION_NEWS_ADVIEW;
                break;
            case MESSAGE_WHAT_TIMESTAMP:
                msg.what = MESSAGE_WHAT_TIMESTAMP;
                break;
            case MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH:
                msg.what = MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH;
                break;
            case MESSAGE_WHAT_OBTAIN_FUNCTION_LIST:
                msg.what = MESSAGE_WHAT_OBTAIN_FUNCTION_LIST;
                break;
            case MESSAGE_WHAT_OBTAIN_CONFIG:
                msg.what = MESSAGE_WHAT_OBTAIN_CONFIG;
                break;
            case MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN:
                msg.what = MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN;
                break;
            case MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE:
                msg.what = MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE;
                break;
            case MESSAGE_WHAT_OBTAIN_SOUGOU_SEARCH_ARTICLE:
                msg.what = MESSAGE_WHAT_OBTAIN_SOUGOU_SEARCH_ARTICLE;
                break;
            case MESSAGE_WHAT_OBTAIN_KINFLOW2_SERVER_CONTROLLER:
                msg.what = MESSAGE_WHAT_OBTAIN_KINFLOW2_SERVER_CONTROLLER;
                break;
            case MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_DIANYING:
                msg.what = MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_DIANYING;
                break;
            case MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_YULE:
                msg.what = MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_YULE;
                break;
        }
        return msg;
    }


    public static final int REQUEST_ALL_KINFLOW[] = {
            MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,//热词
            MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,//网页导航
            MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY,//内容导航
            MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG,//config配置信息
            MessageFactory.MESSAGE_WHAT_OBTAIN_SOUGOU_SEARCH_ARTICLE,//搜狗搜索新闻----待完成
            MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE,//api版本的头条新闻----待完成
//            MessageFactory.MESSAGE_WHAT_OBTAIN_KINFLOW2_SERVER_CONTROLLER//kinflow2后台控制----已经转移到ServerControlManager
    };

    public static final int REQUEST_HOTWORD_KINFLOW[] = {
            MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD
    };

    public static final int REQUEST_NAVIGATION_KINFLOW[] = {
            MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION
    };

    public static final int REQUEST_CONFIG_KINFLOW[] = {
            MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG
    };

    public static final int REQUEST_CARD_KINFLOW[] = {
            MessageFactory.MESSAGE_WHAT_OBTAION_CARD
    };

    public static final int REQUEST_SOUGOU_SEARCH_ARTICLE[] = {
            MessageFactory.MESSAGE_WHAT_OBTAIN_SOUGOU_SEARCH_ARTICLE
    };
}
