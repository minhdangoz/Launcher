package com.kapp.kinflow;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.kapp.kinflow.business.activity.AddCardActivity;
import com.kapp.kinflow.business.activity.SearchActivity;
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
import com.kapp.kinflow.business.util.PermissionUtil;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class KinflowCardActivity extends Activity implements OKHttpUtil.OnRequestListener ,IKinflowCardCallback {
    private static final int CODE_STORAGE = 100;
    private static final boolean DEBUG = true;
    private static final String TAG = KinflowCardActivity.class.getSimpleName();
    private static final int REQUEST_CODE_NAVI_LIST = 101;
    private static final int REQUEST_CODE_DAILY_MITO_CTROL = 102;
    private static final int REQUEST_CODE_DAILY_MITO = 103;
    private static final int REQUEST_CODE_BAND_OFFERS = 104;
    private static final int REQUEST_CODE_NOVEL_RECOMMEND = 105;
    @BindView(R2.id.ptrFrame)
    PullToRefreshView ptrFrame;
    @BindView(R2.id.header_recyclerview)
    RecyclerView headerRecyclerview;
    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R2.id.parent_normal_search)
    View parentNormalSearch;
    @BindView(R2.id.imb_packs)
    View imbPacks;
    @BindView(R2.id.tv_refresh)
    TextView tvRefresh;
    @BindView(R2.id.tv_add_card)
    TextView tvAddCard;
    @BindView(R2.id.nest_scrollview)
    NestedScrollView nestScrollview;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kinflow_card);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();
        initViews();
        initData();

        checkStoragePermission();
    }

    private void checkStoragePermission() {
        int permissionGranted = PermissionUtil.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionGranted == PackageManager.PERMISSION_GRANTED) {

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermissionUtil.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest
                                .permission.WRITE_EXTERNAL_STORAGE},
                        CODE_STORAGE);
            } else {

            }
        }
    }

    private void showShortToast(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[]
            grantResults) {
        switch (requestCode) {
            case CODE_STORAGE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager
                            .PERMISSION_GRANTED) {//存取权限获得

                    } else {
                        //存取权限禁止
                        showShortToast("sdcard权限被禁止！！！");
                    }
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick(R2.id.parent_normal_search)
    public void doParentSearchClick(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    @OnClick(R2.id.imb_packs)
    public void doPacksClick(View view) {

    }

    @OnClick(R2.id.tv_refresh)
    public void onRefreshClick(View view) {
        changeHeaderLineCard(null);
//        changeConstellationCard(null);
//        changeGameNewsCard(null);
        changeHappyCard(null);
        changeRealTimeCard(null);
        changeSiteNaviCard(null);
    }

    @OnClick(R2.id.tv_add_card)
    public void onAddCardClick(View view) {
        Intent intent = new Intent(this, AddCardActivity.class);
        startActivity(intent);
    }

    //
//    private void initData() {
//
//
//        //顶部app应用
//        int size = 8;
//        List<HeaderAppBean> headerList = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            headerList.add(new HeaderAppBean(null, "应用" + i, null));
//        }
//        IItemFactory factory = new HeaderAppItemFactory();
//        RecycleViewCommonAdapter adapter = new RecycleViewCommonAdapter(headerList, headerRecyclerview, factory);
//        headerRecyclerview.setAdapter(adapter);
//
//
//        //信息流开始
//        size = 2;
//        List<BaseItemBean> contentList = new ArrayList<>(size);
//
//        //头条资讯
//        contentList.add(new HeadlineCardBean("image" + 0, "contentTile" + 0, "content" + 0, "secondTittle" + 0,
//                "thirdTittle" + 0));
//
//        //热门游戏
////        String imageUrl="https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3064148438,1777643699&fm=11&gp=0
//// .jpg";
//        size = 8;
//        List<HotGameCardBean.SingleHotGameBean> singleGameList = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            singleGameList.add(new HotGameCardBean.SingleHotGameBean(null, "游戏" + i, HotGameCardBean.SingleHotGameBean
//                    .STATUS_NOT_DOWNLOAD));
//        }
//        HotGameCardBean hotGameCardBean = new HotGameCardBean(singleGameList);
//        contentList.add(hotGameCardBean);
//
//        factory = new MainContentItemFactory();
//        adapter = new RecycleViewCommonAdapter(contentList, recyclerview, factory);
//        recyclerview.setAdapter(adapter);
//
////        testHttp();
//
//    }
    private void initData() {


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
            showShortToast(getString(R.string.no_data));
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
            showShortToast(getString(R.string.no_data));
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
            showShortToast(getString(R.string.no_data));
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
            showShortToast(getString(R.string.no_data));
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
            showShortToast(getString(R.string.no_data));
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
        TextViewUtil.setDrawableLeft(tvRefresh, R.drawable.market_search_guess_refresh, drawableLeftWidth,
                drawableLeftHeight);
        TextViewUtil.setDrawableLeft(tvAddCard, R.drawable.navigation_add_card_btn, drawableLeftWidth,
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

    public void scrollToTop() {
        scrollToPositionWithOffset(0, 0);
    }

    public void stickCardItemToTop(int position) {
        contentAdapter.exchange(position, 0);
    }

    @Override
    public String getDailyMitoMoreUrl() {
        return null;
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
    protected void onDestroy() {
        super.onDestroy();
        OKHttpUtil.cancel(REQUEST_CODE_NAVI_LIST);
        OKHttpUtil.cancel(REQUEST_CODE_DAILY_MITO_CTROL);
        OKHttpUtil.cancel(REQUEST_CODE_DAILY_MITO);
        OKHttpUtil.cancel(REQUEST_CODE_BAND_OFFERS);
        OKHttpUtil.cancel(REQUEST_CODE_NOVEL_RECOMMEND);
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
        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_share_layout, null,
                false);


        int totalSize = 13;
        int pageSize = 6;//每页个数
        int pageNumber = totalSize / pageSize;//总页数
        int remainder = totalSize - pageNumber * pageSize;//余数
        ArrayList<ShareItemBean> pageDatas = new ArrayList<>(pageSize);
        int imageResId = R.drawable.shared_icon_qq;
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
        ViewPager viewPager = (ViewPager) contentView.findViewById(R.id.viewpager);
        CirclePageIndicator indicator = (CirclePageIndicator) contentView.findViewById(R.id.indicator);
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
}
