package com.kapp.kinflow.view.recyclerview.event;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * <p>功能：结合SwipeRefreshLayout，上拉刷新
 * <p>创建人：曹武高
 * <p>时间：2017/04/1 12:04
 */
public class OnPullControllListener extends RecyclerView.OnScrollListener implements SwipeRefreshLayout
        .OnRefreshListener {
    /**
     * 说明：
     *  /**
     * 设置监听
    mRecyclerView.addOnScrollListener(mController);
    mSwipeRefreshLayout.setOnRefreshListener(mController);
     */

    private int[] staggeredGridLastPos;

    private RecycleViewEvent.OnPullToRefreshListener listener;

    public OnPullControllListener(RecycleViewEvent.OnPullToRefreshListener listener) {
        this.listener = listener;
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        if (null != listener) {
            listener.onRefresh();
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int lastVisibleItemPosition = 0;//获取可见的最后一个item的位置
        int visibleCount = 0;//可见个数
        int totalCount = 0;
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();//获取可见的最后一个item的位置
            visibleCount = gridLayoutManager.getChildCount();//可见个数
            totalCount = gridLayoutManager.getItemCount();//总数
        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();//获取可见的最后一个item的位置
            visibleCount = linearLayoutManager.getChildCount();//可见个数
            totalCount = linearLayoutManager.getItemCount();//总数
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;

            if (null == staggeredGridLastPos) {
                staggeredGridLastPos = new int[staggeredGridLayoutManager.getSpanCount()];
            }
            staggeredGridLayoutManager.findLastVisibleItemPositions(staggeredGridLastPos);//获取可见的最后一个item的位置
            lastVisibleItemPosition = getMax(staggeredGridLastPos);//获取最大的那个数即最后一个item的位置
            visibleCount = staggeredGridLayoutManager.getChildCount();//可见个数
            totalCount = staggeredGridLayoutManager.getItemCount();//总数
        }

        if (visibleCount > 0 && newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPosition >= totalCount -
                1) {
            if (null != listener) {
                listener.onLoadMore();
            }
        }
    }

    private int getMax(int[] datas) {
        if (null == datas) {
            return Integer.MAX_VALUE;
        }
        int max = datas[0];
        for (int data : datas) {
            if (data > max) {
                max = data;
            }
        }
        return max;
    }
}
