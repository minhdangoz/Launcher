package com.kapp.kinflow.view.recyclerview.event;

import android.support.v4.util.SparseArrayCompat;
import android.view.View;

/**
 * description：recycleview事件统一定义
 * <br>author：caowugao
 * <br>time： 2017/04/1 12:04
 */

public class RecycleViewEvent {
    public interface OnItemClickListener{
        /**
         * @param view
         * @param position 在正常item部分的索引位置
         */
//        void onItemClick(View view, int position);
        void onNormalItemClick(View view, int position);

        /**
         * 可通过RecycleViewCommonAdapter.getHeaderKeyInRecycleView(position)拿到key，从而headers.get(key)拿到view
         * @param view
         * @param headers
         * @param position 在RecyclerVew中的索引位置
         */
        void onHeaderItemClick(View view, SparseArrayCompat<View> headers, int position);

        /**
         * 可通过RecycleViewCommonAdapter.getFooterKeyInRecycleView(position)拿到key，footers.get(key)拿到view
         * @param view
         * @param footers
         * @param position 在RecyclerVew中的索引位置
         */
        void onFooterItemClick(View view, SparseArrayCompat<View> footers, int position);
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
    public interface OnPullToRefreshListener{
        void onRefresh();
        void onLoadMore();
    }

}
