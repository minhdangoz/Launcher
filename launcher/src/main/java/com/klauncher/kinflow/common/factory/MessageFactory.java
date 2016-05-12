package com.klauncher.kinflow.common.factory;

import android.os.Message;

/**
 * Created by xixionghui on 2016/3/17.
 */
public class MessageFactory {

    public static final int MESSAGE_WHAT_OBTAION_CARD = 1;//获取Card的MESSAGE_WHAT
    public static final int MESSAGE_WHAT_OBTAION_HOTWORD = 2;//获取百度热词的MESSAGE_WHAT
    public static final int MESSAGE_WHAT_OBTAION_NAVIGATION = 3;//获取Navigation的MESSAGE_WHAT
    public static final int MESSAGE_WHAT_OBTAION_CITY_NAME = 4;//获取当前位置的城市名称
    public static final int MESSAGE_WHAT_OBTAION_CITY_WEATHER = 5;//获取天气
    public static final int MESSAGE_WHAT_OBTAION_NEWS_YIDIAN = 6;//获取一点资讯
    public static final int MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO = 7;//获取头条
    public static final int MESSAGE_WHAT_OBTAION_NEWS_YOKMOB = 8;//获取YOKMOB
    public static final int MESSAGE_WHAT_OBTAION_NEWS_ADVIEW = 9;//获取ADVIEW
    public static final int MESSAGE_WHAT_TIMESTAMP = 10;//获取时间戳
    public static final int MESSAGE_WHAT_OBTAIN_CONFIG = 11;//获取配置信息

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
            case MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO:
                msg.what = MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO;
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
            case MESSAGE_WHAT_OBTAIN_CONFIG:
                msg.what = MESSAGE_WHAT_OBTAIN_CONFIG;
                break;
        }
        return msg;
    }

}
