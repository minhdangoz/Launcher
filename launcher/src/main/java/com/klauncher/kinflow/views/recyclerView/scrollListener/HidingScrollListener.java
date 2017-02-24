package com.klauncher.kinflow.views.recyclerView.scrollListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/*
* This class is a ScrollListener for RecyclerView that allows to show/hide
* views when list is scrolled. It assumes that you have added a header
* to your list. @see pl.michalz.hideonscrollexample.adapter.partone.RecyclerAdapter
* */
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener{

    private static final int HIDE_THRESHOLD = 20;

    private float mScrolledDistance = 0;
    private boolean mCurrentVisible = true;


    private static final String TAG = "MyInfo";

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        Log.e(TAG, "......onScrollStateChanged......  newState=" + newState + " ,firstVisibleItem=" + firstVisibleItem+" ,mScrolledDistance="+mScrolledDistance);
        if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {//正在拖拽

        } else if (RecyclerView.SCROLL_STATE_SETTLING == newState) {//The RecyclerView is currently animating to a final position while not under outside control.

        } else if (RecyclerView.SCROLL_STATE_IDLE == newState) {//闲置状态
            Log.e(TAG,"mScrolledDistance="+mScrolledDistance);
            if (firstVisibleItem == 0) {//当前处于RecyclerView的最顶部
                 //判断滑动方向,如果是向上滑动(正值).则忽略
                //如果是向下滑动(负值),则显示

            }
        }

    }

    /**
     * 这个距离是RecyclerView滑动的距离与手指滑动的距离无关.
     * 我们应该判断手指滑动的距离
     * @param recyclerView
     * @param dx
     * @param dy
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        Log.e(TAG, "......onScrolled......");
        super.onScrolled(recyclerView, dx, dy);
//
//        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//
//        if (firstVisibleItem == 0) {
//            if (!mCurrentVisible) {
//                onShow();
//                mCurrentVisible = true;
//            }
//        } else {
//            if (mScrolledDistance > HIDE_THRESHOLD && mCurrentVisible) {
//                onHide();
//                mCurrentVisible = false;
//                mScrolledDistance = 0;
//            } else if (mScrolledDistance < -HIDE_THRESHOLD && !mCurrentVisible) {
//                onShow();
//                mCurrentVisible = true;
//                mScrolledDistance = 0;
//            }
//        }
//        if ((mCurrentVisible && dy > 0) || (!mCurrentVisible && dy < 0)) {
//            mScrolledDistance += dy;
//        }
//        mScrolledDistance+=dy;
    }

    public abstract void onHide();

    public abstract void onShow();

    //-----------------

//    @Override
//    public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//        if (!recyclerView.canScrollVertically(-1)) {
//            onScrolledToTop();
//        } else if (!recyclerView.canScrollVertically(1)) {
//            onScrolledToBottom();
//        } else if (dy < 0) {
//            onScrolledUp();
//        } else if (dy > 0) {
//            onScrolledDown();
//        }
//    }
//
//    public void onScrolledUp() {}
//
//    public void onScrolledDown() {}
//
//    public void onScrolledToTop() {}
//
//    public void onScrolledToBottom() {}
}
