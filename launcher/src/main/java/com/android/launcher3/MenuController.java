package com.android.launcher3;

import android.content.Intent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ViewSwitcher;

import com.android.launcher3.interpolator.BackInterpolator;
import com.android.launcher3.interpolator.CubicInterpolator;
import com.android.launcher3.settings.LauncherSettingActivity;
import com.klauncher.launcher.R;

/**
 * Created by yuanyl2 on 2015/5/30.
 * The MenuController to control the action of the item in the edit panel.
 */
public class MenuController {
    private Launcher mLauncher;
    private ViewSwitcher mMenu;
    private View mMenuPanel;
    private FrameLayout mFunctionPanel;
    private View mCurrentClickedEntry = null;
    private SearchDropTargetBar mToolTipBar = null;

    private static final int ANIM_TRANSLATE_DISTANCE = 250;
    private static final int ANIM_DURATION_EDITMODE_LATER_HALF = 260;
    private static Interpolator animInterpolatorEditmodeFirstHalf = null;
    private static Interpolator animInterpolatorEditmodeLaterHalf = null;
    private boolean isMenuInHideAnimation = false;

    public MenuController(Launcher launcher, View menu) {
        mLauncher = launcher;
        mMenu = (ViewSwitcher) menu;
    }

    // One time View setup
    public void initializeViews() {
        mMenuPanel = mMenu.findViewById(R.id.menu_panel);
        mFunctionPanel = (FrameLayout) mMenu.findViewById(R.id.function_panel);

        View widgetButton = mLauncher.findViewById(R.id.widget_button);
        widgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenuItem(mLauncher.getWidgetListPanel());
                mToolTipBar.updateToolTip(R.string.editmode_add_widget);
            }
        });
        widgetButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        View wallpaperButton = mLauncher.findViewById(R.id.wallpaper_button);
        wallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenuItem(mMenu.findViewById(R.id.wallpaper_list));
                mLauncher.getSearchBar().updateToolTip(R.string.editmode_set_wallpaper);
            }
        });
        wallpaperButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        View preferenceButton = mLauncher.findViewById(R.id.preference_button);
        preferenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLauncher.closeFolder();
                mLauncher.setFullScreen(false);
                if (mLauncher.getWorkspace().isInOverviewMode()) {
                    mLauncher.getWorkspace().exitOverviewMode(true);
                }
                /*Lenovo-sw zhangyj19 20150806 add setting function begin*/
                //mLauncher.showSettingsPanel();
                Intent intent = new Intent(mLauncher, LauncherSettingActivity.class);
                mLauncher.startActivity(intent);
                /*Lenovo-sw zhangyj19 20150806 add setting function end*/
            }
        });
        preferenceButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        View settingsButton = mLauncher.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mLauncher.getWorkspace().isSwitchingState()) {
                	mLauncher.setFullScreen(false);
                    mLauncher.startSettings();
                }
            }
        });
        settingsButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        animInterpolatorEditmodeFirstHalf = new CubicInterpolator(CubicInterpolator.OUT);
        animInterpolatorEditmodeLaterHalf = new BackInterpolator(BackInterpolator.OUT);
    }

    public void setToolTipBar(View toolTipBar) {
        mToolTipBar = (SearchDropTargetBar) toolTipBar;
    }

    private void showMenuItem(View view) {
        mCurrentClickedEntry = view;
        // Add by lenovo-sw-nj luyy1 20151019 start
        if (mCurrentClickedEntry instanceof WallpaperListViewSwitcher) {
            ((WallpaperListViewSwitcher) mCurrentClickedEntry).setmController(this);
        }
        // Add by lenovo-sw-nj luyy1 20151019 end

        // Switch function panel and current clicked view to visible.
        mFunctionPanel.setVisibility(View.VISIBLE);
        mCurrentClickedEntry.setVisibility(View.VISIBLE);
        mMenuPanel.setVisibility(View.GONE);
        if (mMenu.getNextView().getId() != R.id.menu_panel) {
            mMenu.showNext();
        }
    }

    private void hideMenuItem() {
        // Switch function panel and current clicked view to hidden.
        for (int i=0; i<mFunctionPanel.getChildCount(); i++) {
           mFunctionPanel.getChildAt(i).setVisibility(View.INVISIBLE);
        }
        mFunctionPanel.setVisibility(View.GONE);
        mMenuPanel.setVisibility(View.VISIBLE);
        // Switch to menu root.
        if (mMenu.getNextView().getId() == R.id.menu_panel) {
            mMenu.showNext();
        }
    }

    // Handle the back key pressed event.
    public boolean onBackPressed() {
        boolean ret;
        if (mCurrentClickedEntry != null) {
            ret = false;
            handleBackWhenItemShow();
        } else {
            ret = handleBackWhenItemNotShow();
        }

        return ret;
    }

    private void handleBackWhenItemShow() {
        boolean handled = true;
        if (mCurrentClickedEntry != null) {
            handled = ((IEditEntry) mCurrentClickedEntry).onBackPressed();
        }
        if (!handled) {
            mCurrentClickedEntry = null;
            hideMenuItem();
            mToolTipBar.updateToolTip(R.string.multi_selecting_tips);
        }
    }

    private boolean handleBackWhenItemNotShow() {
        if (mLauncher.shouldCloseFolderFirst()) {
            return false;
        }
        XDockView view = mLauncher.getDockView();
        if (mLauncher.isDockViewShowing() && view!= null) {
            view.flyBackAllItems();
            return false;
        }
        return true;
    }

    // Handle the home key pressed event.
    public void onHomePressed() {
        if (mCurrentClickedEntry != null) {
            ((IEditEntry) mCurrentClickedEntry).onHomePressed();
        }
        hideMenuItem();
    }

    public boolean canAcceptShortcut() {
        View dockView = mLauncher.getDockView();
        if (mMenuPanel.getVisibility() == View.VISIBLE || dockView.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    public void onDockViewShow() {
        if (mMenuPanel.getVisibility() == View.VISIBLE && !isMenuInHideAnimation) {
            menuAnimation(mMenu, 1, 0);
        }
    }

    public void onDockViewHide(boolean isNeedAnimation) {
        if (isNeedAnimation) {
            if (mLauncher.getWorkspace().isInOverviewMode()) {
                menuAnimation(mMenu, 0, 1);
            }
        } else {
            mMenu.setVisibility(View.VISIBLE);
            isMenuInHideAnimation = false;
        }
    }

    public void onDockViewHide() {
       onDockViewHide(true);
    }

    private void menuAnimation(final View view, final float fromAlpha, final float toAlpha) {
        menuAnimation(view, fromAlpha, toAlpha, null);
    }

    private void menuAnimation(final View view, final float fromAlpha, final float toAlpha,
                               final Runnable endCallback) {
        if (view == mMenu) {
            if (toAlpha == 0) {
                isMenuInHideAnimation = true;
            } else {
                isMenuInHideAnimation = false;
            }
        }

        float fromYDelta = toAlpha == 1 ? ANIM_TRANSLATE_DISTANCE : 0;
        float toYDelta = fromAlpha == 0 ? 0 : ANIM_TRANSLATE_DISTANCE;
        AnimationSet animSet = new AnimationSet(true);
        animSet.setDuration(ANIM_DURATION_EDITMODE_LATER_HALF);
        animSet.setInterpolator(animInterpolatorEditmodeFirstHalf);
        TranslateAnimation tranAnim = new TranslateAnimation(0, 0, fromYDelta,
                toYDelta);
        AlphaAnimation alphaAnim = new AlphaAnimation(fromAlpha, toAlpha);
        animSet.addAnimation(tranAnim);
        animSet.addAnimation(alphaAnim);
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (toAlpha == 1) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (toAlpha == 0) {
                    view.setVisibility(View.GONE);
                } else if (toAlpha == 1) {
                    view.setVisibility(View.VISIBLE);
                }
                if (view == mMenu) {
                    isMenuInHideAnimation = false;
                }
                if (endCallback != null) {
                    endCallback.run();
                }

            }
        });
        view.startAnimation(animSet);
    }
}
