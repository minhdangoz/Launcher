package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.NewsListAdapter;
import com.kapp.knews.base.recycler.data.NewsBaseData;
import com.kapp.knews.base.recycler.decoration.ItemDecorationDivider;
import com.kapp.knews.base.tabs.TabInfo;
import com.kapp.knews.common.LoadNewsType;
import com.kapp.knews.common.browser.KinflowBrower;
import com.kapp.knews.helper.content.resource.ValuesHelper;
import com.kapp.knews.helper.java.collection.CollectionsUtils;
import com.kapp.knews.repository.bean.DongFangTouTiao;
import com.kapp.knews.repository.bean.lejingda.klauncher.KlauncherAdvertisement;
import com.kapp.knews.repository.server.DongFangRequestManager;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * description：新闻列表页面
 * <br>author：caowugao
 * <br>time： 2017/04/24 17:08
 */

public class NewsListActivity extends Activity implements DongFangRequestManager.DongFangManagerListener,
        NewsListAdapter.OnNewsListItemClickListener {


    private static final boolean DEBUG = true;
    private static final String TAG = NewsListActivity.class.getSimpleName();
    @BindView(R2.id.container_back)
    LinearLayout containerBack;
    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;
    private static final String KEY_TAB_INFO = "key_tab_info";
    private static final String KEY_TITTLE = "key_tittle";
    @BindView(R2.id.empty_view)
    TextView emptyView;
    @BindView(R2.id.tv_tittle)
    TextView tvTittle;
    private TabInfo tabInfo;
    private DongFangRequestManager mDongFangRequestManager;
    private NewsListAdapter mNewsListAdapter = new NewsListAdapter(new LinkedList());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();

        initIntent();
        initViews();
        iniData();
    }

    private void iniData() {
        mDongFangRequestManager = new DongFangRequestManager(
                this,
                this
        );

        if (0 <= mNewsListAdapter.getList().size()) {
            mDongFangRequestManager.loadDongFang(LoadNewsType.TYPE_FIRST, tabInfo);
        }

        recyclerview.setAdapter(mNewsListAdapter);
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (null != intent) {
            tabInfo = intent.getParcelableExtra(KEY_TAB_INFO);
            String tittle = intent.getStringExtra(KEY_TITTLE);
            tvTittle.setText(tittle);
        }
    }

    public static void launch(Context context, TabInfo tabInfo, String tittle) {
        Intent intent = new Intent(context, NewsListActivity.class);
        intent.putExtra(KEY_TAB_INFO, tabInfo);
        intent.putExtra(KEY_TITTLE, tittle);
        context.startActivity(intent);
    }

    private void initViews() {
//        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//
//        int dp_5 = ScreenUnitUtil.asPx(5);
//        int color = ContextCompat.getColor(this, R.color.app_separator_line_color);
//        LinearDecoration divider = new LinearDecoration.Builder().color(color).size(1).margin(dp_5).build();
//        recyclerview.addItemDecoration(divider);

        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerview.addItemDecoration(new ItemDecorationDivider(this, LinearLayoutManager.VERTICAL));
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //获取LayoutManager
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                //获取可见的最后一个item的位置
                int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();
                //获取可见item的个数
                int visibleItemCount = layoutManager.getChildCount();
                //获取item的总数
                int totalItemCount = layoutManager.getItemCount();
                //
                if (visibleItemCount > 0
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItemPosition >= totalItemCount - 1
                        ) {
                    mNewsListAdapter.showFooter();
                    mDongFangRequestManager.loadDongFang(LoadNewsType.TYPE_LOAD_MORE, tabInfo);
                }
            }
        });

        mNewsListAdapter.setOnItemClickListener(this);
    }

    @OnClick(R2.id.container_back)
    public void setContainerBack(View view) {
        finish();
    }


    @Override
    public void onItemClick(View view, int position, boolean isPhoto) {

        NewsBaseData newsBaseData = (NewsBaseData) mNewsListAdapter.getList().get(position);
        String openUrl = "";
        switch (newsBaseData.getDataType()) {
            case NewsBaseData.TYPE_AD://广告类型：目前只有klauncher后台提供的两个广告
                switch (newsBaseData.getContentProvider()) {
                    case KlauncherAdvertisement.contentProvider:
                        KlauncherAdvertisement klauncherAdvertisement = (KlauncherAdvertisement) newsBaseData;
                        openUrl = klauncherAdvertisement.getmNewsBean().getClickUrl();
                        break;
                }
                break;
            case NewsBaseData.TYPE_NEWS://新闻类型:目前只有东方头条
                switch (newsBaseData.getContentProvider()) {
                    case DongFangTouTiao.contentProvider://新闻来源：东方头条
                        DongFangTouTiao dongFangTouTiao = (DongFangTouTiao) newsBaseData;
                        openUrl = dongFangTouTiao.getmNewsBean().getUrl();
                        break;
                }
                break;
        }

        KinflowBrower.openUrl(this, openUrl);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    private void checkIsEmpty(List<? extends NewsBaseData> newsBaseDataList) {
        if (newsBaseDataList == null && mNewsListAdapter.getList() == null) {
            recyclerview.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);

        } else {
            recyclerview.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showShortToast(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    @Override
    public void setNewsList(List<NewsBaseData> newsBaseDataList, int loadType) {
        switch (loadType) {
            case LoadNewsType.TYPE_LOAD_REFRESH_SUCCESS://下拉刷新成功
//                mSwipeRefreshLayout.setRefreshing(false);
//                //---
//                //---
//                mNewsListAdapter.addFirstAll(newsBaseDataList);//这里将来要改变，修改为addNewList
////                mNewsListAdapter.notifyDataSetChanged();
//                checkIsEmpty(newsBaseDataList);
                break;
            case LoadNewsType.TYPE_LOAD_REFRESH_ERROR://下拉刷新失败
//                mSwipeRefreshLayout.setRefreshing(false);
//                checkIsEmpty(newsBaseDataList);
                break;
            case LoadNewsType.TYPE_LOAD_MORE_SUCCESS://上拉加载更多成功
                mNewsListAdapter.hideFooter();
                if (CollectionsUtils.collectionIsNull(newsBaseDataList)) {
//                    mIsAllLoaded = true;
//                    Snackbar.make(mRecyclerView, ValuesHelper.getString(R.string.no_more), Snackbar.LENGTH_SHORT)
//                            .show();
                    showShortToast(ValuesHelper.getString(R.string.no_more));
                } else {

                    //---
                    //---
                    mNewsListAdapter.addMore(newsBaseDataList);
                }
                break;
            case LoadNewsType.TYPE_LOAD_MORE_ERROR://上拉加载更多失败
                mNewsListAdapter.hideFooter();
                break;
        }

    }
}
