/*
 * FolderIconStyleList.java
 *
 *
 * 2015/06/08
 *
 * Copyright notice
 */
package com.android.launcher3;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.FolderIcon.FolderRingAnimator;
import com.klauncher.launcher.BuildConfig;
import com.klauncher.launcher.R;

import java.util.ArrayList;

/**
 * 
 List of folders
 * 
 * @version 2015/06/08
 * @author Yanjun zhang
 */
public class FolderIconStyleList extends FolderIconStyleBase {
	protected float mOffsetX;
	private Resources mResources;

	public FolderIconStyleList(Launcher launcher, FolderIcon icon) {
		// TODO Auto-generated constructor stub
		super(launcher, icon);
		mResources = mLauncher.getResources();
		mNumItems = 9;
		mShiftFactor = 1f;
		/* Lenovo-sw zhangyj19 add 2015/07/22 modify theme start */
		if (FolderRingAnimator.sSharedInnerRingDrawable != null) {
			mIcon.mPreviewBackground.setImageDrawable(FolderRingAnimator.sSharedInnerRingDrawable);
		} else {
			mIcon.mPreviewBackground.setImageDrawable(mResources.getDrawable(R.drawable.portal_ring_outer_holo));
		}
		/* Lenovo-sw zhangyj19 add 2015/07/22 modify theme end */
	}

	public PreviewItemDrawingParams computePreviewItemDrawingParams(int index, PreviewItemDrawingParams params) {
		Configuration config = mLauncher.getResources().getConfiguration();
//		if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
//			if (index == 0) {
//				index = 2;
//			} else if (index == 2) {
//				index = 0;
//			} else if (index == 3) {
//				index = 5;
//			} else if (index == 5) {
//				index = 3;
//			} else if (index == 6) {
//				index = 8;
//			} else if (index == 8) {
//				index = 6;
//			}
//		}
		int index_order = index;
		index = mNumItems - index - 1;
		float r = (index * 1.0f) / (mNumItems - 1);
		// We want to imagine our coordinates from the bottom left, growing up
		// and to the
		// right. This is natural for the x-axis, but for the y-axis, we have to
		// invert things.
		float transY = 0;
		float transX = 0;
		float totalScale = 0;
		final int overlayAlpha = (int) (80 * (1 - r));

		if (TextUtils.equals(BuildConfig.FLAVOR, "vivo")) {
			if (0 <= index_order && index_order < 3) { // 0 1 2
//			transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX);
				if (index_order == 0) {
					transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX) + 5;
				} else if (index_order == 1) {
					transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX);
				} else {
					transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX) - 8;
				}
				transY = 4 * mOffsetX + 3;
			} else if (3 <= index_order && index_order < 6) { // 3 4 5
//			transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX);
				if (index_order == 3) {
					transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX) + 5;
				} else if (index_order == 4) {
					transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX);
				} else {
					transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX) - 8;
				}

				transY = 4 * mOffsetX + 1 * (mMaxPerspectiveShift + mOffsetX) - 5;
			} else if (6 <= index_order && index_order < 9) { // 6 7 8
//			transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX);
				if (index_order == 6) {
					transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX) + 5;
				} else if (index_order == 7) {
					transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX);
				} else {
					transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX) - 8;
				}

				transY = 4 * mOffsetX + 2 * (mMaxPerspectiveShift + mOffsetX) - 9;
			}
		} else if (Build.MODEL.equals("GN3001") || Build.MODEL.equals("GN5001S")) {
			if (0 <= index_order && index_order < 3) { // 0 1 2
				transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX);
				transY = 4 * mOffsetX - 9;
			} else if (3 <= index_order && index_order < 6) { // 3 4 5
				transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX);
				transY = 4 * mOffsetX + 1 * (mMaxPerspectiveShift + mOffsetX) - 9;
			} else if (6 <= index_order && index_order < 9) { // 6 7 8
				transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX);
				transY = 4 * mOffsetX + 2 * (mMaxPerspectiveShift + mOffsetX) - 9;
			}
		} else if (TextUtils.equals(BuildConfig.FLAVOR, "meitu")) {
//			if (0 <= index_order && index_order < 3) { // 0 1 2
//				transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX);
//				transY = 4 * mOffsetX;
//			} else if (3 <= index_order && index_order < 6) { // 3 4 5
//				transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX);
//				transY = 4 * mOffsetX + 1 * (mMaxPerspectiveShift + mOffsetX);
//			} else if (6 <= index_order && index_order < 9) { // 6 7 8
//				transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX);
//				transY = 4 * mOffsetX + 2 * (mMaxPerspectiveShift + mOffsetX);
//			}
			if (0 <= index_order && index_order < 3) { // 0 1 2
//			transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX);
				if (index_order == 0) {
					transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX) + 5;
				} else if (index_order == 1) {
					transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX);
				} else {
					transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX) - 8;
				}
				transY = 4 * mOffsetX;
			} else if (3 <= index_order && index_order < 6) { // 3 4 5
//			transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX);
				if (index_order == 3) {
					transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX) + 5;
				} else if (index_order == 4) {
					transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX);
				} else {
					transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX) - 8;
				}

				transY = 4 * mOffsetX + 1 * (mMaxPerspectiveShift + mOffsetX);
			} else if (6 <= index_order && index_order < 9) { // 6 7 8
//			transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX);
				if (index_order == 6) {
					transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX) + 5;
				} else if (index_order == 7) {
					transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX);
				} else {
					transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX) - 8;
				}

				transY = 4 * mOffsetX + 2 * (mMaxPerspectiveShift + mOffsetX);
			}
		} else {
			if (0 <= index_order && index_order < 3) { // 0 1 2
				transX = 4 * mOffsetX + index_order * (mMaxPerspectiveShift + mOffsetX);
				transY = 4 * mOffsetX;
			} else if (3 <= index_order && index_order < 6) { // 3 4 5
				transX = 4 * mOffsetX + (index_order - 3) * (mMaxPerspectiveShift + mOffsetX);
				transY = 4 * mOffsetX + 1 * (mMaxPerspectiveShift + mOffsetX);
			} else if (6 <= index_order && index_order < 9) { // 6 7 8
				transX = 4 * mOffsetX + (index_order - 6) * (mMaxPerspectiveShift + mOffsetX);
				transY = 4 * mOffsetX + 2 * (mMaxPerspectiveShift + mOffsetX);
			}
		}
		totalScale = mBaselineIconScale * 1 - 0.1f;
		if (params == null) {
			params = new PreviewItemDrawingParams(transX, transY, totalScale, overlayAlpha);
		} else {
			params.transX = transX;
			params.transY = transY;
			params.scale = totalScale;
			params.overlayAlpha = overlayAlpha;
		}
		return params;
	}

	public void drawPreview(Canvas canvas, ArrayList<View> items, Boolean hidden) {
		Drawable d = null;
		TextView v = null;

		// Update our drawing parameters if necessary
		if (mAnimating) {
			computePreviewDrawingParams(mAnimParams.drawable);
		} else {
			v = (TextView) items.get(0);
			d = getTopDrawable(v);
			computePreviewDrawingParams(d);
		}

		int nItemsInPreview = Math.min(items.size(), mNumItems);
		// Hidden folder - don't display Preview
		if (hidden) {
			mParams = computePreviewItemDrawingParams(mNumItems / 2, mParams);
			canvas.save();
			canvas.translate(mParams.transX + mPreviewOffsetX, mParams.transY + mPreviewOffsetY);
			canvas.scale(mParams.scale, mParams.scale);
			Drawable lock = mIcon.getResources().getDrawable(R.drawable.folder_lock);
			lock.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
			lock.draw(canvas);
			canvas.restore();
			return;
		}

		if (!mAnimating) {
			for (int i = 0; i <= nItemsInPreview - 1; i++) {
				v = (TextView) items.get(i);
				if (!mIcon.mHiddenItems.contains(v.getTag())) {
					d = v.getCompoundDrawables()[1];
					mParams = computePreviewItemDrawingParams(i, mParams);
					mParams.drawable = d;
					drawPreviewItem(canvas, mParams);
				}
			}
		} else {
			drawPreviewItem(canvas, mAnimParams);
		}
	}

	public void computePreviewDrawingParams(int drawableSize, int totalSize) {
		if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize || mNeedForceComputePreviewParams) {
			
			/** Lenovo-SW zhaoxin5 20150909 fix bug when show label in hotseat, the folder shows strangely START */
			if (mNeedForceComputePreviewParams) {
				Log.i("xixia", "FolderIconStyleList mNeedForceComputePreviewParams is true");
				mNeedForceComputePreviewParams = false;
			}
			/** Lenovo-SW zhaoxin5 20150909 fix bug when show label in hotseat, the folder shows strangely END */
			
			mIntrinsicIconSize = drawableSize;
			mTotalWidth = totalSize;

			final int previewSize = mIcon.mPreviewBackground.getLayoutParams().height;
			int previewPadding = FolderRingAnimator.sPreviewPadding;
			if (Build.MODEL.equals("GN3001") || Build.MODEL.equals("GN5001S")) {
//            lp.topMargin = grid.folderBackgroundOffset;
				previewPadding = FolderRingAnimator.sPreviewPadding + 4;
			} else if (BuildConfig.FLAVOR.equals("meitu")) {
				previewPadding = FolderRingAnimator.sPreviewPadding + 8;
			} else {
				previewPadding = FolderRingAnimator.sPreviewPadding;
			}

			mAvailableSpaceInPreview = (previewSize - 2 * previewPadding);
			// cos(45) = 0.707 + ~= 0.1) = 0.8f
			int adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 0.8f));

			int unscaledHeight = (int) (mIntrinsicIconSize * (1 + mShiftFactor));

			mBaselineIconScale = (1.0f * adjustedAvailableSpace / unscaledHeight);

			mBaselineIconSize = (int) (mIntrinsicIconSize * mBaselineIconScale);
			mMaxPerspectiveShift = (int) (mIntrinsicIconSize * (mBaselineIconScale * 1 - 0.1f));
			mOffsetX = (previewSize - mMaxPerspectiveShift * 3) / 10;
			mPreviewOffsetX = (mTotalWidth - previewSize) / 2 - 2;
			TypedValue outValue = new TypedValue();
			mResources.getValue(R.dimen.config_folder_preview_offsety_ratio, outValue, true);

			if (BuildConfig.FLAVOR.equals("meitu")) {
				mPreviewOffsetY = mIcon.getPaddingTop() + (int)((FolderRingAnimator.sPreviewPadding -16)  * outValue.getFloat());
			} else {
				mPreviewOffsetY = mIcon.getPaddingTop() + (int)(previewPadding * outValue.getFloat());
			}

		}
	}

	public void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
		canvas.save();
		canvas.translate(params.transX + mPreviewOffsetX, params.transY + mPreviewOffsetY);
		int width = params.drawable.getBounds().width();
		int height = params.drawable.getBounds().height();
		canvas.scale(params.scale * width / height, params.scale);
		Drawable d = params.drawable;

		if (d != null) {
			mOldBounds.set(d.getBounds());
			d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
			if (d instanceof FastBitmapDrawable) {
				FastBitmapDrawable fd = (FastBitmapDrawable) d;
				int oldBrightness = fd.getBrightness();
				fd.setBrightness(params.overlayAlpha);
				d.draw(canvas);
				fd.setBrightness(oldBrightness);
			} else {
				d.setColorFilter(Color.argb(params.overlayAlpha, 255, 255, 255), PorterDuff.Mode.SRC_ATOP);
				d.draw(canvas);
				d.clearColorFilter();
			}
			d.setBounds(mOldBounds);
		}
		canvas.restore();
	}

}
