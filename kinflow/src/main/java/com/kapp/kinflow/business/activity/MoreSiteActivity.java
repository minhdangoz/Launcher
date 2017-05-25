package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.MoreSiteHeaderAppItemFactory;
import com.kapp.kinflow.business.adapter.MoreSiteItemFactory;
import com.kapp.kinflow.business.beans.HeaderAppBean;
import com.kapp.kinflow.business.beans.MoreSiteContentItemBean;
import com.kapp.kinflow.view.DetailRightLayout;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.layoutmanager.FullyGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * description：网址大全页面
 * <br>author：caowugao
 * <br>time： 2017/04/27 16:18
 */

public class MoreSiteActivity extends Activity {

    @BindView(R2.id.container_back)
    LinearLayout containerBack;
    @BindView(R2.id.header_recyclerview)
    RecyclerView headerRecyclerview;
    @BindView(R2.id.iv_change)
    ImageView ivChange;
    @BindView(R2.id.parent_category)
    RelativeLayout parentCategory;
    @BindView(R2.id.content_recyclerview)
    RecyclerView contentRecyclerview;
    @BindView(R2.id.nestScrollview)
    NestedScrollView nestScrollview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_site);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();

        initViews();
        initData();
    }

    @OnClick(R2.id.container_back)
    public void onContainerBackClick(View view) {
        finish();
    }

    @OnClick(R2.id.parent_category)
    public void onParentCategoryClick(View view) {
        int visibility = contentRecyclerview.getVisibility();
        if (visibility == View.VISIBLE) {
            contentRecyclerview.setVisibility(View.GONE);
        } else if (visibility == View.GONE) {
            contentRecyclerview.setVisibility(View.VISIBLE);
        }
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, MoreSiteActivity.class);
        context.startActivity(intent);
    }

    private void initData() {
        //顶部app应用
        int size = 8;
        List<HeaderAppBean> headerList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            headerList.add(new HeaderAppBean(null, "应用" + i, null));
        }
        IItemFactory factory = new MoreSiteHeaderAppItemFactory();
        RecycleViewCommonAdapter headerAdapter = new RecycleViewCommonAdapter(headerList, headerRecyclerview, factory);
        headerRecyclerview.setAdapter(headerAdapter);


        size = 10;
        List<MoreSiteContentItemBean> contentList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            DetailRightLayout.DetailRightData detailRightData = getDetailRightData();
            List<String> rightClickUrls = getRightClickUrls(detailRightData.rightDatas.size());
            List<String> extraClickUrls = geExtraClickUrls(detailRightData.extraDatas.size());
            MoreSiteContentItemBean bean = new MoreSiteContentItemBean(detailRightData, rightClickUrls, extraClickUrls);
            contentList.add(bean);
        }
        IItemFactory contentFactory = new MoreSiteItemFactory();
        RecycleViewCommonAdapter contentAdapter = new RecycleViewCommonAdapter(contentList, contentRecyclerview,
                contentFactory);
        contentRecyclerview.setAdapter(contentAdapter);
    }

    private List<String> getRightClickUrls(int size) {
        if (size <= 0) {
            return null;
        }
        List<String> results = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            results.add("right" + i);
        }
        return results;
    }

    private List<String> geExtraClickUrls(int size) {
        if (size <= 0) {
            return null;
        }
        List<String> results = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            results.add("extra" + i);
        }
        return results;
    }

    private DetailRightLayout.DetailRightData getDetailRightData() {
        int size = 6;
        List<String> rightDatas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rightDatas.add("right" + i);
        }
        size = 2;
        List<String> extraDatas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            extraDatas.add("extra" + i);
        }
        DetailRightLayout.DetailRightData data = new DetailRightLayout.DetailRightData(R.mipmap.ic_launcher,
                "bigText", rightDatas, extraDatas);
        return data;
    }

    private void initViews() {
        nestScrollview.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);//防止parameter must be a
        // descendant of this view
        headerRecyclerview.setLayoutManager(new FullyGridLayoutManager(this, 4));
        headerRecyclerview.setFocusable(false);
        headerRecyclerview.setHasFixedSize(true);
        headerRecyclerview.setNestedScrollingEnabled(false);

        contentRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false));
        contentRecyclerview.setFocusable(false);
        contentRecyclerview.setNestedScrollingEnabled(false);

    }
}
