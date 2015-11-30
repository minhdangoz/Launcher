package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;

import com.lenovo.launcher.ext.LauncherLog;

import java.util.ArrayList;

/**
 * Created by LEI on 2015/6/9.
 * Animation for dock view
 */
public class XDockViewAnimUtil {
    private static final String TAG = "XDockViewAnimUtil";

    private static final int TIME = 400;
    private Launcher mLauncher;
    private XDockView mDockView;
    AnimatorSet bouncer = null;
    DeviceProfile grid = null;

    public enum AlphaStatus {NONE, TO_FOLDER_CENTER, FOLDER_TO_WORKSPACE}

    public XDockViewAnimUtil(Launcher launcher) {
        mLauncher = launcher;
        mDockView = mLauncher.getDockView();
        grid = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
    }


    public void endAnimatorSet() {
        if (bouncer != null) {
            bouncer.cancel();
        }
    }

    public void animAllAnimator(final ArrayList<ValueAnimator> mAnimator, long interval,
                                final boolean isToStackFolder) {
        int count = mAnimator.size();
        bouncer = new AnimatorSet();
        for (int i = 0; i < count; i++) {
            ValueAnimator anim = mAnimator.get(i);
            int timeDelay = 0;

            if (mDockView.getIsAddLayoutMode()) {
                timeDelay = TIME;
            }
            bouncer.play(anim).after(i * interval + timeDelay);
        }

        bouncer.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (LauncherLog.DEBUG_EDITMODE) {
                    LauncherLog.d(TAG, "bouncer start");
                }
                mDockView.setAnimationStatus(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (LauncherLog.DEBUG_EDITMODE) {
                    LauncherLog.d(TAG, "bouncer end");
                }
                mDockView.setAnimationStatus(false);
                mDockView.showFullScreenStatus();
                mDockView.clickDeleteIsCompleted = true;
                mDockView.updateSelectCount();
                if (isToStackFolder) {
                    mDockView.setFolderMode();
                }
                mAnimator.clear();
                mDockView.setIsAddLayoutMode(false);
                bouncer = null;

                if (!mLauncher.getWorkspace().isInOverviewMode()) {
                    int count = mLauncher.getWorkspace().getPageCount();
                    for (int i = 0; i < count; i++) {
                        CellLayout cell = (CellLayout) mLauncher.getWorkspace().getPageAt(i);
                        if (cell != null) {
                            // cell.setNeedUpdateDrawCache(true);
                        }
                    }
                    mLauncher.getWorkspace().onLauncherTransitionEnd(mLauncher, false, true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

                //add by zhangxh14.
                //reason: ValueAnimator will call end if call AnimatorSet.end on Android 4.2.2 but not on Android 4.0.4
                int mCount = mAnimator.size();
                for (int i = 0; i < mCount; i++) {
                    ValueAnimator anim = mAnimator.get(i);
                    if (anim.isStarted()) {
                        anim.end();
                    } else {
                        anim.start();
                        anim.end();
                    }
                }
                //add end
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        bouncer.start();
    }

    public ValueAnimator startToWorkSpaceAnim(final int[] sourcePos,
                                              final View sourceView, final int[] targetPos,
                                              final View targetView, final View snapshot,
                                              final AlphaStatus status) {

        final int sourceX = sourcePos[0];
        final int sourceY = sourcePos[1];
        final int deltaX = targetPos[0] - sourcePos[0];
        final int deltaY = targetPos[1] - sourcePos[1];

        final ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(TIME);
        anim.setInterpolator(new DecelerateInterpolator(1.5f));
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (snapshot != null) {
                    snapshot.setVisibility(View.VISIBLE);
                }

                if (sourceView != null && mDockView != null) {
                    mDockView.removeDockItem(sourceView, true);
                }

/*                if(!grid.isLandscape && sourcePos[0] > grid.widthPx){
                    mLauncher.dismissDockView();
                }else if(grid.isLandscape && sourcePos[1] > grid.heightPx){
                    mLauncher.dismissDockView();
                }*/
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                targetView.setVisibility(View.VISIBLE);
                mLauncher.getDragLayer().removeView(snapshot);

                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    ScaleAnimation scaleAnmation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f
                            , Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnmation.setFillAfter(true);
                    targetView.startAnimation(scaleAnmation);
                }

                mLauncher.getDragLayer().invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation
                        .getAnimatedValue();
                snapshot.setX(sourceX + value * deltaX);
                snapshot.setY(sourceY + value * deltaY);

//                if (status == AlphaStatus.TO_FOLDER_CENTER) {
//                    snapshot.setAlpha(1 - value);
//                } else if (status == AlphaStatus.FOLDER_TO_WORKSPACE) {
//                    snapshot.setAlpha(value);
//                }
                mLauncher.getDragLayer().invalidate();
            }
        });

        return anim;
    }

    public ValueAnimator startToFolderIconAnim(final int[] sourcePos, final View sourceView,
                                               final int[] targetPos, final ItemInfo souceInfo,
                                               final View folderIcon, final View snapshot, final AlphaStatus status) {

        final int sourceX = sourcePos[0];
        final int sourceY = sourcePos[1];
        final int deltaX = targetPos[0] - sourcePos[0];
        final int deltaY = targetPos[1] - sourcePos[1];

        final ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(TIME);
        anim.setInterpolator(new DecelerateInterpolator(1.5f));
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (snapshot != null) {
                    snapshot.setVisibility(View.VISIBLE);
                }
                // FIXME removeDockItem twice
                if (sourceView != null && mDockView != null) {
                    mDockView.removeDockItem(sourceView, true);
                }
                if (!grid.isLandscape && sourcePos[0] > grid.widthPx) {
                    mLauncher.dismissDockView();
                } else if (grid.isLandscape && sourcePos[1] > grid.heightPx) {
                    mLauncher.dismissDockView();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // FIXME removeDockItem twice
                if (sourceView != null && mDockView != null) {
                    mDockView.removeDockItem(sourceView, true);
                }
                mLauncher.getDragLayer().removeView(snapshot);
                ((FolderIcon) folderIcon).showItem((ShortcutInfo) souceInfo);
                mLauncher.getDragLayer().invalidate();

                // 动画结束时刷新一下，防止DrawPage不绘制
                mLauncher.getWorkspace().invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation
                        .getAnimatedValue();
                snapshot.setX(sourceX + value * deltaX);
                snapshot.setY(sourceY + value * deltaY);

                if (status == AlphaStatus.FOLDER_TO_WORKSPACE) {
                    snapshot.setAlpha(value);
                }
            }
        });
        return anim;
    }

    public ValueAnimator startDragViewAnim(final View snapshot, final int[] sourcePos, final View sourceView, final int[] targetPos,
                                           final View targetView, final boolean isToFolderCenter) {

        final int sourceX = sourcePos[0];
        final int sourceY = sourcePos[1];
        final int deltaX = targetPos[0] - sourcePos[0];
        final int deltaY = targetPos[1] - sourcePos[1];

        final ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(TIME);
        anim.setInterpolator(new DecelerateInterpolator(1.5f));
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLauncher.getDragLayer().removeView(sourceView);
                snapshot.setX(sourceX);
                snapshot.setY(sourceY);
                snapshot.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLauncher.getDragLayer().removeView(snapshot);
                // XFolder.unblockViewShow(targetView);
                targetView.setVisibility(View.VISIBLE);
                mLauncher.getDragLayer().invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation
                        .getAnimatedValue();
                snapshot.setX(sourceX + percent * deltaX);
                snapshot.setY(sourceY + percent * deltaY);
                if (isToFolderCenter) {
                    snapshot.setAlpha(1 - percent);
                }
                mLauncher.getDragLayer().invalidate();
            }
        });
        return anim;
        //    anim.start();
    }

    public ValueAnimator startDragViewToFolderIconAnim(final ShortcutInfo info, final DragView view, final Rect from,
                                                       final Rect to,
                                                       final float finalAlpha, final float initScaleX, final float initScaleY,
                                                       final float finalScaleX, final float finalScaleY, int duration,
                                                       final Interpolator motionInterpolator, final Interpolator alphaInterpolator,
                                                       final Runnable onCompleteRunnable, final int animationEndStyle, View anchorView, final FolderIcon folderIcon) {

        // Animate the view
        final float initAlpha = view.getAlpha();
        final float dropViewScale = view.getScaleX();
        view.cancelAnimation();
        view.resetLayoutParams();

        final ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(TIME);
        anim.setInterpolator(new DecelerateInterpolator(1.5f));
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDockView.removeDockItemByInfo(info, true);
                mLauncher.getDragLayer().removeView(view);
                mLauncher.getDragLayer().invalidate();
                folderIcon.showItem(info);
            }
        });
        ValueAnimator.AnimatorUpdateListener updateCb = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                final int width = view.getMeasuredWidth();
                final int height = view.getMeasuredHeight();

                float alphaPercent = alphaInterpolator == null ? percent :
                        alphaInterpolator.getInterpolation(percent);
                float motionPercent = motionInterpolator == null ? percent :
                        motionInterpolator.getInterpolation(percent);

                float initialScaleX = initScaleX * dropViewScale;
                float initialScaleY = initScaleY * dropViewScale;
                float scaleX = finalScaleX * percent + initialScaleX * (1 - percent);
                float scaleY = finalScaleY * percent + initialScaleY * (1 - percent);
                float alpha = finalAlpha * alphaPercent + initAlpha * (1 - alphaPercent);

                float fromLeft = from.left + (initialScaleX - 1f) * width / 2;
                float fromTop = from.top + (initialScaleY - 1f) * height / 2;

                int x = (int) (fromLeft + Math.round(((to.left - fromLeft) * motionPercent)));
                int y = (int) (fromTop + Math.round(((to.top - fromTop) * motionPercent)));

                int xPos = x - view.getScrollX();
                int yPos = y - view.getScrollY();

                view.setTranslationX(xPos);
                view.setTranslationY(yPos);
                view.setScaleX(scaleX);
                view.setScaleY(scaleY);
                view.setAlpha(alpha);
            }
        };
        anim.addUpdateListener(updateCb);
        return anim;
    }
}
