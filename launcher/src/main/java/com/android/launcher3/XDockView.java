package com.android.launcher3;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.launcher3.FolderIcon.FolderRingAnimator;
import com.android.launcher3.XDockViewAnimUtil.AlphaStatus;
import com.webeye.launcher.ext.LauncherLog;
import com.webeye.launcher.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by YUANYL2 on 2015/6/1.
 * The XDockView to handle multi-select application event.
 */
public class XDockView extends HorizontalScrollView implements
        View.OnClickListener, View.OnLongClickListener, DropTarget, DragSource,
        DragController.DragListener {
    private static final String TAG = "XDockView";

    private static final long ANIM_INTERVAL = 80;
    private static final int MAX_COUNT = 35;
    private static final int MIN_SCROLL_DISTANCE = 30;

    protected static final int FLING_DISTANCE = 3600;

    enum State {
        NORMAL, STACK_MODE, FOLDER_MODE
    }

    private State mState = State.NORMAL;

    private Launcher mLauncher;
    private Workspace mWorkspace;
    private ItemInfo mDragInfo;
    private DragController mDragController;
    private DragLayer mDragLayer;
    private IconCache mIconCache;
    private LauncherAppState app = null;
    private DeviceProfile grid = null;

    private XDockViewContainer mContainer;
    private XDockViewLayout mLayout;
    private boolean isDraging = false;

    // Collect icons gesture detector.
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private boolean scaleHandled = false;
    private boolean scaleFinished = true;

    private Context mContext;
    private XDockViewAnimUtil mAnim;

    private boolean isOpenFolder = false;
    private boolean isCreateScreen = false;
    private boolean isAnimation = false;
    private boolean mIsStacking = false;
    private boolean isBackByDrag = false;
    private boolean isToFolderIconByDrag = false;
    private boolean isFromIndicator = false;
    public boolean isDragDelete = false;
    public boolean dragDeleteIsCompleted = false;
    public boolean isDragFromWorkSpace = false;
    public boolean isClickDelete = false;
    public boolean clickDeleteIsCompleted = false;
    public boolean isNeedUpdateSelectCount = false;

    private int mScrollX = -1;
    private FolderIcon mFolderIcon;
    private FolderIcon tmpFolderIcon;

    private ArrayList<FolderRingAnimator> mFolderOuterRings = new ArrayList<>();
    public ArrayList<ValueAnimator> mAllAnimator = new ArrayList<>();
    private ArrayList<ShortcutInfo> mAllInfos = new ArrayList<>();

    private boolean isAddLayoutMode = false;

    private Runnable mDeferredAction;
    private boolean mDeferDropAfterUninstall = false;
    private boolean mUninstallSuccessful;
    public int isRemoveByComponentName = 0;

    private boolean isNeedAdjustLayout = true;

    public XDockView(Context context) {
        this(context, null);
    }

    public XDockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XDockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void setup(Launcher launcher, DragController dragController) {
        mLauncher = launcher;
        mWorkspace = mLauncher.getWorkspace();
        mDragController = dragController;
        mLayout.setup(mLauncher);
        mContext = launcher;
        mDragLayer = launcher.getDragLayer();

        app = LauncherAppState.getInstance();
        grid = app.getDynamicGrid().getDeviceProfile();
        mIconCache = app.getIconCache();

        mGestureDetector = new GestureDetector(this.getContext(),
                new SimpleGestureListener());
        mScaleDetector = new ScaleGestureDetector(this.getContext(),
                new ScaleListener());
        mAnim = new XDockViewAnimUtil(launcher);

        // Must layout from Left to Right.
        if (VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN) {
            setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            if (mScaleFactor != 1.f) {
                // on scale now, enable scaleHandled to disable scroll
                scaleHandled = true;
            }
            // we only doStack once during one scale, so break out when previous
            // scale not
            // finished
            if (!scaleFinished) {
                return true;
            }
            if (mScaleFactor > 1.f) {
                if (!isFolderMode() && isStackMode()) {
                    doStack();
                    mScaleFactor = 1.f;
                    scaleFinished = false;
                }
            } else if (mScaleFactor < 1.f) {
                if (!isFolderMode() && !isStackMode()) {
                    doStack();
                    mScaleFactor = 1.f;
                    scaleFinished = false;
                }
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mScaleFactor = 1.f;
            scaleFinished = true;
        }
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mScrollX == -1) {
                mScrollX = getScrollX();
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // if layout can not scroll anymore, then trigger stack operation

            if (isFolderMode()) {
                return false;
            }
            if (Math.abs(velocityX) > FLING_DISTANCE) {
                doStack();
                return true;
            }

            if (mScrollX != -1 && Math.abs(getScrollX() - mScrollX) > MIN_SCROLL_DISTANCE) {
                mScrollX = -1;
                return false;
            }

            if (velocityX > 0) {
                if (mLayout.getRelativeX() >= 0) {
                    if (!isFolderMode()) {
                        doStack();
                    }
                    mScrollX = -1;
                    return true;
                }
            } else {
                // mLayout.getMeasuredWidth() returned wrong result under stack
                // mode,
                // so we use getProperWidth() instead
                int layoutRight = mLayout.getProperWidth()
                        - Math.abs(mLayout.getRelativeX()) - getDisplayWidth();
                if (layoutRight <= 1 || isStackMode()) {
                    if (!isFolderMode()) {
                        doStack();
                    }
                    mScrollX = -1;
                    return true;
                }
            }
            mScrollX = -1;
            return false;
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContainer = (XDockViewContainer) findViewById(R.id.container);
        mLayout = (XDockViewLayout) findViewById(R.id.dock_layout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isAnimation || isAnimStacking()) {
            return true;
        }

        // handle simple gesture
        mScrollX = -1;
        boolean result = mGestureDetector.onTouchEvent(ev);
        if (result) {
            return true;
        }

        // handle scale gesture
        mScaleFactor = 1.f;
        mScaleDetector.onTouchEvent(ev);
        if (scaleHandled) {
            scaleHandled = false;
            return true;
        }

        return super.onTouchEvent(ev);
    }

    public View addExternalItem(ItemInfo info, int index) {
        isNeedAdjustLayout = true;
        if (info instanceof ShortcutInfo) {
            View view = mLayout.addItemView(info, index);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            return view;
        }
        return null;
    }

    public void addItemToFolderIcon(ItemInfo info) {
        if (info instanceof ShortcutInfo) {
            ShortcutInfo tmpInfo = new ShortcutInfo(mContext,
                    (ShortcutInfo) info);
            mAllInfos.add(tmpInfo);
            if (mFolderIcon != null) {
                mFolderIcon.addItem((ShortcutInfo) info);
                mFolderIcon.hideItem((ShortcutInfo) info);
                updateSelectCount();
            }
        }
    }

    private void removeFolderItemInfo(ItemInfo info) {
        ShortcutInfo rinfo = null;
        for (ShortcutInfo i : mAllInfos) {
            if (i.getIntent().getComponent().getPackageName()
                    .equals(info.getIntent().getComponent().getPackageName())) {
                rinfo = i;
            }
        }
        if (rinfo != null) {
            mAllInfos.remove(rinfo);
            updateSelectCount();
        }
    }

    public View addDragItemInfo(ShortcutInfo target, int addindex) {
        // TODO check if it's stacked
        View view = addExternalItem(target, addindex);
        view.setVisibility(View.INVISIBLE);
        if (!isDragFromWorkSpace) {
            updateSelectCount();
        }
        return view;
    }

    private void recoverItemInfo(ShortcutInfo info) {
        for (ShortcutInfo i : mAllInfos) {
            String catSrc = null;
            String catDes = null;
            String dataSrc;
            String dataDes;
            String titleSrc = null;
            String titleDes = null;
            String packageSrc = null;
            String packageDes = null;

            if (info.title != null) {
                titleSrc = info.title.toString();
            }
            if (i.title != null) {
                titleDes = i.title.toString();
            }

            if (info.intent.getComponent() != null
                    && i.intent.getComponent() != null) {
                if (info.intent.getComponent().getClassName() != null) {
                    catSrc = info.intent.getComponent().getClassName()
                            .toString();
                }
                if (i.intent.getComponent().getClassName() != null) {
                    catDes = i.intent.getComponent().getClassName()
                            .toString();
                }
                int typeSrc = info.itemType;
                int typeDes = i.itemType;

                if (catSrc != null && catSrc.equals(catDes)
                        && (typeSrc == typeDes)) {
                    if (titleSrc == null || titleSrc.equals(titleDes)) {
                        info.copyFrom(i);
                        break;
                    }
                }

            } else if (info.intent.getDataString() != null
                    && i.intent.getDataString() != null) {
                dataSrc = info.intent.getDataString();
                dataDes = i.intent.getDataString();
                if (dataSrc != null && dataSrc.equals(dataDes)) {
                    info.copyFrom(i);
                    break;
                }
            } else {
                /*Lenovo-sw zhangyj19 add 2015/09/06 add XTHREEROW-1419 begin*/
                packageSrc = info.intent.getPackage();
                packageDes = i.intent.getPackage();
                if (packageSrc != null && packageDes != null && packageSrc.equals(packageDes)
                        && (info.itemType == i.itemType)) {
                    if (titleSrc == null || titleSrc.equals(titleDes)) {
                        info.copyFrom(i);
                        break;
                    }
                }
                /*Lenovo-sw zhangyj19 add 2015/09/06 XTHREEROW-1419 end*/
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // The folder outer / inner ring image(s)
        for (int i = 0; i < mFolderOuterRings.size(); i++) {
            FolderRingAnimator fra = mFolderOuterRings.get(i);

            Drawable d;
            int width, height;
            int[] folderLocation = mLauncher.getLocationPosition(mFolderIcon.getPreviewBackground());
            int[] layoutLocation = mLauncher.getLocationPosition(this);
            float scale = getItemScale();

            if (mFolderIcon != null) {
                int centerX = (int) (folderLocation[0] - layoutLocation[0] + mFolderIcon
                        .getPreviewBackground().getWidth() * scale / 2);
                int centerY = (int) (folderLocation[1] - layoutLocation[1] + mFolderIcon
                        .getPreviewBackground().getHeight() * scale / 2);

                // Draw outer ring, if it exists
                if (FolderIcon.HAS_OUTER_RING) {
                    //d = FolderRingAnimator.sSharedOuterRingDrawable;
                    d = FolderRingAnimator.sSharedOuterRingDrawable.getConstantState().newDrawable();
                    width = (int) (fra.getOuterRingSize() * scale);
                    height = width;
                    canvas.save();
                    canvas.translate(centerX - width / 2, centerY - height / 2);

                    d.setBounds(0, 0, width, height);
                    d.draw(canvas);
                    canvas.restore();
                }

                //d = FolderRingAnimator.sSharedInnerRingDrawable;
                d = FolderRingAnimator.sSharedInnerRingDrawable.getConstantState().newDrawable();
                if (d != null) {
                    width = (int) (fra.getInnerRingSize() * scale);
                    height = width;
                    canvas.save();
                    canvas.translate(centerX - width / 2, centerY - width / 2);
                    d.setBounds(0, 0, width, height);
                    d.draw(canvas);
                    canvas.restore();
                }
            }
        }
    }

    @Override
    public void onClick(View item) {
        if (LauncherLog.DEBUG_EDITMODE) {
            LauncherLog.d(TAG, "onClick, item:" + item);
        }

        // Check clickable validation
        if (isAnimation || item.equals(this) || isAnimStacking()) {
            return;
        }

        // Check if items are stacked.
        if (isStackMode()) {
            final ArrayList<View> mSourceItem = new ArrayList<>();
            final ArrayList<View> mTargetItem = new ArrayList<>();

            int count = mLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = mLayout.getChildItemAt(i);
                View targetItem = singleClickItem(v);
                if (targetItem == null) {
                    break;
                } else {
                    mSourceItem.add(v);
                    mTargetItem.add(targetItem);
                }
            }

            int size = mTargetItem.size();
            for (int i = 0; i < size; i++) {
                animItemToWorkSpace(mSourceItem.get(i),
                        mTargetItem.get(i));
            }
            startAnimatorSet(true, false);
        } else if (isNormalMode()) {
            isClickDelete = true;
            clickDeleteIsCompleted = false;
            View targetItem = singleClickItem(item);
            animItemToWorkSpace(item, targetItem);
            startAnimatorSet(true, false);
        } else {
            LauncherLog.d(TAG, "###is folder mode");
        }
    }


    /**
     * Create a item in the target workspace or folder based specified item.
     *
     * @param item the item in the dock.
     * @return the view created in the target workspace or folder.
     */
    private View singleClickItem(final View item) {
        if (item == null || item.getTag() == null) {
            return null;
        }

        final ItemInfo info = (ItemInfo) item.getTag();
        ViewGroup parentView;
        int spanX = info.spanX;
        int spanY = info.spanY;
        if (spanX < 1) {
            spanX = 1;
        }
        if (spanY < 1) {
            spanY = 1;
        }

        if (mWorkspace.getOpenFolder() == null) {
            isOpenFolder = false;
            parentView = mWorkspace.getCurrentDropLayout();
        } else {
            isOpenFolder = true;
            parentView = mWorkspace.getOpenFolder();
            if (((Folder) parentView).isFull()) {
                return null;
            }
        }

        if (parentView == null) {
            return null;
        }

        int[] targetCell = new int[]{-1, -1};
        if (!isOpenFolder) {
            ((CellLayout) parentView).findCellForSpan(targetCell, spanX, spanY);
            if (targetCell == null || targetCell[0] < 0 || targetCell[1] < 0) {
                LauncherLog.e(TAG, "showOutOfSpaceMessage");
                if (isNormalMode()) {
                    mLauncher.showOutOfSpaceMessage(false);
                }
                return null;
            }
        }

        // Create a new item in the target folder or workspace.
        final View targetItem;

        if (isOpenFolder) {
            Folder folder = (Folder) parentView;
            folder.getInfo().add((ShortcutInfo) info);
            targetItem = folder.getViewForInfo((ShortcutInfo) info);
        } else {
            targetItem = mLauncher.createShortcut(R.layout.application, mWorkspace.getCurrentDropLayout(),
                    (ShortcutInfo) info);

            final long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
            final long screenId = mWorkspace.getIdForScreen((CellLayout) parentView);

            info.cellX = targetCell[0];
            info.cellY = targetCell[1];
            mWorkspace.addDockViewItem(targetItem, container, screenId, targetCell[0], targetCell[1], spanX, spanX);
            // add by zhanggx1 for reordering all pages on 2013-11-20. s
 			mLauncher.autoReorder("singleClickItem");
 			// add by zhanggx1 for reordering all pages on 2013-11-20. e
        }

        if (targetItem != null) {
            targetItem.setVisibility(View.INVISIBLE);
        }
        item.setEnabled(false);

        return targetItem;
    }

    /**
     * @param sourceItem the item in the dock
     * @param targetItem the item added to workspace.
     * @see
     */
    private void animItemToWorkSpace(final View sourceItem, final View targetItem) {
        // Create a moving snapshot.
        final View snapshot = mLauncher.createSnapshot(sourceItem);
        if (snapshot == null || targetItem == null) {
            return;
        }

        targetItem.setVisibility(INVISIBLE);
        final int[] itemPos;
        if (isStackMode()) {
            itemPos = mLayout.getStackPosition();
        } else {
            itemPos = mLauncher.getLocationPosition(sourceItem);
        }

        // Set snapshot view layout params.
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        snapshot.setLayoutParams(lp);
        snapshot.setVisibility(View.INVISIBLE);
        float scale = mLauncher.getItemScale();
        snapshot.setPivotX(0);
        snapshot.setPivotY(0);
        // To folder, not scale.
        if (mWorkspace.getOpenFolder() == null) {
            snapshot.setScaleX(scale);
            snapshot.setScaleY(scale);
        }

        mLauncher.getDragLayer().addView(snapshot);

        int[] sourcePos = itemPos;
        int[] targetPos;
        boolean isToFolderCenter = false;
        if (mWorkspace.getOpenFolder() != null) {
            targetPos = mLauncher.getFolderTargetPosition(mWorkspace.getOpenFolder(), targetItem);
            if (targetPos[0] == 0 || targetPos[1] == 0) {
                targetPos = mLauncher.getFolderCenter(mWorkspace.getOpenFolder());
                isToFolderCenter = true;
            }
            mWorkspace.getOpenFolder().scrollToBottom();
        } else {
            targetPos = mLauncher.getWorkspaceTargetPosition(targetItem);
        }

        // Create animation.
        AlphaStatus status = AlphaStatus.NONE;
        if (isToFolderCenter) {
            status = XDockViewAnimUtil.AlphaStatus.TO_FOLDER_CENTER;
        }
        ValueAnimator mAnimator = mAnim.startToWorkSpaceAnim(sourcePos,
                sourceItem, targetPos, targetItem, snapshot, status);
        mAllAnimator.add(mAnimator);
    }

    private void animItemToFolderIcon(final View souceItem,
                                      final ItemInfo targetInfo, final View folderIcon,
                                      final boolean isToStackFolder) {
        final View mSnapshot = mLauncher.createSnapshot(souceItem);

        final int[] itemPos;
        if (isStackMode()) {
            itemPos = mLayout.getStackPosition();
        } else {
            itemPos = mLauncher.getLocationPosition(souceItem);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        mSnapshot.setLayoutParams(lp);
        mSnapshot.setVisibility(View.INVISIBLE);
        float scale = mLauncher.getItemScale();
        mSnapshot.setPivotX(0);
        mSnapshot.setPivotY(0);
        mSnapshot.setScaleX(scale);
        mSnapshot.setScaleY(scale);
        mLauncher.getDragLayer().addView(mSnapshot);

        ((FolderIcon) folderIcon).addItem((ShortcutInfo) targetInfo);
        ((FolderIcon) folderIcon).hideItem((ShortcutInfo) targetInfo);

        int[] sourcePos = itemPos;
        int[] targetPos;
        if (isToStackFolder) {
            targetPos = mLauncher.getLocationPosition(folderIcon);
        } else {
            targetPos = mLauncher.getWorkspaceFolderIconPosition(
                    (FolderIcon) folderIcon, mSnapshot);
        }

        ValueAnimator mAnimator = mAnim.startToFolderIconAnim(sourcePos,
                souceItem, targetPos, targetInfo, folderIcon, mSnapshot, XDockViewAnimUtil.AlphaStatus.NONE);
        mAllAnimator.add(mAnimator);
    }

    // for FOLDER_MODE
    private void animInfoToWorkSpace(View souceItem, ShortcutInfo souceInfo,
                                     final View targetItem, AlphaStatus status) {
        View mSnapshot;
        View tmpSnapshot = mLauncher.createShortcut(R.layout.application,
                mWorkspace.getCurrentDropLayout(), souceInfo);
        mSnapshot = mLauncher.createSnapshot(tmpSnapshot);

        if (mSnapshot == null || targetItem == null) {
            return;
        }
        targetItem.setVisibility(View.INVISIBLE);

        final int[] itemPos = mLauncher.getLocationPosition(souceItem);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        mSnapshot.setLayoutParams(lp);
        mSnapshot.setVisibility(View.INVISIBLE);
        float scale = mLauncher.getItemScale();
        mSnapshot.setPivotX(0);
        mSnapshot.setPivotY(0);
        mSnapshot.setScaleX(scale);
        mSnapshot.setScaleY(scale);

        mLauncher.getDragLayer().addView(mSnapshot);

        int[] sourcePos = itemPos;
        int[] targetPos = mLauncher.getWorkspaceTargetPosition(targetItem);
        ValueAnimator mAnimator = mAnim.startToWorkSpaceAnim(sourcePos, null,
                targetPos, targetItem, mSnapshot, status);
        mAllAnimator.add(mAnimator);
    }

    private void animInfoToFolderIcon(final View souceItem,
                                      final ItemInfo souceInfo, final View folderIcon,
                                      final boolean isStackFolder,final AlphaStatus status) {
        View tmpSnapshot;
        tmpSnapshot = mLauncher
                .createShortcut(R.layout.application,
                        mWorkspace.getCurrentDropLayout(),
                        (ShortcutInfo) souceInfo);

        final View mSnapshot = mLauncher.createSnapshot(tmpSnapshot);

        final int[] itemPos = mLauncher.getLocationPosition(souceItem);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        mSnapshot.setLayoutParams(lp);
        mSnapshot.setVisibility(View.INVISIBLE);
        float scale = mLauncher.getItemScale();
        mSnapshot.setPivotX(0);
        mSnapshot.setPivotY(0);
        mSnapshot.setScaleX(scale);
        mSnapshot.setScaleY(scale);
        mLauncher.getDragLayer().addView(mSnapshot);

        ((FolderIcon) folderIcon).addItem((ShortcutInfo) souceInfo);
        ((FolderIcon) folderIcon).hideItem((ShortcutInfo) souceInfo);

        int[] sourcePos = itemPos;
        int[] targetPos;
        if (isStackFolder) {
            targetPos = mLauncher.getLocationPosition(folderIcon);
        } else {
            targetPos = mLauncher.getWorkspaceFolderIconPosition((FolderIcon) folderIcon, mSnapshot);
        }
        ValueAnimator mAnimator = mAnim.startToFolderIconAnim(sourcePos,
                souceItem, targetPos, souceInfo, folderIcon, mSnapshot, status);
        mAllAnimator.add(mAnimator);
    }

    public void removeDockItemByInfo(ItemInfo info, boolean anim) {
        final View item = findItemByInfo(info);
        if (item != null) {
            isNeedUpdateSelectCount = true;
            removeDockItem(item, anim);
        }
    }

    private View findItemByInfo(ItemInfo info) {
        for (int i = 0; i <= mLayout.getChildCount(); i++) {
            View view = mLayout.getChildItemAt(i);
            if (view == null) {
                return null;
            }
            ItemInfo itemInfo = (ItemInfo) view.getTag();
            if (itemInfo.equals(info)) {
                return view;
            }
        }
        return null;
    }

    /**
     * Remove the item in the dock.
     *
     * @param item the item will be removed.
     * @param anim whether animate or not.
     */
    public void removeDockItem(View item, boolean anim) {
        if (item == null) {
            return;
        }
        if (anim) {
            // animate before remove
            mLayout.removeItem(item, isNeedAdjustLayout);
        } else {
            mLayout.removeView(item);
        }
    }

    public void flyBackAllItems() {
        // TODO find proper location and fly back all items
        if (isAnimation) {
            endAnimatorSet();

            CellLayout cl = (CellLayout) mLauncher.getWorkspace().getPageAt(
                    mLauncher.getWorkspace().getCurrentPage());
            if (cl != null) {
                // cl.setNeedUpdateDrawCache(true);
            }
        }
        isCreateScreen = false;
        if (!isFolderMode()) {
            if (mLayout == null) {
                return;
            }
            int count = mLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = mLayout.getChildItemAt(i);
                // TODO fly back show remove items immediately
                ItemInfo mInfo = (ItemInfo) v.getTag();
                if (mInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    flyItemBackToWorkspace(v);
                } else {
                    FolderInfo mParentInfo = mLauncher.getFolderInfoById(mInfo.container);
                    if (mParentInfo == null || mParentInfo.getCount() == 0) {
                        ItemInfo info = (ItemInfo) v.getTag();
                        info.screenId = mWorkspace.getCurrentScreenId();
                        info.cellX = -1;
                        info.cellY = -1;
                        flyItemBackToWorkspace(v);
                    } else {
                        flyItemBackToFolderIcon(v);
                    }
                }
            }
            startAnimatorSet(false, false);
        } else {
            if (mFolderIcon == null) {
                return;
            }
            FolderInfo folderInfo = mFolderIcon.getFolderInfo();
            int mCount = folderInfo.getCount();
            for (int i = 0; i < mCount; i++) {
                ShortcutInfo mInfo = folderInfo.contents.get(i);
                recoverItemInfo(mInfo);
                if (mInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    flyInfoBackToWorkspace(mInfo, XDockViewAnimUtil.AlphaStatus.FOLDER_TO_WORKSPACE);
                } else {
                    FolderInfo mParentInfo = mLauncher.getFolderInfoById(mInfo.container);
                    if (mParentInfo == null || mParentInfo.getCount() == 0) {
                        mInfo.screenId = mWorkspace.getCurrentScreenId();
                        mInfo.cellX = -1;
                        mInfo.cellY = -1;
                        flyInfoBackToWorkspace(mInfo, XDockViewAnimUtil.AlphaStatus.FOLDER_TO_WORKSPACE);
                    } else {
                        flyInfoBackToFolderIcon(mInfo, XDockViewAnimUtil.AlphaStatus.FOLDER_TO_WORKSPACE);
                    }
                }
            }
            mAllInfos.clear();
            folderInfo.removeAll();
            startAnimatorSet(false, false);
            hide();
        }

        if (!isNormalMode()) {
            // doStack();
        }
        
	    // add by zhanggx1 for reordering all pages on 2013-11-20. s
		mLauncher.autoReorder("flyBackAllItems");
		// add by zhanggx1 for reordering all pages on 2013-11-20. e
    }

    public void exitByReload() {
        // TODO find proper location and fly back all items
        isCreateScreen = false;
        if (!isFolderMode()) {
            int count = mLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = mLayout.getChildItemAt(i);
                ItemInfo mInfo = (ItemInfo) v.getTag();
                if (mInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    getTargetItem(v, mInfo);
                } else {
                    FolderIcon folderIcon = (FolderIcon) mWorkspace.getFolderViewById(mInfo.container);
                    if (folderIcon == null || folderIcon.getFolder().isFull() || folderIcon.getFolderInfo().getCount() == 0) {
                        mInfo.screenId = mWorkspace.getCurrentScreenId();
                        mInfo.cellX = -1;
                        mInfo.cellY = -1;
                        getTargetItem(v, mInfo);
                    } else {
                        folderIcon.addItem((ShortcutInfo) mInfo);
                    }
                }
            }
            mLayout.removeAllViews();
        } else {
            FolderInfo folderInfo = mFolderIcon.getFolderInfo();
            int mCount = folderInfo.getCount();
            for (int i = 0; i < mCount; i++) {
                ShortcutInfo mInfo = folderInfo.contents.get(i);
                recoverItemInfo(mInfo);
                if (mInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    getTargetItem(null, mInfo);
                } else {
                    FolderIcon folderIcon = (FolderIcon) mWorkspace.getFolderViewById(mInfo.container);
                    if (folderIcon == null || folderIcon.getFolder().isFull() || folderIcon.getFolderInfo().getCount() == 0) {
                        mInfo.screenId = mWorkspace.getCurrentScreenId();
                        mInfo.cellX = -1;
                        mInfo.cellY = -1;
                        getTargetItem(null, mInfo);
                    } else {
                        folderIcon.addItem((ShortcutInfo) mInfo);
                    }
                }
            }
            mAllInfos.clear();
            folderInfo.removeAll();
        }

        if (mLayout.getChildCount() <= 1 && mAllInfos.size() <= 1) {
            if (!isNormalMode()) {
                mState = State.NORMAL;
                mLayout.setTag(null);
                // remove folder icon view before animate
                if (mFolderIcon != null) {
                    removeDockViewFolder();
                }
            }
        }

        int folder_item_size = 0;
        if (mFolderIcon != null) {
            folder_item_size = mFolderIcon.getFolderInfo().getCount();
        }
        if (mLayout.getChildCount() == 0 && mAllInfos.size() == 0 && folder_item_size == 0) {
            mLauncher.exitDockViewByReload();
        }

        updateSelectCount();
		mLauncher.autoReorder("exitByReload");
    }

    private FolderIcon createFolderIconOnWorkspace(CellLayout target, View destItem) {
        ShortcutInfo destInfo = (ShortcutInfo) destItem.getTag();
        target.removeView(destItem);
        FolderIcon fi = mLauncher.addFolder(target, destInfo.container,
                destInfo.screenId, destInfo.cellX, destInfo.cellY);
        destInfo.cellX = -1;
        destInfo.cellY = -1;

        fi.addItem(destInfo);

        return fi;
    }

    private FolderIcon mergeFolderIcon() {
        int pageCount = mWorkspace.getChildCount();

        CellLayout targetCell = null;
        View targetView = null;
        boolean hasFindCell = false;
        for (int index = pageCount - 1; index >= 0; index--) {
            long screenID = mWorkspace.getScreenIdForPageIndex(index);
            CellLayout mCellView = mWorkspace.getScreenWithId(screenID);
            int mCountX = mCellView.getCountX();
            int mCountY = mCellView.getCountY();
            for (int j = mCountY - 1; j >= 0; j--) {
                for (int i = mCountX - 1; i >= 0; i--) {
                    View mView = mCellView.getChildAt(i, j);
                    if (mView instanceof BubbleTextView) {
                        targetCell = mCellView;
                        targetView = mView;
                        hasFindCell = true;
                        break;
                    }
                }
                if (hasFindCell) {
                    break;
                }
            }
            if (hasFindCell) {
                break;
            }
        }

        if (hasFindCell) {
            // ToDo
            // workspace合并文件夹
            return createFolderIconOnWorkspace(targetCell, targetView);
        } else {
            // ToDo
            // hotseat生成文件夹
            CellLayout layout = mLauncher.getHotseat().getLayout();

            if (layout.getItemChildCount() < layout.getCountX()) {
                return mLauncher.getHotseat().creatFolderToHotseat();
            }
        }
        return null;
    }


    private View getTargetItem(View item, ItemInfo info) {
        int spanX = info.spanX;
        int spanY = info.spanY;
        if (spanX < 1) {
            spanX = 1;
        }
        if (spanY < 1) {
            spanY = 1;
        }

        int pageIndex = mWorkspace.getPageIndexForScreenId(mWorkspace
                .getCurrentScreenId());

        CellLayout oldParentView = mWorkspace.getScreenWithId(info.screenId);

        if (!isToOriginalPos(oldParentView, info)) {
            if (!isToCurrentScreen(mWorkspace,
                    mWorkspace.getCurrentDropLayout(), info, spanX, spanY)) {
                if (!isToNewScreen(mWorkspace, info, spanX, spanY)) {
                    int pageCount = mWorkspace.getChildCount();
                    boolean hasFindCell = false;
                    for (int i = pageIndex + 1; i < pageCount; i++) {
                        CellLayout mParentView = (CellLayout) mWorkspace
                                .getPageAt(i);
                        int[] targetCell = new int[]{-1, -1};
                        mParentView.findCellForSpan(targetCell, spanX, spanY);
                        if (targetCell == null || targetCell[0] < 0
                                || targetCell[1] < 0) {
                            LauncherLog.e(TAG, "showOutOfSpaceMessage");
                        } else {
                            info.cellX = targetCell[0];
                            info.cellY = targetCell[1];
                            info.screenId = mWorkspace
                                    .getScreenIdForPageIndex(i);
                            hasFindCell = true;
                            break;
                        }
                    }

                    if (!hasFindCell) {
                        for (int i = pageIndex - 1; i >= 0; i--) {
                            CellLayout mParentView = (CellLayout) mWorkspace
                                    .getPageAt(i);
                            int[] targetCell = new int[]{-1, -1};
                            mParentView.findCellForSpan(targetCell, spanX,
                                    spanY);
                            if (targetCell == null || targetCell[0] < 0
                                    || targetCell[1] < 0) {
                                LauncherLog.e(TAG, "showOutOfSpaceMessage");
                            } else {
                                info.cellX = targetCell[0];
                                info.cellY = targetCell[1];
                                info.screenId = mWorkspace
                                        .getScreenIdForPageIndex(i);
                                hasFindCell = true;
                                break;
                            }
                        }
                    }

                    if (!hasFindCell) {
                        if (tmpFolderIcon == null) {
                            tmpFolderIcon = mergeFolderIcon();
                        }

                        if (tmpFolderIcon != null) {
                            if (item != null) {
                                animItemToFolderIcon(item, info, tmpFolderIcon,
                                        false);
                            } else {
                                animInfoToFolderIcon(mFolderIcon, info,
                                        tmpFolderIcon, false, XDockViewAnimUtil.AlphaStatus.FOLDER_TO_WORKSPACE);
                            }
                        }
                        return null;
                    }
                }
            }
        }

        View targetItem;
        targetItem = mLauncher.createShortcut(R.layout.application,
                mWorkspace.getCurrentDropLayout(), (ShortcutInfo) info);

        final long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;

        mWorkspace.addDockViewItemToWorkspace(targetItem, container,
                info.screenId, info.cellX, info.cellY, spanX, spanY);

        return targetItem;

    }

    private void flyItemBackToFolderIcon(View item) {
        if (item == null || item.getTag() == null) {
            return;
        }
        final ItemInfo info = (ItemInfo) item.getTag();
        final View folderIcon = mWorkspace.getFolderViewById(info.container);

        if (((FolderIcon) folderIcon).getFolder().isFull()) {
            info.screenId = mWorkspace.getCurrentScreenId();
            info.cellX = -1;
            info.cellY = -1;
            flyItemBackToWorkspace(item);
        } else {
            animItemToFolderIcon(item, info, folderIcon, false);
        }

    }

    // for FOlDER_MODE
    private void flyInfoBackToFolderIcon(ItemInfo info,AlphaStatus status) {
        final View folderIcon = mWorkspace.getFolderViewById(info.container);
        if (((FolderIcon) folderIcon).getFolder().isFull()) {
            info.screenId = mWorkspace.getCurrentScreenId();
            info.cellX = -1;
            info.cellY = -1;
            flyInfoBackToWorkspace(info, status);
        } else {
            animInfoToFolderIcon(mFolderIcon, info, folderIcon, false, status);
        }
    }

    private void flyItemBackToWorkspace(View item) {
        if (item == null || item.getTag() == null) {
            return;
        }
        if (LauncherLog.DEBUG_EDITMODE) {
            LauncherLog.d(TAG, " flyItemBackToWorkspace, item = " + item);
        }
        final ItemInfo info = (ItemInfo) item.getTag();
        final View targetItem = getTargetItem(item, info);
        animItemToWorkSpace(item, targetItem);
    }

    private void flyInfoBackToWorkspace(ItemInfo info,AlphaStatus status) {
        final View targetItem = getTargetItem(null, info);
        animInfoToWorkSpace(mFolderIcon, (ShortcutInfo) info, targetItem, status);
    }

    public void flyToStackFolderIcon(View folderIcon) {
        int count = mLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mLayout.getChildItemAt(i);
            // TODO fly back show remove items immediately
            ShortcutInfo mInfo = (ShortcutInfo) v.getTag();
            ShortcutInfo tmpInfo = new ShortcutInfo(mContext, mInfo);
            mAllInfos.add(tmpInfo);
            animItemToFolderIcon(v, mInfo, folderIcon, true);
        }
        startAnimatorSet(false, true);
    }

    public void freeStackFolderIcon() {
        if (isDraging) {
            return;
        }
        FolderInfo folderInfo = mFolderIcon.getFolderInfo();
        int mCount = folderInfo.getCount();
        LauncherLog.d(TAG, "freeStackFolderIcon mCount : " + mCount);
        for (int i = mCount - 1; i >= 0; i--) {
            ShortcutInfo mInfo = folderInfo.contents.get(i);
            recoverItemInfo(mInfo);
            View item = addExternalItem(mInfo, 0);
            if (item != null) {
                item.setVisibility(View.VISIBLE);
            }
        }
        mAllInfos.clear();
        folderInfo.removeAll();
        doStack();
    }

    private void clearFolderIcon() {
        mFolderIcon = null;
        mAllInfos.clear();
        doStack();
    }

    public boolean isToOriginalPos(CellLayout view, ItemInfo info) {
        if (view == null) {
            return false;
        }
        return !view.isHaveItemForCell(info.cellX, info.cellY);
    }

    public boolean isToCurrentScreen(Workspace workspace, CellLayout view,
                                     ItemInfo info, int spanX, int spanY) {
        if (view == null) {
            return false;
        }
        int[] targetCell = new int[]{-1, -1};
        view.findCellForSpan(targetCell, spanX, spanY);
        if (targetCell == null || targetCell[0] < 0 || targetCell[1] < 0) {
            return false;
        }

        info.screenId = workspace.getIdForScreen(view);
        info.cellX = targetCell[0];
        info.cellY = targetCell[1];
        return true;
    }

    public boolean isToNewScreen(Workspace workspace, ItemInfo info, int spanX,
                                 int spanY) {
        CellLayout cl = (CellLayout) workspace.getPageAt(workspace
                .getChildCount() - 1);
        if (cl == null) {
            return false;
        }

        if (workspace.isInOverviewMode()) {
            boolean isAddLayout = cl.isAddLayout();

            if (isCreateScreen) {
                cl = (CellLayout) workspace.getPageAt(workspace
                        .getChildCount() - 2);
                if (cl == null) {
                    return false;
                }
            }

            int[] targetCell = new int[]{-1, -1};
            cl.findCellForSpan(targetCell, spanX, spanY);
            if (targetCell == null || targetCell[0] < 0 || targetCell[1] < 0) {
                if (!isAddLayout) {
                    return false;
                }
            } else {
                info.cellX = targetCell[0];
                info.cellY = targetCell[1];
                info.screenId = workspace.getIdForScreen(cl);
                if (workspace.getChildCount() < Workspace.getMaxScreenCount()) {
                    isCreateScreen = true;
                }
                return true;
            }

            cl = (CellLayout) workspace.getPageAt(workspace
                    .getChildCount() - 1);
            if (cl == null) {
                return false;
            }

            if (workspace.getChildCount() < Workspace.getMaxScreenCount()) {
                isCreateScreen = true;
            }
            info.cellX = 0;
            info.cellY = 0;
            info.screenId = workspace.getIdForScreen(cl);
            return true;
        } else {
            if (cl.getItemChildCount() == 0 || isCreateScreen == true) {
                isCreateScreen = true;
                int[] targetCell = new int[]{-1, -1};
                cl.findCellForSpan(targetCell, spanX, spanY);

                if (targetCell == null || targetCell[0] < 0 || targetCell[1] < 0) {

                } else {
                    info.cellX = targetCell[0];
                    info.cellY = targetCell[1];
                    info.screenId = workspace.getIdForScreen(cl);
                    return true;
                }
            }

            if (workspace.getChildCount() == Workspace.getMaxScreenCount()
                    && !cl.isAddLayout()) {
                return false;
            }
            boolean isAddScreen = workspace.addEmptyScreen();

            if (!isAddScreen) {
                return false;
            }
            isCreateScreen = true;
            info.cellX = 0;
            info.cellY = 0;
            info.screenId = workspace.getScreenIdForPageIndex(workspace
                    .getChildCount() - 1);
            return true;
        }

    }

    private void startDragFolder(View child) {
        mDragLayer = mLauncher.getDragLayer();
        mLauncher.getWorkspace().onDragStartedWithDockFolder(child,
                Workspace.DRAG_BITMAP_PADDING);

        // The drag bitmap follows the touch point around on the screen
        final Bitmap b = mWorkspace.createDragBitmap(child, new Canvas(),
                Workspace.DRAG_BITMAP_PADDING);

        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();

        float scale = mDragLayer.getLocationInDragLayer(child, mTempPoint);
        int dragLayerX = Math.round(mTempPoint[0]
                - (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY = Math.round(mTempPoint[1]
                - (bmpHeight - scale * bmpHeight) / 2
                - Workspace.DRAG_BITMAP_PADDING / 2);

        Point dragVisualizeOffset = null;
        Rect dragRect = null;
        if (child instanceof FolderIcon && grid != null) {
            int previewSize = grid.folderIconSizePx;
            dragRect = new Rect(0, child.getPaddingTop(), child.getWidth(),
                    previewSize);
        }

        child.setVisibility(View.INVISIBLE);
        mDragItem = child;

        if (mDragController == null) {
            return;
        }

        if (child instanceof FolderIcon) {
            FolderIcon folderIcon = (FolderIcon) child;
            folderIcon.setLayerType(View.LAYER_TYPE_NONE, null);
            folderIcon.getFolderInfo();
            FolderInfo info = folderIcon.getFolderInfo(); // (FolderInfo)
            // child.getTag();
            FolderInfo c_info = info.copy();
            DragView dragView = mDragController.startDrag(b, dragLayerX,
                    dragLayerY, this, c_info, DragController.DRAG_ACTION_MOVE,
                    dragVisualizeOffset, dragRect, scale);
            if (dragView.getBitmap() != b) {
                b.recycle();
            }
        } else {
            b.recycle();
        }
    }

    private void startDragViewGroup(XDockViewLayout layout, View item) {
        if (layout == null) {
            return;
        }

        mDragLayer = mLauncher.getDragLayer();
        mDragLayer.getLocationInDragLayer(item, mTempPoint);
        final int dragLayerX = mTempPoint[0];
        final int dragLayerY = mTempPoint[1];
        List<Bitmap> list;
        list = createDragBitmap(layout, new Canvas(),
                Workspace.DRAG_BITMAP_PADDING);

        layout.setVisibility(View.INVISIBLE);
        mDragItem = layout;

        if (mDragController == null) {
            return;
        }
        LayoutInfo layoutInfo = new LayoutInfo();
        for (int i = 0; i < mLayout.getChildCount(); i++) {
            View v = layout.getChildItemAt(i);
            v.setVisibility(INVISIBLE);
            Object info = v.getTag();
            layoutInfo.add((ShortcutInfo) info);
        }
        mLayout.setTag(layoutInfo);
        mDragController.startDrag(list, dragLayerX, dragLayerY, this, layout,
                DragController.DRAG_ACTION_MOVE, null, null, 0);

        for (int i = 0; i < list.size(); i++) {
            View v = layout.getChildItemAt(i);
            Bitmap b = list.get(i);
            if (!(v instanceof BubbleTextView)) {
                b.recycle();
            }
        }
    }

    /**
     * Returns a new bitmap list to show when the given ViewGroup is being
     * dragged around. Responsibility for the bitmap is transferred to the
     * caller.
     */
    public List<Bitmap> createDragBitmap(XDockViewLayout layout, Canvas canvas,
                                         int padding) {
        List<Bitmap> list = new ArrayList<>();

        for (int i = 0; i <= layout.getChildCount() - 1; i++) {
            Bitmap b;
            View v = layout.getChildItemAt(i);
            b = mWorkspace.createDragBitmap(v, new Canvas(),
                    Workspace.DRAG_BITMAP_PADDING);
            list.add(b);
        }
        LauncherLog.d(TAG, " createDragBitmap, list :" + list.size());
        return list;
    }

    public void startDrag(View item) {
        if (item == null) {
            return;
        }

        hasDragOut = false;

        mTempCell[0] = mLayout.indexOfChildItem(item);
        mTempCell[1] = 0;

        mDragInfo = (ItemInfo) item.getTag();
        mDragInfo.dockCellX = mTempCell[0];
        mLauncher.getWorkspace().onDragStartedWithDockItem(item,
                Workspace.DRAG_BITMAP_PADDING);
        beginDragShared(item, this);
    }

    private void beginDragShared(View child, DragSource source) {
        mDragLayer = mLauncher.getDragLayer();
        mDragLayer.getLocationInDragLayer(child, mTempPoint);
        final int dragLayerX = mTempPoint[0];
        final int dragLayerY = mTempPoint[1];
        Bitmap b;
        b = mWorkspace.createDragBitmap(child, new Canvas(),
                Workspace.DRAG_BITMAP_PADDING);
        if (b == null) {
            return;
        }
        child.setVisibility(View.INVISIBLE);
        mDragItem = child;

        // Clear the pressed state if necessary
        if (child instanceof BubbleTextView) {
            BubbleTextView icon = (BubbleTextView) child;
            icon.setPressed(false);
        }
        if (mDragController == null) {
            return;
        }
        Point dragVisualizeOffset = new Point(
                -Workspace.DRAG_BITMAP_PADDING / 2,
                Workspace.DRAG_BITMAP_PADDING / 2);
        DragView dragView = mDragController.startDrag(b, dragLayerX,
                dragLayerY, source, child.getTag(),
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, null, 0);

        if (dragView.getBitmap() != b) {
            b.recycle();
        }
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        isDraging = true;
    }

    @Override
    public void onDragEnd() {
        isDraging = false;
    }

    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {

    }

    public void animDragObjectBack(DragObject d) {
        // TODO animate to former position
        for (DragView v : d.dragViewList) {
            mLauncher.getDragLayer().removeView(v);
        }
        for (int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildItemAt(i);
            v.setVisibility(VISIBLE);
        }
    }

    public void deferCompleteDropAfterUninstallActivity() {
        mDeferDropAfterUninstall = true;
    }

    public void onUninstallActivityReturned(boolean success) {
        mDeferDropAfterUninstall = false;
        mUninstallSuccessful = success;
        if (mDeferredAction != null) {
            mDeferredAction.run();
        }
    }


    @Override
    public void onDropCompleted(final View target, final DragObject d, final boolean isFlingToDelete, final boolean success) {
        LauncherLog.i(TAG, "onDropCompleted, target: " + target + ", XDragObject: " + d
                + ", success: " + success);
        if (mDeferDropAfterUninstall) {
            mDeferredAction = new Runnable() {
                public void run() {
                    onDropCompleted(target, d, isFlingToDelete, success);
                    mDeferredAction = null;
                }
            };
            return;
        }

        if (target instanceof DeleteDropTarget) {
            mDragController.onEndDrag();
            if (isStackMode()) {
                mLayout.setVisibility(View.VISIBLE);
            }
            mDragInfo = null;
            mDragItem = null;
            // TODO : PageIndicator Function.
            // mWorkspace.getPageIndicator().processIndicatorAfterDrop(mWorkspace.isInEditViewMode());
            mWorkspace.onDropCompleteOutside();
            return;
        } else if (isStackMode()
                || (d.dragViewList != null && d.dragViewList.size() != 0)) {
            mDragController.onEndDrag();
            // if drag object is layout
            if (!success && isToFolderIcon) {
                int count = mLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    View v = mLayout.getChildItemAt(i);
                    ShortcutInfo mInfo = (ShortcutInfo) v.getTag();
                    ShortcutInfo tmpInfo = new ShortcutInfo(mContext, mInfo);
                    mAllInfos.add(tmpInfo);
                }

                mWorkspace.addToDockviewFolderIcon(d, mFolderIcon);
            } else {
                if (!success) {
                    animDragObjectBack(d);
                }
            }
            mLayout.setVisibility(View.VISIBLE);
            mDragInfo = null;
            mDragItem = null;
            // TODO : PageIndicator Function.
            // mWorkspace.getPageIndicator().processIndicatorAfterDrop(mWorkspace.isInEditViewMode());
            mDragInfo = null;
            mDragItem = null;

            // add by zhanggx1 for memory on 2014-07-03.s
            mWorkspace.onDropCompleteOutside();
            // add by zhanggx1 for memory on 2014-07-03.e
            return;
        } else if (isFolderMode()) {
            if (success) {
                if (target != this) {
                    if (mLayout.getChildCount() == 0) {
                        // exit folder mode and clear folder
                        clearFolderIcon();
                        mLauncher.dismissDockView();
                        updateSelectCount();
                    }
                }
            } else {
                mLauncher.getDragLayer().removeView(d.dragView);
                mFolderIcon.setVisibility(View.VISIBLE);
                mDragController.onEndDrag();
            }

            mDragInfo = null;
            mDragItem = null;
            // TODO : PageIndicator Function.
            // mWorkspace.getPageIndicator().processIndicatorAfterDrop(mWorkspace.isInEditViewMode());
            // add by zhanggx1 for memory on 2014-07-03.s
            mWorkspace.onDropCompleteOutside();
            // add by zhanggx1 for memory on 2014-07-03.e
            return;
        }
        // dumpDragObject(d);
        Object ob = mDragItem.getTag();
        if (ob instanceof FolderInfo) {
            // begin fix bug LVP-3329 by zhangxh14
            final View targetView = mLayout.getChildItemAt(0);
            if (targetView == null) {
                mLauncher.getDragLayer().removeView(d.dragView);
            } else {
                Rect r = new Rect();
                mLauncher.getDragLayer().getViewRectRelativeToSelf(d.dragView, r);
                final int fromX = r.left;
                final int fromY = r.top;
                final int[] sourcePos = new int[]{fromX, fromY};

                final int[] targetPos = mLauncher.getDockViewTargetPosition();
                final View snapshot = mLauncher.createSnapshot(targetView);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.LEFT;
                snapshot.setLayoutParams(lp);
                snapshot.setVisibility(View.INVISIBLE);
                snapshot.setPivotX(0);
                snapshot.setPivotY(0);
                float scale = getItemScale();
                snapshot.setScaleX(scale);
                snapshot.setScaleY(scale);
                snapshot.setX(sourcePos[0]);
                snapshot.setY(sourcePos[1]);
                mLauncher.getDragLayer().addView(snapshot);

                ValueAnimator mAnimator = mAnim.startDragViewAnim(snapshot,
                        sourcePos, d.dragView, targetPos, targetView, false);
                mAllAnimator.add(mAnimator);
                startAnimatorSet(true, false);

                mDragController.onEndDrag();
            }
        } else {
            ItemInfo info = (ItemInfo) ob;
            if (success) {
                info.cellX = mDragInfo.cellX;
                info.cellY = mDragInfo.cellY;
                if (target != this) {
                    if (mLayout.getChildCount() == 0) {
                        mLauncher.dismissDockView();
                    }
                }
                if (isDragDelete) {
                    updateSelectCount();
                }
                dragDeleteIsCompleted = true;
            } else {
                View item = addDragItemInfo((ShortcutInfo) info, mTempCell[0]);
                if (item != null) {
                    if (!mLauncher.isDockViewShowing()) {
                        mLauncher.showDockView();
                    }
                    Rect r = new Rect();
                    mLauncher.getDragLayer().getViewRectRelativeToSelf(
                            d.dragView, r);
                    animDragviewIntoPosition(d.dragView, mDragInfo, r);
                    startAnimatorSet(true, false);
                }
                mDragController.onEndDrag();
            }
        }
        mDragInfo = null;
        mDragItem = null;

        // TODO : PageIndicator Function.
        // mWorkspace.getPageIndicator().processIndicatorAfterDrop(mWorkspace.isInEditViewMode());
        // add by zhanggx1 for memory on 2014-07-03.s
        mWorkspace.onDropCompleteOutside();
        // add by zhanggx1 for memory on 2014-07-03.e
    }

    @Override
    public boolean isDropEnabled() {
        return this.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDrop(DragObject dragObject) {
        LauncherLog.d(TAG, "onDrop");
        // dumpDragObject(dragObject);

        if (mDragItem != null) {
            mDragItem.setVisibility(View.VISIBLE);
        }
        hasInvisible = false;
        LauncherLog.d(TAG, "onDrop isFolderMode : " + isFolderMode());
        if (isFolderMode()) {
            if (dragObject.dragSource != this) {
                // TODO add item to folder
                ShortcutInfo mInfo = (ShortcutInfo) dragObject.dragInfo;
                ShortcutInfo tmpInfo = new ShortcutInfo(mContext, mInfo);
                mAllInfos.add(tmpInfo);
                mFolderIcon.getFolderInfo().add(mInfo);
                updateSelectCount();
                // animItemToFolderIcon(v, mInfo, mFolder);
            }
            mFolderIcon.onDragExit(null);
        } else if (isNormalMode()) {
            // mLayout.showLayoutItems();
        }

        for (DragView v : dragObject.dragViewList) {
            mLauncher.getDragLayer().removeView(v);
        }

        // mLauncher.getDragLayer().removeView(dragObject.dragView);
        mDragController.onDeferredEndDrag(dragObject.dragView);

        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        if (mShowToast != null) {
            mShowToast = null;
        }
        if (dragObject.dragSource != this) {
            mDragInfo = null;
            mDragItem = null;
        }
    }

    private boolean hasDragOut = false;
    private boolean hasInvisible = false;

    @Override
    public void onDragEnter(DragObject dragObject) {
        if (isStackMode()
                && (dragObject.dragViewList != null && dragObject.dragViewList
                .size() != 0)) {
            return;
        } else if (isFolderMode()) {
            return;
        }
        lastTargetX = -1;
        if (dragObject.dragSource != this) {
            if (!acceptDropWhenDragEnter(dragObject)) {
                return;
            }
            
            /** Lenovo-SW zhaoxin5 20150710 add for LANCHROW-231 START */
            if((dragObject.dragSource instanceof Folder) && mLauncher.isDockViewShowing()) {
            	((Folder)dragObject.dragSource).animateFromLastToFirst();
            }
            /** Lenovo-SW zhaoxin5 20150710 add for LANCHROW-231 START */
            
            isDragFromWorkSpace = true;

            Folder folder = mWorkspace.getOpenFolder();
            if (folder != null) {
                folder.cancelExitFolder();
            }

            final int[] touchXY = new int[]{dragObject.x - Math.abs(mLayout.getRelativeX()), dragObject.y};
            mReorderAlarm.cancelAlarm();
            // add an invisible item
            if (isStackMode()) {
                touchXY[0] = 0;
            }
            onDropExternal(touchXY, dragObject.dragInfo, dragObject);
        } else {
            // out and in
            if (hasDragOut) {
                final int[] touchXY = new int[]{dragObject.x, dragObject.y};
                mReorderAlarm.cancelAlarm();
                // add an invisible item
                onDropExternal(touchXY, dragObject.dragInfo, dragObject);
            }
        }
    }

    private int lastTargetX = -1;

    private boolean isToFolderIcon = false;

    @Override
    public void onDragOver(DragObject dragObject) {
        if (!acceptDropWhenDragEnter(dragObject)) {
            return;
        }

        isToFolderIcon = false;
        if ((isStackMode() && dragObject.dragViewList != null && dragObject.dragViewList
                .size() != 0) || isFolderMode()) {
            if (mFolderIcon == null) {
                return;
            }

            int[] layoutLocation = mLauncher.getLocationPosition(mLayout);
            int x = layoutLocation[0] + dragObject.x + mLayout.getRelativeX();
            int y = layoutLocation[1] + dragObject.y;

            int[] location = mLauncher.getLocationPosition(mFolderIcon);
            int folderWidth = mFolderIcon.getWidth();
            int folderHeight = mFolderIcon.getHeight();
            Rect folderRect = new Rect(location[0], location[1], location[0]
                    + folderWidth, location[1] + folderHeight);

            float scale = 1.0f;
            if (!isFolderMode()) {
                scale = dragObject.dragViewList.get(0).getScaleX();
            }

            if (folderRect.contains(x, y) && scale > 0.9) {
                if (mFolderOuterRings.size() == 0) {
                    mFolderIcon.onDockViewDragEnter(dragObject.dragInfo);
				}
                isToFolderIcon = true;
            } else {
                mFolderIcon.onDragExit(null);
            }
            return;
        }

        if (isNormalMode()) {
            int pixelX = dragObject.x + mLayout.getRelativeX();
            int pixelY = dragObject.y;
            mTargetCell = mLayout.findTargetCell(pixelX, pixelY);

            if (mTargetCell[0] != lastTargetX) {
                lastTargetX = mTargetCell[0];
                mReorderAlarm.cancelAlarm();
                mReorderAlarm.setOnAlarmListener(mReorderAlarmListener);
                mReorderAlarm.setAlarm(BEGIN_REORDER_DURATION);
            }
        }
    }

    private final Alarm mReorderAlarm = new Alarm();
    private static final int REORDER_ANIMATION_DURATION = 150;
    private static final int BEGIN_REORDER_DURATION = 160;
    OnAlarmListener mReorderAlarmListener = new OnAlarmListener() {
        @Override
        public void onAlarm(Alarm alarm) {
            mLayout.animateItemToPosition(mDragItem, mTargetCell[0],
                    REORDER_ANIMATION_DURATION);
        }
    };

    @Override
    public void onDragExit(DragObject dragObject) {
        if (isStackMode()
                && (dragObject.dragViewList != null && dragObject.dragViewList
                .size() != 0)) {
            if (mFolderIcon != null) {
                mFolderIcon.onDragExit(null);
            }
            return;
        } else if (isFolderMode()) {
            if (isToFolderIcon && mFolderIcon != null) {
                mFolderIcon.onDragExit(null);
            }
            return;
        }
        // dumpDragObject(dragObject);
        mReorderAlarm.cancelAlarm();
        if (!dragObject.dragComplete) {
            hasDragOut = true;
            isNeedAdjustLayout = true;
            isDragDelete = true;
            dragDeleteIsCompleted = false;
            removeDockItem(mDragItem, true);
        }
    }

    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {

    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        boolean retVal = true;

        if (dragObject.dragViewList != null
                && dragObject.dragViewList.size() != 0) {
            dragObject.deferDragViewCleanupPostAnimation = false;
            return false;
        }

        if (!(dragObject.dragInfo instanceof ItemInfo)) {
            return false;
        }

        if (dragObject.dragInfo instanceof FolderInfo) {
            return false;
        } else {
            ItemInfo info = (ItemInfo) dragObject.dragInfo;
            switch (info.itemType) {
                // case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                    retVal = false;
                    break;
                default:
                    retVal = true;
                    break;
            }
        }

        if (hasInvisible) {
            return true;
        }
        if (dragObject.dragSource != this && isDockViewFull()) {
            retVal = false;
        }

        return retVal;
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        // We want the dockview to have the area of view's own display
        if (this.getVisibility() == View.VISIBLE) {
            mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this,
                    outRect);
        }
    }

    private ShowToast mShowToast = null;
    private Toast mToast = null;

    private class ShowToast implements Runnable {
        private int mStringId = 0;

        public ShowToast(int stringId) {
            mStringId = stringId;
        }

        public void setMessageId(int stringId) {
            mStringId = stringId;
        }

        @Override
        public void run() {
            if (mToast == null) {
                mToast = Toast.makeText(mContext, mStringId, Toast.LENGTH_SHORT);
            } else {
                mToast.setText(mStringId);
            }
            mToast.show();
        }
    }

    // check if dragObject is allowed to drop
    private boolean acceptDropWhenDragEnter(DragObject dragObject) {
        boolean retVal = true;

        if (dragObject.dragSource == this) {
            return true;
        }

        if (dragObject.dragViewList != null
                && dragObject.dragViewList.size() != 0) {
            return true;
        }

        if (!(dragObject.dragInfo instanceof ItemInfo)) {
            if (mShowToast == null) {
                mShowToast = new ShowToast(
                        R.string.cannot_be_placed_on_dockview);
            } else {
                mShowToast.setMessageId(R.string.cannot_be_placed_on_dockview);
            }
            post(mShowToast);
            return false;
        }
        if (dragObject.dragInfo instanceof FolderInfo) {
            if (mShowToast == null) {
                mShowToast = new ShowToast(
                        R.string.cannot_be_placed_on_dockview_folder);
            } else {
                mShowToast
                        .setMessageId(R.string.cannot_be_placed_on_dockview_folder);
            }
            post(mShowToast);
            return false;
        } else {
            ItemInfo info = (ItemInfo) dragObject.dragInfo;
            switch (info.itemType) {
                // case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                    retVal = false;
                    break;
                default:
                    retVal = true;
                    break;
            }
        }

        if (!retVal) {
            if (mShowToast == null) {
                mShowToast = new ShowToast(
                        R.string.cannot_be_placed_on_dockview);
            } else {
                mShowToast.setMessageId(R.string.cannot_be_placed_on_dockview);
            }
            post(mShowToast);
            return false;
        }
        if (hasInvisible) {
            return true;
        }
        if (dragObject.dragSource != this && isDockViewFull()) {
            retVal = false;
            showOutOfDockViewMessage();
        }
        return retVal;
    }

    private final int[] mTempPoint = new int[2];
    private final int[] mTempCell = new int[2];
    private int[] mTargetCell = new int[2];
    // item dragged from or drop to dockview
    private View mDragItem;

    public View getDragItem() {
        return mDragItem;
    }

    private void onDropExternal(final int[] touchXY, final Object dragInfo,
                                DragObject d) {
        ItemInfo info = (ItemInfo) dragInfo;
        switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (info instanceof ShortcutInfo) {
                    mTargetCell = mLayout.findTargetCell(touchXY[0], touchXY[1]);
                    LauncherLog.d(TAG,
                            "onDrop external mTargetCell =" + mTargetCell[0]
                                    + "=mContent.getChildCount()="
                                    + mLayout.getChildCount());

                    mDragItem = addDragItemInfo((ShortcutInfo) info, mTargetCell[0]);
                    hasInvisible = true;
                }
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_LAYOUT:
                break;
            default:
                throw new IllegalStateException("Unknown item type: "
                        + info.itemType);
        }
    }

    public void dumpDragObject(DragObject dragObject) {
        LauncherLog.i(TAG, "dragObject: " + dragObject);
        LauncherLog.i(TAG, "dragObject.dragComplete: " + dragObject.dragComplete);
        LauncherLog.i(TAG, "dragObject.dragSource: " + dragObject.dragSource);
        LauncherLog.i(TAG, "dragObject.dragInfo: " + dragObject.dragInfo);
        LauncherLog.i(TAG, "dragObject.cancelled: " + dragObject.cancelled);
        LauncherLog.i(TAG,
                "dragObject.dragViewList.size(): "
                        + dragObject.dragViewList.size());
        LauncherLog.i(TAG, "x = " + dragObject.x + "y = " + dragObject.y);
    }

    public void dumpStack() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();

        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                LauncherLog.e("XDockView", stackElements[i].getClassName());
                LauncherLog.e("XDockView", stackElements[i].getFileName());
                LauncherLog.e("XDockView", "" + stackElements[i].getLineNumber());
                LauncherLog.e("XDockView", stackElements[i].getMethodName());
                LauncherLog.e("XDockView", "-------------the " + i
                        + " level-------------------");
            }
        }
    }

    public void show(boolean backmost) {
        mDragLayer = mLauncher.getDragLayer();
        LauncherLog.d(TAG, "show, backmost: " + backmost);
        if (mDragLayer != null) {
            bringChildToFront(mDragLayer, this);
            invalidate();
        }
    }

    private void bringChildToFront(ViewGroup parent, View child) {
        parent.removeView(child);
        LauncherLog.d(TAG, "bringChildToFront, parent.getChildCount(): " + parent.getChildCount());
        parent.addView(child, parent.getChildCount());
    }

    @Override
    public boolean onLongClick(View item) {
        // Validate
        if (item.equals(this)) {
            return false;
        }

        if (mWorkspace != null && !mWorkspace.isInOverviewMode()) {
            return false;
        }

        if (mDragController.isDragging() || isAnimation || isAnimStacking()) {
            return false;
        }

        if (isStackMode()) {
            if (!(item instanceof FolderIcon)) {
                startDragViewGroup(mLayout, item);
            }
        } else if (isFolderMode()) {
            LauncherLog.i(TAG, "onLongClick, isFolderMode: " + item);
            if (item instanceof FolderIcon) {
                startDragFolder(item);
            }
        } else {
            Object o = item.getTag();
            if (o instanceof ItemInfo) {
                if (mWorkspace.getOpenFolder() != null) {
                    mWorkspace.getOpenFolder().beginExternalDrag((ShortcutInfo) item.getTag());
                }
                startDrag(item);
            }
        }

        return true;
    }

    public void createFolder() {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = "";// mLauncher.getText(R.string.folder_name);
        LauncherModel.addItemToDatabase(mLauncher, folderInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP, -1, 1, 1, false);

        CellLayout cl = mWorkspace.getCurrentDropLayout();
        mFolderIcon = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
                cl, folderInfo, mIconCache);
        mFolderIcon.setOnLongClickListener(XDockView.this);
        mFolderIcon.setVisibility(VISIBLE);
        mFolderIcon.setPivotX(0);
        mFolderIcon.setPivotY(0);
        mFolderIcon.setScaleX(mLauncher.getItemScale());
        mFolderIcon.setScaleY(mLauncher.getItemScale());
        mFolderIcon.setTextVisible(false);
        mFolderIcon.isStackFolderIcon = true;

        mContainer.addView(mFolderIcon);
    }

    public void doStack() {
        if (isStackMode() || isFolderMode()) {
            mState = State.NORMAL;
            mLayout.setTag(null);
            // remove folder icon view before animate
            if (mFolderIcon != null) {
                removeDockViewFolder();
            }
            mLayout.animateStack(false, new Runnable() {
                public void run() {
//				    updateSelectCount();
                }
            });
        } else {
            // Collect.
            if (mLayout.getChildCount() <= 1) {
                return;
            }

            setAnimationStatus(true);
            mLayout.animateStack(true, new Runnable() {
                public void run() {
                    mState = State.STACK_MODE;
                    setAnimationStatus(false);
                    createFolder();
                }
            });
        }
    }

    public void setFolderMode() {
        mState = State.FOLDER_MODE;
//		updateSelectCount();
        animateToFolderCount = 0;
    }


    // Get status.
    public boolean isStackMode() {
        return mState == State.STACK_MODE;
    }

    public boolean isNormalMode() {
        return mState == State.NORMAL;
    }

    public boolean isFolderMode() {
        return mState == State.FOLDER_MODE;
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {

    }

    public void removeDockViewFolder() {
        if (mFolderIcon != null) {
            mContainer.removeView(mFolderIcon);
            mFolderIcon.getFolder().deleteFolderFormDockView();
        }
    }

    public void animDragviewIntoPosition(final DragView dragView,
                                         ItemInfo dragInfo, Rect r) {
        final View item = findItemByInfo(dragInfo);

        if (item == null) {
            return;
        }

        // Rect r = new Rect();
        // mLauncher.getDragLayer().getViewRectRelativeToSelf(dragView, r);
        final int fromX = r.left;
        final int fromY = r.top;
        final int[] sourcePos = new int[]{fromX, fromY};

        final View mSnapshot = mLauncher.createSnapshot(dragView);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        mSnapshot.setLayoutParams(lp);
        mSnapshot.setVisibility(View.INVISIBLE);
        float scale = mLauncher.getItemScale();
        mSnapshot.setPivotX(0);
        mSnapshot.setPivotY(0);
        mSnapshot.setScaleX(scale);
        mSnapshot.setScaleY(scale);
        mLauncher.getDragLayer().addView(mSnapshot);

        final int[] targetPos = mLauncher.getLocationPosition(item);

        ValueAnimator mAnimator = mAnim.startDragViewAnim(mSnapshot, sourcePos, dragView,
                targetPos, item, false);
        mAllAnimator.add(mAnimator);

    }

    public void animDragviewIntoFolderIcon(final ShortcutInfo item,
                                           final DragView view, final Rect from, final Rect to,
                                           final float finalAlpha, final float initScaleX,
                                           final float initScaleY, final float finalScaleX,
                                           final float finalScaleY, final int duration,
                                           final Interpolator motionInterpolator,
                                           final Interpolator alphaInterpolator,
                                           final Runnable onCompleteRunnable, final int animationEndStyle,
                                           final View anchorView, final FolderIcon folderIcon) {
        ValueAnimator mAnimator = mAnim.startDragViewToFolderIconAnim(item,
                view, from, to, finalAlpha, initScaleX, initScaleY,
                finalScaleX, finalScaleY, duration, motionInterpolator,
                alphaInterpolator, onCompleteRunnable, animationEndStyle,
                anchorView, folderIcon);
        mAllAnimator.add(mAnimator);

    }

    public View dropToCurrentScreen(final DragView view, ItemInfo info, int[] dropTargetCell) {
        if (view == null) {
            return null;
        }

        ViewGroup parentView;
        if (mWorkspace.getOpenFolder() == null) {
            isOpenFolder = false;
            parentView = mWorkspace.getCurrentDropLayout();
        } else {
            isOpenFolder = true;
            parentView = mWorkspace.getOpenFolder();
            // check folder items count
            if (((Folder) parentView).isFull()) {
                return null;
            }
        }

        int spanX = 1;
        int spanY = 1;
        if (isOpenFolder) {
        } else {
            if (parentView == null) {
                return null;
            }
            int[] targetCell = dropTargetCell;
            if (targetCell == null || targetCell[0] < 0 || targetCell[1] < 0) {
                LauncherLog.e(TAG, "showOutOfSpaceMessage");
                return null;
            }

            info.screenId = mWorkspace.getIdForScreen((CellLayout) parentView);
            info.cellX = targetCell[0];
            info.cellY = targetCell[1];
        }

        View targetItem = null;

        if (isOpenFolder) {
            ((Folder) parentView).getInfo().add((ShortcutInfo) info);
            targetItem = ((Folder) parentView)
                    .getViewForInfo((ShortcutInfo) info);
        } else {
            if (info instanceof ShortcutInfo) {
                targetItem = mLauncher.createShortcut(R.layout.application,
                        mWorkspace.getCurrentDropLayout(),
                        (ShortcutInfo) info);
                final long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                mWorkspace.addDockViewItemToWorkspace(targetItem, container,
                        info.screenId, info.cellX, info.cellY, spanX, spanY);
            } else {
                targetItem = null;
            }
        }
        return targetItem;
    }

    public void animDropToCurrentScreen(View view, View targetItem, Rect r) {
        if (targetItem == null) {
            return;
        }
        targetItem.setVisibility(INVISIBLE);
        final int fromX = r.left;
        final int fromY = r.top;
        final int[] sourcePos = new int[]{fromX, fromY};

        // create a target snapshot to calculate correct target location
        // final View mSnapshot = mLauncher.createSnapshot(R.layout.application,
        // mWorkspace.getCurrentDropLayout(), (ShortcutInfo) info);
        final View mSnapshot = mLauncher.createSnapshot(view);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        mSnapshot.setLayoutParams(lp);
        mSnapshot.setVisibility(View.INVISIBLE);
        float scale = mLauncher.getItemScale();
        mSnapshot.setPivotX(0);
        mSnapshot.setPivotY(0);
        mSnapshot.setScaleX(scale);
        mSnapshot.setScaleY(scale);
        mLauncher.getDragLayer().addView(mSnapshot);

        int[] targetPos;
        boolean isToFolderCenter = false;

        if (mWorkspace.getOpenFolder() != null) {
            targetPos = mLauncher.getFolderTargetPosition(mWorkspace.getOpenFolder(), targetItem);
            if (targetPos[0] == 0 || targetPos[1] == 0) {
                targetPos = mLauncher.getFolderCenter(
                        mWorkspace.getOpenFolder());
                isToFolderCenter = true;
            }
			// Folder.blockViewShow(targetItem);
        } else {
            targetPos = mLauncher.getWorkspaceTargetPosition(targetItem);
        }
        ValueAnimator mAnimator = mAnim.startDragViewAnim(mSnapshot, sourcePos, view, targetPos,
                targetItem, isToFolderCenter);
        mAllAnimator.add(mAnimator);
    }

    public int getDisplayWidth() {
        WindowManager wm = (WindowManager) mLauncher.getSystemService(Context.WINDOW_SERVICE);
        Display dl = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        dl.getMetrics(metrics);

        return metrics.widthPixels;
    }

    public boolean needAdjustLayout() {
        return isNeedAdjustLayout;
    }

    public void setAdjustStatus(boolean action) {
        isNeedAdjustLayout = action;
    }

    public void hide() {
        if (LauncherLog.DEBUG_EDITMODE) {
            LauncherLog.d(TAG, "hide");
        }

        if (mLayout.getChildCount() <= 1 && mAllInfos.size() <= 1) {
            if (!isNormalMode()) {
                mState = State.NORMAL;
                mLayout.setTag(null);
                // remove folder icon view before animate
                if (mFolderIcon != null) {
                    removeDockViewFolder();
                }
            }
        }
        int folderItemSize = 0;
        if (mFolderIcon != null) {
            folderItemSize = mFolderIcon.getFolderInfo().getCount();
        }
        if (mLayout.getChildCount() == 0 && mAllInfos.size() == 0
				&& folderItemSize == 0) {
			mLauncher.dismissDockView();
        }
    }

    public void startAnimatorSet(final boolean isNeedAdjust,
                                 final boolean isToStackFolder) {
        isAnimation = true;
        isAddLayoutMode = false;
		tmpFolderIcon = null;
        isNeedAdjustLayout = isNeedAdjust;
        mAnim.animAllAnimator(mAllAnimator, ANIM_INTERVAL, isToStackFolder);
        // mAllAnimator.clear();
    }

    public float getItemScale() {
        return mLauncher.getItemScale();
    }

    public FolderIcon getFolderIcon() {
        return mFolderIcon;
    }

    void updateShortcuts(ArrayList<AppInfo> apps) {
        LauncherLog.d(TAG, "updateShortcuts");
        int childCount = mLayout.getChildCount();

        for (int j = 0; j < childCount; j++) {
            View view = mLayout.getChildItemAt(j);
            Object tag = view.getTag();

            if (LauncherModel.isShortcutInfoUpdateable((ItemInfo) tag)) {
                if (tag instanceof ShortcutInfo) {
                    ShortcutInfo info = (ShortcutInfo) tag;

                    final Intent intent = info.intent;
                    final ComponentName name = intent.getComponent();
                    final int appCount = apps.size();
                    for (int k = 0; k < appCount; k++) {
                        AppInfo app = apps.get(k);
                        if (app.componentName.equals(name)) {
                            LauncherLog.d("XDockView", "updateShortcuts, applyFromShortcutInfo");
                            info.updateIcon(mIconCache);
                            info.title = app.title.toString();
                            isNeedAdjustLayout = true;

                            if (view instanceof BubbleTextView) {
                                BubbleTextView shortcut = (BubbleTextView) view;
                                shortcut.applyFromShortcutInfo(info, mIconCache, false);
                                shortcut.setTextVisibility(false);
                            }
                        }
                    }
                }
            }
        } // End for
    }

    // Removes ALL items that match a given package name, this is usually called
    // when a package
    // has been removed and we want to remove all components (widgets,
    // shortcuts, apps) that
    // belong to that package.
    public void removeItemsByPackageName(final ArrayList<String> packages) {
        final HashSet<String> packageNames = new HashSet<>();
        packageNames.addAll(packages);

        // Filter out all the ItemInfos that this is going to affect
        final HashSet<ItemInfo> infos = new HashSet<>();
        final HashSet<ComponentName> cns = new HashSet<>();
        int childCount = mLayout.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View view = mLayout.getChildItemAt(i);
            infos.add((ItemInfo) view.getTag());
        }
        FolderInfo folder;
        if (mFolderIcon != null) {
            folder = mFolderIcon.getFolderInfo();
            infos.add(folder);
        }
        LauncherModel.ItemInfoFilter filter = new LauncherModel.ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info,
                                      ComponentName cn) {
                if (packageNames.contains(cn.getPackageName())) {
                    LauncherLog.d(TAG, "removeItemsByPackageName, packageNames: " + packageNames);
                    cns.add(cn);
                    return true;
                }
                return false;
            }
        };
        LauncherModel.filterItemInfos(infos, filter);

        // Remove the affected components
        removeItemsByComponentName(cns);
    }

    // Removes items that match the application info specified, when
    // applications are removed
    // as a part of an update, this is called to ensure that other widgets and
    // application
    // shortcuts are not removed.
    public void removeItemsByApplicationInfo(final ArrayList<AppInfo> appInfos) {
        for (AppInfo info : appInfos) {
            LauncherLog.d("XDockView", "removeItemsByApplicationInfo info: " + info);
        }
        // Just create a hash table of all the specific components that this
        // will affect
        HashSet<ComponentName> cns = new HashSet<ComponentName>();
        for (AppInfo info : appInfos) {
            cns.add(info.componentName);
        }

        // Remove all the things
        removeItemsByComponentName(cns);
    }

    public void removeItemsByComponentName(final HashSet<ComponentName> componentNames) {

        for (ComponentName name : componentNames) {
            LauncherLog.d("XDockView", "ComponentName name: " + name);
        }

        final HashMap<ItemInfo, View> children = new HashMap<ItemInfo, View>();
        if (mLayout != null) {
            for (int j = 0; j < mLayout.getChildCount(); j++) {
                final View view = mLayout.getChildItemAt(j);
                if (view != null) {
                    children.put((ItemInfo) view.getTag(), view);
                }
            }
        }

        final ArrayList<ItemInfo> appsToRemove = new ArrayList<ItemInfo>();
        FolderInfo folder = null;
        if (mFolderIcon != null) {
            folder = mFolderIcon.getFolderInfo();
            children.put(folder, this);
        }

        final ArrayList<View> childrenToRemove = new ArrayList<View>();
        LauncherModel.ItemInfoFilter filter = new LauncherModel.ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info,
                                      ComponentName cn) {
                if (parent instanceof FolderInfo) {
                    if (componentNames.contains(cn)) {
                        LauncherLog.d(TAG, "appsToRemove, cn: " + cn);
                        appsToRemove.add(info);
                        return true;
                    }
                } else {
                    if (componentNames.contains(cn)) {
                        childrenToRemove.add(children.get(info));
                        return true;
                    }
                }
                return false;
            }
        };
        LauncherModel.filterItemInfos(children.keySet(), filter);

        // Remove all the apps from their folders
        for (ItemInfo info : appsToRemove) {
            LauncherLog.d(TAG, "folder.remove, info: " + info);
            removeFolderItemInfo(info);
            for (ShortcutInfo i : mAllInfos) {
                LauncherLog.d(TAG, "remove folder info: " + i);
            }
            if (folder != null && info instanceof ShortcutInfo) {
                folder.remove((ShortcutInfo) info);
            }
        }

        // Remove all the other children
        for (View child : childrenToRemove) {
            // Note: We can not remove the view directly from
            // CellLayoutChildren as this
            // does not re-mark the spaces as unoccupied.
            isNeedAdjustLayout = true;
            isRemoveByComponentName++;
            removeDockItem(child, true);
            if (child instanceof DropTarget && mDragController != null) {
                mDragController.removeDropTarget((DropTarget) child);
            }
        }

        if (folder != null && folder.contents.size() == 0) {
            if (isFolderMode()) {
                freeStackFolderIcon();
            }
            hide();
        }
    }

    private int mAnimationFlag = 0;

    public void setAnimationStatus(boolean anim) {
        if (anim) {
            mAnimationFlag++;
        } else {
            mAnimationFlag--;
            if (mAnimationFlag < 0) {
                mAnimationFlag = 0;
            }
        }

        if (mAnimationFlag > 0) {
            isAnimation = true;
        } else {
            isAnimation = false;
        }
    }

    public boolean getAnimationStatus() {
        return isAnimation;
    }

    public void endAnimatorSet() {
        mAnim.endAnimatorSet();
    }

    public void setAnimStacking(boolean anim) {
        mIsStacking = anim;
    }

    public boolean isAnimStacking() {
        return mIsStacking;
    }

    private int animateToFolderCount = 0;

    public XDockViewLayout getLayout() {
        return mLayout;
    }


    public void setDragTargerStatus(boolean isToFolderIcon) {
        isToFolderIconByDrag = isToFolderIcon;
    }

    public void setBackByDrag() {
        isBackByDrag = true;
    }

    public boolean isToStackFolderIcon() {
        return isToFolderIcon;
    }

    public void showFullScreenStatus() {
        boolean toFolderIcon = isToFolderIconByDrag;
        isToFolderIconByDrag = false;

        if (isBackByDrag) {
            isBackByDrag = false;
			/* add by huangyn1 for LAUNCHERHD-727 20140701 start */
            if (isFromIndicator) {
                isFromIndicator = false;
                return;
            }
			/* add by huangyn1 for LAUNCHERHD-727 20140701 end */
            if (mWorkspace.getOpenFolder() == null) {
                if (toFolderIcon) {
                    showOutOfFolderMessage();
                } else {
                    mLauncher.showOutOfSpaceMessage(false);
                }
            } else {
                if (mWorkspace.getOpenFolder().isFull()) {
                    showOutOfFolderMessage();
                }
            }
            return;
        }
        if (isStackMode() && mWorkspace.isInOverviewMode()
                && mWorkspace.getOpenFolder() == null) {
            CellLayout view = mWorkspace.getCurrentDropLayout();
            if (view == null) {
                return;
            }
            int[] targetCell = new int[]{-1, -1};
            view.findCellForSpan(targetCell, 1, 1);
            if (targetCell == null || targetCell[0] < 0 || targetCell[1] < 0) {
                if (mLayout != null && mLayout.getChildCount() != 0) {
                    mLauncher.showOutOfSpaceMessage(false);
                }
            }
        } else if (isStackMode() && mWorkspace.isInOverviewMode()
                && mWorkspace.getOpenFolder() != null) {
            if (mWorkspace.getOpenFolder().isFull()) {
                if (mLayout != null && mLayout.getChildCount() != 0) {
                    showOutOfFolderMessage();
                }
            }
        }

        if (isNormalMode() && mWorkspace.isInOverviewMode() && mWorkspace.getOpenFolder() != null) {
            if (mWorkspace.getOpenFolder().isFull()) {
                if (mLayout != null && mLayout.getChildCount() != 0) {
                    showOutOfFolderMessage();
                }
            }
        }
    }

    /**
     * Check the select panel is full.
     *
     * @return ture: the select panel is full; else return false.
     */
    public boolean isDockViewFull() {
        if (isFolderMode()) {
            return mAllInfos.size() + animateToFolderCount >= MAX_COUNT;
        } else {
            return mLayout.getChildCount() >= MAX_COUNT;
        }
    }


    public void showOutOfDockViewMessage() {
        if (mShowToast == null) {
            mShowToast = new ShowToast(R.string.dockview_out_of_space);
        } else {
            mShowToast.setMessageId(R.string.dockview_out_of_space);
        }
        post(mShowToast);
    }

    public void showOutOfFolderMessage() {
        if (mShowToast == null) {
            mShowToast = new ShowToast(R.string.folder_out_of_space);
        } else {
            mShowToast.setMessageId(R.string.folder_out_of_space);
        }
        post(mShowToast);
    }

    public void showFolderAccept(FolderRingAnimator fra) {
        mFolderOuterRings.add(fra);
    }

    public void hideFolderAccept(FolderRingAnimator fra) {
        if (mFolderOuterRings.contains(fra)) {
            mFolderOuterRings.remove(fra);
        }
        invalidate();
    }

    public boolean contains(FolderRingAnimator fra) {
        return mFolderOuterRings.contains(fra);
    }

    public void setIsAddLayoutMode(boolean mode) {
        isAddLayoutMode = mode;
    }

    public boolean getIsAddLayoutMode() {
        return isAddLayoutMode;
    }

    interface SelectCountListener {
        void onSelectCountChanged(int count);
    }

    private SelectCountListener mSelectCountListener;

    public void setSelectCountListener(SelectCountListener listener) {
        mSelectCountListener = listener;
    }

    public void updateSelectCount() {
        if (LauncherLog.DEBUG_EDITMODE) {
            LauncherLog.d(TAG, "updateSelectCount mState: " + mState);
        }

        if (mSelectCountListener != null) {
            switch (mState) {
                case NORMAL:
                case STACK_MODE:
                    int count = mLayout.getChildCount();
                    if (mFolderIcon != null) {
                        count = mFolderIcon.getFolderInfo().contents.size();
                        if (LauncherLog.DEBUG_EDITMODE) {
                            LauncherLog.d(TAG, "updateSelectCount count: " + count);
                        }
                        if (count == 0) {
                            count = mLayout.getChildCount();
                        }
                    }
                    mSelectCountListener.onSelectCountChanged(count);
                    break;

                case FOLDER_MODE:
                    mSelectCountListener.onSelectCountChanged(mAllInfos.size());
                    break;
            }
        }
    }

    public void addDeleteItem(ShortcutInfo info) {
        View item = addDragItemInfo(info, mTempCell[0]);
        if (item != null) {
            item.setVisibility(View.VISIBLE);
            if (!mLauncher.isDockViewShowing()) {
                mLauncher.showDockView();
            }
        }
    }

    public void visibleDeleteItem(ShortcutInfo info) {
        final View item = findItemByInfo(info);
        item.setVisibility(View.VISIBLE);
    }

    private void showToast(int stringId) {
        if (mShowToast == null) {
            mShowToast = new ShowToast(stringId);
        } else {
            mShowToast.setMessageId(stringId);
        }
        post(mShowToast);
    }

}
