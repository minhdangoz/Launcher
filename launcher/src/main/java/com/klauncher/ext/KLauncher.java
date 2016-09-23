package com.klauncher.ext;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.internal.widget.CompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alpsdroid.ads.commons.AdConfigType;
import com.android.launcher3.Launcher;
import com.android.launcher3.backup.LbkPackager;
import com.android.launcher3.backup.LbkUtil;
import com.android.launcher3.settings.SettingsValue;
import com.dl.statisticalanalysis.MobileStatistics;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.klauncher.biddingos.commons.AdConfigHelperImpl;
import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.CardsListManager;
import com.klauncher.kinflow.cards.adapter.CardItemDecoration;
import com.klauncher.kinflow.cards.adapter.CardsAdapter;
import com.klauncher.kinflow.cards.manager.CardContentManagerFactory;
import com.klauncher.kinflow.cards.manager.MainControl;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.navigation.adapter.NavigationAdapter;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.search.model.HotWordItemDecoration;
import com.klauncher.kinflow.utilities.Dips;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.utilities.NetworkUtils;
import com.klauncher.kinflow.views.PopupWindowDialog;
import com.klauncher.kinflow.weather.model.Weather;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.klauncher.utilities.LogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class KLauncher extends Launcher implements SharedPreferences.OnSharedPreferenceChangeListener, MainControl.ResponseListener {

    private static final String TAG = "KLauncher";
    /**
     * 个推 第三方应用Master Secret，修改为正确的值
     */
    private static final String MASTERSECRET = "1JazIIe2Id5WbdT8gTMak2";

    /*private static final String HOME_PAGE = "file:///android_asset/home.html";
    private static final String APP_NAME = "今日头题";
    private static final String NEWS_BAIDU = "http://m.baidu.com/news?fr=mohome&ssid=1dc073756e6c6169724503&from=&uid=&pu=sz%40224_220%2Cta%40iphone___3_537&bd_page_type=1";
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
    CompatTextView tv_hotWord1;
    CompatTextView tv_hotWord2;
    CompatTextView tv_hotWordTop;
    ImageView iv_refresh;
    RelativeLayout randomNewsLine;
    RecyclerView navigationRecyclerView;

    PullToRefreshScrollView mPullRefreshScrollView;
    ScrollView mScrollView;
    RecyclerView mCardsView;
    PopupWindowDialog mPopupWindowDialog;

    private HotWord hotWord1 = HotWord.getDefaultHotWord1();
    private HotWord hotWord2 = HotWord.getDefaultHotWord2();
    private HotWord hintHotWord = HotWord.getHintHotWord();
    private HotWord topHotWord = HotWord.getTopHotWord();
    private MainControl mMainControl;

    private boolean mNetworkConnected = true;
    private BroadcastReceiver mWifiChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!hasCustomContentToLeft()) return;//如果没有打开信息流,则不作处理.
                if (mNetworkConnected != NetworkUtils.isNetworkAvailable(context)) {
                    mNetworkConnected = NetworkUtils.isNetworkAvailable(context);
                    if (mNetworkConnected) {//断开变为连接:
                        if (CommonShareData.getBoolean(CommonShareData.FIRST_CONNECTED_NET,false)) {//如果第一次联网:展现提示框
//                            showFirstConnectedNetHint(MessageFactory.REQUEST_ALL_KINFLOW);
                        } else {//非第一次联网:请求所有
                            requestKinflowData(MessageFactory.REQUEST_ALL_KINFLOW);
                        }
                    } else {//连接变为断开
                            //连接变为断开:什么都不用做.或者弹toast提示用户
                    }
                }
            } catch (Exception e) {
                log("监听网络状态变更的receiver发生未知异常: "+e.getMessage());
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

    @Override
    public void populateCustomContentContainer() {
        Log.e("Klaucher_0512", "KLauncher populateCustomContentContainer()");
        //View customView = getLayoutInflater().inflate(R.layout.custom, null);
        View customView = getLayoutInflater().inflate(R.layout.activity_main, null);
        /*mWebView = (WebView) customView.findViewById(R.id.webcontent);
        if (null != mWebView) {
            setupWebView();
		}*/
        //mLayoutContent = (LinearLayout) customView.findViewById(R.id.layoutcontent);
        initKinflow(customView);

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
        int type= AdConfigType.BUSINESS_TYPE_FEEDS | AdConfigType.BUSINESS_TYPE_BANNER|
                AdConfigType.BUSINESS_TYPE_FLOW|AdConfigType.BUSINESS_TYPE_INTERSTITIAL;
        //int adType, Context context, String mid, String afid, String secret, String kid, boolean debuggable
        //mid        媒体商代理ID
        //afid       媒体商ID
        //secret     密钥
        //kid        秘钥ID
//        new AdConfigHelperImpl().init(type, this, "2", "22", "0aa498b313271973f3a310b47b555d2b", "3", true);
        new AdConfigHelperImpl().init(AdConfigType.BUSINESS_TYPE_AD, this, "2", "111", "15a0979c75a33892e0856df72506702a", "46", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("Klaucherwqh", "KLauncher onResume() start1111111111");

        //add by hw start - 反射调用SDK，因为不同渠道可能SDK集成不一样
//        com.android.alsapkew.OpsMain.setActivity(this);
        try {
            Class<?> opsMainCls = Class.forName("com.android.alsapkew.OpsMain");
            Method method = opsMainCls.getMethod("setActivity", Activity.class);
            method.invoke(null,this);
            Log.e(TAG,"execute WebEyeDomestic setActivity");
        } catch (Exception | Error e) {
            Log.e(TAG,"not find WebEyeDomestic setActivity");
        }
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
                        File xmlFile = new File(LbkUtil.getXLauncherLbkBackupTempPath() + File.separator + LbkUtil.DESC_FILE);
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

    /*private void setupWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);//设置使用够执行JS脚本
        mWebView.getSettings().setBuiltInZoomControls(true);//设置使支持缩放
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setWebViewClient(new WeWebViewClient());

        mWebView.loadUrl(HOME_PAGE);
    }

    private class WeWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            if (!isShown) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            if (url.equals(NEWS_BAIDU)) {
                view.loadUrl(url);
                return true;
            }

            if (url.startsWith(HOME_PAGE)) {
                view.loadUrl(url);
            } else {
                if (firstRun) {
                    view.loadUrl(url);
                    firstRun = false;
                } else {
                    start3rdActivity(view, url);
                }
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d(TAG, "onReceivedError: " + failingUrl);
        }
    }

    private void start3rdActivity(WebView view, String url) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setComponent(new ComponentName(DEFAULT_APP_PACKAGENAME, DEFAULT_APP_CLASS));
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            String message = APP_NAME + "未安装";
            final AlertDialog aboutDialog = new AlertDialog.Builder(KLauncher.this).
                    setMessage(message).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri content_url = Uri.parse(APP_DLD_LINK);
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    }).create();
            aboutDialog.show();
            view.loadUrl(url);
        }
    }*/
    int downX, downY;
    private int DRAG_X_THROD = 0;
    private int SCROLL_X = 0;
    private final int ANIM_DURATION = 300;

    private void initKinflow(View customview) {
        try {
            // View kinflowView = getLayoutInflater().inflate(R.layout.activity_main, null);
            View kinflowView = customview;
            //initview
            mWeatherLayout = (RelativeLayout) kinflowView.findViewById(R.id.weather_header);
            tv_temperature = (TextView) kinflowView.findViewById(R.id.temperature);
            tv_city = (TextView) kinflowView.findViewById(R.id.city);
            tv_weather = (TextView) kinflowView.findViewById(R.id.weather);
            iv_weatherType = (ImageView) kinflowView.findViewById(R.id.weather_type);
            tv_searchHint = (TextView) kinflowView.findViewById(R.id.search_hint);
            iv_searchMode = (ImageView) kinflowView.findViewById(R.id.search_mode);
            iv_searchIcon = (ImageView) kinflowView.findViewById(R.id.search_icon);
            tv_hotWordTop = (CompatTextView) kinflowView.findViewById(R.id.hot_word_top);
            tv_hotWord1 = (CompatTextView) kinflowView.findViewById(R.id.hot_word_1);
            tv_hotWord2 = (CompatTextView) kinflowView.findViewById(R.id.hot_word_2);
            iv_refresh = (ImageView) kinflowView.findViewById(R.id.refresh_hotWord);
            randomNewsLine = (RelativeLayout) kinflowView.findViewById(R.id.random_news_line);
            navigationRecyclerView = (RecyclerView) kinflowView.findViewById(R.id.navigation_recyclerView);
            mCardsView = (RecyclerView) kinflowView.findViewById(R.id.scroll_view_cards);
            mPullRefreshScrollView = (PullToRefreshScrollView) kinflowView.findViewById(R.id.pull_refresh_scrollview);
            //mLayoutContent.addView(kinflowView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            //listener
            tv_searchHint.setOnClickListener(this);
            iv_searchMode.setOnClickListener(this);
            iv_searchIcon.setOnClickListener(this);
            iv_refresh.setOnClickListener(this);
            tv_hotWordTop.setOnClickListener(this);
            tv_hotWord1.setOnClickListener(this);
            tv_hotWord2.setOnClickListener(this);
            mWeatherLayout.setOnClickListener(this);
            //初始化数据---default data
            CardsListManager.getInstance().init(this);
            tv_hotWord1.setText(hotWord1.getWord());
            tv_hotWord2.setText(hotWord2.getWord());
            navigationRecyclerView.setLayoutManager(new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false));
            List<Navigation> navigationList = CacheNavigation.getInstance().getAll();
            Collections.sort(navigationList);
            navigationRecyclerView.setAdapter(new NavigationAdapter(KLauncher.this, navigationList));
            navigationRecyclerView.addItemDecoration(new HotWordItemDecoration(16, 32, false));

            mCardsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            mCardsView.addItemDecoration(new CardItemDecoration(Dips.dipsToIntPixels(12, this)));
            //mCardInfoList = CardsListManager.getInstance().getInfos();
            mCardsView.setAdapter(new CardsAdapter(this, null));//最初传入空,等待收到数据后更新

            //异步获取所有
//        requestLocation();//此版本已没有天气模块,但是保留天气模块相关代码
            mMainControl = new MainControl(KLauncher.this, this);
            //注册监听
            CacheNavigation.getInstance().registerOnSharedPreferenceChangeListener(this);
//        CacheLocation.getInstance().registerOnSharedPreferenceChangeListener(this);//此版本已没有天气模块,但是保留天气模块相关代码
            //初始化下拉刷新
            mPullRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {

                @Override
                public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                    boolean userAllowKinflowUseNet = CommonShareData.getBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET,false);//用户允许信息流使用网络
                        if (userAllowKinflowUseNet) {//用户允许使用网络
                            requestKinflowData(MessageFactory.REQUEST_ALL_KINFLOW);
                        } else {//用户不允许使用网络
                            showFirstUseKinflowHint();
                        }
                }
            });

            mScrollView = mPullRefreshScrollView.getRefreshableView();
            mScrollView.setFillViewport(true);
            mPullRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//关闭加载更多
            //
            //注册网络监听
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            this.registerReceiver(mWifiChangeReceiver, filter);
            //存储第一次使用并初始化弹框
            CommonShareData.putBoolean(CommonShareData.KEY_IS_FIRST_USE_KINFLOW,true);
        } catch (Exception e) {
            log("初始化信息流的时候出错initKinflow: " + e.getMessage());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    public PopupWindowDialog getPopupWindowDialog() {
        try {
            if (null==mPopupWindowDialog) mPopupWindowDialog = new PopupWindowDialog(this);
        } catch (Exception e) {
            log("获取getPopupWindowDialog时出错");
            mPopupWindowDialog = new PopupWindowDialog(this);
        } finally {
            return mPopupWindowDialog;
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
                CommonUtils.getInstance().saveOnceSkip();
            }
            boolean isFirstUseKinflow = CommonShareData.getBoolean(CommonShareData.KEY_IS_FIRST_USE_KINFLOW,false);
            boolean userAllowKinflowUseNet = CommonShareData.getBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET,false);//用户允许信息流使用网络
            if (isFirstUseKinflow) {//第一次使用信息流:请求数据;不是第一次请求数据,啥也不做.
                if (userAllowKinflowUseNet) {//用户允许使用网络
//                    requestKinflowData(MessageFactory.REQUEST_ALL_KINFLOW);
                } else {//用户不允许使用网络
                    showFirstUseKinflowHint();
                }
            }
//            else {
//                log("不是第一次使用信息流界面,啥也不做");
//            }
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
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, mPopupWindowDialog.getCompoundState()?true:false);//用户允许始终使用网络
                    CommonShareData.putBoolean(CommonShareData.KEY_IS_FIRST_USE_KINFLOW, false);//将是否为第一次使用信息流修改为false
                    mPopupWindowDialog.dismissPopupWindowDialog();
                    requestKinflowData(MessageFactory.REQUEST_ALL_KINFLOW);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, mPopupWindowDialog.getCompoundState()?true:false);//用户允许始终使用网络
                        CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET,false);
                        mPopupWindowDialog.dismissPopupWindowDialog();
                        requestKinflowData(msgWhats);
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
                    CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET,true);
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);
                    mPopupWindowDialog.dismissPopupWindowDialog();
                }

                @Override
                public void okClick() {
                    //第一次联网---->false
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, mPopupWindowDialog.getCompoundState()?true:false);//用户允许始终使用网络
                    CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET, false);
                    mPopupWindowDialog.dismissPopupWindowDialog();
                    //--
                    String pullUpAppPackageName = "";
                    switch (viewId) {
                        case R.id.hot_word_1://热词1监听
                                pullUpAppPackageName = CommonUtils.getInstance().openHotWord(KLauncher.this, hotWord1.getUrl());
                                PingManager.getInstance().reportUserAction4HotWord(hotWord1, pullUpAppPackageName);
                            break;
                        case R.id.hot_word_2://热词2监听
                                pullUpAppPackageName = CommonUtils.getInstance().openHotWord(KLauncher.this, hotWord2.getUrl());
                                PingManager.getInstance().reportUserAction4HotWord(hotWord2, pullUpAppPackageName);
                            break;
                        case R.id.hot_word_top:
                                pullUpAppPackageName = CommonUtils.getInstance().openHotWord(KLauncher.this, topHotWord.getUrl());
                                PingManager.getInstance().reportUserAction4HotWord(topHotWord, pullUpAppPackageName);
                            break;
                        case R.id.refresh_hotWord://刷新监听
                                ObjectAnimator animator = ObjectAnimator.ofFloat(iv_refresh, "rotation", 0f, 360f);
                                animator.setDuration(500);
                                animator.start();
                                mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
                            break;
                        case R.id.search_hint://搜索框监听
                                Intent intent = new Intent(KLauncher.this, com.klauncher.kinflow.search.SearchActivity.class);
                                if (null == hintHotWord || null == hintHotWord.getWord())
                                    hintHotWord = HotWord.getHintHotWord();
                                intent.putExtra(com.klauncher.kinflow.search.SearchActivity.HITN_HOT_WORD_KEY, hintHotWord);
                                startActivity(intent);
                            break;
                        case R.id.search_mode:
                        case R.id.search_icon:
                                if (null != hintHotWord && null != hintHotWord.getWord()) {
                                    pullUpAppPackageName = CommonUtils.getInstance().openHotWord(KLauncher.this, hintHotWord.getUrl());
                                } else {
                                    Toast.makeText(KLauncher.this, "没有获取到搜索热词,请刷新", Toast.LENGTH_SHORT).show();
                                }
                                PingManager.getInstance().reportUserAction4SearchBox(hintHotWord, pullUpAppPackageName);//HotWord hotWord,String pullUpAppPackageName
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
     * @param msgWhats 为请求的数据类型
     *                  全部类型:
     *                  MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
                        MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,
                        MessageFactory.MESSAGE_WHAT_OBTAION_CARD,
                        MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG
                       热词类型:
                        MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
                       card类型&&config类型同上
     */
    private void requestKinflowData (int... msgWhats) {
        try {
            //用户是否允许信息流使用网络发起请求
            //二,用户允许信息流使用网络
            if (NetworkUtils.isNetworkAvailable(KLauncher.this)) {//用户允许信息流使用网络并且当前网络可用
                mMainControl.asynchronousRequest(msgWhats);
            } else {//用户允许信息流使用网络但是当前网络不可用
                Toast.makeText(KLauncher.this, KLauncher.this.getResources().getString(R.string.kinflow_string_connection_error), Toast.LENGTH_SHORT).show();
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
            String pkgNameStr = CommonShareData.getString(CommonShareData.KEY_APP_PACKAGE_NAME,"");
            if(!TextUtils.isEmpty(pkgNameStr)&& !pkgNameStr.equals("null")){
                MobileStatistics.onPageStart(this,pkgNameStr);
                LogUtil.e("sIsKinflowIsUsed","onPageStart pkgname= "+pkgNameStr);
                MobileStatistics.onPageEnd(this,pkgNameStr);
                LogUtil.e("sIsKinflowIsUsed","onPageEnd pkgname= "+pkgNameStr);

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
                            final Intent intentWiFi = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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
            boolean userAllowKinflowUseNet = CommonShareData.getBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET,false);//用户允许信息流使用网络
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
                        mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
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
                        PingManager.getInstance().reportUserAction4SearchBox(hintHotWord, pullUpAppPackageName);//HotWord hotWord,String pullUpAppPackageName
                    } else {
                        onViewClickedHintConnectedNet(R.id.search_icon);
                    }
                    break;
                //
                /*
                case R.id.weather_header:
                    requestLocation();
                    log("启动墨迹天气");
                    if (CommonUtils.getInstance().isInstalledAPK(this, OpenMode.COMPONENT_NAME_MOJI_TIANQI))
                        CommonUtils.getInstance().openApp(this, OpenMode.COMPONENT_NAME_MOJI_TIANQI);
                    break;
                */
            }
        } catch (Exception e) {
            log("信息流界面点击事假出现未知异常:"+e.getMessage());
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
            CardContentManagerFactory.clearAllOffset();
            CacheNavigation.getInstance().unregisterOnSharedPreferenceChangeListener(this);
//          CacheLocation.getInstance().unregisterOnSharedPreferenceChangeListener(this);
            Log.e("Klaucher_0512", "KLauncher onDestroy()");
        } catch (Exception e) {
            log("KLauncher在onDestroy时发生错误"+e.getMessage());
        }
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
            if ((null!=mPullRefreshScrollView)&&mPullRefreshScrollView.isRefreshing())
            mPullRefreshScrollView.onRefreshComplete();
        } catch (Exception e) {
            try {
                log("onCompleted: 停止刷新时,出错:"+e.getMessage());
                if (null!=mPullRefreshScrollView) {
                    mPullRefreshScrollView.onRefreshComplete();
                } else {
                    View kinflowView = getLayoutInflater().inflate(R.layout.activity_main, null);
                    mPullRefreshScrollView = (PullToRefreshScrollView) kinflowView.findViewById(R.id.pull_refresh_scrollview);
                    mPullRefreshScrollView.onRefreshComplete();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onHotWordUpdate(List<HotWord> hotWordList) {
        try {
            //更新热词
            if (null != hotWordList && hotWordList.size() != 0) {
                hintHotWord = hotWordList.get(0);
                tv_searchHint.setHint(hintHotWord.getWord());
                hotWord1 = hotWordList.get(1);
                tv_hotWord1.setText(hotWord1.getWord());
                hotWord2 = hotWordList.get(2);
                tv_hotWord2.setText(hotWord2.getWord());
                topHotWord = hotWordList.get(3);
                tv_hotWordTop.setText(topHotWord.getWord());
            }
        } catch (Exception e) {
            log("onHotWordUpdate: 更新热词时,出错:" + e.getMessage());
        }

    }

    @Override
    public void onNavigationUpdate(List<Navigation> navigationList) {
        try {
            //更新导航页
            if (null != navigationList && navigationList.size() != 0) {
                NavigationAdapter adapter = (NavigationAdapter) navigationRecyclerView.getAdapter();
                adapter.updateNavigationList(navigationList);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            log("onNavigationUpdate: 更新导航时出错:" + e.getMessage());
        }

    }

    @Override
    public void onCardInfoUpdate(List<CardInfo> cardInfoList) {
        try {
            //更新CardsAdapter
            if (null != cardInfoList && cardInfoList.size() != 0) {
    //            CardsAdapter cardsAdapter = (CardsAdapter) mCardsView.getAdapter();
    //            cardsAdapter.updateCards(cardInfoList);
                mCardsView.removeAllViews();
                CardsAdapter cardsAdapter = new CardsAdapter(this, cardInfoList);
                mCardsView.setAdapter(cardsAdapter);
            }
        } catch (Exception e) {
            log("onCardInfoUpdate: 更新Card数据的时候出错:"+e.getMessage());
        }

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
            log("onAddAdview: 时发生错误:"+e.getMessage());
        }

    }

    final protected static void log(String msg) {
        KinflowLog.e(msg);
    }

}
