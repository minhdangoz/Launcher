package com.kapp.kinflow.business.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.activity.NovelDetailActivity;
import com.kapp.kinflow.business.adapter.RecommendNovelsItemFactory;
import com.kapp.kinflow.business.beans.RecommendNovelsBean;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.knews.base.recycler.decoration.ItemDecorationDivider;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：小说推荐页面
 * <br>author：caowugao
 * <br>time： 2017/05/19 16:53
 */

public class RecommendNovelsFragment extends Fragment implements RecycleViewEvent.OnItemClickListener {

    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;
    private List<RecommendNovelsBean> datas;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View contentView = inflater.inflate(R.layout.recyclerview, null, false);
        ButterKnife.bind(this, contentView);
        initViews(contentView);
        initData();
        return contentView;
    }

    private void initViews(View contentView) {
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        recyclerview.addItemDecoration(new ItemDecorationDivider(getContext(), LinearLayoutManager.VERTICAL));
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//
//        if (getUserVisibleHint()) {
//            onVisible();
//        } else {
//            onInvisible();
//        }
//    }
//
//    private void onInvisible() {
//
//    }
//
//    private void onVisible() {
//        initData();
//    }

    private void initData() {
        int size = 10;
        datas = new ArrayList<>(size);
        String imageUrl = "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=928861582,28679776&fm=117&gp=0" +
                ".jpg";
        for (int i = 0; i < size; i++) {
            RecommendNovelsBean bean = new RecommendNovelsBean(imageUrl, "title" + i, "author" + i, "subtitle" + i,
                    "clickUrl" + i);
            datas.add(bean);
        }
        IItemFactory factory = new RecommendNovelsItemFactory();
        RecycleViewCommonAdapter<RecommendNovelsBean> adapter = new RecycleViewCommonAdapter<>(datas, recyclerview,
                factory);
        adapter.setOnItemClickListener(this);
        adapter.commitItemEvent();
        recyclerview.setAdapter(adapter);
    }

    @Override
    public void onNormalItemClick(View view, int position) {
        // TODO: 2017/5/19 0019 这里处理点击
        NovelDetailActivity.launch(getContext());
    }

    @Override
    public void onHeaderItemClick(View view, SparseArrayCompat<View> headers, int position) {

    }

    @Override
    public void onFooterItemClick(View view, SparseArrayCompat<View> footers, int position) {

    }
}
