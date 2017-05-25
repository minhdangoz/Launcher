package com.kapp.kinflow.business.holder;

import android.content.res.Resources;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.business.activity.MoreNovelsActivity2;
import com.kapp.kinflow.business.adapter.NovelsCardItemFactory;
import com.kapp.kinflow.business.beans.MineNovelsBean;
import com.kapp.kinflow.business.beans.NovelCardBean;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.common.browser.KinflowBrower;

import java.util.List;


/**
 * description：小说阅读卡片
 * <br>author：caowugao
 * <br>time： 2017/05/19 20:07
 */

public class NovelCardViewHolder extends BaseCustomViewHolder {
    private RecyclerView recyclerview;

    public NovelCardViewHolder(View itemView) {
        super(itemView, NovelCardBean.TYPE_NOVEL);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.novel_card);
    }

    @Override
    protected View onCreateButtomLeftView(View itemView, LinearLayout containerButtomLeft, LayoutInflater inflater) {
        View buttomLeftView = inflater.inflate(R.layout.item_custom_buttom_left, null);
        TextView tvMore = (TextView) buttomLeftView.findViewById(R.id.textview1);
        TextView textview2 = (TextView) buttomLeftView.findViewById(R.id.textview2);
        TextView textview3 = (TextView) buttomLeftView.findViewById(R.id.textview3);
        textview2.setVisibility(View.GONE);
        textview3.setVisibility(View.GONE);

        Resources resources = itemView.getResources();
        tvMore.setText(resources.getString(R.string.more_novel));
        tvMore.setOnClickListener(this);
        return buttomLeftView;
    }

    @Override
    protected View onCreateMainContentView(View itemView, FrameLayout containerMain, LayoutInflater inflater) {
        View mainContentView = inflater.inflate(R.layout.recyclerview, null);
        recyclerview = (RecyclerView) mainContentView.findViewById(R.id.recyclerview);
        return mainContentView;
    }

    @Override
    public void onBindViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int viewType) {
        BaseItemBean baseBean = beanList.get(position);
        if (null != baseBean && baseBean instanceof NovelCardBean) {
            final NovelCardBean cardBean = (NovelCardBean) baseBean;
            recyclerview.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager
                    .VERTICAL, false));
            IItemFactory factory = new NovelsCardItemFactory();
            RecycleViewCommonAdapter adapter = new RecycleViewCommonAdapter(cardBean.datas, recyclerview, factory);
            adapter.setOnItemClickListener(new RecycleViewEvent.OnItemClickListener() {
                @Override
                public void onNormalItemClick(View view, int position) {
                    MineNovelsBean bean = cardBean.datas.get(position);
                    KinflowBrower.openUrl(view.getContext(), bean.clickUrl);
                }

                @Override
                public void onHeaderItemClick(View view, SparseArrayCompat<View> headers, int position) {

                }

                @Override
                public void onFooterItemClick(View view, SparseArrayCompat<View> footers, int position) {

                }
            });
            adapter.commitItemEvent();
            recyclerview.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if(id== R.id.textview1){//更多小说
            doMore();
        }
//        switch (view.getId()) {
//            case R2.id.textview1://更多小说
//                doMore();
//                break;
//        }
    }

    private void doMore() {
        MoreNovelsActivity2.launch(itemView.getContext());
    }
}
