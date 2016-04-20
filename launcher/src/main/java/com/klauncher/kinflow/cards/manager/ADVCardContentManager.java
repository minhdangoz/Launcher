package com.klauncher.kinflow.cards.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.kyview.interfaces.AdNativeInterface;
import com.kyview.natives.AdNativeManager;
import com.kyview.natives.NativeAdInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 16/4/18.
 */
public class ADVCardContentManager extends BaseCardContentManager {

    private Context mContext;
    private AdNativeManager mAdNativeManager;
    private CardInfo mCardInfo;
    private Handler mMainControlHandler;//MainControl的handler;
    private List<NativeAdInfo> mNativeAdInfoList;

    /**
     * @param context
     */
    ADVCardContentManager(Context context) {
        super(context);
        this.mContext = context;
        mAdNativeManager = new AdNativeManager((Activity) mContext, "SDK20161518030339tw90ofxelsoctuo");
    }

    @Override
    public void moreNews() {

    }

    @Override
    public void changeNews() {

    }

    public List<NativeAdInfo> getNativeAdInfoList() {
        return mNativeAdInfoList;
    }

    @Override
    public void requestCardContent(Handler mainControlHandler, CardInfo cardInfo) {
        log("开始请求adView数据");
        this.mMainControlHandler = mainControlHandler;
        this.mCardInfo = cardInfo;
        this.mAdNativeManager.setAdNativeInterface(new AdNativeInterface() {
            @Override
            public void onFailedReceivedAd(String s) {
                ADVCardContentManager.this.mNativeAdInfoList = new ArrayList<NativeAdInfo>();
                Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW);
                ADVCardContentManager.this.mMainControlHandler.sendMessage(msg);
            }

            @Override
            public void onReceivedAd(List list) {
                ADVCardContentManager.this.mNativeAdInfoList = list;
                Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW);
                ADVCardContentManager.this.mMainControlHandler.sendMessage(msg);
            }

            @Override
            public void onAdStatusChanged(int i) {

            }
        });
        mAdNativeManager.requestAd();
    }
}
