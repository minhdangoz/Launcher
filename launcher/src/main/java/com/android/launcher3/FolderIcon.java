/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.FolderInfo.FolderListener;
import com.android.launcher3.settings.SettingsProvider;
import com.klauncher.launcher.R;
import com.klauncher.theme.ziptheme.ZipThemeUtils;
import com.klauncher.utilities.DeviceInfoUtils;

import java.util.ArrayList;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends FrameLayout implements FolderListener {
    private Launcher mLauncher;
    private Folder mFolder;
    private FolderInfo mInfo;
    private static boolean sStaticValuesDirty = true;

    private CheckLongPressHelper mLongPressHelper;

    // The number of icons to display in the

    private static final int CONSUMPTION_ANIMATION_DURATION = 100;
    private static final int DROP_IN_ANIMATION_DURATION = 400;
    private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
    private static final int FINAL_ITEM_ANIMATION_DURATION = 200;

    // The degree to which the inner ring grows when accepting drop
    private static final float INNER_RING_GROWTH_FACTOR = 0.15f;

    // The degree to which the outer ring is scaled in its natural state
    private static final float OUTER_RING_GROWTH_FACTOR = 0.3f;

    // Flag as to whether or not to draw an outer ring. Currently none is designed.
    public static final boolean HAS_OUTER_RING = true;

    // Flag whether the folder should open itself when an item is dragged over is enabled.
    public static final boolean SPRING_LOADING_ENABLED = true;

    // Delay when drag enters until the folder opens, in miliseconds.
    private static final int ON_OPEN_DELAY = 800;

    public static Drawable sSharedFolderLeaveBehind = null;

    public ImageView mPreviewBackground;
    public BubbleTextView mFolderName;

    FolderRingAnimator mFolderRingAnimator = null;

    private float mSlop;

    public ArrayList<ShortcutInfo> mHiddenItems = new ArrayList<ShortcutInfo>();

    private Alarm mOpenAlarm = new Alarm();
    private ItemInfo mDragInfo;
    private static AppDrawerListAdapter.FolderSortType mFolderSortType;
    FolderIconStyleBase mFolderIconStyle = null;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolderIcon(Context context) {
        super(context);
        init();
    }

    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);
    }

    public boolean isDropEnabled() {
        final ViewGroup cellLayoutChildren = (ViewGroup) getParent();
        final ViewGroup cellLayout = (ViewGroup) cellLayoutChildren.getParent();
        final Workspace workspace = (Workspace) cellLayout.getParent();
        return !workspace.workspaceInModalState();
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
                              FolderInfo folderInfo, IconCache iconCache) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
        icon.setClipToPadding(false);
        icon.mFolderName = (BubbleTextView) icon.findViewById(R.id.folder_icon_name);
        /*Lenovo-sw zhangyj19 add 2015/09/24 modify Default Folder Name begin */
        String defaultFolderName = launcher.getResources().getString(R.string.folder_name);
        if("".equals(folderInfo.title)){
            icon.mFolderName.setText(defaultFolderName);
        }else{
            icon.mFolderName.setText(folderInfo.title);
        }
        /*Lenovo-sw zhangyj19 add 2015/09/24 modify Default Folder Name end */
        icon.mFolderName.setCompoundDrawablePadding(0);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) icon.mFolderName.getLayoutParams();
        /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 START */
        lp.topMargin = grid.iconSizePx + grid.iconDrawablePaddingPx - 5;
        /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 END */

        // Offset the preview background to center this view accordingly
        icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);
        lp = (FrameLayout.LayoutParams) icon.mPreviewBackground.getLayoutParams();
        lp.topMargin = grid.folderBackgroundOffset;
        lp.width = grid.folderIconSizePx;
        lp.height = grid.folderIconSizePx;

        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        Folder folder = Folder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        folder.bind(folderInfo);
        icon.mFolder = folder;
        mFolderSortType = AppDrawerListAdapter.FolderSortType
                .getModeForValue(SettingsProvider
                        .getInt(launcher,
                                SettingsProvider.SETTINGS_UI_FOLDER_SORT_TYPE,
                                R.integer.preferences_interface_Folder_sort_type_default));
        if (mFolderSortType == AppDrawerListAdapter.FolderSortType.Pile) {
            icon.mFolderIconStyle = new FolderIconStylePile(launcher, icon);
        } else {
            icon.mFolderIconStyle = new FolderIconStyleList(launcher, icon);
        }

        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);

        icon.setOnFocusChangeListener(launcher.mFocusHandler);
        return icon;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        // Lenovo-sw:yuanyl2, Add edit mode function.
        // private CellLayout mCellLayout;
        private ViewGroup mCellLayout;

        public FolderRingAnimator() {
            super();
        }

        public float mOuterRingSize;
        public float mInnerRingSize;
        public FolderIcon mFolderIcon = null;
        public static Drawable sSharedOuterRingDrawable = null;
        public static Drawable sSharedInnerRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;

        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            mFolderIcon = folderIcon;
            Resources res = launcher.getResources();

            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
            if (sStaticValuesDirty) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    throw new RuntimeException("FolderRingAnimator loading drawables on non-UI thread "
                            + Thread.currentThread());
                }

                LauncherAppState app = LauncherAppState.getInstance();
                DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
                sPreviewSize = grid.folderIconSizePx;
                sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
                //sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_holo);
                //sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_nolip_holo);
                //sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest);
                if(sSharedOuterRingDrawable == null){
                    Drawable inner = DeviceInfoUtils.loadFolderImageFromAsserts(launcher,LauncherApplication.mModellbkValue + "_folder_outer.png");
                    if (inner != null) {
                        sSharedOuterRingDrawable = inner;
                    } else {
                        sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_holo);
                    }
                }
                if(sSharedInnerRingDrawable == null){
                    Drawable inner = DeviceInfoUtils.loadFolderImageFromAsserts(launcher,LauncherApplication.mModellbkValue + "_folder_inner.png");
                    if (inner != null) {
                        sSharedInnerRingDrawable = inner;
                    } else {
                        sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_holo);
                    }
                }
                if(sSharedFolderLeaveBehind == null){
                    Drawable inner = DeviceInfoUtils.loadFolderImageFromAsserts(launcher,LauncherApplication.mModellbkValue + "_folder_inner.png");
                    if (inner != null) {
                        sSharedFolderLeaveBehind = inner;
                    } else {
                        sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_outer_holo);
                    }
                }

                sStaticValuesDirty = false;
            }
        }

        public void animateToAcceptState() {
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel();
            }
            mAcceptAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mAcceptAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + percent * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                    }
                }
            });
            mAcceptAnimator.start();
        }

        public void animateToNaturalState() {
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            mNeutralAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mNeutralAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCellLayout != null) {
                        // Lenovo-sw:yuanyl2, Add edit mode function.
                        // mCellLayout.hideFolderAccept(FolderRingAnimator.this);
                        if(mCellLayout instanceof CellLayout){
                            ((CellLayout) mCellLayout).hideFolderAccept(FolderRingAnimator.this);
                        }else if(mCellLayout instanceof XDockView){
                            ((XDockView) mCellLayout).hideFolderAccept(FolderRingAnimator.this);
                        }
                    }
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                    }
                }
            });
            mNeutralAnimator.start();
        }

        // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
        public boolean isDisplay() {
            if (mCellLayout == null) {
                return false;
            }

            if (mCellLayout instanceof CellLayout) {
                return ((CellLayout) mCellLayout).conatins(FolderRingAnimator.this);
            } else if (mCellLayout instanceof XDockView) {
                return ((XDockView) mCellLayout).contains(FolderRingAnimator.this);
            }
            return false;
        }
        // Lenovo-sw:yuanyl2, Add edit mode function. End.

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public void setCellLayout(ViewGroup layout) {
            mCellLayout = layout;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }

        public float getInnerRingSize() {
            return mInnerRingSize;
        }
    }

    public Folder getFolder() {
        return mFolder;
    }

    FolderInfo getFolderInfo() {
        return mInfo;
    }

    private boolean willAcceptItem(ItemInfo item) {
        final int itemType = item.itemType;

        // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
        if (itemType == LauncherSettings.Favorites.ITEM_TYPE_LAYOUT) {
            return true;
        }
        // Lenovo-sw:yuanyl2, Add edit mode function. End.

        boolean hidden = false;
        if (item instanceof FolderInfo) {
            hidden = ((FolderInfo) item).hidden;
        }
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_FOLDER &&
                        canMergeDragFolder((FolderInfo) item)) &&
                !mFolder.isFull() && item != mInfo && !mInfo.opened &&
                !hidden);
    }

    private boolean canMergeDragFolder(FolderInfo info) {
        int currentCount = mFolder.getInfo().contents.size();
        int dragFolderCount = info.contents.size();
        return (currentCount + dragFolderCount) <= mFolder.getMaxItems();
    }

    public boolean acceptDrop(Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        if (mInfo.hidden) {
            return false;
        }
        return !mFolder.isDestroyed() && willAcceptItem(item);
    }

    public void addItem(ShortcutInfo item) {
        mInfo.add(item);
    }

    public void onDragEnter(Object dragInfo) {
        if (mFolder.isDestroyed() || !willAcceptItem((ItemInfo) dragInfo)) return;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) getLayoutParams();
        CellLayout layout = (CellLayout) getParent().getParent();
        mFolderRingAnimator.setCell(lp.cellX, lp.cellY);
        mFolderRingAnimator.setCellLayout(layout);
        mFolderRingAnimator.animateToAcceptState();
        layout.showFolderAccept(mFolderRingAnimator);
        mOpenAlarm.setOnAlarmListener(mOnOpenListener);
        if (SPRING_LOADING_ENABLED &&
                ((dragInfo instanceof AppInfo) || (dragInfo instanceof ShortcutInfo))) {
            // TODO: we currently don't support spring-loading for PendingAddShortcutInfos even
            // though widget-style shortcuts can be added to folders. The issue is that we need
            // to deal with configuration activities which are currently handled in
            // Workspace#onDropExternal.
            mOpenAlarm.setAlarm(ON_OPEN_DELAY);
        }
        mDragInfo = (ItemInfo) dragInfo;
    }

    public void onDragOver(Object dragInfo) {
    }

    OnAlarmListener mOnOpenListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            ShortcutInfo item;
            if (mDragInfo instanceof AppInfo) {
                // Came from all apps -- make a copy.
                item = ((AppInfo) mDragInfo).makeShortcut();
                item.spanX = 1;
                item.spanY = 1;
            } else if (mDragInfo instanceof FolderInfo) {
                return;
            } else {
                // ShortcutInfo
                item = (ShortcutInfo) mDragInfo;
            }
            mFolder.beginExternalDrag(item);
            mLauncher.openFolder(FolderIcon.this);
        }
    };

    public void performCreateAnimation(final ShortcutInfo destInfo, final View destView,
                                       final ShortcutInfo srcInfo, final DragView srcView, Rect dstRect,
                                       float scaleRelativeToDragLayer, Runnable postAnimationRunnable) {

        // These correspond two the drawable and view that the icon was dropped _onto_
        Drawable animateDrawable = mFolderIconStyle
                .getTopDrawable((TextView) destView);
        mFolderIconStyle.computePreviewDrawingParams(
                animateDrawable.getIntrinsicWidth(),
                destView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        mFolderIconStyle.animateFirstItem(animateDrawable,
                INITIAL_ITEM_ANIMATION_DURATION, false, null);
        addItem(destInfo);

        // This will animate the dragView (srcView) into the new folder
        onDrop(srcInfo, srcView, dstRect, scaleRelativeToDragLayer, 1,
                postAnimationRunnable, null);
    }

    public void performDestroyAnimation(final View finalView, Runnable onCompleteRunnable) {
        Drawable animateDrawable = mFolderIconStyle
                .getTopDrawable((TextView) finalView);
        mFolderIconStyle.computePreviewDrawingParams(
                animateDrawable.getIntrinsicWidth(),
                finalView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        mFolderIconStyle.animateFirstItem(animateDrawable,
                FINAL_ITEM_ANIMATION_DURATION, true, onCompleteRunnable);
    }

    public void onDragExit(Object dragInfo) {
        onDragExit();
    }

    public void onDragExit() {
        mFolderRingAnimator.animateToNaturalState();
        mOpenAlarm.cancelAlarm();
    }

    private void onDrop(final ShortcutInfo item, DragView animateView, Rect finalRect,
                        float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
                        DragObject d) {
        item.cellX = -1;
        item.cellY = -1;

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                // Set cellLayout and this to it's final state to compute final animation locations
                workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current state
                setScaleX(scaleX);
                setScaleY(scaleY);
                workspace.resetTransitionTransform((CellLayout) getParent().getParent());
            }

            int[] center = new int[2];
            float scale = mFolderIconStyle
                    .getLocalCenterForIndex(index, center);
            center[0] = (int) Math.round(scaleRelativeToDragLayer * center[0]);
            center[1] = (int) Math.round(scaleRelativeToDragLayer * center[1]);

            to.offset(center[0] - animateView.getMeasuredWidth() / 2,
                    center[1] - animateView.getMeasuredHeight() / 2);

            float finalAlpha = index < mFolderIconStyle.mNumItems ? 0.5f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DROP_IN_ANIMATION_DURATION,
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
            addItem(item);
            mHiddenItems.add(item);
            mFolder.hideItem(item);
            postDelayed(new Runnable() {
                public void run() {
                    mHiddenItems.remove(item);
                    mFolder.showItem(item);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }
    }

    public void onDrop(DragObject d) {
        ShortcutInfo item;
        if (d.dragInfo instanceof AppInfo) {
            // Came from all apps -- make a copy
            item = ((AppInfo) d.dragInfo).makeShortcut();
        } else if (d.dragInfo instanceof FolderInfo) {
            FolderInfo folder = (FolderInfo) d.dragInfo;
            mFolder.notifyDrop();
            for (ShortcutInfo fItem : folder.contents) {
                onDrop(fItem, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
            }
            mLauncher.removeFolder(folder);
            LauncherModel.deleteItemFromDatabase(mLauncher, folder);
            return;
        } else if (d.dragInfo instanceof LayoutInfo) {
            mFolder.notifyDrop();
            onDrop(1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
            return;
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        mFolder.notifyDrop();

        onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mFolder == null) return;
        if (mFolder.getItemCount() == 0 && !mFolderIconStyle.mAnimating) return;
        ArrayList<View> items = mFolder.getItemsInReadingOrder();

        mFolderIconStyle.drawPreview(canvas, items, mInfo.hidden);

    }

    public void setTextVisible(boolean visible) {
        if (visible) {
            mFolderName.setVisibility(VISIBLE);
        } else {
            mFolderName.setVisibility(INVISIBLE);
        }
    }

    public boolean getTextVisible() {
        return mFolderName.getVisibility() == VISIBLE;
    }

    public void onItemsChanged() {
        invalidate();
        requestLayout();
    }

    @Override
    public void onRemoveAll() {
        invalidate();
        requestLayout();
    }

    public void onAdd(ShortcutInfo item) {
        invalidate();
        requestLayout();
    }

    public void onRemove(ShortcutInfo item) {
        invalidate();
        requestLayout();
    }

    public void onTitleChanged(CharSequence title) {
        /*Lenovo-sw zhangyj19 add 2015/09/24 modify Default Folder Name begin */
        if("".equals(title)){
            mFolderName.setText(mLauncher.getResources().getString(R.string.folder_name));
        }else{
            mFolderName.setText(title.toString());
        }
        /*Lenovo-sw zhangyj19 add 2015/09/24 modify Default Folder Name end */
        setContentDescription(String.format(getContext().getString(R.string.folder_name_format),
                title));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!Utilities.pointInView(this, event.getX(), event.getY(), mSlop)) {
                    mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
    private float getLocalCenterForIndex(int index, int[] center) {
        return mFolderIconStyle.getLocalCenterForIndex(index, center);

//        mParams = computePreviewItemDrawingParams(index, mParams);
//
//        mParams.transX += mPreviewOffsetX;
//        mParams.transY += mPreviewOffsetY;
//        float offsetX = mParams.transX + (mParams.scale * mIntrinsicIconSize) / 2;
//        float offsetY = mParams.transY + (mParams.scale * mIntrinsicIconSize) / 2;
//
//        center[0] = (int) Math.round(offsetX);
//        center[1] = (int) Math.round(offsetY);
//        return mParams.scale;
    }

    public boolean onLayoutDrop(final ShortcutInfo item, Rect from, DragView animateView, Rect finalRect,
                                float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
                                DragObject d, boolean isStackFolder) {
        XDockView dockView = mLauncher.getDockView();
        if (mFolder.isFull()) {
            // show toast when folder is full
            return false;
        }
        item.cellX = -1;
        item.cellY = -1;

        final ArrayList<View> items = mFolder.getItemsInReadingOrder();

        // Typically, the animateView corresponds to the DragView; however, if
        // this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from
        // AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
//            Rect from = new Rect();
//            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                // Set cellLayout and this to it's final state to compute final
                // animation locations
//                workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current
                // state
                setScaleX(scaleX);
                setScaleY(scaleY);
//                workspace.resetTransitionTransform((CellLayout) getParent().getParent());
            }

            int[] center = new int[2];
            float scale = getLocalCenterForIndex(index, center);
            center[0] = (int) Math.round(scaleRelativeToDragLayer * center[0]);
            center[1] = (int) Math.round(scaleRelativeToDragLayer * center[1]);

            if (!isStackFolder) {
                to.offset(center[0] - animateView.getMeasuredWidth() / 2,
                        center[1] - animateView.getMeasuredHeight() / 2);
            }

            float finalAlpha = index < mFolderIconStyle.mNumItems ? 0.5f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            // animate to folder icon
            dockView.animDragviewIntoFolderIcon(item, animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DROP_IN_ANIMATION_DURATION,
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null, this);
            addItem(item);
            mHiddenItems.add(item);
            mFolder.hideItem(item);
            postDelayed(new Runnable() {
                public void run() {
//                    mHiddenItems.remove(item);
                    mFolder.showItem(item);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }

        return true;
    }

    public void onDrop(float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
                       DragObject d) {
        XDockView dockView = mLauncher.getDockView();
        LayoutInfo layoutInfo = (LayoutInfo) d.dragInfo;
        boolean isStackFolder = false;
        if (getParent() != null && getParent().getParent() instanceof XDockView) {
            isStackFolder = true;
        }
        Rect r = new Rect();
        for (int i = 0; i < layoutInfo.getCount(); i++) {
            // if v available, add to workspace and do anim, remove in dockview
            // else anim to XDockView
            ShortcutInfo shortcutInfo = layoutInfo.getInfoAt(i);
            DragView view = d.dragViewList.get(i);
            if (i == 0) {
                mLauncher.getDragLayer().getViewRectRelativeToSelf(view, r);
            }
            boolean result = onLayoutDrop(shortcutInfo, r, view, null, 1.0f, index,
                    d.postAnimationRunnable, d, isStackFolder);
            if (result) {
                if (!dockView.isToStackFolderIcon()) {
                    dockView.removeDockItemByInfo(shortcutInfo, true);
                }
            } else {
                dockView.setBackByDrag();
                dockView.setDragTargerStatus(true);
                dockView.animDragviewIntoPosition(view, shortcutInfo, r);
            }
        }

        if (isStackFolder) {
            dockView.startAnimatorSet(true, true);
        } else {
            dockView.hide();
            dockView.startAnimatorSet(true, false);
        }
    }

    public void showItem(ShortcutInfo info) {
        mHiddenItems.remove(info);
        mFolder.showItem(info);
        invalidate();
    }

    public void hideItem(ShortcutInfo info) {
        mHiddenItems.add(info);
        mFolder.hideItem(info);
    }

    public View getPreviewBackground() {
        return mPreviewBackground;
    }

    public boolean isStackFolderIcon = false;

    public void onDockViewDragEnter(Object dragInfo) {
        if (mFolder.isDestroyed() || !willAcceptItem((ItemInfo) dragInfo))
            return;
        XDockView layout = mLauncher.getDockView();
        mFolderRingAnimator.setCell(0, 0);
        mFolderRingAnimator.setCellLayout(layout);
        mFolderRingAnimator.animateToAcceptState();
        layout.showFolderAccept(mFolderRingAnimator);
    }

    public void checkFolderRingStatus() {
        if (mFolderRingAnimator.isDisplay()) {
            mFolderRingAnimator.animateToNaturalState();
        }
    }

    // Lenovo-sw:yuanyl2, Add edit mode function. End.
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
