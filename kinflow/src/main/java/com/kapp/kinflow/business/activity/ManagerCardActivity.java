package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.ManagerCardItemFactory;
import com.kapp.kinflow.business.beans.ManagerCardBean;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * description：管理卡片页面
 * <br>author：caowugao
 * <br>time： 2017/04/20 15:16
 */

public class ManagerCardActivity extends Activity {

    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_manager);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();

        initViews();
        initData();
    }

    @OnClick(R2.id.container_back)
    public void setContainerBack(View view) {
        finish();
    }


    private void initData() {
        int size = 4;
        List<ManagerCardBean> datas = new ArrayList<>(size);
        ManagerCardBean headlines = new ManagerCardBean(R.mipmap.ic_launcher, getString(R.string.card_headlines), getString
                (R.string.card_headlines_subtittle), false);
        ManagerCardBean constellationFortune = new ManagerCardBean(R.mipmap.ic_launcher, getString(R.string
                .constellation_fortune), getString
                (R.string.card_constellation_fortune_subtittle), false);
        ManagerCardBean happyMoment = new ManagerCardBean(R.mipmap.ic_launcher, getString(R.string.card_happy_moment), getString
                (R.string.card_happy_moment_subtittle), false);
        ManagerCardBean topGames = new ManagerCardBean(R.mipmap.ic_launcher, getString(R.string.card_top_games), getString
                (R.string.card_top_games_subtittle), false);

        datas.add(headlines);
        datas.add(constellationFortune);
        datas.add(happyMoment);
        datas.add(topGames);
        IItemFactory factory = new ManagerCardItemFactory();
        RecycleViewCommonAdapter<ManagerCardBean> adapter = new RecycleViewCommonAdapter<>(datas, recyclerview, factory);
        recyclerview.setAdapter(adapter);
    }

    private void initViews() {
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }
}
