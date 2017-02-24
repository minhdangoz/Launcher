/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.klauncher.launcher.R;

/*
 * Ths bar will manage the transition between the QSB search bar and the delete drop
 * targets so that each of the individual IconDropTargets don't have to.
 */
public class SearchDropTargetBar extends FrameLayout implements DragController.DragListener {

    public static final int sTransitionInDuration = 200;
    private static final int sTransitionOutDuration = 175;

    private ObjectAnimator mDropTargetBarAnim;
    private ObjectAnimator mQSBSearchBarAnim;
    private static final AccelerateInterpolator sAccelerateInterpolator =
            new AccelerateInterpolator();

    private boolean mIsSearchBarHidden;
    private View mQSBSearchBar;
    private View mDropTargetBar;
    //private ButtonDropTarget mInfoDropTarget;
    private ButtonDropTarget mDeleteDropTarget;
    /** Lenovo-SW zhaoxin5 20150617 add for qucik share START */
    private ButtonDropTarget mQuickShareDropTarget;
    public QuickShareDropTarget getQuickShareDropTarget() {
    	return (QuickShareDropTarget) mQuickShareDropTarget;
    }
    /** Lenovo-SW zhaoxin5 20150617 add for qucik share END */
    private int mBarHeight;
    private boolean mDeferOnDragEnd = false;

    private Drawable mPreviousBackground;
    private boolean mEnableDropDownDropTargets;

    public SearchDropTargetBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchDropTargetBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setup(Launcher launcher, DragController dragController) {
        dragController.addDragListener(this);
        //dragController.addDragListener(mInfoDropTarget);
        dragController.addDragListener(mDeleteDropTarget);
        //dragController.addDropTarget(mInfoDropTarget);
        dragController.addDropTarget(mDeleteDropTarget);
        /** Lenovo-SW zhaoxin5 20150617 add for qucik share START */
        dragController.addDragListener(mQuickShareDropTarget);
        dragController.addDropTarget(mQuickShareDropTarget);
        mQuickShareDropTarget.setLauncher(launcher);
        /** Lenovo-SW zhaoxin5 20150617 add for qucik share END */
        dragController.setFlingToDeleteDropTarget(mDeleteDropTarget);
        //mInfoDropTarget.setLauncher(launcher);
        mDeleteDropTarget.setLauncher(launcher);

        setupQSB(launcher);
    }

    public void setupQSB(Launcher launcher) {
        mQSBSearchBar = launcher.getQsbBar();
        if (mEnableDropDownDropTargets) {
            mQSBSearchBarAnim = LauncherAnimUtils.ofFloat(mQSBSearchBar, "translationY", 0,
                    -mBarHeight);
        } else {
            mQSBSearchBarAnim = LauncherAnimUtils.ofFloat(mQSBSearchBar, "alpha", 1f, 0f);
        }
        setupAnimation(mQSBSearchBarAnim, mQSBSearchBar);
    }

    private void prepareStartAnimation(View v) {
        // Enable the hw layers before the animation starts (will be disabled in the onAnimationEnd
        // callback below)
        v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void setupAnimation(ObjectAnimator anim, final View v) {
        anim.setInterpolator(sAccelerateInterpolator);
        anim.setDuration(sTransitionInDuration);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get the individual components
        mDropTargetBar = findViewById(R.id.drag_target_bar);
        //mInfoDropTarget = (ButtonDropTarget) mDropTargetBar.findViewById(R.id.info_target_text);
        mDeleteDropTarget = (ButtonDropTarget) mDropTargetBar.findViewById(R.id.delete_target_text);
        /** Lenovo-SW zhaoxin5 20150617 add for qucik share START */
        mQuickShareDropTarget = (ButtonDropTarget) mDropTargetBar.findViewById(R.id.quick_share_target_text);
        /** Lenovo-SW zhaoxin5 20150617 add for qucik share END */
        
        //mInfoDropTarget.setSearchDropTargetBar(this);
        mDeleteDropTarget.setSearchDropTargetBar(this);
        /** Lenovo-SW zhaoxin5 20150617 add for qucik share START */
        mQuickShareDropTarget.setSearchDropTargetBar(this);
        /** Lenovo-SW zhaoxin5 20150617 add for qucik share END */

        mEnableDropDownDropTargets =
            getResources().getBoolean(R.bool.config_useDropTargetDownTransition);

        // Create the various fade animations
        if (mEnableDropDownDropTargets) {
            LauncherAppState app = LauncherAppState.getInstance();
            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            mBarHeight = grid.searchBarSpaceHeightPx;
            mDropTargetBar.setTranslationY(-mBarHeight);
            mDropTargetBarAnim = LauncherAnimUtils.ofFloat(mDropTargetBar, "translationY",
                    -mBarHeight, 0f);

        } else {
            mDropTargetBar.setAlpha(0f);
            mDropTargetBarAnim = LauncherAnimUtils.ofFloat(mDropTargetBar, "alpha", 0f, 1f);
        }
        setupAnimation(mDropTargetBarAnim, mDropTargetBar);
    }

    public void finishAnimations() {
        prepareStartAnimation(mDropTargetBar);
        mDropTargetBarAnim.reverse();
        prepareStartAnimation(mQSBSearchBar);
        mQSBSearchBarAnim.reverse();
    }

    /*
     * Shows and hides the search bar.
     */
    public void showSearchBar(boolean animated) {
        boolean needToCancelOngoingAnimation = mQSBSearchBarAnim.isRunning() && !animated;
        if (!mIsSearchBarHidden && !needToCancelOngoingAnimation) return;
        if (animated) {
            prepareStartAnimation(mQSBSearchBar);
            mQSBSearchBarAnim.reverse();
        } else {
            mQSBSearchBarAnim.cancel();
            if (mEnableDropDownDropTargets) {
                mQSBSearchBar.setTranslationY(0);
            } else {
                mQSBSearchBar.setAlpha(1f);
            }
        }
        mIsSearchBarHidden = false;
    }
    public void hideSearchBar(boolean animated) {
        boolean needToCancelOngoingAnimation = mQSBSearchBarAnim.isRunning() && !animated;
        if (mIsSearchBarHidden && !needToCancelOngoingAnimation) return;
        if (animated) {
            prepareStartAnimation(mQSBSearchBar);
            mQSBSearchBarAnim.start();
        } else {
            mQSBSearchBarAnim.cancel();
            if (mEnableDropDownDropTargets) {
                mQSBSearchBar.setTranslationY(-mBarHeight);
            } else {
                mQSBSearchBar.setAlpha(0f);
            }
        }
        mIsSearchBarHidden = true;
    }

    /*
     * Gets various transition durations.
     */
    public int getTransitionInDuration() {
        return sTransitionInDuration;
    }
    public int getTransitionOutDuration() {
        return sTransitionOutDuration;
    }

    /*
     * DragController.DragListener implementation
     */
    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        // Animate out the QSB search bar, and animate in the drop target bar
        prepareStartAnimation(mDropTargetBar);
        mDropTargetBarAnim.start();
        if (!mIsSearchBarHidden || mQSBSearchBar.getAlpha() > 0f) {
            prepareStartAnimation(mQSBSearchBar);
            mQSBSearchBarAnim.start();
        }

        // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
        // If Tooltip bar has been visible, set it's alpha to 0.
        if (mIsToolTipShowing && mMultiSelectToolTip.getAlpha() > 0f) {
            prepareStartAnimation(mMultiSelectToolTip);
            mMultiSelectToolTipAnim.reverse();
        }
        // Lenovo-sw:yuanyl2, Add edit mode function. End.
    }

    public void deferOnDragEnd() {
        mDeferOnDragEnd = true;
    }

    @Override
    public void onDragEnd() {
        if (!mDeferOnDragEnd) {
            // Restore the QSB search bar, and animate out the drop target bar
            prepareStartAnimation(mDropTargetBar);
            mDropTargetBarAnim.reverse();
            if (!mIsSearchBarHidden || mQSBSearchBar.getAlpha() < 1f) {
                prepareStartAnimation(mQSBSearchBar);
                mQSBSearchBarAnim.reverse();
            }

            // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
            // If ToolTip bar has been visible, set it's alpha to 1
            if (mIsToolTipShowing && mMultiSelectToolTip.getAlpha() < 1f) {
                prepareStartAnimation(mMultiSelectToolTip);
                mMultiSelectToolTipAnim.start();
            }
            // Lenovo-sw:yuanyl2, Add edit mode function. End.
        } else {
            mDeferOnDragEnd = false;
        }
    }

    public void onSearchPackagesChanged(boolean searchVisible, boolean voiceVisible) {
        if (mQSBSearchBar != null) {
            Drawable bg = mQSBSearchBar.getBackground();
            if (bg != null && (!searchVisible && !voiceVisible)) {
                // Save the background and disable it
                mPreviousBackground = bg;
                mQSBSearchBar.setBackgroundResource(0);
            } else if (mPreviousBackground != null && (searchVisible || voiceVisible)) {
                // Restore the background
                mQSBSearchBar.setBackground(mPreviousBackground);
            }
        }
    }

    public Rect getSearchBarBounds() {
        if (mQSBSearchBar != null) {
            final int[] pos = new int[2];
            mQSBSearchBar.getLocationOnScreen(pos);

            final Rect rect = new Rect();
            rect.left = pos[0];
            rect.top = pos[1];
            rect.right = pos[0] + mQSBSearchBar.getWidth();
            rect.bottom = pos[1] + mQSBSearchBar.getHeight();
            return rect;
        } else {
            return null;
        }
    }

    public View getDropTargetBar() {
        return mDropTargetBar;
    }

    // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
    private boolean mIsToolTipShowing; // to record the Tooltip bar is visible or not.
    private TextView mMultiSelectToolTip;
    private ObjectAnimator mMultiSelectToolTipAnim; // the animation to hide Tooltip
    private ToolTipUpdater mToolTipUpdater;

    public  void setupMultiSelectToolTip(XDockView dockView) {
        mMultiSelectToolTip = (TextView) findViewById(R.id.multi_selecting_tips);
        if (mEnableDropDownDropTargets) {
            mMultiSelectToolTip.setTranslationY(-mBarHeight);
            mMultiSelectToolTipAnim = LauncherAnimUtils.ofFloat(mMultiSelectToolTip, "translationY",
                    -mBarHeight, 0);
        } else {
            mMultiSelectToolTip.setAlpha(0f);
            mMultiSelectToolTipAnim = LauncherAnimUtils.ofFloat(mMultiSelectToolTip, "alpha", 0f, 1f);
        }
        setupAnimation(mMultiSelectToolTipAnim, mMultiSelectToolTip);

        mToolTipUpdater = new ToolTipUpdater();
        dockView.setSelectCountListener(mToolTipUpdater);
    }

    /*
     * Shows and hides the multi-select tool tip bar.
     */
    public void showMultiSelectToolTip(boolean animated) {
        boolean needToCancelOngoingAnimation = mMultiSelectToolTipAnim.isRunning() && !animated;
        if (mIsToolTipShowing && !needToCancelOngoingAnimation) return;

        if (animated) {
            prepareStartAnimation(mMultiSelectToolTip);
            mMultiSelectToolTipAnim.start();
        } else {
            mMultiSelectToolTipAnim.cancel();
            if (mEnableDropDownDropTargets) {
                mMultiSelectToolTip.setTranslationY(0);
            } else {
                mMultiSelectToolTip.setAlpha(1f);
            }
        }
        mIsToolTipShowing = true;
    }

    public void hideMultiSelectToolTip(boolean animated) {
        boolean needToCancelOngoingAnimation = mMultiSelectToolTipAnim.isRunning() && !animated;
        if (!mIsToolTipShowing && !needToCancelOngoingAnimation) return;

        if (animated) {
            prepareStartAnimation(mMultiSelectToolTip);
            mMultiSelectToolTipAnim.reverse();
        } else {
            mMultiSelectToolTipAnim.cancel();
            if (mEnableDropDownDropTargets) {
                mMultiSelectToolTip.setTranslationY(-mBarHeight);
            } else {
                mMultiSelectToolTip.setAlpha(0f);
            }
        }
        mIsToolTipShowing = false;
    }

    private void tooltipSetText(final int textID) {
        mMultiSelectToolTip.setText(textID);
        AlphaAnimation show = new AlphaAnimation(0f, 1f);
        show.setDuration(sTransitionOutDuration);
        show.setFillAfter(true);
        show.setFillBefore(true);
        mMultiSelectToolTip.startAnimation(show);
    }

    public void updateToolTip(int textID) {
        if (mMultiSelectToolTip == null) return;
        prepareStartAnimation(mMultiSelectToolTip);
        tooltipSetText(textID);
    }

    private class ToolTipUpdater implements XDockView.SelectCountListener {
        @Override
        public void onSelectCountChanged(int count) {
            if (count == 0) {
                mMultiSelectToolTip.setText(R.string.multi_selecting_tips);
            } else {
                mMultiSelectToolTip.setText(getResources().getString(R.string.select_item_count, count));
            }
        }
    }
    // Lenovo-sw:yuanyl2, Add edit mode function. End.
}
