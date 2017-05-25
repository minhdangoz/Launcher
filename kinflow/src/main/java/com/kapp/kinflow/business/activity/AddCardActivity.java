package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.AddCardItemFactory;
import com.kapp.kinflow.business.beans.AddCardBean;
import com.kapp.kinflow.business.util.ScreenUnitUtil;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.decoration.LinearDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/04/20 12:11
 */

public class AddCardActivity extends Activity {

    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();
        initViews();
        initData();
    }

    @OnClick(R2.id.container_back)
    public void setContainerBack(View view) {
        finish();
    }

    @OnClick(R2.id.tv_manager)
    public void onManagerClick(View view) {
        Intent intent = new Intent(this, ManagerCardActivity.class);
        startActivity(intent);
    }

    private void initData() {
        int size = 4;
        List<AddCardBean> datas = new ArrayList<>(size);
        AddCardBean headlines = new AddCardBean(R.drawable.navi_card_jrtt_ic, getString(R.string.card_headlines), getString
                (R.string.card_headlines_subtittle), false);
        AddCardBean constellationFortune = new AddCardBean(R.drawable.navi_star_ic, getString(R.string
                .constellation_fortune), getString
                (R.string.card_constellation_fortune_subtittle), false);
        AddCardBean happyMoment = new AddCardBean(R.drawable.navi_card_joke_ic, getString(R.string.card_happy_moment), getString
                (R.string.card_happy_moment_subtittle), false);
        AddCardBean topGames = new AddCardBean(R.drawable.navi_card_game_ic, getString(R.string.card_top_games), getString
                (R.string.card_top_games_subtittle), false);

        datas.add(headlines);
        datas.add(constellationFortune);
        datas.add(happyMoment);
        datas.add(topGames);
        IItemFactory factory = new AddCardItemFactory();
        RecycleViewCommonAdapter<AddCardBean> adapter = new RecycleViewCommonAdapter<>(datas, recyclerview, factory);
        recyclerview.setAdapter(adapter);
    }

    private void initViews() {
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        int dp_5 = ScreenUnitUtil.asPx(5);
        int color = ContextCompat.getColor(this, R.color.app_separator_line_color);
        LinearDecoration divider = new LinearDecoration.Builder().color(color).size(1).margin(dp_5).build();
        recyclerview.addItemDecoration(divider);
    }
}
