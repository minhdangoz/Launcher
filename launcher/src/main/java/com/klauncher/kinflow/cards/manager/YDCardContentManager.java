package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.model.yidian.YiDianModel;
import com.klauncher.kinflow.cards.utils.CardUtils;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.task.AsynchronousGet;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.common.utils.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 16/4/12.
 */
public class YDCardContentManager extends BaseCardContentManager {

    private CardInfo mCardInfo;
    private int mOurDefineChannelId = CardIdMap.CARD_TYPE_NEWS_YD_REDIAN;//其实就是上面CardInfo的cardSecondTypeId
    private final int mCount = 5;//每次请求个数,通常为5不变
    private List<YiDianModel> mYiDianModelList = new ArrayList<>();

    private Handler mHandler;//本Card的handler
    private Handler mainControlHandler;//MainControl的handler

    //初始化
    static {
        CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME + CardIdMap.CARD_TYPE_NEWS_YD_JINGXUAN, 0);
        CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME + CardIdMap.CARD_TYPE_NEWS_YD_REDIAN, 0);
        CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME + CardIdMap.CARD_TYPE_NEWS_YD_YULE, 0);
        CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME + CardIdMap.CARD_TYPE_NEWS_YD_QICHE, 0);
        CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME + CardIdMap.CARD_TYPE_NEWS_YD_JIANKANG, 0);
        CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME + CardIdMap.CARD_TYPE_NEWS_YD_LVYOU, 0);
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MessageFactory.MESSAGE_WHAT_TIMESTAMP:
                    if (msg.arg1 == AsynchronousGet.SUCCESS && null != msg.obj) {
                        timestamp = (String) msg.obj;
                    }else {
                        timestamp = String.valueOf((int) ((System.currentTimeMillis()) / 1000));
                    }
                    try {
                        new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YIDIAN).run(getRequestUrl());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YIDIAN:
                    if (msg.arg1 == AsynchronousGet.SUCCESS) {
                        //计算下次的偏移量offset
                        int offset = CommonShareData.getInt(CardContentManagerFactory.OFFSET_NAME + mOurDefineChannelId, 0) + 5;
                        //存储偏移量offset
                        CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME + mOurDefineChannelId, offset);
                        handleObtainedData(msg);
                    } else {
                        onFailToast(msg);
                    }
                    mainControlHandler.handleMessage(msg);
                    break;
            }
            return true;
        }
    };

    YDCardContentManager(Context context) {
        super(context);
        this.mHandler = new Handler(mCallback);
    }

    public int getmOurDefineChannelId() {
        return mOurDefineChannelId;
    }

    /**
     0,card的网络请求都是根据cardInfo.senCondType发起的请求
     1,如何发起网络请求，如何处理网络请求结果
     2,如何统一管理子类的网络请求，与数据处理
     */


    /**
     * 恢复管理器的默认值
     * mOffset = 0;
     * isFirst = true;
     */
//    private void recover() {
//        this.mOffSet = 0;
//        this.isFirst = true;
//    }

    String timestamp;

    public String getRequestUrl() {
//        StringBuilder stringBuilder = new StringBuilder(Const.URL_YI_DIAN_ZI_XUN_HOUT_DEBUG);
        StringBuilder stringBuilder = new StringBuilder(Const.URL_YI_DIAN_ZI_XUN_HOUT_RELEASE);
//        /*
        if (null == timestamp)
            timestamp = String.valueOf((int) ((System.currentTimeMillis()) / 1000));
//        int timestamp = (int)((new Date().getTime())/1000);
        String nonce = CommonUtils.getInstance().getRandomString(5);//生成5个随机字符串
        //String appkey,String nonce,String timestamp
        String secretkey = CommonUtils.getSecretkey(Const.YIDIAN_APPKEY, nonce, timestamp);
        stringBuilder.append("?appid=").append(Const.YIDIAN_APPID);
        stringBuilder.append("&secretkey=").append(secretkey);
        stringBuilder.append("&timestamp=").append(timestamp);
        stringBuilder.append("&nonce=").append(nonce);
//        */
        //获取偏移量,如果没有获取到偏移量则使用默认偏移量0
        int mOffSet = CommonShareData.getInt(CardContentManagerFactory.OFFSET_NAME + mOurDefineChannelId, 0);
        stringBuilder.append("&channel_id=").append(CardIdMap.getYiDianChannelId(mOurDefineChannelId));//channelId
        stringBuilder.append("&offset=").append(String.valueOf(mOffSet));//偏移量
        stringBuilder.append("&count=").append(String.valueOf(this.mCount));
        return stringBuilder.toString();
    }


    @Override
    public void moreNews() {

    }

    @Override
    public void changeNews() {
        log("changeNews");
        if (null != mainControlHandler)
            requestCardContent(this.mainControlHandler, this.mCardInfo);
    }

    public List<YiDianModel> getmYiDianModelList() {
        return mYiDianModelList;
    }

    public void setYiDianModelList(List<YiDianModel> yiDianModelList) {
        mYiDianModelList = yiDianModelList;
    }

    //    String timestampUrl = "http://api.klauncher.com/v1/card/gettime";

    /**
     * 查看如何根据要请求的channel：secondType获取下一组一点资讯数据
     */
    @Override
    public void requestCardContent(Handler mainControlHandler, CardInfo cardInfo) {
        this.mCardInfo = cardInfo;
        this.mainControlHandler = mainControlHandler;
        this.mOurDefineChannelId = cardInfo.getCardSecondTypeId();
        try {
//            new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YIDIAN).run(getRequestUrl());
            new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_TIMESTAMP).run(Const.URL_TIMESTAMP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void handleObtainedData(Message msg) {
        if(null!=msg.obj) {
            List<YiDianModel> newYiDianModelList = (List<YiDianModel>) msg.obj;
            if (newYiDianModelList.size()!=0) {
                mYiDianModelList.clear();
                mYiDianModelList = CardUtils.sortYiDianModelList(newYiDianModelList);
            }
        }
    }

}
