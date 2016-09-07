package com.klauncher.kinflow;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.task.JRTTAsynchronousTask;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.model.GlobalCategory;
import com.klauncher.kinflow.navigation.adapter.NavigationAdapter2;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWordItemDecoration;
import com.klauncher.kinflow.views.adapter.GlobalCategoryAdapter;
import com.klauncher.kinflow.views.adapter.Kinflow2NewsAdapter;
import com.klauncher.kinflow.views.recyclerView.itemDecoration.ItemDecorationDivider;
import com.klauncher.kinflow.views.recyclerView.scrollListener.HidingScrollListener;
import com.klauncher.kinflow.views.ucview.UCIndexView;
import com.klauncher.launcher.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kinflow2Activity2 extends Activity implements View.OnClickListener{

    UCIndexView mUCIndexView;

    RecyclerView mGlobalCategoryRecyclerView, mNavigationRecyclerView, mNewsBodyRecyclerView;
    LinearLayout mLinearLayoutHeader;
    RelativeLayout searchLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uc_index_view);
        mUCIndexView = (UCIndexView) findViewById(R.id.ucindexview);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestJinRiTouTiao();
    }

    Kinflow2NewsAdapter kinflow2NewsAdapter;
    private void initView() {
        mLinearLayoutHeader = (LinearLayout) findViewById(R.id.kinflow_scrolling_header);
        //总体分类
        mGlobalCategoryRecyclerView = (RecyclerView) findViewById(R.id.global_category_recyclerView);
        mGlobalCategoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false));
        mGlobalCategoryRecyclerView.setAdapter(new GlobalCategoryAdapter(this, GlobalCategory.getDefaultGlobalCategoryList()));
        //快捷导航
        mNavigationRecyclerView = (RecyclerView) findViewById(R.id.navigation_recyclerView);
        mNavigationRecyclerView.setLayoutManager(new GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false));
        List<Navigation> navigationList = CacheNavigation.getInstance().getAll();
        Collections.sort(navigationList);
        mNavigationRecyclerView.setAdapter(new NavigationAdapter2(Kinflow2Activity2.this, navigationList));
        mNavigationRecyclerView.addItemDecoration(new HotWordItemDecoration(16, 32, false));
        //新闻体部分
        mNewsBodyRecyclerView = (RecyclerView) findViewById(R.id.kinflow_body);
        mNewsBodyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNewsBodyRecyclerView.addItemDecoration(new ItemDecorationDivider(this, LinearLayoutManager.VERTICAL));
//        mNewsBodyRecyclerView.addOnScrollListener(mHidingScrollListener);
//        mNewsBodyRecyclerView.addOnScrollListener(new HidingScrollListener() {
//            @Override
//            public void onScrolledUp() {
//                super.onScrolledUp();
//            }
//
//            @Override
//            public void onScrolledDown() {
//                super.onScrolledDown();
//            }
//
//            @Override
//            public void onScrolledToTop() {
//                super.onScrolledToTop();
//                Toast.makeText(Kinflow2Activity2.this, "到达顶部", Toast.LENGTH_SHORT).show();
//                mUCIndexView.showPageNavigationView();
//            }
//
//            @Override
//            public void onScrolledToBottom() {
//                super.onScrolledToBottom();
//                Toast.makeText(Kinflow2Activity2.this, "到达底部", Toast.LENGTH_SHORT).show();
//            }
//        });


        mNewsBodyRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {

            }

            @Override
            public void onShow() {

            }
        });

        kinflow2NewsAdapter = new Kinflow2NewsAdapter(this,null);
        mNewsBodyRecyclerView.setAdapter(kinflow2NewsAdapter);
        //other
        searchLayout = (RelativeLayout) mLinearLayoutHeader.findViewById(R.id.search_layout);
        searchLayout.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        mUCIndexView.onBackRestore();
    }

    //=====================================================================================
    private List<JinRiTouTiaoArticle> jinRiTouTiaoArticleList = new ArrayList<>();
    public static final int REQUEST_ARTICLE_COUNT = 25;

    private Handler mRefreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN:
                    try {
                        if (JRTTAsynchronousTask.SUCCESS==msg.arg1&& null != msg.obj) {//获取token成功:继续发起请求article
//                            Log.e(TAG, "handleMessage: 获取头条token成功");
//                            log("JRTTCardContentManager获取到token后在handler中处理数据时,当前所在线程 = " + Thread.currentThread().getName());
//                            new JRTTAsynchronousTask(Kinflow2Activity.this, mRefreshHandler,MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE).getJinRiToutiaoArticle(CardIdMap.getTouTiaoCategory(mCardInfo.getCardSecondTypeId()));
                            new JRTTAsynchronousTask(Kinflow2Activity2.this, mRefreshHandler,MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE).getJinRiToutiaoArticle(CardIdMap.getTouTiaoCategory(CardIdMap.CARD_TYPE_NEWS_TT_REDIAN));
                        } else {//获取token失败:通知MainControl获取失败
//                            Log.e(TAG, "handleMessage: 获取头条token失败");
//                            sendMessage2MainControl(msg);
                            Toast.makeText(Kinflow2Activity2.this, "获取头条token失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {//发生异常:通知MainControl获取失败
//                        Log.e(TAG, "handleMessage: 处理今日头条token时,发生未知错误:" + e.getMessage());
//                        sendMessage2MainControl(msg);
                        Toast.makeText(Kinflow2Activity2.this, "获取头条tokens时,发生未知异常", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE:
                    try {
                        if (msg.arg1 == JRTTAsynchronousTask.SUCCESS&& null != msg.obj) {//获取article成功
//                            Log.e(TAG, "handleMessage: 获取头条article成功");
                            jinRiTouTiaoArticleList.clear();
                            jinRiTouTiaoArticleList = (List<JinRiTouTiaoArticle>) msg.obj;
//                            Log.e(TAG, "获取到的今日头条个数="+jinRiTouTiaoArticleList.size()+" ,分别为:\n");
//                            for (int j = 0 ; j < jinRiTouTiaoArticleList.size() ; j++) {
//                                Log.e(TAG, "今日头条toString结果 = "+jinRiTouTiaoArticleList.get(j).toString());
//                            }
                            Toast.makeText(Kinflow2Activity2.this, "获取今日头条内容成功,当前所在线程:"+Thread.currentThread().getName(), Toast.LENGTH_SHORT).show();
//                            kinflow2NewsAdapter.updateAdapter(jinRiTouTiaoArticleList);
                            mNewsBodyRecyclerView.setAdapter(new Kinflow2NewsAdapter(Kinflow2Activity2.this,jinRiTouTiaoArticleList));
                        } else {//获取article失败
//                            Log.e(TAG, "handleMessage: 获取头条失败");
                            Toast.makeText(Kinflow2Activity2.this, "获取头条article失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
//                        Log.e(TAG, "handleMessage: 处理今日头条article时,发生未知错误:"+e.getMessage());
                        Toast.makeText(Kinflow2Activity2.this, "获取头条article时,发生未知异常", Toast.LENGTH_SHORT).show();
                    } finally {//无论获取article成功还是失败,都需要告诉MainControl
//                        sendMessage2MainControl(msg);
                    }
                    break;
                default:
//                    sendMessage2MainControl(msg);
                    break;
            }
        }
    };


    public void requestJinRiTouTiao() {
        Toast.makeText(this, "开始异步请求今日头条数据", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //                new JRTTAsynchronousTask(mContext, mRefreshHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN).accessToken2();
                new JRTTAsynchronousTask(Kinflow2Activity2.this, mRefreshHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN).accessToken();
            }
        }).start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_layout:
                Toast.makeText(this, "搜索框被点击", Toast.LENGTH_SHORT).show();
                requestJinRiTouTiao();
                break;
        }
    }

    private static final String TAG = "MyInfo";

}
