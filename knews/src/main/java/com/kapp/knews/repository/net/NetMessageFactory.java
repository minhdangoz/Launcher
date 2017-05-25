package com.kapp.knews.repository.net;

import android.os.Message;

import com.apkfuns.logutils.LogUtils;
import com.kapp.knews.R;
import com.kapp.knews.helper.content.resource.ValuesHelper;


/**
 * Created by xixionghui on 2016/11/24.
 */

public class NetMessageFactory {



    //热词部分
    public static final int MESSAGE_WHAT_OBTAION_HOTWORD = 21;//获取百度热词的MESSAGE_WHAT

    //导航部分
    public static final int MESSAGE_WHAT_OBTAION_NAVIGATION = 31;//获取Navigation的MESSAGE_WHAT

    //新闻部分
    public static final int MESSAGE_OBTAION_DONGFANG_KEY = 51;
    public static final int MESSAGE_OBTAION_DONGFANG_ARTICLE = 52;


    public static Message createMessage(int what) {
        Message msg = Message.obtain();
        msg.arg1 = NetResponse.UNKNOW_EXECTION;
        switch (what) {
            case MESSAGE_OBTAION_DONGFANG_KEY:
                msg.what = MESSAGE_OBTAION_DONGFANG_KEY;
                break;
            case MESSAGE_OBTAION_DONGFANG_ARTICLE:
                msg.what = MESSAGE_OBTAION_DONGFANG_ARTICLE;
                break;
            case MESSAGE_WHAT_OBTAION_HOTWORD:
                msg.what = MESSAGE_WHAT_OBTAION_HOTWORD;
                break;
            case MESSAGE_WHAT_OBTAION_NAVIGATION:
                msg.what = MESSAGE_WHAT_OBTAION_NAVIGATION;
                break;
            default:
                LogUtils.e(ValuesHelper.getString(R.string.unknow_request_message_id));
                break;
        }
        return msg;
    }
}
