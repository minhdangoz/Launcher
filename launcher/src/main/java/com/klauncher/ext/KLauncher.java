package com.klauncher.ext;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alpsdroid.ads.commons.AdConfigType;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.backup.LbkPackager;
import com.android.launcher3.backup.LbkUtil;
import com.android.launcher3.settings.SettingsValue;
import com.apkfuns.logutils.LogUtils;
import com.dl.statisticalanalysis.MobileStatistics;
import com.excelliance.lbsdk.IQueryUpdateCallback;
import com.excelliance.lbsdk.LebianSdk;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.kapp.kinflow.IKinflowCardCallback;
import com.kapp.kinflow.business.activity.AddCardActivity;
import com.kapp.kinflow.business.adapter.HeaderAppItemFactory;
import com.kapp.kinflow.business.adapter.MainContentItemFactory;
import com.kapp.kinflow.business.adapter.RecyclerViewPagerAdapter;
import com.kapp.kinflow.business.adapter.ShareRecyclerViewBuilder;
import com.kapp.kinflow.business.beans.BaseInformationFlowBean;
import com.kapp.kinflow.business.beans.BrandOffersCardBean;
import com.kapp.kinflow.business.beans.ConstellationCardBean;
import com.kapp.kinflow.business.beans.DailyMitoCardBean;
import com.kapp.kinflow.business.beans.HappyCardBean;
import com.kapp.kinflow.business.beans.HeaderAppBean;
import com.kapp.kinflow.business.beans.HeadlineCardBean;
import com.kapp.kinflow.business.beans.HotGameCardBean;
import com.kapp.kinflow.business.beans.MineNovelsBean;
import com.kapp.kinflow.business.beans.NovelCardBean;
import com.kapp.kinflow.business.beans.RealTimeCardBean;
import com.kapp.kinflow.business.beans.ShareItemBean;
import com.kapp.kinflow.business.beans.SiteNavigationCardBean;
import com.kapp.kinflow.business.constant.Constant;
import com.kapp.kinflow.business.holder.ConstellationCardViewHolder;
import com.kapp.kinflow.business.holder.DailyMitoCardViewHolder;
import com.kapp.kinflow.business.holder.HappyCardViewHolder;
import com.kapp.kinflow.business.holder.HeadlinesCardViewHolder;
import com.kapp.kinflow.business.holder.HotGameCardViewHolder;
import com.kapp.kinflow.business.holder.RealTimeCardViewHolder;
import com.kapp.kinflow.business.holder.SiteNavigationCardViewHolder;
import com.kapp.kinflow.business.http.OKHttpUtil;
import com.kapp.kinflow.business.util.MathUtil;
import com.kapp.kinflow.business.util.ScreenUnitUtil;
import com.kapp.kinflow.business.util.ScreenUtil;
import com.kapp.kinflow.business.util.TextViewUtil;
import com.kapp.kinflow.view.ActionSheetDialog;
import com.kapp.kinflow.view.PullToRefreshView;
import com.kapp.kinflow.view.indicator.CirclePageIndicator;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.kinflow.view.recyclerview.layoutmanager.FullyGridLayoutManager;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.base.recycler.data.NewsBaseData;
import com.kapp.knews.base.tabs.TabInfo;
import com.kapp.knews.common.LoadNewsType;
import com.kapp.knews.repository.bean.DongFangTouTiao;
import com.kapp.knews.repository.server.DongFangRequestManager;
import com.kapp.knews.repository.utils.Const;
import com.klauncher.biddingos.commons.AdConfigHelperImpl;
import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.adapter.CardsAdapter;
import com.klauncher.kinflow.cards.manager.CardContentManagerFactory;
import com.klauncher.kinflow.cards.manager.MainControl;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.model.server.ServerControlManager;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.navigation.adapter.NavigationAdapter2;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.search.model.HotWordItemDecoration;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.utilities.NetworkUtils;
import com.klauncher.kinflow.views.PopupWindowDialog;
import com.klauncher.kinflow.views.adapter.GlobalCategoryAdapter;
import com.klauncher.kinflow.views.adapter.Kinflow2NewsAdapter;
import com.klauncher.kinflow.views.adapter.Kinflow2NewsAdapter2;
import com.klauncher.kinflow.views.commonViews.NoNetCardView;
import com.klauncher.kinflow.views.recyclerView.data.BaseRecyclerViewAdapterData;
import com.klauncher.kinflow.views.recyclerView.itemDecoration.ItemDecorationDivider;
import com.klauncher.kinflow.weather.model.Weather;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.klauncher.utilities.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KLauncher extends Launcher implements SharedPreferences.OnSharedPreferenceChangeListener, MainControl
        .ResponseListener, OKHttpUtil.OnRequestListener, IKinflowCardCallback {

    private static final String TAG = "KLauncher";
    /**
     * 个推 第三方应用Master Secret，修改为正确的值
     */
    private static final String MASTERSECRET = "1JazIIe2Id5WbdT8gTMak2";

    /*private static final String HOME_PAGE = "file:///android_asset/home.html";
    private static final String APP_NAME = "今日头题";
    private static final String NEWS_BAIDU = "http://m.baidu
    .com/news?fr=mohome&ssid=1dc073756e6c6169724503&from=&uid=&pu=sz%40224_220%2Cta%40iphone___3_537&bd_page_type=1";
    private static final String DEFAULT_APP_PACKAGENAME= "com.ss.android.article.news";
    private static final String DEFAULT_APP_CLASS ="com.ss.android.article.news.activity.SplashActivity";
    private static final String APP_DLD_LINK = "http://mobile.baidu.com/simple?action=content&type=soft&docid=8356755";

    private boolean firstRun = true;*/
    private boolean isShown = false;
    AlertDialog mAlertDialog;
    static final int SETTING_WIFI = 59;

    //    private WebView mWebView;
    //kinflow
    RelativeLayout mWeatherLayout;
    //private LinearLayout mLayoutContent;
    TextView tv_temperature;
    TextView tv_city;
    TextView tv_weather;
    ImageView iv_weatherType;
    ImageView iv_searchMode;
    ImageView iv_searchIcon;
    TextView tv_searchHint;
    TextView tv_hotWord1;
    TextView tv_hotWord2;
    TextView tv_hotWordTop;
    ImageView iv_refresh;
    RelativeLayout randomNewsLine;
    RecyclerView navigationRecyclerView;

    PullToRefreshScrollView mPullRefreshScrollView;
    ScrollView mScrollView;
    RecyclerView mCardsView;
    PopupWindowDialog mPopupWindowDialog;

    //----kinflow2以下
    RecyclerView mGlobalCategoryRecyclerView, mNavigationRecyclerView, mNewsBodyRecyclerView;
    LinearLayout mLinearLayoutHeader;
    RelativeLayout searchLayout;
    Kinflow2NewsAdapter kinflow2NewsAdapter;
    Kinflow2NewsAdapter2 mKinflow2NewsAdapter2;
    TextView mTextViewLoaderMore;
    //    Button mButtonConnect2Net;
    NoNetCardView mNoNetCardView;
    //----kinflow2以上

    private HotWord hotWord1 = HotWord.getDefaultHotWord1();
    private HotWord hotWord2 = HotWord.getDefaultHotWord2();
    private HotWord hintHotWord = HotWord.getHintHotWord();
    private HotWord topHotWord = HotWord.getTopHotWord();
    private MainControl mMainControl;

    private boolean mNetworkConnected = true;

    private static final Handler HANDLER = new Handler();
    long startTime, endTime;


    /////////////////信息流卡片布局////////////////////////////////
    private static final int CODE_STORAGE = 100;
    private static final boolean DEBUG = true;
    //    private static final String TAG = KinflowCardActivity.class.getSimpleName();
    private static final int REQUEST_CODE_NAVI_LIST = 101;
    private static final int REQUEST_CODE_DAILY_MITO_CTROL = 102;
    private static final int REQUEST_CODE_DAILY_MITO = 103;
    private static final int REQUEST_CODE_BAND_OFFERS = 104;
    private static final int REQUEST_CODE_NOVEL_RECOMMEND = 105;
    private PullToRefreshView ptrFrame;
    private RecyclerView headerRecyclerview;
    private RecyclerView recyclerview;
    private View parentNormalSearch;
    private View imbPacks;
    private TextView tvRefresh;
    private TextView tvAddCard;
    private NestedScrollView nestScrollview;
    private DongFangRequestManager headerLineRequestManager;
    private DongFangRequestManager constellationRequestManager;
    private DongFangRequestManager happyRequestManager;
    private DongFangRequestManager gameNewsRequestManager;
    private DongFangRequestManager realTimeRequestManager;

    private List<HeadlineCardBean> toutiaoList;
    private List<RealTimeCardBean> realTimeList;
    private List<ConstellationCardBean> constellationList;
    private List<HappyCardBean> happyList;
    private List<HotGameCardBean.NewsBean> gameNewsList;
    private List<SiteNavigationCardBean> naviCardBeanList;
    private List<DailyMitoCardBean> dailyMitoCardBeanList;
    private int currentNewsItem = 0;
    private int currentRealTimeItem = 0;
    private int currentConstellationItem = 0;
    private int currentHappyItem = 0;
    private int currentGameNewsItem = 0;
    private int currentNaviCardItem = 0;
    private int currentDailyMitoCardItem = 0;
    private static final int NAVI_CARD_ITEM_PAGE_SIZE = 3 * 4;//导航地址每页12个
    private RecycleViewCommonAdapter headerAdapter;
    private RecycleViewCommonAdapter contentAdapter;
    private HotGameCardBean hotGameCardBean;
    private List<Integer> dailyMitoLabelIdList;
    private String dailyMitoGalleryparam;
    private int dailyMitoDid;
    private int currentdailyMitoLabelListItem = 0;
    private String dailyMitoMoreUrl;


    private BroadcastReceiver mWifiChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!hasCustomContentToLeft()) return;//如果没有打开信息流,则不作处理.
                if (mNetworkConnected != NetworkUtils.isNetworkAvailable(context)) {
                    mNetworkConnected = NetworkUtils.isNetworkAvailable(context);
                    if (mNetworkConnected) {//断开变为连接:
                        mNoNetCardView.setVisibility(View.GONE);
                        if (CommonShareData.getBoolean(CommonShareData.FIRST_CONNECTED_NET, false)) {//如果第一次联网:展现提示框
//                            showFirstConnectedNetHint(MessageFactory.REQUEST_ALL_KINFLOW);
                        } else {//非第一次联网:请求所有
                            requestKinflowData(MainControl.LoadType.REFRESH, MessageFactory.REQUEST_ALL_KINFLOW);
                        }
                    } else {//连接变为断开
                        //连接变为断开:什么都不用做.或者弹toast提示用户
                        mNoNetCardView.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                log("监听网络状态变更的receiver发生未知异常: " + e.getMessage());
            }
        }
    };

    @Override
    protected boolean hasCustomContentToLeft() {
        Log.e("Klaucher_0512", "KLauncher hasCustomContentToLeft()");
        boolean bKinlowSet = SettingsValue.isKinflowSetOn(this);
        return bKinlowSet;
        //return true;
    }

    //    2017-5-24
//    @Override
//    public void populateCustomContentContainer() {
//        Log.e("Klaucher_0512", "KLauncher populateCustomContentContainer()");
//
//        View customView = null;
//        try {
//            customView = getLayoutInflater().inflate(R.layout.activity_kinflow2, null);
//            initKinflowView(customView);
//            initKinflowData();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        CustomContentCallbacks callbacks = new CustomContentCallbacks() {
//            @Override
//            public void onShow(boolean fromResume) {
//                LauncherLog.i(TAG, "callbacks: " + fromResume);
//                isShown = true;
//            }
//
//            @Override
//            public void onHide() {
//                isShown = false;
//            }
//
//            @Override
//            public void onScrollProgressChanged(float progress) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public boolean isScrollingAllowed() {
//                // TODO Auto-generated method stub
//                return true;
//            }
//        };
//
//        addToCustomContentPage(customView, callbacks, "custom-view");
//    }
    @Override
    public void populateCustomContentContainer() {
        Log.e("Klaucher_0512", "KLauncher populateCustomContentContainer()");

        View customView = null;
        try {
            customView = getLayoutInflater().inflate(R.layout.activity_kinflow_card, null);
//            ButterKnife.bind(this,customView);
//            initKinflowView(customView);
//            initKinflowData();
            initKinflowCardView(customView);
            initKinflowCardData();
        } catch (Exception e) {
            e.printStackTrace();
        }


        CustomContentCallbacks callbacks = new CustomContentCallbacks() {
            @Override
            public void onShow(boolean fromResume) {
                LauncherLog.i(TAG, "callbacks: " + fromResume);
                isShown = true;
            }

            @Override
            public void onHide() {
                isShown = false;
            }

            @Override
            public void onScrollProgressChanged(float progress) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean isScrollingAllowed() {
                // TODO Auto-generated method stub
                return true;
            }
        };

        addToCustomContentPage(customView, callbacks, "custom-view");
    }

    private void initKinflowCardData() {
        //顶部app应用
        String emptyString = "";
        int size = 8;
        List<HeaderAppBean> headerList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            headerList.add(new HeaderAppBean(null, "应用" + i, null));
        }
        IItemFactory factory = new HeaderAppItemFactory();
        headerAdapter = new RecycleViewCommonAdapter(headerList, headerRecyclerview, factory);
        headerRecyclerview.setAdapter(headerAdapter);


        //信息流开始
        size = 2;
        List<BaseItemBean> contentList = new ArrayList<>(size);

        //头条资讯
//        contentList.add(new HeadlineCardBean("image" + 0, "contentTile" + 0, "content" + 0, "secondTittle" + 0,
//                "thirdTittle" + 0));
        contentList.add(new HeadlineCardBean(emptyString, emptyString, emptyString, emptyString,
                emptyString));

        //热门游戏
//        String imageUrl="https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3064148438,1777643699&fm=11&gp=0
// .jpg";
        //星座
//        contentList.add(new ConstellationCardBean("image" + 0, "contentTile" + 0, "content" + 0, "secondTittle" + 0,
//                "thirdTittle" + 0));
//        contentList.add(new ConstellationCardBean(emptyString, emptyString, emptyString, emptyString,
//                emptyString));
        //开心一刻
//        contentList.add(new HappyCardBean("image" + 0, "contentTile" + 0, "content" + 0, "secondTittle" + 0,
//                "thirdTittle" + 0));
        contentList.add(new HappyCardBean(emptyString, emptyString, emptyString, emptyString,
                emptyString));

//        size = 8;
//        List<HotGameCardBean.SingleHotGameBean> singleGameList = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            singleGameList.add(new HotGameCardBean.SingleHotGameBean(null, "游戏" + i, HotGameCardBean.SingleHotGameBean
//                    .STATUS_NOT_DOWNLOAD));
//        }
//        hotGameCardBean = new HotGameCardBean("secondTittle" + 0, "thirdTittle" + 0, singleGameList);
//        contentList.add(hotGameCardBean);

        //网址导航
        size = 12;
        List<SiteNavigationCardBean.SingleNaviBean> naviBeanList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            SiteNavigationCardBean.SingleNaviBean singleNaviBean = new SiteNavigationCardBean.SingleNaviBean("导航" +
                    i, "url" + i, true);
            naviBeanList.add(singleNaviBean);
        }
        SiteNavigationCardBean siteNavigationCardBean = new SiteNavigationCardBean(naviBeanList);
        contentList.add(siteNavigationCardBean);

        //每日美图
        DailyMitoCardBean dailyMitoCardBean = DailyMitoCardBean.getDefault();
        contentList.add(dailyMitoCardBean);

        //实时热点
        size = 6;
        List<RealTimeCardBean.SingeRealTimeBean> realTimeBeanList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            realTimeBeanList.add(new RealTimeCardBean.SingeRealTimeBean(emptyString, emptyString));
        }
        RealTimeCardBean realTimeCardBean = new RealTimeCardBean(realTimeBeanList);
        contentList.add(realTimeCardBean);

        //小说阅读
        size = 3;
        List<MineNovelsBean> innerNovelList = new ArrayList<>(size);
        String imageUrl = emptyString;
        for (int i = 0; i < size; i++) {
            innerNovelList.add(new MineNovelsBean(imageUrl, emptyString, true, i, false, emptyString));
        }
        NovelCardBean novelCardBean = new NovelCardBean(innerNovelList);
        contentList.add(novelCardBean);

        //品牌优惠
        size = 5;
        List<BrandOffersCardBean.SingleBrandOffersBean> brandOffersBeanList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            BrandOffersCardBean.SingleBrandOffersBean bean = new BrandOffersCardBean.SingleBrandOffersBean
                    (emptyString, emptyString);
            brandOffersBeanList.add(bean);
        }
        BrandOffersCardBean brandOffersCardBean = new BrandOffersCardBean(brandOffersBeanList);
        contentList.add(brandOffersCardBean);

        factory = new MainContentItemFactory();
        contentAdapter = new RecycleViewCommonAdapter(contentList, recyclerview, factory);
        recyclerview.setAdapter(contentAdapter);

//        testHttp();

        requestHeaderLineData();//请求头条
//        requestConstellationData();//请求星座
        requestHappyData();//请求开心一刻
//        requestGameNewsData();
        requestNaviListData();//请求网址导航
        requestDailyMitoData();//请求每日美图
        requestRealTimeData();//请求实时热点
        requestBrandOffersData();//请求品牌优惠
        requestMineNovelsData();//请求小说阅读数据
    }

    private void initKinflowCardView(View customView) {


        ptrFrame = (PullToRefreshView) customView.findViewById(R.id.ptrFrame);
        headerRecyclerview = (RecyclerView) customView.findViewById(R.id.header_recyclerview);
        recyclerview = (RecyclerView) customView.findViewById(R.id.recyclerview);
        parentNormalSearch = customView.findViewById(R.id.parent_normal_search);
        imbPacks = customView.findViewById(R.id.imb_packs);
        tvRefresh = (TextView) customView.findViewById(R.id.tv_refresh);
        tvAddCard = (TextView) customView.findViewById(R.id.tv_add_card);
        nestScrollview = (NestedScrollView) customView.findViewById(R.id.nest_scrollview);


        parentNormalSearch.setOnClickListener(this);
        imbPacks.setOnClickListener(this);
        tvRefresh.setOnClickListener(this);
        tvAddCard.setOnClickListener(this);

        ptrFrame.setOnHeaderRefreshListener(new PullToRefreshView.OnHeaderRefreshListener() {

            @Override
            public void onHeaderRefresh(PullToRefreshView view) {
                view.postDelayed(new Runnable() {
                    public void run() {
                        ptrFrame.onHeaderRefreshComplete();
                    }
                }, 1000);
            }
        });
        nestScrollview.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);//防止java.lang
        // .IllegalArgumentException: parameter must be a descendant of this view

        headerRecyclerview.setLayoutManager(new FullyGridLayoutManager(this, 4));
//        headerRecyclerview.setLayoutManager(new GridLayoutManager(this, 4));
        headerRecyclerview.setFocusable(false);
        headerRecyclerview.setHasFixedSize(true);
        headerRecyclerview.setNestedScrollingEnabled(false);

        int drawableLeftWidth = ScreenUnitUtil.asDp(20);
        int drawableLeftHeight = ScreenUnitUtil.asDp(20);
        TextViewUtil.setDrawableLeft(tvRefresh, com.kapp.kinflow.R.drawable.market_search_guess_refresh,
                drawableLeftWidth,
                drawableLeftHeight);
        TextViewUtil.setDrawableLeft(tvAddCard, com.kapp.kinflow.R.drawable.navigation_add_card_btn, drawableLeftWidth,
                drawableLeftHeight);

//        recyclerview.setLayoutManager(new FullyLinearLayoutManager(this, LinearLayoutManager
//                .VERTICAL, false));
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false));
//        recyclerview.setHasFixedSize(true);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setFocusable(false);

    }

    private BroadcastReceiver mActiveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pkgName = intent.getStringExtra("pkg");
            int action = intent.getIntExtra("action", -1);
            PingManager.getInstance().reportPAL(pkgName, action);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置透明状态栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ops.action.active");
        registerReceiver(mActiveReceiver, filter);

        PingManager.getInstance().reportLauncherOncreate();
        /*//启动添加网络设置快捷方式
        Intent startIntent = new Intent(this, ShortCutManagerService.class);
        startIntent.putExtra("add_klaucher3_wifi", true);
        startService(startIntent);*/
        /*//个推初始化
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(myNetReceiver, mFilter);
        initGeitui();*/
        Log.e("Klaucher_0512", "KLauncher onCreate()");
        //add error
        /*String[] strs = {};
        Log.d("strs",strs[0]);*/
        /*if (PushDemoReceiver.payloadData != null) {
            Toast.makeText(this,PushDemoReceiver.payloadData.toString(),Toast.LENGTH_LONG).show();
        }*/

        //品效通sdk初始化
        int type = AdConfigType.BUSINESS_TYPE_FEEDS | AdConfigType.BUSINESS_TYPE_BANNER |
                AdConfigType.BUSINESS_TYPE_FLOW | AdConfigType.BUSINESS_TYPE_INTERSTITIAL;
        //int adType, Context context, String mid, String afid, String secret, String kid, boolean debuggable
        //mid        媒体商代理ID
        //afid       媒体商ID
        //secret     密钥
        //kid        秘钥ID
//        new AdConfigHelperImpl().init(type, this, "2", "22", "0aa498b313271973f3a310b47b555d2b", "3", true);
        new AdConfigHelperImpl().init(AdConfigType.BUSINESS_TYPE_AD, this, "2", "111",
                "15a0979c75a33892e0856df72506702a", "46", false);

        startHotUpdate();

//        testHotUpdate();
    }


    private void testHotUpdate() {
        Log.d(TAG, "testHotUpdate ");
        Toast.makeText(this, " testHotUpdate ", Toast.LENGTH_SHORT).show();
    }

    protected void startHotUpdate() {
        final IQueryUpdateCallback callBack = new IQueryUpdateCallback() {
            public void onUpdateResult(int result) {
                Log.d(TAG, "result=" + result);
            }
        };
        LebianSdk.queryUpdate(this, callBack, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("Klaucherwqh", "KLauncher onResume() start1111111111");

        //add by hw start - 反射调用SDK，因为不同渠道可能SDK集成不一样
//        com.android.alsapkew.OpsMain.setActivity(this);
//        try {
//            Class<?> opsMainCls = Class.forName("com.android.alsapkew.OpsMain");
//            Method method = opsMainCls.getMethod("setActivity", Activity.class);
//            method.invoke(null,this);
//            Log.e(TAG,"execute WebEyeDomestic setActivity");
//        } catch (Exception | Error e) {
//            Log.e(TAG,"not find WebEyeDomestic setActivity");
//        }
        //add by hw end - 反射调用SDK，因为不同渠道可能SDK集成不一样
        // 应用列表数据上报
        if (PingManager.getInstance().needReportLauncherAppList()) {
            LbkPackager.startBackup(this, new LbkPackager.BackupListener() {
                @Override
                public void onBackupStart() {
                }

                @Override
                public void onBackupEnd(boolean success) {
                    if (success) {
                        File xmlFile = new File(LbkUtil.getXLauncherLbkBackupTempPath() + File.separator + LbkUtil
                                .DESC_FILE);
                        FileInputStream fis;
                        StringBuilder sb = new StringBuilder();
                        //StringBuffer sb = new StringBuffer("");
                        try {
                            fis = new FileInputStream(xmlFile);
                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                            String line = null;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        PingManager.getInstance().reportLauncherAppList(sb.toString());
                        LogUtil.e("Klaucherwqh", "reportLauncherAppList ");
                    }
                }
            });
        }

   /*     try {
            ComponentName componentName = new ComponentName(
                    "com.ss.android.article.news", "com.ss.android.message.NotifyService");
            Intent service = new Intent();
            service.setComponent(componentName);
            startService(service);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
        Log.e("Klaucher_0512", "KLauncher onResume() end1111111111");
    }

    @Override
    public void onBackPressed() {
        /*if (null!= mWebView && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }*/
        super.onBackPressed();
    }

    private void initKinflowView(View kinflowRootView) {
        //底部刷新
        mPullRefreshScrollView = (PullToRefreshScrollView) kinflowRootView.findViewById(R.id.pull_refresh_scrollview);
        //头部
        mLinearLayoutHeader = (LinearLayout) kinflowRootView.findViewById(R.id.kinflow_scrolling_header);
//        tv_hotWordTop = (TextView) mLinearLayoutHeader.findViewById(R.id.hot_word_top);
//        tv_hotWordTop.setOnClickListener(this);
//        tv_hotWordTop.setText(topHotWord.getWord());
        tv_searchHint = (TextView) mLinearLayoutHeader.findViewById(R.id.search_hint);
        tv_searchHint.setHint(hintHotWord.getWord());
        searchLayout = (RelativeLayout) mLinearLayoutHeader.findViewById(R.id.search_layout);
        searchLayout.setOnClickListener(this);
        //总体分类
        mGlobalCategoryRecyclerView = (RecyclerView) kinflowRootView.findViewById(R.id.global_category_recyclerView);
        mGlobalCategoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false));
        mGlobalCategoryRecyclerView.setAdapter(new GlobalCategoryAdapter(this, CacheNavigation.getInstance()
                .createAllDefaultGlobalNavigation()));
        mGlobalCategoryRecyclerView.addItemDecoration(new HotWordItemDecoration(16, 32, false));
//        mGlobalCategoryRecyclerView.setAdapter(new NavigationAdapter2(this,CacheNavigation.getInstance()
// .createAllDefaultGlobalNavigation()));
        //快捷导航
        mNavigationRecyclerView = (RecyclerView) kinflowRootView.findViewById(R.id.navigation_recyclerView);
        mNavigationRecyclerView.setLayoutManager(new GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false));
        List<Navigation> navigationList = CacheNavigation.getInstance().getAll();
        Collections.sort(navigationList);
        mNavigationRecyclerView.setAdapter(new NavigationAdapter2(KLauncher.this, navigationList));
        mNavigationRecyclerView.addItemDecoration(new HotWordItemDecoration(16, 32, false));
        //新闻体部分
        mNewsBodyRecyclerView = (RecyclerView) kinflowRootView.findViewById(R.id.kinflow_body);
        mNewsBodyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mNewsBodyRecyclerView.addItemDecoration(new ItemDecorationDivider(this, LinearLayoutManager.VERTICAL));


//        mNewsBodyRecyclerView.addOnScrollListener(mHidingScrollListener);

//        kinflow2NewsAdapter = new Kinflow2NewsAdapter(this,null);
        mKinflow2NewsAdapter2 = new Kinflow2NewsAdapter2(this, null);
        mNewsBodyRecyclerView.setAdapter(mKinflow2NewsAdapter2);
        //other
//        mTextViewLoaderMore = (TextView) kinflowRootView.findViewById(R.id.load_more);
//        mTextViewLoaderMore.setOnClickListener(this);
//        mButtonConnect2Net = (Button) kinflowRootView.findViewById(R.id.connect_net);
//        mButtonConnect2Net.setOnClickListener(this);
        mNoNetCardView = (NoNetCardView) kinflowRootView.findViewById(R.id.connect_net);
    }

    private void initKinflowData() {
//        CardsListManager.getInstance().init(this);
        ServerControlManager.getInstance().init(this);
        mMainControl = new MainControl(KLauncher.this, this);
        //注册监听
        CacheNavigation.getInstance().registerOnSharedPreferenceChangeListener(this);
//        CacheLocation.getInstance().registerOnSharedPreferenceChangeListener(this);//此版本已没有天气模块,但是保留天气模块相关代码

        //初始化下拉刷新
        mPullRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                onPullForKinflowData(MainControl.LoadType.REFRESH, MessageFactory.REQUEST_ALL_KINFLOW);
            }

            /**上滑
             * @param refreshView
             */
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                onPullForKinflowData(MainControl.LoadType.LOAD_MORE, MessageFactory
                        .MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE, MessageFactory
                        .MESSAGE_WHAT_OBTAIN_SOUGOU_SEARCH_ARTICLE);

            }
        });

        mScrollView = mPullRefreshScrollView.getRefreshableView();
        mScrollView.setFillViewport(true);
//        mPullRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//关闭加载更多
        mPullRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        //
        //注册网络监听
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(mWifiChangeReceiver, filter);
        //存储第一次使用并初始化弹框
        CommonShareData.putBoolean(CommonShareData.KEY_IS_FIRST_USE_KINFLOW, true);

    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    public PopupWindowDialog getPopupWindowDialog() {
        try {
            if (null == mPopupWindowDialog) mPopupWindowDialog = new PopupWindowDialog(this);
        } catch (Exception e) {
            log("获取getPopupWindowDialog时出错");
            mPopupWindowDialog = new PopupWindowDialog(this);
        } finally {
            return mPopupWindowDialog;
        }
    }

    /**
     * 请求信息流
     *
     * @param loadType 上滑或者下拉
     * @param msgWhats
     */
    private void onPullForKinflowData(int loadType, int... msgWhats) {

        if (mWorkspace.isPageMoving()) {
            return;
        }

        boolean userAllowKinflowUseNet = CommonShareData.getBoolean(CommonShareData
                .KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);//用户允许信息流使用网络
        if (userAllowKinflowUseNet) {//用户允许使用网络
            startTime = System.currentTimeMillis();
            Log.e("下拉刷新", "下拉刷新的开始时间:" + startTime);
            requestKinflowData(loadType, msgWhats);
        } else {//用户不允许使用网络
            showFirstUseKinflowHint();
        }
    }

    @Override
    public void scrollToKinflow() {
        //super.scrollToKinflow();

        //初始化 是否试用 fasle
        PingManager.sIsKinflowIsUsed = false;
        LogUtil.e("sIsKinflowIsUsed", "scrollToKinflow" + PingManager.sIsKinflowIsUsed);
//        log("滑到信息流界面");
        if (!NetworkUtils.isNetworkAvailable(this)) {
            return;
        }
        try {
            if (CommonUtils.getInstance().allowUrlSkip()) {
                //获取跳转的url
                String skipUrl = CommonUtils.getInstance().currentSkipUrl();
                //跳转
                CommonUtils.getInstance().openDefaultBrowserUrl(this, skipUrl);//跳转--->埋点统计
                //埋点统计
                PingManager.getInstance().reportAutoAction4Skip2Url(skipUrl);
                //记录一次跳转
                CommonUtils.getInstance().saveSkipTime();
            }
            boolean isFirstUseKinflow = CommonShareData.getBoolean(CommonShareData.KEY_IS_FIRST_USE_KINFLOW, false);
            boolean userAllowKinflowUseNet = CommonShareData.getBoolean(CommonShareData
                    .KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);//用户允许信息流使用网络
            if (isFirstUseKinflow) {//第一次使用信息流:请求数据;不是第一次请求数据,啥也不做.
                if (userAllowKinflowUseNet) {//用户允许使用网络
//                    requestKinflowData(MessageFactory.REQUEST_ALL_KINFLOW);
                } else {//用户不允许使用网络
                    showFirstUseKinflowHint();
                }
            } else {
//                log("不是第一次使用信息流界面,啥也不做");
                autoRequestData();
            }
        } catch (Exception e) {
            log("scrollToKinflow时发生错误:" + e.getMessage());
        }
    }

    private void showFirstUseKinflowHint() {
        try {
            onCompleted();//如果正在刷新,则停止
//            getPopupWindowDialog().showPopupWindowDialog();
            getPopupWindowDialog().showPopupWindowDialog(new PopupWindowDialog.PopupWindowDialogListener() {
                @Override
                public void cancleClick() {
                    //总是使用网络-->false
                    //第一次使用信息流--->true
                    //隐藏popupWindowDialog

                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);
                    CommonShareData.putBoolean(CommonShareData.KEY_IS_FIRST_USE_KINFLOW, true);//将是否为第一次使用信息流修改为true
                    mPopupWindowDialog.dismissPopupWindowDialog();
                }

                @Override
                public void okClick() {
                    //总是使用网络--->根据checkbox的当前状态判断是否为为总是使用true||false
                    //第一次使用信息流--->false
                    //隐藏popupWindowDialog
                    //请求数据
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET,
                            mPopupWindowDialog.getCompoundState() ? true : false);//用户允许始终使用网络
                    CommonShareData.putBoolean(CommonShareData.KEY_IS_FIRST_USE_KINFLOW, false);//将是否为第一次使用信息流修改为false
                    mPopupWindowDialog.dismissPopupWindowDialog();
                    requestKinflowData(MainControl.LoadType.REFRESH, MessageFactory.REQUEST_ALL_KINFLOW);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoRequestData() {
        HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mWorkspace.isPageMoving()) {
                    return;
                }

                //网络可用---判断是啥网络
                if (NetworkUtils.isMobile(KLauncher.this)) {//移动网络
                    if (CommonShareData.refreshInterval4hour()) {//请求一次数据之前：判断距离上次清空数据缓存是否超过4个小时，如果超过则清空数据缓存并记录一次当前时间
                        requestKinflowData(MainControl.LoadType.REFRESH, MessageFactory.REQUEST_ALL_KINFLOW);
                    }
                } else if (NetworkUtils.isWifi(KLauncher.this)) {//wifi网络
                    requestKinflowData(MainControl.LoadType.REFRESH, MessageFactory.REQUEST_ALL_KINFLOW);
                }
            }
        }, 1000);

    }

    private void showFirstConnectedNetHint(final int... msgWhats) {
        try {
            onCompleted();//如果正在刷新,则停止
//            getPopupWindowDialog().showPopupWindowDialog();
            getPopupWindowDialog().showPopupWindowDialog(new PopupWindowDialog.PopupWindowDialogListener() {
                @Override
                public void cancleClick() {
                    //第一次联网---->true
                    CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET, true);
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);
                    mPopupWindowDialog.dismissPopupWindowDialog();
                }

                @Override
                public void okClick() {
                    //第一次联网---->false
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET,
                            mPopupWindowDialog.getCompoundState() ? true : false);//用户允许始终使用网络
                    CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET, false);
                    mPopupWindowDialog.dismissPopupWindowDialog();
                    requestKinflowData(MainControl.LoadType.REFRESH, msgWhats);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onViewClickedHintConnectedNet(final int viewId) {
        try {
            onCompleted();//如果正在刷新,则停止
            getPopupWindowDialog().showPopupWindowDialog();
            getPopupWindowDialog().showPopupWindowDialog(new PopupWindowDialog.PopupWindowDialogListener() {
                @Override
                public void cancleClick() {
                    //第一次联网---->true
                    CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET, true);
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);
                    mPopupWindowDialog.dismissPopupWindowDialog();
                }

                @Override
                public void okClick() {
                    //第一次联网---->false
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET,
                            mPopupWindowDialog.getCompoundState() ? true : false);//用户允许始终使用网络
                    CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET, false);
                    mPopupWindowDialog.dismissPopupWindowDialog();
                    //--
                    String pullUpAppPackageName = "";
                    switch (viewId) {
                        case R.id.hot_word_1://热词1监听
                            pullUpAppPackageName = CommonUtils.getInstance().openHotWord(KLauncher.this, hotWord1
                                    .getUrl());
                            PingManager.getInstance().reportUserAction4HotWord(hotWord1, pullUpAppPackageName);
                            break;
                        case R.id.hot_word_2://热词2监听
                            pullUpAppPackageName = CommonUtils.getInstance().openHotWord(KLauncher.this, hotWord2
                                    .getUrl());
                            PingManager.getInstance().reportUserAction4HotWord(hotWord2, pullUpAppPackageName);
                            break;
                        case R.id.hot_word_top:
                            pullUpAppPackageName = CommonUtils.getInstance().openHotWord(KLauncher.this, topHotWord
                                    .getUrl());
                            PingManager.getInstance().reportUserAction4HotWord(topHotWord, pullUpAppPackageName);
                            break;
                        case R.id.refresh_hotWord://刷新监听
                            ObjectAnimator animator = ObjectAnimator.ofFloat(iv_refresh, "rotation", 0f, 360f);
                            animator.setDuration(500);
                            animator.start();
//                                mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
                            break;
                        case R.id.search_hint://搜索框监听
                            Intent intent = new Intent(KLauncher.this, com.klauncher.kinflow.search.SearchActivity
                                    .class);
                            if (null == hintHotWord || null == hintHotWord.getWord())
                                hintHotWord = HotWord.getHintHotWord();
                            intent.putExtra(com.klauncher.kinflow.search.SearchActivity.HITN_HOT_WORD_KEY, hintHotWord);
                            startActivity(intent);
                            break;
                        case R.id.search_mode:
                        case R.id.search_icon:
                            if (null != hintHotWord && null != hintHotWord.getWord()) {
                                pullUpAppPackageName = CommonUtils.getInstance().openHotWord(KLauncher.this,
                                        hintHotWord.getUrl());
                            } else {
                                Toast.makeText(KLauncher.this, "没有获取到搜索热词,请刷新", Toast.LENGTH_SHORT).show();
                            }
                            PingManager.getInstance().reportUserAction4SearchBox(hintHotWord, pullUpAppPackageName);
                            //HotWord hotWord,String pullUpAppPackageName
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求信息流数据
     *
     * @param loadType 加载类型
     * @param msgWhats 为请求的数据类型
     *                 全部类型:
     *                 MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
     *                 MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,
     *                 MessageFactory.MESSAGE_WHAT_OBTAION_CARD,
     *                 MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG
     *                 热词类型:
     *                 MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
     *                 card类型&&config类型同上
     */
    private void requestKinflowData(int loadType, int... msgWhats) {
        try {
            //用户是否允许信息流使用网络发起请求
            //二,用户允许信息流使用网络
            if (NetworkUtils.isNetworkAvailable(KLauncher.this)) {//用户允许信息流使用网络并且当前网络可用
//                mMainControl.asynchronousRequest(msgWhats);
                mMainControl.asynchronousRequest_kinflow2(loadType, msgWhats);
            } else {//用户允许信息流使用网络但是当前网络不可用
                Toast.makeText(KLauncher.this, KLauncher.this.getResources().getString(R.string
                        .kinflow_string_connection_error), Toast.LENGTH_SHORT).show();
                onCompleted();
            }
        } catch (Exception e) {
            Log.e(TAG, "requestKinflowData: 请求数据失败");
            onCompleted();
        }

    }

    @Override
    public void scrollOutKinflow() {
        //进入 信息流 且有点击事件 会上报进入 事件  再上报退出事件
        LogUtil.e("sIsKinflowIsUsed", "scrollOutKinflow" + PingManager.sIsKinflowIsUsed);
        if (PingManager.sIsKinflowIsUsed) {
            String pkgNameStr = CommonShareData.getString(CommonShareData.KEY_APP_PACKAGE_NAME, "");
            if (!TextUtils.isEmpty(pkgNameStr) && !pkgNameStr.equals("null")) {
                MobileStatistics.onPageStart(this, pkgNameStr);
                LogUtil.e("sIsKinflowIsUsed", "onPageStart pkgname= " + pkgNameStr);
                MobileStatistics.onPageEnd(this, pkgNameStr);
                LogUtil.e("sIsKinflowIsUsed", "onPageEnd pkgname= " + pkgNameStr);

            }
            //super.scrollToKinflow();
            //super.scrollOutKinflow();
        }
        //退出 初始化false
        PingManager.sIsKinflowIsUsed = false;
    }

    /**
     * 请求定位:此版本已没有天气模块,但是保留天气模块相关代码
     */
    /*
    void requestLocation() {
        if (!DeviceState.gspIsOPen(this)) {
            mAlertDialog = DialogUtils.getCommontAlertDialog(this,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAlertDialog.dismiss();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAlertDialog.dismiss();
                            final Intent intentWiFi = new Intent(android.provider.Settings
                            .ACTION_LOCATION_SOURCE_SETTINGS);
                            intentWiFi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(intentWiFi, SETTING_WIFI);
                        }
                    });
            mAlertDialog.show();
        } else {
            startService(new Intent(KLauncher.this, LocationService.class));
        }
    }
    */
    @Override
    public void onClick(View view) {
//        startService(new Intent(KLauncher.this, LocationService.class));
        super.onClick(view);
        try {
            boolean userAllowKinflowUseNet = CommonShareData.getBoolean(CommonShareData
                    .KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);//用户允许信息流使用网络
            String pullUpAppPackageName = "";
            switch (view.getId()) {
                case R.id.hot_word_1://热词1监听
                    if (userAllowKinflowUseNet) {
                        pullUpAppPackageName = CommonUtils.getInstance().openHotWord(this, hotWord1.getUrl());
                        PingManager.getInstance().reportUserAction4HotWord(hotWord1, pullUpAppPackageName);
                    } else {
                        onViewClickedHintConnectedNet(R.id.hot_word_1);
                    }

                    break;
                case R.id.hot_word_2://热词2监听
                    if (userAllowKinflowUseNet) {
                        pullUpAppPackageName = CommonUtils.getInstance().openHotWord(this, hotWord2.getUrl());
                        PingManager.getInstance().reportUserAction4HotWord(hotWord2, pullUpAppPackageName);
                    } else {
                        onViewClickedHintConnectedNet(R.id.hot_word_2);
                    }
                    break;
                case R.id.hot_word_top:
                    if (userAllowKinflowUseNet) {
                        pullUpAppPackageName = CommonUtils.getInstance().openHotWord(this, topHotWord.getUrl());
                        PingManager.getInstance().reportUserAction4HotWord(topHotWord, pullUpAppPackageName);
                    } else {
                        onViewClickedHintConnectedNet(R.id.hot_word_top);
                    }

                    break;
                case R.id.refresh_hotWord://刷新监听
                    if (userAllowKinflowUseNet) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(iv_refresh, "rotation", 0f, 360f);
                        animator.setDuration(500);
                        animator.start();
//                        mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
                    } else {
                        onViewClickedHintConnectedNet(R.id.refresh_hotWord);
                    }
                    break;
                case R.id.search_hint://搜索框监听
                    if (userAllowKinflowUseNet) {
                        Intent intent = new Intent(KLauncher.this, com.klauncher.kinflow.search.SearchActivity.class);
                        if (null == hintHotWord || null == hintHotWord.getWord())
                            hintHotWord = HotWord.getHintHotWord();
                        intent.putExtra(com.klauncher.kinflow.search.SearchActivity.HITN_HOT_WORD_KEY, hintHotWord);
                        startActivity(intent);
                    } else {
                        onViewClickedHintConnectedNet(R.id.search_hint);
                    }

                    break;
                case R.id.search_mode:
                case R.id.search_icon:
                    if (userAllowKinflowUseNet) {
                        if (null != hintHotWord && null != hintHotWord.getWord()) {
                            pullUpAppPackageName = CommonUtils.getInstance().openHotWord(this, hintHotWord.getUrl());
                        } else {
                            Toast.makeText(this, "没有获取到搜索热词,请刷新", Toast.LENGTH_SHORT).show();
                        }
                        PingManager.getInstance().reportUserAction4SearchBox(hintHotWord, pullUpAppPackageName);
                        //HotWord hotWord,String pullUpAppPackageName
                    } else {
                        onViewClickedHintConnectedNet(R.id.search_icon);
                    }
                    break;
                case R.id.search_layout:
                    if (userAllowKinflowUseNet) {
                        Intent intent = new Intent(KLauncher.this, com.klauncher.kinflow.search.SearchActivity.class);
                        if (null == hintHotWord || null == hintHotWord.getWord())
                            hintHotWord = HotWord.getHintHotWord();
                        intent.putExtra(com.klauncher.kinflow.search.SearchActivity.HITN_HOT_WORD_KEY, hintHotWord);
                        startActivity(intent);
                    } else {
                        onViewClickedHintConnectedNet(R.id.search_hint);
                    }
//                    startActivity(new Intent(this,SearchActivity.class));
                    break;

//                case R.id.load_more:
//                    Toast.makeText(this, "加载更多", Toast.LENGTH_SHORT).show();
//                    break;
//                case R.id.connect_net:
//                    try {
//                        Intent intentWifi=new Intent();
//                        if (android.os.Build.VERSION.SDK_INT > 10) {
//                            intentWifi.setAction(android.provider.Settings.ACTION_SETTINGS);
//                        } else {
//                            intentWifi.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
//                        }
//                        this.startActivity(intentWifi);
//                    } catch (Exception e) {
//                        Toast.makeText(this, "打开网络设置失败,请在设置中手动打开", Toast.LENGTH_SHORT).show();
//                        log("打开网络设置失败,请在设置中手动打开");
//                    }
//                    break;

                //
                /*
                case R.id.weather_header:
                    requestLocation();
                    log("启动墨迹天气");
                    if (CommonUtils.getInstance().isInstalledAPK(this, OpenMode.COMPONENT_NAME_MOJI_TIANQI))
                        CommonUtils.getInstance().openApp(this, OpenMode.COMPONENT_NAME_MOJI_TIANQI);
                    break;
                */
                case R.id.parent_normal_search://搜索
                    doParentSearchClick(view);
                    break;
                case R.id.imb_packs://右侧二维码
                    doPacksClick(view);
                    break;
                case R.id.tv_refresh://刷新
                    onRefreshClick(view);
                    break;
                case R.id.tv_add_card://添加卡片
                    doAddCardClick(view);
                    break;
            }

        } catch (Exception e) {
            log("信息流界面点击事假出现未知异常:" + e.getMessage());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return super.onLongClick(v);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mWifiChangeReceiver);
            unregisterReceiver(mActiveReceiver);
            CardContentManagerFactory.clearAllOffset();
            CacheNavigation.getInstance().unregisterOnSharedPreferenceChangeListener(this);
//          CacheLocation.getInstance().unregisterOnSharedPreferenceChangeListener(this);
            Log.e("Klaucher_0512", "KLauncher onDestroy()");
        } catch (Exception e) {
            log("KLauncher在onDestroy时发生错误" + e.getMessage());
        }

        OKHttpUtil.cancel(REQUEST_CODE_NAVI_LIST);
        OKHttpUtil.cancel(REQUEST_CODE_DAILY_MITO_CTROL);
        OKHttpUtil.cancel(REQUEST_CODE_DAILY_MITO);
        OKHttpUtil.cancel(REQUEST_CODE_BAND_OFFERS);
        OKHttpUtil.cancel(REQUEST_CODE_NOVEL_RECOMMEND);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*
        Log.d("MainActivity", "收到缓存数据变更通知 key=" +
                key);
        if (key.equals(CacheLocation.KEY_LAT_LNG)) {//位置信息变更----->驱动天气数据变更
            mMainControl.obtainCityName();
        } else {//Navigation变更

        }
        */

        //Navigation变更
    }

    /**
     * 当更新天气,此版本已没有天气模块,但是保留天气模块相关代码
     *
     * @param weatherObject
     */
    @Override
    public void onWeatherUpdate(Weather weatherObject) {

        if (mWorkspace.isPageMoving()) {
            return;
        }

        tv_temperature.setText(weatherObject.getTemp());
        tv_city.setText(weatherObject.getCity());
        tv_weather.setText(weatherObject.getWeather());

        String weatherType = weatherObject.getWeather();
        if (weatherType.equals("晴")) {
            iv_weatherType.setBackgroundResource(R.drawable.sunny);

        } else if (weatherType.equals("多云")) {
            iv_weatherType.setBackgroundResource(R.drawable.cloudy);

        } else if (weatherType.equals("阴")) {
            iv_weatherType.setBackgroundResource(R.drawable.overcast);

        } else if (weatherType.equals("阵雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.shower);

        } else if (weatherType.equals("雷阵雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.thundershow);

        } else if (weatherType.equals("雷阵雨伴有冰雹")) {
            iv_weatherType.setBackgroundResource(R.drawable.thundershower_with_hail);

        } else if (weatherType.equals("雨夹雪")) {
            iv_weatherType.setBackgroundResource(R.drawable.sleet);

        } else if (weatherType.equals("小雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.light_rain);

        } else if (weatherType.equals("中雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.moderate_rain);

        } else if (weatherType.equals("大雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.heavy_rain);

        } else if (weatherType.equals("暴雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.storm);

        } else if (weatherType.equals("大暴雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.heavy_storm);

        } else if (weatherType.equals("特大暴雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.severe_storm);

        } else if (weatherType.equals("阵雪")) {
            iv_weatherType.setBackgroundResource(R.drawable.snow_flurry);

        } else if (weatherType.equals("小雪")) {
            iv_weatherType.setBackgroundResource(R.drawable.light_snow);

        }
    }

    @Override
    public void onCompleted() {
        try {
            if ((null != mPullRefreshScrollView) && mPullRefreshScrollView.isRefreshing())
                mPullRefreshScrollView.onRefreshComplete();
        } catch (Exception e) {
            try {
                log("onCompleted: 停止刷新时,出错:" + e.getMessage());
                if (null != mPullRefreshScrollView) {
                    mPullRefreshScrollView.onRefreshComplete();
                } else {
                    View kinflowView = getLayoutInflater().inflate(R.layout.activity_main, null);
                    mPullRefreshScrollView = (PullToRefreshScrollView) kinflowView.findViewById(R.id
                            .pull_refresh_scrollview);
                    mPullRefreshScrollView.onRefreshComplete();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        endTime = System.currentTimeMillis();
        Log.e("下拉刷新", "下拉刷新的结束时间:" + endTime);
        Log.e("下拉刷新", "下拉刷新耗时:" + String.valueOf(endTime - startTime) + "\n" +
                "===================================================================" + "\n");
    }

    @Override
    public void onHotWordUpdate(List<HotWord> hotWordList) {
        if (mWorkspace.isPageMoving()) {
            return;
        }
        try {
            //更新热词
            if (null != hotWordList && hotWordList.size() != 0) {
                //kinflow1
//                hintHotWord = hotWordList.get(0);
//                tv_searchHint.setHint(hintHotWord.getWord());
//                hotWord1 = hotWordList.get(1);
//                tv_hotWord1.setText(hotWord1.getWord());
//                hotWord2 = hotWordList.get(2);
//                tv_hotWord2.setText(hotWord2.getWord());
//                topHotWord = hotWordList.get(3);
//                tv_hotWordTop.setText(topHotWord.getWord());
                //kinflow2
                hintHotWord = hotWordList.get(0);
                tv_searchHint.setHint(hintHotWord.getWord());
//                ((TextView)(mLinearLayoutHeader.findViewById(R.id.search_hint))).setHint(hintHotWord.getWord());
//                 topHotWord = hotWordList.get(3);
//                tv_hotWordTop.setText(topHotWord.getWord());
            }
        } catch (Exception e) {
            log("onHotWordUpdate: 更新热词时,出错:" + e.getMessage());
        }

    }

    @Override
    public void onNavigationUpdate(List<Navigation> navigationList) {
        log("onGlobalCategoryNavigationUpdate: 开始更新网页导航");
        if (mWorkspace.isPageMoving()) {
            return;
        }
        try {
            if (null != navigationList && navigationList.size() != 0) {
                NavigationAdapter2 adapter = (NavigationAdapter2) mNavigationRecyclerView.getAdapter();
                adapter.updateNavigationList(navigationList);
//                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            log("onNavigationUpdate: 更新导航时出错:" + e.getMessage());
        }

    }

    @Override
    public void onGlobalCategoryNavigationUpdate(List<Navigation> navigationList) {
        log("onGlobalCategoryNavigationUpdate: 开始更新全局导航");
        if (mWorkspace.isPageMoving()) {
            return;
        }
        try {
            if (null != navigationList && navigationList.size() != 0) {
                if (navigationList.size() > 5) navigationList = navigationList.subList(0, 5);
                GlobalCategoryAdapter adapter = (GlobalCategoryAdapter) mGlobalCategoryRecyclerView.getAdapter();
                adapter.updateAdapter(navigationList);
            }
        } catch (Exception e) {
            log("onGlobalCategoryNavigationUpdate: 更新全局导航时出错:" + e.getMessage());
        }
    }

    /**
     * kinflow1使用onCardInfoUpdate更新内容,kinflow2使用onNewsAndAdUpdate更新内容
     *
     * @param cardInfoList
     */
    public void onCardInfoUpdate(List<CardInfo> cardInfoList) {
        if (mWorkspace.isPageMoving()) {
            return;
        }

        try {
            //kinflow1:更新CardsAdapter
            if (null != cardInfoList && cardInfoList.size() != 0) {
                mCardsView.removeAllViews();
                CardsAdapter cardsAdapter = new CardsAdapter(this, cardInfoList);
                mCardsView.setAdapter(cardsAdapter);
            }

        } catch (Exception e) {
            log("onCardInfoUpdate: 更新Card数据的时候出错:" + e.getMessage());
        }

    }

    @Override
    public void onNewsAndAdUpdate(final List<BaseRecyclerViewAdapterData> baseRecyclerViewAdapterDataList, final
    boolean append) {
        if (mWorkspace.isPageMoving()) {
            return;
        }
        onCompleted();
        HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mWorkspace.isPageMoving()) {
                    return;
                }
                if (append) {
                    mKinflow2NewsAdapter2.appendAdapter(baseRecyclerViewAdapterDataList);
                } else {
                    mNewsBodyRecyclerView.removeAllViews();
                    mKinflow2NewsAdapter2.updateAdapter(baseRecyclerViewAdapterDataList);
//                   scrollToTop();
                }
            }
        }, 500); //延迟防止出现刷新视图卡顿的情况
    }

    private void scrollToTop() {
        final ScrollView refreshableView = mPullRefreshScrollView.getRefreshableView();
        refreshableView.post(new Runnable() {
            @Override
            public void run() {
                refreshableView.smoothScrollTo(0, 0);
            }
        });
    }

    @Override
    public void onAddAdview(CardInfo cardInfo) {
        try {
            CardsAdapter adapter = (CardsAdapter) mCardsView.getAdapter();
            if (null == adapter) return;
            int adapterItemCount = adapter.getItemCount();
            if (adapterItemCount <= 1) {
                log("Card的个数<=1,所以不添加adview广告");
                return;
            }
            int type = adapter.getItemViewType(adapter.getItemCount() - 1);
            if (type != CardIdMap.ADVERTISEMENT_ADVIEW) {//没有adview----添加
                adapter.addCard(cardInfo);
            } else if (type == CardIdMap.ADVERTISEMENT_ADVIEW) {//有adview-----删除换新
                adapter.notifyItemRemoved(adapter.getItemCount() - 1);
                adapter.addCard(cardInfo);
            }
        } catch (Exception e) {
            log("onAddAdview: 时发生错误:" + e.getMessage());
        }

    }

    final protected static void log(String msg) {
        KinflowLog.e(msg);
    }


    private void requestMineNovelsData() {
        OKHttpUtil.get(Constant.NOVEL_RECOMMEND_URL, null, REQUEST_CODE_NOVEL_RECOMMEND, this);
    }

    private void requestBrandOffersData() {
        String url = Constant.getBandOffersUrl(0, 5);
        OKHttpUtil.get(url, null, REQUEST_CODE_BAND_OFFERS, this);
    }

    private void requestRealTimeData() {
        realTimeRequestManager = new DongFangRequestManager(
                this,
                new DongFangRequestManager.DongFangManagerListener() {
                    @Override
                    public void setNewsList(List<NewsBaseData> newsBaseDataList, int loadType) {
                        handlerRealTimeResult(newsBaseDataList);
                    }
                }
        );
        TabInfo tabInfo = HeadlinesCardViewHolder.newHeadlineTabInfo();
        realTimeRequestManager.loadDongFang(LoadNewsType.TYPE_FIRST, tabInfo);
        realTimeRequestManager.loadDongFang(LoadNewsType.TYPE_LOAD_MORE, tabInfo);
    }

    private void requestDailyMitoData() {
//        OKHttpUtil.get(Constant.DAILY_MITO_URL, null, REQUEST_CODE_DAILY_MITO_CTROL, this);
        OKHttpUtil.get(Constant.DAILY_MITO_CTROL_URL, null, REQUEST_CODE_DAILY_MITO_CTROL, this);
    }

    private void requestNaviListData() {
        OKHttpUtil.get(Const.NAVIGATION_WEB2, null, REQUEST_CODE_NAVI_LIST, this);
    }

    private void requestGameNewsData() {
        gameNewsRequestManager = new DongFangRequestManager(
                this,
                new DongFangRequestManager.DongFangManagerListener() {
                    @Override
                    public void setNewsList(List<NewsBaseData> newsBaseDataList, int loadType) {
                        handlerGameNewsResult(newsBaseDataList);
                    }
                }
        );
        TabInfo tabInfo = HotGameCardViewHolder.newGameNewsTabInfo();
        gameNewsRequestManager.loadDongFang(LoadNewsType.TYPE_FIRST, tabInfo);
        gameNewsRequestManager.loadDongFang(LoadNewsType.TYPE_LOAD_MORE, tabInfo);
    }

    private void handlerGameNewsResult(List<NewsBaseData> newsBaseDataList) {
        List<HotGameCardBean.NewsBean> beanList = parseGameList(newsBaseDataList);
        if (null != beanList && !beanList.isEmpty()) {
            if (null == gameNewsList) {
                gameNewsList = new ArrayList<>(beanList.size());
            }
            gameNewsList.addAll(beanList);
            updateGameNewsItemCard(currentGameNewsItem, true);
        } else {
            showShortToast(getString(com.kapp.kinflow.R.string.no_data));
        }
    }

    private void updateGameNewsItemCard(int currentGameNewsItem, boolean isUpdateUI) {
        if (currentGameNewsItem < gameNewsList.size()) {
            HotGameCardBean.NewsBean newsBean = gameNewsList.get(currentGameNewsItem);
            int contentCardPos = findContentCardPos(hotGameCardBean.getItemType());
            if (-1 != contentCardPos) {
                hotGameCardBean.updateDongFangTouTiao(newsBean);
                if (isUpdateUI) {
                    contentAdapter.updateItem(contentCardPos);
                }
            }

        }
    }

    private List<HotGameCardBean.NewsBean> parseGameList(List<NewsBaseData> newsBaseDataList) {
        List<HotGameCardBean.NewsBean> result = new ArrayList<>();

        //筛选出新闻部分数据
        List<DongFangTouTiao> news = new ArrayList<>();
        if (null != newsBaseDataList && !newsBaseDataList.isEmpty()) {
            for (NewsBaseData data : newsBaseDataList) {
                if (NewsBaseData.TYPE_NEWS == data.getDataType()) {
                    news.add((DongFangTouTiao) data);
                }
            }
        }

        //每隔两条插入头条卡片
        int newsSize = news.size();
        int duration = 2;
        int integerSize = newsSize / duration;
        int remainder = newsSize - integerSize * duration;//余数
        if (0 != newsSize) {//有新闻数据
            if (newsSize < duration) {//新闻数据小于2条
                if (1 == newsSize) {//1条新闻
                    result.add(new HotGameCardBean.NewsBean(news.get(0), null));
                    LogUtils.e("parseGameList() 只有一条新闻...");
                }
            } else {
                LogUtils.e("parseGameList() 大于2条新闻...");
                for (int i = 0; i < integerSize; i++) {
                    result.add(new HotGameCardBean.NewsBean(news.get(i), news.get(i + 1)));
                }
                if (0 != remainder) {//存在余数
                    if (1 == remainder) {//余数1
                        LogUtils.e("parseGameList) 余数为1");
                        result.add(new HotGameCardBean.NewsBean(news.get(newsSize - 1), null));
                    }
                }
            }
        }
        return result;
    }

    private void requestHappyData() {
        happyRequestManager = new DongFangRequestManager(
                this,
                new DongFangRequestManager.DongFangManagerListener() {
                    @Override
                    public void setNewsList(List<NewsBaseData> newsBaseDataList, int loadType) {
                        handlerHappyResult(newsBaseDataList);
                    }
                }
        );
        TabInfo tabInfo = HappyCardViewHolder.newHappyTabInfo();
        happyRequestManager.loadDongFang(LoadNewsType.TYPE_FIRST, tabInfo);
        happyRequestManager.loadDongFang(LoadNewsType.TYPE_LOAD_MORE, tabInfo);
    }

    private void requestConstellationData() {
        constellationRequestManager = new DongFangRequestManager(
                this,
                new DongFangRequestManager.DongFangManagerListener() {
                    @Override
                    public void setNewsList(List<NewsBaseData> newsBaseDataList, int loadType) {
                        handlerConstellationResult(newsBaseDataList);
                    }
                }
        );
        TabInfo tabInfo = ConstellationCardViewHolder.newConstellationTabInfo();
        constellationRequestManager.loadDongFang(LoadNewsType.TYPE_FIRST, tabInfo);
        constellationRequestManager.loadDongFang(LoadNewsType.TYPE_LOAD_MORE, tabInfo);
    }

    private void handlerConstellationResult(List<NewsBaseData> newsBaseDataList) {
        List<ConstellationCardBean> beanList = parseConstellationList(newsBaseDataList);
        if (null != beanList && !beanList.isEmpty()) {
            if (null == constellationList) {
                constellationList = new ArrayList<>(beanList.size());
            }
            constellationList.addAll(beanList);
            updateConstellationItemCard(currentConstellationItem, true);
        } else {
            showShortToast(getString(com.kapp.kinflow.R.string.no_data));
        }
    }

    private void handlerHappyResult(List<NewsBaseData> newsBaseDataList) {
        List<HappyCardBean> beanList = parseHappyList(newsBaseDataList);
        if (null != beanList && !beanList.isEmpty()) {
            if (null == happyList) {
                happyList = new ArrayList<>(beanList.size());
            }
            happyList.addAll(beanList);
            updateHappyItemCard(currentHappyItem, true);
        } else {
            showShortToast(getString(com.kapp.kinflow.R.string.no_data));
        }
    }

    private void requestHeaderLineData() {
        headerLineRequestManager = new DongFangRequestManager(
                this,
                new DongFangRequestManager.DongFangManagerListener() {
                    @Override
                    public void setNewsList(List<NewsBaseData> newsBaseDataList, int loadType) {
                        handlerHeaderLineResult(newsBaseDataList);
                    }
                }
        );
        TabInfo tabInfo = HeadlinesCardViewHolder.newHeadlineTabInfo();
        headerLineRequestManager.loadDongFang(LoadNewsType.TYPE_FIRST, tabInfo);
        headerLineRequestManager.loadDongFang(LoadNewsType.TYPE_LOAD_MORE, tabInfo);
    }

    private void handlerHeaderLineResult(List<NewsBaseData> newsBaseDataList) {
        List<HeadlineCardBean> beanList = parseHeaderLineList(newsBaseDataList);
        if (null != beanList && !beanList.isEmpty()) {
            if (null == toutiaoList) {
                toutiaoList = new ArrayList<>(beanList.size());
            }
            toutiaoList.addAll(beanList);
            updateHeaderLineCard(currentNewsItem, true);
        } else {
            showShortToast(getString(com.kapp.kinflow.R.string.no_data));
        }
    }

    private void handlerRealTimeResult(List<NewsBaseData> newsBaseDataList) {
        List<RealTimeCardBean> beanList = parseRealTimeList(newsBaseDataList);
        if (null != beanList && !beanList.isEmpty()) {
            if (null == realTimeList) {
                realTimeList = new ArrayList<>(beanList.size());
            }
            realTimeList.addAll(beanList);
            updateRealTimeCard(currentRealTimeItem, true);
        } else {
            showShortToast(getString(com.kapp.kinflow.R.string.no_data));
        }
    }

    private List<RealTimeCardBean> parseRealTimeList(List<NewsBaseData> newsBaseDataList) {
        if (null == newsBaseDataList || newsBaseDataList.isEmpty()) {
            return null;
        }
        //筛选出新闻部分数据
        List<DongFangTouTiao> news = new ArrayList<>();
        if (null != newsBaseDataList && !newsBaseDataList.isEmpty()) {
            for (NewsBaseData data : newsBaseDataList) {
                if (NewsBaseData.TYPE_NEWS == data.getDataType()) {
                    news.add((DongFangTouTiao) data);
                }
            }
        }

        if (null == news || news.isEmpty()) {
            return null;
        }

        int duration = 6;
        int totalSize = news.size();
        int resultSize = totalSize / duration;
        int remainder = totalSize - resultSize * duration;
        if (0 != remainder) {
            resultSize++;
        }
        List<RealTimeCardBean> realTimeBeanList = new ArrayList<>(resultSize);
        int resultIndex = 0;
        int j = 1;
        List<RealTimeCardBean.SingeRealTimeBean> itemList = new ArrayList<>(duration);
        for (int i = 0; i < totalSize; i++) {
            DongFangTouTiao dongFangTouTiao = news.get(i);
            String title = dongFangTouTiao.getmNewsBean().getSummaryTitle();
            String url = dongFangTouTiao.getmNewsBean().getUrl();
            RealTimeCardBean.SingeRealTimeBean itemBean = new RealTimeCardBean.SingeRealTimeBean(title, url);
            if (i < j * duration) {
                itemList.add(itemBean);
                if (i == totalSize - 1) {//最后一条新闻
                    realTimeBeanList.add(new RealTimeCardBean(itemList));
                }
            } else {
                realTimeBeanList.add(new RealTimeCardBean(itemList));
                resultIndex++;
                if (resultIndex == resultSize - 1) {//最后一个
                    if (0 != remainder) {//有余数
                        itemList = new ArrayList<>(remainder);
                    } else {//恰好整除
                        itemList = new ArrayList<>(duration);
                    }
                } else {//不是最后一个
                    itemList = new ArrayList<>(duration);
                }
                itemList.add(itemBean);
                j++;
            }
        }
        return realTimeBeanList;
    }

//    private void testHttp() {
//        int requestCode = 100;
////        String url="http://api.kxcontrol.com:666/v1/config/funclist?cid=1004_1101_10500200&param=interval";
////      String url=Const.DONGFANG_RANDOM_CHARS;
//        String url = "http://kdhcodeapi.dftoutiao.com/newskey_kdh/code";
////        String url="https://www.baidu.com/";
//        OKHttpUtil.get(url, null, requestCode, new OKHttpUtil.OnRequestListener() {
//            @Override
//            public void onFailure(int requestCode, String msg) {
//                LogUtils.e("testrequest onFailure msg=" + msg);
//            }
//
//            @Override
//            public void onResponse(int requestCode, Response fullResponse, Object simpleResponse) {
//                LogUtils.e("testrequest onResponse msg=" + fullResponse.toString());
//                fullResponse.close();
//            }
//        });
//    }

    private void initViews() {
//        ptrFrame.setPtrHandler(new PtrHandler() {
//            @Override
//            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
//                return true;
//            }
//
//            @Override
//            public void onRefreshBegin(PtrFrameLayout frame) {
//                ptrFrame.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        ptrFrame.refreshComplete();
//                    }
//                }, 2000);
//            }
//        });

        ptrFrame.setOnHeaderRefreshListener(new PullToRefreshView.OnHeaderRefreshListener() {

            @Override
            public void onHeaderRefresh(PullToRefreshView view) {
                view.postDelayed(new Runnable() {
                    public void run() {
                        ptrFrame.onHeaderRefreshComplete();
                    }
                }, 1000);
            }
        });
        nestScrollview.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);//防止java.lang
        // .IllegalArgumentException: parameter must be a descendant of this view

        headerRecyclerview.setLayoutManager(new FullyGridLayoutManager(this, 4));
//        headerRecyclerview.setLayoutManager(new GridLayoutManager(this, 4));
        headerRecyclerview.setFocusable(false);
        headerRecyclerview.setHasFixedSize(true);
        headerRecyclerview.setNestedScrollingEnabled(false);

        int drawableLeftWidth = ScreenUnitUtil.asDp(20);
        int drawableLeftHeight = ScreenUnitUtil.asDp(20);
        TextViewUtil.setDrawableLeft(tvRefresh, com.kapp.kinflow.R.drawable.market_search_guess_refresh,
                drawableLeftWidth,
                drawableLeftHeight);
        TextViewUtil.setDrawableLeft(tvAddCard, com.kapp.kinflow.R.drawable.navigation_add_card_btn, drawableLeftWidth,
                drawableLeftHeight);

//        recyclerview.setLayoutManager(new FullyLinearLayoutManager(this, LinearLayoutManager
//                .VERTICAL, false));
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false));
//        recyclerview.setHasFixedSize(true);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setFocusable(false);
    }


    private <T extends BaseInformationFlowBean> T findContentCard(int viewType) {
        List<BaseItemBean> normalDatas = contentAdapter.getNormalDatas();
        for (BaseItemBean baseItemBean : normalDatas) {
            if (baseItemBean.getItemType() == viewType) {
                return (T) baseItemBean;
            }
        }
        return null;
    }

    private int findContentCardPos(int viewType) {
        List<BaseItemBean> normalDatas = contentAdapter.getNormalDatas();
        int size = normalDatas.size();
        for (int i = 0; i < size; i++) {
            BaseItemBean baseItemBean = normalDatas.get(i);
            if (baseItemBean.getItemType() == viewType) {
                return i;
            }
        }
        return -1;
    }

    public void changeHeaderLineCard(HeadlinesCardViewHolder holder) {
        int cacheSize = toutiaoList.size();
        currentNewsItem++;
        if (currentNewsItem == cacheSize) {
            currentNewsItem = 0;
        }
        if (null == holder) {
            updateHeaderLineCard(currentNewsItem, true);
            return;
        }

        HeadlineCardBean cardBean = updateHeaderLineCard(currentNewsItem, false);
        if (null != cardBean) {
            holder.updateNewsContent(cardBean);
        }
    }

    public void changeRealTimeCard(RealTimeCardViewHolder holder) {
        int cacheSize = realTimeList.size();
        currentRealTimeItem++;
        if (currentRealTimeItem == cacheSize) {
            currentRealTimeItem = 0;
        }
        if (null == holder) {
            updateRealTimeCard(currentRealTimeItem, true);
            return;
        }

        RealTimeCardBean cardBean = updateRealTimeCard(currentRealTimeItem, false);
        if (null != cardBean) {
            holder.updateContent(cardBean);
        }
    }

    public void changeSiteNaviCard(SiteNavigationCardViewHolder holder) {
        int cacheSize = naviCardBeanList.size();
        currentNaviCardItem++;
        if (currentNaviCardItem == cacheSize) {
            currentNaviCardItem = 0;
        }
        if (null == holder) {
            updateSiteNaviItemCard(currentNaviCardItem, true);
            return;
        }
        SiteNavigationCardBean cardBean = updateSiteNaviItemCard(currentNaviCardItem, false);
        if (null != cardBean) {
            holder.updateContent(cardBean);
        }
    }

    public void nextDailyMitoCard(DailyMitoCardViewHolder holder) {
        currentDailyMitoCardItem++;
        int cacheSize = dailyMitoCardBeanList.size();
//        currentDailyMitoCardItem++;
        if (currentDailyMitoCardItem == cacheSize - 2) {//快要到头的时候请求
            requestNextDailyMitoData();
        }

        if (currentDailyMitoCardItem == cacheSize) {
            currentDailyMitoCardItem = 0;
        } else if (currentDailyMitoCardItem < 0) {
            currentDailyMitoCardItem = cacheSize - 1;
        }
        changeDailyMitoCard(currentDailyMitoCardItem, holder);
    }

    private void requestNextDailyMitoData() {
        currentdailyMitoLabelListItem++;
        String dailyMitoUrl = getDailyMitoUrl();
        OKHttpUtil.get(dailyMitoUrl, null, REQUEST_CODE_DAILY_MITO, this);
    }

    public void preDailyMitoCard(DailyMitoCardViewHolder holder) {
        currentDailyMitoCardItem--;
        int cacheSize = dailyMitoCardBeanList.size();
//        currentDailyMitoCardItem++;
        if (currentDailyMitoCardItem == cacheSize) {
            currentDailyMitoCardItem = 0;
        } else if (currentDailyMitoCardItem < 0) {
            currentDailyMitoCardItem = cacheSize - 1;
        }
        changeDailyMitoCard(currentDailyMitoCardItem, holder);
    }

    private void changeDailyMitoCard(int currentDailyMitoCardItem, DailyMitoCardViewHolder holder) {
        if (null == holder) {
            updateDailyMitoItemCard(currentDailyMitoCardItem, true);
            return;
        }
        DailyMitoCardBean cardBean = updateDailyMitoItemCard(currentDailyMitoCardItem, false);
        if (null != cardBean) {
            holder.updateContent(cardBean);
        }
    }
//    新版kinflow卡片回到顶部
//    public void scrollToTop() {
//        scrollToPositionWithOffset(0, 0);
//    }

    public void stickCardItemToTop(int position) {
        contentAdapter.exchange(position, 0);
    }

    @Override
    public String getDailyMitoMoreUrl() {
        return dailyMitoMoreUrl;
    }

    @Override
    public ArrayList<HeaderAppBean> getRecommendApps() {
        if (null == headerAdapter) {
            return null;
        }
        return (ArrayList<HeaderAppBean>) headerAdapter.getNormalDatas();
    }

    public void scrollToPositionWithOffset(int position, int offset) {
        if (null == recyclerview) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerview.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position, offset);
        }
    }

    public void changeGameNewsCard(HotGameCardViewHolder holder) {
        int cacheSize = gameNewsList.size();
        currentGameNewsItem++;
        if (currentGameNewsItem == cacheSize) {
            currentGameNewsItem = 0;
        }
        if (null == holder) {
            updateGameNewsItemCard(currentGameNewsItem, true);
            return;
        }
        updateGameNewsItemCard(currentGameNewsItem, false);
        holder.updateNewsContent(hotGameCardBean);
    }

    public void changeHappyCard(HappyCardViewHolder holder) {
        int cacheSize = happyList.size();
        currentHappyItem++;
        if (currentHappyItem == cacheSize) {
            currentHappyItem = 0;
        }
        if (null == holder) {
            updateHappyItemCard(currentHappyItem, true);
            return;
        }

        HappyCardBean happyCardBean = updateHappyItemCard(currentHappyItem, false);
        if (null != happyCardBean) {
            holder.updateNewsContent(happyCardBean);
        }
    }

    public void changeConstellationCard(ConstellationCardViewHolder holder) {
        int cacheSize = constellationList.size();
        currentConstellationItem++;
        if (currentConstellationItem == cacheSize) {
            currentConstellationItem = 0;
        }
        if (null == holder) {
            updateConstellationItemCard(currentConstellationItem, true);
            return;
        }
        ConstellationCardBean constellationCardBean = updateConstellationItemCard(currentConstellationItem, false);
        if (null != constellationCardBean) {
            holder.updateNewsContent(constellationCardBean);
        }
    }


    private HeadlineCardBean updateHeaderLineCard(int currentNewsItem, boolean isUpdateUI) {
        if (currentNewsItem < toutiaoList.size()) {
            HeadlineCardBean cardBean = toutiaoList.get(currentNewsItem);
//            BaseInformationFlowBean contentCard = findContentCard(cardBean.getItemType());
            int contentCardPos = findContentCardPos(cardBean.getItemType());
            if (-1 != contentCardPos) {
                HeadlineCardBean target = (HeadlineCardBean) contentAdapter.getNormalDatas().get(contentCardPos);
//                target = cardBean;
                target.updateCardBean(cardBean);
                if (isUpdateUI) {
                    contentAdapter.updateItem(contentCardPos);
                }
                return target;
            }
        }
        return null;
    }

    private RealTimeCardBean updateRealTimeCard(int currentItem, boolean isUpdateUI) {
        if (currentItem < realTimeList.size()) {
            RealTimeCardBean cardBean = realTimeList.get(currentItem);
//            BaseInformationFlowBean contentCard = findContentCard(cardBean.getItemType());
            int contentCardPos = findContentCardPos(cardBean.getItemType());
            if (-1 != contentCardPos) {
                RealTimeCardBean target = (RealTimeCardBean) contentAdapter.getNormalDatas().get(contentCardPos);
//                target = cardBean;
                target.updateCardBean(cardBean);
                if (isUpdateUI) {
                    contentAdapter.updateItem(contentCardPos);
                }
                return target;
            }
        }
        return null;
    }

    private ConstellationCardBean updateConstellationItemCard(int currentItem, boolean isUpdateUI) {
        if (currentItem < constellationList.size()) {
            ConstellationCardBean cardBean = constellationList.get(currentItem);
//            BaseInformationFlowBean contentCard = findContentCard(cardBean.getItemType());
            int contentCardPos = findContentCardPos(cardBean.getItemType());
            if (-1 != contentCardPos) {
                ConstellationCardBean target = (ConstellationCardBean) contentAdapter.getNormalDatas().get
                        (contentCardPos);
//                target = cardBean;
                target.updateCardBean(cardBean);
                if (isUpdateUI) {
                    contentAdapter.updateItem(contentCardPos);
                }
                return target;
            }
        }
        return null;
    }

    private SiteNavigationCardBean updateSiteNaviItemCard(int currentItem, boolean isUpdateUI) {
        if (currentItem < naviCardBeanList.size()) {
            SiteNavigationCardBean cardBean = naviCardBeanList.get(currentItem);
            int contentCardPos = findContentCardPos(cardBean.getItemType());
            if (-1 != contentCardPos) {
                SiteNavigationCardBean target = (SiteNavigationCardBean) contentAdapter.getNormalDatas().get
                        (contentCardPos);
                target.updateCardBean(cardBean);
                if (isUpdateUI) {
                    contentAdapter.updateItem(contentCardPos);
                }
                return target;
            }
        }
        return null;
    }

    private DailyMitoCardBean updateDailyMitoItemCard(int currentItem, boolean isUpdateUI) {
        if (currentItem < dailyMitoCardBeanList.size()) {
            DailyMitoCardBean cardBean = dailyMitoCardBeanList.get(currentItem);
            int contentCardPos = findContentCardPos(cardBean.getItemType());
            if (-1 != contentCardPos) {
                DailyMitoCardBean target = (DailyMitoCardBean) contentAdapter.getNormalDatas().get
                        (contentCardPos);
                target.updateCardBean(cardBean);
                if (isUpdateUI) {
                    contentAdapter.updateItem(contentCardPos);
                }
                return target;
            }
        }
        return null;
    }

    private HappyCardBean updateHappyItemCard(int currentItem, boolean isUpdateUI) {
        if (currentItem < happyList.size()) {
            HappyCardBean cardBean = happyList.get(currentItem);
//            BaseInformationFlowBean contentCard = findContentCard(cardBean.getItemType());
            int contentCardPos = findContentCardPos(cardBean.getItemType());
            if (-1 != contentCardPos) {
                HappyCardBean target = (HappyCardBean) contentAdapter.getNormalDatas().get
                        (contentCardPos);
//                target = cardBean;
                target.updateCardBean(cardBean);
                if (isUpdateUI) {
                    contentAdapter.updateItem(contentCardPos);
                }
                return target;
            }

        }
        return null;
    }

    private List<HeadlineCardBean> parseHeaderLineList(List<NewsBaseData> newsBaseDataList) {
        List<HeadlineCardBean> result = new ArrayList<>();

        //筛选出新闻部分数据
        List<DongFangTouTiao> news = new ArrayList<>();
        if (null != newsBaseDataList && !newsBaseDataList.isEmpty()) {
            for (NewsBaseData data : newsBaseDataList) {
                if (NewsBaseData.TYPE_NEWS == data.getDataType()) {
                    news.add((DongFangTouTiao) data);
                }
            }
        }

        //每隔三条插入头条卡片
        int newsSize = news.size();
        int duration = 3;
        int integerSize = newsSize / duration;
        int remainder = newsSize - integerSize * duration;//余数
        if (0 != newsSize) {//有新闻数据
            if (newsSize < duration) {//新闻数据小于3条
                if (1 == newsSize) {//1条新闻
                    result.add(new HeadlineCardBean(news.get(0), null, null));
                    LogUtils.e("parseHeaderLineList() 只有一条新闻...");
                } else if (2 == newsSize) {//2条新闻
                    result.add(new HeadlineCardBean(news.get(0), news.get(1), null));
                    LogUtils.e("parseHeaderLineList() 只有两条新闻...");
                }
            } else {
                LogUtils.e("parseHeaderLineList() 大于3条新闻...");
                for (int i = 0; i < integerSize; i++) {
                    result.add(new HeadlineCardBean(news.get(i), news.get(i + 1), news.get(i + 2)));
                }
                if (0 != remainder) {//存在余数
                    if (1 == remainder) {//余数1
                        LogUtils.e("parseToutiaoLis) 余数为1");
                        result.add(new HeadlineCardBean(news.get(newsSize - 1), null, null));
                    } else if (2 == remainder) {//余数2
                        LogUtils.e("parseHeaderLineList() 余数为2");
                        result.add(new HeadlineCardBean(news.get(newsSize - 1), news.get(newsSize - 2), null));
                    }
                }
            }
        }
        return result;
    }

    private List<ConstellationCardBean> parseConstellationList(List<NewsBaseData> newsBaseDataList) {
        List<ConstellationCardBean> result = new ArrayList<>();

        //筛选出新闻部分数据
        List<DongFangTouTiao> news = new ArrayList<>();
        if (null != newsBaseDataList && !newsBaseDataList.isEmpty()) {
            for (NewsBaseData data : newsBaseDataList) {
                if (NewsBaseData.TYPE_NEWS == data.getDataType()) {
                    news.add((DongFangTouTiao) data);
                }
            }
        }

        //每隔三条插入头条卡片
        int newsSize = news.size();
        int duration = 3;
        int integerSize = newsSize / duration;
        int remainder = newsSize - integerSize * duration;//余数
        if (0 != newsSize) {//有新闻数据
            if (newsSize < duration) {//新闻数据小于3条
                if (1 == newsSize) {//1条新闻
                    result.add(new ConstellationCardBean(news.get(0), null, null));
                    LogUtils.e("parseConstellationList() 只有一条新闻...");
                } else if (2 == newsSize) {//2条新闻
                    result.add(new ConstellationCardBean(news.get(0), news.get(1), null));
                    LogUtils.e("parseConstellationList() 只有两条新闻...");
                }
            } else {
                LogUtils.e("parseConstellationList() 大于3条新闻...");
                for (int i = 0; i < integerSize; i++) {
                    result.add(new ConstellationCardBean(news.get(i), news.get(i + 1), news.get(i + 2)));
                }
                if (0 != remainder) {//存在余数
                    if (1 == remainder) {//余数1
                        LogUtils.e("parseConstellationList) 余数为1");
                        result.add(new ConstellationCardBean(news.get(newsSize - 1), null, null));
                    } else if (2 == remainder) {//余数2
                        LogUtils.e("parseConstellationList() 余数为2");
                        result.add(new ConstellationCardBean(news.get(newsSize - 1), news.get(newsSize - 2), null));
                    }
                }
            }
        }
        return result;
    }

    private List<HappyCardBean> parseHappyList(List<NewsBaseData> newsBaseDataList) {
        List<HappyCardBean> result = new ArrayList<>();

        //筛选出新闻部分数据
        List<DongFangTouTiao> news = new ArrayList<>();
        if (null != newsBaseDataList && !newsBaseDataList.isEmpty()) {
            for (NewsBaseData data : newsBaseDataList) {
                if (NewsBaseData.TYPE_NEWS == data.getDataType()) {
                    news.add((DongFangTouTiao) data);
                }
            }
        }

        //每隔三条插入头条卡片
        int newsSize = news.size();
        int duration = 3;
        int integerSize = newsSize / duration;
        int remainder = newsSize - integerSize * duration;//余数
        if (0 != newsSize) {//有新闻数据
            if (newsSize < duration) {//新闻数据小于3条
                if (1 == newsSize) {//1条新闻
                    result.add(new HappyCardBean(news.get(0), null, null));
                    LogUtils.e("parseHappyList() 只有一条新闻...");
                } else if (2 == newsSize) {//2条新闻
                    result.add(new HappyCardBean(news.get(0), news.get(1), null));
                    LogUtils.e("parseHappyList() 只有两条新闻...");
                }
            } else {
                LogUtils.e("parseHappyList() 大于3条新闻...");
                for (int i = 0; i < integerSize; i++) {
                    result.add(new HappyCardBean(news.get(i), news.get(i + 1), news.get(i + 2)));
                }
                if (0 != remainder) {//存在余数
                    if (1 == remainder) {//余数1
                        LogUtils.e("parseHappyList) 余数为1");
                        result.add(new HappyCardBean(news.get(newsSize - 1), null, null));
                    } else if (2 == remainder) {//余数2
                        LogUtils.e("parseHappyList() 余数为2");
                        result.add(new HappyCardBean(news.get(newsSize - 1), news.get(newsSize - 2), null));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void onFailure(int requestCode, String msg) {
        LogUtils.e("requestCode=" + requestCode + ", msg=" + msg);
    }

    @Override
    public void onResponse(int requestCode, Object simpleResponse) {
        LogUtils.v("requestCode=" + requestCode);
        switch (requestCode) {
            case REQUEST_CODE_NAVI_LIST://导航地址部分
                handlerSiteNaviListResult(simpleResponse);
                break;
            case REQUEST_CODE_DAILY_MITO_CTROL://每日美图控制
                handlerDailyMitoContrlResult(simpleResponse);
                break;
            case REQUEST_CODE_DAILY_MITO://每日美图
                handlerDailyMitoResult(simpleResponse);
                break;
            case REQUEST_CODE_BAND_OFFERS://品牌优惠
                handlerBrandOffersResult(simpleResponse);
                break;
            case REQUEST_CODE_NOVEL_RECOMMEND://小说推荐
                handlerNovelRecommend(simpleResponse);
                break;
        }
    }

    private void handlerNovelRecommend(Object simpleResponse) {
        if (null == simpleResponse) {
            LogUtils.e("获取图书推荐失败，io错误!!!");
            return;
        }
        try {
            JSONArray rootJson = new JSONArray(simpleResponse.toString());
            int length = rootJson.length();
            int resultSize = 3;
            List<MineNovelsBean> datas = new ArrayList<>(resultSize);
            Integer[] randDifferentIntValue = MathUtil.getRandDifferentIntValue(length, resultSize);
            for (Integer integer : randDifferentIntValue) {
                JSONObject itemJson = rootJson.getJSONObject(integer.intValue());
                MineNovelsBean bean = new MineNovelsBean(itemJson);
                datas.add(bean);
            }
//
//            for (int i = 0; i < length; i++) {
//                JSONObject itemJson = rootJson.getJSONObject(i);
//                MineNovelsBean bean = new MineNovelsBean(itemJson);
//                datas.add(bean);
//            }


            NovelCardBean cardBean = new NovelCardBean(datas);
            int contentCardPos = findContentCardPos(cardBean.getItemType());
            if (-1 != contentCardPos) {
                NovelCardBean target = (NovelCardBean) contentAdapter.getNormalDatas().get
                        (contentCardPos);
                target.updateCardBean(cardBean);
                contentAdapter.updateItem(contentCardPos);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlerBrandOffersResult(Object simpleResponse) {
        if (null == simpleResponse) {
            LogUtils.e("品牌优惠响应失败，发生io错误");
            return;
        }
        try {
            JSONObject rootJson = new JSONObject(simpleResponse.toString());
            String codeString = rootJson.getString("code");
            int code = Integer.parseInt(codeString);
            if (200 == code) {
                JSONArray data = rootJson.getJSONArray("data");
                int length = data.length();
                List<BrandOffersCardBean.SingleBrandOffersBean> beans = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    JSONObject itemJson = data.getJSONObject(i);
                    BrandOffersCardBean.SingleBrandOffersBean bean = new BrandOffersCardBean.SingleBrandOffersBean
                            (itemJson);
                    beans.add(bean);
                }
                BrandOffersCardBean cardBean = new BrandOffersCardBean(beans);
                int contentCardPos = findContentCardPos(cardBean.getItemType());
                if (-1 != contentCardPos) {
                    BrandOffersCardBean target = (BrandOffersCardBean) contentAdapter.getNormalDatas().get
                            (contentCardPos);
                    target.updateCardBean(cardBean);
                    contentAdapter.updateItem(contentCardPos);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void handlerDailyMitoContrlResult(Object simpleResponse) {
        if (null == simpleResponse) {
            LogUtils.e("每日美图控制响应失败，发生io错误");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(simpleResponse.toString());
            String statusString = jsonObject.getString("status");
            int status = Integer.parseInt(statusString);
            if (1 == status) {
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray labelList = data.getJSONArray("labelList");
                int length = labelList.length();
                dailyMitoLabelIdList = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    JSONObject jsonObject1 = labelList.getJSONObject(i);
                    int id = jsonObject1.getInt("id");
                    dailyMitoLabelIdList.add(id);
                }

//                Random RANDOM = new Random(100);
//                int randIndex = RANDOM.nextInt(length);
//                JSONObject randJson = labelList.getJSONObject(randIndex);
//                int randId = randJson.getInt("id");
//                LogUtils.e("randId=" + randId);
                dailyMitoDid = data.getInt("did");
                dailyMitoGalleryparam = data.getString("galleryparam");
                dailyMitoMoreUrl = data.getString("more");

//                int dailyMitoLabelListId = getDailyMitoLabelListId(dailyMitoLabelIdList,
// currentdailyMitoLabelListItem);
//                String randDailyMitoUrl = Constant.getDailyMitoUrl(dailyMitoDid, dailyMitoLabelListId,
//                        dailyMitoGalleryparam);
                String dailyMitoUrl = getDailyMitoUrl();
                OKHttpUtil.get(dailyMitoUrl, null, REQUEST_CODE_DAILY_MITO, this);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getDailyMitoUrl() {
        int dailyMitoLabelListId = getDailyMitoLabelListId(dailyMitoLabelIdList, currentdailyMitoLabelListItem);
        String randDailyMitoUrl = Constant.getDailyMitoUrl(dailyMitoDid, dailyMitoLabelListId,
                dailyMitoGalleryparam);
        return randDailyMitoUrl;
    }

    public void openShareDialog() {

        int screenW = ScreenUtil.getScreenW(this);
        int height = (int) (ScreenUtil.getScreenH(this) * 0.43);
        View contentView = LayoutInflater.from(this).inflate(com.kapp.kinflow.R.layout.dialog_share_layout, null,
                false);


        int totalSize = 13;
        int pageSize = 6;//每页个数
        int pageNumber = totalSize / pageSize;//总页数
        int remainder = totalSize - pageNumber * pageSize;//余数
        ArrayList<ShareItemBean> pageDatas = new ArrayList<>(pageSize);
        int imageResId = com.kapp.kinflow.R.drawable.shared_icon_qq;
        int resultPageNumber = 0 == remainder ? pageNumber : pageNumber + 1;
        List<RecyclerView> recyclerViews = new ArrayList<>(resultPageNumber);
        int j = 1;
        for (int i = 0; i < totalSize; i++) {
            ShareItemBean bean = new ShareItemBean(imageResId, "分享" + i);
            if (i < j * pageSize) {
                pageDatas.add(bean);
                if (i == totalSize - 1) {//最后一个
                    RecyclerView recyclerView = getShareRecyclerView(pageSize, pageDatas);
                    recyclerViews.add(recyclerView);
                }
            } else {
                RecyclerView recyclerView = getShareRecyclerView(pageSize, pageDatas);
                recyclerViews.add(recyclerView);

                if (i == totalSize - 1 - remainder) {//最后一页
                    if (0 != remainder) {
                        pageDatas = new ArrayList<>(remainder);

                    } else {//凑整数
                        pageDatas = new ArrayList<>(pageSize);
                    }
                } else {
                    pageDatas = new ArrayList<>(pageSize);
                }
                pageDatas.add(bean);
                if (i == totalSize - 1) {//最后一个
                    recyclerView = getShareRecyclerView(pageSize, pageDatas);
                    recyclerViews.add(recyclerView);
                    break;
                }
                j++;
            }
        }
        ViewPager viewPager = (ViewPager) contentView.findViewById(com.kapp.kinflow.R.id.viewpager);
        CirclePageIndicator indicator = (CirclePageIndicator) contentView.findViewById(com.kapp.kinflow.R.id.indicator);
        RecyclerViewPagerAdapter adapter = new RecyclerViewPagerAdapter(recyclerViews);
        viewPager.setAdapter(adapter);
        indicator.setViewPager(viewPager);

        new ActionSheetDialog.Builder()
                .contentView(contentView)
                .gravity(Gravity.BOTTOM)
                .size(screenW, height)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .build().show();


    }

    private RecyclerView getShareRecyclerView(int pageSize, ArrayList<ShareItemBean> pageDatas) {
        return ShareRecyclerViewBuilder.newGridRecyclerView(this, pageDatas,
                pageSize / 2, new RecycleViewEvent.OnItemClickListener() {

                    @Override
                    public void onNormalItemClick(View view, int position) {

                    }

                    @Override
                    public void onHeaderItemClick(View view, SparseArrayCompat<View> headers, int
                            position) {

                    }

                    @Override
                    public void onFooterItemClick(View view, SparseArrayCompat<View> footers, int
                            position) {

                    }
                });
    }

    private int getDailyMitoLabelListId(List<Integer> dailyMitoLabelIdList, int currentdailyMitoLabelListItem) {
        if (null == dailyMitoLabelIdList || dailyMitoLabelIdList.isEmpty()) {
            return -1;
        }
        if (currentdailyMitoLabelListItem >= dailyMitoLabelIdList.size()) {
            currentdailyMitoLabelListItem = 0;
        } else if (currentdailyMitoLabelListItem < 0) {
            currentdailyMitoLabelListItem = dailyMitoLabelIdList.size() - 1;
        }
        return dailyMitoLabelIdList.get(currentdailyMitoLabelListItem);
    }

    private void handlerDailyMitoResult(Object simpleResponse) {
        if (null == simpleResponse) {
            LogUtils.e("每日美图响应失败，发生io错误");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(simpleResponse.toString());
            if (null == jsonObject) {
                return;
            }
            String statusString = jsonObject.getString("status");
            int status = Integer.parseInt(statusString);
            if (1 == status) {
                JSONArray data = jsonObject.getJSONArray("data");
                int length = data.length();
                if (null == dailyMitoCardBeanList) {
                    dailyMitoCardBeanList = new ArrayList<>(length);
                }
                for (int i = 0; i < length; i++) {
                    DailyMitoCardBean bean = new DailyMitoCardBean(data.getJSONObject(i));
                    dailyMitoCardBeanList.add(bean);
                }
                updateDailyMitoItemCard(currentDailyMitoCardItem, true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlerSiteNaviListResult(Object result) {
//        if (!fullResponse.isSuccessful()) {
//            LogUtils.e("导航地址响应失败。。。。");
//            showDefaultSiteNavi();
//            fullResponse.close();
//            return;
//        }
//        String resultString = null;
//        try {
//            resultString = fullResponse.body().string();
//        } catch (IOException e) {
//            e.printStackTrace();
//            LogUtils.e("导航地址响应失败，发生io错误");
//            showDefaultSiteNavi();
//            fullResponse.close();
//            return;
//        }
//        if (null == resultString || "".equals(resultString)) {
//            LogUtils.e("导航地址响应成功，数据返回空！！！");
//            showDefaultSiteNavi();
//            fullResponse.close();
//            return;
//        }
        if (null == result) {
            LogUtils.e("导航地址响应失败，发生io错误");
            showDefaultSiteNavi();
            return;
        }

        String rootJsonKey = "wnavs";
        try {
            JSONObject jsonObjectAll = new JSONObject(result.toString());
            if (null == jsonObjectAll) {
                return;
            }
            JSONArray jsonArray = jsonObjectAll.getJSONArray(rootJsonKey);
            int length = jsonArray.length();
            if (null == jsonArray || 0 == length) {
                return;
            }


            int naviCardSize = length / NAVI_CARD_ITEM_PAGE_SIZE;
            int remainder = length - naviCardSize;//余数
            int resultCardSize = naviCardSize;
            if (0 != remainder) {//有余数，加一页
                resultCardSize++;
            }
            if (null == naviCardBeanList) {
                naviCardBeanList = new ArrayList<>(resultCardSize);
            }

            List<SiteNavigationCardBean.SingleNaviBean> singleNaviBeanList = new ArrayList<>(NAVI_CARD_ITEM_PAGE_SIZE);
            for (int i = 0, j = 1; i < length && j <= resultCardSize; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                SiteNavigationCardBean.SingleNaviBean singleNaviBean = new SiteNavigationCardBean.SingleNaviBean
                        (jsonObject);
                singleNaviBeanList.add(singleNaviBean);
                if (i == j * NAVI_CARD_ITEM_PAGE_SIZE - 1) {
                    j++;
                    naviCardBeanList.add(new SiteNavigationCardBean(singleNaviBeanList));
                    if (j == resultCardSize && 0 != remainder) {//最后一页，并且有余数
                        singleNaviBeanList = new ArrayList<>(remainder);
                    } else {
                        singleNaviBeanList = new ArrayList<>(NAVI_CARD_ITEM_PAGE_SIZE);
                    }
                }
            }
            updateSiteNaviItemCard(currentNaviCardItem, true);

        } catch (JSONException e) {
            e.printStackTrace();
            showDefaultSiteNavi();
        } catch (Exception e) {
            e.printStackTrace();
            showDefaultSiteNavi();
        }

    }

    private void showDefaultSiteNavi() {
        List<SiteNavigationCardBean.SingleNaviBean> defaultNaviList = SiteNavigationCardBean.getDefaultNaviList();
        if (null != defaultNaviList && !defaultNaviList.isEmpty()) {
            int length = defaultNaviList.size();
            int naviCardSize = length / NAVI_CARD_ITEM_PAGE_SIZE;
            int remainder = length - naviCardSize;//余数
            int resultCardSize = naviCardSize;
            if (0 != remainder) {//有余数，加一页
                resultCardSize++;
            }
            if (null == naviCardBeanList) {
                naviCardBeanList = new ArrayList<>(resultCardSize);
            }
            List<SiteNavigationCardBean.SingleNaviBean> singleNaviBeanList = new ArrayList<>(NAVI_CARD_ITEM_PAGE_SIZE);
            for (int i = 0, j = 1; i < length && j <= resultCardSize; i++) {
                SiteNavigationCardBean.SingleNaviBean singleNaviBean = defaultNaviList.get(i);
                singleNaviBeanList.add(singleNaviBean);
                if (i == j * NAVI_CARD_ITEM_PAGE_SIZE - 1) {
                    j++;
                    naviCardBeanList.add(new SiteNavigationCardBean(singleNaviBeanList));
                    if (j == resultCardSize && 0 != remainder) {//最后一页，并且有余数
                        singleNaviBeanList = new ArrayList<>(remainder);
                    } else {
                        singleNaviBeanList = new ArrayList<>(NAVI_CARD_ITEM_PAGE_SIZE);
                    }
                }
            }
            updateSiteNaviItemCard(currentNaviCardItem, true);
        }
    }


    private void checkStoragePermission() {
//        int permissionGranted = PermissionUtil.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (permissionGranted == PackageManager.PERMISSION_GRANTED) {
//
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                PermissionUtil.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
// Manifest
//                                .permission.WRITE_EXTERNAL_STORAGE},
//                        CODE_STORAGE);
//            } else {
//
//            }
//        }
    }

    private void showShortToast(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[]
//            grantResults) {
//        switch (requestCode) {
//            case CODE_STORAGE:
//                if (grantResults.length > 0) {
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager
//                            .PERMISSION_GRANTED) {//存取权限获得
//
//                    } else {
//                        //存取权限禁止
//                        showShortToast("sdcard权限被禁止！！！");
//                    }
//                }
//                break;
//        }
//
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }


    private void doParentSearchClick(View view) {
        Intent intent = new Intent(this, com.kapp.kinflow.business.activity.SearchActivity.class);
        startActivity(intent);
    }

    private void doPacksClick(View view) {

    }

    private void onRefreshClick(View view) {
        changeHeaderLineCard(null);
//        changeConstellationCard(null);
//        changeGameNewsCard(null);
        changeHappyCard(null);
        changeRealTimeCard(null);
        changeSiteNaviCard(null);
    }

    private void doAddCardClick(View view) {
        Intent intent = new Intent(this, AddCardActivity.class);
        startActivity(intent);
    }


    @Override
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end, boolean forceAnimateIcons) {
        super.bindItems(shortcuts, start, end, forceAnimateIcons);
        // TODO: 2017/5/25 0025 这里处理头部应用

        int size = 8;
        int i = 0;
        List<HeaderAppBean> headerList = new ArrayList<>(size);
        for (ItemInfo item : shortcuts) {
            if (i >= size) {
                break;
            }
            if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || item.itemType ==
                    LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
                ShortcutInfo info = (ShortcutInfo) item;
                HeaderAppBean bean = new HeaderAppBean(info.getIcon(mIconCache), info.title.toString(), info
                        .getIntent());
                headerList.add(bean);
                i++;
            }
        }

        if (null != headerRecyclerview) {
            if (null != headerAdapter) {
                headerAdapter.updateAll(headerList);
            }
        }
    }

}
