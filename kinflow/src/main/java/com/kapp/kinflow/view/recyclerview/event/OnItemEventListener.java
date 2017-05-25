package com.kapp.kinflow.view.recyclerview.event;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;


/**
 * <p>功能：RecycleView的点击、长按、双击等事件处理都在此
 * <p>创建人：caowugao
 * <p>时间：2017/04/1 12:04
 */
public class OnItemEventListener implements RecyclerView.OnItemTouchListener {

    private static final String TAG = OnItemEventListener.class.getSimpleName();
    private static final boolean DEBUG = true;
    private GestureDetector mGestureDetector;
    private RecycleViewEvent.OnItemClickListener onItemClick;
    private RecycleViewEvent.OnItemLongClickListener onItemLongClick;
    private RecyclerView recyclerView;
    private Context context;

    public void setOnItemClickListener(RecycleViewEvent.OnItemClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setOnItemLongClickListener(RecycleViewEvent.OnItemLongClickListener onItemLongClick) {
        this.onItemLongClick = onItemLongClick;
    }

    /**
     * 初始化，该方法要放在设置item事件之后
     */
    public void commitEvent() {
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() { //这里选择SimpleOnGestureListener实现类，可以根据需要选择重写的方法
                    //单击事件
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if (null == onItemClick) {
                            return false;
                        }
                        logDebug("onSingleTapUp()" + e.getAction());
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null) {
                            int position = recyclerView.getChildLayoutPosition(childView);
                            RecyclerView.Adapter adapter = recyclerView.getAdapter();
                            if (adapter instanceof RecycleViewCommonAdapter) {
                                RecycleViewCommonAdapter commonAdapter = (RecycleViewCommonAdapter) adapter;
                                if (commonAdapter.isHeaderPos(position)) {//头部
                                    onItemClick.onHeaderItemClick(childView,commonAdapter.getHeaders(),position);
                                } else if (commonAdapter.isFooterPos(position)) {//尾部
                                    onItemClick.onFooterItemClick(childView,commonAdapter.getFooters(),position);
                                } else {//正常部分
                                    int normalPos=position-commonAdapter.getHeaderSize();
                                    onItemClick.onNormalItemClick(childView, normalPos);
                                }
                            }

                            return true;
                        }
                        return false;
                    }

                    //长按事件
                    @Override
                    public void onLongPress(MotionEvent e) {
                        if (null == onItemLongClick) {
                            return;
                        }
                        logDebug("onLongPress()" + e.getAction());
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (null != childView) {
                            onItemLongClick.onItemLongClick(childView, recyclerView.getChildLayoutPosition
                                    (childView));
                        }

                    }
                });
    }

    private void logDebug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public OnItemEventListener(Context context, final RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        //把事件交给GestureDetector处理
        if (mGestureDetector.onTouchEvent(e)) {
            return true;
        } else
            return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}