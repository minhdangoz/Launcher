/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.webeye.launcher.R;


/**
 * Implements a DropTarget.
 */
public class ButtonDropTarget extends TextView implements DropTarget, DragController.DragListener {

    protected final int mTransitionDuration;

    /* Lenovo-SW 20150514 zhaoxin5 add for launher single/double layer support */
    protected static Launcher mLauncher;
    /* Lenovo-SW 20150514 zhaoxin5 add for launher single/double layer support */
    private int mBottomDragPadding;
    protected TextView mText;
    protected SearchDropTargetBar mSearchDropTargetBar;

    /** Whether this drop target is active for the current drag */
    protected boolean mActive;

    /** The paint applied to the drag view on hover */
    protected int mHoverColor = 0;

    public ButtonDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources r = getResources();
        mTransitionDuration = r.getInteger(R.integer.config_dropTargetBgTransitionDuration);
        mBottomDragPadding = r.getDimensionPixelSize(R.dimen.drop_target_drag_padding);
    }

    void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public boolean acceptDrop(DragObject d) {
        return false;
    }

    public void setSearchDropTargetBar(SearchDropTargetBar searchDropTargetBar) {
        mSearchDropTargetBar = searchDropTargetBar;
    }

    protected Drawable getCurrentDrawable() {
        Drawable[] drawables = getCompoundDrawablesRelative();
        for (int i = 0; i < drawables.length; ++i) {
            if (drawables[i] != null) {
                return drawables[i];
            }
        }
        return null;
    }

    public void onDrop(DragObject d) {
    }

    public void onFlingToDelete(DragObject d, int x, int y, PointF vec) {
        // Do nothing
    }

    public void onDragEnter(DragObject d) {
        d.dragView.setColor(mHoverColor);
    }

    public void onDragOver(DragObject d) {
        // Do nothing
    }

    public void onDragExit(DragObject d) {
        d.dragView.setColor(0);
    }

    public void onDragStart(DragSource source, Object info, int dragAction) {
    	/** Lenovo-SW zhaoxin5 20150702 LANCHROW-187 START */
    	mLauncher.setNeedInterruptOnDragEnd(false);
    	if(!mLauncher.getWorkspace().isInOverviewMode()) {
        	mLauncher.setFullScreen(true);	
    	}
    	/** Lenovo-SW zhaoxin5 20150702 LANCHROW-187 START */
    }

    public boolean isDropEnabled() {
        return mActive;
    }

    public void onDragEnd() {
    	/** Lenovo-SW zhaoxin5 20150702 LANCHROW-187 START */
    	if(!mLauncher.getWorkspace().isInOverviewMode() && !mLauncher.needInterruptOnDragEnd()) {
        	if(mLauncher.isQuickShareAnimatorRunning()) {
        		this.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
		        		mLauncher.setFullScreen(false);	
					}
				}, 300 + 30);
        	} else {
        		/** Lenovo-SW zhaoxin5 20150723 LANCHROW-232 START */
        		// SearchDropTargetBar会首先收到onDragEnd的回调,
        		// 然后会进行SearchDropTargetBar的渐隐动画,
        		// 如果此时关闭全屏幕显示,会出现Share或者是Uninstall砸到下面
        		// app上面的效果,因此先等SearchDropTargetBar完全消失以后,
        		// 再关闭全屏显示
        		this.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
		        		mLauncher.setFullScreen(false);	
					}
				}, SearchDropTargetBar.sTransitionInDuration + 30);
        		/** Lenovo-SW zhaoxin5 20150723 LANCHROW-232 END */
        	}
    	}
    	/** Lenovo-SW zhaoxin5 20150702 LANCHROW-187 START */
    }

    @Override
    public void getHitRectRelativeToDragLayer(android.graphics.Rect outRect) {
        super.getHitRect(outRect);
        outRect.bottom += mBottomDragPadding;

        int[] coords = new int[2];
        mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, coords);
        outRect.offsetTo(coords[0], coords[1]);
    }

    private boolean isRtl() {
        return (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    Rect getIconRect(int viewWidth, int viewHeight, int drawableWidth, int drawableHeight) {
        DragLayer dragLayer = mLauncher.getDragLayer();

        // Find the rect to animate to (the view is center aligned)
        Rect to = new Rect();
        dragLayer.getViewRectRelativeToSelf(this, to);

        final int width = drawableWidth;
        final int height = drawableHeight;

        final int left;
        final int right;

        if (isRtl()) {
            right = to.right - getPaddingRight();
            left = right - width;
        } else {
            left = to.left + getPaddingLeft();
            right = left + width;
        }

        final int top = to.top + (getMeasuredHeight() - height) / 2;
        final int bottom = top +  height;

        to.set(left, top, right, bottom);

        // Center the destination rect about the trash icon
        final int xOffset = (int) -(viewWidth - width) / 2;
        final int yOffset = (int) -(viewHeight - height) / 2;
        to.offset(xOffset, yOffset);

        return to;
    }

    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }
}
