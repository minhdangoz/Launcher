/*
 * FolderIconStyleList.java
 *
 *
 * 2015/06/08
 *
 * Copyright notice
 */
package com.android.launcher3;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.settings.SettingsProvider;
import com.webeye.launcher.R;
/**
 * 
   the alignment base class of folders
 *
 * @version     
        2015/06/08
 * @author          
        Yanjun zhang
 */
public abstract class FolderIconStyleBase {
	public int mNumItems;
	public FolderIcon mIcon;
	// These variables are all associated with the drawing of the preview; they
	// are stored
	// as member variables for shared usage and to avoid computation on each
	// frame
	int mIntrinsicIconSize;
	int mTotalWidth = -1;
	int mPreviewOffsetX;
	int mPreviewOffsetY;
	boolean mAnimating = false;

	/** Lenovo-SW zhaoxin5 20150909 fix bug when show label in hotseat, the folder shows strangely START */
	boolean mNeedForceComputePreviewParams = false;
	/** Lenovo-SW zhaoxin5 20150909 fix bug when show label in hotseat, the folder shows strangely END */
	
	// The amount of vertical spread between items in the stack [0...1]
	protected float mShiftFactor;
	protected int mAvailableSpaceInPreview;
	protected int mBaselineIconSize;
	protected float mBaselineIconScale;
	protected float mMaxPerspectiveShift;
	// The degree to which the item in the back of the stack is scaled [0...1]
	// (0 means it's not scaled at all, 1 means it's scaled to nothing)
	protected static final float PERSPECTIVE_SCALE_FACTOR = 0.35f;
	protected Launcher mLauncher;

	protected Rect mOldBounds = new Rect();

	public PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0,
			0, 0, 0);
	public PreviewItemDrawingParams mAnimParams = new PreviewItemDrawingParams(
			0, 0, 0, 0);

	public abstract PreviewItemDrawingParams computePreviewItemDrawingParams(
			int index, PreviewItemDrawingParams params);

	public abstract void drawPreview(Canvas canvas, ArrayList<View> items,
			Boolean hidden);

	public abstract void computePreviewDrawingParams(int drawableSize,
			int totalSize);
	public abstract void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params);
	/**
	 * Constructs a new FolderIconStyle
	 */
	public FolderIconStyleBase(Launcher launcher, FolderIcon icon) {

		// TODO Auto-generated constructor stub
		if (launcher != null && icon != null) {
			mLauncher = launcher;
			mIcon = icon;
		} else {
			return;
		}

	}

	class PreviewItemDrawingParams {
		PreviewItemDrawingParams(float transX, float transY, float scale,
				int overlayAlpha) {
			this.transX = transX;
			this.transY = transY;
			this.scale = scale;
			this.overlayAlpha = overlayAlpha;
		}

		float transX;
		float transY;
		float scale;
		int overlayAlpha;
		Drawable drawable;
	}

	public Drawable getTopDrawable(TextView v) {
		Drawable d = v.getCompoundDrawables()[1];
		return (d instanceof PreloadIconDrawable) ? ((PreloadIconDrawable) d).mIcon
				: d;
	}



	public void animateFirstItem(final Drawable d, int duration,
			final boolean reverse, final Runnable onCompleteRunnable) {
		final PreviewItemDrawingParams finalParams = computePreviewItemDrawingParams(
				0, null);

		final float scale0 = 1.0f;
		final float transX0 = (mAvailableSpaceInPreview - d.getIntrinsicWidth()) / 2;
		final float transY0 = (mAvailableSpaceInPreview - d
				.getIntrinsicHeight()) / 2 + mIcon.getPaddingTop();
		mAnimParams.drawable = d;

		ValueAnimator va = LauncherAnimUtils.ofFloat(mIcon, 0f, 1.0f);
		va.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				float progress = (Float) animation.getAnimatedValue();
				if (reverse) {
					progress = 1 - progress;
					mIcon.mPreviewBackground.setAlpha(progress);
				}

				mAnimParams.transX = transX0 + progress
						* (finalParams.transX - transX0);
				mAnimParams.transY = transY0 + progress
						* (finalParams.transY - transY0);
				mAnimParams.scale = scale0 + progress
						* (finalParams.scale - scale0);
				mIcon.invalidate();
			}
		});
		va.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mAnimating = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimating = false;
				if (onCompleteRunnable != null) {
					mLauncher.runOnUiThread(onCompleteRunnable);
				}
			}
		});
		va.setDuration(duration);
		va.start();
	}

	public void computePreviewDrawingParams(Drawable d) {
		computePreviewDrawingParams(d.getIntrinsicWidth(),
				mIcon.getMeasuredWidth());
	}

	public float getLocalCenterForIndex(int index, int[] center) {
		mParams = computePreviewItemDrawingParams(Math.min(mNumItems, index),
				mParams);

		mParams.transX += mPreviewOffsetX;
		mParams.transY += mPreviewOffsetY;
		float offsetX = mParams.transX + (mParams.scale * mIntrinsicIconSize)
				/ 2;
		float offsetY = mParams.transY + (mParams.scale * mIntrinsicIconSize)
				/ 2;

		center[0] = (int) Math.round(offsetX);
		center[1] = (int) Math.round(offsetY);
		return mParams.scale;
	}
}
