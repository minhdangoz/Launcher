/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.android.launcher3.ModeSwitchHelper.Mode;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat.PackageInstallInfo;
import com.android.launcher3.settings.SettingsValue;
import com.klauncher.launcher.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class LauncherAppState implements DeviceProfile.DeviceProfileCallbacks {
    private static final String TAG = "LauncherAppState";
    private static final String SHARED_PREFERENCES_KEY = "com.android.launcher3.prefs";

    private static final boolean DEBUG = false;

    private final AppFilter mAppFilter;
    private final BuildInfo mBuildInfo;
    private LauncherModel mModel;
    private IconCache mIconCache;
    private WidgetPreviewLoader.CacheDb mWidgetPreviewCacheDb;
    private boolean mIsScreenLarge;
    private float mScreenDensity;
    private int mLongPressTimeout = 300;
    private boolean mWallpaperChangedSinceLastCheck;

    private static WeakReference<LauncherProvider> sLauncherProvider;
    private static Context sContext;

    private static LauncherAppState INSTANCE;

    private DynamicGrid mDynamicGrid;

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherAppState();
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public Context getContext() {
        return sContext;
    }

    public static void setApplicationContext(Context context) {
        if (sContext != null) {
            Log.w(Launcher.TAG, "setApplicationContext called twice! old=" + sContext + " new=" + context);
        }
        sContext = context.getApplicationContext();
    }

    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }

        Log.v(Launcher.TAG, "LauncherAppState inited");

        if (sContext.getResources().getBoolean(R.bool.debug_memory_enabled)) {
            MemoryTracker.startTrackingMe(sContext, "L");
        }

        // set sIsScreenXLarge and mScreenDensity *before* creating icon cache
        mIsScreenLarge = isScreenLarge(sContext.getResources());
        mScreenDensity = sContext.getResources().getDisplayMetrics().density;

        recreateWidgetPreviewDb();
        mIconCache = new IconCache(sContext);

        mAppFilter = AppFilter.loadByName(sContext.getString(R.string.app_filter_class));
        mBuildInfo = BuildInfo.loadByName(sContext.getString(R.string.build_info_class));
        mModel = new LauncherModel(this, mIconCache, mAppFilter);
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(sContext);
        launcherApps.addOnAppsChangedCallback(mModel);

        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        sContext.registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED);
        sContext.registerReceiver(mModel, filter);

        filter = new IntentFilter();
        if (LauncherApplication.LAUNCHER_SHOW_UNREAD_NUMBER) {
            filter.addAction(LauncherModel.ACTION_UNREAD_CHANGED);
            sContext.registerReceiver(mModel, filter);
        }

        filter = new IntentFilter();
        filter.addAction(SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED);
        sContext.registerReceiver(mModel, filter);
        /*Lenovo-sw zhangyj19 add 2015/07/21 modify theme start*/
        filter = new IntentFilter();
        filter.addAction(LauncherModel.ACTION_THEME_CHANGED);
        sContext.registerReceiver(mModel, filter);
        /*Lenovo-sw zhangyj19 add 2015/07/21 modify theme end*/
        /*Lenovo-sw luyy1 add 20150729 for Yandex search start*/
        filter = new IntentFilter();
        filter.addAction(LauncherModel.ACTION_SEARCH_CHANGED);
        sContext.registerReceiver(mModel, filter);
        /*Lenovo-sw luyy1 add 20150729 for Yandex search end*/
        /*Lenovo-sw luyy1 add 20150819 for wallpaper listener start*/
        filter = new IntentFilter();
        filter.addAction(LauncherModel.ACTION_THEME_WALLPAPER_CHANGED);
        sContext.registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
        sContext.registerReceiver(mModel, filter);
        /*Lenovo-sw luyy1 add 20150819 for wallpaper listener end*/
        // Register for changes to the favorites
        ContentResolver resolver = sContext.getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);
        
        mCurrentMode = isDisableAllApps() ? Mode.VIBEUI : Mode.ANDROID;
    }

    public void recreateWidgetPreviewDb() {
        if (mWidgetPreviewCacheDb != null) {
            mWidgetPreviewCacheDb.close();
        }
        mWidgetPreviewCacheDb = new WidgetPreviewLoader.CacheDb(sContext);
    }

    /**
     * Call from Application.onTerminate(), which is not guaranteed to ever be called.
     */
    public void onTerminate() {
        sContext.unregisterReceiver(mModel);
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(sContext);
        launcherApps.removeOnAppsChangedCallback(mModel);

        ContentResolver resolver = sContext.getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
    }

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // If the database has ever changed, then we really need to force a reload of the
            // workspace on the next load
            mModel.resetLoadedState(false, true);
            mModel.startLoaderFromBackground();
        }
    };

    LauncherModel setLauncher(Launcher launcher) {
        if (mModel == null) {
            throw new IllegalStateException("setLauncher() called before init()");
        }
        mModel.initialize(launcher);
        return mModel;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    boolean shouldShowAppOrWidgetProvider(ComponentName componentName) {
        return mAppFilter == null || mAppFilter.shouldShowApp(componentName);
    }

    WidgetPreviewLoader.CacheDb getWidgetPreviewCacheDb() {
        return mWidgetPreviewCacheDb;
    }

    static void setLauncherProvider(LauncherProvider provider) {
        sLauncherProvider = new WeakReference<LauncherProvider>(provider);
    }

    public static LauncherProvider getLauncherProvider() {
        return sLauncherProvider.get();
    }

    public static String getSharedPreferencesKey() {
        return SHARED_PREFERENCES_KEY;
    }

    DeviceProfile initDynamicGrid(Context context, int minWidth, int minHeight,
                                  int width, int height,
                                  int availableWidth, int availableHeight) {

        mDynamicGrid = new DynamicGrid(context,
                context.getResources(),
                minWidth, minHeight, width, height,
                availableWidth, availableHeight);
        mDynamicGrid.getDeviceProfile().addCallback(this);

        // Update the icon size
        DeviceProfile grid = mDynamicGrid.getDeviceProfile();
        grid.updateFromConfiguration(context, context.getResources(), width, height,
                availableWidth, availableHeight);
        return grid;
    }
    public DynamicGrid getDynamicGrid() {
        return mDynamicGrid;
    }

    public boolean isScreenLarge() {
        return mIsScreenLarge;
    }

    // Need a version that doesn't require an instance of LauncherAppState for the wallpaper picker
    public static boolean isScreenLarge(Resources res) {
        return res.getBoolean(R.bool.is_large_tablet);
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
    }

    public float getScreenDensity() {
        return mScreenDensity;
    }

    public int getLongPressTimeout() {
        return mLongPressTimeout;
    }

    public void onWallpaperChanged() {
        mWallpaperChangedSinceLastCheck = true;
    }

    public boolean hasWallpaperChangedSinceLastCheck() {
        boolean result = mWallpaperChangedSinceLastCheck;
        mWallpaperChangedSinceLastCheck = false;
        return result;
    }

    @Override
    public void onAvailableSizeChanged(DeviceProfile grid) {
        Utilities.setIconSize(grid.iconSizePx);
        String[] keywords = sContext.getResources().getStringArray(R.array.config_theme_load_icon_mask_padding);
        String mask_padding = "0";
        String default_padding = "0";
        for (String keyword : keywords) {
            String[] modelTheme = keyword.split(",");
            if (Build.MODEL.contains(modelTheme[0])) {
                mask_padding = modelTheme[1];
                break;
            } else if ("default".equals(modelTheme[0])) {
                default_padding = modelTheme[1];
            }
        }
        if (TextUtils.equals("0",mask_padding)) {
            mask_padding = default_padding;
        }
        com.klauncher.theme.Utilities.setIconSize(grid.iconSizePx,Integer.valueOf(mask_padding));
    }

    /* Lenovo zhaoxin5 20140902 add for single layer open or close START*/
    public static boolean isDisableAllApps() {
        Log.d("hw","isDisableAllApps : " + SettingsValue.isDisableAllApps(sContext));
    	return SettingsValue.isDisableAllApps(sContext);
    }



    public static boolean isDisableAllAppsHotseat() {
        String[] keywords = sContext.getResources().getStringArray(R.array.config_disable_hotseat_allapp);
        String disable = "";
        String default_value = "";
        for (String keyword : keywords) {
            String[] modelTheme = keyword.split(",");
            if (Build.MODEL.contains(modelTheme[0])) {
                disable = modelTheme[1];
                break;
            } else if ("default".equals(modelTheme[0])) {
                default_value = modelTheme[1];
            }
        }
        if (TextUtils.equals("",disable)) {
            disable = default_value;
        }
        Log.d("hw","isDisableAllAppsHotseat : " + disable);
        return Boolean.parseBoolean(disable);
    }

    private Mode mCurrentMode = Mode.VIBEUI;
    public int getCurrentLayoutInt(){
    	return mCurrentMode.ordinal();
    }
    
	public Mode getCurrentLayoutMode(){
		// IPHONE , 单层, 0 
		// ANDROID, 双层, 1
		return mCurrentMode;
	}
	
	public void setCurrentLayoutMode(Mode m) {
		mCurrentMode = m;
	}
	
	public String getCurrentLayoutString(){
		return mCurrentMode.name();
	}
	
	public boolean isCurrentAndroidMode() {
		return getCurrentLayoutMode() == Mode.ANDROID;
	}
	
	public boolean isCurrentVibeuiMode() {
		return getCurrentLayoutMode() == Mode.VIBEUI;
	}
    /* Lenovo zhaoxin5 20140902 add for single layer open or close END*/

    public static boolean isDogfoodBuild() {
        return getInstance().mBuildInfo.isDogfoodBuild();
    }

    public void setPackageState(ArrayList<PackageInstallInfo> installInfo) {
        mModel.setPackageState(installInfo);
    }

    /**
     * Updates the icons and label of all icons for the provided package name.
     */
    public void updatePackageBadge(String packageName) {
        mModel.updatePackageBadge(packageName);
    }
        
    // added by xutg1, reload the workspace forcibly
    public void reloadWorkspaceForcibly(int loadFlags) {
        mModel.resetLoadedState(false, true);
        mModel.startLoaderFromBackground(loadFlags);
    }
    // add end, xutg1, reload the workspace forcibly
    
    /** Lenovo-SW zhaoxin5 20150727 add partnet for phone START */
    public static boolean isTablet() {
    	return sContext.getResources().getBoolean(R.bool.is_tablet);
    }
    /** Lenovo-SW zhaoxin5 20150727 add partnet for phone END */
}
