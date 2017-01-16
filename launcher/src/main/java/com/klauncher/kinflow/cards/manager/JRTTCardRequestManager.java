package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.task.JRTTAsynchronousTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 16/6/14.
 * 此为今日头条API版本的CardContentManager
 */
public class JRTTCardRequestManager {
    public static final String TAG = "Kinflow";
    public static final int REQUEST_ARTICLE_COUNT = 25;
    private int channelId = CardIdMap.CARD_TYPE_NEWS_TT_REDIAN;
    private Handler mainControlHandler;//MainControl的handler
    private Context mContext;

    private List<JinRiTouTiaoArticle> jinRiTouTiaoArticleList = new ArrayList<>();

    private JrttCallback mJrttCallback;
    public interface  JrttCallback {
        void onSuccess(List<JinRiTouTiaoArticle> jinRiTouTiaoArticleList);
        void onFial(String failFlag);
    }
//    2017-1-12
//    private Handler mRefreshHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN:
//                    try {
//                        if (JRTTAsynchronousTask.SUCCESS==msg.arg1&& null != msg.obj) {//获取token成功:继续发起请求article
//                            Log.e(TAG, "handleMessage: 获取头条token成功");
////                            log("JRTTCardContentManager获取到token后在handler中处理数据时,当前所在线程 = " + Thread.currentThread().getName());
//                            new JRTTAsynchronousTask(mContext, mRefreshHandler,MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE).getJinRiToutiaoArticle(CardIdMap.getTouTiaoCategory(channelId));
//                        } else {//获取token失败:通知MainControl获取失败
//                            Log.e(TAG, "handleMessage: 获取头条token失败");
//                            sendMessage2MainControl(msg);
//                        }
//                    } catch (Exception e) {//发生异常:通知MainControl获取失败
//                        Log.e(TAG, "handleMessage: 处理今日头条token时,发生未知错误:"+e.getMessage());
//                        sendMessage2MainControl(msg);
//                    }
//                    break;
//                case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE:
//                    try {
//                        if (msg.arg1 == JRTTAsynchronousTask.SUCCESS&& null != msg.obj) {//获取article成功
//                            Log.e(TAG, "handleMessage: 获取头条article成功");
//                            jinRiTouTiaoArticleList.clear();
//                            jinRiTouTiaoArticleList = (List<JinRiTouTiaoArticle>) msg.obj;
////                            Log.e(TAG, "获取到的今日头条个数="+mJinRiTouTiaoArticleList.size()+" ,分别为:\n");
////                            for (int j = 0 ; j < mJinRiTouTiaoArticleList.size() ; j++) {
////                                Log.e(TAG, "今日头条toString结果 = "+mJinRiTouTiaoArticleList.get(j).toString());
////                            }
//                        } else {//获取article失败
//                            Log.e(TAG, "handleMessage: 获取头条失败");
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG, "handleMessage: 处理今日头条article时,发生未知错误:"+e.getMessage());
//                    } finally {//无论获取article成功还是失败,都需要告诉MainControl
//                        sendMessage2MainControl(msg);
//                    }
//                    break;
//                default:
//                    sendMessage2MainControl(msg);
//                    break;
//            }
//        }
//    };

    private Handler mRefreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN:
                    try {
                        if (JRTTAsynchronousTask.SUCCESS==msg.arg1&& null != msg.obj) {//获取token成功:继续发起请求article
                            Log.e(TAG, "handleMessage: 获取头条token成功");
//                            log("JRTTCardContentManager获取到token后在handler中处理数据时,当前所在线程 = " + Thread.currentThread().getName());
                            new JRTTAsynchronousTask(mContext, mRefreshHandler,MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE).getJinRiToutiaoArticle(CardIdMap.getTouTiaoCategory(channelId));
                        } else {//获取token失败:通知MainControl获取失败
                            Log.e(TAG, "handleMessage: 获取头条token失败");
                            sendMessage2MainControl(msg);
                        }
                    } catch (Exception e) {//发生异常:通知MainControl获取失败
                        Log.e(TAG, "handleMessage: 处理今日头条token时,发生未知错误:"+e.getMessage());
                        sendMessage2MainControl(msg);
                    }
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE:
                    try {
                        if (msg.arg1 == JRTTAsynchronousTask.SUCCESS&& null != msg.obj) {//获取article成功
                            Log.e(TAG, "handleMessage: 获取头条article成功");
                            jinRiTouTiaoArticleList.clear();
                            jinRiTouTiaoArticleList = (List<JinRiTouTiaoArticle>) msg.obj;
//                            Log.e(TAG, "获取到的今日头条个数="+mJinRiTouTiaoArticleList.size()+" ,分别为:\n");
//                            for (int j = 0 ; j < mJinRiTouTiaoArticleList.size() ; j++) {
//                                Log.e(TAG, "今日头条toString结果 = "+mJinRiTouTiaoArticleList.get(j).toString());
//                            }
                        } else {//获取article失败
                            Log.e(TAG, "handleMessage: 获取头条失败");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "handleMessage: 处理今日头条article时,发生未知错误:"+e.getMessage());
                    } finally {//无论获取article成功还是失败,都需要告诉MainControl
                        sendMessage2MainControl(msg);
                    }
                    break;
                default:
                    sendMessage2MainControl(msg);
                    break;
            }
        }
    };

    /**
     * 发送消息到MainControl
     * @param msg
     */
    private void sendMessage2MainControl (Message msg) {
        try {
            Message msg2Main = Message.obtain();
            msg2Main.arg1 = msg.arg1;
            msg2Main.what = msg.what;
            msg2Main.obj = jinRiTouTiaoArticleList;
            mainControlHandler.sendMessage(msg2Main);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param context
     */
    public JRTTCardRequestManager(Context context,int channelId) {
        this.mContext = context;
        this.channelId = channelId;
    }
    public void moreNews() {//这里应该是一个回调
        Toast.makeText(mContext, "更多今日头条新闻", Toast.LENGTH_SHORT).show();
    }

    public void rightButtom(JrttCallback jrttCallback) {
        try {
            this.mJrttCallback = jrttCallback;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new JRTTAsynchronousTask(mContext, mMoreHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN).accessToken();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void changeNews() {

    }

    public List<JinRiTouTiaoArticle> getJinRiTouTiaoArticleList() {
        return jinRiTouTiaoArticleList;
    }

    public void setJinRiTouTiaoArticleList(List<JinRiTouTiaoArticle> jinRiTouTiaoArticleList) {
//        this.mJinRiTouTiaoArticleList.clear();
//        this.mJinRiTouTiaoArticleList.addAll(mJinRiTouTiaoArticleList);
        this.jinRiTouTiaoArticleList = jinRiTouTiaoArticleList;
    }

    public void requestCardContent(Handler mainControlHandler) {
        try {
            this.mainControlHandler = mainControlHandler;
//        new JRTTAsynchronousTask(mContext, mRefreshHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN).accessToken();
            new Thread(new Runnable() {
                @Override
                public void run() {
    //                new JRTTAsynchronousTask(mContext, mRefreshHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN).accessToken2();
                    new JRTTAsynchronousTask(mContext, mRefreshHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN).accessToken();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler mMoreHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN:
                    try {
                        if (JRTTAsynchronousTask.SUCCESS==msg.arg1&& null != msg.obj) {//获取token成功:继续发起请求article
//                            Log.e(TAG, "mMoreHandler: 获取头条token成功");
//                            log("JRTTCardContentManager获取到token后在handler中处理数据时,当前所在线程 = " + Thread.currentThread().getName());
                            new JRTTAsynchronousTask(mContext, mMoreHandler,MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE).getJinRiToutiaoArticle(CardIdMap.getTouTiaoCategory(channelId));
                        } else {//获取token失败:通知MainControl获取失败
//                            Log.e(TAG, "mMoreHandler: 获取头条token失败");
                            if (null!=mJrttCallback) mJrttCallback.onFial("服务器繁忙,或者网络连接错误");
                        }
                    } catch (Exception e) {//发生异常:通知MainControl获取失败
                        Log.e(TAG, "mMoreHandler: 处理今日头条token时,发生未知错误:" + e.getMessage());
                        if (null!=mJrttCallback) mJrttCallback.onFial("服务器繁忙,或者网络连接错误");
                    }
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE:
                    try {
                        if (msg.arg1 == JRTTAsynchronousTask.SUCCESS&& null != msg.obj) {//获取article成功
                            Log.e(TAG, "mMoreHandler: 获取头条article成功");
                            jinRiTouTiaoArticleList.clear();
                            jinRiTouTiaoArticleList = (List<JinRiTouTiaoArticle>) msg.obj;
                            mJrttCallback.onSuccess(jinRiTouTiaoArticleList);
                        } else {//获取article失败
                            Log.e(TAG, "mMoreHandler: 获取头条失败失败");
                            if (null!=mJrttCallback) mJrttCallback.onFial("服务器繁忙,或者网络连接错误");
                        }
                    } catch (Exception e) {
//                        Log.e(TAG, "handleMessage: 处理今日头条article时,发生未知错误:"+e.getMessage());
                        if (null!=mJrttCallback) mJrttCallback.onFial("服务器繁忙,或者网络连接错误");
                    }
//                    finally {//无论获取article成功还是失败,都需要告诉MainControl
//                        sendMessage2MainControl(msg);
//                    }
                    break;
                default:
//                    sendMessage2MainControl(msg);
                    if (null!=mJrttCallback) mJrttCallback.onFial("服务器繁忙,或者网络连接错误");
                    break;
            }
        }
    };
}
