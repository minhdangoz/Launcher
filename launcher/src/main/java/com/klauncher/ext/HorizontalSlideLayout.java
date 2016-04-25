package com.klauncher.ext;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.LinearLayout;

/**
 * Created by wangqinghao on 2016/4/19.
 */
public class HorizontalSlideLayout extends LinearLayout{
    private static String TAG = "VerticalSlideLayout";
    private int DRAG_X_THROD = 0;
    private int SCROLL_X = 0;
    private final int ANIM_DURATION = 300;

    private static final int SLIDE_TO_LEFT = -1;
    private static final int SLIDE_TO_RIGHT = 1;
    private int mSlideDirection = 0;

    public HorizontalSlideLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //判断横划的阈值,为了兼容不同尺寸的设备，以dp为单位
        DRAG_X_THROD = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
        SCROLL_X = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
    }

    public HorizontalSlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalSlideLayout(Context context) {
        this(context, null, 0);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    int downX, downY;
    boolean isNeedToGoBack;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        boolean isInterceptHere = false;
        try {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = (int) ev.getX();
                    downY = (int) ev.getY();
                    isInterceptHere = true;
                    break;

                case MotionEvent.ACTION_MOVE:

                    int dx = (int) Math.abs(ev.getX() - downX);
                    int dy = (int) Math.abs(ev.getY() - downY);
                    if (dx > dy && dx > DRAG_X_THROD) {
                        Log.i(TAG, "横划！拦截它");
                        setParentInterceptTouchEvent(true);
                        isInterceptHere = true;
                        mSlideDirection = (ev.getX() - downX) > 0 ? SLIDE_TO_RIGHT : SLIDE_TO_LEFT;
                        if (mSlideDirection == SLIDE_TO_LEFT) {
                            isNeedToGoBack = true;
                        } else if (mSlideDirection == SLIDE_TO_RIGHT && isNeedToGoBack) {
                        }
                    } else if (dy > dx) {
                        Log.i(TAG, "竖划！不拦截");
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    setParentInterceptTouchEvent(false);
                    isInterceptHere = false;
                    downX = 0;
                    downY = 0;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isInterceptHere;
    }

    /*float touchDownX;
    int startX;
    boolean mScrolling;
    int mTouchSlop = 300;
    *//**
     * 是否传递到子view
     * true  不传递
     * false 传递
     * 一次触摸屏幕中，如果拦截过则不会再次进入onInterceptTouchEvent
     * 触摸屏幕中，如果在ACTION_DOWN时返回true，则不会再有ACTION_MOVE和ACTION_UP拦截
     * 如果ACTION_DOWN返回false,ACTION_MOVE中返回true，则不会有ACTION_UP拦截
     *//*
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //如果ACTION_DOWN未被拦截，则有可能被子view消费，一旦消费后，
                //则父容器中OnTouchEvent中就不会接受到ACTION_DOWN事件
                System.out.println("onInterceptTouchEvent ACTION_DOWN");
                touchDownX = event.getX();
                startX = this.getScrollX();

                mScrolling = false;

                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println("onInterceptTouchEvent ACTION_MOVE");

                if(Math.abs(touchDownX - event.getX())>=mTouchSlop){
                    mScrolling = true;
                }
                else{
                    mScrolling = false;
                }

                break;
            case MotionEvent.ACTION_UP:
                //如果ACTION_MOVE判断是点击就会进入ACTION_UP，是滚动则不会进入
                System.out.println("onInterceptTouchEvent ACTION_UP");

                //返回false以便子控件接收ACTION_UP事件
                mScrolling = false;

                break;
        }

        return mScrolling;
    }

    *//**
     * onTouchEvent() 用于处理事件，返回值决定当前控件是否消费（consume）了这个事件。
     * 可能你要问是否消费了又区别吗，反正我已经针对事件编写了处理代码？
     * 答案是有区别！比如ACTION_MOVE或者ACTION_UP发生的前提是一定曾经发生了ACTION_DOWN，
     * 如果你没有消费ACTION_DOWN，那么系统会认为ACTION_DOWN没有发生过，
     * 所以ACTION_MOVE或者ACTION_UP就不能被捕获。
     * *//*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG,"ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG,"ACTION_MOVE");
                int offsetX = Float.valueOf(startX + touchDownX - event.getX() ).intValue();

                if (offsetX>=0 && offsetX<=this.getWidth()) {
                    scrollTo(offsetX,0);
                }

//                velocityTracker.addMovement(event);

                break;
            case MotionEvent.ACTION_UP:
                System.out.println("ACTION_UP");

//                velocityTracker.computeCurrentVelocity(500);

                //从左往右,velocity>0,实际是想往回拔
//                int speed = -Float.valueOf(velocityTracker.getXVelocity()).intValue();

//                Log.i(TAG,"scrollX:" + this.getScrollX() + ",speed:" + speed);

//                //开始滚动->起始点，偏移距离,持续事件
//                mScroller.startScroll(this.getScrollX(), 0,speed, 0,2000);
//                velocityTracker.recycle();
                break;
            default:
                break;
        }

        return true;
    }
*/

    /*    这个函数很重要，请求禁止父容器拦截触摸事件  */
    public void setParentInterceptTouchEvent(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }

}
