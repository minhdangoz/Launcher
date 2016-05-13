package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.utils.CardUtils;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.ss.android.sdk.minusscreen.SsNewsApi;
import com.ss.android.sdk.minusscreen.control.feed.ArticleListQueryCallBack;
import com.ss.android.sdk.minusscreen.model.Article;

import java.util.List;

/**
 * Created by xixionghui on 16/4/12.
 */
public class TTCardContentManager extends BaseCardContentManager {

    private Handler mMainControlHandler;//MainControl的handler
    private CardInfo mCardInfo;
    private int mChannelId;
    private ConnectivityManager mCM;
    private List<Article>[] mArticleListArrays = new List[2];//会生成固定长度的两个集合List<Article>数组

    public List<Article>[] getArticleListArrays() {
        return mArticleListArrays;
    }

    //    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO:
//                    if (msg.arg1 == AsynchronousGet.SUCCESS) {
//                        mArticleListArrays = (List<Article>[]) msg.obj;
//                        mMainControlHandler.sendMessage(msg);
//                    } else {
//                        onFailToast(msg);
//                    }
//                    break;
//            }
//        }
//    };

    TTCardContentManager(Context context) {
        super(context);
        mCM = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void moreNews() {

    }

    @Override
    public void changeNews() {

    }


    /**
     * 查看如何根据要请求的channel：secondType获取下一组今日头条
     */
    @Override
    public void requestCardContent(final Handler handler,CardInfo cardInfo) {
        this.mCardInfo = cardInfo;
        log("开始获取今日头条信息,频道号="+cardInfo.getCardSecondTypeId());
        //目前今日头条不支持按channel获取新闻.此处可以忽略,为下一个版本准备
        this.mChannelId = CardIdMap.getTouTiaoChannelId(cardInfo.getCardSecondTypeId());
        this.mMainControlHandler = handler;

        //获取数据
        SsNewsApi.queryArticleList(mContext, new ArticleListQueryCallBack() {
            @Override
            public void onArticleListReceived(boolean b, List<Article> list) {

//                if (!b) {
//                    Toast.makeText(mContext, (mContext.getResources().getString(R.string.kinflow_string_obtain_toutiao_fail)), Toast.LENGTH_SHORT).show();
//                    log("获取头条新闻失败");
//                    return;
//                }
                Message msg = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO);
                if (!b) {
                    TTCardContentManager.this.mMainControlHandler.sendMessage(msg);
                }else {
//                    mArticleListArrays = CardUtils.groupByAbstract2(list);
//                    msg.obj = mArticleListArrays;
//                    TTCardContentManager.this.mMainControlHandler.sendMessage(msg);
                    if (isConnected2Net()) {
                        mArticleListArrays = CardUtils.groupByAbstract2(list);
                        msg.obj = mArticleListArrays;
                        TTCardContentManager.this.mMainControlHandler.sendMessage(msg);
                    } else {
                        TTCardContentManager.this.mMainControlHandler.sendMessage(msg);
                    }
                }
            }

        }, 20);
    }

    public boolean isConnected2Net() {
        NetworkInfo networkInfoWifi = mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo networkInfoMobile = mCM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfoMobile.isConnected() || networkInfoWifi.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
