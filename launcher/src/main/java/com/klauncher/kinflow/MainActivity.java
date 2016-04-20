package com.klauncher.kinflow;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.internal.widget.CompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.klauncher.kinflow.cards.CardsListManager;
import com.klauncher.kinflow.cards.adapter.CardItemDecoration;
import com.klauncher.kinflow.cards.adapter.CardsAdapter;
import com.klauncher.kinflow.cards.manager.CardContentManagerFactory;
import com.klauncher.kinflow.cards.manager.MainControl;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.utils.CacheLocation;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.navigation.adapter.NavigationAdapter;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.SearchActivity;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.search.model.HotWordItemDecoration;
import com.klauncher.kinflow.utilities.Dips;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.weather.model.Weather;
import com.klauncher.kinflow.weather.service.LocationService;
import com.klauncher.launcher.R;

import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, MainControl.ResponseListener {


    //    ArrayList<HotWord> hotWordArrayList;
    TextView tv_temperature;
    TextView tv_city;
    TextView tv_weather;
    ImageView iv_weatherType;
    TextView tv_searchHint;
    CompatTextView tv_hotWord1;
    CompatTextView tv_hotWord2;
    ImageView iv_refresh;
    RelativeLayout randomNewsLine;
    RecyclerView navigationRecyclerView;

    PullToRefreshScrollView mPullRefreshScrollView;
    ScrollView mScrollView;
    RecyclerView mCardsView;

    private HotWord hotWord1 = HotWord.getDefaultHotWord1();
    private HotWord hotWord2 = HotWord.getDefaultHotWord2();
    private HotWord hintHotWord;
    private MainControl mMainControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        CardsListManager.getInstance().init(this);
        init();

        //异步获取所有
        startService(new Intent(MainActivity.this, LocationService.class));
        mMainControl = new MainControl(MainActivity.this, this);
        mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
                MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,
                MessageFactory.MESSAGE_WHAT_OBTAION_CARD);
        //注册监听
        CacheNavigation.getInstancce().registerOnSharedPreferenceChangeListener(this);
        CacheLocation.getInstance(MainActivity.this).registerOnSharedPreferenceChangeListener(this);
        //初始化下拉刷新
        mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
        mPullRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                //当下拉刷新的时候
                mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
                        MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,
                        MessageFactory.MESSAGE_WHAT_OBTAION_CARD);
            }
        });

        mScrollView = mPullRefreshScrollView.getRefreshableView();
        mScrollView.setFillViewport(true);
        mPullRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//关闭加载更多
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    private void initView() {
        tv_temperature = (TextView) findViewById(R.id.temperature);
        tv_city = (TextView) findViewById(R.id.city);
        tv_weather = (TextView) findViewById(R.id.weather);
        iv_weatherType = (ImageView) findViewById(R.id.weather_type);
        tv_searchHint = (TextView) findViewById(R.id.search_hint);
        tv_hotWord1 = (CompatTextView) findViewById(R.id.hot_word_1);
        tv_hotWord2 = (CompatTextView) findViewById(R.id.hot_word_2);
        iv_refresh = (ImageView) findViewById(R.id.refresh_hotWord);
        randomNewsLine = (RelativeLayout) findViewById(R.id.random_news_line);
        navigationRecyclerView = (RecyclerView) findViewById(R.id.navigation_recyclerView);
        mCardsView = (RecyclerView) findViewById(R.id.scroll_view_cards);
    }

    //初始化数据---default data
    public void init() {
        tv_hotWord1.setText(hotWord1.getWord());
        tv_hotWord2.setText(hotWord2.getWord());
        navigationRecyclerView.setLayoutManager(new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false));
        List<Navigation> navigationList = CacheNavigation.getInstancce().getAll();
        Collections.sort(navigationList);
        navigationRecyclerView.setAdapter(new NavigationAdapter(MainActivity.this, navigationList));
        navigationRecyclerView.addItemDecoration(new HotWordItemDecoration(16, 32, false));

//        CardsListManager.getInstance().loadCardList();
        mCardsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mCardsView.addItemDecoration(new CardItemDecoration(Dips.dipsToIntPixels(12, this)));

//        mCardInfoList = CardsListManager.getInstance().getInfos();
        mCardsView.setAdapter(new CardsAdapter(this, null));//最初传入空,等待收到数据后更新
    }


    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.hot_word_1://热词1监听
                CommonUtils.getInstance().openHotWord(this, hotWord1.getUrl());
                break;
            case R.id.hot_word_2://热词2监听
                CommonUtils.getInstance().openHotWord(this, hotWord2.getUrl());
                break;
            case R.id.refresh_hotWord://刷新监听
//                asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
                //此处传入mCardInfoList无用
                mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
                break;
            case R.id.search_hint://搜索框监听
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra(SearchActivity.HITN_HOT_WORD_KEY, hintHotWord);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CardContentManagerFactory.clearAllOffset();
        CacheNavigation.getInstancce().unregisterOnSharedPreferenceChangeListener(this);
        CacheLocation.getInstance(MainActivity.this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("MainActivity", "收到缓存数据变更通知 key=" +
                key);
        if (key.equals(CacheLocation.KEY_LAT_LNG)) {//位置信息变更----->驱动天气数据变更
            mMainControl.obtainCityName();
        } else {//Navigation变更

        }
    }

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
        mPullRefreshScrollView.onRefreshComplete();
    }

    @Override
    public void onHotWordUpdate(List<HotWord> hotWordList) {
        //更新热词
        if (null != hotWordList && hotWordList.size() != 0) {
            log("==========更新热词===========");
            hintHotWord = hotWordList.get(0);
            tv_searchHint.setHint(hintHotWord.getWord());
            hotWord1 = hotWordList.get(1);
            tv_hotWord1.setText(hotWord1.getWord());
            hotWord2 = hotWordList.get(2);
            tv_hotWord2.setText(hotWord2.getWord());
        }
    }

    @Override
    public void onNavigationUpdate(List<Navigation> navigationList) {
        //更新导航页
        if (null != navigationList && navigationList.size() != 0) {
            log("==========更新导航页===========");
            NavigationAdapter adapter = (NavigationAdapter) navigationRecyclerView.getAdapter();
            adapter.updateNavigationList(navigationList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCardInfoUpdate(List<CardInfo> cardInfoList) {
        //更新CardsAdapter
        if (null != cardInfoList&&cardInfoList.size()!=0) {
            log("==========更新CardsAdapter===========");
            CardsAdapter cardsAdapter = (CardsAdapter) mCardsView.getAdapter();
            cardsAdapter.updateCards(cardInfoList);
        }
    }

    final protected static void log(String msg) {
        KinflowLog.i(msg);
    }
}
