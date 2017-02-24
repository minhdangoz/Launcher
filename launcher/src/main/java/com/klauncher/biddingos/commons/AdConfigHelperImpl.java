package com.klauncher.biddingos.commons;

import android.content.Context;
import android.util.Log;

import com.alpsdroid.ads.commons.AdConfigHelper;
import com.klauncher.biddingos.commons.ads.AdsRequestObject;
import com.klauncher.biddingos.commons.analytics.AppScanner;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.feeds.FeedsAdsPool;
import com.klauncher.biddingos.impl.FlowAdHelplerImpl;

/**
 * Created by：lizw on 2016/1/8 18:04
 */
public class AdConfigHelperImpl implements AdConfigHelper {
    /**
     * 记录初始化类型
     */
    private static int adType;
    private static final String TAG = "AdConfigHelperImpl";

    /**
     * SDK统一初始化方法法
     *
     * @param adType
     * @param context
     * @param mid
     * @param afid
     * @param kid
     * @param secret
     * @param debuggable
     */
    @Override
    public void init(int adType, Context context, String mid, String afid, String secret, String kid, boolean debuggable) {
        //两次状态判断，保证性能和唯一
        if (Setting.isInitDone()) {
            Log.i(TAG, "init has been launch");
            return;
        }
        synchronized (AdConfigHelperImpl.class) {
            if (Setting.isInitDone()) {
                Log.i(TAG, "init has been launch");
                return;
            }
            if (null == context) {
                Log.e(TAG, "BiddingOS sdk init failed, context is null");
                return;
            }

            if (null == mid || "" == mid) {
                Log.e(TAG, "BiddingOS sdk init failed, mid is empty");
                return;
            }
            initSame(context, mid, afid, secret, debuggable);
            //到这里，表示相同初始化已经完成
            this.adType = adType;
            if ((adType & 0x01) == 1) {//ad 投放
                LogUtils.i(TAG, "init ad");
                setSessionId();
                Setting.KID = kid;
            }
            if (((adType >> 1) & 0x01) == 1) {//flow
                LogUtils.i(TAG, "init flow");
                setSessionId();
                Setting.KID = kid;
                FlowAdHelplerImpl flowAdHelplerImpl = new FlowAdHelplerImpl();
                //异步刷新广告池
                flowAdHelplerImpl.refleshAdsPool();
                //加载广告
                flowAdHelplerImpl.loadAds();
            }
            if (((adType >> 3) & 0x01) == 1) {//feeds
                LogUtils.i(TAG, "init feeds");
                setSessionId();
                Setting.KID = kid;
            }
            if (((adType >> 2) & 0x01) == 1) {//banner
                LogUtils.i(TAG, "init banner");
            }
            if (((adType >> 4) & 0x01) == 1) {//Interstitial
                LogUtils.i(TAG, "init Interstitial");
            }
            //设置初始化成功
            Setting.setInitDone();
        }

    }

    private void initSame(Context context, String mid, String afid, String secret, boolean debuggable) {


        if (null == afid || "" == afid) {
            Log.e(TAG, "afid is empty");
            afid = "0";
        }

        if (null == secret || "" == secret || 32 > secret.length()) {
            Log.e(TAG, "secret is wrong");
        }
//        Setting.context = context.getApplicationContext();
        Setting.context = context.getApplicationContext();
        Setting.MID = mid;
        Setting.AFID = afid;
        Setting.SECRET = secret;
        Setting.DEGUGGABLE = debuggable;
        Setting.bWriteLog = debuggable;


    }

    /**
     * v2接口生成sessionId
     */
    private void setSessionId() {
        //生成SessionId
        AdsRequestObject.setSessionid();
        AppScanner.init();
    }


    @Override
    public void destroy() {
        if ((adType & 0x01) == 1) {//ad 投放
        }
        if (((adType >> 1) & 0x01) == 1) {//flow
        }
        if (((adType >> 3) & 0x01) == 1) {//feeds
            FeedsAdsPool.getInstance().removeFeedsAdsPool();
            LogUtils.i(TAG, "feeds destroy done");
        }
        if (((adType >> 2) & 0x01) == 1) {//banner
        }
    }
}
