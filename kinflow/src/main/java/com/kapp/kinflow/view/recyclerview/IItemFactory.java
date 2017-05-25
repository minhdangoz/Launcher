package com.kapp.kinflow.view.recyclerview;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * description：item工厂接口，所有的item初始化和绑定数据都在此
 * <br>author：caowugao
 * <br>time： 2017/04/1 12:04
 */

public interface IItemFactory<DATA, VIEW_HOLDER extends IItemFactory.BaseViewHolder> {

    void bindAdapter(RecyclerView.Adapter adapter);

    RecyclerView.Adapter getAdapter();

    void onAttachedToRecyclerView(RecyclerView recyclerView);

    void onDetachedFromRecyclerView(RecyclerView recyclerView);

    void onViewDetachedFromWindow(VIEW_HOLDER holder);


    /**
     * 初始化控件
     *
     * @param parent
     * @param viewType
     * @return
     */
    VIEW_HOLDER onCreateNormalViewHolder(ViewGroup parent, int viewType);

    /**
     * 绑定数据
     *
     * @param holder
     * @param datas
     * @param position
     * @param viewType
     */
    void onBindNormalViewHolder(VIEW_HOLDER holder, List<DATA> datas, int position, int viewType);

    /**
     * 绑定头部数据
     *
     * @param holder
     * @param headers
     * @param key
     */
    void onBindHeaderViewHolder(VIEW_HOLDER holder, SparseArrayCompat<View> headers, int key);

    /**
     * 绑定尾部数据
     *
     * @param holder
     * @param footers
     * @param key
     */
    void onBindFooterViewHolder(VIEW_HOLDER holder, SparseArrayCompat<View> footers, int key);

    /**
     * 设置item动画
     *
     * @param holder
     * @param position
     */
    void setItemAnimation(VIEW_HOLDER holder, int position);

    class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
        }
    }

    class CommonViewHolder extends BaseViewHolder {
        public CommonViewHolder(View itemView) {
            super(itemView);
        }
    }
}
