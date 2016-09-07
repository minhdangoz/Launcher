package com.klauncher.kinflow.views.recyclerView.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by xixionghui on 16/8/24.
 */
public abstract class BaseRecyclerViewHolder<E> extends RecyclerView.ViewHolder {
    public E modelData;
    public View itemRootView;
    public BaseRecyclerViewHolder(View itemView) {
        super(itemView);
        this.itemRootView = itemView;
        initView(this.itemRootView);
    }

    public abstract void initView(View itemRootView);
    public abstract void bundData2View(E modelData);

}
