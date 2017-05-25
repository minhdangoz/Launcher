package com.kapp.kinflow.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;

/**
 * description：具有滑动回弹效果的HorizontalScrollView
 * <br>author：caowugao
 * <br>time： 2017/05/21 18:35
 */

public class PullHorizontalScrollView extends HorizontalScrollView {

    private static final float MOVE_FACTOR = 0.5f; // 移动因子,手指移动100dp,那么View就只移动50dp

    private static final int ANIM_TIME = 300; // 松开手指后, 界面回到正常位置需要的动画时间

    private static final boolean DEBUG = true;

    private static final String TAG = PullHorizontalScrollView.class.getSimpleName();

    private float startX;// 手指按下时的Y值, 用于在移动时计算移动距离,如果按下时不能上拉和下拉

    // 会在手指移动时更新为当前手指的Y值

    // ui
    private View contentView; // ScrollView的唯一内容控件

    private Rect originalRect = new Rect();// 用于记录正常的布局位置

    // flag
    private boolean canPullRight = false; // 是否可以继续下拉

    private boolean canPullLeft = false; // 是否可以继续上拉

    private boolean isMoved = false; // 记录是否移动了布局

    private OnScrollListener onScrollListener;


    private boolean isStopPull = false;

    public PullHorizontalScrollView(Context context) {
        super(context);
        init(context);
    }

    public PullHorizontalScrollView(Context context, AttributeSet attrs) {
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (contentView == null) return;
        // ScrollView中的唯一子控件的位置信息, 这个位置信息在整个控件的生命周期中保持不变
        originalRect.set(contentView.getLeft(), contentView.getTop(), contentView.getRight(), contentView.getBottom());
    }

    /**
     * 在触摸事件中, 处理左拉和右拉的逻辑
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (contentView == null) {
            try {
                return super.dispatchTouchEvent(ev);
            } catch (Exception ex) {
                return false;
            }
        }
        // 手指是否移动到了当前ScrollView控件之外
        boolean isTouchOutOfScrollView = ev.getY() >= this.getHeight() || ev.getY() <= 0;
        if (isTouchOutOfScrollView) { // 如果移动到了当前ScrollView控件之外
            if (isMoved) {// 如果当前contentView已经被移动, 首先把布局移到原位置
                boundBack();
            }
            return true;
        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 判断是否可以左拉和右拉
                canPullRight = isCanPullRight();
                canPullLeft = isCanPullLeft();
                // 记录按下时的X值
                startX = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                boundBack();
                break;
            case MotionEvent.ACTION_MOVE:
                // 在移动的过程中， 既没有滚动到可以上拉的程度， 也没有滚动到可以下拉的程度
                if (!canPullRight && !canPullLeft) {
                    startX = ev.getX();
                    canPullRight = isCanPullRight();
                    canPullLeft = isCanPullLeft();
                    break;
                }

                if (isStopPull) {
                    break;
                }

                // 计算手指移动的距离
                float nowX = ev.getX();
                int deltaX = (int) (nowX - startX);

                // 是否应该移动布局
                boolean shouldMove = ((canPullRight && deltaX > 0) // 可以向右拉，并且手指向右移动
                        || (canPullLeft && deltaX < 0) // 可以向左拉， 并且手指向左移动
                        || (canPullLeft && canPullRight)); // 既可以左拉也可以右拉（这种情况出现在ScrollView包裹的控件比ScrollView还小）
                if (shouldMove) {
                    // 计算偏移量
                    int offset = (int) (deltaX * MOVE_FACTOR);
                    // 随着手指的移动而移动布局
                    contentView.layout(originalRect.left + offset, originalRect.top, originalRect.right + offset,
                            originalRect.bottom);
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
        if (!isMoved) {
            return; // 如果没有移动布局， 则跳过执行
        }
        // 开启动画
        TranslateAnimation anim = new TranslateAnimation(contentView.getLeft(), originalRect.left, 0, 0);
        anim.setDuration(ANIM_TIME);
        contentView.startAnimation(anim);
        // 设置回到正常的布局位置
        contentView.layout(originalRect.left, originalRect.top, originalRect.right, originalRect.bottom);
        // 将标志位设回false
        canPullRight = false;
        canPullLeft = false;
        isMoved = false;
    }

    /**
     * 判断是否滚动到左边
     */
    private boolean isCanPullRight() {
        return getScrollX() == 0 || contentView.getWidth() < getWidth() + getScrollX();
    }

    /**
     * 判断是否滚动到右边
     */
    private boolean isCanPullLeft() {
        return contentView.getWidth() <= getWidth() + getScrollX();
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    private void logDebug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (null != onScrollListener) {
            onScrollListener.onScrollChanged(l, t, oldl, oldt);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.onScrollListener = listener;
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

    public interface OnScrollLeftRightListener {
        void onScrolledToLeft();

        void onScrolledToRight();
    }

    // /**
    // * 设置上下拉刷新的监听器(本功能暂未完成，不对外开放本方法)
    // */
    // private void setOnRefreshListener(OnRefreshListener l) {}

    // /**
    // * 设置头控件
    // */
    // private void setHeadView() {}
}
