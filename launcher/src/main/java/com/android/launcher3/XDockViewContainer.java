package com.android.launcher3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.webeye.launcher.R;

/**
 * Created by YUANYL2 on 2015/6/1.
 * XDockViewContainer contains a group of shortcuts and a folder while editing.
 */
public class XDockViewContainer extends ViewGroup {
    private static final String TAG = "XDockViewContainer";

    private XDockView mDockView;
    private XDockViewLayout mLayout;
    private int mCellWidth = 0;
    private int mCellHeight = 0;

    private LauncherAppState mAppState;
    private DeviceProfile mProfile;

    // Implements the @ViewGroup interface.
    public XDockViewContainer(Context context) {
        this(context, null);
    }

    public XDockViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XDockViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mAppState = LauncherAppState.getInstance();
        mProfile = mAppState.getDynamicGrid().getDeviceProfile();
        this.setClipChildren(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mLayout == null) {
            mLayout = (XDockViewLayout) findViewById(R.id.dock_layout);
        }
        if (mDockView == null) {
            mDockView = (XDockView) getParent();
        }

        mCellHeight = mProfile.cellHeightPx;// MeasureSpec.getSize(heightMeasureSpec);
        mCellWidth = mProfile.cellWidthPx;

        int count = getChildCount();
        for (int index = 0; index < count; index++) {
            final View child = getChildAt(index);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }

        // if stacked, then measure width with stacked width
        if (mDockView.isStackMode() || mDockView.isFolderMode()) {
            setMeasuredDimension(resolveSize(mProfile.widthPx, widthMeasureSpec),
                    resolveSize(mCellHeight + getPaddingTop() + getPaddingBottom(), heightMeasureSpec));
        } else {
            int width = mLayout.getProperWidth();
            if (!mDockView.needAdjustLayout()) {
                int relativeX = mLayout.getRelativeX() - mLayout.getPaddingLeft();
                int screenWidth = mDockView.getDisplayWidth();
                if (width + relativeX < screenWidth) {
                    width = screenWidth - relativeX;
                }
            }
            setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                    resolveSize(mCellHeight + getPaddingTop() + getPaddingBottom(), heightMeasureSpec));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mLayout == null) {
            mLayout = (XDockViewLayout) findViewById(R.id.dock_layout);
        }
        if (mDockView == null) {
            mDockView = (XDockView) getParent();
        }
        final View layout = getChildAt(0);
        final View folderIcon = getChildAt(1);

        int w = layout.getMeasuredWidth();
        int h = layout.getMeasuredHeight();

        int layoutLeft = 0;
        int layoutRight = 0;
        if (folderIcon != null) {
            layoutLeft = (int) layout.getX();
            layoutRight = r - folderIcon.getMeasuredWidth() - mLayout.getPaddingRight();
        }

        if (mDockView.isStackMode()) {
            if (w > layoutRight - layoutLeft) {
                w = layoutRight - layoutLeft;
            }
        } else if (mDockView.isNormalMode()) {
            int relativeX = mLayout.getRelativeX() - mLayout.getPaddingLeft();
            int screenWidth = mDockView.getDisplayWidth();
            if (w + relativeX < screenWidth) {
                w = screenWidth - relativeX;
            }
        }

        layout.layout(0, 0, w, h);

        if (mDockView.isNormalMode()) {
            return;
        } else if (mDockView.isStackMode()) {
            if (folderIcon != null) {
                int width = folderIcon.getMeasuredWidth();
                int height = folderIcon.getMeasuredHeight();
                int left = r - width - mLayout.getPaddingRight();
                int top = mLayout.getPaddingTop();
                folderIcon.layout(left, top, left + width, top + height);
            }
        } else if (mDockView.isFolderMode()) {
            if (folderIcon != null) {
                int width = folderIcon.getMeasuredWidth();
                int height = folderIcon.getMeasuredHeight();
                int left = (getMeasuredWidth() - width) / 2;
                int top = mLayout.getPaddingTop();

                folderIcon.layout(left, top, left + width, top + height);
            }
        }
    }

}
