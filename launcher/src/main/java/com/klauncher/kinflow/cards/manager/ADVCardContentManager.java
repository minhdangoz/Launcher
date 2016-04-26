package com.klauncher.kinflow.cards.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
//    private Handler mHandler;
//    private Handler.Callback advHandlerCallback = new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            mNativeAdInfoList = (List<NativeAdInfo>) msg.obj;
//
//            Message msgAdv = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW);
//            msgAdv.obj = mNativeAdInfoList;
//            mMainControlHandler.sendMessage(msgAdv);
//            return true;
//        }
//    };

    /**
     * @param context
     */
    ADVCardContentManager(Context context) {
        super(context);
        this.mContext = context;
//        this.mHandler = new Handler(advHandlerCallback);
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

    public void setNativeAdInfoList(List<NativeAdInfo> nativeAdInfoList) {
        mNativeAdInfoList = nativeAdInfoList;
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
                Log.i("MyInfo", "ADVCardContentManager.获取失败");
            }

            @Override
            public void onReceivedAd(List list) {
                for (int i = 0; i < list.size(); i++) {
                    NativeAdInfo info = (NativeAdInfo) list.get(i);
                    Log.i("MyInfo", "NativeAdInfo.title="+info.getTitle()
                            +"\n   info.getIconUrl="+info.getIconUrl()
                            +"\n   info.getImageUrl()="+info.getImageUrl()
                            +"\n   info.getDescription="+info.getDescription());
                }
                setNativeAdInfoList(list);
                Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW);
                msg.obj = ADVCardContentManager.this.mNativeAdInfoList;
                ADVCardContentManager.this.mMainControlHandler.sendMessage(msg);
                Log.i("MyInfo", "ADVCardContentManager.获取成功");
            }

            @Override
            public void onAdStatusChanged(int i) {

            }
        });
        mAdNativeManager.requestAd();
    }
}
