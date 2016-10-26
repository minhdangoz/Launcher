/*
 * FolderIconStyleList.java
 *
 *
 * 2015/06/08
 *
 * Copyright notice
 */
package com.android.launcher3;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.FolderIcon.FolderRingAnimator;
import com.klauncher.launcher.R;
import com.klauncher.utilities.DeviceInfoUtils;

import java.util.ArrayList;

/**
 * 
 Pile of folders
 * 
 * @version 2015/06/08
 * @author Yanjun zhang
 */
public class FolderIconStylePile extends FolderIconStyleBase {

	public FolderIconStylePile(Launcher launcher, FolderIcon icon) {

		// TODO Auto-generated constructor stub
		super(launcher, icon);
		Resources res = mLauncher.getResources();
		mNumItems = 3;
		mShiftFactor = 0.18f;
		/*Lenovo-sw zhangyj19 add 2015/07/22 modify theme start*/
		if(FolderRingAnimator.sSharedInnerRingDrawable != null){
			mIcon.mPreviewBackground.setImageDrawable(FolderRingAnimator.sSharedInnerRingDrawable);
		} else {
			Drawable inner = DeviceInfoUtils.loadFolderImageFromAsserts(launcher,LauncherApplication.mModellbkValue + "_folder_inner.png");
			if (inner != null) {
				mIcon.mPreviewBackground.setImageDrawable(inner);
			} else {
				mIcon.mPreviewBackground.setImageDrawable(res
						.getDrawable(R.drawable.portal_ring_inner_holo));
			}

		}
		/*Lenovo-sw zhangyj19 add 2015/07/22 modify theme end*/
	}

	public PreviewItemDrawingParams computePreviewItemDrawingParams(int index,
			PreviewItemDrawingParams params) {
		index = mNumItems - index - 1;
		float r = (index * 1.0f) / (mNumItems - 1);
		float scale = (1 - PERSPECTIVE_SCALE_FACTOR * (1 - r));

		float offset = (1 - r) * mMaxPerspectiveShift;
		float scaledSize = scale * mBaselineIconSize;
		float scaleOffsetCorrection = (1 - scale) * mBaselineIconSize;

		// We want to imagine our coordinates from the bottom left, growing up
		// and to the
		// right. This is natural for the x-axis, but for the y-axis, we have to
		// invert things.
		float transY = mAvailableSpaceInPreview
				- (offset + scaledSize + scaleOffsetCorrection)
				+ mIcon.getPaddingTop();
		float transX = (mAvailableSpaceInPreview - scaledSize) / 2;
		float totalScale = mBaselineIconScale * scale;
		final int overlayAlpha = (int) (80 * (1 - r));

		if (params == null) {
			params = new PreviewItemDrawingParams(transX, transY, totalScale,
					overlayAlpha);
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
			canvas.translate(mParams.transX + mPreviewOffsetX, mParams.transY
					+ mPreviewOffsetY);
			canvas.scale(mParams.scale, mParams.scale);
			Drawable lock = mIcon.getResources().getDrawable(
					R.drawable.folder_lock);
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
		if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize) {
			LauncherAppState app = LauncherAppState.getInstance();
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

			mIntrinsicIconSize = drawableSize;
			mTotalWidth = totalSize;

			final int previewSize = mIcon.mPreviewBackground.getLayoutParams().height;
			final int previewPadding = FolderRingAnimator.sPreviewPadding;

			mAvailableSpaceInPreview = (previewSize - 2 * previewPadding);
			// cos(45) = 0.707 + ~= 0.1) = 0.8f
			int adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 0.8f));

			int unscaledHeight = (int) (mIntrinsicIconSize * (1 + mShiftFactor));

			mBaselineIconScale = (1.0f * adjustedAvailableSpace / unscaledHeight);

			mBaselineIconSize = (int) (mIntrinsicIconSize * mBaselineIconScale);
			mMaxPerspectiveShift = mBaselineIconSize * mShiftFactor;

			mPreviewOffsetX = (mTotalWidth - mAvailableSpaceInPreview) / 2;
			mPreviewOffsetY = previewPadding + grid.folderBackgroundOffset;
		}
	}

	public void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
		canvas.save();
		canvas.translate(params.transX + mPreviewOffsetX, params.transY
				+ mPreviewOffsetY);
		canvas.scale(params.scale, params.scale);
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
				d.setColorFilter(
						Color.argb(params.overlayAlpha, 255, 255, 255),
						PorterDuff.Mode.SRC_ATOP);
				d.draw(canvas);
				d.clearColorFilter();
			}
			d.setBounds(mOldBounds);
		}
		canvas.restore();
	}
}
