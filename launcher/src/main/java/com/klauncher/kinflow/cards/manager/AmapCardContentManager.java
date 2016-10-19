package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;

import org.json.JSONObject;

/**
 * Created by xixionghui on 16/4/18.
 * 高德电影院和周末去哪儿的内容控制管理器
 */
public class AmapCardContentManager extends BaseCardContentManager {
    CardInfo mCardInfo;
    String imageUrl;
    private Handler mMainControlHandler;//MainControl的handler

    /**
     * @param context
     */
    AmapCardContentManager(Context context) {
        super(context);
    }

    @Override
    public void moreNews() {

    }

    @Override
    public void changeNews() {

    }

    public String getImageUrl() {
        return imageUrl;
    }


    @Override
    public void requestCardContent(Handler mainControlHandler, CardInfo cardInfo) {
        Log.d("Kinflow", "Amap请求内容数据requestCardContent");
        try {
            this.mMainControlHandler = mainControlHandler;
            this.mCardInfo = cardInfo;

            JSONObject json = null;
            json = new JSONObject(cardInfo.getCardExtra());
            Log.d("Kinflow", "Amap请求到json数据: "+json.toString());
            if (json != null && json.has("img")) {
                imageUrl = json.optString("img");
                Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_DIANYING);
                msg.arg1 = -1;
                mMainControlHandler.sendMessage(msg);
            } else {
                log("服务器端配置的Amap背景大图的json数据有误,可能为:1>json为空;2>json不包含img字段;3>json不包含clc字段. \njson=" + json.toString());
                Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_DIANYING);
                msg.arg1 = -1;
                mMainControlHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            log("请求Amap的图片时出错:" + e.getMessage());
            Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAIN_AMAP_BACKGROUND_DIANYING);
            msg.arg1 = -1;
            mMainControlHandler.sendMessage(msg);
        }

    }
}
