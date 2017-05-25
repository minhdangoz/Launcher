package com.kapp.kinflow.business.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.kapp.kinflow.R;
import com.kapp.kinflow.business.beans.ShareItemBean;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;

import java.util.List;



/**
 * description：分享的RecyclerView管理器
 * <br>author：caowugao
 * <br>time： 2017/05/23 16:28
 */

public class ShareRecyclerViewBuilder {

    public static RecyclerView newGridRecyclerView(Context context, List<ShareItemBean> datas, int spanCount,
                                                   RecycleViewEvent.OnItemClickListener itemClickListener) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.recyclerview, null, false);
        RecyclerView recyclerview = (RecyclerView) contentView.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(context, spanCount));
        IItemFactory factory = new ShareItemFactory();
        RecycleViewCommonAdapter<ShareItemBean> adapter = new RecycleViewCommonAdapter<>(datas, recyclerview,
                factory);
        if (null != itemClickListener) {
            adapter.setOnItemClickListener(itemClickListener);
            adapter.commitItemEvent();
        }
        recyclerview.setAdapter(adapter);
        return recyclerview;
    }
}
