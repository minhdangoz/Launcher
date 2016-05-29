package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by xixionghui on 16/4/18.
 */
public class YMCardContentManager extends BaseCardContentManager {
    CardInfo mCardInfo;
    String imageUrl;
    String clickUrl;
//    static final String KEY_IMAGE_URL="imageUrl";
//    static final String KEY_CLICK_URL="clickUrl";
    private Handler mMainControlHandler;//MainControl的handler
//    private Handler mHandle = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            HashMap<String,String> resultHashMap = (HashMap<String, String>) msg.obj;
//            imageUrl = resultHashMap.get(KEY_IMAGE_URL);
//            clickUrl = resultHashMap.get(KEY_CLICK_URL);
//            mMainControlHandler.sendMessage(msg);
//        }
//    };

    /**
     * @param context
     */
    YMCardContentManager(Context context) {
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

    public String getClickUrl() {
        return clickUrl;
    }

    @Override
    public void requestCardContent(Handler mainControlHandler, CardInfo cardInfo) {
        try {
            this.mMainControlHandler = mainControlHandler;
            this.mCardInfo = cardInfo;

            JSONObject json = null;
            json = new JSONObject(cardInfo.getCardExtra());
            if (json != null && json.has("img") && json.has("clc")) {
                imageUrl = json.optString("img");
                clickUrl = json.optString("clc");
                HashMap<String,String> resultHashMap = new HashMap<>();
                Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YOKMOB);
                mMainControlHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            log("请求YOKMOB时出错:"+e.getMessage());
            Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YOKMOB);
            mMainControlHandler.sendMessage(msg);
        }

    }
}
