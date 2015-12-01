package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import com.android.launcher3.interpolator.LenovoCubicBezierInterpolator;
import com.webeye.launcher.ext.LauncherLog;
import com.webeye.launcher.R;

/**
 * Created by YUANYL2 on 2015/6/1.
 * MultiSelectShortcutGroup contains shortcuts for editing mode.
 */
public class XDockViewLayout extends ViewGroup {
    private static final String TAG = "XDockViewLayout";

    // TODO: CELL_DISTANCE, CELL_STACK_DISTANCE should be defined in DynamicGrid.
    private static final int CELL_DISTANCE = 30;
    private static final int CELL_STACK_DISTANCE = 10;

    private Launcher mLauncher;
    private LauncherAppState mAppState;
    private DeviceProfile mProfile;
    private IconCache mIconCache;
    private XDockView mDockView;

    private int mCellWidth = 0;
    private int mCellHeight = 0;
    private int mCellDistance = 10;
    private int mStackMaxWidth = 0;

    private int mCellStackDistance = CELL_STACK_DISTANCE;

    private static final int ANIM_STACK_FIRST_HALF_DURATION = 300;
    private AnimatorSet mLayoutAnim;
    private boolean mLayoutAnimCanceled = false;
    private boolean mLayoutAnimStart = false;

    public static final int timeDelay = 40;
    public static final long durationTime = 400;

    public XDockViewLayout(Context context) {
        this(context, null);
    }

    public XDockViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XDockViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCellDistance = CELL_DISTANCE;

        mAppState = LauncherAppState.getInstance();
        mProfile = mAppState.getDynamicGrid().getDeviceProfile();
        mIconCache = mAppState.getIconCache();

        mCellHeight = mProfile.cellHeightPx;
        mCellWidth = mProfile.cellWidthPx;
        mCellStackDistance = mCellWidth / 2;

        mStackMaxWidth = mProfile.widthPx - mProfile.cellWidthPx - mProfile.edgeMarginPx * 2;

        this.setClipToPadding(false);
    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
        mDockView = launcher.getDockView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // it need adjust again if rotate pad
        if (mDockView.isStackMode() && h != oldh) {
            adjustStackDistance();
            adjustStackedLayout();
        }
    }

    public int getIconDis(){
        return mCellWidth + mCellDistance;
    }

    public View addItemView(ItemInfo info, int index) {
        View view;

        switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (info.container == NO_ID && info instanceof AppInfo) {
                    info = new ShortcutInfo((AppInfo) info);
                }

                view = mLauncher.createShortcut(R.layout.application, this, (ShortcutInfo) info);
                BubbleTextView icon = (BubbleTextView) view;
                icon.setTextVisibility(false);
                break;

            default:
                throw new IllegalArgumentException("Unknown item type:" + info.itemType);
        }

        float scale = mDockView.getItemScale();
        view.setPivotX(0);
        view.setPivotY(0);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setVisibility(View.INVISIBLE);
        addViewItem(view, index);
		info.dockCellX = index;
        if (!mDockView.isStackMode()) {
            performLayoutAnimation(index, false, null);
        } else {
            performLayoutAnimation(-1, true, null);
        }

        return view;
    }

    public void performLayoutAnimation(int index, final boolean stack, final Runnable onCompleteRunnable) {
        mDockView.setAnimStacking(true);
        if (stack) {
            adjustStackDistance();
        }

        boolean left = true; // whether layout item from left to right.
        int screenWidth = mDockView.getDisplayWidth();
        int leftLen = Math.abs(getRelativeX());
        int rightLen = getMeasuredWidth() - leftLen - screenWidth;
        int rightDelta = rightLen;
        int leftDelta = 0;
        if ((rightLen > 0 && rightLen < mCellWidth + mCellDistance)
                || (getRelativeX() >= 0 && rightLen < 0)) {
            if (!(Math.abs(getRelativeX()) < mCellDistance)) {
                left = false;
            }
            // layout from left when adding items.
            if (index != -1) {
                left = true;
            }
        }

        int indexDel = -1;
        for (int i = 0; i < getChildCount(); i++) {
            final View item = getChildItemAt(i);
            if (item.getVisibility() == View.GONE) {
                indexDel = i;
            }
        }

        if (mLayoutAnim != null && mLayoutAnim.isRunning()) {
            mLayoutAnim.cancel();
        }
        mLayoutAnim = LauncherAnimUtils.createAnimatorSet();

        for (int i = 0, j = 0; i < this.getChildCount(); i++) {
            final View item = this.getChildItemAt(i);
            if (item.getVisibility() == View.GONE) {
                continue;
            }
            ItemInfo info = (ItemInfo) item.getTag();
            int preCellX = info.dockCellX;
            int w = mCellWidth;
            int preDistance;
            int distance;
            preDistance = getRelativeX(item);
            if (!stack) {
                if (!left) {
                    // layout from right to left
                    if (rightLen <= 1) {
                        rightDelta = 0;
                    }
                    leftDelta = w + mCellDistance - rightDelta;
                    if (leftLen < leftDelta) {
                        leftDelta = leftLen;
                        rightDelta = w + mCellDistance - leftDelta;
                    }
                    if (i > indexDel) {
                        distance = preDistance - rightDelta;
                    } else {
                        distance = preDistance + leftDelta;
                    }
                } else {
                    // layout from left to right
                    distance = (w + mCellDistance) * j + getPaddingLeft();// + mPreRelativeX;
                }

                if (j == index) {
                    preDistance = distance;
                }
            } else {
                distance = (mCellStackDistance) * j + getPaddingLeft() - getRelativeX();// - mDockView.getScrollX();
            }

            info.dockCellX = j;
            j++;

            ValueAnimator itemAnim = ValueAnimator.ofFloat(preDistance, distance);

            itemAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) (animation.getAnimatedValue());
                    if (XDockViewLayout.this != null) {
                        setRelativeX(item, value);
                    }
                }
            });

            itemAnim.setInterpolator(new AccelerateInterpolator((float) 0.2));
            mLayoutAnim.play(itemAnim);

            if (item.getVisibility() == View.VISIBLE) {
//			    item.startAnimation(rotate);
            }
        } // End for

        mLayoutAnim.setDuration(ANIM_STACK_FIRST_HALF_DURATION);
        mLayoutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator arg0) {
                mLayoutAnimStart = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mLayoutAnimCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mLayoutAnimStart = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
                if (!mLayoutAnimCanceled) {
                    if (stack) {
                        adjustStackedLayout();
                        // setRelativeX(0);
                    } else {
                        adjustLayout();
                        // setRelativeX(mPreRelativeX);
                    }
                    dumpItems();
                }
                mDockView.setAnimStacking(false);
                mLayoutAnimCanceled = false;
            }
        });

        mLayoutAnim.start();
    }

    // Note that we do layout animations before the layout actually changes.
    // Because
    // we not not want it measure and layout during animation.

    private ValueAnimator getIconValueAnimator(final View item, int i, int j, boolean left, int index,
                                               int index_del, int left_len, int right_len, int left_delta, int right_delta) {
        ItemInfo info = (ItemInfo) item.getTag();
        int preCellX = info.dockCellX;
        int w = mCellWidth;
        int preDistance = 0;
        int mdistance = 0;
        preDistance = getRelativeX(item);
        if (!left) {
            // layout from right to left
            if (right_len <= 1) {
                right_delta = 0;
            }
            left_delta = w + mCellDistance - right_delta;
            if (left_len < left_delta) {
                left_delta = left_len;
                right_delta = w + mCellDistance - left_delta;
            }
            if (i > index_del) {
                mdistance = preDistance - right_delta;
            } else {
                mdistance = preDistance + left_delta;
            }
        } else {
            // layout from left to right
            mdistance = (w + mCellDistance) * j + getPaddingLeft();// +
            // mPreRelativeX;
        }

        if (j == index) {
            preDistance = mdistance;
        }

        info.dockCellX = j;
        ValueAnimator itemAnim = ValueAnimator.ofFloat(preDistance, mdistance);

        itemAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) (animation.getAnimatedValue());
                if (XDockViewLayout.this != null) {
                    setRelativeX(item, value);
                }
            }
        });
        itemAnim.setDuration(durationTime);
        itemAnim.setInterpolator(getCubicBezierInterpolator(0.71f, 0.0f, 0.0f,
                1.0f));
        return itemAnim;

    }

    private void addIconAnimation(int index, final Runnable onCompleteRunnable) {

        mDockView.setAnimStacking(true);

        // whether if we layout item from left to right
        boolean left = true;
        int screenWidth = mDockView.getDisplayWidth();
        int left_len = Math.abs(getRelativeX());
        int right_len = getMeasuredWidth() - left_len - screenWidth;
        int right_delta = right_len;
        int left_delta = 0;

        if (mLayoutAnim != null && mLayoutAnim.isRunning()) {
            mLayoutAnim.cancel();
        }
        mLayoutAnim = LauncherAnimUtils.createAnimatorSet();

        int paddingLeft = getPaddingLeft();

        int leftIconIndex = 0;
        int rightIconIndex = 0;
        int IconDis = mCellWidth + mCellDistance;
        int IconSize = this.getChildCount();

        if (left_len <= paddingLeft) {
            int leftDis = paddingLeft - left_len;
            leftIconIndex = 0;
            rightIconIndex = (screenWidth - leftDis) / IconDis;   // first icon index is 0;
        } else {
            int leftDis = left_len - paddingLeft;
            leftIconIndex = leftDis / IconDis;
            rightIconIndex = (screenWidth + leftDis) / IconDis;
        }
        if (rightIconIndex > IconSize - 1) {
            rightIconIndex = IconSize - 1;
        }

        if (leftIconIndex > 0) {
            leftIconIndex = leftIconIndex - 1;
        }
//		if(rightIconIndex < IconSize - 1){
//			rightIconIndex = rightIconIndex + 1;
//		}

        //	direction is to right.
        int actionDelayCount = 0;
        for (int i = IconSize - 1, j = IconSize - 1; i >= 0; i--) {
            final View item = this.getChildItemAt(i);

            ValueAnimator itemAnim = getIconValueAnimator(item, i, j, left,
                    index, -1, left_len, right_len, left_delta,
                    right_delta);
            j--;
            if (i >= leftIconIndex && i <= rightIconIndex) {
                mLayoutAnim.play(itemAnim).after(timeDelay * actionDelayCount);
                actionDelayCount++;
            } else {
                mLayoutAnim.play(itemAnim);
            }

        }

        // mLayoutanim.setDuration(XDockView.ANIM_STACK_FIRST_HALF_DURATION);
        mLayoutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator arg0) {
                mLayoutAnimStart = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                int mCount = mLayoutAnim.getChildAnimations().size();
                for (int i = 0; i < mCount; i++) {
                    ValueAnimator anim = (ValueAnimator) mLayoutAnim.getChildAnimations().get(i);
                    if (anim.isStarted()) {
                        anim.end();
                    } else {
                        anim.start();
                        anim.end();
                    }
                }
                mLayoutAnimCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mLayoutAnimStart = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
                if (!mLayoutAnimCanceled) {
                    adjustLayout();
                    dumpItems();
                }
                mDockView.setAnimStacking(false);
                mLayoutAnimCanceled = false;
            }
        });

        mLayoutAnim.start();
    }

    private void deleteIconAnimation(int index, final Runnable onCompleteRunnable) {

        mDockView.setAnimStacking(true);

        // whether if we layout item from left to right
        boolean left = true; // true is  icons have the only direction.
        int screenWidth = mDockView.getDisplayWidth();
        int leftLen = Math.abs(getRelativeX());
        int rightLen = getMeasuredWidth() - leftLen - screenWidth;
        int rightDelta = rightLen;
        int leftDelta = 0;
        if ((rightLen > 0 && rightLen < mCellWidth + mCellDistance)
                || (getRelativeX() >= 0 && rightLen < 0)) {
            if (!(Math.abs(getRelativeX()) < mCellDistance)) {
                left = false;
            }
        }

        if (rightLen < mCellWidth + mCellDistance
                && Math.abs(getRelativeX()) > 0
                && Math.abs(getRelativeX()) < mCellWidth + mCellDistance) {
            left = false;
        }

        int indexDel = index;
//		for (int i = 0; i < this.getChildCount(); i++) {
//			final View item = this.getChildItemAt(i);
//			if (item.getVisibility() == View.GONE) {
//				index_del = i;
//			}
//		}

        if (mLayoutAnim != null && mLayoutAnim.isRunning()) {
            mLayoutAnim.cancel();
        }
        mLayoutAnim = LauncherAnimUtils.createAnimatorSet();

        int paddingLeft = getPaddingLeft();
        int timeDelay = 40;

        int leftIconIndex = 0;
        int rightIconIndex = 0;
        int IconDis = mCellWidth + mCellDistance;
        int IconSize = this.getChildCount();

        if (leftLen <= paddingLeft) {
            int leftDis = paddingLeft - leftLen;
            leftIconIndex = 0;
            rightIconIndex = (screenWidth - leftDis) / IconDis;   // first icon index is 0;
        } else {
            int leftDis = leftLen - paddingLeft;
            leftIconIndex = leftDis / IconDis;
            rightIconIndex = (screenWidth + leftDis) / IconDis;
        }

        if (rightIconIndex > IconSize - 1) {
            rightIconIndex = IconSize - 1;
        }

        if (left) {
            boolean directionToRight = false;

            if (getRelativeX() >= 0) {
                directionToRight = false;
            } else if (rightLen > mCellWidth + mCellDistance) {
                directionToRight = false;
            } else {
                directionToRight = true;
            }

            if (directionToRight) {
                if (leftIconIndex > 0) {
                    leftIconIndex = leftIconIndex - 1;
                }
//				if(rightIconIndex < IconSize - 1){
//					rightIconIndex = rightIconIndex + 1;
//				}
                int actionDelayCount = 0;
                for (int i = IconSize - 1, j = IconSize - 1; i >= 0; i--) {
                    final View item = this.getChildItemAt(i);
                    if (item.getVisibility() == View.GONE) {
                        continue;
                    }
                    j--;
                    ValueAnimator itemAnim = getIconValueAnimator(item, i, j, left,
                            -1, indexDel, leftLen, rightLen, leftDelta,
                            rightDelta);
                    if (i >= leftIconIndex && i <= rightIconIndex) {
                        mLayoutAnim.play(itemAnim).after(timeDelay * actionDelayCount);
                        actionDelayCount++;
                    } else {
                        mLayoutAnim.play(itemAnim);
                    }
                }
            } else if (!directionToRight) {
                if (rightIconIndex < IconSize - 1) {
                    rightIconIndex = rightIconIndex + 1;
                }
                int actionDelayCount = 0;
                for (int i = 0, j = 0; i <= IconSize - 1; i++) {
                    final View item = this.getChildItemAt(i);
                    if (item.getVisibility() == View.GONE) {
                        continue;
                    }
                    ValueAnimator itemAnim = getIconValueAnimator(item, i, j, left,
                            -1, indexDel, leftLen, rightLen, leftDelta,
                            rightDelta);
                    j++;
                    if (i >= leftIconIndex && i <= rightIconIndex) {
                        mLayoutAnim.play(itemAnim).after(timeDelay * actionDelayCount);
                        actionDelayCount++;
                    } else {
                        mLayoutAnim.play(itemAnim);
                    }
                }
            }
        } else if (!left) {
            if (leftIconIndex > 0) {
                leftIconIndex = leftIconIndex - 1;
            }
            if (rightIconIndex < IconSize - 1) {
                rightIconIndex = rightIconIndex + 1;
            }

            int actionDelayCount = 0;
            for (int i = indexDel + 1, j = indexDel; i <= IconSize - 1; i++) {
                final View item = this.getChildItemAt(i);
                ValueAnimator itemAnim = getIconValueAnimator(item, i, j, left,
                        -1, indexDel, leftLen, rightLen, leftDelta,
                        rightDelta);
                j++;
                if (i >= leftIconIndex && i <= rightIconIndex) {
                    mLayoutAnim.play(itemAnim).after(timeDelay * actionDelayCount);
                    actionDelayCount++;
                } else {
                    mLayoutAnim.play(itemAnim);
                }
            }

            actionDelayCount = 0;
            for (int i = indexDel - 1, j = indexDel - 1; i >= 0; i--) {
                final View item = this.getChildItemAt(i);
                if (null == item) {
                    continue;
                }
                ValueAnimator itemAnim = getIconValueAnimator(item, i, j, left,
                        -1, indexDel, leftLen, rightLen, leftDelta,
                        rightDelta);
                j--;
                if (i >= leftIconIndex && i <= rightIconIndex) {
                    mLayoutAnim.play(itemAnim).after(timeDelay * actionDelayCount);
                    actionDelayCount++;
                } else {
                    mLayoutAnim.play(itemAnim);
                }
            }

        }

        // mLayoutanim.setDuration(XDockView.ANIM_STACK_FIRST_HALF_DURATION);
        mLayoutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator arg0) {
                mLayoutAnimStart = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                int mCount = mLayoutAnim.getChildAnimations().size();
                for (int i = 0; i < mCount; i++) {
                    ValueAnimator anim = (ValueAnimator) mLayoutAnim.getChildAnimations().get(i);
                    if (anim.isStarted()) {
                        anim.end();
                    } else {
                        anim.start();
                        anim.end();
                    }
                }
                mLayoutAnimCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mLayoutAnimStart = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
                if (!mLayoutAnimCanceled) {
                    adjustLayout();
                    dumpItems();
                }
                mDockView.setAnimStacking(false);
                mLayoutAnimCanceled = false;
            }
        });

        mLayoutAnim.start();
    }

    private LenovoCubicBezierInterpolator getCubicBezierInterpolator(float f1,
                                                                     float f2, float f3, float f4) {
        // 0.25f, 0.13f, 0.27f, 1.0f
        // 0.05f, 0.35f, 0.25f, 1.0f
        // 0.05f, 0.5f, 0.25f, 1.0f
        PointF start = new PointF(f1, f2);
        PointF end = new PointF(f3, f4);
        return new LenovoCubicBezierInterpolator(start, end);
    }


    public boolean animateItemToPosition(final View item, int target,
                                         int duration) {
        if (item == null) {
            return false;
        }
        ItemInfo info = (ItemInfo) item.getTag();
        int formerCellX = info.dockCellX;

        int start;
        int end;
        int deltX;

        if (formerCellX == target) {
            return false;
        }

        if (target >= getChildCount()) {
            target = getChildCount() - 1;

        }

        if (formerCellX == target || formerCellX == -1 || target == -1) {
            return false;
        } else if (formerCellX < target) {
            start = formerCellX + 1;
            end = target;
            deltX = -1;
        } else {
            start = target;
            end = formerCellX - 1;
            deltX = 1;
        }
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        if (deltX == 1) {

            for (int i = end; i >= start; i--) {
                if (i >= getChildCount()) {
                    break;
                }
                final View icon = this.getChildItemAt(i);
                final ItemInfo iconInfo = (ItemInfo) icon.getTag();
                int preCellX = i;
                int preDistance = (mCellWidth + mCellDistance) * preCellX
                        + getPaddingLeft();
                iconInfo.dockCellX = preCellX + deltX;

                int ndistance = (mCellWidth + mCellDistance)
                        * iconInfo.dockCellX + getPaddingLeft();

                // items.set(icon_info.dockCellX, icon);
                // icon.setRelativeX(ndistance);
                ValueAnimator itemAnim;
                if (i == start) {
                    itemAnim = initSwapAnimation(icon, iconInfo, preDistance,
                            ndistance, duration, true);
                } else {
                    itemAnim = initSwapAnimation(icon, iconInfo, preDistance,
                            ndistance, duration, false);
                }
                anim.play(itemAnim);
            }
        } else {
            for (int i = start; i <= end; i++) {
                if (i >= getChildCount()) {
                    break;
                }
                final View icon = this.getChildItemAt(i);
                final ItemInfo iconInfo = (ItemInfo) icon.getTag();
                int preCellX = i;
                int preDistance = (mCellWidth + mCellDistance) * preCellX
                        + getPaddingLeft();
                iconInfo.dockCellX = preCellX + deltX;

                int distance = (mCellWidth + mCellDistance)
                        * iconInfo.dockCellX + getPaddingLeft();

                // items.set(icon_info.dockCellX, icon);
                // icon.setRelativeX(ndistance);

                ValueAnimator itemAnim;
                if (i == end) {
                    itemAnim = initSwapAnimation(icon, iconInfo, preDistance,
                            distance, duration, true);
                } else {
                    itemAnim = initSwapAnimation(icon, iconInfo, preDistance,
                            distance, duration, false);
                }

                anim.play(itemAnim);
            }
        }
        mDockView.setAdjustStatus(false);
        anim.start();

        int distanceToTarget = (mCellWidth + mCellDistance) * target
                + getPaddingLeft();

        info.dockCellX = target;
        setChildIndex(item, info.dockCellX);
        setRelativeX(item, distanceToTarget);

        return true;
    }

    private ValueAnimator initSwapAnimation(final View icon,
                                            final ItemInfo icon_info, final int preDistance,
                                            final int mdistance, int duration, final boolean last) {
        ValueAnimator itemAnim = ValueAnimator.ofFloat(preDistance, mdistance);
        itemAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) (animation.getAnimatedValue());
                if (XDockViewLayout.this != null) {
                    setRelativeX(icon, value);
                }
            }
        });
        itemAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                setChildIndex(icon, icon_info.dockCellX);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                setRelativeX(icon, mdistance);
                if (last) {
                    adjustLayout();
                    invalidate();
                    dumpItems();
                }
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
        itemAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        itemAnim.setDuration(duration);
        return itemAnim;
    }

    public void adjustLayout() {
        requestLayout();
        for (int i = 0; i < this.getChildCount(); i++) {
            View item = getChildItemAt(i);
            if (item.getVisibility() == View.GONE) {
                continue;
            }
            ItemInfo info = (ItemInfo) item.getTag();

            int w = mCellWidth;
            int h = mCellHeight;
            int dis = (mCellDistance + w) * i + getPaddingLeft();
            int left = dis;
            int top = getPaddingTop();
            item.layout(left, top, left + w, top + h);
            info.dockCellX = i;
        }
    }

    public void showLayoutItems() {
        for (int i = 0; i < this.getChildCount(); i++) {
            View item = getChildItemAt(i);
            item.setVisibility(VISIBLE);
        }
    }

    private void adjustStackDistance() {
        int count = this.getChildCount();
        mCellStackDistance = mCellWidth / 3;

        if (mStackMaxWidth != 0) {
            int currentWidth = mCellStackDistance * (count - 1) + mCellWidth + getPaddingLeft() + getPaddingRight();

            if (currentWidth > mStackMaxWidth && count > 1) {
                mCellStackDistance = (mStackMaxWidth - getPaddingLeft() - getPaddingRight() - mCellWidth) / (count - 1);
            }
        }
    }

    /**
     * Do stack animation
     * @param stack collected icons or not
     * @param r
     */
    public void animateStack(boolean stack, Runnable r) {
        performLayoutAnimation(-1, stack, r);
    }

    private void adjustStackedLayout() {
        requestLayout();
        int w = mCellWidth;
        int h = mCellHeight;
        int count = this.getChildCount();
        for (int i = 0, j = 0; i < count; i++) {
            View item = this.getChildItemAt(i);
            if (item.getVisibility() == View.GONE) {
                continue;
            }

            int dis = (mCellStackDistance) * j + getPaddingLeft();
            int left = dis;
            int top = getPaddingTop();
            item.layout(left, top, left + w, top + h);
            j++;
        }
    }

    public int[] getStackPosition() {
        int[] layoutPos = mLauncher.getLocationPosition(this);
        layoutPos[0] = layoutPos[0] + getPaddingLeft();
        layoutPos[1] = layoutPos[1] + getPaddingTop();
        return layoutPos;
    }

    private void dumpItems() {
        if (LauncherLog.DEBUG_EDITMODE) {
            LauncherLog.d(TAG, "dumpItems, getRelativeX()" + getRelativeX());
        }
        for (int i = 0; i < this.getChildCount(); i++) {
            View item = this.getChildItemAt(i);
            ItemInfo icon_info = (ItemInfo) item.getTag();
            if (LauncherLog.DEBUG_EDITMODE) {
                LauncherLog.d(TAG, "dumpItems, item dockCellX: " +/* icon_info.dockCellX +*/ ", index: " + i + ", RelativeX: "
                        + getRelativeX(item) + ", visibility: " + item.getVisibility());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cellWidthSpec = MeasureSpec.makeMeasureSpec(mCellWidth,
                MeasureSpec.AT_MOST);
        int cellHeightSpec = MeasureSpec.makeMeasureSpec(mCellHeight,
                MeasureSpec.AT_MOST);

        final int count = getChildCount();
        for (int index = 0; index < count; index++) {
            final View child = getChildItemAt(index);
            child.measure(cellWidthSpec, cellHeightSpec);
        }

        // TODO if stacked, then measure width with stacked width
        if (mDockView.isStackMode() || mDockView.isFolderMode()) {
            setMeasuredDimension(
                    resolveSize(getProperWidth(), widthMeasureSpec),
                    resolveSize(mCellHeight + getPaddingTop()
                            + getPaddingBottom(), heightMeasureSpec));
        } else {
            setMeasuredDimension(
                    resolveSize(getProperWidth(), widthMeasureSpec),
                    resolveSize(mCellHeight + getPaddingTop()
                            + getPaddingBottom(), heightMeasureSpec));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // we already layout views in animator when adding or removing items, so
        // layout only when layout animation is over
        if (!mDockView.isAnimStacking()) {
            if (mDockView.isStackMode()) {
                adjustStackedLayout();
            } else if (mDockView.isNormalMode()) {
                if (mDockView.needAdjustLayout() && !mLayoutAnimStart) {
                    adjustLayout();
                }
            }
        }
    }

    /*
     * return layout width under different mode, use this method to get width when layout
     * is not measured yet
     */
    public int getProperWidth() {
        int width;
        final int count = getChildCount();
        if (mDockView.isStackMode() || mDockView.isFolderMode()) {
            width = mCellStackDistance * (count - 1) + mCellWidth + getPaddingLeft() + getPaddingRight();
        } else {
            width = (mCellDistance + mCellWidth) * count + getPaddingLeft() + getPaddingRight();
        }
        return width;
    }

    public void removeItem(final View item, boolean needChange) {
        // set GONE to do animation
        item.setVisibility(View.GONE);
        final ItemInfo info = (ItemInfo) item.getTag();
        int index = info.dockCellX;
        Runnable r = new Runnable() {
            public void run() {
                info.dockCellX = -1;
                removeView(item);
                if (mDockView.isDragDelete && mDockView.dragDeleteIsCompleted) {
                    mDockView.dragDeleteIsCompleted = false;
                    mDockView.isDragDelete = false;
                    mDockView.updateSelectCount();
                }
                if (mDockView.isClickDelete && mDockView.clickDeleteIsCompleted) {
                    mDockView.clickDeleteIsCompleted = false;
                    mDockView.isClickDelete = false;
                    mDockView.updateSelectCount();
                }
                if (mDockView.isNeedUpdateSelectCount) {
                    mDockView.isNeedUpdateSelectCount = false;
                    mDockView.updateSelectCount();
                }
                if (mDockView.isRemoveByComponentName == 1) {
                    mDockView.updateSelectCount();
                    mDockView.isRemoveByComponentName = 0;
                }
                if (mDockView.isRemoveByComponentName > 1) {
                    mDockView.isRemoveByComponentName--;
                }
                mDockView.hide();
            }
        };

        if (!mDockView.isStackMode()) {
            if (needChange) {
                deleteIconAnimation(index, r);
            } else {
                r.run();
            }
        } else {
            adjustStackDistance();
            adjustStackedLayout();
            r.run();
        }
    }

    public int[] findTargetCell(int touchX, int touchY) {
        final int[] bestXY = new int[2];
        bestXY[0] = 0;
        bestXY[1] = 0;

        bestXY[0] = (touchX + Math.abs(getRelativeX()) - getPaddingLeft()) / (mCellDistance + mCellWidth);

        if ((touchX + Math.abs(getRelativeX()) - getPaddingLeft()) > (bestXY[0]
                * (mCellDistance + mCellWidth) + getPaddingLeft() + mCellWidth / 2)) {
            bestXY[0]++;
        }
        if (bestXY[0] > getChildCount()) {
            bestXY[0] = getChildCount();
        }

        return bestXY;
    }

    public int getRelativeX() {
        int[] location = new int[2];
        getLocationOnScreen(location);
        return location[0];
    }

    public void setRelativeX(float x) {
        mDockView.scrollTo((int) Math.abs(x), 0);
        // mDockView.scrollBy((int) x, 0);
    }

    private int getRelativeX(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return Math.round(view.getX());
    }

    public void setRelativeX(View view, float x) {
        int left = Math.round(x);
        int top = getPaddingTop();
        int w = mCellWidth;
        int h = mCellHeight;
        view.layout(left, top, left + w, top + h);
    }

    private synchronized void setChildIndex(final View child, final int index) {
        removeView(child);
        addViewItem(child, index);
    }

    public View getChildItemAt(int index) {
        int count = getChildCount() - 1;
        int pos = count - index;
        return getChildAt(pos);
    }

    public int indexOfChildItem(View child) {
        if (indexOfChild(child) == -1) {
            return -1;
        }
        int count = getChildCount() - 1;
        return count - indexOfChild(child);
    }

    /**
     * Add a view to layout.
     *
     * @param child the view will to be added.
     * @param index index of added.
     */
    private void addViewItem(View child, int index) {
        index = getChildCount() - index;
        addView(child, index);
    }
}
