package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.CommonFragmentPageAdapter;
import com.kapp.kinflow.business.fragment.MineNovelsFragment;
import com.kapp.kinflow.business.fragment.RecommendNovelsFragment;
import com.kapp.kinflow.view.indicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * description：更多小说页面
 * <br>author：caowugao
 * <br>time： 2017/05/19 15:32
 */

public class MoreNovelsActivity extends FragmentActivity {
    @BindView(R2.id.container_back)
    LinearLayout containerBack;
    @BindView(R2.id.tv_recommend)
    TextView tvRecommend;
    @BindView(R2.id.tv_mine)
    TextView tvMine;
    @BindView(R2.id.indicator)
    UnderlinePageIndicator indicator;
    @BindView(R2.id.pager)
    ViewPager pager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_novels);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();
        intViews();
        initData();
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, MoreNovelsActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private void intViews() {
        indicator.setFades(false);
    }

    @OnClick(R2.id.tv_recommend)
    public void onRecommendClick(View view) {
        pager.setCurrentItem(0);
    }

    @OnClick(R2.id.tv_mine)
    public void onMineClick(View view) {
        pager.setCurrentItem(1);
    }

    @OnClick(R2.id.iv_search)
    public void onSearchBtnClick(View view) {
        NovelSearchActivity.launch(this);
    }

    private void initData() {
        List<Fragment> fragments = new ArrayList<>(2);
        fragments.add(new RecommendNovelsFragment());
        fragments.add(new MineNovelsFragment());
        List<String> tittles = new ArrayList<>(2);
        tittles.add(getString(R.string.recommend));
        tittles.add(getString(R.string.mine));
        CommonFragmentPageAdapter adapter = new CommonFragmentPageAdapter(getSupportFragmentManager(), fragments,
                tittles);
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
    }

    @OnClick(R2.id.container_back)
    public void back(View view) {
        finish();
    }
}
