package com.android.launcher3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.klauncher.launcher.R;

import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ViewSwitcher;

interface OnWallpaperListClickListener{
    void onClick(View v);
}

class WallpaperListItemInfo {
    String Tag = "";
    String Path = "";
    WallpaperManager WallpaperManager = null;
    String WallpaperPackage = "";
    int ResId = -1;

    WallpaperListItemInfo(String t) {
        Tag = t;
    }

    WallpaperListItemInfo(String t, String p, WallpaperManager manager, String mWallpaperPackage, int ID) {
        Tag = t;
        Path = p;
        WallpaperManager = manager;
        WallpaperPackage = mWallpaperPackage;
        ResId = ID;
    }
}

class WallpaperListOtherItemInfo {
    String AppName = "";
    String PackageName = "";
    String ClassName = "";

    WallpaperListOtherItemInfo(String appName,String packageName, String className) {
        AppName = appName;
        PackageName = packageName;
        ClassName = className;
    }
}

public class WallpaperListViewSwitcher extends ViewSwitcher implements IEditEntry {

    WallpaperListHorizontalRecyclerView mFirstListRecyclerView = null;
    WallpaperListHorizontalRecyclerView mSecondListRecyclerView = null;
    private Context mContext = null;
    private static final String THEME_CENTER_PACKAGE = "com.lenovo.themecenter";
    private static final String THEME_CENTER_PACKAGE_MAIN = "com.lenovo.themecenter.main";
    private static final String THEME_CENTER_PACKAGE_LOAD = "com.lenovo.themecenter.LoadingActivity";
    private static final String INVOKE = "invoke_external";
    public static final String ACTION_LETHEME_LAUNCH = "com.lenovo.launcher.action.THEME_SETTING";
    private Launcher mLauncher;

    // Add by lenovo-sw-nj luyy1 20151019
    private MenuController mController;

    public void setmController(MenuController mController) {
        this.mController = mController;
    }

    // mIsClickApk is for judging whether clicking APK image.
    // If so, we no longer handle it in LauncherModel.onReceive.
    private boolean mIsClickApk = false;

    public boolean ismIsClickApk() {
        return mIsClickApk;
    }

    public void setmIsClickApk(boolean clickApk) {
        this.mIsClickApk = clickApk;
    }

    private ShowType mCurrentType = ShowType.FIRST_PRELOAD;
    enum ShowType {
        FIRST_PRELOAD,
        SECOND_OTHERS
    }

    public WallpaperListViewSwitcher(Context context) {
        super(context);
        setup2ListViews(context);
    }

    public WallpaperListViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup2ListViews(context);
    }

    OnWallpaperListClickListener mClickListener = new OnWallpaperListClickListener() {
        @Override
        public void onClick(View v) {
            Object object = v.getTag();
            if(object instanceof WallpaperListItemInfo) {
                WallpaperListItemInfo info = (WallpaperListItemInfo)object;
                if (info.Tag.equals(mFirstListRecyclerView.SHOW_THEME_CENTER_TAG)) {
                    showThemeCenter();
                } else if (info.Tag.equals(mFirstListRecyclerView.SHOW_OTHERS_TAG)) {
                    showOtherApp();
                } else if (info.WallpaperManager != null &&
                    info.Tag.equals(mFirstListRecyclerView.SHOW_APK_WALLPAPER_TAG)) {
                    setApkWallpaper(info.WallpaperManager, info.WallpaperPackage, info.ResId, info.Path);
                } else if (info.WallpaperManager != null &&
                    info.Tag.equals(mFirstListRecyclerView.SHOW_FILE_WALLPAPER_TAG)) {
                    setFileWallpaper(info.Path, info.WallpaperManager);
                }
            } else if (object instanceof WallpaperListOtherItemInfo) {
                WallpaperListOtherItemInfo info = (WallpaperListOtherItemInfo)object;
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(info.PackageName, info.ClassName));
              if (ACTION_LETHEME_LAUNCH.equals(intent.getAction())) {
                  intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
              }
              try {
                  mContext.startActivity(intent);
              } catch (ActivityNotFoundException e) {
                  e.printStackTrace();
              } catch (SecurityException e) {
                  e.printStackTrace();
              }
            }
        }
    };

    public void setup(Launcher launcher) {
        mLauncher = launcher;
    }

    private void showThemeCenter() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (getCurentVersionName(mContext,THEME_CENTER_PACKAGE)) {
            intent.setAction(THEME_CENTER_PACKAGE_MAIN);
            intent.putExtra(INVOKE, true);
        } else {
            intent.setComponent(new ComponentName(THEME_CENTER_PACKAGE,THEME_CENTER_PACKAGE_LOAD));
        }

        if (ACTION_LETHEME_LAUNCH.equals(intent.getAction())) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Exit Overview Mode after entering theme center. Add by lenovo-sw-nj luyy1 20151015.
        if (mController != null) {
            mController.onBackPressed();
        }
        mLauncher.setFullScreen(false);
        if (mLauncher.getWorkspace().isInOverviewMode()) {
            mLauncher.getWorkspace().exitOverviewMode(true);
        }
    }

    private void showOtherApp() {
        mCurrentType = ShowType.SECOND_OTHERS;
        mSecondListRecyclerView.updateSecondList(mClickListener);
        mSecondListRecyclerView.scrollToPosition(0);
        this.showNext();
    }

    private void setFileWallpaper(String path, WallpaperManager manager) {
        try {
            FileInputStream fis = new FileInputStream(path);
            WallpaperCropActivity.resetSuggestWallpaperDimension(manager);
            manager.setStream(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // We need to mark this current wallpaper. Since the current wallpaper is shown,
        // we do not show it twice. Add by Lenovo-sw luyy1.
        reloadItemAndUpdateEditPoint(false, path);
    }

    private void reloadItemAndUpdateEditPoint(boolean isCurrentShown, String path) {
        mFirstListRecyclerView.initFirstLayerDataForAdapter(isCurrentShown);
        mFirstListRecyclerView.updateEditPoint(path, isCurrentShown);
    }

    public void reloadItemAndUpdateEditPoint(boolean isCurrentShown, int id) {
        mFirstListRecyclerView.initFirstLayerDataForAdapter(isCurrentShown);
        mFirstListRecyclerView.updateEditPoint(id, isCurrentShown);
    }

    private void setApkWallpaper(WallpaperManager manager, String WallpaperPackage, int mResId, String path) {
        try {
            Resources res = getContext().getPackageManager()
                    .getResourcesForApplication(WallpaperPackage);
            InputStream is = res.openRawResource(mResId);
            WallpaperCropActivity.resetSuggestWallpaperDimension(manager);
            manager.setStream(is);
            is.close();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // We need to mark this current wallpaper. Since the current wallpaper is shown,
        // we do not show it twice. Add by Lenovo-sw luyy1.
        mIsClickApk = true;
        reloadItemAndUpdateEditPoint(false, mResId);
    }

    private boolean getCurentVersionName(Context context,String packageName) {
        final PackageManager manager = context.getPackageManager();
        try { PackageInfo info = manager.getPackageInfo(packageName, 0);
            String appVersion = info.versionName;
            float versionnum = Float.valueOf(appVersion.substring(0,3));
            if(versionnum < 1.0) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private void setup2ListViews(Context context) {
        mContext = context;
        mFirstListRecyclerView = (WallpaperListHorizontalRecyclerView) this.findViewById(R.id.preload_wallpaper_list_first);
        mSecondListRecyclerView = (WallpaperListHorizontalRecyclerView) this.findViewById(R.id.other_wallpaper_list_second);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        update();
    }

    public void update() {
        if(mFirstListRecyclerView == null) {
           setup2ListViews(mContext);
        }
        if (mFirstListRecyclerView != null) {
            mFirstListRecyclerView.updateFirstList(mClickListener);
        }
    }

    @Override
    public boolean onBackPressed() {
        // If current wallpaper is live, then it will not send broadcast. Hence, we shell
        // reload the wallpaper list manually. Add by Lenovo-sw luyy1.
        if (mFirstListRecyclerView.isCurrentWallpaperLive()) {
            reloadItemAndUpdateEditPoint(true, LauncherModel.WALLPAPER_OTHER_SOURCE);
        }
        if(mCurrentType == ShowType.FIRST_PRELOAD) {
            return false;
        }
        showNextType();
        return true;
    }

    @Override
    public boolean onHomePressed() {
        // If current wallpaper is live, then it will not send broadcast. Hence, we shell
        // reload the wallpaper list manually. Add by Lenovo-sw luyy1.
        if (mFirstListRecyclerView.isCurrentWallpaperLive()) {
            reloadItemAndUpdateEditPoint(true, LauncherModel.WALLPAPER_OTHER_SOURCE);
        }
        if(mCurrentType == ShowType.FIRST_PRELOAD) {
            return false;
        }
        showNextType();
        return false;
    }

    private void showNextType() {
        this.showNext();
        if(mCurrentType == ShowType.FIRST_PRELOAD) {
            mCurrentType = ShowType.SECOND_OTHERS;
        } else {
            mCurrentType = ShowType.FIRST_PRELOAD;
        }
    }

    /* Lenovo-sw luyy1 add to fix LANCHROW-186 2015-07-01*/
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE && mFirstListRecyclerView != null) {
            mFirstListRecyclerView.scrollToPosition(0);
        }
    }
}
