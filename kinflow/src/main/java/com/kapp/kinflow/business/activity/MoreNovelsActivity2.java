package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.RecommendNovelsItemFactory;
import com.kapp.kinflow.business.beans.RecommendNovelsBean;
import com.kapp.kinflow.business.constant.Constant;
import com.kapp.kinflow.business.http.OKHttpUtil;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.knews.base.recycler.decoration.ItemDecorationDivider;
import com.kapp.knews.common.browser.KinflowBrower;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class MoreNovelsActivity2 extends Activity implements OKHttpUtil.OnRequestListener, RecycleViewEvent
        .OnItemClickListener {
    private static final int REQUEST_CODE_RECOMMEND = 100;
    @BindView(R2.id.container_back)
    LinearLayout containerBack;
    @BindView(R2.id.iv_search)
    ImageView ivSearch;
    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R2.id.container_loading)
    RelativeLayout containerLoading;
    private List<RecommendNovelsBean> datas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_novels2);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();
        intViews();
        initData();
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, MoreNovelsActivity2.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private void intViews() {
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerview.addItemDecoration(new ItemDecorationDivider(this, LinearLayoutManager.VERTICAL));
    }


    @OnClick(R2.id.iv_search)
    public void onSearchBtnClick(View view) {
        NovelSearchActivity.launch(this);
    }

    private void initData() {
        containerLoading.setVisibility(View.VISIBLE);
        OKHttpUtil.get(Constant.NOVEL_RECOMMEND_URL, null, REQUEST_CODE_RECOMMEND, this);
    }

    @OnClick(R2.id.container_back)
    public void back(View view) {
        finish();
    }

    @Override
    public void onFailure(int requestCode, String msg) {
        containerLoading.setVisibility(View.GONE);
        showShortToast(msg);
    }

    private void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(int requestCode, Object simpleResponse) {
        switch (requestCode) {
            case REQUEST_CODE_RECOMMEND:
                containerLoading.setVisibility(View.GONE);
                handlerResult(simpleResponse);
                break;
        }
    }

    private void handlerResult(Object simpleResponse) {
        if (null == simpleResponse) {
            LogUtils.e("获取图书推荐失败，io错误!!!");
            return;
        }
        try {
            JSONArray rootJson = new JSONArray(simpleResponse.toString());
            int length = rootJson.length();
            datas = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                JSONObject itemJson = rootJson.getJSONObject(i);
                RecommendNovelsBean bean = new RecommendNovelsBean(itemJson);
                datas.add(bean);
            }
            IItemFactory factory = new RecommendNovelsItemFactory();
            RecycleViewCommonAdapter<RecommendNovelsBean> adapter = new RecycleViewCommonAdapter<>(datas, recyclerview,
                    factory);
            adapter.setOnItemClickListener(this);
            adapter.commitItemEvent();
            recyclerview.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OKHttpUtil.cancel(REQUEST_CODE_RECOMMEND);
    }

    @Override
    public void onNormalItemClick(View view, int position) {
        RecommendNovelsBean bean = datas.get(position);
        KinflowBrower.openUrl(this, bean.clickUrl);
    }

    @Override
    public void onHeaderItemClick(View view, SparseArrayCompat<View> headers, int position) {

    }

    @Override
    public void onFooterItemClick(View view, SparseArrayCompat<View> footers, int position) {

    }
}
