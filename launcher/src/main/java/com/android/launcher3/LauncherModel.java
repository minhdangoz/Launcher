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

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.android.launcher3.IconCache.CacheEntry;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.PackageInstallerCompat.PackageInstallInfo;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.settings.SettingsProvider;
import com.klauncher.ext.LauncherLog;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.launcher.R;
import com.klauncher.utilities.LogUtil;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 * Launcher里面的数据，以及处理数据操作
 */
@SuppressLint("UseSparseArrays")
@SuppressWarnings("unused")
public class LauncherModel extends BroadcastReceiver
        implements LauncherAppsCompat.OnAppsChangedCallbackCompat {
    static final boolean DEBUG_LOADERS = true;
    private static final boolean DEBUG_RECEIVER = true;
    private static final boolean REMOVE_UNRESTORED_ICONS = true;

    static final String TAG = "Launcher.Model";
    static final String TAG_ITEMS_REMOVE = "Items.ToRemove";
    public static final String SETTINGS_PROTECTED_COMPONENTS = "protected_components";

    // true = use a "More Apps" folder for non-workspace apps on upgrade
    // false = strew non-workspace apps across the workspace on upgrade
    public static final boolean UPGRADE_USE_MORE_APPS_FOLDER = false;
    public static final int LOADER_FLAG_NONE = 0;
    public static final int LOADER_FLAG_CLEAR_WORKSPACE = 1 << 0;
    public static final int LOADER_FLAG_MIGRATE_SHORTCUTS = 1 << 1;
    /* Lenovo-SW zhaoxin5 20150528 add for 2 layer spport */
    public static final int LOADER_FLAG_MIGRATE_LENOVO_LAUNCHER = 1 << 5;
    /* Lenovo-SW zhaoxin5 20150528 add for 2 layer spport */
    public static final int LOADER_FLAG_RECOVERY_FROM_LBK = 1 << 6;
    //每次bind数量6,应该是为了防止一次加载太多卡界面。分批放到队列里面执行
    private static final int ITEMS_CHUNK = 6; // batch size for the workspace icons
    private static final long INVALID_SCREEN_ID = -1L;

    private final boolean mAppsCanBeOnRemoveableStorage;
    private final boolean mOldContentProviderExists;

    private final LauncherAppState mApp;
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
    private LoaderTask mLoaderTask;
    private boolean mIsLoaderTaskRunning;
    private volatile boolean mFlushingWorkerThread;

    public static final String KLAUNCHER_WIDGET_CLOCK_CLASS = "com.klauncher.ext.ClockWidgetProvider";
    public static final String KLAUNCHER_WIDGET_SEARCH_CLASS = "com.klauncher.ext.SearchWidgetProvider";

    // Specific runnable types that are run on the main thread deferred handler, this allows us to
    // clear all queued binding runnables when the Launcher activity is destroyed.
    // 跑在主线程deferred handler上的runnable类型，在Launcher Activity销毁时清除此类型的任务队列。
    private static final int MAIN_THREAD_NORMAL_RUNNABLE = 0;// 从来没用过
    private static final int MAIN_THREAD_BINDING_RUNNABLE = 1;

    private static final String MIGRATE_AUTHORITY = "com.android.launcher2.settings";

    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    // We start off with everything not loaded.  After that, we assume that
    // our monitoring of the package manager provides all updates and we never
    // need to do a requery.  These are only ever touched from the loader thread.
    // 刚刚进入程序时什么都没有加载，我们认为程序之后会加载到所有程序的更新等等。
    // 然后不会再去查询查找程序。只会在loader线程中被修改为true.
    private boolean mWorkspaceLoaded;
    private boolean mAllAppsLoaded;

    // When we are loading pages synchronously, we can't just post the binding of items on the side
    // pages as this delays the rotation process.  Instead, we wait for a callback from the first
    // draw (in Workspace) to initiate the binding of the remaining side pages.  Any time we start
    // a normal load, we also clear this set of Runnables.
    static final ArrayList<Runnable> mDeferredBindRunnables = new ArrayList<Runnable>();

    private WeakReference<Callbacks> mCallbacks;

    // < only access in worker thread >
    AllAppsList mBgAllAppsList;

    // The lock that must be acquired before referencing any static bg data structures.  Unlike
    // other locks, this one can generally be held long-term because we never expect any of these
    // static data structures to be referenced outside of the worker thread except on the first
    // load after configuration change.
    static final Object sBgLock = new Object();

    // sBgItemsIdMap maps *all* the ItemInfos (shortcuts, folders, and widgets) created by
    // LauncherModel to their ids
    // LauncherModel创建的所有itemInfos都在这里
    static final HashMap<Long, ItemInfo> sBgItemsIdMap = new HashMap<Long, ItemInfo>();

    // sBgWorkspaceItems is passed to bindItems, which expects a list of all folders and shortcuts
    //       created by LauncherModel that are directly on the home screen (however, no widgets or
    //       shortcuts within folders).
    static final ArrayList<ItemInfo> sBgWorkspaceItems = new ArrayList<ItemInfo>();

    // sBgAppWidgets is all LauncherAppWidgetInfo created by LauncherModel. Passed to bindAppWidget()
    static final ArrayList<LauncherAppWidgetInfo> sBgAppWidgets =
        new ArrayList<LauncherAppWidgetInfo>();

    // sBgFolders is all FolderInfos created by LauncherModel. Passed to bindFolders()
    static final HashMap<Long, FolderInfo> sBgFolders = new HashMap<Long, FolderInfo>();

    // sBgDbIconCache is the set of ItemInfos that need to have their icons updated in the database
    static final HashMap<Object, byte[]> sBgDbIconCache = new HashMap<Object, byte[]>();

    // sBgWorkspaceScreens is the ordered set of workspace screens.
    static final ArrayList<Long> sBgWorkspaceScreens = new ArrayList<Long>();

    // sPendingPackages is a set of packages which could be on sdcard and are not available yet
    static final HashMap<UserHandleCompat, HashSet<String>> sPendingPackages =
            new HashMap<UserHandleCompat, HashSet<String>>();

    /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 START */
    // 记录在加载过程中,检测出来的有问题的应用的列表
    ArrayList<ShortcutInfo> mShortcutInfosCachedLoaded = new ArrayList<ShortcutInfo>();
    // 记录在收到Package Added消息以后,进行匹配的应用信息列表
    ArrayList<ShortcutInfo> mShortcutInfosCachedAdded = new ArrayList<ShortcutInfo>();
    static final String TAG_SDCARD = "Loader.SDCARD";
    boolean isShortcutInfosCachedLoaded(ShortcutInfo s) {
    	if(null == s || null == s.getIntent() || null == s.getIntent().getComponent()) {
    		return false;
    	}
    	for(ShortcutInfo sInfo : mShortcutInfosCachedLoaded) {
    		if(sInfo.getIntent() !=null &&
    				sInfo.getIntent().getComponent() != null) {
    			boolean pkgSame = sInfo.getIntent().getComponent().getPackageName().equals(s.getIntent().getComponent().getPackageName());
    			boolean classSame = sInfo.getIntent().getComponent().getClassName().equals(s.getIntent().getComponent().getClassName());
    			if(pkgSame && classSame) {
    				mShortcutInfosCachedLoaded.remove(sInfo);
    				LauncherLog.i(TAG, TAG_SDCARD + ", isShortcutInfosCachedLoaded : " + s
    						+ ", mShortcutInfosCachedLoaded.size : " + mShortcutInfosCachedLoaded.size());
    				return true;
    			}
    		}
    	}
    	return false;
    }

    /** Lenovo-SW zhaoxin5 20150831 XTHREEROW-1232 START */
    boolean isShortcutInfosInCachedAdded(ComponentName cn) {
    	if(mShortcutInfosCachedAdded == null || mShortcutInfosCachedAdded.isEmpty() || mShortcutInfosCachedAdded.size() < 1 || null == cn) {
    		return false;
    	}
    	for(ShortcutInfo sInfo : mShortcutInfosCachedAdded) {
    		if(sInfo.getIntent() != null) {
    			boolean pkgSame = sInfo.getIntent().getComponent().getPackageName().equals(cn.getPackageName());
    			boolean classSame = sInfo.getIntent().getComponent().getClassName().equals(cn.getClassName());
    			if(pkgSame && classSame) {
    				LauncherLog.i(TAG, TAG_SDCARD + ", isShortcutInfosInCachedAdded : " + cn);
    				return true;
    			}
    		}

    	}
    	return false;
    }
    /** Lenovo-SW zhaoxin5 20150831 XTHREEROW-1232 END */

    ShortcutInfo isShortcutInfosCachedAdded(ComponentName cn) {
    	if(mShortcutInfosCachedAdded == null || mShortcutInfosCachedAdded.isEmpty() || mShortcutInfosCachedAdded.size() < 1 || null == cn) {
    		return null;
    	}
    	for(ShortcutInfo sInfo : mShortcutInfosCachedAdded) {
    		if(sInfo.getIntent() != null) {
    			boolean pkgSame = sInfo.getIntent().getComponent().getPackageName().equals(cn.getPackageName());
    			boolean classSame = sInfo.getIntent().getComponent().getClassName().equals(cn.getClassName());
    			if(pkgSame && classSame) {
    				// 如果在收到Package Add消息时,
    	    		// 在当前数组中检测到该应用则将该应用添加到对应的位置
    	    		// 如果该位置还存在的话,
    	    		// 然后移除该记录
    				LauncherLog.i(TAG, TAG_SDCARD + ", isShortcutInfosCachedAdded : " + cn);
    				mShortcutInfosCachedAdded.remove(sInfo);
    				LauncherLog.i(TAG, TAG_SDCARD + ", isPackagesOnAdd size : " + mShortcutInfosCachedAdded.size());
    				return sInfo;
    			}
    		}

    	}
    	return null;
    }
    /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 END */

    // </ only access in worker thread >

    public static final String ACTION_UNREAD_CHANGED =
            "com.android.launcher.action.UNREAD_CHANGED";
    //Lenovo-sw zhangyj19 add 2015/07/21 modify theme
    public static final String ACTION_THEME_CHANGED =
            "appiconintegration.action.UPDATE_THEME_COMPLETED";
    //Lenovo-sw luyy1 add 20150729 for Yandex search
    public static final String ACTION_SEARCH_CHANGED =
          "com.android.launcher.CHANGE_SEARCH_ACTION";
    //Lenovo-sw luyy1 add 20150819 for wallpaper listener
    public static final String ACTION_THEME_WALLPAPER_CHANGED =
            "com.lenovo.themecenter.setwallpaper";
    private IconCache mIconCache;

    protected int mPreviousConfigMcc;

    private final LauncherAppsCompat mLauncherApps;
    private final UserManagerCompat mUserManager;

    //Lenovo-sw luyy1 add 20150819 for wallpaper listener
    public static final int WALLPAPER_DEFAULT_SOURCE = -2;
    public static final int WALLPAPER_OTHER_SOURCE = -1;
    public static final int WALLPAPER_OTHER_THEME_SOURCE = 0;
    private int mWallpaperResId = WALLPAPER_DEFAULT_SOURCE;

    public interface Callbacks {
        /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success START */
        public boolean setLoadOnResume(int flags);
        /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success END */
        public void loadSettingsAndFolderTitlesFromLbk(); // 从LBK中读取Settings设置属性
        public int getCurrentWorkspaceScreen();
        public void loadResource();
        public void startBinding();
        public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end,
                              boolean forceAnimateIcons);
        public void bindScreens(ArrayList<Long> orderedScreenIds);
        public void bindAddScreens(ArrayList<Long> orderedScreenIds);
        public void bindFolders(HashMap<Long,FolderInfo> folders);
        public void finishBindingItems(boolean upgradePath);
        public void bindAppWidget(LauncherAppWidgetInfo info);
        public void bindAllApplications(ArrayList<AppInfo> apps);
        public void bindAppsAdded(ArrayList<Long> newScreens,
                                  ArrayList<ItemInfo> addNotAnimated,
                                  ArrayList<ItemInfo> addAnimated,
                                  ArrayList<AppInfo> addedApps);
        public void bindAppsUpdated(ArrayList<AppInfo> apps);
        public void bindAppsRestored(ArrayList<AppInfo> apps);
        public void updatePackageState(ArrayList<PackageInstallInfo> installInfo);
        public void updatePackageBadge(String packageName);
        public void bindComponentsRemoved(ArrayList<String> packageNames,
                        ArrayList<AppInfo> appInfos, UserHandleCompat user);
        public void bindPackagesUpdated(ArrayList<Object> widgetsAndShortcuts);
        public void bindSearchablesChanged();
        //Lenovo-sw luyy1 add 20150729 for Yandex search
        public void bindGoogleSearchChanged();
        public boolean isAllAppsButtonRank(int rank);
        public void onPageBoundSynchronously(int page);
        public void dumpLogsToLocalData();

        public Launcher getLauncherInstance();
        //add by zhanggx1 for reordering all pages on 2013-11-20. s
		void autoReorder(String from);
        public void finishLoadingAndBindingWorkspace();
        //add by zhanggx1 for reordering all pages on 2013-11-20. e

        void beforeBinding();
        void afterBinding();
        /** Lenovo-SW zhaoxin5 20150909 Launcher 启动优化 START */
        void afterBindingCurrentPage();
        /** Lenovo-SW zhaoxin5 20150909 Launcher 启动优化 END */
        //Lenovo-sw luyy1 add 20150819 for wallpaper list
        public void updateWallpaperItem(int id);
        public boolean isApkWallpaperItemClick();
        public void reloadApkWallpaper();
    }

    private HashMap<ComponentName, UnreadInfo> unreadChangedMap =
            new HashMap<ComponentName, LauncherModel.UnreadInfo>();

    private class UnreadInfo {
        ComponentName mComponentName;
        int mUnreadNum;

        public UnreadInfo(ComponentName componentName, int unreadNum) {
            mComponentName = componentName;
            mUnreadNum = unreadNum;
        }
    }

    private class UnreadNumberChangeTask implements Runnable {
        public void run() {
            ArrayList<UnreadInfo> unreadInfos = new ArrayList<LauncherModel.UnreadInfo>();
            synchronized (unreadChangedMap) {
                unreadInfos.addAll(unreadChangedMap.values());
                unreadChangedMap.clear();
            }

            Context context = mApp.getContext();
            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                /** Lenovo-SW zhaoxin5 20150916 KOLEOSROW-2238 KOLEOSROW-698 START */
                sendCheckUnreadBroadcast("UnreadNumberChangeTask");
                /** Lenovo-SW zhaoxin5 20150916 KOLEOSROW-2238 KOLEOSROW-698 END */
                return;
            }

            final ArrayList<AppInfo> unreadChangeFinal = new ArrayList<AppInfo>();
            for (UnreadInfo uInfo : unreadInfos) {
                AppInfo info = mBgAllAppsList.unreadNumbersChanged(context,
                        uInfo.mComponentName, uInfo.mUnreadNum);
                if (info != null) {
                    unreadChangeFinal.add(info);
                }
            }

            if (unreadChangeFinal.isEmpty()) return;

            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.bindAppsUpdated(unreadChangeFinal);
                    }
                }
            });
        }
    }

    private UnreadNumberChangeTask mUnreadUpdateTask = new UnreadNumberChangeTask();

    /*Lenovo-sw luyy1 add 20150729 for Yandex search start*/
    private class SearchWidgetChangeTask implements Runnable {
        public void run() {
            Context context = mApp.getContext();
            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.bindGoogleSearchChanged();
                    }
                }
            });
        }
    }

    private SearchWidgetChangeTask mSearchUpdateTask = new SearchWidgetChangeTask();
    /*Lenovo-sw luyy1 add 20150729 for Yandex search end*/

    /*Lenovo-sw luyy1 add 20150924 to fix KOLEOSROW-2813 start*/
    private class WallpaperImageChangeTask implements Runnable {
        public void run() {
            Context context = mApp.getContext();
            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        if (callbacks.isApkWallpaperItemClick()) {
                            mWallpaperResId = WALLPAPER_OTHER_SOURCE;
                            return;
                        }
                        // Case 1. APK file: mWallpaperResId is above WALLPAPER_OTHER_THEME_SOURCE.
                        // Case 2. not APK file using ThemeCenter: WALLPAPER_OTHER_THEME_SOURCE
                        // Case 3. not APK file not using ThemeCenter(Google photos): WALLPAPER_OTHER_STATIC_SOURCE
                        callbacks.updateWallpaperItem(mWallpaperResId);
                        mWallpaperResId = WALLPAPER_OTHER_SOURCE;
                    }
                }
            });

        }
    }

    private WallpaperImageChangeTask mWallpaperImageUpdateTask = new WallpaperImageChangeTask();
    /*Lenovo-sw luyy1 add 20150924 to fix KOLEOSROW-2813 end*/

    public interface ItemInfoFilter {
        public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn);
    }

    LauncherModel(LauncherAppState app, IconCache iconCache, AppFilter appFilter) {
        Context context = app.getContext();

        mAppsCanBeOnRemoveableStorage = Environment.isExternalStorageRemovable();
        String oldProvider = context.getString(R.string.old_launcher_provider_uri);
        // This may be the same as MIGRATE_AUTHORITY, or it may be replaced by a different
        // resource string.
        String redirectAuthority = Uri.parse(oldProvider).getAuthority();
        ProviderInfo providerInfo =
                context.getPackageManager().resolveContentProvider(MIGRATE_AUTHORITY, 0);
        ProviderInfo redirectProvider =
                context.getPackageManager().resolveContentProvider(redirectAuthority, 0);

        Log.d(TAG, "Old launcher provider: " + oldProvider);
        mOldContentProviderExists = (providerInfo != null) && (redirectProvider != null);

        if (mOldContentProviderExists) {
            Log.d(TAG, "Old launcher provider exists.");
        } else {
            Log.d(TAG, "Old launcher provider does not exist.");
        }

        mApp = app;
        mBgAllAppsList = new AllAppsList(iconCache, appFilter);
        mIconCache = iconCache;

        final Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        mPreviousConfigMcc = config.mcc;
        mLauncherApps = LauncherAppsCompat.getInstance(context);
        mUserManager = UserManagerCompat.getInstance(context);
    }

    /** Runs the specified runnable immediately if called from the main thread, otherwise it is
     * posted on the main thread handler. */
    private void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }
    private void runOnMainThread(Runnable r, int type) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            mHandler.post(r);
        } else {
            r.run();
        }
    }

    /** Runs the specified runnable immediately if called from the worker thread, otherwise it is
     * posted on the worker thread handler. */
    private static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker handler
            sWorker.post(r);
        }
    }

    boolean canMigrateFromOldLauncherDb(Launcher launcher) {
        return mOldContentProviderExists && !launcher.isLauncherPreinstalled() ;
    }

    static boolean findNextAvailableIconSpaceInScreen(ArrayList<ItemInfo> items, int[] xy,
                                 long screen) {
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        final int xCount = (int) grid.numColumns;
        final int yCount = (int) grid.numRows;
        boolean[][] occupied = new boolean[xCount][yCount];

        int cellX, cellY, spanX, spanY;
        for (int i = 0; i < items.size(); ++i) {
            final ItemInfo item = items.get(i);
            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                if (item.screenId == screen) {
                    cellX = item.cellX;
                    cellY = item.cellY;
                    spanX = item.spanX;
                    spanY = item.spanY;
                    for (int x = cellX; 0 <= x && x < cellX + spanX && x < xCount; x++) {
                        for (int y = cellY; 0 <= y && y < cellY + spanY && y < yCount; y++) {
                            occupied[x][y] = true;
                        }
                    }
                }
            }
        }

        return CellLayout.findVacantCell(xy, 1, 1, xCount, yCount, occupied);
    }
    static Pair<Long, int[]> findNextAvailableIconSpace(Context context, String name,
                                                        Intent launchIntent,
                                                        int firstScreenIndex,
                                                        ArrayList<Long> workspaceScreens) {
        // Lock on the app so that we don't try and get the items while apps are being added
        LauncherAppState app = LauncherAppState.getInstance();
        LauncherModel model = app.getModel();
        boolean found = false;
        synchronized (app) {
            if (sWorkerThread.getThreadId() != Process.myTid()) {
                // Flush the LauncherModel worker thread, so that if we just did another
                // processInstallShortcut, we give it time for its shortcut to get added to the
                // database (getItemsInLocalCoordinates reads the database)
                model.flushWorkerThread();
            }
            final ArrayList<ItemInfo> items = LauncherModel.getItemsInLocalCoordinates(context);

            // Try adding to the workspace screens incrementally, starting at the default or center
            // screen and alternating between +1, -1, +2, -2, etc. (using ~ ceil(i/2f)*(-1)^(i-1))
            firstScreenIndex = Math.min(firstScreenIndex, workspaceScreens.size());
            int count = workspaceScreens.size();
            for (int screen = firstScreenIndex; screen < count && !found; screen++) {
                int[] tmpCoordinates = new int[2];
                if (findNextAvailableIconSpaceInScreen(items, tmpCoordinates,
                        workspaceScreens.get(screen))) {
                    // Update the Launcher db
                    return new Pair<Long, int[]>(workspaceScreens.get(screen), tmpCoordinates);
                }
            }
        }
        return null;
    }

    public void setPackageState(final ArrayList<PackageInstallInfo> installInfo) {
        // Process the updated package state
        Runnable r = new Runnable() {
            public void run() {
                Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                if (callbacks != null) {
                    callbacks.updatePackageState(installInfo);
                }
            }
        };
        mHandler.post(r);
    }

    public void updatePackageBadge(final String packageName) {
        // Process the updated package badge
        Runnable r = new Runnable() {
            public void run() {
                Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                if (callbacks != null) {
                    callbacks.updatePackageBadge(packageName);
                }
            }
        };
        mHandler.post(r);
    }

    public void addAppsToAllApps(final Context ctx, final ArrayList<AppInfo> allAppsApps) {
        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;

        if (allAppsApps == null) {
            throw new RuntimeException("allAppsApps must not be null");
        }
        if (allAppsApps.isEmpty()) {
            return;
        }

        final ArrayList<AppInfo> restoredAppsFinal = new ArrayList<AppInfo>();
        Iterator<AppInfo> iter = allAppsApps.iterator();
        while (iter.hasNext()) {
            ItemInfo a = iter.next();
            if (LauncherModel.appWasPromise(ctx, a.getIntent(), a.user)) {
                restoredAppsFinal.add((AppInfo) a);
            }
        }

        // Process the newly added applications and add them to the database first
        Runnable r = new Runnable() {
            public void run() {
                runOnMainThread(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            if (!restoredAppsFinal.isEmpty()) {
                                for (AppInfo info : restoredAppsFinal) {
                                    final Intent intent = info.getIntent();
                                    if (intent != null) {
                                        mIconCache.deletePreloadedIcon(intent.getComponent(),
                                                info.user);
                                    }
                                }
                                callbacks.bindAppsUpdated(restoredAppsFinal);
                            }
                            callbacks.bindAppsAdded(null, null, null, allAppsApps);
                        }
                    }
                });
            }
        };
        runOnWorkerThread(r);
    }

    public void addAndBindAddedWorkspaceApps(final Context context,
            final ArrayList<ItemInfo> workspaceApps) {
        Thread.dumpStack();
        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;

        if (workspaceApps == null) {
            throw new RuntimeException("workspaceApps and allAppsApps must not be null");
        }
        if (workspaceApps.isEmpty()) {
            return;
        }
        // Process the newly added applications and add them to the database first
        Runnable r = new Runnable() {
            public void run() {
                final ArrayList<ItemInfo> addedShortcutsFinal = new ArrayList<ItemInfo>();
                final ArrayList<Long> addedWorkspaceScreensFinal = new ArrayList<Long>();
                final ArrayList<AppInfo> restoredAppsFinal = new ArrayList<AppInfo>();

                // Get the list of workspace screens.  We need to append to this list and
                // can not use sBgWorkspaceScreens because loadWorkspace() may not have been
                // called.
                ArrayList<Long> workspaceScreens = new ArrayList<Long>();
                TreeMap<Integer, Long> orderedScreens = loadWorkspaceScreensDb(context);
                for (Integer i : orderedScreens.keySet()) {
                    long screenId = orderedScreens.get(i);
                    workspaceScreens.add(screenId);
                }

                synchronized(sBgLock) {
                    Iterator<ItemInfo> iter = workspaceApps.iterator();
                    while (iter.hasNext()) {
                        ItemInfo a = iter.next();
                        final String name = a.title.toString();
                        final Intent launchIntent = a.getIntent();

                        // Short-circuit this logic if the icon exists somewhere on the workspace
                        /** Lenovo-SW zhaoxin5 20151014 PASSIONROW-2195	START */
                        if (LauncherModel.shortcutExists(context, name, launchIntent, false)) {
                            // Only InstallShortcutReceiver sends us shortcutInfos, ignore them
                            if (a instanceof AppInfo &&
                                    LauncherModel.appWasPromise(context, launchIntent, a.user)) {
                                restoredAppsFinal.add((AppInfo) a);
                            }
                            continue;
                        }
                        /** Lenovo-SW zhaoxin5 20151014 PASSIONROW-2195	END */

                        // Add this icon to the db, creating a new page if necessary.  If there
                        // is only the empty page then we just add items to the first page.
                        // Otherwise, we add them to the next pages.
                        int startSearchPageIndex = workspaceScreens.isEmpty() ? 0 :
                                context.getResources().getInteger(R.integer.config_workspaceInstalledAppStartScreen);
                        Pair<Long, int[]> coords = LauncherModel.findNextAvailableIconSpace(context,
                                name, launchIntent, startSearchPageIndex, workspaceScreens);
                        if (coords == null) {
                            LauncherProvider lp = LauncherAppState.getLauncherProvider();

                            // If we can't find a valid position, then just add a new screen.
                            // This takes time so we need to re-queue the add until the new
                            // page is added.  Create as many screens as necessary to satisfy
                            // the startSearchPageIndex.
                            int numPagesToAdd = Math.max(1, startSearchPageIndex + 1 -
                                    workspaceScreens.size());
                            while (numPagesToAdd > 0) {
                                long screenId = lp.generateNewScreenId();
                                // Save the screen id for binding in the workspace
                                workspaceScreens.add(screenId);
                                addedWorkspaceScreensFinal.add(screenId);
                                numPagesToAdd--;
                            }

                            // Find the coordinate again
                            coords = LauncherModel.findNextAvailableIconSpace(context,
                                    name, launchIntent, startSearchPageIndex, workspaceScreens);
                        }
                        if (coords == null) {
                            throw new RuntimeException("Coordinates should not be null");
                        }

                        ShortcutInfo shortcutInfo;
                        if (a instanceof ShortcutInfo) {
                            shortcutInfo = (ShortcutInfo) a;
                        } else if (a instanceof AppInfo) {
                            shortcutInfo = ((AppInfo) a).makeShortcut();
                        } else {
                            throw new RuntimeException("Unexpected info type");
                        }

                        /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 START */
						boolean needSkip = false;
						long screenId = coords.first;
						int cellX = coords.second[0];
						int cellY = coords.second[1];
						LauncherLog.i(TAG, TAG_SDCARD + ", " + launchIntent.getComponent()
								+ ", x:" + coords.second[0] + ", y:"
								+ coords.second[1] + ", screend:"
								+ coords.first);
						final ShortcutInfo infoCachedOnRemoved = isShortcutInfosCachedAdded(launchIntent.getComponent());
						if (null != infoCachedOnRemoved) {
							// 如果当前应用是在AppsAvailabilityCheck检查中删除的应用
							if ((infoCachedOnRemoved.container != LauncherSettings.Favorites.CONTAINER_DESKTOP)
									&& (infoCachedOnRemoved.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT)) {
								// 原来是放在文件夹中的应用
								final FolderInfo f = sBgFolders.get(infoCachedOnRemoved.container);
								if (null != f) {
									//
									LauncherLog.i(TAG, TAG_SDCARD + ", find original position for the new added apps : " + launchIntent.getComponent()+ ", " + f);
									runOnMainThread(new Runnable() {
										@Override
										public void run() {
											// TODO Auto-generated method stub
											infoCachedOnRemoved.prepareDataForSDCardApp();
											f.add(infoCachedOnRemoved);
										}
									});
									needSkip = true;
								}
							} else if (infoCachedOnRemoved.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
								// 是在桌面上的应用
								boolean empty = checkPositionIsEmpty(infoCachedOnRemoved, context, workspaceScreens);
								if(empty) {
									screenId = infoCachedOnRemoved.screenId;
									cellX = infoCachedOnRemoved.cellX;
									cellY = infoCachedOnRemoved.cellY;
								} else {
									LauncherLog.i(TAG, TAG_SDCARD + ", not find old position for : " + infoCachedOnRemoved);
								}
							}
						}
                        // modify by yanni: stk/utk special move into folder
                        String packageName = null;
                        try {
                            packageName = shortcutInfo.getTargetComponent().getPackageName();
                        } catch (Exception e) { }
                        if ("com.android.stk".equals(packageName) || "com.android.utk".equals(packageName)) {
                            LauncherLog.i(TAG, TAG_SDCARD + ": component=" + shortcutInfo.getTargetComponent().getClassName());
                            final ShortcutInfo shortcut = shortcutInfo;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Collection<FolderInfo> folderList = sBgFolders.values();
                                    for (FolderInfo folderInfo : folderList) {
                                        if (mApp.getContext().getResources().getString(R.string.folder_system_tool)
                                                .equals(folderInfo.title)) {
                                            folderInfo.add(shortcut);
                                            break;
                                        }
                                    }
                                }
                            });
                        } else {
                            if(!needSkip) {
                                if (!TextUtils.isEmpty(packageName) && !isFilterPackage(packageName)) {
                                    // Add the shortcut to the db
                                    addItemToDatabase(context, shortcutInfo,
                                            LauncherSettings.Favorites.CONTAINER_DESKTOP,
                                            screenId, cellX, cellY, false);
                                    // Save the ShortcutInfo for binding in the workspace
                                    addedShortcutsFinal.add(shortcutInfo);
                                }

                            }
                        }
                        /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 END */
                        //添加到系统工具文件夹
                        /*if(SettingsValue.isKlauncherCommon(mApp.getContext()) && !TextUtils.isEmpty(packageName) && !isContain(packageName)) {
                            LauncherLog.i(TAG, TAG_SDCARD + ": component=" + shortcutInfo.getTargetComponent().getClassName());
                            final ShortcutInfo shortcut = shortcutInfo;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Collection<FolderInfo> folderList = sBgFolders.values();
                                    for (FolderInfo folderInfo : folderList) {
                                        if (mApp.getContext().getResources().getString(R.string.folder_system_tool)
                                                .equals(folderInfo.title)) {
                                            folderInfo.add(shortcut);
                                            break;
                                        }
                                    }
                                }
                            });
                            LogUtil.e("wqhLauncherModer"," systemfolderInfo.add");
                        }*/
                    }
                }

                // Update the workspace screens
                updateWorkspaceScreenOrder(context, workspaceScreens);

                if (!addedShortcutsFinal.isEmpty()) {
                    runOnMainThread(new Runnable() {
                        public void run() {
                            Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                            if (callbacks == cb && cb != null) {
                                final ArrayList<ItemInfo> addAnimated = new ArrayList<ItemInfo>();
                                final ArrayList<ItemInfo> addNotAnimated = new ArrayList<ItemInfo>();
                                if (!addedShortcutsFinal.isEmpty()) {
                                    ItemInfo info = addedShortcutsFinal.get(addedShortcutsFinal.size() - 1);
                                    long lastScreenId = info.screenId;
                                    for (ItemInfo i : addedShortcutsFinal) {
                                    	/** Lenovo-SW zhaoxin5 20150909 Launcher 启动优化 START
                                    	 *  将应用添加到屏幕最后时，
                                    	 *  不要自动将屏幕滚动到最后
                                    	 * */
                                        /*if (i.screenId == lastScreenId) {
                                            addAnimated.add(i);
                                        } else {
                                            addNotAnimated.add(i);
                                        }*/
                                    	addNotAnimated.add(i);
                                    	/** Lenovo-SW zhaoxin5 20150909 Launcher 启动优化 START */
                                    }
                                }
                                callbacks.bindAppsAdded(addedWorkspaceScreensFinal,
                                        addNotAnimated, addAnimated, null);
                                if (!restoredAppsFinal.isEmpty()) {
                                    callbacks.bindAppsUpdated(restoredAppsFinal);
                                }
                            }
                        }
                    });
                }
            }
        };
        runOnWorkerThread(r);
    }

    public void unbindItemInfosAndClearQueuedBindRunnables() {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            throw new RuntimeException("Expected unbindLauncherItemInfos() to be called from the " +
                    "main thread");
        }

        // Clear any deferred bind runnables
        synchronized (mDeferredBindRunnables) {
            mDeferredBindRunnables.clear();
        }
        // Remove any queued bind runnables
        mHandler.cancelAllRunnablesOfType(MAIN_THREAD_BINDING_RUNNABLE);
        // Unbind all the workspace items
        unbindWorkspaceItemsOnMainThread();
    }

    /** Unbinds all the sBgWorkspaceItems and sBgAppWidgets on the main thread */
    void unbindWorkspaceItemsOnMainThread() {
        // Ensure that we don't use the same workspace items data structure on the main thread
        // by making a copy of workspace items first.
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
            tmpAppWidgets.addAll(sBgAppWidgets);
        }
        Runnable r = new Runnable() {
                @Override
                public void run() {
                   for (ItemInfo item : tmpWorkspaceItems) {
                       item.unbind();
                   }
                   for (ItemInfo item : tmpAppWidgets) {
                       item.unbind();
                   }
                }
            };
        runOnMainThread(r);
    }

    /**
     * Adds an item to the DB if it was not created previously, or move it to a new
     * <container, screen, cellX, cellY>
     * 如果不曾有，就添加，如果已经存在，则修改
     */
    static void addOrMoveItemInDatabase(Context context, ItemInfo item, long container,
            long screenId, int cellX, int cellY) {
        if (item.container == ItemInfo.NO_ID) {
            // From all apps
            addItemToDatabase(context, item, container, screenId, cellX, cellY, false);
        } else {
            // From somewhere else
            moveItemInDatabase(context, item, container, screenId, cellX, cellY);
        }
    }

    static void checkItemInfoLocked(
            final long itemId, final ItemInfo item, StackTraceElement[] stackTrace) {
/*        ItemInfo modelItem = sBgItemsIdMap.get(itemId);
        if (modelItem != null && item != modelItem) {
            // check all the data is consistent
            if (modelItem instanceof ShortcutInfo && item instanceof ShortcutInfo) {
                ShortcutInfo modelShortcut = (ShortcutInfo) modelItem;
                ShortcutInfo shortcut = (ShortcutInfo) item;
                if (modelShortcut.title.toString().equals(shortcut.title.toString()) &&
                        modelShortcut.intent.filterEquals(shortcut.intent) &&
                        modelShortcut.id == shortcut.id &&
                        modelShortcut.itemType == shortcut.itemType &&
                        modelShortcut.container == shortcut.container &&
                        modelShortcut.screenId == shortcut.screenId &&
                        modelShortcut.cellX == shortcut.cellX &&
                        modelShortcut.cellY == shortcut.cellY &&
                        modelShortcut.spanX == shortcut.spanX &&
                        modelShortcut.spanY == shortcut.spanY &&
                        ((modelShortcut.dropPos == null && shortcut.dropPos == null) ||
                        (modelShortcut.dropPos != null &&
                                shortcut.dropPos != null &&
                                modelShortcut.dropPos[0] == shortcut.dropPos[0] &&
                        modelShortcut.dropPos[1] == shortcut.dropPos[1]))) {
                    // For all intents and purposes, this is the same object
                    return;
                }
            }

            // the modelItem needs to match up perfectly with item if our model is
            // to be consistent with the database-- for now, just require
            // modelItem == item or the equality check above
            String msg = "item: " + ((item != null) ? item.toString() : "null") +
                    "modelItem: " +
                    ((modelItem != null) ? modelItem.toString() : "null") +
                    "Error: ItemInfo passed to checkItemInfo doesn't match original";
            RuntimeException e = new RuntimeException(msg);
            if (stackTrace != null) {
                e.setStackTrace(stackTrace);
            }
            // throw e;
        }*/
    }

    static void checkItemInfo(final ItemInfo item) {
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        final long itemId = item.id;
        Runnable r = new Runnable() {
            public void run() {
                synchronized (sBgLock) {
                    checkItemInfoLocked(itemId, item, stackTrace);
                }
            }
        };
        runOnWorkerThread(r);
    }

    static void updateItemInDatabaseHelper(Context context, final ContentValues values,
            final ItemInfo item, final String callingFunction) {
        final long itemId = item.id;
        final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
        final ContentResolver cr = context.getContentResolver();

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Runnable r = new Runnable() {
            public void run() {
                cr.update(uri, values, null, null);
                updateItemArrays(item, itemId, stackTrace);
            }
        };
        runOnWorkerThread(r);
    }

    static void updateItemsInDatabaseHelper(Context context, final ArrayList<ContentValues> valuesList,
            final ArrayList<ItemInfo> items, final String callingFunction) {
        final ContentResolver cr = context.getContentResolver();

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Runnable r = new Runnable() {
            public void run() {
                ArrayList<ContentProviderOperation> ops =
                        new ArrayList<ContentProviderOperation>();
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    ItemInfo item = items.get(i);
                    final long itemId = item.id;
                    final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
                    ContentValues values = valuesList.get(i);

                    ops.add(ContentProviderOperation.newUpdate(uri).withValues(values).build());
                    updateItemArrays(item, itemId, stackTrace);

                }
                try {
                    cr.applyBatch(LauncherProvider.AUTHORITY, ops);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        runOnWorkerThread(r);
    }

    static void updateItemArrays(ItemInfo item, long itemId, StackTraceElement[] stackTrace) {
        // Lock on mBgLock *after* the db operation
        synchronized (sBgLock) {
            checkItemInfoLocked(itemId, item, stackTrace);

            if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                    item.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                // Item is in a folder, make sure this folder exists
                if (!sBgFolders.containsKey(item.container)) {
                    // An items container is being set to a that of an item which is not in
                    // the list of Folders.
                    String msg = "item: " + item + " container being set to: " +
                            item.container + ", not in the list of folders";
                    Log.e(TAG, msg);
                }
            }

            // Items are added/removed from the corresponding FolderInfo elsewhere, such
            // as in Workspace.onDrop. Here, we just add/remove them from the list of items
            // that are on the desktop, as appropriate
            ItemInfo modelItem = sBgItemsIdMap.get(itemId);
            if (modelItem != null &&
                    (modelItem.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                     modelItem.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT)) {
                switch (modelItem.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                        if (!sBgWorkspaceItems.contains(modelItem)) {
                            sBgWorkspaceItems.add(modelItem);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                sBgWorkspaceItems.remove(modelItem);
            }
        }
    }

    public void flushWorkerThread() {
        mFlushingWorkerThread = true;
        Runnable waiter = new Runnable() {
                public void run() {
                    synchronized (this) {
                        notifyAll();
                        mFlushingWorkerThread = false;
                    }
                }
            };

        synchronized(waiter) {
            runOnWorkerThread(waiter);
            if (mLoaderTask != null) {
                synchronized(mLoaderTask) {
                    mLoaderTask.notify();
                }
            }
            boolean success = false;
            while (!success) {
                try {
                    waiter.wait();
                    success = true;
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Move an item in the DB to a new <container, screen, cellX, cellY>
     */
    static void moveItemInDatabase(Context context, final ItemInfo item, final long container,
            final long screenId, final int cellX, final int cellY) {
        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;

        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screenId < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screenId = screenId;
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screenId);

        updateItemInDatabaseHelper(context, values, item, "moveItemInDatabase");
    }

    /**
     * Move items in the DB to a new <container, screen, cellX, cellY>. We assume that the
     * cellX, cellY have already been updated on the ItemInfos.
     */
    static void moveItemsInDatabase(Context context, final ArrayList<ItemInfo> items,
            final long container, final int screen) {

        ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
        int count = items.size();

        for (int i = 0; i < count; i++) {
            ItemInfo item = items.get(i);
            item.container = container;

            // We store hotseat items in canonical form which is this orientation invariant position
            // in the hotseat
            if (context instanceof Launcher && screen < 0 &&
                    container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(item.cellX,
                        item.cellY);
            } else {
                item.screenId = screen;
            }

            final ContentValues values = new ContentValues();
            values.put(LauncherSettings.Favorites.CONTAINER, item.container);
            values.put(LauncherSettings.Favorites.CELLX, item.cellX);
            values.put(LauncherSettings.Favorites.CELLY, item.cellY);
            values.put(LauncherSettings.Favorites.SCREEN, item.screenId);

            contentValues.add(values);
        }
        updateItemsInDatabaseHelper(context, contentValues, items, "moveItemInDatabase");
    }

    /**
     * Move and/or resize item in the DB to a new <container, screen, cellX, cellY, spanX, spanY>
     */
    static void modifyItemInDatabase(Context context, final ItemInfo item, final long container,
            final long screenId, final int cellX, final int cellY, final int spanX, final int spanY) {
        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;
        item.spanX = spanX;
        item.spanY = spanY;

        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screenId < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screenId = screenId;
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SPANX, item.spanX);
        values.put(LauncherSettings.Favorites.SPANY, item.spanY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screenId);

        updateItemInDatabaseHelper(context, values, item, "modifyItemInDatabase");
    }

    /**
     * Update an item to the database in a specified container.
     */
    static void updateItemInDatabase(Context context, final ItemInfo item) {
        final ContentValues values = new ContentValues();
        item.onAddToDatabase(context, values);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }

    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists(Context context, String title, Intent intent) {
        final ContentResolver cr = context.getContentResolver();
        final Intent intentWithPkg, intentWithoutPkg;

        if (intent.getComponent() != null) {
            // If component is not null, an intent with null package will produce
            // the same result and should also be a match.
            if (intent.getPackage() != null) {
                intentWithPkg = intent;
                intentWithoutPkg = new Intent(intent).setPackage(null);
            } else {
                intentWithPkg = new Intent(intent).setPackage(
                        intent.getComponent().getPackageName());
                intentWithoutPkg = intent;
            }
        } else {
            intentWithPkg = intent;
            intentWithoutPkg = intent;
        }
        // changed by xutg1, support to switch between single layer & two layers
        int for2layerWorkspace = LauncherAppState.getInstance().getCurrentLayoutInt();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { "title", "intent", "for2layerWorkspace"}, "title=? and (intent=? or intent=?) and for2layerWorkspace=?",
                new String[] { title, intentWithPkg.toUri(0), intentWithoutPkg.toUri(0), String.valueOf(for2layerWorkspace) }, null);
        // change end xutg1, support to switch between single layer & two layers
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }
        return result;
    }

    /** Lenovo-SW zhaoxin5 20151014 PASSIONROW-2195	START */
    /***
     *
     * 说明,这个函数是针对单层桌面全部应用加载完成后,
     * 检查是否有遗漏的未添加的应用,将其再加到桌面上,
     * 然后通过这个函数判断这个应用是否已经添加过了.
     *
     * 但是因为目前Launcher中会给应用的Intent中添加一个Extra:l.profile=0,
     * 比如:
     * #Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;
     * component=com.android.chrome/com.google.android.apps.chrome.Main;l.profile=0;end
     *
     * 同时因为这个函数还会匹配应用的名称,
     * 结合以上两个原因,导致这个函数不起作用.
     *
     * 目前处理方法如下:
     * 1,去掉Extra:l.profile=0,
     * 2,针对App,不检查title
     *
     */
    static boolean shortcutExists(Context context, String title, Intent intent, boolean isInstallShortcut) {
        final Intent intentWithPkg, intentWithoutPkg;

        if (intent.getComponent() != null) {
            if ("com.android.settings.MainSettings".equals(intent.getComponent().getClassName()) ||
                    context.getPackageName().equals(intent.getComponent().getPackageName())) {
                return true;
            }
            // If component is not null, an intent with null package will produce
            // the same result and should also be a match.
            if (intent.getPackage() != null) {
                intentWithPkg = intent;
                intentWithoutPkg = new Intent(intent).setPackage(null);
            } else {
                intentWithPkg = new Intent(intent).setPackage(
                        intent.getComponent().getPackageName());
                intentWithoutPkg = intent;
            }
        } else {
            intentWithPkg = intent;
            intentWithoutPkg = intent;
        }

    	if (!isInstallShortcut) {
    		return shortcutExistsForApp(context, intentWithPkg, intentWithoutPkg);
    	}
    	return shortcutExistsForShortcut(context, title, intentWithPkg, intentWithoutPkg);
    }

    static boolean shortcutExistsForApp(Context context, final Intent intentWithPkg, final Intent intentWithoutPkg) {
    	// 针对App安装的情况,忽略Title,只匹配intent
        final ContentResolver cr = context.getContentResolver();

        // 1) 
        // 先检查存在如下intent的情况
        // #Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.android.chrome/com.google.android.apps.chrome.Main;l.profile=0;end
        // 可看到上面intent的最后有一个Extra:l.profile=0

        // changed by xutg1, support to switch between single layer & two layers
        int for2layerWorkspace = LauncherAppState.getInstance().getCurrentLayoutInt();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { "itemType", "intent", "for2layerWorkspace"}, "itemType=? and (intent=? or intent=?) and for2layerWorkspace=?",
                new String[] { String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_APPLICATION), intentWithPkg.toUri(0), intentWithoutPkg.toUri(0), String.valueOf(for2layerWorkspace) }, null);
        // change end xutg1, support to switch between single layer & two layers
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }

        if (result == true) {
        	// 找到直接返回true
        	LauncherLog.i(TAG, "shortcutExistsForApp 1 : " + intentWithPkg);
        	return true;
        }

        // 2) 
        // 再检查去掉Extra:l.profile=0
        // 继续查找

        /* Lenovo-sw luyy1 add for Android FW 2015-10-23 START (KOLEOSROW-3226)*/
        if (intentWithPkg.getExtras() != null && intentWithoutPkg.getExtras() != null &&
            intentWithPkg.getExtras().containsKey(ItemInfo.EXTRA_PROFILE) &&
            intentWithPkg.getExtras().getLong(ItemInfo.EXTRA_PROFILE) != 0 &&
            intentWithoutPkg.getExtras().containsKey(ItemInfo.EXTRA_PROFILE) &&
            intentWithoutPkg.getExtras().getLong(ItemInfo.EXTRA_PROFILE) != 0) {
            // The first user's EXTRA_PROFILE value is marked as 0, while Profile user is not.
            // Apps belonging to Profile user should be displayed on homescreen for VIBEUI mode.
            return false;
        }
        /* Lenovo-sw luyy1 add for Android FW 2015-10-23 END (KOLEOSROW-3226)*/

        Intent intentWithPkg_ = intentWithPkg;
        /** Lenovo-SW zhaoxin5 20151021 KOLEOSROW-3180 START */
		if (null != intentWithPkg_.getExtras()) {
			if (intentWithPkg_.getExtras().containsKey(ItemInfo.EXTRA_PROFILE)) {
				intentWithPkg_.removeExtra(ItemInfo.EXTRA_PROFILE);
			}
		}

        Intent intentWithoutPkg_ = intentWithoutPkg;
		if (null != intentWithoutPkg_.getExtras()) {
			if (intentWithoutPkg_.getExtras().containsKey(
					ItemInfo.EXTRA_PROFILE)) {
				intentWithoutPkg_.removeExtra(ItemInfo.EXTRA_PROFILE);
			}
		}
        /** Lenovo-SW zhaoxin5 20151021 KOLEOSROW-3180 END */
        c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { "itemType", "intent", "for2layerWorkspace"}, "itemType=? and (intent=? or intent=?) and for2layerWorkspace=?",
                new String[] { String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_APPLICATION), intentWithPkg_.toUri(0), intentWithoutPkg_.toUri(0), String.valueOf(for2layerWorkspace) }, null);
        // change end xutg1, support to switch between single layer & two layers
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }

    	LauncherLog.i(TAG, "shortcutExistsForApp 2 : result:" + result + ", " + intentWithPkg);
        return result;
    }

    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExistsForShortcut(Context context, String title, final Intent intentWithPkg, final Intent intentWithoutPkg) {
        final ContentResolver cr = context.getContentResolver();

        // 1) 
        // 先检查存在如下intent的情况
        // #Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.android.chrome/com.google.android.apps.chrome.Main;l.profile=0;end
        // 可看到上面intent的最后有一个Extra:l.profile=0

        // changed by xutg1, support to switch between single layer & two layers
        int for2layerWorkspace = LauncherAppState.getInstance().getCurrentLayoutInt();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { "itemType", "title", "intent", "for2layerWorkspace"}, "itemType= ? and title=? and (intent=? or intent=?) and for2layerWorkspace=?",
                new String[] { String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT), title, intentWithPkg.toUri(0), intentWithoutPkg.toUri(0), String.valueOf(for2layerWorkspace) }, null);
        // change end xutg1, support to switch between single layer & two layers
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }

        if (result == true) {
        	// 找到直接返回true
        	LauncherLog.i(TAG, "shortcutExistsForShortcut 1 : " + intentWithPkg);
        	return true;
        }

        // 2) 
        // 再检查去掉Extra:l.profile=0
        // 继续查找
        Intent intentWithPkg_ = intentWithPkg;
        /** Lenovo-SW zhaoxin5 20151021 KOLEOSROW-3180 START */
		if (null != intentWithPkg_.getExtras()) {
			if (intentWithPkg_.getExtras().containsKey(ItemInfo.EXTRA_PROFILE)) {
				intentWithPkg_.removeExtra(ItemInfo.EXTRA_PROFILE);
			}
		}

        Intent intentWithoutPkg_ = intentWithoutPkg;
        if (null != intentWithoutPkg_.getExtras()) {
	        if (intentWithoutPkg_.getExtras().containsKey(ItemInfo.EXTRA_PROFILE)) {
	        	intentWithoutPkg_.removeExtra(ItemInfo.EXTRA_PROFILE);
	        }
        }
        /** Lenovo-SW zhaoxin5 20151021 KOLEOSROW-3180 END */
        c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { "itemType", "title", "intent", "for2layerWorkspace"}, "itemType= ? and title=? and (intent=? or intent=?) and for2layerWorkspace=?",
                new String[] { String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT), title, intentWithPkg_.toUri(0), intentWithoutPkg_.toUri(0), String.valueOf(for2layerWorkspace) }, null);
        // change end xutg1, support to switch between single layer & two layers

        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }

    	LauncherLog.i(TAG, "shortcutExistsForShortcut 2 : result:" + result + ", " + intentWithPkg);
        return result;
    }
    /** Lenovo-SW zhaoxin5 20151014 PASSIONROW-2195	END */

    /**
     * Returns true if the promise shortcuts with the same package name exists on the workspace.
     */
    static boolean appWasPromise(Context context, Intent intent, UserHandleCompat user) {
        final ComponentName component = intent.getComponent();
        if (component == null) {
            return false;
        }
        return !getItemsByPackageName(component.getPackageName(), user).isEmpty();
    }

    /**
     * Returns an ItemInfo array containing all the items in the LauncherModel.
     * The ItemInfo.id is not set through this function.
     */
    static ArrayList<ItemInfo> getItemsInLocalCoordinates(Context context) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
                LauncherSettings.Favorites.ITEM_TYPE, LauncherSettings.Favorites.CONTAINER,
                LauncherSettings.Favorites.SCREEN,
                LauncherSettings.Favorites.CELLX, LauncherSettings.Favorites.CELLY,
                LauncherSettings.Favorites.SPANX, LauncherSettings.Favorites.SPANY,
                LauncherSettings.Favorites.PROFILE_ID,
                /* Lenovo-SW zhaoxin5 20150515 add for 2Layer switch */
                LauncherSettings.Favorites.FOR_2LAYER_WORKSPACE}, null, null, null);
        		/* Lenovo-SW zhaoxin5 20150515 add for 2Layer switch */

        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
        final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
        final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
        final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
        final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
        final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
        final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);
        final int profileIdIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PROFILE_ID);
        /* Lenovo-SW zhaoxin5 20150515 add for 2Layer switch */
        final int for2LayerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.FOR_2LAYER_WORKSPACE);
        /* Lenovo-SW zhaoxin5 20150515 add for 2Layer switch */
        UserManagerCompat userManager = UserManagerCompat.getInstance(context);
        try {
            while (c.moveToNext()) {
                /* Lenovo-SW zhaoxin5 20150515 add for 2Layer switch */
                if(LauncherAppState.getInstance().getCurrentLayoutInt() != c.getInt(for2LayerIndex)){
                	continue;
                }
                /* Lenovo-SW zhaoxin5 20150515 add for 2Layer switch */
                ItemInfo item = new ItemInfo();
                item.cellX = c.getInt(cellXIndex);
                item.cellY = c.getInt(cellYIndex);
                item.spanX = Math.max(1, c.getInt(spanXIndex));
                item.spanY = Math.max(1, c.getInt(spanYIndex));
                item.container = c.getInt(containerIndex);
                item.itemType = c.getInt(itemTypeIndex);
                item.screenId = c.getInt(screenIndex);
                long serialNumber = c.getInt(profileIdIndex);
                item.user = userManager.getUserForSerialNumber(serialNumber);
                // Skip if user has been deleted.
                if (item.user != null) {
                    items.add(item);
                }
            }
        } catch (Exception e) {
            items.clear();
        } finally {
            c.close();
        }

        return items;
    }

    /**
     * Find a folder in the db, creating the FolderInfo if necessary, and adding it to folderList.
     */
    FolderInfo getFolderById(Context context, HashMap<Long,FolderInfo> folderList, long id) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
                "_id=? and (itemType=? or itemType=?)",
                new String[] { String.valueOf(id),
                        String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_FOLDER)}, null);

        try {
            if (c.moveToFirst()) {
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
                final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
                final int hiddenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.HIDDEN);

                FolderInfo folderInfo = null;
                switch (c.getInt(itemTypeIndex)) {
                    case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                        folderInfo = findOrMakeFolder(folderList, id);
                        break;
                }

                folderInfo.title = c.getString(titleIndex);
                folderInfo.id = id;
                folderInfo.container = c.getInt(containerIndex);
                folderInfo.screenId = c.getInt(screenIndex);
                folderInfo.cellX = c.getInt(cellXIndex);
                folderInfo.cellY = c.getInt(cellYIndex);
                folderInfo.hidden = c.getInt(hiddenIndex) > 0;

                return folderInfo;
            }
        } finally {
            c.close();
        }

        return null;
    }

    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     * 将一个item放到数据库里面
     */
    static void addItemToDatabase(Context context, final ItemInfo item, final long container,
            final long screenId, final int cellX, final int cellY, final boolean notify) {
        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;
        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        // hotseat的item位置是方向无关的
        if (context instanceof Launcher && screenId < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screenId = screenId;
        }

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        item.onAddToDatabase(context, values);

        item.id = LauncherAppState.getLauncherProvider().generateNewItemId();
        values.put(LauncherSettings.Favorites._ID, item.id);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);

        // added by xutg1, support to switch workspaces between 1 layer and 2 layers
        values.put(LauncherSettings.Favorites.FOR_2LAYER_WORKSPACE, LauncherAppState.getInstance().getCurrentLayoutInt());
        // add end, xutg1, support to switch workspaces between 1 layer and 2 layers

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Runnable r = new Runnable() {
            public void run() {
                cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
                        LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    checkItemInfoLocked(item.id, item, stackTrace);
                    sBgItemsIdMap.put(item.id, item);
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                            sBgFolders.put(item.id, (FolderInfo) item);
                            // Fall through
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                                    item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                                sBgWorkspaceItems.add(item);
                            } else {
                                if (!sBgFolders.containsKey(item.container)) {
                                    // Adding an item to a folder that doesn't exist.
                                    String msg = "adding item: " + item + " to a folder that " +
                                            " doesn't exist";
                                    Log.e(TAG, msg);
                                }
                            }
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            sBgAppWidgets.add((LauncherAppWidgetInfo) item);
                            break;
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Creates a new unique child id, for a given cell span across all layouts.
     * 创建一个独一无二的id——根据位置
     */
    static int getCellLayoutChildId(
            long container, long screen, int localCellX, int localCellY, int spanX, int spanY) {
        return (((int) container & 0xFF) << 24)
                | ((int) screen & 0xFF) << 16 | (localCellX & 0xFF) << 8 | (localCellY & 0xFF);
    }

    private static ArrayList<ItemInfo> getItemsByPackageName(
            final String pn, final UserHandleCompat user) {
        ItemInfoFilter filter  = new ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn) {
                return cn.getPackageName().equals(pn) && info.user.equals(user);
            }
        };
        return filterItemInfos(sBgItemsIdMap.values(), filter);
    }

    /**
     * Removes all the items from the database corresponding to the specified package.
     */
    static void deletePackageFromDatabase(Context context, final String pn,
            final UserHandleCompat user) {
        deleteItemsFromDatabase(context, getItemsByPackageName(pn, user));
    }

    /**
     * Removes the specified item from the database
     * @param context
     * @param item
     */
    static void deleteItemFromDatabase(Context context, final ItemInfo item) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        items.add(item);
        deleteItemsFromDatabase(context, items);
    }

    /**
     * Removes the specified items from the database
     * @param context
     * @param item
     */
    static void deleteItemsFromDatabase(Context context, final ArrayList<ItemInfo> items) {
        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                for (ItemInfo item : items) {
                    final Uri uri = LauncherSettings.Favorites.getContentUri(item.id, false);
                    cr.delete(uri, null, null);

                    // Lock on mBgLock *after* the db operation
                    synchronized (sBgLock) {
                        switch (item.itemType) {
                            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                                sBgFolders.remove(item.id);
                                for (ItemInfo info: sBgItemsIdMap.values()) {
                                    if (info.container == item.id) {
                                        // We are deleting a folder which still contains items that
                                        // think they are contained by that folder.
                                        String msg = "deleting a folder (" + item + ") which still " +
                                                "contains items (" + info + ")";
                                        Log.e(TAG, msg);
                                    }
                                }
                                sBgWorkspaceItems.remove(item);
                                break;
                            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                                sBgWorkspaceItems.remove(item);
                                break;
                            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                                sBgAppWidgets.remove((LauncherAppWidgetInfo) item);
                                break;
                        }
                        sBgItemsIdMap.remove(item.id);
                        sBgDbIconCache.remove(item);
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Update the order of the workspace screens in the database. The array list contains
     * a list of screen ids in the order that they should appear.
     */
    void updateWorkspaceScreenOrder(Context context, final ArrayList<Long> screens) {
        // Log to disk
        Launcher.addDumpLog(TAG, "11683562 - updateWorkspaceScreenOrder()", true);
        Launcher.addDumpLog(TAG, "11683562 -   screens: " + TextUtils.join(", ", screens), true);
        final ArrayList<Long> screensCopy = new ArrayList<Long>(screens);
        final ContentResolver cr = context.getContentResolver();
        final Uri uri = LauncherSettings.WorkspaceScreens.CONTENT_URI;

        // Remove any negative screen ids -- these aren't persisted
        Iterator<Long> iter = screensCopy.iterator();
        while (iter.hasNext()) {
            long id = iter.next();
            if (id < 0) {
                iter.remove();
            }
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                // changed by xutg1, support to switch workspaces between 1 layer and 2 layers
                // Clear the table
                int for2layerWorkspace = LauncherAppState.getInstance().getCurrentLayoutInt();
                ops.add(ContentProviderOperation.newDelete(uri).
                        withSelection(LauncherSettings.WorkspaceScreens.SCREEN_FOR_2LAYER_WORKSPACE + "=?", new String[]{String.valueOf(for2layerWorkspace)}).
                        build());
                int count = screensCopy.size();
                for (int i = 0; i < count; i++) {
                    ContentValues v = new ContentValues();
                    long screenId = screensCopy.get(i);
                    v.put(LauncherSettings.WorkspaceScreens._ID, screenId);
                    v.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK, i);
                    v.put(LauncherSettings.WorkspaceScreens.SCREEN_FOR_2LAYER_WORKSPACE, for2layerWorkspace);
                    ops.add(ContentProviderOperation.newInsert(uri).withValues(v).build());
                }
                // change end, xutg1, support to switch workspaces between 1 layer and 2 layers
                try {
                    cr.applyBatch(LauncherProvider.AUTHORITY, ops);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                synchronized (sBgLock) {
                    sBgWorkspaceScreens.clear();
                    sBgWorkspaceScreens.addAll(screensCopy);
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Remove the contents of the specified folder from the database
     */
    static void deleteFolderContentsFromDatabase(Context context, final FolderInfo info) {
        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                cr.delete(LauncherSettings.Favorites.getContentUri(info.id, false), null, null);
                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    sBgItemsIdMap.remove(info.id);
                    sBgFolders.remove(info.id);
                    sBgDbIconCache.remove(info);
                    sBgWorkspaceItems.remove(info);
                }

                cr.delete(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
                        LauncherSettings.Favorites.CONTAINER + "=" + info.id, null);
                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    for (ItemInfo childInfo : info.contents) {
                        sBgItemsIdMap.remove(childInfo.id);
                        sBgDbIconCache.remove(childInfo);
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    @Override
    public void onPackageChanged(String packageName, UserHandleCompat user) {
        int op = PackageUpdatedTask.OP_UPDATE;
        enqueuePackageUpdated(new PackageUpdatedTask(op, new String[] { packageName },
                user));
        /* Lenovo-sw luyy1 add to fix XTHREEROW-2059 2015-09-18
         * If com.lenovo.ideawallpaper is disabled or enabled, then reload the apk image files.*/
        if (mCallbacks != null && TextUtils.equals(packageName, "com.lenovo.ideawallpaper")) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                callbacks.reloadApkWallpaper();
            }
        }
        /* Lenovo-sw luyy1 add to fix XTHREEROW-2059 2015-09-18*/
    }

    @Override
    public void onPackageRemoved(String packageName, UserHandleCompat user) {
        int op = PackageUpdatedTask.OP_REMOVE;
        enqueuePackageUpdated(new PackageUpdatedTask(op, new String[] { packageName },
                user));
    }

    @Override
    public void onPackageAdded(String packageName, UserHandleCompat user) {
        if (isFilterPackage(packageName)) {
            return;
        }
        int op = PackageUpdatedTask.OP_ADD;
        enqueuePackageUpdated(new PackageUpdatedTask(op, new String[] { packageName },
                user));
    }

    @Override
    public void onPackagesAvailable(String[] packageNames, UserHandleCompat user,
            boolean replacing) {
        if (!replacing) {
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packageNames,
                    user));
            if (mAppsCanBeOnRemoveableStorage) {
                // Only rebind if we support removable storage. It catches the
                // case where
                // apps on the external sd card need to be reloaded
                startLoaderFromBackground();
            }
        } else {
            // If we are replacing then just update the packages in the list
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_UPDATE,
                    packageNames, user));
        }
    }

    @Override
    public void onPackagesUnavailable(String[] packageNames, UserHandleCompat user,
            boolean replacing) {
        if (!replacing) {
            enqueuePackageUpdated(new PackageUpdatedTask(
                    PackageUpdatedTask.OP_UNAVAILABLE, packageNames,
                    user));
        }

    }

    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
     * ACTION_PACKAGE_CHANGED.
     * 监听程序安装、卸载、改变。
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG_RECEIVER) Log.d(TAG, "onReceive intent=" + intent);

        final String action = intent.getAction();
        if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            // If we have changed locale we need to clear out the labels in all apps/workspace.
            forceReload("ACTION_LOCALE_CHANGED");
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
             // Lenovo-sw:yuanyl2, Add edit mode function. Begin
             Launcher launcher = getLauncherInstance();
             if (null!=launcher && launcher.getDockView()!=null) {
                 launcher.getDockView().exitByReload();
             }
             // Lenovo-sw:yuanyl2, Add edit mode function. End

            // Check if configuration change was an mcc/mnc change which would affect app resources
             // and we would need to clear out the labels in all apps/workspace. Same handling as
             // above for ACTION_LOCALE_CHANGED
             Configuration currentConfig = context.getResources().getConfiguration();
             if (mPreviousConfigMcc != currentConfig.mcc) {
                   Log.d(TAG, "Reload apps on config change. curr_mcc:"
                       + currentConfig.mcc + " prevmcc:" + mPreviousConfigMcc);
                   /** Lenovo-SW zhaoxin5 20150831 KOLEOSROW-1391 XTHREEROW-983 START
                    * 参考6.5国内Launcher,不对mcc进行处理
                    * */
                   // forceReload();
                   /** Lenovo-SW zhaoxin5 20150831 KOLEOSROW-1391 XTHREEROW-983 END */
             }
             // Update previousConfig
             mPreviousConfigMcc = currentConfig.mcc;
        } else if (SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED.equals(action) ||
                   SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED.equals(action)) {
            if (mCallbacks != null) {
                Callbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
                    callbacks.bindSearchablesChanged();
                }
            }
        } else if (ACTION_UNREAD_CHANGED.equals(action)) {
            Log.d("YUANYL2", ACTION_UNREAD_CHANGED);
            ComponentName componentName = intent.getParcelableExtra("component_name");
            int unreadNum = intent.getIntExtra("unread_number", 0);

            if (componentName == null) return;

            Log.d("YUANYL2", ACTION_UNREAD_CHANGED + " - " + componentName + " - " + unreadNum);
            synchronized (unreadChangedMap) {
                unreadChangedMap.put(componentName, new UnreadInfo(componentName, unreadNum));
            }
            sWorker.removeCallbacks(mUnreadUpdateTask);
            sWorker.post(mUnreadUpdateTask);
          /*Lenovo-sw zhangyj19 add 2015/07/22 modify theme start*/
        } else if (ACTION_THEME_CHANGED.equals(action)) {
            forceReloadForThemeApply();
            /*Lenovo-sw zhangyj19 add 2015/07/22 modify theme end*/
            /*Lenovo-sw luyy1 add 20150729 for Yandex search start*/
        } else if (ACTION_SEARCH_CHANGED.equals(action)) {
            sWorker.removeCallbacks(mSearchUpdateTask);
            sWorker.post(mSearchUpdateTask);
            /*Lenovo-sw luyy1 add 20150729 for Yandex search end*/
            /*Lenovo-sw luyy1 add 20150819 for wallpaper listener start*/
        } else if (ACTION_THEME_WALLPAPER_CHANGED.equals(action)) {
            if (mCallbacks != null) {
                Callbacks callbacks = mCallbacks.get();
                mWallpaperResId = intent.getIntExtra("resid", WALLPAPER_OTHER_THEME_SOURCE);
            }
        } else if (Intent.ACTION_WALLPAPER_CHANGED.equals(action)) {
            sWorker.removeCallbacks(mWallpaperImageUpdateTask);
            sWorker.postDelayed(mWallpaperImageUpdateTask, 1*1000);
        }
        /*Lenovo-sw luyy1 add 20150819 for wallpaper listener end*/
    }

    void forceReload(String from) {
    	LauncherLog.i(TAG, "forceReload from : " + from);
        resetLoadedState(true, true);

        // Do this here because if the launcher activity is running it will be restarted.
        // If it's not running startLoaderFromBackground will merely tell it that it needs
        // to reload.
        startLoaderFromBackground();
    }

    public void resetLoadedState(boolean resetAllAppsLoaded, boolean resetWorkspaceLoaded) {
        synchronized (mLock) {
            // Stop any existing loaders first, so they don't set mAllAppsLoaded or
            // mWorkspaceLoaded to true later
            stopLoaderLocked();
            if (resetAllAppsLoaded) mAllAppsLoaded = false;
            if (resetWorkspaceLoaded) mWorkspaceLoaded = false;
        }
    }

    /**
     * When the launcher is in the background, it's possible for it to miss paired
     * configuration changes.  So whenever we trigger the loader from the background
     * tell the launcher that it needs to re-run the loader when it comes back instead
     * of doing it now.
     */
    /* Lenovo-SW zhaoxin5 20150528 add for 2 layer support */
    public void startLoaderFromBackground(int loadFlags) {
        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                // Only actually run the loader if they're not paused.
                /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success START */
                if (!callbacks.setLoadOnResume(loadFlags)) {
                    runLoader = true;
                }
                /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success END */
            }
        }
        if (runLoader) {
            startLoader(false, PagedView.INVALID_RESTORE_PAGE, loadFlags);
        }
    }
    /* Lenovo-SW zhaoxin5 20150528 add for 2 layer support */

    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                // Only actually run the loader if they're not paused.
                /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success START */
                if (!callbacks.setLoadOnResume(Integer.MIN_VALUE)) {
                    runLoader = true;
                }
                /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success END */
            }
        }
        if (runLoader) {
            startLoader(false, PagedView.INVALID_RESTORE_PAGE);
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    public boolean isCurrentCallbacks(Callbacks callbacks) {
        return (mCallbacks != null && mCallbacks.get() == callbacks);
    }

    public void startLoader(boolean isLaunching, int synchronousBindPage) {
        startLoader(isLaunching, synchronousBindPage, LOADER_FLAG_NONE);
    }

    public void startLoader(boolean isLaunching, int synchronousBindPage, int loadFlags) {
        synchronized (mLock) {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "startLoader isLaunching=" + isLaunching);
            }

            // Clear any deferred bind-runnables from the synchronized load process
            // We must do this before any loading/binding is scheduled below.
            synchronized (mDeferredBindRunnables) {
                mDeferredBindRunnables.clear();
            }

            // Don't bother to start the thread if we know it's not going to do anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop.
                // also, don't downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(mApp.getContext(), isLaunching, loadFlags);
                if (synchronousBindPage != PagedView.INVALID_RESTORE_PAGE
                        && mAllAppsLoaded && mWorkspaceLoaded) {
                    mLoaderTask.runBindSynchronousPage(synchronousBindPage);
                } else {
                    sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                    sWorker.post(mLoaderTask);
                }
            }
        }
    }

    void bindRemainingSynchronousPages() {
        // Post the remaining side pages to be loaded
        if (!mDeferredBindRunnables.isEmpty()) {
            Runnable[] deferredBindRunnables = null;
            synchronized (mDeferredBindRunnables) {
                deferredBindRunnables = mDeferredBindRunnables.toArray(
                        new Runnable[mDeferredBindRunnables.size()]);
                mDeferredBindRunnables.clear();
            }
            for (final Runnable r : deferredBindRunnables) {
                mHandler.post(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }
    }

    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                mLoaderTask.stopLocked();
            }
        }
    }

    /** Loads the workspace screens db into a map of Rank -> ScreenId */
    private static TreeMap<Integer, Long> loadWorkspaceScreensDb(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri screensUri = LauncherSettings.WorkspaceScreens.CONTENT_URI;
        final Cursor sc = contentResolver.query(screensUri, null, null, null, null);
        TreeMap<Integer, Long> orderedScreens = new TreeMap<Integer, Long>();

        try {
            final int idIndex = sc.getColumnIndexOrThrow(
                    LauncherSettings.WorkspaceScreens._ID);
            final int rankIndex = sc.getColumnIndexOrThrow(
                    LauncherSettings.WorkspaceScreens.SCREEN_RANK);

            // added by xutg1, support to switch workspaces between 1 layer and 2 layers
            final int for2LayerWorkspace = sc.getColumnIndexOrThrow(
                    LauncherSettings.WorkspaceScreens.SCREEN_FOR_2LAYER_WORKSPACE);
            // add end, xutg1, support to switch workspaces between 1 layer and 2 layers

            while (sc.moveToNext()) {
                try {
                    long screenId = sc.getLong(idIndex);
                    int rank = sc.getInt(rankIndex);

                    // added by xutg1, support to switch workspaces between 1 layer and 2 layers
                    /*boolean isFor2LayerWorkspace = 0 != sc.getInt(for2LayerWorkspace);
                    // skip if the workspace is not for the current layer solution(1 or 2)
                    if (isFor2LayerWorkspace == LauncherAppState.isDisableAllApps())
                    	continue;*/
                    if(LauncherAppState.getInstance().getCurrentLayoutInt() != sc.getInt(for2LayerWorkspace)){
                    	continue;
                    }
                    // add end, xutg1, support to switch workspaces between 1 layer and 2 layers

                    orderedScreens.put(rank, screenId);
                } catch (Exception e) {
                    Launcher.addDumpLog(TAG, "Desktop items loading interrupted - invalid screens: " + e, true);
                }
            }
        } finally {
            sc.close();
        }

        // Log to disk
        Launcher.addDumpLog(TAG, "11683562 - loadWorkspaceScreensDb()", true);
        ArrayList<String> orderedScreensPairs= new ArrayList<String>();
        for (Integer i : orderedScreens.keySet()) {
            orderedScreensPairs.add("{ " + i + ": " + orderedScreens.get(i) + " }");
        }
        Launcher.addDumpLog(TAG, "11683562 -   screens: " +
                TextUtils.join(", ", orderedScreensPairs), true);
        return orderedScreens;
    }

    public boolean isAllAppsLoaded() {
        return mAllAppsLoaded;
    }

    boolean isLoadingWorkspace() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                return mLoaderTask.isLoadingWorkspace();
            }
        }
        return false;
    }

    // LENOVO:yuanyl2 : Add for Multi-Language folder supported BEGIN
    private void updateFolderTitle(final Context context) {
        // Update pre-set folder titles after language changed.

        LauncherLog.d(TAG, "updateFolder");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String country = context.getResources().getConfiguration().locale.toString();
                LauncherLog.d(TAG, "country = " + country);
                if (null != LauncherAppState.getLauncherProvider()) {
                    LauncherAppState.getLauncherProvider().updateFoldTitles(null, country);
                } else {
                    LauncherLog.d(TAG, "updateFolder - app.getLauncherProvider()=null");
                }
            }
        };
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }
    // LENOVO:YUANYL2 : Add for Multi-Language folder supported End

    /**
     * Runnable for the thread that loads the contents of the launcher:
     *   - workspace icons
     *   - widgets
     *   - all apps icons
     */
    private class LoaderTask implements Runnable {
        private Context mContext;
        private boolean mIsLaunching;
        private boolean mIsLoadingAndBindingWorkspace;
        private boolean mStopped;
        private boolean mLoadAndBindStepFinished;
        private int mFlags;

        private HashMap<Object, CharSequence> mLabelCache;

        LoaderTask(Context context, boolean isLaunching, int flags) {
            mContext = context;
            mIsLaunching = isLaunching;
            mLabelCache = new HashMap<Object, CharSequence>();
            mFlags = flags;
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        boolean isLoadingWorkspace() {
            return mIsLoadingAndBindingWorkspace;
        }

        /** Returns whether this is an upgrade path */
        private boolean loadAndBindWorkspace() {
            mIsLoadingAndBindingWorkspace = true;

            // Load the workspace
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindWorkspace mWorkspaceLoaded=" + mWorkspaceLoaded);
            }

            boolean isUpgradePath = false;
            if (!mWorkspaceLoaded) {
                //遍历数据库 生成ItemInfo 获取数据
                isUpgradePath = loadWorkspace();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return isUpgradePath;
                    }
                    mWorkspaceLoaded = true;
                }
            }

            // Bind the workspace 绑定数据
            bindWorkspace(-1, isUpgradePath);
            return isUpgradePath;
        }

        private void waitForIdle() {
            // Wait until the either we're stopped or the other threads are done.
            // This way we don't start loading all apps until the workspace has settled
            // down.
            synchronized (LoaderTask.this) {
                final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                mHandler.postIdle(new Runnable() {
                        public void run() {
                            synchronized (LoaderTask.this) {
                                mLoadAndBindStepFinished = true;
                                if (DEBUG_LOADERS) {
                                    Log.d(TAG, "done with previous binding step");
                                }
                                LoaderTask.this.notify();
                            }
                        }
                    });

                while (!mStopped && !mLoadAndBindStepFinished && !mFlushingWorkerThread) {
                    try {
                        // Just in case mFlushingWorkerThread changes but we aren't woken up,
                        // wait no longer than 1sec at a time
                        this.wait(1000);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "waited "
                            + (SystemClock.uptimeMillis()-workspaceWaitTime)
                            + "ms for previous step to finish binding");
                }
            }
        }

        void runBindSynchronousPage(int synchronousBindPage) {
            if (synchronousBindPage == PagedView.INVALID_RESTORE_PAGE) {
                // Ensure that we have a valid page index to load synchronously
                throw new RuntimeException("Should not call runBindSynchronousPage() without " +
                        "valid page index");
            }
            if (!mAllAppsLoaded || !mWorkspaceLoaded) {
                // Ensure that we don't try and bind a specified page when the pages have not been
                // loaded already (we should load everything asynchronously in that case)
                throw new RuntimeException("Expecting AllApps and Workspace to be loaded");
            }
            synchronized (mLock) {
                if (mIsLoaderTaskRunning) {
                    // Ensure that we are never running the background loading at this point since
                    // we also touch the background collections
                    throw new RuntimeException("Error! Background loading is already running");
                }
            }

            // XXX: Throw an exception if we are already loading (since we touch the worker thread
            //      data structures, we can't allow any other thread to touch that data, but because
            //      this call is synchronous, we can get away with not locking).

            // The LauncherModel is static in the LauncherAppState and mHandler may have queued
            // operations from the previous activity.  We need to ensure that all queued operations
            // are executed before any synchronous binding work is done.
            mHandler.flush();

            // Divide the set of loaded items into those that we are binding synchronously, and
            // everything else that is to be bound normally (asynchronously).
            bindWorkspace(synchronousBindPage, false);
            // XXX: For now, continue posting the binding of AllApps as there are other issues that
            //      arise from that.
            onlyBindAllApps();

            // LENOVO-SW:YUANYL2, Add loading progress function. Begin
            if (!mStopped) {
                final Callbacks oldCallbacks = mCallbacks.get();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null && !mStopped) {
                            callbacks.afterBinding();
                        }
                    }
                });
            }
            // LENOVO-SW:YUANYL2, Add loading progress function. End
        }

        public void run() {
            boolean isUpgrade = false;

            synchronized (mLock) {
                mIsLoaderTaskRunning = true;
            }

            // LENOVO-SW:YUANYL2, Add loading progress function. Begin
            //显示加载progress
            final Callbacks cbk = mCallbacks.get();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(cbk);
                    if (callbacks != null) {
                        callbacks.beforeBinding();
                    }
                }
            });
            // LENOVO-SW:YUANYL2, Add loading progress function. End
            /*Lenovo-sw zhangyj19 add 2015/07/22 modify theme start*/
            //加载主题
            Callbacks callbacks = tryGetCallbacks(cbk);
            if (callbacks != null) {
                Log.w(TAG, "loadResource");
                mCallbacks.get().loadResource();
            }
            /*Lenovo-sw zhangyj19 add 2015/07/22 modify theme end*/

            /** Lenovo-SW zhaoxin5 20150902 KOLEOSROW-1643 KOLEOSROW-1447 START */
            callbacks = tryGetCallbacks(cbk);
            if (callbacks != null) {
                Log.d(TAG, "loadSettingsAndFolderTitlesFromLbk");
                mCallbacks.get().loadSettingsAndFolderTitlesFromLbk();
            }
            /** Lenovo-SW zhaoxin5 20150902 KOLEOSROW-1643 KOLEOSROW-1447 END */

            // Optimize for end-user experience: if the Launcher is up and // running with the
            // All Apps interface in the foreground, load All Apps first. Otherwise, load the
            // workspace first (default).
            keep_running: {
                // Elevate priority when Home launches for the first time to avoid
                // starving at boot time. Staring at a blank home is not cool.
                synchronized (mLock) {
                    if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to " +
                            (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
                    android.os.Process.setThreadPriority(mIsLaunching
                            ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                }

                if (DEBUG_LOADERS) Log.d(TAG, "step 1: loading workspace");
                //root
                //Thread.dumpStack();
                isUpgrade = loadAndBindWorkspace();

                if (mStopped) {
                    break keep_running;
                }

                // Whew! Hard work done.  Slow us down, and wait until the UI thread has
                // settled down.
                synchronized (mLock) {
                    if (mIsLaunching) {
                        if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to BACKGROUND");
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }
                waitForIdle();

                // second step
                if (DEBUG_LOADERS) Log.d(TAG, "step 2: loading all apps");
                loadAndBindAllApps();

                // Restore the default thread priority after we are done loading items
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }

            // Update the saved icons if necessary
            if (DEBUG_LOADERS) Log.d(TAG, "Comparing loaded icons to database icons");
            synchronized (sBgLock) {
                for (Object key : sBgDbIconCache.keySet()) {
                    updateSavedIcon(mContext, (ShortcutInfo) key, sBgDbIconCache.get(key));
                }
                sBgDbIconCache.clear();
            }

        	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
            if (LauncherAppState.isDisableAllApps() && mLoaderTaskEqualsThis()) {
                // Ensure that all the applications that are in the system are
                // represented on the home screen.
                if (!UPGRADE_USE_MORE_APPS_FOLDER || !isUpgrade) {
                    verifyApplications();
                }
            }
        	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */

            // LENOVO-SW:YUANYL2, Add loading progress function. Begin
            if (!mStopped) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(cbk);
                        if (callbacks != null && !mStopped) {
                            callbacks.afterBinding();
                        }
                    }
                });
            }
            // LENOVO-SW:YUANYL2, Add loading progress function. End

            // Clear out this reference, otherwise we end up holding it until all of the
            // callback runnables are done.
            mContext = null;

            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
                mIsLoaderTaskRunning = false;
            }
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        /**
         * Gets the callbacks object.  If we've been stopped, or if the launcher object
         * has somehow been garbage collected, return null instead.  Pass in the Callbacks
         * object that was around when the deferred message was scheduled, and if there's
         * a new Callbacks object around then also return null.  This will save us from
         * calling onto it with data that will be ignored.
         */
        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final Callbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    Log.w(TAG, "no mCallbacks");
                    return null;
                }

                return callbacks;
            }
        }

        private void verifyApplications() {
            final Context context = mApp.getContext();

            // Cross reference all the applications in our apps list with items in the workspace
            ArrayList<ItemInfo> tmpInfos;
            ArrayList<ItemInfo> added = new ArrayList<ItemInfo>();
            synchronized (sBgLock) {
                for (AppInfo app : mBgAllAppsList.data) {
                	/** Lenovo-SW zhaoxin5 20150626 not show LenovoLauncher in the workspace START */
                    //过滤 app
                	if(isFilterPackage(app.componentName.getPackageName())) {
                		continue;
                	}
                	/** Lenovo-SW zhaoxin5 20150626 not show LenovoLauncher in the workspace END */
                    tmpInfos = getItemInfoForComponentName(app.componentName, app.user);
                    if (tmpInfos.isEmpty()) {
                        // We are missing an application icon, so add this to the workspace
                        added.add(app);
                        // This is a rare event, so lets log it
                        Log.e(TAG, "Missing Application on load: " + app);
                    }
                }
            }
        	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
            if (!added.isEmpty() && mLoaderTaskEqualsThis()) {
                addAndBindAddedWorkspaceApps(context, added);
            }
        	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */
        }

        // check & update map of what's occupied; used to discard overlapping/invalid items
        private boolean checkItemPlacement(HashMap<Long, ItemInfo[][]> occupied, ItemInfo item,
                                           AtomicBoolean deleteOnInvalidPlacement) {
            LauncherAppState app = LauncherAppState.getInstance();
            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            final int countX = (int) grid.numColumns;
            final int countY = (int) grid.numRows;

            long containerIndex = item.screenId;
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                // Return early if we detect that an item is under the hotseat button
                if (mCallbacks == null ||
                        mCallbacks.get().isAllAppsButtonRank((int) item.screenId)) {
                    deleteOnInvalidPlacement.set(true);
                    Log.e(TAG, "Error loading shortcut into hotseat " + item
                            + " into position (" + item.screenId + ":" + item.cellX + ","
                            + item.cellY + ") occupied by all apps");
                    return false;
                }

                final ItemInfo[][] hotseatItems =
                        occupied.get((long) LauncherSettings.Favorites.CONTAINER_HOTSEAT);

                if (item.screenId >= grid.numHotseatIcons) {
                    Log.e(TAG, "Error loading shortcut " + item
                            + " into hotseat position " + item.screenId
                            + ", position out of bounds: (0 to " + (grid.numHotseatIcons - 1)
                            + ")");
                    return false;
                }

                if (hotseatItems != null) {
                    if (hotseatItems[(int) item.screenId][0] != null) {
                        Log.e(TAG, "Error loading shortcut into hotseat " + item
                                + " into position (" + item.screenId + ":" + item.cellX + ","
                                + item.cellY + ") occupied by "
                                + occupied.get(LauncherSettings.Favorites.CONTAINER_HOTSEAT)
                                [(int) item.screenId][0]);
                            return false;
                    } else {
                        hotseatItems[(int) item.screenId][0] = item;
                        return true;
                    }
                } else {
                    final ItemInfo[][] items = new ItemInfo[(int) grid.numHotseatIcons][1];
                    items[(int) item.screenId][0] = item;
                    occupied.put((long) LauncherSettings.Favorites.CONTAINER_HOTSEAT, items);
                    return true;
                }
            } else if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                // Skip further checking if it is not the hotseat or workspace container
                return true;
            }

            if (!occupied.containsKey(item.screenId)) {
                ItemInfo[][] items = new ItemInfo[countX + 1][countY + 1];
                occupied.put(item.screenId, items);
            }

            final ItemInfo[][] screens = occupied.get(item.screenId);
            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                    item.cellX < 0 || item.cellY < 0 ||
                    item.cellX + item.spanX > countX || item.cellY + item.spanY > countY) {
                Log.e(TAG, "Error loading shortcut " + item
                        + " into cell (" + containerIndex + "-" + item.screenId + ":"
                        + item.cellX + "," + item.cellY
                        + ") out of screen bounds ( " + countX + "x" + countY + ")");
                return false;
            }

            // Check if any workspace icons overlap with each other
            for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                    if (screens[x][y] != null) {
                        Log.e(TAG, "Error loading shortcut " + item
                            + " into cell (" + containerIndex + "-" + item.screenId + ":"
                            + x + "," + y
                            + ") occupied by "
                            + screens[x][y]);
                        return false;
                    }
                }
            }
            for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                    screens[x][y] = item;
                }
            }

            return true;
        }

    	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
        private boolean mLoaderTaskEqualsThis() {
        	// 判断当前的this和mLoaderTask
        	// 是否是同一个实例
        	boolean rtValue = (this == mLoaderTask);
        	if(!rtValue) {
        		LauncherLog.i(TAG, "this != mLoaderTask", new Throwable());
        	}
        	return rtValue;
        }
    	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */

        /** Clears all the sBg data structures */
        private void clearSBgDataStructures() {
            synchronized (sBgLock) {

            	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
            	LauncherLog.i(TAG, "clearSBgDataStructures this : " + this);
            	LauncherLog.i(TAG, "clearSBgDataStructures mLoaderTask : " + mLoaderTask);

            	if(mLoaderTaskEqualsThis()) {
            		LauncherLog.i(TAG, "clearSBgDataStructures this == mLoaderTask");
            	} else {
            		LauncherLog.i(TAG, "clearSBgDataStructures this != mLoaderTask");
            		// 如果当前LoaderTask和mLoaderTask不一样的话,禁止执行清空操作
            		return;
            	}
            	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */

                sBgWorkspaceItems.clear();
                sBgAppWidgets.clear();
                sBgFolders.clear();
                sBgItemsIdMap.clear();
                sBgDbIconCache.clear();
                sBgWorkspaceScreens.clear();
            }
        }
       /* //快捷方式 包名称
        String[] shortPkgStrs = {"cn.kuwo.player", "com.moji.mjweather", "cn.etouch.ecalendar",
                "com.tencent.qqpimsecure", "com.tencent.android.qqdownloader",
                "com.miguan.market", "com.tencent.qqlive", "com.alex.nuclear", "com.ss.android.article.news", "com.autonavi.minimap"};
        private boolean isContain(String pkgStr) {
            boolean bIsContain = false;
            for (String str : shortPkgStrs) {
                if (pkgStr.equals(str)) {
                    bIsContain = true;
                    break;
                }
            }
            return bIsContain;
        }*/
        /** Returns whether this is an upgrade path */
        //遍历数据库里的每条记录，判断他的类型，
        // 生成对应的ItemInfo对象（ShortcutInfo，FolderInfo，LauncherAppWidgetInfo）
        private boolean loadWorkspace() {
        	LauncherLog.i("xixia", "loadWorkspace");
            // Log to disk
            Launcher.addDumpLog(TAG, "11683562 - loadWorkspace()", true);

            /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 START */
            if(null != mShortcutInfosCachedLoaded) {
            	mShortcutInfosCachedLoaded.clear();
            	mShortcutInfosCachedAdded.clear();
            } else {
            	mShortcutInfosCachedLoaded = new ArrayList<ShortcutInfo>();
            	mShortcutInfosCachedAdded = new ArrayList<ShortcutInfo>();
            }
            LauncherLog.i(TAG, TAG_SDCARD + ", loadWorkspace start mShortcutInfosCached.size : " + mShortcutInfosCachedLoaded.size());
            /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 END */

            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager manager = context.getPackageManager();
            final AppWidgetManager widgets = AppWidgetManager.getInstance(context);
            final boolean isSafeMode = manager.isSafeMode();
            final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
            final boolean isSdCardReady = context.registerReceiver(null,
                    new IntentFilter(StartupReceiver.SYSTEM_READY)) != null;

            LauncherAppState app = LauncherAppState.getInstance();
            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            int countX = (int) grid.numColumns;
            int countY = (int) grid.numRows;

            if ((mFlags & LOADER_FLAG_CLEAR_WORKSPACE) != 0) {
            	LauncherLog.i("xixia", "loadWorkspace 0 : resetting launcher database");
                Launcher.addDumpLog(TAG, "loadWorkspace: resetting launcher database", true);
                LauncherAppState.getLauncherProvider().deleteDatabase();
            }

            /* Lenovo-SW zhaoxin5 add for 2layer support */
            if ((mFlags & LOADER_FLAG_MIGRATE_LENOVO_LAUNCHER) != 0){
            	LauncherLog.i("xixia", "loadWorkspace 1 : load from old lenovo launcher");
                LauncherAppState.getLauncherProvider().migrateLenovoLauncherDesktop();
            }
            /* Lenovo-SW zhaoxin5 add for 2layer support */
            if ((mFlags & LOADER_FLAG_MIGRATE_SHORTCUTS) != 0) {
            	LauncherLog.i("xixia", "loadWorkspace 2 : migrating from launcher2");
                // append the user's Launcher2 shortcuts
                Launcher.addDumpLog(TAG, "loadWorkspace: migrating from launcher2", true);
                LauncherAppState.getLauncherProvider().migrateLauncher2Shortcuts();
            } else {
            	LauncherLog.i("xixia", "loadWorkspace 3 : loading default favorites");
                // Make sure the default workspace is loaded
                Launcher.addDumpLog(TAG, "loadWorkspace: loading default favorites", false);
                /** Lenovo-SW zhaoxin5 20150711 add for recovery from lbk support START */
                //初次加载
                LauncherAppState.getLauncherProvider().loadDefaultFavoritesIfNecessary(mFlags);
                /** Lenovo-SW zhaoxin5 20150711 add for recovery from lbk support END */
            }

            // LENOVO-SW:YUANYL2, Add loading progress function. Begin
            // 前面的全部工作就是查找可能的桌面配置数据并填充到数据库中
            // 后面紧接着就是从数据库中读取所有的桌面数据并显示到Launcher上
            // 因此必须赶在这个前面,更新数据库中文件夹title的信息

            updateFolderTitle(mContext);

            // This code path is for our old migration code and should no longer be exercised
            boolean loadedOldDb = false;

            // Log to disk
            Launcher.addDumpLog(TAG, "11683562 -   loadedOldDb: " + loadedOldDb, true);

            synchronized (sBgLock) {
                clearSBgDataStructures();
                final HashSet<String> installingPkgs = PackageInstallerCompat
                        .getInstance(mContext).updateAndGetActiveSessionCache();

                final ArrayList<Long> itemsToRemove = new ArrayList<Long>();
                final ArrayList<Long> restoredRows = new ArrayList<Long>();
                final Uri contentUri = LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION;
                if (DEBUG_LOADERS) Log.d(TAG, "loading model from " + contentUri);
                final Cursor c = contentResolver.query(contentUri, null, null, null, null);

                // +1 for the hotseat (it can be larger than the workspace)
                // Load workspace in reverse order to ensure that latest items are loaded first (and
                // before any earlier duplicates)
                final HashMap<Long, ItemInfo[][]> occupied = new HashMap<Long, ItemInfo[][]>();

                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                    final int intentIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.INTENT);
                    final int titleIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.TITLE);
                    final int iconTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_TYPE);
                    final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
                    final int iconPackageIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_PACKAGE);
                    final int iconResourceIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_RESOURCE);
                    final int containerIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CONTAINER);
                    final int itemTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ITEM_TYPE);
                    final int appWidgetIdIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.APPWIDGET_ID);
                    final int appWidgetProviderIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.APPWIDGET_PROVIDER);
                    final int screenIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SCREEN);
                    final int cellXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLX);
                    final int cellYIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLY);
                    final int spanXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.SPANX);
                    final int spanYIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SPANY);
                    final int restoredIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.RESTORED);
                    final int profileIdIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.PROFILE_ID);
                    //final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
                    //final int displayModeIndex = c.getColumnIndexOrThrow(
                    final int hiddenIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.HIDDEN);

                    // added by xutg1, support to switch workspaces between 1 layer and 2 layers
                    final int for2LayerWorkspace = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.FOR_2LAYER_WORKSPACE);
                    // add end, xutg1, support to switch workspaces between 1 layer and 2 layers

                    //final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI); //final int displayModeIndex = c.getColumnIndexOrThrow(
                    //        LauncherSettings.Favorites.DISPLAY_MODE);

                    ShortcutInfo info;
                    String intentDescription;
                    LauncherAppWidgetInfo appWidgetInfo;
                    int container;
                    long id;
                    Intent intent;
                    UserHandleCompat user;

                    while (!mStopped && c.moveToNext()) {
                        AtomicBoolean deleteOnInvalidPlacement = new AtomicBoolean(false);
                        try {
                            int itemType = c.getInt(itemTypeIndex);
                            boolean restored = 0 != c.getInt(restoredIndex);
                            boolean allowMissingTarget = false;

                            // added by xutg1, support to switch workspaces between 1 layer and 2 layers
                            // skip if the current item is not for the current layer workspace(1 or 2 layer)
                            /*boolean isFor2LayerWorkspace = 0 != c.getInt(for2LayerWorkspace);
                            if (isFor2LayerWorkspace == LauncherAppState.isDisableAllApps())
                            	continue;*/
                            if(LauncherAppState.getInstance().getCurrentLayoutInt() != c.getInt(for2LayerWorkspace)){
                            	continue;
                            }
                            // add end, xutg1, support to switch workspaces between 1 layer and 2 layers

                            switch (itemType) {
                            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                                //应用和快捷方式
                                id = c.getLong(idIndex);
                                intentDescription = c.getString(intentIndex);
                                long serialNumber = c.getInt(profileIdIndex);
                                user = mUserManager.getUserForSerialNumber(serialNumber);
                                int promiseType = c.getInt(restoredIndex);
                                if (user == null) {
                                    // User has been deleted remove the item.
                                	LauncherLog.i(TAG_ITEMS_REMOVE, "user == null, serialNumber : " + serialNumber +
                                			", android.os.Process.myUserHandle() : " + android.os.Process.myUserHandle());
                                    itemsToRemove.add(id);
                                    continue;
                                }
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                    ComponentName cn = intent.getComponent();
                                    if (cn != null && cn.getPackageName() != null) {
                                        boolean validPkg = launcherApps.isPackageEnabledForProfile(
                                                cn.getPackageName(), user);
                                        boolean validComponent = validPkg &&
                                                launcherApps.isActivityEnabledForProfile(cn, user);

                                        if (validComponent) {
                                            if (restored) {
                                                // no special handling necessary for this item
                                                restoredRows.add(id);
                                                restored = false;
                                            }
                                        } else if (validPkg) {
                                            intent = null;
                                            if ((promiseType & ShortcutInfo.FLAG_AUTOINTALL_ICON) != 0) {
                                                // We allow auto install apps to have their intent
                                                // updated after an install.
                                                intent = manager.getLaunchIntentForPackage(
                                                        cn.getPackageName());
                                                if (intent != null) {
                                                    ContentValues values = new ContentValues();
                                                    values.put(LauncherSettings.Favorites.INTENT,
                                                            intent.toUri(0));
                                                    String where = BaseColumns._ID + "= ?";
                                                    String[] args = {Long.toString(id)};
                                                    contentResolver.update(contentUri, values, where, args);
                                                }
                                            }

                                            if (intent == null) {
                                                // The app is installed but the component is no
                                                // longer available.
                                                Launcher.addDumpLog(TAG,
                                                        "Invalid component removed: " + cn, true);
                                            	LauncherLog.i(TAG_ITEMS_REMOVE, "Invalid component removed: " + cn);
                                                itemsToRemove.add(id);
                                                continue;
                                            } else {
                                                // no special handling necessary for this item
                                                restoredRows.add(id);
                                                restored = false;
                                            }
                                        } else if (restored) {
                                            // Package is not yet available but might be
                                            // installed later.
                                            Launcher.addDumpLog(TAG,
                                                    "package not yet restored: " + cn, true);

                                            if ((promiseType & ShortcutInfo.FLAG_RESTORE_STARTED) != 0) {
                                                // Restore has started once.
                                            } else if (installingPkgs.contains(cn.getPackageName())) {
                                                // App restore has started. Update the flag
                                                promiseType |= ShortcutInfo.FLAG_RESTORE_STARTED;
                                                ContentValues values = new ContentValues();
                                                values.put(LauncherSettings.Favorites.RESTORED,
                                                        promiseType);
                                                String where = BaseColumns._ID + "= ?";
                                                String[] args = {Long.toString(id)};
                                                contentResolver.update(contentUri, values, where, args);

                                            } else if (REMOVE_UNRESTORED_ICONS) {
                                                Launcher.addDumpLog(TAG,
                                                        "Unrestored package removed: " + cn, true);
                                                LauncherLog.i(TAG_ITEMS_REMOVE, "Unrestored package removed: " + cn);
                                                itemsToRemove.add(id);
                                                continue;
                                            }
                                        } else if (isSdCardReady) {
                                            // Do not wait for external media load anymore.
                                            // Log the invalid package, and remove it
                                            Launcher.addDumpLog(TAG,
                                                    "Invalid package removed: " + cn, true);
                                            LauncherLog.i(TAG_ITEMS_REMOVE, "Invalid package removed: " + cn);
                                            itemsToRemove.add(id);
                                            continue;
                                        } else {
                                            // SdCard is not ready yet. Package might get available,
                                            // once it is ready.
                                            Launcher.addDumpLog(TAG, "Invalid package: " + cn
                                                    + " (check again later)", true);
                                            HashSet<String> pkgs = sPendingPackages.get(user);
                                            if (pkgs == null) {
                                                pkgs = new HashSet<String>();
                                                sPendingPackages.put(user, pkgs);
                                            }
                                            pkgs.add(cn.getPackageName());
                                            allowMissingTarget = true;
                                            // Add the icon on the workspace anyway.
                                        }
                                    } else if (cn == null) {
                                        // For shortcuts with no component, keep them as they are
                                        restoredRows.add(id);
                                        restored = false;
                                    }
                                } catch (URISyntaxException e) {
                                    Launcher.addDumpLog(TAG,
                                            "Invalid uri: " + intentDescription, true);
                                    continue;
                                }

                                if (restored) {
                                    if (user.equals(UserHandleCompat.myUserHandle())) {
                                        Launcher.addDumpLog(TAG,
                                                "constructing info for partially restored package",
                                                true);
                                        info = getRestoredItemInfo(c, titleIndex, intent, promiseType);
                                        intent = getRestoredItemIntent(c, context, intent);
                                    } else {
                                        // Don't restore items for other profiles.
                                    	LauncherLog.i(TAG_ITEMS_REMOVE, "!user.equals(UserHandleCompat.myUserHandle(), user : " + user +
                                    			", android.os.Process.myUserHandle() : " +android.os.Process.myUserHandle());
                                        itemsToRemove.add(id);
                                        continue;
                                    }
                                } else if (itemType ==
                                        LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                                    info = getShortcutInfo(manager, intent, user, context, c,
                                            iconIndex, titleIndex, mLabelCache, allowMissingTarget);
                                } else {
                                    info = getShortcutInfo(c, context, iconTypeIndex,
                                            iconPackageIndex, iconResourceIndex, iconIndex,
                                            titleIndex);

                                    String title = c.getString(titleIndex);

                                    if (!TextUtils.isEmpty(title)) {
                                        info.title = title;
                                    } else {
                                        CharSequence packagetitle = getShortcutTitle(manager, intent);
                                        info.title = packagetitle;
                                    }

                                    // App shortcuts that used to be automatically added to Launcher
                                    // didn't always have the correct intent flags set, so do that
                                    // here
                                    if (intent.getAction() != null &&
                                        intent.getCategories() != null &&
                                        intent.getAction().equals(Intent.ACTION_MAIN) &&
                                        intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                                        intent.addFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    }
                                }

                                if (info != null) {
                                    info.id = id;
                                    info.intent = intent;

                                    container = c.getInt(containerIndex);
                                    info.container = container;
                                    info.screenId = c.getInt(screenIndex);
                                    info.cellX = c.getInt(cellXIndex);
                                    info.cellY = c.getInt(cellYIndex);
                                    info.spanX = 1;
                                    info.spanY = 1;
                                    info.intent.putExtra(ItemInfo.EXTRA_PROFILE, serialNumber);
                                    info.isDisabled = isSafeMode
                                            && !Utilities.isSystemApp(context, intent);
                                    LogUtil.e("wqhLauncherModer","app and shortcut container"+info.toString());

                                    /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 START */
                                    if(allowMissingTarget) {
                                    	// 为空表明这是一个应用在加载过程中
                                    	// 因为SDcard的原因
                                    	// 上没有加载成功
                                    	mShortcutInfosCachedAdded.add(info);
                                    	LauncherLog.i(TAG, TAG_SDCARD + ", add info : " + info);

                                    	// 不应该在桌面上显示这个应用
                                        itemsToRemove.add(id);

                                        if(container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                                        		container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                                        	// 如果这是本来就放置在桌面上的应用
                                        	// 直接删除
                                        	break;
                                        } else {
                                        	// 加载中的删除操作只针对文件夹进行
                                        	mShortcutInfosCachedLoaded.add(info);
                                        	// 这是放置在文件夹中的应用
                                        	// 先将其添加到文件夹中
                                        	// 保证文件夹是存在的
                                        	FolderInfo folderInfo =
                                                    findOrMakeFolder(sBgFolders, container);
                                            folderInfo.add(info);
                                            break;
                                        }

                                    }
                                    /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 END */

                                    // check & update map of what's occupied
                                    deleteOnInvalidPlacement.set(false);
                                    if (!checkItemPlacement(occupied, info, deleteOnInvalidPlacement)) {
                                        if (deleteOnInvalidPlacement.get()) {
                                        	LauncherLog.i(TAG_ITEMS_REMOVE, "!checkItemPlacement(occupied, info, deleteOnInvalidPlacement)");
                                            itemsToRemove.add(id);
                                        }
                                        break;
                                    }
                                    //short app 处理
                                    LogUtil.e("wqhLauncherModer","app and shortcut container"+container);
                                    switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                        /*//不包含在十项快捷方式的 快捷方式 放入系统工具文件夹
                                        String strPkg = info.getIntent().getComponent().getPackageName();
                                        LogUtil.e("wqhLauncherModer","strPkg"+strPkg);
                                        if(SettingsValue.isKlauncherCommon(mApp.getContext()) && !TextUtils.isEmpty(strPkg) && !isContain(strPkg)) {
                                            FolderInfo systemfolderInfo =
                                                    findOrMakeFolder(sBgFolders, 1);
                                            systemfolderInfo.add(info);
                                            LogUtil.e("wqhLauncherModer"," systemfolderInfo.add");
                                        }else{
                                            if(mLoaderTaskEqualsThis()) {
                                                sBgWorkspaceItems.add(info);
                                                LogUtil.e("wqhLauncherModer"," sBgWorkspaceItems.add");
                                            }
                                        }
                                        break;*/
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                    	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
                                    	if(mLoaderTaskEqualsThis()) {
                                    		sBgWorkspaceItems.add(info);
                                        }
                                    	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */
                                        break;
                                    default:
                                        // Item is in a user folder
                                        FolderInfo folderInfo =
                                                findOrMakeFolder(sBgFolders, container);
                                        folderInfo.add(info);
                                        break;
                                    }
                                	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
                                    if(mLoaderTaskEqualsThis()) {
                                    	sBgItemsIdMap.put(info.id, info);
                                    }
                                	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */
                                    // now that we've loaded everthing re-save it with the
                                    // icon in case it disappears somehow.
                                    queueIconToBeChecked(sBgDbIconCache, info, c, iconIndex);
                                } else {
                                    throw new RuntimeException("Unexpected null ShortcutInfo");
                                }
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                                id = c.getLong(idIndex);
                                FolderInfo folderInfo = findOrMakeFolder(sBgFolders, id);

                                folderInfo.title = c.getString(titleIndex);
                                folderInfo.id = id;
                                container = c.getInt(containerIndex);
                                folderInfo.container = container;
                                folderInfo.screenId = c.getInt(screenIndex);
                                folderInfo.cellX = c.getInt(cellXIndex);
                                folderInfo.cellY = c.getInt(cellYIndex);
                                folderInfo.spanX = 1;
                                folderInfo.spanY = 1;
                                folderInfo.hidden = c.getInt(hiddenIndex) > 0;
                                LogUtil.e("wqhLauncherModer","FOLDER"+folderInfo.toString());

                                // check & update map of what's occupied
                                deleteOnInvalidPlacement.set(false);
                                if (!checkItemPlacement(occupied, folderInfo,
                                        deleteOnInvalidPlacement)) {
                                    if (deleteOnInvalidPlacement.get()) {
                                    	LauncherLog.i(TAG_ITEMS_REMOVE, "2 !checkItemPlacement(occupied, folderInfo, deleteOnInvalidPlacement)");
                                        itemsToRemove.add(id);
                                    }
                                    break;
                                }

                                switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                    	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
                                    	if(mLoaderTaskEqualsThis()) {
                                    		sBgWorkspaceItems.add(folderInfo);
                                        }
                                    	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */
                                        break;
                                }

                                if (restored) {
                                    // no special handling required for restored folders
                                    restoredRows.add(id);
                                }
                            	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
                                if(mLoaderTaskEqualsThis()) {
	                                sBgItemsIdMap.put(folderInfo.id, folderInfo);
	                                sBgFolders.put(folderInfo.id, folderInfo);
                                }
                            	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                                // Read all Launcher-specific widget details
                                int appWidgetId = c.getInt(appWidgetIdIndex);
                                String savedProvider = c.getString(appWidgetProviderIndex);
                                id = c.getLong(idIndex);
                                final ComponentName component =
                                        ComponentName.unflattenFromString(savedProvider);

                                final int restoreStatus = c.getInt(restoredIndex);
                                boolean isIdValid = (restoreStatus &
                                        LauncherAppWidgetInfo.FLAG_ID_NOT_VALID) == 0;

                                final boolean wasProviderReady = (restoreStatus &
                                        LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY) == 0;
//                                isIdValid = false;
                                final AppWidgetProviderInfo provider = isIdValid
                                        ? widgets.getAppWidgetInfo(appWidgetId)
                                        : findAppWidgetProviderInfoWithComponent(context, component);

                                boolean isKlauncherWidget = false;
                                if (component.toString().contains(KLAUNCHER_WIDGET_CLOCK_CLASS) || component.toString().contains(KLAUNCHER_WIDGET_SEARCH_CLASS)) {
                                    isKlauncherWidget = true;
                                }
                                final boolean isProviderReady = isValidProvider(provider);
                                if (!isSafeMode && wasProviderReady && !isProviderReady && !isKlauncherWidget) {
                                    String log = "Deleting widget that isn't installed anymore: "
                                            + "id=" + id + " appWidgetId=" + appWidgetId;
                                    Log.e(TAG, log);
                                    Launcher.addDumpLog(TAG, log, false);
                                    itemsToRemove.add(id);
                                } else {
                                    if (isProviderReady) {
                                        appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId,
                                                provider.provider);
                                        int[] minSpan =
                                                Launcher.getMinSpanForWidget(context, provider);
                                        appWidgetInfo.minSpanX = minSpan[0];
                                        appWidgetInfo.minSpanY = minSpan[1];

                                        int status = restoreStatus;
                                        if (!wasProviderReady) {
                                            // If provider was not previously ready, update the
                                            // status and UI flag.

                                            // Id would be valid only if the widget restore broadcast was received.
                                            if (isIdValid) {
                                                status = LauncherAppWidgetInfo.RESTORE_COMPLETED;
                                            } else {
                                                status &= ~LauncherAppWidgetInfo
                                                        .FLAG_PROVIDER_NOT_READY;
                                            }
                                        }
                                        appWidgetInfo.restoreStatus = status;
                                    } else {
                                        Log.v(TAG, "Widget restore pending id=" + id
                                                + " appWidgetId=" + appWidgetId
                                                + " status =" + restoreStatus);
                                        appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId,
                                                component);
                                        appWidgetInfo.restoreStatus = restoreStatus;

                                        if ((restoreStatus & LauncherAppWidgetInfo.FLAG_RESTORE_STARTED) != 0) {
                                            // Restore has started once.
                                        } else if(isKlauncherWidget){

                                        }else if (installingPkgs.contains(component.getPackageName())) {
                                            // App restore has started. Update the flag
                                            appWidgetInfo.restoreStatus |=
                                                    LauncherAppWidgetInfo.FLAG_RESTORE_STARTED;
                                        } else if (REMOVE_UNRESTORED_ICONS) {
                                            Launcher.addDumpLog(TAG,
                                                    "Unrestored widget removed: " + component, true);
                                            LauncherLog.i(TAG_ITEMS_REMOVE, "Unrestored widget removed: " + component);
                                            itemsToRemove.add(id);
                                            continue;
                                        }
                                    }

                                    appWidgetInfo.id = id;
                                    appWidgetInfo.screenId = c.getInt(screenIndex);
                                    appWidgetInfo.cellX = c.getInt(cellXIndex);
                                    appWidgetInfo.cellY = c.getInt(cellYIndex);
                                    appWidgetInfo.spanX = c.getInt(spanXIndex);
                                    appWidgetInfo.spanY = c.getInt(spanYIndex);

                                    container = c.getInt(containerIndex);
                                    if (container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                        container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                                        Log.e(TAG, "Widget found where container != " +
                                            "CONTAINER_DESKTOP nor CONTAINER_HOTSEAT - ignoring!");
                                        continue;
                                    }

                                    appWidgetInfo.container = c.getInt(containerIndex);
                                    // check & update map of what's occupied
                                    deleteOnInvalidPlacement.set(false);
                                    if (!checkItemPlacement(occupied, appWidgetInfo,
                                            deleteOnInvalidPlacement)) {
                                        if (deleteOnInvalidPlacement.get()) {
                                        	LauncherLog.i(TAG_ITEMS_REMOVE, "3 !checkItemPlacement(occupied, appWidgetInfo, deleteOnInvalidPlacement)");
                                            itemsToRemove.add(id);
                                        }
                                        break;
                                    }

                                    String providerName = appWidgetInfo.providerName.flattenToString();
                                    if (!providerName.equals(savedProvider) ||
                                            (appWidgetInfo.restoreStatus != restoreStatus)) {
                                        ContentValues values = new ContentValues();
                                        values.put(LauncherSettings.Favorites.APPWIDGET_PROVIDER,
                                                providerName);
                                        values.put(LauncherSettings.Favorites.RESTORED,
                                                appWidgetInfo.restoreStatus);
                                        String where = BaseColumns._ID + "= ?";
                                        String[] args = {Long.toString(id)};
                                        contentResolver.update(contentUri, values, where, args);
                                    }
                                	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 START */
                                    if(mLoaderTaskEqualsThis()) {
	                                    sBgItemsIdMap.put(appWidgetInfo.id, appWidgetInfo);
	                                    sBgAppWidgets.add(appWidgetInfo);
                                    }
                                	/** Lenovo-SW zhaoxin5 20150810 XTHREEROW-640 KOLEOSROW-308 END */
                                }
                                break;
                            }
                        } catch (Exception e) {
                            Launcher.addDumpLog(TAG, "Desktop items loading interrupted", e, true);
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                // Break early if we've stopped loading
                if (mStopped) {
                    clearSBgDataStructures();
                    return false;
                }

                if (itemsToRemove.size() > 0) {
                    ContentProviderClient client = contentResolver.acquireContentProviderClient(
                            contentUri);
                    // Remove dead items
                    for (long id : itemsToRemove) {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "Removed id = " + id);
                        }
                        // Don't notify content observers
                        try {
                            client.delete(LauncherSettings.Favorites.getContentUri(id, false),
                                    null, null);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Could not remove id = " + id);
                        }
                    }
                }

                if (restoredRows.size() > 0) {
                    ContentProviderClient updater = contentResolver.acquireContentProviderClient(
                            contentUri);
                    // Update restored items that no longer require special handling
                    try {
                        StringBuilder selectionBuilder = new StringBuilder();
                        selectionBuilder.append(LauncherSettings.Favorites._ID);
                        selectionBuilder.append(" IN (");
                        selectionBuilder.append(TextUtils.join(", ", restoredRows));
                        selectionBuilder.append(")");
                        ContentValues values = new ContentValues();
                        values.put(LauncherSettings.Favorites.RESTORED, 0);
                        updater.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
                                values, selectionBuilder.toString(), null);
                    } catch (RemoteException e) {
                        Log.w(TAG, "Could not update restored rows");
                    }
                }

                if (!isSdCardReady && !sPendingPackages.isEmpty() && (mShortcutInfosCachedAdded.size()<1)) {
                    context.registerReceiver(new AppsAvailabilityCheck(),
                            new IntentFilter(StartupReceiver.SYSTEM_READY),
                            null, sWorker);
                }

                if (loadedOldDb) {
                    long maxScreenId = 0;
                    // If we're importing we use the old screen order.
                    for (ItemInfo item: sBgItemsIdMap.values()) {
                        long screenId = item.screenId;
                        if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                !sBgWorkspaceScreens.contains(screenId)) {
                            sBgWorkspaceScreens.add(screenId);
                            if (screenId > maxScreenId) {
                                maxScreenId = screenId;
                            }
                        }
                    }
                    Collections.sort(sBgWorkspaceScreens);
                    // Log to disk
                    Launcher.addDumpLog(TAG, "11683562 -   maxScreenId: " + maxScreenId, true);
                    Launcher.addDumpLog(TAG, "11683562 -   sBgWorkspaceScreens: " +
                            TextUtils.join(", ", sBgWorkspaceScreens), true);

                    LauncherAppState.getLauncherProvider().updateMaxScreenId(maxScreenId);
                    updateWorkspaceScreenOrder(context, sBgWorkspaceScreens);

                    // Update the max item id after we load an old db
                    long maxItemId = 0;
                    // If we're importing we use the old screen order.
                    for (ItemInfo item: sBgItemsIdMap.values()) {
                        maxItemId = Math.max(maxItemId, item.id);
                    }
                    LauncherAppState.getLauncherProvider().updateMaxItemId(maxItemId);
                } else {
                    TreeMap<Integer, Long> orderedScreens = loadWorkspaceScreensDb(mContext);
                    // change by xutg1, support switch between 1 and 2 layer solution
                    /** Lenovo-SW zhaoxin5 20150813 add for screen backup and restore support START */
                    if (orderedScreens.size() == 0/* || true*/) { // Lenovo-SW zhaoxin5 20150515 every times we need to update the maxScreenId
                    /** Lenovo-SW zhaoxin5 20150813 add for screen backup and restore support END */
                        long maxScreenId = 0;
                        // If we're importing we use the old screen order.
                        for (ItemInfo item: sBgItemsIdMap.values()) {
                            long screenId = item.screenId;
                            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                    !sBgWorkspaceScreens.contains(screenId)) {
                                sBgWorkspaceScreens.add(screenId);
                                if (screenId > maxScreenId) {
                                    maxScreenId = screenId;
                                }
                            }
                        }
                        Collections.sort(sBgWorkspaceScreens);
                        LauncherLog.i("xixia", " if " +
                        		(LauncherAppState.isDisableAllApps() ? "Single Layer" : "Double Layer")
                        		+ ", maxScreenId:" + maxScreenId);
                        LauncherAppState.getLauncherProvider().updateMaxScreenId(maxScreenId);
                    } else {
                        /*Lenovo-sw zhangyj19 add 2015/09/16 keep empty screens start*/
                        long maxScreenId = 0;
                        for (Integer i : orderedScreens.keySet()) {
                        	long screenId = orderedScreens.get(i);
                        	LauncherLog.i(TAG, "else screenId : " + screenId);
                            sBgWorkspaceScreens.add(screenId);
                            if (screenId > maxScreenId) {
                                maxScreenId = screenId;
                            }
                        }

                        /** Lenovo-SW zhaoxin5 20150813 add for screen backup and restore support START */
                        /*long maxScreenId = 0;
                        // If we're importing we use the old screen order.
                        for (ItemInfo item: sBgItemsIdMap.values()) {
                            long screenId = item.screenId;
                            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                if (screenId > maxScreenId) {
                                    maxScreenId = screenId;
                                }
                            }
                        }
                        */
                        /*Lenovo-sw zhangyj19 add 2015/09/16 keep empty screens end*/
                        LauncherLog.i("xixia", " else " +
                        		(LauncherAppState.isDisableAllApps() ? "Single Layer" : "Double Layer")
                        		+ ", maxScreenId:" + maxScreenId);
                        LauncherAppState.getLauncherProvider().updateMaxScreenId(maxScreenId);
                        /** Lenovo-SW zhaoxin5 20150813 add for screen backup and restore support END */
                    }
                    // Log to disk
                    Launcher.addDumpLog(TAG, "11683562 -   sBgWorkspaceScreens: " +
                            TextUtils.join(", ", sBgWorkspaceScreens), true);
                    /*Lenovo-sw zhangyj19 add 2015/09/16 keep empty screens start*/
                    // Remove any empty screens
                    /*ArrayList<Long> unusedScreens = new ArrayList<Long>(sBgWorkspaceScreens);
                    for (ItemInfo item: sBgItemsIdMap.values()) {
                        long screenId = item.screenId;
                        if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                unusedScreens.contains(screenId)) {
                            unusedScreens.remove(screenId);
                        }
                    }

                    // If there are any empty screens remove them, and update.
                    if (unusedScreens.size() != 0) {
                        // Log to disk
                        Launcher.addDumpLog(TAG, "11683562 -   unusedScreens (to be removed): " +
                                TextUtils.join(", ", unusedScreens), true);

                        sBgWorkspaceScreens.removeAll(unusedScreens);
                    }*/
                   /*Lenovo-sw zhangyj19 add 2015/09/16 keep empty screens end*/
                    updateWorkspaceScreenOrder(context, sBgWorkspaceScreens);
                    // end change, xutg1, support switch between 1 and 2 layer solution

                }

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "loaded workspace in " + (SystemClock.uptimeMillis()-t) + "ms");
                    Log.d(TAG, "workspace layout: ");
                    int nScreens = occupied.size();
                    for (int y = 0; y < countY; y++) {
                        String line = "";

                        Iterator<Long> iter = occupied.keySet().iterator();
                        while (iter.hasNext()) {
                            long screenId = iter.next();
                            if (screenId > 0) {
                                line += " | ";
                            }
                            for (int x = 0; x < countX; x++) {
                                ItemInfo[][] screen = occupied.get(screenId);
                                if (x < screen.length && y < screen[x].length) {
                                    line += (screen[x][y] != null) ? "#" : ".";
                                } else {
                                    line += "!";
                                }
                            }
                        }
                        Log.d(TAG, "[ " + line + " ]");
                    }
                }
            }
            return loadedOldDb;
        }

        /** Filters the set of items who are directly or indirectly (via another container) on the
         * specified screen. */
        private void filterCurrentWorkspaceItems(long currentScreenId,
                ArrayList<ItemInfo> allWorkspaceItems,
                ArrayList<ItemInfo> currentScreenItems,
                ArrayList<ItemInfo> otherScreenItems) {
            // Purge any null ItemInfos
            Iterator<ItemInfo> iter = allWorkspaceItems.iterator();
            while (iter.hasNext()) {
                ItemInfo i = iter.next();
                if (i == null) {
                    iter.remove();
                }
            }

            // Order the set of items by their containers first, this allows use to walk through the
            // list sequentially, build up a list of containers that are in the specified screen,
            // as well as all items in those containers.
            Set<Long> itemsOnScreen = new HashSet<Long>();
            Collections.sort(allWorkspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    return (int) (lhs.container - rhs.container);
                }
            });
            for (ItemInfo info : allWorkspaceItems) {
                if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    if (info.screenId == currentScreenId) {
                        currentScreenItems.add(info);
                        itemsOnScreen.add(info.id);
                    } else {
                        otherScreenItems.add(info);
                    }
                } else if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                    currentScreenItems.add(info);
                    itemsOnScreen.add(info.id);
                } else {
                    if (itemsOnScreen.contains(info.container)) {
                        currentScreenItems.add(info);
                        itemsOnScreen.add(info.id);
                    } else {
                        otherScreenItems.add(info);
                    }
                }
            }
        }

        /** Filters the set of widgets which are on the specified screen. */
        private void filterCurrentAppWidgets(long currentScreenId,
                ArrayList<LauncherAppWidgetInfo> appWidgets,
                ArrayList<LauncherAppWidgetInfo> currentScreenWidgets,
                ArrayList<LauncherAppWidgetInfo> otherScreenWidgets) {

            for (LauncherAppWidgetInfo widget : appWidgets) {
                if (widget == null) continue;
                if (widget.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                        widget.screenId == currentScreenId) {
                    currentScreenWidgets.add(widget);
                } else {
                    otherScreenWidgets.add(widget);
                }
            }
        }

        /** Filters the set of folders which are on the specified screen. */
        private void filterCurrentFolders(long currentScreenId,
                HashMap<Long, ItemInfo> itemsIdMap,
                HashMap<Long, FolderInfo> folders,
                HashMap<Long, FolderInfo> currentScreenFolders,
                HashMap<Long, FolderInfo> otherScreenFolders) {

            for (long id : folders.keySet()) {
                ItemInfo info = itemsIdMap.get(id);
                FolderInfo folder = folders.get(id);
                if (info == null || folder == null) continue;
                if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                        info.screenId == currentScreenId) {
                    currentScreenFolders.put(id, folder);
                } else {
                    otherScreenFolders.put(id, folder);
                }
            }
        }

        /** Sorts the set of items by hotseat, workspace (spatially from top to bottom, left to
         * right) */
        private void sortWorkspaceItemsSpatially(ArrayList<ItemInfo> workspaceItems) {
            final LauncherAppState app = LauncherAppState.getInstance();
            final DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            // XXX: review this
            Collections.sort(workspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    int cellCountX = (int) grid.numColumns;
                    int cellCountY = (int) grid.numRows;
                    int screenOffset = cellCountX * cellCountY;
                    int containerOffset = screenOffset * (Launcher.SCREEN_COUNT + 1); // +1 hotseat
                    long lr = (lhs.container * containerOffset + lhs.screenId * screenOffset +
                            lhs.cellY * cellCountX + lhs.cellX);
                    long rr = (rhs.container * containerOffset + rhs.screenId * screenOffset +
                            rhs.cellY * cellCountX + rhs.cellX);
                    return (int) (lr - rr);
                }
            });
        }

        private void bindWorkspaceScreens(final Callbacks oldCallbacks,
                final ArrayList<Long> orderedScreens) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        //bind screen
                        callbacks.bindScreens(orderedScreens);
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
        }

        private void removeHiddenAppsWorkspaceItems(
                final ArrayList<ItemInfo> workspaceItems,
                final ArrayList<LauncherAppWidgetInfo> appWidgets,
                final HashMap<Long, FolderInfo> folders) {

            // Get hidden apps
            ArrayList<ComponentName> mHiddenApps = new ArrayList<ComponentName>();
            ArrayList<String> mHiddenAppsPackages = new ArrayList<String>();
            Context context = mApp.getContext();
            // Since Trebuchet is compiled using the SDK we have to hardcode this string
            String protectedComponents = Settings.Secure.getString(context.getContentResolver(),
                    SETTINGS_PROTECTED_COMPONENTS);
            protectedComponents = protectedComponents == null ? "" : protectedComponents;
            String[] flattened = protectedComponents.split("\\|");
            boolean hideShortcuts = SettingsProvider.getBoolean(context,
                    SettingsProvider.SETTINGS_UI_DRAWER_REMOVE_HIDDEN_APPS_SHORTCUTS,
                    R.bool.preferences_interface_drawer_remove_hidden_apps_shortcuts_default);
            boolean hideWidgets = SettingsProvider.getBoolean(context,
                    SettingsProvider.SETTINGS_UI_DRAWER_REMOVE_HIDDEN_APPS_WIDGETS,
                    R.bool.preferences_interface_drawer_remove_hidden_apps_widgets_default);

            for (String flat : flattened) {
                ComponentName cmp = ComponentName.unflattenFromString(flat);
                if (cmp != null) {
                    mHiddenApps.add(cmp);
                    mHiddenAppsPackages.add(cmp.getPackageName());
                }
            }

            // Shortcuts
            if (hideShortcuts) {
                int N = workspaceItems.size() - 1;
                for (int i = N; i >= 0; i--) {
                    final ItemInfo item = workspaceItems.get(i);
                    if (item instanceof ShortcutInfo) {
                        ShortcutInfo shortcut = (ShortcutInfo) item;
                        if (shortcut.intent != null && shortcut.intent.getComponent() != null) {
                            if (mHiddenApps.contains(shortcut.intent.getComponent())) {
                                LauncherModel.deleteItemFromDatabase(mContext, shortcut);
                                workspaceItems.remove(i);
                            }
                        }
                    } else {
                        // Only remove items from folders that aren't hidden
                        final FolderInfo folder = (FolderInfo) item;
                        List<ShortcutInfo> shortcuts = folder.contents;

                        int NN = shortcuts.size() - 1;
                        for (int j = NN; j >= 0; j--) {
                            final ShortcutInfo sci = shortcuts.get(j);
                            if (sci.intent != null && sci.intent.getComponent() != null) {
                                if (!folder.hidden) {
                                    if (mHiddenApps.contains(sci.intent.getComponent())) {
                                        LauncherModel.deleteItemFromDatabase(mContext, sci);
                                        Runnable r = new Runnable() {
                                            public void run() {
                                                folder.remove(sci);
                                            }
                                        };
                                        runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                                    }
                                } else {
                                    if (!mHiddenApps.contains(sci.intent.getComponent())) {
                                        LauncherModel.deleteItemFromDatabase(mContext, sci);
                                        Runnable r = new Runnable() {
                                            public void run() {
                                                folder.remove(sci);
                                            }
                                        };
                                        runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                                    }
                                }

                            }
                        }

                        if (folder.contents.size() == 1 && !folder.hidden) {
                            ShortcutInfo finalItem = folder.contents.get(0);
                            finalItem.container = folder.container;
                            LauncherModel.deleteItemFromDatabase(mContext, folder);
                            // only replace this item back on the workspace if it's not protected
                            if (!mHiddenApps.contains(finalItem.intent.getComponent())) {
                                LauncherModel.addOrMoveItemInDatabase(mContext, finalItem, folder.container,
                                        folder.screenId, folder.cellX, folder.cellY);
                                workspaceItems.add(finalItem);
                            }
                            workspaceItems.remove(i);
                            folders.remove(Long.valueOf(item.id));
                        } else if (folder.contents.size() == 0 /*&& !(folder instanceof LiveFolderInfo)*/) {
                            LauncherModel.deleteFolderContentsFromDatabase(mContext, folder);
                            workspaceItems.remove(i);
                            folders.remove(Long.valueOf(item.id));
                        }
                    }
                }
            }

            // AppWidgets
            if (hideWidgets) {
                int N = appWidgets.size() - 1;
                for (int i = N; i >= 0; i--) {
                    final LauncherAppWidgetInfo item = appWidgets.get(i);
                    if (item.providerName != null) {
                        if (mHiddenAppsPackages.contains(item.providerName.getPackageName())) {
                            LauncherModel.deleteItemFromDatabase(mContext, item);
                            appWidgets.remove(i);
                        }
                    }
                }
            }
        }

        private void bindWorkspaceItems(final Callbacks oldCallbacks,
                final ArrayList<ItemInfo> workspaceItems,
                final ArrayList<LauncherAppWidgetInfo> appWidgets,
                final HashMap<Long, FolderInfo> folders,
                ArrayList<Runnable> deferredBindRunnables) {

            final boolean postOnMainThread = (deferredBindRunnables != null);
            //
            removeHiddenAppsWorkspaceItems(workspaceItems, appWidgets, folders);

            // Bind the workspace items
            int N = workspaceItems.size();
            for (int i = 0; i < N; i += ITEMS_CHUNK) {
                final int start = i;
                final int chunkSize = (i+ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N-i);
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindItems(workspaceItems, start, start+chunkSize,
                                    false);
                        }
                    }
                };
                if (postOnMainThread) {
                    synchronized (deferredBindRunnables) {
                        deferredBindRunnables.add(r);
                    }
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }

            // Bind the folders
            if (!folders.isEmpty()) {
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindFolders(folders);
                        }
                    }
                };
                if (postOnMainThread) {
                    synchronized (deferredBindRunnables) {
                        deferredBindRunnables.add(r);
                    }
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }

            // Bind the widgets, one at a time
            N = appWidgets.size();
            for (int i = 0; i < N; i++) {
                final LauncherAppWidgetInfo widget = appWidgets.get(i);
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindAppWidget(widget);
                        }
                    }
                };
                if (postOnMainThread) {
                    deferredBindRunnables.add(r);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }
        }

        /**
         * Binds all loaded data to actual views on the main thread.
         */
        private void bindWorkspace(int synchronizeBindPage, final boolean isUpgradePath) {
            final long t = SystemClock.uptimeMillis();
            Runnable r;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher");
                return;
            }

            // Save a copy of all the bg-thread collections
            ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> appWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
            HashMap<Long, ItemInfo> itemsIdMap = new HashMap<Long, ItemInfo>();
            ArrayList<Long> orderedScreenIds = new ArrayList<Long>();
            synchronized (sBgLock) {
                workspaceItems.addAll(sBgWorkspaceItems);
                appWidgets.addAll(sBgAppWidgets);
                folders.putAll(sBgFolders);
                itemsIdMap.putAll(sBgItemsIdMap);
                orderedScreenIds.addAll(sBgWorkspaceScreens);
            }

            final boolean isLoadingSynchronously =
                    synchronizeBindPage != PagedView.INVALID_RESTORE_PAGE;
            int currScreen = isLoadingSynchronously ? synchronizeBindPage :
                oldCallbacks.getCurrentWorkspaceScreen();
            if (currScreen >= orderedScreenIds.size()) {
                // There may be no workspace screens (just hotseat items and an empty page).
                currScreen = PagedView.INVALID_RESTORE_PAGE;
            }
            final int currentScreen = currScreen;
            final long currentScreenId = currentScreen < 0
                    ? INVALID_SCREEN_ID : orderedScreenIds.get(currentScreen);

            // Load all the items that are on the current page first (and in the process, unbind
            // all the existing workspace items before we call startBinding() below.
            unbindWorkspaceItemsOnMainThread();

            // Separate the items that are on the current screen, and all the other remaining items
            ArrayList<ItemInfo> currentWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<ItemInfo> otherWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> currentAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            ArrayList<LauncherAppWidgetInfo> otherAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> currentFolders = new HashMap<Long, FolderInfo>();
            HashMap<Long, FolderInfo> otherFolders = new HashMap<Long, FolderInfo>();

            filterCurrentWorkspaceItems(currentScreenId, workspaceItems, currentWorkspaceItems,
                    otherWorkspaceItems);
            filterCurrentAppWidgets(currentScreenId, appWidgets, currentAppWidgets,
                    otherAppWidgets);
            filterCurrentFolders(currentScreenId, itemsIdMap, folders, currentFolders,
                    otherFolders);
            sortWorkspaceItemsSpatially(currentWorkspaceItems);
            sortWorkspaceItemsSpatially(otherWorkspaceItems);

            // Tell the workspace that we're about to start binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.startBinding();
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            //bind Workspace
            bindWorkspaceScreens(oldCallbacks, orderedScreenIds);

            // Load items on the current page
            bindWorkspaceItems(oldCallbacks, currentWorkspaceItems, currentAppWidgets,
                    currentFolders, null);

            /** Lenovo-SW zhaoxin5 20150909 Launcher 启动优化 START */
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.afterBindingCurrentPage();
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            /** Lenovo-SW zhaoxin5 20150909 Launcher 启动优化 END */

            if (isLoadingSynchronously) {
                r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null && currentScreen != PagedView.INVALID_RESTORE_PAGE) {
                            callbacks.onPageBoundSynchronously(currentScreen);
                        }
                    }
                };
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }

            // Load all the remaining pages (if we are loading synchronously, we want to defer this
            // work until after the first render)
            synchronized (mDeferredBindRunnables) {
                mDeferredBindRunnables.clear();
            }
            bindWorkspaceItems(oldCallbacks, otherWorkspaceItems, otherAppWidgets, otherFolders,
                    (isLoadingSynchronously ? mDeferredBindRunnables : null));

            // Tell the workspace that we're done binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindingItems(isUpgradePath);
                    }

                    // If we're profiling, ensure this is the last thing in the queue.
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound workspace in "
                            + (SystemClock.uptimeMillis()-t) + "ms");
                    }

                    mIsLoadingAndBindingWorkspace = false;

                    /** Lenovo-SW zhaoxin5 20150721 add for auto-reorder support START */
                    if (callbacks != null) {
                    	callbacks.finishLoadingAndBindingWorkspace();
                    }
                    /** Lenovo-SW zhaoxin5 20150721 add for auto-reorder support END */
                }
            };
            if (isLoadingSynchronously) {
                synchronized (mDeferredBindRunnables) {
                    mDeferredBindRunnables.add(r);
                }
            } else {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }

        private void loadAndBindAllApps() {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindAllApps mAllAppsLoaded=" + mAllAppsLoaded);
            }
            if (!mAllAppsLoaded) {
                loadAllApps();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            } else {
                onlyBindAllApps();
            }
        }

        private void onlyBindAllApps() {
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
                return;
            }

            // shallow copy
            @SuppressWarnings("unchecked")
            final ArrayList<AppInfo> list
                    = (ArrayList<AppInfo>) mBgAllAppsList.data.clone();
            Runnable r = new Runnable() {
                public void run() {
                    final long t = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(list);
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound all " + list.size() + " apps from cache in "
                                + (SystemClock.uptimeMillis()-t) + "ms");
                    }
                }
            };
            boolean isRunningOnMainThread = !(sWorkerThread.getThreadId() == Process.myTid());
            if (isRunningOnMainThread) {
                r.run();
            } else {
                mHandler.post(r);
            }
        }

        private void loadAllApps() {
            final long loadTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (loadAllApps)");
                return;
            }

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final List<UserHandleCompat> profiles = mUserManager.getUserProfiles();

            // Clear the list of apps
            mBgAllAppsList.clear();
            for (UserHandleCompat user : profiles) {
                // Query for the set of apps
                final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                List<LauncherActivityInfoCompat> apps = mLauncherApps.getActivityList(null, user);
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "getActivityList took "
                            + (SystemClock.uptimeMillis()-qiaTime) + "ms for user " + user);
                    Log.d(TAG, "getActivityList got " + apps.size() + " apps for user " + user);
                }
                // Fail if we don't have any apps
                if (apps == null || apps.isEmpty()) {
                    return;
                }
                // Sort the applications by name
                final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                Collections.sort(apps,
                        new LauncherModel.ShortcutNameComparator(mLabelCache));
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "sort took "
                            + (SystemClock.uptimeMillis()-sortTime) + "ms");
                }

                // Create the ApplicationInfos
                for (int i = 0; i < apps.size(); i++) {
                    LauncherActivityInfoCompat app = apps.get(i);
                    /** Lenovo-SW zhaoxin5 20150702 LANCHROW-196 START */
                    if(isFilterPackage(app.getApplicationInfo().packageName)) {
                    	continue;
                    }
                    /** Lenovo-SW zhaoxin5 20150702 LANCHROW-196 END */
                    // This builds the icon bitmaps.
                    mBgAllAppsList.add(new AppInfo(mContext, app, user, mIconCache, mLabelCache));
                }
            }
            // Huh? Shouldn't this be inside the Runnable below?
            final ArrayList<AppInfo> added = mBgAllAppsList.added;
            mBgAllAppsList.added = new ArrayList<AppInfo>();

            // Post callback on main thread
            mHandler.post(new Runnable() {
                public void run() {
                    final long bindTime = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(added);
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "bound " + added.size() + " apps in "
                                + (SystemClock.uptimeMillis() - bindTime) + "ms");
                        }
                    } else {
                        Log.i(TAG, "not binding apps: no Launcher activity");
                    }
                }
            });

            if (DEBUG_LOADERS) {
                Log.d(TAG, "Icons processed in "
                        + (SystemClock.uptimeMillis() - loadTime) + "ms");
            }
        }

        public void dumpState() {
            synchronized (sBgLock) {
                Log.d(TAG, "mLoaderTask.mContext=" + mContext);
                Log.d(TAG, "mLoaderTask.mIsLaunching=" + mIsLaunching);
                Log.d(TAG, "mLoaderTask.mStopped=" + mStopped);
                Log.d(TAG, "mLoaderTask.mLoadAndBindStepFinished=" + mLoadAndBindStepFinished);
                Log.d(TAG, "mItems size=" + sBgWorkspaceItems.size());
            }
        }
    }

    void enqueuePackageUpdated(PackageUpdatedTask task) {
        sWorker.post(task);
    }

    private class AppsAvailabilityCheck extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (sBgLock) {
                final LauncherAppsCompat launcherApps = LauncherAppsCompat
                        .getInstance(mApp.getContext());
                ArrayList<String> packagesRemoved;
                for (Entry<UserHandleCompat, HashSet<String>> entry : sPendingPackages.entrySet()) {
                    UserHandleCompat user = entry.getKey();
                    packagesRemoved = new ArrayList<String>();
                    for (String pkg : entry.getValue()) {
                        if (!launcherApps.isPackageEnabledForProfile(pkg, user)) {
                            Launcher.addDumpLog(TAG, "Package not found: " + pkg, true);
                            packagesRemoved.add(pkg);
                        }
                    }
                    if (!packagesRemoved.isEmpty()) {
                        enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_REMOVE,
                                packagesRemoved.toArray(new String[packagesRemoved.size()]), user));
                    }
                }
                sPendingPackages.clear();
            }
        }
    }

    /**
     * Workaround to re-check unrestored items, in-case they were installed but the Package-ADD
     * runnable was missed by the launcher.
     */
    public void recheckRestoredItems(final Context context) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
                HashSet<String> installedPackages = new HashSet<String>();
                UserHandleCompat user = UserHandleCompat.myUserHandle();
                synchronized(sBgLock) {
                    for (ItemInfo info : sBgItemsIdMap.values()) {
                        if (info instanceof ShortcutInfo) {
                            ShortcutInfo si = (ShortcutInfo) info;
                            if (si.isPromise() && si.getTargetComponent() != null
                                    && launcherApps.isPackageEnabledForProfile(
                                            si.getTargetComponent().getPackageName(), user)) {
                                //Yanni: do not add filtered package
                                if (!isFilterPackage(si.getTargetComponent().getPackageName())) {
                                    installedPackages.add(si.getTargetComponent().getPackageName());
                                }
                            }
                        } else if (info instanceof LauncherAppWidgetInfo) {
                            LauncherAppWidgetInfo widget = (LauncherAppWidgetInfo) info;
                            if (widget.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY)
                                    && launcherApps.isPackageEnabledForProfile(
                                            widget.providerName.getPackageName(), user)) {
                                installedPackages.add(widget.providerName.getPackageName());
                            }
                        }
                    }
                }

                if (!installedPackages.isEmpty()) {
                    final ArrayList<AppInfo> restoredApps = new ArrayList<AppInfo>();
                    for (String pkg : installedPackages) {
                        for (LauncherActivityInfoCompat info : launcherApps.getActivityList(pkg, user)) {
                            restoredApps.add(new AppInfo(context, info, user, mIconCache, null));
                        }
                    }

                    final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                    if (!restoredApps.isEmpty()) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                                if (callbacks == cb && cb != null) {
                                    callbacks.bindAppsRestored(restoredApps);
                                }
                            }
                        });
                    }

                }
            }
        };
        sWorker.post(r);
    }

    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;
        UserHandleCompat mUser;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted


        public PackageUpdatedTask(int op, String[] packages, UserHandleCompat user) {
            mOp = op;
            mPackages = packages;
            mUser = user;
        }

        public void run() {
            final Context context = mApp.getContext();

            final String[] packages = mPackages;
            final int N = packages.length;
            switch (mOp) {
                case OP_ADD:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.addPackage " + packages[i]);
                        mIconCache.remove(packages[i], mUser);
                        mBgAllAppsList.addPackage(context, packages[i], mUser);
                    }
                    break;
                case OP_UPDATE:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.updatePackage " + packages[i]);
                        mBgAllAppsList.updatePackage(context, packages[i], mUser);
                        WidgetPreviewLoader.removePackageFromDb(
                                mApp.getWidgetPreviewCacheDb(), packages[i]);
                    }
                    break;
                case OP_REMOVE:
                case OP_UNAVAILABLE:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.removePackage " + packages[i]);
                        mBgAllAppsList.removePackage(packages[i], mUser);
                        WidgetPreviewLoader.removePackageFromDb(
                                mApp.getWidgetPreviewCacheDb(), packages[i]);
                    }
                    break;
            }

            ArrayList<AppInfo> added = null;
            ArrayList<AppInfo> modified = null;
            final ArrayList<AppInfo> removedApps = new ArrayList<AppInfo>();

            if (mBgAllAppsList.added.size() > 0) {
                added = new ArrayList<AppInfo>(mBgAllAppsList.added);
                mBgAllAppsList.added.clear();
            }
            if (mBgAllAppsList.modified.size() > 0) {
                modified = new ArrayList<AppInfo>(mBgAllAppsList.modified);
                mBgAllAppsList.modified.clear();
            }
            if (mBgAllAppsList.removed.size() > 0) {
                removedApps.addAll(mBgAllAppsList.removed);
                mBgAllAppsList.removed.clear();

                for (AppInfo a : removedApps) {
                    ArrayList<ItemInfo> infos = getItemInfoForComponentName(a.componentName, mUser);
                    /** Lenovo-SW zhaoxin5 20150831 XTHREEROW-1232 START */
    				for (ItemInfo iInfo : infos) {
    					LauncherLog.i(TAG, TAG_SDCARD + ", package update for " + iInfo);
    					if (iInfo instanceof ShortcutInfo) {
    						ShortcutInfo sInfo = (ShortcutInfo) iInfo;
    						if (null != sInfo.getIntent() && null != sInfo.getIntent().getComponent()) {
    							if (!isShortcutInfosInCachedAdded(sInfo.getIntent().getComponent())) {
    								LauncherLog.i(TAG, TAG_SDCARD + ", add " + iInfo + " to mShortcutInfosCachedAdded");
    								mShortcutInfosCachedAdded.add(sInfo);
    							} // !isShortcutInfosInCachedAdded(sInfo.getIntent().getComponent()
    						} // null != sInfo.getIntent() && null != sInfo.getIntent().getComponent()
    					} // iInfo instanceof ShortcutInfo
    				}
                    /** Lenovo-SW zhaoxin5 20150831 XTHREEROW-1232 END */
                }
            }

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }

            if (added != null) {
                // Ensure that we add all the workspace applications to the db
                if (LauncherAppState.isDisableAllApps()) {
                    final ArrayList<ItemInfo> addedInfos = new ArrayList<ItemInfo>(added);
                    addAndBindAddedWorkspaceApps(context, addedInfos);
                } else {
                    addAppsToAllApps(context, added);
                }
            }

            if (modified != null) {
                final ArrayList<AppInfo> modifiedFinal = modified;

                // Update the launcher db to reflect the changes
                for (AppInfo a : modifiedFinal) {
                    ArrayList<ItemInfo> infos =
                            getItemInfoForComponentName(a.componentName, mUser);
                    for (ItemInfo i : infos) {
                        if (isShortcutInfoUpdateable(i)) {
                            ShortcutInfo info = (ShortcutInfo) i;
                            info.title = a.title.toString();
                            info.contentDescription = a.contentDescription;
                            updateItemInDatabase(context, info);
                        }
                    }
                }

                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsUpdated(modifiedFinal);
                        }
                    }
                });
            }

            final ArrayList<String> removedPackageNames =
                    new ArrayList<String>();
            if (mOp == OP_REMOVE) {
                // Mark all packages in the broadcast to be removed
                removedPackageNames.addAll(Arrays.asList(packages));
            } else if (mOp == OP_UPDATE) {
                // Mark disabled packages in the broadcast to be removed
                final PackageManager pm = context.getPackageManager();
                for (int i=0; i<N; i++) {
                    if (isPackageDisabled(context, packages[i], mUser)) {
                        removedPackageNames.add(packages[i]);
                    }
                }
            }
            // Remove all the components associated with this package
            for (String pn : removedPackageNames) {
                deletePackageFromDatabase(context, pn, mUser);
            }
            // Remove all the specific components
            for (AppInfo a : removedApps) {
                ArrayList<ItemInfo> infos = getItemInfoForComponentName(a.componentName, mUser);
                deleteItemsFromDatabase(context, infos);
            }
            if (!removedPackageNames.isEmpty() || !removedApps.isEmpty()) {
                // Remove any queued items from the install queue
                String spKey = LauncherAppState.getSharedPreferencesKey();
                SharedPreferences sp =
                        context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
                InstallShortcutReceiver.removeFromInstallQueue(sp, removedPackageNames);
                // Call the components-removed callback
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindComponentsRemoved(removedPackageNames, removedApps, mUser);
                        }
                    }
                });
            }

            final ArrayList<Object> widgetsAndShortcuts =
                    getSortedWidgetsAndShortcuts(context);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.bindPackagesUpdated(widgetsAndShortcuts);
                    }
                }
            });

            // Write all the logs to disk
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.dumpLogsToLocalData();
                    }
                }
            });
        }
    }

    // Returns a list of ResolveInfos/AppWindowInfos in sorted order
    public static ArrayList<Object> getSortedWidgetsAndShortcuts(Context context) {
        PackageManager packageManager = context.getPackageManager();
        final ArrayList<Object> widgetsAndShortcuts = new ArrayList<Object>();
        widgetsAndShortcuts.addAll(AppWidgetManagerCompat.getInstance(context).getAllProviders());

        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        widgetsAndShortcuts.addAll(packageManager.queryIntentActivities(shortcutsIntent, 0));
        Collections.sort(widgetsAndShortcuts, new WidgetAndShortcutNameComparator(context));
        return widgetsAndShortcuts;
    }

    private static boolean isPackageDisabled(Context context, String packageName,
            UserHandleCompat user) {
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
        return !launcherApps.isPackageEnabledForProfile(packageName, user);
    }

    public static boolean isValidPackageActivity(Context context, ComponentName cn,
            UserHandleCompat user) {
        if (cn == null) {
            return false;
        }
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
        if (!launcherApps.isPackageEnabledForProfile(cn.getPackageName(), user)) {
            return false;
        }
        return launcherApps.isActivityEnabledForProfile(cn, user);
    }

    public static boolean isValidPackage(Context context, String packageName,
            UserHandleCompat user) {
        if (packageName == null) {
            return false;
        }
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
        return launcherApps.isPackageEnabledForProfile(packageName, user);
    }

    /**
     * Make an ShortcutInfo object for a restored application or shortcut item that points
     * to a package that is not yet installed on the system.
     */
    public ShortcutInfo getRestoredItemInfo(Cursor cursor, int titleIndex, Intent intent,
            int promiseType) {
        final ShortcutInfo info = new ShortcutInfo();
        info.user = UserHandleCompat.myUserHandle();
        mIconCache.getTitleAndIcon(info, intent, info.user, true);

        if ((promiseType & ShortcutInfo.FLAG_RESTORED_ICON) != 0) {
            String title = (cursor != null) ? cursor.getString(titleIndex) : null;
            if (!TextUtils.isEmpty(title)) {
                info.title = title;
            }
            info.status = ShortcutInfo.FLAG_RESTORED_ICON;
        } else if  ((promiseType & ShortcutInfo.FLAG_AUTOINTALL_ICON) != 0) {
            if (TextUtils.isEmpty(info.title)) {
                info.title = (cursor != null) ? cursor.getString(titleIndex) : "";
            }
            info.status = ShortcutInfo.FLAG_AUTOINTALL_ICON;
        } else {
            throw new InvalidParameterException("Invalid restoreType " + promiseType);
        }
        if (info.title != null) {
            info.contentDescription = mUserManager.getBadgedLabelForUser(
                    info.title.toString(), info.user);
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
        info.promisedIntent = intent;
        return info;
    }

    /**
     * Make an Intent object for a restored application or shortcut item that points
     * to the market page for the item.
     */
    private Intent getRestoredItemIntent(Cursor c, Context context, Intent intent) {
        ComponentName componentName = intent.getComponent();
        return getMarketIntent(componentName.getPackageName());
    }

    static Intent getMarketIntent(String packageName) {
        return new Intent(Intent.ACTION_VIEW)
            .setData(new Uri.Builder()
                .scheme("market")
                .authority("details")
                .appendQueryParameter("id", packageName)
                .build());
    }

    /**
     * This is called from the code that adds shortcuts from the intent receiver.  This
     * doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent,
            UserHandleCompat user, Context context) {
        return getShortcutInfo(manager, intent, user, context, null, -1, -1, null, false);
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent,
            UserHandleCompat user, Context context, Cursor c, int iconIndex, int titleIndex,
            HashMap<Object, CharSequence> labelCache, boolean allowMissingTarget) {
        if (user == null) {
            Log.d(TAG, "Null user found in getShortcutInfo");
            return null;
        }

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            Log.d(TAG, "Missing component found in getShortcutInfo: " + componentName);
            return null;
        }

        Intent newIntent = new Intent(intent.getAction(), null);
        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        newIntent.setComponent(componentName);
        LauncherActivityInfoCompat lai = mLauncherApps.resolveActivity(newIntent, user);
        if ((lai == null) && !allowMissingTarget) {
            Log.d(TAG, "Missing activity found in getShortcutInfo: " + componentName);
            return null;
        }

        final ShortcutInfo info = new ShortcutInfo();

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.
        Bitmap icon = mIconCache.getIcon(componentName, lai, labelCache);

        // the db
        if (icon == null) {
            if (c != null) {
                icon = getIconFromCursor(c, iconIndex, context);
            }
        }
        // the fallback icon
        if (icon == null) {
            icon = mIconCache.getDefaultIcon(user);
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        // from the db
        if (info.title == null) {
            if (c != null) {
                info.title = c.getString(titleIndex);
            }
        }
        // From the cache.
        if (info.title == null && labelCache != null) {
            info.title = labelCache.get(componentName);
        }
        // from the resource
        if (info.title == null && lai != null) {
            info.title = lai.getLabel();
            if (labelCache != null) {
                labelCache.put(componentName, info.title);
            }
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        info.user = user;
        if (info.title != null) {
            info.contentDescription = mUserManager.getBadgedLabelForUser(
                    info.title.toString(), info.user);
        }
        /* Lenovo-SW zhaoxin5 20150522 add for 2 layer support START */
        /*for(int i=0; i<mBgAllAppsList.size(); i++){
        	if(mBgAllAppsList.get(i).componentName == null){
        		continue;
        	}
        	if(mBgAllAppsList.get(i).componentName.equals(componentName)){
        		info.flags = mBgAllAppsList.get(i).flags;
        	}
        }*/
        /* Lenovo-SW zhaoxin5 20150522 add for 2 layer support START */

        return info;
    }

    static ArrayList<ItemInfo> filterItemInfos(Collection<ItemInfo> infos,
            ItemInfoFilter f) {
        HashSet<ItemInfo> filtered = new HashSet<ItemInfo>();
        for (ItemInfo i : infos) {
            if (i instanceof ShortcutInfo) {
                ShortcutInfo info = (ShortcutInfo) i;
                ComponentName cn = info.getTargetComponent();
                if (cn != null && f.filterItem(null, info, cn)) {
                    filtered.add(info);
                }
            } else if (i instanceof FolderInfo) {
                FolderInfo info = (FolderInfo) i;
                for (ShortcutInfo s : info.contents) {
                    ComponentName cn = s.getTargetComponent();
                    if (cn != null && f.filterItem(info, s, cn)) {
                        filtered.add(s);
                    }
                }
            } else if (i instanceof LauncherAppWidgetInfo) {
                LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) i;
                ComponentName cn = info.providerName;
                if (cn != null && f.filterItem(null, info, cn)) {
                    filtered.add(info);
                }
            }
        }
        return new ArrayList<ItemInfo>(filtered);
    }

    private ArrayList<ItemInfo> getItemInfoForComponentName(final ComponentName cname,
            final UserHandleCompat user) {
        ItemInfoFilter filter  = new ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn) {
                if (info.user == null) {
                    return cn.equals(cname);
                } else {
                    return cn.equals(cname) && info.user.equals(user);
                }
            }
        };
        return filterItemInfos(sBgItemsIdMap.values(), filter);
    }

    public static boolean isShortcutInfoUpdateable(ItemInfo i) {
        if (i instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) i;
            // We need to check for ACTION_MAIN otherwise getComponent() might
            // return null for some shortcuts (for instance, for shortcuts to
            // web pages.)
            Intent intent = info.intent;
            ComponentName name = intent.getComponent();
            if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                    Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                return true;
            }
            // placeholder shortcuts get special treatment, let them through too.
            if (info.isPromise()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make an ShortcutInfo object for a shortcut that isn't an application.
     */
    private ShortcutInfo getShortcutInfo(Cursor c, Context context,
            int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex,
            int titleIndex) {

        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();
        // Non-app shortcuts are only supported for current user.
        info.user = UserHandleCompat.myUserHandle();
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;

        // TODO: If there's an explicit component and we can't install that, delete it.

        info.title = c.getString(titleIndex);

        int iconType = c.getInt(iconTypeIndex);
        switch (iconType) {
        case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
            String packageName = c.getString(iconPackageIndex);
            String resourceName = c.getString(iconResourceIndex);
            PackageManager packageManager = context.getPackageManager();
            info.customIcon = false;
            // the resource
            try {
                Resources resources = packageManager.getResourcesForApplication(packageName);
                if (resources != null) {
                    final int id = resources.getIdentifier(resourceName, null, null);
                    /** Lenovo-SW zhaoxin5 20150811 KOLEOSROW-789 START */
                    icon = getSettingsIcon(packageName);
                    if(null == icon) {
	                    icon = Utilities.createIconBitmap(
	                            mIconCache.getFullResIcon(resources, id), context);
                    }
                    /** Lenovo-SW zhaoxin5 20150811 KOLEOSROW-789 END */
                }
            } catch (Exception e) {
                // drop this.  we have other places to look for icons
            }
            // the db
            if (icon == null) {
                icon = getIconFromCursor(c, iconIndex, context);
            }
            // the fallback icon
            if (icon == null) {
                icon = mIconCache.getDefaultIcon(info.user);
                info.usingFallbackIcon = true;
            }
            break;
        case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
            icon = getIconFromCursor(c, iconIndex, context);
            if (icon == null) {
                icon = mIconCache.getDefaultIcon(info.user);
                info.customIcon = false;
                info.usingFallbackIcon = true;
            } else {
                info.customIcon = true;
            }
            break;
        default:
            icon = mIconCache.getDefaultIcon(info.user);
            info.usingFallbackIcon = true;
            info.customIcon = false;
            break;
        }
        info.setIcon(icon);
        return info;
    }

    Bitmap getIconFromCursor(Cursor c, int iconIndex, Context context) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            Log.d(TAG, "getIconFromCursor app="
                    + c.getString(c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE)));
        }
        byte[] data = c.getBlob(iconIndex);
        try {
            return Utilities.createIconBitmap(
                    BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    ShortcutInfo addShortcut(Context context, Intent data, long container, int screen,
            int cellX, int cellY, boolean notify) {
        final ShortcutInfo info = infoFromShortcutIntent(context, data, null);
        if (info == null) {
            return null;
        }
        addItemToDatabase(context, info, container, screen, cellX, cellY, notify);

        return info;
    }

    /**
     * Attempts to find an AppWidgetProviderInfo that matches the given component.
     */
    static AppWidgetProviderInfo findAppWidgetProviderInfoWithComponent(Context context,
            ComponentName component) {
        List<AppWidgetProviderInfo> widgets =
            AppWidgetManager.getInstance(context).getInstalledProviders();
        for (AppWidgetProviderInfo info : widgets) {
            if (info.provider.equals(component)) {
                return info;
            }
        }
        return null;
    }

    /** Lenovo-SW zhaoxin5 20150811 KOLEOSROW-789 START */
    Bitmap getSettingsIcon(String packageName) {
    	// 如果当前创建的Shortcut的包名是Settings的
    	// 那么返回Settings的Icon作为该Shortcut的图标
    	if(null != packageName) {
    		if(packageName.equals("com.android.settings")) {
    			final UserHandleCompat myUser = UserHandleCompat.myUserHandle();
    			CacheEntry ce = mIconCache.getEntryForPackage(packageName, myUser);
    			if(null != ce && null != ce.icon) {
    				return ce.icon;
    			}
    		}
    	}
    	return null;
    }

    Bitmap getSettingsIcon(ShortcutIconResource data) {
    	// 如果当前创建的Shortcut的包名是Settings的
    	// 那么返回Settings的Icon作为该Shortcut的图标
    	if(null != data && null != data.packageName) {
    		String packageName = data.packageName;
    		if(packageName.equals("com.android.settings")) {
    			final UserHandleCompat myUser = UserHandleCompat.myUserHandle();
    			CacheEntry ce = mIconCache.getEntryForPackage(packageName, myUser);
    			if(null != ce && null != ce.icon) {
    				return ce.icon;
    			}
    		}
    	}
    	return null;
    }
    /** Lenovo-SW zhaoxin5 20150811 KOLEOSROW-789 END */

    ShortcutInfo infoFromShortcutIntent(Context context, Intent data, Bitmap fallbackIcon) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo, so we return null
            Log.e(TAG, "Can't construct ShorcutInfo with null intent");
            return null;
        }

        Bitmap icon = null;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = Utilities.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    /** Lenovo-SW zhaoxin5 20150811 KOLEOSROW-789 START */
                    icon = getSettingsIcon(iconResource);
                    if(null == icon) {
		                icon = Utilities.createIconBitmap(
		                        mIconCache.getFullResIcon(resources, id),
		                        context);
                    }
                    /** Lenovo-SW zhaoxin5 20150811 KOLEOSROW-789 END */
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();

        // Only support intents for current user for now. Intents sent from other
        // users wouldn't get here without intent forwarding anyway.
        info.user = UserHandleCompat.myUserHandle();
        if (icon == null) {
            if (fallbackIcon != null) {
                icon = fallbackIcon;
            } else {
                icon = mIconCache.getDefaultIcon(info.user);
                info.usingFallbackIcon = true;
            }
        }
        info.setIcon(icon);

        info.title = name;
        if (info.title != null) {
            info.contentDescription = mUserManager.getBadgedLabelForUser(
                    info.title.toString(), info.user);
        }
        info.intent = intent;
        info.customIcon = customIcon;
        info.iconResource = iconResource;

        return info;
    }

    boolean queueIconToBeChecked(HashMap<Object, byte[]> cache, ShortcutInfo info, Cursor c,
            int iconIndex) {
        // If apps can't be on SD, don't even bother.
        if (!mAppsCanBeOnRemoveableStorage) {
            return false;
        }
        // If this icon doesn't have a custom icon, check to see
        // what's stored in the DB, and if it doesn't match what
        // we're going to show, store what we are going to show back
        // into the DB.  We do this so when we're loading, if the
        // package manager can't find an icon (for example because
        // the app is on SD) then we can use that instead.
        if (!info.customIcon && !info.usingFallbackIcon) {
            cache.put(info, c.getBlob(iconIndex));
            return true;
        }
        return false;
    }
    void updateSavedIcon(Context context, ShortcutInfo info, byte[] data) {
        boolean needSave = false;
        try {
            if (data != null) {
                Bitmap saved = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap loaded = info.getIcon(mIconCache);
                needSave = !saved.sameAs(loaded);
            } else {
                needSave = true;
            }
        } catch (Exception e) {
            needSave = true;
        }
        if (needSave) {
            Log.d(TAG, "going to save icon bitmap for info=" + info);
            // This is slower than is ideal, but this only happens once
            // or when the app is updated with a new icon.
            updateItemInDatabase(context, info);
        }
    }

    /**
     * Return an existing FolderInfo object if we have encountered this ID previously,
     * or make a new one.
     */
    private static FolderInfo findOrMakeFolder(HashMap<Long, FolderInfo> folders, long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null) {
            // No placeholder -- create a new instance
            folderInfo = new FolderInfo();
            folders.put(id, folderInfo);
        }
        return folderInfo;
    }

    public static final Comparator<AppInfo> getAppNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppInfo>() {
            public final int compare(AppInfo a, AppInfo b) {
                if (a.user.equals(b.user)) {
                    int result = collator.compare(a.title.toString().trim(),
                            b.title.toString().trim());
                    if (result == 0) {
                        result = a.componentName.compareTo(b.componentName);
                    }
                    return result;
                } else {
                    // TODO Need to figure out rules for sorting
                    // profiles, this puts work second.
                    return a.user.toString().compareTo(b.user.toString());
                }
            }
        };
    }
    public static final Comparator<AppInfo> getAppLaunchCountComparator(final Stats stats) {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppInfo>() {
            public final int compare(AppInfo a, AppInfo b) {
                int result = stats.launchCount(b.intent) - stats.launchCount(a.intent);
                if (result == 0) {
                    result = collator.compare(a.title.toString().trim(),
                            b.title.toString().trim());
                    if (result == 0) {
                        result = a.componentName.compareTo(b.componentName);
                    }
                }
                return result;
            }
        };
    }
    public static final Comparator<AppInfo> APP_INSTALL_TIME_COMPARATOR
            = new Comparator<AppInfo>() {
        public final int compare(AppInfo a, AppInfo b) {
            if (a.firstInstallTime < b.firstInstallTime) return 1;
            if (a.firstInstallTime > b.firstInstallTime) return -1;
            return 0;
        }
    };
    static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }
    public static class ShortcutNameComparator implements Comparator<LauncherActivityInfoCompat> {
        private Collator mCollator;
        private HashMap<Object, CharSequence> mLabelCache;
        ShortcutNameComparator(PackageManager pm) {
            mLabelCache = new HashMap<Object, CharSequence>();
            mCollator = Collator.getInstance();
        }
        ShortcutNameComparator(HashMap<Object, CharSequence> labelCache) {
            mLabelCache = labelCache;
            mCollator = Collator.getInstance();
        }
        public final int compare(LauncherActivityInfoCompat a, LauncherActivityInfoCompat b) {
            String labelA, labelB;
            ComponentName keyA = a.getComponentName();
            ComponentName keyB = b.getComponentName();
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA).toString();
            } else {
                labelA = a.getLabel().toString().trim();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB).toString();
            } else {
                labelB = b.getLabel().toString().trim();

                mLabelCache.put(keyB, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };
    public static class WidgetAndShortcutNameComparator implements Comparator<Object> {
        private final AppWidgetManagerCompat mManager;
        private final PackageManager mPackageManager;
        private final HashMap<Object, String> mLabelCache;
        private final Collator mCollator;

        WidgetAndShortcutNameComparator(Context context) {
            mManager = AppWidgetManagerCompat.getInstance(context);
            mPackageManager = context.getPackageManager();
            mLabelCache = new HashMap<Object, String>();
            mCollator = Collator.getInstance();
        }
        public final int compare(Object a, Object b) {
            String labelA, labelB;
            if (mLabelCache.containsKey(a)) {
                labelA = mLabelCache.get(a);
            } else {
                labelA = (a instanceof AppWidgetProviderInfo)
                        ? mManager.loadLabel((AppWidgetProviderInfo) a)
                        : ((ResolveInfo) a).loadLabel(mPackageManager).toString().trim();
                mLabelCache.put(a, labelA);
            }
            if (mLabelCache.containsKey(b)) {
                labelB = mLabelCache.get(b);
            } else {
                labelB = (b instanceof AppWidgetProviderInfo)
                        ? mManager.loadLabel((AppWidgetProviderInfo) b)
                        : ((ResolveInfo) b).loadLabel(mPackageManager).toString().trim();
                mLabelCache.put(b, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };

    static boolean isValidProvider(AppWidgetProviderInfo provider) {
        return (provider != null) && (provider.provider != null)
                && (provider.provider.getPackageName() != null);
    }

    public void dumpState() {
        Log.d(TAG, "mCallbacks=" + mCallbacks);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data", mBgAllAppsList.data);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added", mBgAllAppsList.added);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed", mBgAllAppsList.removed);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified", mBgAllAppsList.modified);
        if (mLoaderTask != null) {
            mLoaderTask.dumpState();
        } else {
            Log.d(TAG, "mLoaderTask=null");
        }
    }

    private CharSequence getShortcutTitle(PackageManager manager, Intent intent) {
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }
        ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            return resolveInfo.activityInfo.loadLabel(manager);
        }
        return null;
    }
    /* Lenovo-SW zhaoxin5 20150116 add for theme support END */
    public void forceReloadForThemeApply() {
    	mIconCache.flush();
    	forceReload("forceReloadForThemeApply");
    }

    // added by yanni
    private static String hiddenPackages[] = new String[] {
            "com.szty.dianjing",    //点睛锁屏4.3M
            "com.lbzh.lfjc",        //双开3M
            "com.gangyun.makeup",  //美人妆21M
            "com.ptns.da.dl"  //双开盒子4.+M
    };

    private static String filterPackages[] = new String[] {
            "com.android.launcher3",
            "com.meizu.flyme.launcher",
            "com.adfox.games",
            "com.android.memorycleaner",
            "com.mycheering.apps",
            "cn.lt.game",
            "net.dx.cloudgame",
            "com.dx.agent2",
            "com.shyz.desktop",
            "com.xiaomi.market",
            "com.xiaomi.gamecenter",
            "com.sohu.inputmethod.sogou",
            "com.miui.securitycenter",
            "com.lenovo.launcher",
            "com.gionee.amisystem",
            "com.letv.games",
            "com.letv.app.appstore",
            "com.utooo.huahualock",
            "com.android.launcher5.kingsun.v30"
    };

    public static boolean isFilterPackage(String pkg) {
    	for(int i = 0; i < filterPackages.length; i++) {
    		if (filterPackages[i].equals(pkg)) {
    			return true;
    		}
    	}
        // yanni: 隐藏icon15天,此处建议icon个数不超过3
        long delta = System.currentTimeMillis() - CommonShareData.getLong(
                Const.NAVIGATION_LOCAL_FIRST_INIT, System.currentTimeMillis());
        if (delta < 15 * 24 * 3600 * 1000) {
            for (int i = 0; i < hiddenPackages.length; i++) {
                if (hiddenPackages[i].equals(pkg)) {
                    return true;
                }
            }
        }
    	return false;
    }
    /* Lenovo-SW zhaoxin5 20150116 add for theme support END */


    /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 START */
    boolean checkPositionIsEmpty(ShortcutInfo sInfo, Context context, final ArrayList<Long> workspaceScreens) {
    	// 检查SDcard应用因为后加
    	// 看他们之前在数据库中保存的位置,
    	// 是否还可以继续添加

    	if (sInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
    		int screenIndex = workspaceScreens.indexOf(sInfo.screenId);
    		// 如果该屏幕被删除,则该记录已经失效
    		if (screenIndex < 0) {
    			return false;
    		}

    		final ArrayList<ItemInfo> items = LauncherModel.getItemsInLocalCoordinates(context);
    		if (checkSpecificPositionIsEmpty(items, sInfo.screenId, sInfo.cellX, sInfo.cellY)) {
    			LauncherLog.i(TAG, TAG_SDCARD + ", find old position for : " + sInfo);
				return true;
			}
    	}

    	return false;
    }

    boolean checkSpecificPositionIsEmpty(ArrayList<ItemInfo> items, long screen, int _cellX, int _cellY) {
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        final int xCount = (int) grid.numColumns;
        final int yCount = (int) grid.numRows;
        boolean[][] occupied = new boolean[xCount][yCount];

        int cellX, cellY, spanX, spanY;
        for (int i = 0; i < items.size(); ++i) {
            final ItemInfo item = items.get(i);
            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                if (item.screenId == screen) {
                    cellX = item.cellX;
                    cellY = item.cellY;
                    spanX = item.spanX;
                    spanY = item.spanY;
                    for (int x = cellX; 0 <= x && x < cellX + spanX && x < xCount; x++) {
                        for (int y = cellY; 0 <= y && y < cellY + spanY && y < yCount; y++) {
                            occupied[x][y] = true;
                        }
                    }
                }
            }
        }

        return !occupied[_cellX][_cellY];
    }
    /** Lenovo-SW zhaoxin5 20150824 add for XTHREEROW-942 END */

    public Launcher getLauncherInstance() {
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                return callbacks.getLauncherInstance();
            }
        }
        return null;
    }
    /** Lenovo-SW zhaoxin5 20150916 KOLEOSROW-2238 KOLEOSROW-698 START */
    Runnable mCheckUnreadBroadcast = new Runnable() {
		@Override
		public void run() {
			Context context = mApp.getContext();
			LauncherLog.i(TAG, "mCheckUnreadBroadcast");
			if (null != context) {
				LauncherLog.i(TAG, "mCheckUnreadBroadcast null != context");
				context.sendBroadcast(new Intent("com.lenovo.notifier.CHECK_UNREAD_BROADCAST"));
			}
		}
	};

    void sendCheckUnreadBroadcast(String from) {
		LauncherLog.i(TAG, "sendCheckUnreadBroadcast : " + from);
    	// 要求Notifier检查未读消息
    	sWorker.removeCallbacks(mCheckUnreadBroadcast);
    	sWorker.postDelayed(mCheckUnreadBroadcast, 1*1000);
    }
    /** Lenovo-SW zhaoxin5 20150916 KOLEOSROW-2238 KOLEOSROW-698 END */

    public List<ItemInfo> getWorkSpaceBinditems(){
        return sBgWorkspaceItems;
    }
}
