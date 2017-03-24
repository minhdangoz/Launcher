package com.klauncher.setupwizard.common;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * description：仿PullToRefreshScrollView
 * <br>author：caowugao
 * <br>time： 2017/02/13 10:23
 */
public class PullToScrollView extends ScrollView {
    private static final float MOVE_FACTOR = 0.5f; // 移动因子,手指移动100dp,那么View就只移动50dp

    private static final int ANIM_TIME = 300; // 松开手指后, 界面回到正常位置需要的动画时间
    private static final boolean DEBUG = true;
    private static final String TAG = PullToScrollView.class.getSimpleName();

    private float startY;// 手指按下时的Y值, 用于在移动时计算移动距离,如果按下时不能上拉和下拉，
    // 会在手指移动时更新为当前手指的Y值

    // ui
    private View contentView; // ScrollView的唯一内容控件
    private Rect originalRect = new Rect();// 用于记录正常的布局位置

    // flag
    private boolean canPullDown = false; // 是否可以继续下拉
    private boolean canPullUp = false; // 是否可以继续上拉
    private boolean isMoved = false; // 记录是否移动了布局
    private OnScrollListener onScrollListener;
    private OnScrollTopButtomListener onScrollTopButtomListener;
    private boolean isScrolledToTop;
    private boolean isScrolledToBottom;
    private boolean isStopPull = false;
    // 滑动距离及坐标
    private float xDistance, yDistance, xLast, yLast;
    private boolean allowAnimation = false;

    public PullToScrollView(Context context) {
        super(context);
        init(context);
    }

    public PullToScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 初始化上下拉的控件布局
     *
     * @param context
     */
    private void init(Context context) {
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            contentView = getChildAt(0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r,
                            int b) {
        super.onLayout(changed, l, t, r, b);
        if (contentView == null)
            return;
        // ScrollView中的唯一子控件的位置信息, 这个位置信息在整个控件的生命周期中保持不变
        originalRect.set(contentView.getLeft(), contentView.getTop(),
                contentView.getRight(), contentView.getBottom());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        logDebug("onInterceptTouchEvent() ....");
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;

                if (xDistance > yDistance) {
                    allowAnimation = false;
                    return false;
                } else {
                    allowAnimation = true;
                }

        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 在触摸事件中, 处理上拉和下拉的逻辑
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        logDebug("dispatchTouchEvent() ....");
        if (contentView == null) {
            try {
                return super.dispatchTouchEvent(ev);
            } catch (Exception ex) {
                return false;
            }
        }






        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;

                if (xDistance > yDistance) {
                    allowAnimation = false;
                } else {
                    allowAnimation = true;
                }

        }

        // 手指是否移动到了当前ScrollView控件之外
        boolean isTouchOutOfScrollView = ev.getY() >= this
                .getHeight() || ev.getY() <= 0;
        if (isTouchOutOfScrollView) { // 如果移动到了当前ScrollView控件之外
            if (isMoved) {// 如果当前contentView已经被移动, 首先把布局移到原位置
                boundBack();
            }
            return true;
        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 判断是否可以上拉和下拉
                canPullDown = isCanPullDown();
                canPullUp = isCanPullUp();
                // 记录按下时的Y值
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                boundBack();
                break;
            case MotionEvent.ACTION_MOVE:
                // 在移动的过程中， 既没有滚动到可以上拉的程度， 也没有滚动到可以下拉的程度
                if (!canPullDown && !canPullUp) {
                    startY = ev.getY();
                    canPullDown = isCanPullDown();
                    canPullUp = isCanPullUp();
                    break;
                }

                if (isStopPull) {
                    break;
                }

                // 计算手指移动的距离
                float nowY = ev.getY();
                int deltaY = (int) (nowY - startY);

                // 是否应该移动布局
                boolean shouldMove = allowAnimation&&((canPullDown && deltaY > 0) // 可以下拉， 并且手指向下移动
                        || (canPullUp && deltaY < 0) // 可以上拉， 并且手指向上移动
                        || (canPullUp && canPullDown)); // 既可以上拉也可以下拉（这种情况出现在ScrollView包裹的控件比ScrollView还小）
                if (shouldMove) {
                    // 计算偏移量
                    int offset = (int) (deltaY * MOVE_FACTOR);
                    // 随着手指的移动而移动布局
                    contentView.layout(originalRect.left,
                            originalRect.top + offset,
                            originalRect.right, originalRect.bottom
                                    + offset);
                    isMoved = true; // 记录移动了布局
                }
                break;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception ex) {
            return false;
        }

    }


    /**
     * 将内容布局移动到原位置 可以在UP事件中调用, 也可以在其他需要的地方调用, 如手指移动到当前ScrollView外时
     */
    private void boundBack() {
        if (!isMoved && !allowAnimation) {
            return; // 如果没有移动布局， 则跳过执行
        }
        // 开启动画
        TranslateAnimation anim = new TranslateAnimation(0, 0,
                contentView.getTop(), originalRect.top);
        anim.setDuration(ANIM_TIME);
        contentView.startAnimation(anim);
        // 设置回到正常的布局位置
        contentView.layout(originalRect.left, originalRect.top,
                originalRect.right, originalRect.bottom);
        // 将标志位设回false
        canPullDown = false;
        canPullUp = false;
        isMoved = false;
    }

    /**
     * 判断是否滚动到顶部
     */
    private boolean isCanPullDown() {
        return getScrollY() == 0
                || contentView.getHeight() < getHeight()
                + getScrollY();
    }

    /**
     * 判断是否滚动到底部
     */
    private boolean isCanPullUp() {
        return contentView.getHeight() <= getHeight() + getScrollY();
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (scrollY == 0) {
            isScrolledToTop = clampedY;
            isScrolledToBottom = false;
//            logDebug("onOverScrolled() isScrolledToTop:" + isScrolledToTop);
        } else {
            isScrolledToTop = false;
            isScrolledToBottom = clampedY;
//            logDebug("onOverScrolled() isScrolledToBottom:" + isScrolledToBottom);
        }
        notifyScrollChangedListeners();
    }

    private void notifyScrollChangedListeners() {
        if (isScrolledToTop) {
            if (onScrollTopButtomListener != null) {
                onScrollTopButtomListener.onScrolledToTop();
            }
        } else if (isScrolledToBottom) {
            if (onScrollTopButtomListener != null) {
                onScrollTopButtomListener.onScrolledToBottom();
            }
        }
    }

    private void logDebug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    //    @Override
//    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//        if (null != onScrollListener) {
//            onScrollListener.onScrollChanged(l, t, oldl, oldt);
//        }
//        super.onScrollChanged(l, t, oldl, oldt);
//
//        if (getScrollY() == 0) {
//            isScrolledToTop = true;
//            isScrolledToBottom = false;
//            logDebug("onScrollChanged isScrolledToTop:" + isScrolledToTop);
//        } else if (getScrollY() + getHeight() - getPaddingTop() - getPaddingBottom() == getChildAt(0).getHeight()) {
//            isScrolledToBottom = true;
//            logDebug("onScrollChanged isScrolledToBottom:" + isScrolledToBottom);
//            isScrolledToTop = false;
//        } else {
//            isScrolledToTop = false;
//            isScrolledToBottom = false;
//        }
//        notifyScrollChangedListeners();
//    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        logDebug("onScrollChanged() ....");
        if (null != onScrollListener) {
            onScrollListener.onScrollChanged(l, t, oldl, oldt);
        }
        super.onScrollChanged(l, t, oldl, oldt);

        if (getScrollY() == 0) {//顶部
            isScrolledToTop = true;
            isScrolledToBottom = false;
//            logDebug("onScrollChanged() isScrolledToTop:" + isScrolledToTop);
        } else if (getScrollY() + getHeight() - getPaddingTop() - getPaddingBottom() == getChildAt(0).getHeight()) {//底部
            isScrolledToBottom = true;
            isScrolledToTop = false;
//            logDebug("onScrollChanged() isScrolledToBottom:" + isScrolledToBottom);
        } else {
            isScrolledToTop = false;
            isScrolledToBottom = false;
        }
        notifyScrollChangedListeners();
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.onScrollListener = listener;
    }

    public void setOnScrollTopButtomListener(OnScrollTopButtomListener listener) {
        this.onScrollTopButtomListener = listener;
    }

    public void stopPull() {
        isStopPull = true;
    }

    public void allowPull() {
        isStopPull = false;
    }

    public interface OnScrollListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    public interface OnScrollTopButtomListener {
        void onScrolledToTop();


        void onScrolledToBottom();
    }

    //    /**
//     * 设置上下拉刷新的监听器(本功能暂未完成，不对外开放本方法)
//     */
//    private void setOnRefreshListener(OnRefreshListener l) {}

//    /**
//     * 设置头控件
//     */
//    private void setHeadView() {}
}
