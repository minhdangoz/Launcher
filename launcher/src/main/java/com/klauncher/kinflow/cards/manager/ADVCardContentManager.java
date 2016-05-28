package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;
//import com.kyview.natives.AdNativeManager;

//import com.kyview.natives.AdNativeManager;
//import com.kyview.natives.NativeAdInfo;

/**
 * Created by xixionghui on 16/4/18.
 */
public class ADVCardContentManager extends BaseCardContentManager {

    private Context mContext;
//    private AdNativeManager mAdNativeManager;
    private CardInfo mCardInfo;
    private Handler mMainControlHandler;//MainControl的handler;

    /**
     * @param context
     */
    ADVCardContentManager(Context context) {
        super(context);
        this.mContext = context;
//        mAdNativeManager = new AdNativeManager((Activity) mContext, "SDK20161518030339tw90ofxelsoctuo");
    }

    @Override
    public void moreNews() {

    }

    @Override
    public void changeNews() {

    }
    /*
    public List<NativeAdInfo> getNativeAdInfoList() {
        return mNativeAdInfoList;
    }

    public void setNativeAdInfoList(List<NativeAdInfo> nativeAdInfoList) {
        mNativeAdInfoList = nativeAdInfoList;
    }
    */
    @Override
    public void requestCardContent(Handler mainControlHandler, CardInfo cardInfo) {
        log("开始请求adView数据");
        this.mMainControlHandler = mainControlHandler;
        this.mCardInfo = cardInfo;
        /*
        this.mAdNativeManager.setAdNativeInterface(new AdNativeInterface() {
            @Override
            public void onFailedReceivedAd(String s) {
                ADVCardContentManager.this.mNativeAdInfoList = new ArrayList<NativeAdInfo>();
                Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW);
                ADVCardContentManager.this.mMainControlHandler.sendMessage(msg);
//                Log.i("MyInfo", "ADVCardContentManager.获取失败");
            }

            @Override
            public void onReceivedAd(List list) {
                    Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW);
                if (null==list||list.size()==0) {//获取adview成功,但是依旧可能出现为空的情况
                    ADVCardContentManager.this.mMainControlHandler.sendMessage(msg);
                }else {//获取adview成功&&肯定有数据
                    setNativeAdInfoList(list);
                    msg.obj = ADVCardContentManager.this.mNativeAdInfoList;
                    ADVCardContentManager.this.mMainControlHandler.sendMessage(msg);
                }
//                Log.i("MyInfo", "ADVCardContentManager.获取成功");
            }

            @Override
            public void onAdStatusChanged(int i) {

            }
        });
        mAdNativeManager.requestAd();
        */

        Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW);
        ADVCardContentManager.this.mMainControlHandler.sendMessage(msg);
    }
}
