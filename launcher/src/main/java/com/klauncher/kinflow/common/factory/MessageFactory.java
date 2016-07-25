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
    public static final int MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_DIANYING = 16;//获取到amap的电影背景图的url
    public static final int MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_YULE = 17;//获取到amap的娱乐(周末去哪儿)背景图的url


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
            MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
            MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,
            MessageFactory.MESSAGE_WHAT_OBTAION_CARD,
            MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG
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
}
