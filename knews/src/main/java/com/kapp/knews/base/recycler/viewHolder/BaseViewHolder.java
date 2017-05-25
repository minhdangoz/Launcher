package com.kapp.knews.base.recycler.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kapp.knews.base.recycler.data.NewsBaseData;


/**
 * Created by xixionghui on 2016/11/22.
 */

public  abstract class BaseViewHolder<T extends NewsBaseData> extends RecyclerView.ViewHolder {

    public T modelData;
    public View itemRootView;



    public BaseViewHolder(View itemView) {
        super(itemView);
        this.itemRootView = itemView;
        initView(this.itemRootView);
    }

    public abstract void initView(View itemRootView);
    public abstract void boundData2View(T modelData);
}
