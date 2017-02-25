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
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.PagedView.PageSwitchListener;
import com.android.launcher3.PagedView.TransitionEffect;
import com.android.launcher3.backup.LbkSettingsLoader;
import com.android.launcher3.backup.LbkWallpaperLoader;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.PackageInstallerCompat.PackageInstallInfo;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.settings.SettingsController;
import com.android.launcher3.settings.SettingsProvider;
import com.android.launcher3.settings.SettingsValue;
import com.kappmob.sdk.csc.Csc;
import com.klauncher.cplauncher.vxny.Launch;
import com.klauncher.ext.ClockWidgetProvider;
import com.klauncher.ext.ClockWidgetView;
import com.klauncher.ext.KLauncherApplication;
import com.klauncher.ext.LauncherLog;
import com.klauncher.ext.SearchActivity;
import com.klauncher.ext.SearchWidgetView;
import com.klauncher.ext.ThemeResourceUtils;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.klauncher.theme.ThemeController;
import com.klauncher.upgrade.UpgradeHelper;
import com.klauncher.utilities.DeviceInfoUtils;
import com.klauncher.utilities.LogUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default launcher application.
 */
@SuppressLint("UseSparseArrays")
@SuppressWarnings("unused")
public class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener, PageSwitchListener, LauncherProviderChangeListener,
                   OnShareToApkFileSearchEndListener {
    static final String TAG = "Launcher";
    static final boolean LOGD = false;

    DeviceProfile mGrid;

    static final boolean PROFILE_STARTUP = false;
    static final boolean DEBUG_WIDGETS = false;
    static final boolean DEBUG_STRICT_MODE = false;
    static final boolean DEBUG_RESUME_TIME = false;
    static final boolean DEBUG_DUMP_LOG = false;

    static final boolean ENABLE_DEBUG_INTENTS = false; // allow DebugIntents to run

    private static final int REQUEST_CREATE_SHORTCUT = 1;//shortcut
    private static final int REQUEST_CREATE_APPWIDGET = 5;//appwidget
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;

    private static final int REQUEST_BIND_APPWIDGET = 11;
    private static final int REQUEST_RECONFIGURE_APPWIDGET = 12;
    public static final int REQUEST_TRANSITION_EFFECTS = 14;

    private static final float OVERSHOOT_TENSION = 1.4f;

    static final int REQUEST_PICK_ICON = 13;

    private static final int REQUEST_LOCK_PATTERN = 14;

    private static final String IS_SHOW_SK_SC_DIALOG_AGAIN_QQ = "is_show_shuangkai_sc_dialog_again_qq";
    private static final String IS_SHOW_SK_SC_DIALOG_AGAIN_WECHAT = "is_show_shuangkai_sc_dialog_again_wechat";

    /**
     * IntentStarter uses request codes starting with this. This must be greater than all activity
     * request codes used internally.
     */
    protected static final int REQUEST_LAST = 100;

    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final int SCREEN_COUNT = 5;
    static final int DEFAULT_SCREEN = 2;

    private static final String PREFERENCES = "launcher.preferences";
    // To turn on these properties, type
    // adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
    static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
    static final String DUMP_STATE_PROPERTY = "launcher_dump_state";
    private static final String DISABLE_ALL_APPS_PROPERTY = "launcher_noallapps";

    // The Intent extra that defines whether to ignore the launch animation
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.android.launcher3.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: int
    private static final String RUNTIME_STATE = "launcher.state";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
    // Type: parcelable
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";
    // Type: parcelable
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_ID = "launcher.add_widget_id";
    // Type: int[]
    private static final String RUNTIME_STATE_VIEW_IDS = "launcher.view_ids";

    static final String INTRO_SCREEN_DISMISSED = "launcher.intro_screen_dismissed";
    static final String FIRST_RUN_ACTIVITY_DISPLAYED = "launcher.first_run_activity_displayed";

    static final String FIRST_LOAD_COMPLETE = "launcher.first_load_complete";
    static final String ACTION_FIRST_LOAD_COMPLETE =
            "com.android.launcher3.action.FIRST_LOAD_COMPLETE";

    private static final String TOOLBAR_ICON_METADATA_NAME = "com.android.launcher.toolbar_icon";
    private static final String TOOLBAR_SEARCH_ICON_METADATA_NAME =
            "com.android.launcher.toolbar_search_icon";
    private static final String TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME =
            "com.android.launcher.toolbar_voice_search_icon";

    public static final String SHOW_WEIGHT_WATCHER = "debug.show_mem";
    public static final boolean SHOW_WEIGHT_WATCHER_DEFAULT = false;

    public static final String USER_HAS_MIGRATED = "launcher.user_migrated_from_old_data";

    /**
     * The different states that Launcher can be in.
     */
    private enum State {
        NONE, WORKSPACE, APPS_CUSTOMIZE, APPS_CUSTOMIZE_SPRING_LOADED
    };
    private State mState = State.WORKSPACE;
    private AnimatorSet mStateAnimation;

    private boolean mIsSafeModeEnabled;

    static final int APPWIDGET_HOST_ID = 1024;
    public static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
    private static final int ON_ACTIVITY_RESULT_ANIMATION_DELAY = 500;
    private static final int ACTIVITY_START_DELAY = 1000;

    private static final Object sLock = new Object();
    private static int sScreen = DEFAULT_SCREEN;

    private HashMap<Integer, Integer> mItemIdToViewId = new HashMap<Integer, Integer>();
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    // How long to wait before the new-shortcut animation automatically pans the workspace
    private static int NEW_APPS_PAGE_MOVE_DELAY = 500;
    private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 5;
    private static int NEW_APPS_ANIMATION_DELAY = 500;
    private static final int SINGLE_FRAME_DELAY = 16;

    private final BroadcastReceiver mCloseSystemDialogsReceiver
            = new CloseSystemDialogsIntentReceiver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    protected Workspace mWorkspace;
    private View mLauncherView;
    private View mPageIndicators;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private View mWeightWatcher;
    private DynamicGridSizeFragment mDynamicGridSizeFragment;
    private LauncherClings mLauncherClings;
    protected HiddenFolderFragment mHiddenFolderFragment;

    private AppWidgetManagerCompat mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;
    private int mPendingAddWidgetId = -1;

    private int[] mTmpAddItemCellCoordinates = new int[2];

    private FolderInfo mFolderInfo;

    protected FolderIcon mHiddenFolderIcon;
    private boolean mHiddenFolderAuth = false;

    private Hotseat mHotseat;
    private ViewGroup mOverviewPanel;
    /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
    private ViewGroup mOverviewPanelAndroid;
    /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
    /** Lenovo-SW luyy1 20150910 add for primary home screen START */
    private View mDefaultHomeScreenPanel;
    public View getDefaultHomeScreenPanel() {
        return mDefaultHomeScreenPanel;
    }
    /** Lenovo-SW luyy1 20150910 add for primary home screen END */
    private View mDarkPanel;
    OverviewSettingsPanel mOverviewSettingsPanel;
    // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
    private MenuController mMenuController;
    private XDockView mDockView;
    private WidgetListViewSwitcher mWidgetListPanel = null;
    private WallpaperListViewSwitcher mWallpaperListPanel = null;
    private View mSettingsPanel = null;
    private SettingsController mSettingsController = null;
    private boolean mIsSettingPanelShowing = false;

    private QuickShareGridView mQuickShareGridView = null;
    private LinearLayout mQuickShareZone = null;
    // Lenovo-sw:yuanyl2, Add edit mode function. End.

    private View mAllAppsButton;
    protected RecyclerView mAppDrawer;
    private AppDrawerListAdapter mAppDrawerAdapter;
    private AppDrawerScrubber mScrubber;

    protected SearchDropTargetBar mSearchDropTargetBar;
    private AppsCustomizeTabHost mAppsCustomizeTabHost;
    private AppsCustomizePagedView mAppsCustomizeContent;
    private boolean mAutoAdvanceRunning = false;
    private View mQsb;

    private Bundle mSavedState;
    // We set the state in both onCreate and then onNewIntent in some cases, which causes both
    // scroll issues (because the workspace may not have been measured yet) and extra work.
    // Instead, just save the state that we need to restore Launcher to, and commit it in onResume.
    private State mOnResumeState = State.NONE;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mDynamicGridUpdateRequired = false;

    private boolean mPaused = true;
    private boolean mRestoring;
    private boolean mWaitingForResult;
    private boolean mOnResumeNeedsLoad;
    /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success START */
    // 备份还原过程中,调用setLoadOnResume函数时候,
    // 如果传递的flags不为空,则在onResume过程中,
    // 根据这个flags来加载LoaderTask
    private int mOnResumeLoaderTaskFlags = Integer.MIN_VALUE;
    /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success START */

    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();
    private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<Runnable>();

    private Bundle mSavedInstanceState;

    private Dialog mTransitionEffectDialog;

    protected LauncherModel mModel;
    private IconCache mIconCache;
    private boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mHasFocus = false;
    private boolean mAttached = false;

    private static LocaleConfiguration sLocaleConfiguration = null;

    private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();

    private View.OnTouchListener mHapticFeedbackTouchListener;

    // Related to the auto-advancing of widgets
    private final int ADVANCE_MSG = 1;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceSentTime;
    private long mAutoAdvanceTimeLeft = -1;
    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance =
            new HashMap<View, AppWidgetProviderInfo>();

    // Determines how long to wait after a rotation before restoring the screen orientation to
    // match the sensor state.
    private final int mRestoreScreenOrientationDelay = 500;

    // External icons saved in case of resource changes, orientation, etc.
    private static Drawable.ConstantState[] sGlobalSearchIcon = new Drawable.ConstantState[2];
    private static Drawable.ConstantState[] sVoiceSearchIcon = new Drawable.ConstantState[2];

    private Drawable mWorkspaceBackgroundDrawable;

    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();
    private static final boolean DISABLE_SYNCHRONOUS_BINDING_CURRENT_PAGE = false;

    static final ArrayList<String> sDumpLogs = new ArrayList<String>();
    static Date sDateStamp = new Date();
    static DateFormat sDateFormat =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    static long sRunStart = System.currentTimeMillis();
    static final String CORRUPTION_EMAIL_SENT_KEY = "corruptionEmailSent";

    // We only want to get the SharedPreferences once since it does an FS stat each time we get
    // it from the context.
    private SharedPreferences mSharedPrefs;

    private static ArrayList<ComponentName> mIntentsOnWorkspaceFromUpgradePath = null;

    private int mMySearchWidgetScreenId = -1;

    // Holds the page that we need to animate to, and the icon views that we need to animate up
    // when we scroll to that page on resume.
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    private Rect mRectForFolderAnimation = new Rect();

    private BubbleTextView mWaitingForResume;
    // Lenovo-sw luyy1 add 20150729 for Yandex search
    static final String YANDEX_WIDGET_NAME = "ru.yandex.searchplugin.Widget";
    static final String GOOGLE_WIDGET_NAME = "com.google.android.googlequicksearchbox";
    private String mNewWidgetClassName;

    // Preferences
    private boolean mHideIconLabels;
    private AppDrawerListAdapter.DrawerType mDrawerType;

    private Runnable mBuildLayersRunnable = new Runnable() {
        public void run() {
            if (mWorkspace != null) {
                mWorkspace.buildPageHardwareLayers();
            }
        }
    };

    private static PendingAddArguments sPendingAddItem;

    public static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);

    private static class PendingAddArguments {
        int requestCode;
        Intent intent;
        long container;
        long screenId;
        int cellX;
        int cellY;
        int appWidgetId;
    }

    private Stats mStats;

    FocusIndicatorView mFocusHandler;

    public Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator arg0) {
        }

        @Override
        public void onAnimationRepeat(Animator arg0) {
        }

        @Override
        public void onAnimationEnd(Animator arg0) {
            mDarkPanel.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationCancel(Animator arg0) {
        }
    };

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    Runnable mUpdateDynamicGridRunnable = new Runnable() {
        @Override
        public void run() {
            updateDynamicGrid();
        }
    };

    private BroadcastReceiver protectedAppsChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the workspace
            if (waitUntilResume(mUpdateDynamicGridRunnable, true)) {
                return;
            }

            updateDynamicGrid();
        }
    };

    public class MockAppWidgetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.e("MockAppWidgetReceiver", Launcher.this.getPackageName() + "; " + ClockWidgetProvider.class.getName());
            AppWidgetProviderInfo widgetInfo = LauncherModel.findAppWidgetProviderInfoWithComponent(
                    Launcher.this, new ComponentName(Launcher.this.getPackageName(), ClockWidgetProvider.class.getName()));
            if (widgetInfo != null) {
                LogUtil.e("MockAppWidgetReceiver", widgetInfo.toString());
                PendingAddWidgetInfo paddingWidgetInfo = new PendingAddWidgetInfo(widgetInfo, null, null);
                addWidgetAfterAnimation(paddingWidgetInfo);
            }
        }
    }
    MockAppWidgetReceiver mockAppReceiver;
    private void registerAppWidgetReceiver() {
        mockAppReceiver = new MockAppWidgetReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klauncher.action.MOCK_APP_WIDGET");
        registerReceiver(mockAppReceiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        super.onCreate(savedInstanceState);
        LauncherLog.i("xixia", "onCreate");
        initializeDynamicGrid();
        mHideIconLabels = SettingsProvider.getBoolean(this,
                SettingsProvider.SETTINGS_UI_HOMESCREEN_HIDE_ICON_LABELS,
                R.bool.preferences_interface_homescreen_hide_icon_labels_default);

        // the LauncherApplication should call this, but in case of Instrumentation it might not be present yet
        /*mSharedPrefs: 桌面在第一次启动的时候会有一个指导界面，告诉用户怎么使用，在以后的启动中，就不会再出现了。
        控制这个逻辑就是通过SharedPreference，他以key-value的形式把信息存储到一个xml文件当中。
         */
        mSharedPrefs = getSharedPreferences(LauncherAppState.getSharedPreferencesKey(),
                Context.MODE_PRIVATE);
        mIsSafeModeEnabled = getPackageManager().isSafeMode();
        //mDragController: 用来控制拖拽对象的东西，与DragLayer结合起使用
        mDragController = new DragController(this);
        //mInflater: 在图标加载的时候，会一个一个去生成BubbleTextView，
        //而这些都是通过mInflater.inflate(..)方法从xml生成出来的，使用次数还是挺多的，详情参加Launcher.creatShortcut方法。
        mInflater = getLayoutInflater();

        mStats = new Stats(this);
        //mAppWidgetManager用来管理小工具，mAppWidgetHost用来生成小工具的View对象并更新。
        mAppWidgetManager = AppWidgetManagerCompat.getInstance(this);

        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        // If we are getting an onCreate, we can actually preempt onResume and unset mPaused here,
        // this also ensures that any synchronous binding below doesn't re-trigger another
        // LauncherModel load.
        mPaused = false;

        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing(
                    Environment.getExternalStorageDirectory() + "/launcher");
        }
        //语言环境判断
        checkForLocaleChange();
        setContentView(R.layout.launcher);
        //初始化控件 正常配置
        setupViews();
        mGrid.layout(this);

        registerContentObservers();

        lockAllApps();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);
        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        if (!mRestoring) {//不需要恢复 第一次加载
            if (DISABLE_SYNCHRONOUS_BINDING_CURRENT_PAGE) {
                LauncherLog.i("xixia", "onCreate befor loader task 1");
                // If the user leaves launcher, then we should just load items asynchronously when
                // they return.
                /* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
                //Launcher 加载lbk数据入口
                mModel.startLoader(true, PagedView.INVALID_RESTORE_PAGE, ModeSwitchHelper.getLauncherModelLoaderTaskLoaderFlag(this));
            	/* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
            } else {//从 lbk文件恢复
                LauncherLog.i("xixia", "onCreate befor loader task 2");
                // We only load the page synchronously if the user rotates (or triggers a
                // configuration change) while launcher is in the foreground
            	/* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
                mModel.startLoader(true, mWorkspace.getRestorePage(), ModeSwitchHelper.getLauncherModelLoaderTaskLoaderFlag(this));
            	/* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
            }
        }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);

        updateGlobalIcons();

        // On large interfaces, we want the screen to auto-rotate based on the current orientation
        unlockScreenOrientation(true);

        if (shouldShowIntroScreen()) {
            showIntroScreen();
        } else {
            showFirstRunActivity();
            showFirstRunClings();
        }
        IntentFilter protectedAppsFilter = new IntentFilter(
                "cyanogenmod.intent.action.PROTECTED_COMPONENT_UPDATE");
        registerReceiver(protectedAppsChangedReceiver, protectedAppsFilter,
                "cyanogenmod.permission.PROTECTED_APP", null);

        /** Lenovo-SW zhaoxin5 20150721 add for auto-reorder support START */
        LauncherAppState.getLauncherProvider().setLauncherProviderChangeListener(this);
        /** Lenovo-SW zhaoxin5 20150721 add for auto-reorder support END */
        // Support theme
//        applyDefaultTheme();
        //vivo error
        registerAppWidgetReceiver();

//        UpgradeHelper.getInstance(this).startUpgradeMission();
    }

    @Override
    public void onLauncherProviderChange() {
        /** Lenovo-SW zhaoxin5 20150721 add for auto-reorder support START */
    	/*if(!isAnimating() && !mModel.isLoadingWorkspace()) {
    		autoReorder();
    		// Lenovo-SW zhaoxin5 close for duplicate icons
    	}*/
        /** Lenovo-SW zhaoxin5 20150721 add for auto-reorder support END */
    }

    private void initializeDynamicGrid() {
        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();

        mHideIconLabels = SettingsProvider.getBoolean(this,
                SettingsProvider.SETTINGS_UI_HOMESCREEN_HIDE_ICON_LABELS,
                R.bool.preferences_interface_homescreen_hide_icon_labels_default);
        mDrawerType = AppDrawerListAdapter.DrawerType.getModeForValue(
                SettingsProvider.getInt(this,
                        SettingsProvider.SETTINGS_UI_DRAWER_TYPE,
                        R.integer.preferences_interface_drawer_type_default));

        // Determine the dynamic grid properties
        Point smallestSize = new Point();
        Point largestSize = new Point();
        Point realSize = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getCurrentSizeRange(smallestSize, largestSize);
        display.getRealSize(realSize);
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        // Lazy-initialize the dynamic grid
        mGrid = app.initDynamicGrid(this,
                Math.min(smallestSize.x, smallestSize.y),
                Math.min(largestSize.x, largestSize.y),
                realSize.x, realSize.y,
                dm.widthPixels, dm.heightPixels);
        // TODO  mModel初始化
        //LauncherModel的对象，他是通过LauncherAppState.setLauncher(..)方法获得的，
        // 在其中执行了LauncherModel.initialize(..)方法，他给LaucherModel传递了一个CallBacks对象(在Launcher.java中定义)。
        // 为什么要传这个对象呢，因为LauncherModel主要是负责数据层的东西，界面方面的操作都交给了Launcher来做，那当LauncherModel需要更新界面的时候怎么操作呢，就是通过调用这些个回调函数来完成的。
        mModel = app.setLauncher(this);
        //桌面图标缓存 提高性能
        mIconCache = app.getIconCache();
        mIconCache.flushInvalidIcons(mGrid);
    }

    /**
     * To be overriden by subclasses to hint to Launcher that we have custom content
     */
    protected boolean hasCustomContentToLeft() {
        return false;
    }
    /*protected boolean hasCustomContentToLeft() {
        //boolean kinflowSet  = SettingsValue.isKinflowSetOn(this);
        *//*if(!isAnimating() && !mModel.isLoadingWorkspace() &&
                LauncherAppState.getInstance().isCurrentVibeuiMode()) {
            autoReorder(true);
        }*//*
        return true;
    }*/


    /**
     * To be overridden by subclasses to populate the custom content container and call
     * {@link #addToCustomContentPage}. This will only be invoked if
     * {@link #hasCustomContentToLeft()} is {@code true}.
     */
    public void populateCustomContentContainer() {
    }

    private void initializeScrubber() {
    	if (!Utilities.isLmpOrAbove()) {
    		return;
    	}
    	
//        if (mScrubber == null) {
//            FrameLayout view = (FrameLayout) findViewById(R.id.app_drawer_container);
//            mScrubber = (AppDrawerScrubber) view.findViewById(R.id.app_drawer_scrubber);
//            mScrubber.setSource(mAppDrawer);
//            mScrubber.setScrubberIndicator((TextView) view.findViewById(R.id.scrubberIndicator));
//        }
    }

    public void updateScrubber() {
    	if (null != mScrubber) {
    		mScrubber.updateSections();
    	}
    }

    public void initializeAdapter() {
        mAppDrawerAdapter = new AppDrawerListAdapter(this);
        mAppDrawerAdapter.notifyDataSetChanged();
    }

    /**
     * Invoked by subclasses to signal a change to the {@link #addCustomContentToLeft} value to
     * ensure the custom content page is added or removed if necessary.
     */
    //protected
    public void invalidateHasCustomContentToLeft() {
        if (mWorkspace == null || mWorkspace.getScreenOrder().isEmpty()) {
            // Not bound yet, wait for bindScreens to be called.
            return;
        }

        if (!mWorkspace.hasCustomContent() && hasCustomContentToLeft()) {
            // Create the custom content page and call the subclass to populate it.
            mWorkspace.createCustomContentContainer();
            populateCustomContentContainer();
        } else if (mWorkspace.hasCustomContent() && !hasCustomContentToLeft()) {
            mWorkspace.removeCustomContentPage();
        }
    }

    /**
     * 跟新获取搜索 声音图标
     */
    private void updateGlobalIcons() {
        boolean searchVisible = false;
        boolean voiceVisible = false;
        // If we have a saved version of these external icons, we load them up immediately
        int coi = getCurrentOrientationIndexForGlobalIcons();
        if (sGlobalSearchIcon[coi] == null || sVoiceSearchIcon[coi] == null) {
            searchVisible = updateGlobalSearchIcon();
            voiceVisible = updateVoiceSearchIcon(searchVisible);
        }
        if (sGlobalSearchIcon[coi] != null) {
            updateGlobalSearchIcon(sGlobalSearchIcon[coi]);
            searchVisible = true;
        }
        if (sVoiceSearchIcon[coi] != null) {
            updateVoiceSearchIcon(sVoiceSearchIcon[coi]);
            voiceVisible = true;
        }
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }

    /*Lenovo-sw luyy1 add 20150729 for Yandex search start*/
    private LauncherAppWidgetInfo toDelete;
    private void replaceWidget(String oldWidgetPackageName, String newWidgetClassName) {
        ArrayList<LauncherAppWidgetInfo> rmList = new ArrayList<LauncherAppWidgetInfo>();
        for(final LauncherAppWidgetInfo info : LauncherModel.sBgAppWidgets) {
            String packageName = null;
            AppWidgetProviderInfo appWidgetInfo = AppWidgetManager.getInstance(
                    this).getAppWidgetInfo(info.appWidgetId);
            if(appWidgetInfo != null && appWidgetInfo.provider != null) {
                packageName = appWidgetInfo.provider.getPackageName();
            }

            if(packageName!=null && packageName.equals(oldWidgetPackageName)) {
                rmList.add(info);
            }
        }
        if (0 == rmList.size()) {
            return;
        }
        for(int i = 0;i<rmList.size();i++) {
            toDelete = rmList.get(i);
            try {
                ((ViewGroup)(toDelete.hostView.getParent())).removeView( toDelete.hostView );
            } catch (Exception e) {
                e.printStackTrace();
                new Throwable("!!!!WARNING!!!! HostView remove failed .").printStackTrace();
            }
            removeAppWidget(rmList.get(i));
            LauncherModel.deleteItemFromDatabase(this, rmList.get(i));
            final LauncherAppWidgetHost appWidgetHost = getAppWidgetHost();
            if (appWidgetHost != null) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        appWidgetHost.deleteAppWidgetId(toDelete.appWidgetId);
                    }
                }.start();
            }
        }
        for(int i = 0;i<rmList.size();i++) {
            installWidgetByClassName(newWidgetClassName, rmList.get(i).cellX, rmList.get(i).cellY,
                    (int)rmList.get(i).screenId,rmList.get(i).spanX,rmList.get(i).spanY);
        }
    }
    //error  void android.view.View.bringToFront()' on a null object reference
    private void installWidgetByClassName(String classname, int x, int y, int screen,int spanX,int spanY) {
        Log.d(TAG, "Position==> CellXY: " + x + "," + y + " Screen: " + screen + " SpanXY: " + spanX + "," + spanY);
        List<AppWidgetProviderInfo> widgets = AppWidgetManager
                .getInstance(this).getInstalledProviders();
        for (AppWidgetProviderInfo widget : widgets) {
            if (widget.minWidth > 0 && widget.minHeight > 0) {
                // Ensure that all widgets we show can be added on a workspace
                // of this size
                if (classname != null && classname.equals(widget.provider.getClassName())) {
                    int[] minSpanXY = getMinResizeSpanForWidget(widget, null);

                    PendingAddWidgetInfo createItemInfo = new PendingAddWidgetInfo(
                            widget, null, null);

                    int targetSpanX = spanX;
                    int targetSpanY = spanY;
                    int[] targetSpanXY = new int[] { targetSpanX, targetSpanY };
                    createItemInfo.spanX = targetSpanXY[0];
                    createItemInfo.spanY = targetSpanXY[1];

                    createItemInfo.minSpanX = minSpanXY[0];
                    createItemInfo.minSpanY = minSpanXY[1];

                    addAppWidgetAuto(createItemInfo,
                            LauncherSettings.Favorites.CONTAINER_DESKTOP,
                            screen, new int[] { x, y }, null,targetSpanXY);
                }
            }
        }
    }

    int[] getMinResizeSpanForWidget(AppWidgetProviderInfo info, int[] spanXY) {
        return getSpanForWidget(this,info.provider, info.minResizeWidth, info.minResizeHeight);
    }

    private void addAppWidgetAuto(PendingAddWidgetInfo info, long container, int screen,
            int[] cell, int[] loc,int[] targetSpanXY) {
        resetAddInfo();

        mPendingAddInfo.container = info.container = container;
        mPendingAddInfo.screenId = info.screenId = screen;
        mPendingAddInfo.dropPos = loc;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }

        mPendingAddInfo.spanX = targetSpanXY[0];
        mPendingAddInfo.spanY = targetSpanXY[1];

        int appWidgetId = getAppWidgetHost().allocateAppWidgetId();

        try {
            AppWidgetManager.
            getInstance(this).bindAppWidgetIdIfAllowed(appWidgetId, info.componentName);
        } catch (SecurityException e) {
        }
        // Warning: If com.lenovo.olauncher is in system's, then we enter here. Otherwise,
        // installing Yandex search can only success when there is only one google search widget.
        addAppWidgetImplAuto(appWidgetId, info, null, info.info);
    }
    /*Lenovo-sw luyy1 add 20150729 for Yandex search end*/

    /**
     * 语言环境判断
     */
    private void checkForLocaleChange() {
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {
                @Override
                protected LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                @Override
                protected void onPostExecute(LocaleConfiguration result) {
                    sLocaleConfiguration = result;
                    checkForLocaleChange();  // recursive, but now with a locale configuration
                }
            }.execute();
            return;
        }

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = sLocaleConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = sLocaleConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = sLocaleConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;

            mIconCache.flush();

            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            new AsyncTask<Void, Void, Void>() {
                public Void doInBackground(Void... args) {
                    writeConfiguration(Launcher.this, localeConfiguration);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public Stats getStats() {
        return mStats;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    boolean isDraggingEnabled() {
        // We prevent dragging when we are loading the workspace as it is possible to pick up a view
        // that is subsequently removed from the workspace in startBinding().
        return !mModel.isLoadingWorkspace();
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= 17) {
            return View.generateViewId();
        } else {
            // View.generateViewId() is not available. The following fallback logic is a copy
            // of its implementation.
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }

    public int getViewIdForItem(ItemInfo info) {
        // This cast is safe given the > 2B range for int.
        int itemId = (int) info.id;
        if (mItemIdToViewId.containsKey(itemId)) {
            return mItemIdToViewId.get(itemId);
        }
        int viewId = generateViewId();
        mItemIdToViewId.put(itemId, viewId);
        return viewId;
    }

    /**
     * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
     * a configuration step, this allows the proper animations to run after other transitions.
     */
    private long completeAdd(PendingAddArguments args) {
        long screenId = args.screenId;
        if (args.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            // When the screen id represents an actual screen (as opposed to a rank) we make sure
            // that the drop page actually exists.
            screenId = ensurePendingDropLayoutExists(args.screenId);
        }

        switch (args.requestCode) {
            case REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(args.intent, args.container, screenId, args.cellX,
                        args.cellY);
                break;
            case REQUEST_CREATE_APPWIDGET:
                completeAddAppWidget(args.appWidgetId, args.container, screenId, null, null);
                break;
            case REQUEST_RECONFIGURE_APPWIDGET:
                completeRestoreAppWidget(args.appWidgetId);
                break;
        }
        // Before adding this resetAddInfo(), after a shortcut was added to a workspace screen,
        // if you turned the screen off and then back while in All Apps, Launcher would not
        // return to the workspace. Clearing mAddInfo.container here fixes this issue
        resetAddInfo();
        return screenId;
    }

    @Override
    protected void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        // Reset the startActivity waiting flag
        setWaitingForResult(false);
        final int pendingAddWidgetId = mPendingAddWidgetId;
        mPendingAddWidgetId = -1;

        Runnable exitSpringLoaded = new Runnable() {
            @Override
            public void run() {
                exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED),
                        EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
            }
        };

        if (requestCode == REQUEST_BIND_APPWIDGET) {
            final int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
                mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoaded,
                        ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
            } else if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, mPendingAddInfo, null,
                        mPendingAddWidgetInfo, ON_ACTIVITY_RESULT_ANIMATION_DELAY);
            }
    		/*Lenovo-sw [zhanggx1] [MODEL] [2013-01-16] [begin] (for auto reorder)*/
    		autoReorder("onActivityResult, requestCode == REQUEST_BIND_APPWIDGET");
    		/*Lenovo-sw [zhanggx1] [MODEL] [2013-01-16] [end]*/
            return;
        } else if (requestCode == REQUEST_PICK_WALLPAPER) {
            if (resultCode == RESULT_OK && mWorkspace.isInOverviewMode()) {
                mWorkspace.exitOverviewMode(false);
            }
            return;
        } else if (requestCode == REQUEST_LOCK_PATTERN) {
            mHiddenFolderAuth = true;
            switch (resultCode) {
                case RESULT_OK:
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.setCustomAnimations(0, 0);
                    fragmentTransaction.replace(R.id.launcher, mHiddenFolderFragment,
                            HiddenFolderFragment.HIDDEN_FOLDER_FRAGMENT);
                    fragmentTransaction.commit();
                    break;
                case RESULT_CANCELED:
                    // User failed to enter/confirm a lock pattern, back out
                    break;
            }
            return;
        } else if (requestCode == REQUEST_PICK_APPWIDGET) {
            if (resultCode == RESULT_OK) {
                final int appWidgetId;
                int widgetId = data != null ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        : -1;
                if (widgetId < 0) {
                    appWidgetId = pendingAddWidgetId;
                } else {
                    appWidgetId = widgetId;
                }
                AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

                PendingAddWidgetInfo createItemInfo = getPendingAddWidgetInfo(info);

                int currentScreen = getCurrentWorkspaceScreen();
                Workspace workspace = getWorkspace();
                CellLayout layout = (CellLayout) workspace.getPageAt(currentScreen);

                ItemInfo itemInfo = (ItemInfo) createItemInfo;
                int[] targetCell = new int[2];

                boolean showOutOfSpaceMessage = true;
                if (layout != null) {
                    // it will change span of widget. by zhangxh14
                    // layout.calculateSpans(itemInfo);
                    if (itemInfo != null) {
                        showOutOfSpaceMessage = !layout.findCellForSpan(targetCell, itemInfo.spanX, itemInfo.spanY);
                    } else {
                        showOutOfSpaceMessage = !layout.findCellForSpan(targetCell, 1, 1);
                    }
                }

                if (showOutOfSpaceMessage) {
                    // there are no enough spaces for this widget to add in
                    showOutOfSpaceMessage(false);
                } else {
                    createItemInfo.cellX = targetCell[0];
                    createItemInfo.cellY = targetCell[1];
                    createItemInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                    createItemInfo.screenId = workspace.getIdForScreen(layout);
                    resetAddInfo();
                    setAddInfo(createItemInfo);
                    addAppWidgetImpl(appWidgetId, createItemInfo, null, createItemInfo.info);
                }


            }
            return;
        }

        boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET);

        final boolean workspaceLocked = isWorkspaceLocked();
        // We have special handling for widgets
        if (isWidgetDrop) {
            final int appWidgetId;
            int widgetId = data != null ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    : -1;
            if (widgetId < 0) {
                appWidgetId = pendingAddWidgetId;
            } else {
                appWidgetId = widgetId;
            }

            final int result;
            if (appWidgetId < 0 || resultCode == RESULT_CANCELED) {
                Log.e(TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not " +
                        "returned from the widget configuration activity.");
                result = RESULT_CANCELED;
                completeTwoStageWidgetDrop(result, appWidgetId);
                final Runnable onComplete = new Runnable() {
                    @Override
                    public void run() {
                        exitSpringLoadedDragModeDelayed(false, 0, null);
                    }
                };
                if (workspaceLocked) {
                    // No need to remove the empty screen if we're mid-binding, as the
                    // the bind will not add the empty screen.
                    mWorkspace.postDelayed(onComplete, ON_ACTIVITY_RESULT_ANIMATION_DELAY);
                } else {
                	/** Lenovo-SW zhaoxin5 20150805 XTHREEROW-574 START */
                	if(!mWorkspace.isInOverviewMode()) {
	                    mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete,
	                            ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
                	}
                	/** Lenovo-SW zhaoxin5 20150805 XTHREEROW-574 END */
                }
            } else {
                if (!workspaceLocked) {
                    if (mPendingAddInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                        // When the screen id represents an actual screen (as opposed to a rank)
                        // we make sure that the drop page actually exists.
                        mPendingAddInfo.screenId =
                                ensurePendingDropLayoutExists(mPendingAddInfo.screenId);
                    }
                    final CellLayout dropLayout = mWorkspace.getScreenWithId(mPendingAddInfo.screenId);

                    dropLayout.setDropPending(true);
                    final Runnable onComplete = new Runnable() {
                        @Override
                        public void run() {
                            completeTwoStageWidgetDrop(resultCode, appWidgetId);
                            dropLayout.setDropPending(false);
                        }
                    };
                    /** Lenovo-SW zhaoxin5 20150827 XTHREEROW-1131 START */
                	if(!mWorkspace.isInOverviewMode()) {
                		mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete,
                				ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
                    } else if(null != onComplete) {
                    	mWorkspace.postDelayed(onComplete, ON_ACTIVITY_RESULT_ANIMATION_DELAY);
                    }
                    /** Lenovo-SW zhaoxin5 20150827 XTHREEROW-1131 END */
                } else {
                    PendingAddArguments args = preparePendingAddArgs(requestCode, data, appWidgetId,
                            mPendingAddInfo);
                    sPendingAddItem = args;
                }
            }
            if (resultCode == RESULT_CANCELED) {
                autoReorder("onActivityResult, isWidgetDrop resultCode == RESULT_CANCELED");
            }
            return;
        }

        if (requestCode == REQUEST_RECONFIGURE_APPWIDGET) {
            if (resultCode == RESULT_OK) {
                // Update the widget view.
                PendingAddArguments args = preparePendingAddArgs(requestCode, data,
                        pendingAddWidgetId, mPendingAddInfo);
                if (workspaceLocked) {
                    sPendingAddItem = args;
                } else {
                    completeAdd(args);
                }
            }
            // Leave the widget in the pending state if the user canceled the configure.
            return;
        }

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.
        if (resultCode == RESULT_OK && mPendingAddInfo.container != ItemInfo.NO_ID) {
            final PendingAddArguments args = preparePendingAddArgs(requestCode, data, -1,
                    mPendingAddInfo);
            if (isWorkspaceLocked()) {
                sPendingAddItem = args;
            } else {
                completeAdd(args);
                /** Lenovo-SW zhaoxin5 20150730 XTHREEROW-301 START */
                if(!mWorkspace.isInOverviewMode()) {
	                mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoaded,
	                        ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
                }
                /** Lenovo-SW zhaoxin5 20150730 XTHREEROW-301 END */
            }
        } else if (resultCode == RESULT_CANCELED) {
        	/** Lenovo-SW zhaoxin5 20150805 XTHREEROW-574 START */
        	if(!mWorkspace.isInOverviewMode()) {
        		/** Lenovo-SW zhaoxin5 20151105 PASSIONROW-2249 START */
        		/***
        		 * 这个bug是这么产生的:
        		 * 用户取消添加widget以后,
        		 * 会回退到显示那个添加widget的页面,
        		 * 此时用户同时点击home键,相当于两个操作相反,导致出错
        		 * 
        		 * 修改方法,
        		 * 就是取消直接回退到桌面
        		 */
                Runnable exitSpringLoadedForCanceled = new Runnable() {
                    @Override
                    public void run() {
                        exitSpringLoadedDragModeDelayed((resultCode == RESULT_CANCELED),
                                EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
                    }
                };
	            mWorkspace.removeExtraEmptyScreenDelayed(true, exitSpringLoadedForCanceled,
	                    ON_ACTIVITY_RESULT_ANIMATION_DELAY, false);
        		/** Lenovo-SW zhaoxin5 20151105 PASSIONROW-2249 END */
            }
        	/** Lenovo-SW zhaoxin5 20150805 XTHREEROW-574 END */
        }
        mDragLayer.clearAnimatedView();
        
		/*Lenovo-sw [zhanggx1] [MODEL] [2013-01-16] [begin] (for auto reorder)*/
		autoReorder("onActivityResult, end");
		/*Lenovo-sw [zhanggx1] [MODEL] [2013-01-16] [end]*/
    }

    private PendingAddWidgetInfo getPendingAddWidgetInfo(AppWidgetProviderInfo aInfo) {
        PendingAddWidgetInfo createItemInfo = new PendingAddWidgetInfo(
                aInfo, null, null);
        // Determine the widget spans and min resize spans.
        int[] spanXY = Launcher.getSpanForWidget(this, aInfo);
        createItemInfo.spanX = spanXY[0];
        createItemInfo.spanY = spanXY[1];
        int[] minSpanXY = Launcher.getMinSpanForWidget(this, aInfo);
        createItemInfo.minSpanX = minSpanXY[0];
        createItemInfo.minSpanY = minSpanXY[1];
        createItemInfo.title = aInfo.label;
        return createItemInfo;
    }

    private PendingAddArguments preparePendingAddArgs(int requestCode, Intent data, int
            appWidgetId, ItemInfo info) {
        PendingAddArguments args = new PendingAddArguments();
        args.requestCode = requestCode;
        args.intent = data;
        args.container = info.container;
        args.screenId = info.screenId;
        args.cellX = info.cellX;
        args.cellY = info.cellY;
        args.appWidgetId = appWidgetId;
        return args;
    }

    /**
     * Check to see if a given screen id exists. If not, create it at the end, return the new id.
     *
     * @param screenId the screen id to check
     * @return the new screen, or screenId if it exists
     */
    private long ensurePendingDropLayoutExists(long screenId) {
        CellLayout dropLayout =
                (CellLayout) mWorkspace.getScreenWithId(screenId);
        if (dropLayout == null) {
            // it's possible that the add screen was removed because it was
            // empty and a re-bind occurred
            mWorkspace.addExtraEmptyScreen();
            return mWorkspace.commitExtraEmptyScreen();
        } else {
            return screenId;
        }
    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        CellLayout cellLayout =
                (CellLayout) mWorkspace.getScreenWithId(mPendingAddInfo.screenId);
        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                    mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container,
                            mPendingAddInfo.screenId, layout, null);
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED),
                            EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
        }
        if (mDragLayer.getAnimatedView() != null) {
            mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout,
                    (DragView) mDragLayer.getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else if (onCompleteRunnable != null) {
            // The animated view may be null in the case of a rotation during widget configuration
            onCompleteRunnable.run();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Lenovo-sw zhangyj19 delete 2015/09/10 KOLEOSROW-1309 delete FirstFrameAnimatorHelper function
        //FirstFrameAnimatorHelper.setIsVisible(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Lenovo-sw zhangyj19 delete 2015/09/10 KOLEOSROW-1309 delete FirstFrameAnimatorHelper function
        //FirstFrameAnimatorHelper.setIsVisible(true);
    }

    @Override
    protected void onResume() {
        long startTime = 0;
        if (DEBUG_RESUME_TIME) {
            startTime = System.currentTimeMillis();
            Log.v(TAG, "Launcher.onResume()");
        }
        super.onResume();

        // Umeng Analytics SDK
        MobclickAgent.onResume(this);
        isLoadUUsdkFolderAd();
        // Restore the previous launcher state
        if (mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (mOnResumeState == State.APPS_CUSTOMIZE) {
            showAllApps(false, mAppsCustomizeContent.getContentType(), false);
        }
        mOnResumeState = State.NONE;

        // Background was set to gradient in onPause(), restore to black if in all apps.
        setWorkspaceBackground(mState == State.WORKSPACE);

        mPaused = false;
        if (mRestoring || mOnResumeNeedsLoad) {
            setWorkspaceLoading(true);
            /* Lenovo-SW zhaoxin5 20150529 add for 2 Layer support */
            int flags = (mOnResumeLoaderTaskFlags == Integer.MIN_VALUE ? ModeSwitchHelper.getLauncherModelLoaderTaskLoaderFlag(this) 
            		: mOnResumeLoaderTaskFlags);
            mModel.startLoader(true, PagedView.INVALID_RESTORE_PAGE, flags);
            /* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
            mRestoring = false;
            mOnResumeNeedsLoad = false;
            /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success START */
            mOnResumeLoaderTaskFlags = Integer.MIN_VALUE;
            /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success END */
        }
        if (mBindOnResumeCallbacks.size() > 0) {
            // We might have postponed some bind calls until onResume (see waitUntilResume) --
            // execute them here
            long startTimeCallbacks = 0;
            if (DEBUG_RESUME_TIME) {
                startTimeCallbacks = System.currentTimeMillis();
            }

            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setBulkBind(true);
            }
            for (int i = 0; i < mBindOnResumeCallbacks.size(); i++) {
                mBindOnResumeCallbacks.get(i).run();
            }
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setBulkBind(false);
            }
            mBindOnResumeCallbacks.clear();
            if (DEBUG_RESUME_TIME) {
                Log.d(TAG, "Time spent processing callbacks in onResume: " +
                        (System.currentTimeMillis() - startTimeCallbacks));
            }
        }
        if (mOnResumeCallbacks.size() > 0) {
            for (int i = 0; i < mOnResumeCallbacks.size(); i++) {
                mOnResumeCallbacks.get(i).run();
            }
            mOnResumeCallbacks.clear();
        }

        // Reset the pressed state of icons that were locked in the press state while activities
        // were launching
        if (mWaitingForResume != null) {
            // Resets the previous workspace icon press state
            mWaitingForResume.setStayPressed(false);
        }

        // It is possible that widgets can receive updates while launcher is not in the foreground.
        // Consequently, the widgets will be inflated in the orientation of the foreground activity
        // (framework issue). On resuming, we ensure that any widgets are inflated for the current
        // orientation.
        getWorkspace().reinflateWidgetsIfNecessary();

        // Process any items that were added while Launcher was away.
        InstallShortcutReceiver.disableAndFlushInstallQueue(this);

        // Update the voice search button proxy
        updateVoiceButtonProxyVisible(false);

        // Again, as with the above scenario, it's possible that one or more of the global icons
        // were updated in the wrong orientation.
        updateGlobalIcons();
        if (DEBUG_RESUME_TIME) {
            Log.d(TAG, "Time spent in onResume: " + (System.currentTimeMillis() - startTime));
        }

        if (mWorkspace.getCustomContentCallbacks() != null) {
            // If we are resuming and the custom content is the current page, we call onShow().
            // It is also poassible that onShow will instead be called slightly after first layout
            // if PagedView#setRestorePage was set to the custom content page in onCreate().
            if (mWorkspace.isOnOrMovingToCustomContent()) {
                mWorkspace.getCustomContentCallbacks().onShow(true);
            }
        }
        mWorkspace.updateInteractionForState();
        mWorkspace.onResume();

        PackageInstallerCompat.getInstance(this).onResume();

        //Close out Fragments
        TransitionEffectsFragment tef =
                (TransitionEffectsFragment) getFragmentManager().findFragmentByTag(
                        TransitionEffectsFragment.TRANSITION_EFFECTS_FRAGMENT);
        if (tef != null) {
            tef.setEffect();
        }
        Fragment f = getFragmentManager().findFragmentByTag(
                DynamicGridSizeFragment.DYNAMIC_GRID_SIZE_FRAGMENT);
        if (f != null) {
            mDynamicGridSizeFragment.setSize();
            mWorkspace.hideOutlines();
        }
        Fragment f1 = getFragmentManager().findFragmentByTag(
                HiddenFolderFragment.HIDDEN_FOLDER_FRAGMENT);
        if (f1 != null && !mHiddenFolderAuth) {
            mHiddenFolderFragment.saveHiddenFolderStatus(-1);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction
                    .remove(mHiddenFolderFragment).commit();
        } else {
            mHiddenFolderAuth = false;
        }

        updateGridIfNeeded();
        /* Lenovo-sw luyy1 add start for loop support 2015-08-31
         * If user clickS on Preference button when in Overview Mode, then exitOverviewMode(true)
         * is performed in MenuController.java. Hence, here we need to update SlideLoop flag.*/
        if (mWorkspace != null) {
            mWorkspace.updateSlideLoopValue();
        }
        /* Lenovo-sw luyy1 add end for loop support 2015-08-31*/
       /* if(hasCustomContentToLeft()&& mWorkspace.mCurrentPage == 1){
            setFullScreen(true);
        }else{
            setFullScreen(false);
        }*/
    }

    @Override
    protected void onPause() {
        // Ensure that items added to Launcher are queued until Launcher returns
        InstallShortcutReceiver.enableInstallQueue();
        PackageInstallerCompat.getInstance(this).onPause();

        super.onPause();
        mPaused = true;
        mDragController.cancelDrag();
        mDragController.resetLastGestureUpTime();

        // We call onHide() aggressively. The custom content callbacks should be able to
        // debounce excess onHide calls.
        if (mWorkspace.getCustomContentCallbacks() != null) {
            mWorkspace.getCustomContentCallbacks().onHide();
        }
        /** Lenovo-SW luyy1 20150910 change for primary home screen START */
        if (!mWorkspace.isInOverviewMode() && (null != mDefaultHomeScreenPanel)) { // Lenovo-SW zhaoxin5 20150922 KOLEOSROW-2712 START
            mDefaultHomeScreenPanel.setVisibility(View.INVISIBLE);
        }
        /** Lenovo-SW luyy1 20150910 change for primary home screen END */

        // Umeng Analytics SDK
        MobclickAgent.onPause(this);
    }

    QSBScroller mQsbScroller = new QSBScroller() {
        int scrollY = 0;

        @Override
        public void setScrollY(int scroll) {
            scrollY = scroll;

            if (mWorkspace.isOnOrMovingToCustomContent()) {
                mSearchDropTargetBar.setTranslationY(-scrollY);
                getQsbBar().setTranslationY(-scrollY);
            }
        }
    };

    public void resetQSBScroll() {
        mSearchDropTargetBar.animate().translationY(0).start();
        if (isSearchBarEnabled()) {
            getQsbBar().animate().translationY(0).start();
        }
    }

    public interface CustomContentCallbacks {
        // Custom content is completely shown. {@code fromResume} indicates whether this was caused
        // by a onResume or by scrolling otherwise.
        public void onShow(boolean fromResume);

        // Custom content is completely hidden
        public void onHide();

        // Custom content scroll progress changed. From 0 (not showing) to 1 (fully showing).
        public void onScrollProgressChanged(float progress);

        // Indicates whether the user is allowed to scroll away from the custom content.
        boolean isScrollingAllowed();
    }

    protected boolean hasSettings() {
        return false;
    }

    public void updateDrawerType() {
        mDrawerType = AppDrawerListAdapter.DrawerType.getModeForValue(
                SettingsProvider.getInt(this,
                        SettingsProvider.SETTINGS_UI_DRAWER_TYPE,
                        R.integer.preferences_interface_drawer_type_default));
    }

    public AppDrawerListAdapter.DrawerType getDrawerType() {
        return mDrawerType;
    }

    public void onClickSortModeButton(View v) {
        final PopupMenu popupMenu = new PopupMenu(this, v);
        final Menu menu = popupMenu.getMenu();
        popupMenu.inflate(R.menu.apps_customize_sort_mode);
        switch (mAppsCustomizeContent.getSortMode()) {
            case Title:
                menu.findItem(R.id.sort_mode_title).setChecked(true);
                break;
            case LaunchCount:
                menu.findItem(R.id.sort_mode_launch_count).setChecked(true);
                break;
            case InstallTime:
                menu.findItem(R.id.sort_mode_install_time).setChecked(true);
                break;
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.sort_mode_title:
                        mAppsCustomizeContent.setSortMode(AppsCustomizePagedView.SortMode.Title);
                        break;
                    case R.id.sort_mode_install_time:
                        mAppsCustomizeContent.setSortMode(AppsCustomizePagedView.SortMode.InstallTime);
                        break;
                    case R.id.sort_mode_launch_count:
                        mAppsCustomizeContent.setSortMode(AppsCustomizePagedView.SortMode.LaunchCount);
                        break;
                }
                // Lenovo-sw:yuanyl2, Add setting function. Begin.
                // mOverviewSettingsPanel.notifyDataSetInvalidated();
                mSettingsController.notifyDataSetInvalidated();
                SettingsProvider.putInt(getBaseContext(), SettingsProvider.SETTINGS_UI_DRAWER_SORT_MODE,
                        mAppsCustomizeContent.getSortMode().getValue());
                return true;
            }
        });
        popupMenu.show();
    }

    public void onClickDynamicGridSizeButton() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        mDynamicGridSizeFragment = new DynamicGridSizeFragment();
        fragmentTransaction.replace(R.id.launcher, mDynamicGridSizeFragment,
                DynamicGridSizeFragment.DYNAMIC_GRID_SIZE_FRAGMENT);
        fragmentTransaction.commit();
    }

    public void setDynamicGridSize(DeviceProfile.GridSize size) {
        int gridSize = SettingsProvider.getIntCustomDefault(this,
                SettingsProvider.SETTINGS_UI_DYNAMIC_GRID_SIZE, 0);
        boolean customValuesChanged = false;
        if (gridSize == size.getValue() && size == DeviceProfile.GridSize.Custom) {
            int tempRows = SettingsProvider.getIntCustomDefault(this,
                    SettingsProvider.SETTINGS_UI_HOMESCREEN_ROWS, (int) mGrid.numRows);
            int tempColumns = SettingsProvider.getIntCustomDefault(this,
                    SettingsProvider.SETTINGS_UI_HOMESCREEN_COLUMNS, (int) mGrid.numColumns);
            if (tempColumns != (int) mGrid.numColumns || tempRows != (int) mGrid.numRows) {
                customValuesChanged = true;
            }
        }

        if (gridSize != size.getValue() || customValuesChanged) {
            SettingsProvider.putInt(this,
                    SettingsProvider.SETTINGS_UI_DYNAMIC_GRID_SIZE, size.getValue());

            setUpdateDynamicGrid();
        }

        // Lenovo-sw:yuanyl2, Add setting function. Begin.
        // mOverviewSettingsPanel.notifyDataSetInvalidated();
        mSettingsController.notifyDataSetInvalidated();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Configuration config = getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            fragmentTransaction
                    .setCustomAnimations(0, R.anim.exit_out_left);
        } else {
            fragmentTransaction
                    .setCustomAnimations(0, R.anim.exit_out_right);
        }
        fragmentTransaction
                .remove(mDynamicGridSizeFragment).commit();

        mDarkPanel.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(
                mDarkPanel, "alpha", 0.3f, 0.0f);
        anim.start();
        anim.addListener(mAnimatorListener);

    }
    //Lenovo-sw zhangyj19 20150806 add setting function
    public void onClickTransitionEffectButton(final boolean pageOrDrawer) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(TransitionEffectsFragment.PAGE_OR_DRAWER_SCROLL_SELECT,
                pageOrDrawer);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        TransitionEffectsFragment tef = new TransitionEffectsFragment();
        tef.setArguments(bundle);
        fragmentTransaction.setCustomAnimations(0, 0);
        fragmentTransaction.replace(R.id.launcher, tef,
                TransitionEffectsFragment.TRANSITION_EFFECTS_FRAGMENT);
        fragmentTransaction.commit();

        updateStatusBarColor(getResources().getColor(R.color.settings_bg_color));
    }
    //Lenovo-sw zhangyj19 20150806 add setting function
    public void setTransitionEffectBySetting(boolean pageOrDrawer, String newTransitionEffect) {
        String mSettingsProviderValue = pageOrDrawer ?
                SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_TRANSITION_EFFECT
                : SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_TRANSITION_EFFECT;
        PagedView pagedView = pageOrDrawer ? mAppsCustomizeContent : mWorkspace;

        SettingsProvider
                .get(getApplicationContext())
                .edit()
                .putString(mSettingsProviderValue,
                        newTransitionEffect).commit();
        TransitionEffect.setFromString(pagedView, newTransitionEffect);

        // Lenovo-sw:yuanyl2, Add setting function. Begin.
        // mOverviewSettingsPanel.notifyDataSetInvalidated();
        mSettingsController.notifyDataSetInvalidated();
    }
    public void setTransitionEffect(boolean pageOrDrawer, String newTransitionEffect) {
        String mSettingsProviderValue = pageOrDrawer ?
                SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_TRANSITION_EFFECT
                : SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_TRANSITION_EFFECT;
        PagedView pagedView = pageOrDrawer ? mAppsCustomizeContent : mWorkspace;

        SettingsProvider
                .get(getApplicationContext())
                .edit()
                .putString(mSettingsProviderValue,
                        newTransitionEffect).commit();
        TransitionEffect.setFromString(pagedView, newTransitionEffect);

        // Lenovo-sw:yuanyl2, Add setting function. Begin.
        // mOverviewSettingsPanel.notifyDataSetInvalidated();
        mSettingsController.notifyDataSetInvalidated();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Configuration config = getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            fragmentTransaction
                    .setCustomAnimations(0, R.anim.exit_out_left);
        } else {
            fragmentTransaction
                    .setCustomAnimations(0, R.anim.exit_out_right);
        }
        Fragment f = getFragmentManager().findFragmentByTag(
                TransitionEffectsFragment.TRANSITION_EFFECTS_FRAGMENT);
        fragmentTransaction
                .remove(f).commit();

        mDarkPanel.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(
                mDarkPanel, "alpha", 0.3f, 0.0f);
        anim.start();
        anim.addListener(mAnimatorListener);
    }

    public void onClickTransitionEffectOverflowMenuButton(View v, final boolean drawer) {
        final PopupMenu popupMenu = new PopupMenu(this, v);

        final Menu menu = popupMenu.getMenu();
        popupMenu.inflate(R.menu.scrolling_settings);
        MenuItem pageOutlines = menu.findItem(R.id.scrolling_page_outlines);
        MenuItem fadeAdjacent = menu.findItem(R.id.scrolling_fade_adjacent);

        pageOutlines.setVisible(!drawer);
        pageOutlines.setChecked(SettingsProvider.getBoolean(this,
                SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_PAGE_OUTLINES,
                R.bool.preferences_interface_homescreen_scrolling_page_outlines_default
        ));

        fadeAdjacent.setChecked(SettingsProvider.getBoolean(this,
                !drawer ?
                        SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_FADE_ADJACENT :
                        SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_FADE_ADJACENT,
                !drawer ?
                        R.bool.preferences_interface_homescreen_scrolling_fade_adjacent_default :
                        R.bool.preferences_interface_drawer_scrolling_fade_adjacent_default
        ));

        final PagedView pagedView = !drawer ? mWorkspace : mAppsCustomizeContent;

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.scrolling_page_outlines:
                        SettingsProvider.get(Launcher.this).edit()
                                .putBoolean(SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_PAGE_OUTLINES, !item.isChecked()).commit();
                        mWorkspace.setShowOutlines(!item.isChecked());
                        break;
                    case R.id.scrolling_fade_adjacent:
                        SettingsProvider.get(Launcher.this).edit()
                                .putBoolean(!drawer ?
                                        SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_FADE_ADJACENT :
                                        SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_FADE_ADJACENT, !item.isChecked()).commit();
                        pagedView.setFadeInAdjacentScreens(!item.isChecked());
                        break;
                    default:
                        return false;
                }

                return true;
            }
        });

        popupMenu.show();
    }

    protected void startSettings() {
        Intent settings;
        settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        startActivity(settings);
        if (mWorkspace.isInOverviewMode()) {
            mWorkspace.exitOverviewMode(false);
        }
    }

    public interface QSBScroller {
        public void setScrollY(int scrollY);
    }

    public QSBScroller addToCustomContentPage(View customContent,
                                              CustomContentCallbacks callbacks, String description) {
        mWorkspace.addToCustomContentPage(customContent, callbacks, description);
        return mQsbScroller;
    }

    // The custom content needs to offset its content to account for the QSB
    public int getTopOffsetForCustomContent() {
        return mWorkspace.getPaddingTop();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        LauncherLog.i("xixia", "onRetainNonConfigurationInstance");
        // Flag the loader to stop early before switching
        if (mModel.isCurrentCallbacks(this)) {
            mModel.stopLoader();
        }
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.surrender();
        }
        return Boolean.TRUE;
    }

    // We can't hide the IME if it was forced open.  So don't bother
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mHasFocus = hasFocus;
    }

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final int uniChar = event.getUnicodeChar();
        final boolean handled = super.onKeyDown(keyCode, event);
        final boolean isKeyNotWhitespace = uniChar > 0 && !Character.isWhitespace(uniChar);
        if (!handled && acceptFilter() && isKeyNotWhitespace) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        }

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        }

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Given the integer (ordinal) value of a State enum instance, convert it to a variable of type
     * State
     */
    private static State intToState(int stateOrdinal) {
        State state = State.WORKSPACE;
        final State[] stateValues = State.values();
        for (int i = 0; i < stateValues.length; i++) {
            if (stateValues[i].ordinal() == stateOrdinal) {
                state = stateValues[i];
                break;
            }
        }
        return state;
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    @SuppressWarnings("unchecked")
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        State state = intToState(savedState.getInt(RUNTIME_STATE, State.WORKSPACE.ordinal()));
        if (state == State.APPS_CUSTOMIZE) {
            mOnResumeState = State.APPS_CUSTOMIZE;
        }

        int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN,
                PagedView.INVALID_RESTORE_PAGE);
        if (currentScreen != PagedView.INVALID_RESTORE_PAGE) {
            mWorkspace.setRestorePage(currentScreen);
        }

        final long pendingAddContainer = savedState.getLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
        final long pendingAddScreen = savedState.getLong(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);

        if (pendingAddContainer != ItemInfo.NO_ID && pendingAddScreen > -1) {
            mPendingAddInfo.container = pendingAddContainer;
            mPendingAddInfo.screenId = pendingAddScreen;
            mPendingAddInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            mPendingAddInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            mPendingAddInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            mPendingAddInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            mPendingAddWidgetInfo = savedState.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
            mPendingAddWidgetId = savedState.getInt(RUNTIME_STATE_PENDING_ADD_WIDGET_ID);
            setWaitingForResult(true);
            mRestoring = true;
        }

        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, sFolders, id);
            mRestoring = true;
        }

        // Restore the AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String curTab = savedState.getString("apps_customize_currentTab");
            if (curTab != null) {
                mAppsCustomizeTabHost.setContentTypeImmediate(
                        mAppsCustomizeTabHost.getContentTypeForTabTag(curTab));
                mAppsCustomizeContent.loadAssociatedPages(
                        mAppsCustomizeContent.getCurrentPage());
            }

            int currentIndex = savedState.getInt("apps_customize_currentIndex");
            mAppsCustomizeContent.restorePageForIndex(currentIndex);
        }
        mItemIdToViewId = (HashMap<Integer, Integer>)
                savedState.getSerializable(RUNTIME_STATE_VIEW_IDS);
    }

    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        final DragController dragController = mDragController;

        mLauncherView = findViewById(R.id.launcher);
        mFocusHandler = (FocusIndicatorView) findViewById(R.id.focus_indicator);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mWorkspace.setPageSwitchListener(this);

        //add by zhanggx1.s Lenovo-SW zhaoxin5 add for auto-reorder support START
        mWorkspace.setReorderingChangedListener(mReorderChangedListener);
        //add by zhanggx1.e Lenovo-SW zhaoxin5 add for auto-reorder support END
        
        mPageIndicators = mDragLayer.findViewById(R.id.page_indicator);

        mLauncherView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mWorkspaceBackgroundDrawable = getResources().getDrawable(R.drawable.workspace_bg);

        // Setup the drag layer
        mDragLayer.setup(this, dragController);

        // Setup the hotseat
        mHotseat = (Hotseat) findViewById(R.id.hotseat);
        if (mHotseat != null) {
            mHotseat.setup(this);
            mHotseat.setOnLongClickListener(this);
        }

		/* Lenovo-SW zhaoxin5 20150610 add for widgetlist support START */

        // Lenovo-sw:yuanyl2, Add edit mode function. Begin.

        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
        mOverviewPanelAndroid = (ViewGroup) findViewById(R.id.overview_panel);
        mOverviewSettingsPanel = new OverviewSettingsPanel(
                this, mOverviewPanelAndroid);
        mOverviewSettingsPanel.initializeAdapter();
        mOverviewSettingsPanel.initializeViews();
        mDarkPanel = ((SlidingUpPanelLayout) mOverviewPanelAndroid).findViewById(R.id.dark_panel);
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
        /** Lenovo-SW luyy1 20150910 add for primary home screen START */
        mDefaultHomeScreenPanel = findViewById(R.id.default_home_screen_panel_overview_mode);
        /** Lenovo-SW luyy1 20150910 add for primary home screen END */

        mOverviewPanel = (ViewGroup) findViewById(R.id.editing_entry_view);
        if (null != mOverviewPanel) {
            mMenuController = new MenuController(this, mOverviewPanel);
            mMenuController.initializeViews();
        }
        String orientation = "";
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	orientation = "LANDSCAPE";
        } else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        	orientation = "PORTRAIT";
        }
        LauncherLog.i(TAG, "setupViews, orientation : " + orientation);
        mWidgetListPanel = (WidgetListViewSwitcher) findViewById(R.id.widget_list);
        mWidgetListPanel.setup(this, dragController);
        mWallpaperListPanel = (WallpaperListViewSwitcher) findViewById(R.id.wallpaper_list);
        mWallpaperListPanel.setup(this);
        mSettingsPanel = findViewById(R.id.settings_panel);
        if (null != mSettingsPanel) {
            mSettingsController = new SettingsController(this, mSettingsPanel);
            mSettingsController.initializeAdapter();
            setupSeetingsPanelAnimation();
        }
        mDarkPanel = mSettingsPanel.findViewById(R.id.dark_panel);

        mQuickShareGridView = (QuickShareGridView) this.findViewById(R.id.quick_share_grid_view);
        mQuickShareGridView.setupViews(this);
        mQuickShareZone = (LinearLayout) this.findViewById(R.id.quick_share_zone);
        // Lenovo-sw:yuanyl2, Add edit mode function. End.

        // Setup the workspace
        mWorkspace.setHapticFeedbackEnabled(false);
        mWorkspace.setOnLongClickListener(this);
        mWorkspace.setup(dragController);
        dragController.addDragListener(mWorkspace);
        // Lenovo-sw:yuanyl2, Add edit mode function.
        dragController.addDragEndListener(mWorkspace);

        // Get the search/delete bar
        mSearchDropTargetBar = (SearchDropTargetBar)
                mDragLayer.findViewById(R.id.search_drop_target_bar);

        // Setup AppsCustomize
        mAppsCustomizeTabHost = (AppsCustomizeTabHost) findViewById(R.id.apps_customize_pane);
        mAppsCustomizeContent = (AppsCustomizePagedView)
                mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        mAppsCustomizeContent.setup(this, dragController);

        // Setup AppDrawer
        setupAppDrawer();

        // Setup the drag controller (drop targets have to be added in reverse order in priority)
        dragController.setDragScoller(mWorkspace);
        dragController.setScrollView(mDragLayer);
        dragController.setMoveTarget(mWorkspace);
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(this, dragController);
        }

        // Set tool tip bar to show infomation.
        mMenuController.setToolTipBar(mSearchDropTargetBar);

        mDockView = (XDockView) findViewById(R.id.multi_select_panel);
        if (null != mDockView) {
            mDockView.setup(this, dragController);
            mDockView.setOnLongClickListener(mDockView);
            dragController.addDragListener(mDockView);
            dragController.addDropTarget(mDockView);

            // Setup dock select tool tip
            mSearchDropTargetBar.setupMultiSelectToolTip(mDockView);
        }

        if (getResources().getBoolean(R.bool.debug_memory_enabled)) {
            Log.v(TAG, "adding WeightWatcher");
            mWeightWatcher = new WeightWatcher(this);
            mWeightWatcher.setAlpha(0.5f);
            ((FrameLayout) mLauncherView).addView(mWeightWatcher,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.BOTTOM)
            );

            boolean show = shouldShowWeightWatcher();
            mWeightWatcher.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void setupAppDrawer() {
        if (mAppDrawer == null) {
            FrameLayout view = (FrameLayout) findViewById(R.id.app_drawer_container);
            mAppDrawer = (RecyclerView) view.findViewById(R.id.app_drawer_recyclerview);
            mAppDrawer.setLayoutManager(new LinearLayoutManager(this));
            if (mAppDrawerAdapter == null) {
                initializeAdapter();
            }
            mAppDrawer.setHasFixedSize(true);
            mAppDrawer.setAdapter(mAppDrawerAdapter);
            mAppDrawer.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    mAppDrawerAdapter.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    mAppDrawerAdapter.onScrolled(recyclerView, dx, dy);
                }
            });
            initializeScrubber();
        }
    }

    /**
     * Sets the all apps button. This method is called from {@link Hotseat}.
     */
    public void setAllAppsButton(View allAppsButton) {
        mAllAppsButton = allAppsButton;
    }

    public View getAllAppsButton() {
        return mAllAppsButton;
    }

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent      The group the shortcut belongs to.
     * @param info        The data structure describing the shortcut.
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, mIconCache, true);
        favorite.setTextVisibility(!mHideIconLabels);
        favorite.setOnClickListener(this);
        favorite.setOnFocusChangeListener(mFocusHandler);
        return favorite;
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data     The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, long container, long screenId, int cellX,
                                     int cellY) {
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        CellLayout layout = getCellLayout(container, screenId);

        boolean foundCellSpan = false;

        ShortcutInfo info = mModel.infoFromShortcutIntent(this, data, null);
        if (info == null) {
            return;
        }
        final View view = createShortcut(info);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
            foundCellSpan = true;

            // If appropriate, either create a folder or add to an existing folder
            if (mWorkspace.createUserFolderIfNecessary(view, container, layout, cellXY, 0,
                    true, null, null)) {
                return;
            }
            DragObject dragObject = new DragObject();
            dragObject.dragInfo = info;
            if (mWorkspace.addToExistingFolderIfNecessary(view, layout, cellXY, 0, dragObject,
                    true)) {
                return;
            }
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY);
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
        }

        if (!foundCellSpan) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        LauncherModel.addItemToDatabase(this, info, container, screenId, cellXY[0], cellXY[1], false);

        if (!mRestoring) {
            mWorkspace.addInScreen(view, container, screenId, cellXY[0], cellXY[1], 1, 1,
                    isWorkspaceLocked());
        }
    }

    static int[] getSpanForWidget(Context context, ComponentName component, int minWidth,
                                  int minHeight) {
    	/** 
    	 * 原来这里是认为置成0的,
    	 * 因为在平板上,LenovoWeather返回的值是0
    	 * 但是其他的都是有值的,导致天气和视频两个Widget不能对齐
    	 * 
    	 *  Lenovo-SW zhaoxin5 20150821 XTHREEROW-1056 START 
    	 * */
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        /** Lenovo-SW zhaoxin5 20150821 XTHREEROW-1056 END */
        // We want to account for the extra amount of padding that we are adding to the widget
        // to ensure that it gets the full amount of space that it has requested
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        int[] span = CellLayout.rectToCell(requiredWidth, requiredHeight, null);
        if (false) {
	        LauncherLog.i("xixia", "getSpanForWidget " +
	                component.getPackageName() +
	                ", minWidth:" + requiredWidth +
	                ", minHeight:" + requiredHeight +
	                ", spanX:" + span[0] +
	                ", spanY:" + span[1]);
        }
        return span;
    }

    static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }

    static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minResizeWidth,
                info.minResizeHeight);
    }

    /**
     * Add a widget to the workspace.
     *
     * @param appWidgetId The app widget id
     * @param cellInfo    The position on screen where to create the widget.
     */
    private void completeAddAppWidget(final int appWidgetId, long container, long screenId,
                                      AppWidgetHostView hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = getCellLayout(container, screenId);
        
        /** Lenovo-SW zhaoxin5 20150924 KOLEOSROW-2746 START */
        if (null == layout) {
        	LauncherLog.i(TAG, "CellLayout is null in completeAddAppWidget");
        	showOutOfSpaceMessage(false);
        	return;
        }
        /** Lenovo-SW zhaoxin5 20150924 KOLEOSROW-2746 END */
        
        int[] minSpanXY = getMinSpanForWidget(this, appWidgetInfo);
        int[] spanXY = getSpanForWidget(this, appWidgetInfo);

        // Try finding open space on Launcher screen
        // We have saved the position to which the widget was dragged-- this really only matters
        // if we are placing widgets on a "spring-loaded" screen
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        int[] finalSpan = new int[2];
        boolean foundCellSpan = false;
        if (mPendingAddInfo.cellX >= 0 && mPendingAddInfo.cellY >= 0) {
            cellXY[0] = mPendingAddInfo.cellX;
            cellXY[1] = mPendingAddInfo.cellY;
            spanXY[0] = mPendingAddInfo.spanX;
            spanXY[1] = mPendingAddInfo.spanY;
            foundCellSpan = true;
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(
                    touchXY[0], touchXY[1], minSpanXY[0], minSpanXY[1], spanXY[0],
                    spanXY[1], cellXY, finalSpan);
            spanXY[0] = finalSpan[0];
            spanXY[1] = finalSpan[1];
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, minSpanXY[0], minSpanXY[1]);
        }

        if (!foundCellSpan) {
            if (appWidgetId != -1) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new AsyncTask<Void, Void, Void>() {
                    public Void doInBackground(Void... args) {
                        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
            }
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }
        if (appWidgetInfo !=null && appWidgetInfo.provider.getClassName().equals(DeleteDropTarget.SEARCH_BOX_CLASS_NAME)) {
            if (mMySearchWidgetScreenId != -1) {
                mWorkspace.setCurrentPage(mMySearchWidgetScreenId);
                return;
            } else {
                mMySearchWidgetScreenId = getCurrentWorkspaceScreen();
            }
        }
        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId,
                appWidgetInfo.provider);
        launcherInfo.spanX = spanXY[0];
        launcherInfo.spanY = spanXY[1];
        launcherInfo.minSpanX = mPendingAddInfo.minSpanX;
        launcherInfo.minSpanY = mPendingAddInfo.minSpanY;
        launcherInfo.user = mAppWidgetManager.getUser(appWidgetInfo);

        LauncherModel.addItemToDatabase(this, launcherInfo,
                container, screenId, cellXY[0], cellXY[1], false);

        if (!mRestoring) {
            if (appWidgetInfo.provider.getClassName().toString().contains(LauncherModel.KLAUNCHER_WIDGET_CLOCK_CLASS) ) {
                LayoutInflater flater = LayoutInflater.from(this);
                ClockWidgetView view = new ClockWidgetView(this);
                view.setTag(launcherInfo);
//                RelativeLayout clockLayout = (RelativeLayout) view.findViewById(R.id.clock_layout);
//                clockLayout.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        PackageManager packageManager = getPackageManager();
//                        if (packageManager != null) {
//                            Intent AlarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(
//                                    Intent.CATEGORY_LAUNCHER).setComponent(
//                                    new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClock"));
//                            ResolveInfo resolved = packageManager.resolveActivity(AlarmClockIntent,
//                                    PackageManager.MATCH_DEFAULT_ONLY);
//                            if (resolved != null) {
//                                startActivity(AlarmClockIntent);
//                            }
//                        }
//                    }
//                });
//
//                RelativeLayout weatherLayout = (RelativeLayout) view.findViewById(R.id.weather_layout);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PackageManager packageManager = getPackageManager();
                        if (packageManager != null) {
                            Intent intent = null;
                            intent = packageManager.getLaunchIntentForPackage("com.moji.mjweather");
                            if (intent != null && intent.getComponent() != null) {
                                startActivity(intent);
                            }
                        }
                    }
                });

                mWorkspace.addInScreen(view, container, screenId, cellXY[0], cellXY[1],
                        launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
//            addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

//                workspace.requestLayout();
            }else {
                if (hostView == null) {
                    // Perform actual inflation because we're live
                    launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
                    launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
                } else {
                    // The AppWidgetHostView has already been inflated and instantiated
                    launcherInfo.hostView = hostView;
                }

                launcherInfo.hostView.setTag(launcherInfo);
                launcherInfo.hostView.setVisibility(View.VISIBLE);
                launcherInfo.notifyWidgetSizeChanged(this);

                mWorkspace.addInScreen(launcherInfo.hostView, container, screenId, cellXY[0], cellXY[1],
                        launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());

                addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
            }
        }
        resetAddInfo();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUserPresent = false;
                mDragLayer.clearAllResizeFrames();
                updateRunning();

                // Reset AllApps to its initial state only if we are not in the middle of
                // processing a multi-step drop
/*              if (mAppsCustomizeTabHost != null && mPendingAddInfo.container == ItemInfo.NO_ID) {
                    showWorkspace(false);
                }*/
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning();
            } else if (ENABLE_DEBUG_INTENTS && DebugIntents.DELETE_DATABASE.equals(action)) {
                mModel.resetLoadedState(false, true);
                mModel.startLoader(false, PagedView.INVALID_RESTORE_PAGE,
                        LauncherModel.LOADER_FLAG_CLEAR_WORKSPACE);
            } else if (ENABLE_DEBUG_INTENTS && DebugIntents.MIGRATE_DATABASE.equals(action)) {
                mModel.resetLoadedState(false, true);
                mModel.startLoader(false, PagedView.INVALID_RESTORE_PAGE,
                        LauncherModel.LOADER_FLAG_CLEAR_WORKSPACE
                                | LauncherModel.LOADER_FLAG_MIGRATE_SHORTCUTS);
            } else if (LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED.equals(action)
                    || LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED.equals(action)) {
                getModel().forceReload("ACTION_MANAGED_PROFILE_ADDED or ACTION_MANAGED_PROFILE_REMOVED");
            }
//            else if (ThemeController.ACTION_LAUNCHER_THEME_FORCE_RELOAD_LAUNCHER.equals(action)) {
//                Log.d(TAG,"ACTION_LAUNCHER_THEME_FORCE_RELOAD_LAUNCHER");
//                if (!mRestoring) {//不需要恢复 第一次加载
//                    if (DISABLE_SYNCHRONOUS_BINDING_CURRENT_PAGE) {
//                        LauncherLog.i("xixia", "onCreate befor loader task 1");
//                        // If the user leaves launcher, then we should just load items asynchronously when
//                        // they return.
//                /* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
//                        //Launcher 加载lbk数据入口
//                        mModel.startLoader(true, PagedView.INVALID_RESTORE_PAGE, ModeSwitchHelper.getLauncherModelLoaderTaskLoaderFlag(Launcher.this));
//                /* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
//                    } else {//从 lbk文件恢复
//                        LauncherLog.i("xixia", "onCreate befor loader task 2");
//                        // We only load the page synchronously if the user rotates (or triggers a
//                        // configuration change) while launcher is in the foreground
//            	/* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
//                        mModel.startLoader(true, mWorkspace.getRestorePage(), ModeSwitchHelper.getLauncherModelLoaderTaskLoaderFlag(Launcher.this));
//            	/* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
//                    }
//                } else {
//                    setWorkspaceLoading(true);
//            /* Lenovo-SW zhaoxin5 20150529 add for 2 Layer support */
//                    int flags = (mOnResumeLoaderTaskFlags == Integer.MIN_VALUE ? ModeSwitchHelper.getLauncherModelLoaderTaskLoaderFlag(Launcher.this)
//                            : mOnResumeLoaderTaskFlags);
//                    mModel.startLoader(true, PagedView.INVALID_RESTORE_PAGE, flags);
//            /* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
//                    mRestoring = false;
//                    /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success START */
//                    mOnResumeLoaderTaskFlags = Integer.MIN_VALUE;
//                }
//            }
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Listen for broadcasts related to user-presence
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // For handling managed profiles
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED);
        if (ENABLE_DEBUG_INTENTS) {
            filter.addAction(DebugIntents.DELETE_DATABASE);
            filter.addAction(DebugIntents.MIGRATE_DATABASE);
        }
//        filter.addAction(ThemeController.ACTION_LAUNCHER_THEME_FORCE_RELOAD_LAUNCHER);
        registerReceiver(mReceiver, filter);
        //Lenovo-sw zhangyj19 delete 2015/09/10 KOLEOSROW-1309 delete FirstFrameAnimatorHelper function
        //FirstFrameAnimatorHelper.initializeDrawListener(getWindow().getDecorView());
        setupTransparentSystemBarsForLmp();
        mAttached = true;
        mVisible = true;
    }

    /**
     * Sets up transparent navigation and status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(19)
    private void setupTransparentSystemBarsForLmp() {
        // TODO(sansid): use the APIs directly when compiling against L sdk.
        // Currently we use reflection to access the flags and the API to set the transparency
        // on the System bars.
        if (Utilities.isLmpOrAbove()) {
            try {
                getWindow().getAttributes().systemUiVisibility |=
                        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                Field drawsSysBackgroundsField = WindowManager.LayoutParams.class.getField(
                        "FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS");
                getWindow().addFlags(drawsSysBackgroundsField.getInt(null));

                Method setStatusBarColorMethod =
                        Window.class.getDeclaredMethod("setStatusBarColor", int.class);
                Method setNavigationBarColorMethod =
                        Window.class.getDeclaredMethod("setNavigationBarColor", int.class);
                setStatusBarColorMethod.invoke(getWindow(), Color.TRANSPARENT);
                setNavigationBarColorMethod.invoke(getWindow(), Color.TRANSPARENT);
            } catch (NoSuchFieldException e) {
                Log.w(TAG, "NoSuchFieldException while setting up transparent bars");
            } catch (NoSuchMethodException ex) {
                Log.w(TAG, "NoSuchMethodException while setting up transparent bars");
            } catch (IllegalAccessException e) {
                Log.w(TAG, "IllegalAccessException while setting up transparent bars");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "IllegalArgumentException while setting up transparent bars");
            } catch (InvocationTargetException e) {
                Log.w(TAG, "InvocationTargetException while setting up transparent bars");
            } finally {
            }
        }
    }

    @TargetApi(19)
    public void setupTransparentSystemBarsForLmp(Window window) {
        // TODO(sansid): use the APIs directly when compiling against L sdk.
        // Currently we use reflection to access the flags and the API to set the transparency
        // on the System bars.
        if (Utilities.isLmpOrAbove()) {
            try {
                window.getAttributes().systemUiVisibility |=
                        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                Field drawsSysBackgroundsField = WindowManager.LayoutParams.class.getField(
                        "FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS");
                window.addFlags(drawsSysBackgroundsField.getInt(null));

                Method setStatusBarColorMethod =
                        Window.class.getDeclaredMethod("setStatusBarColor", int.class);
                Method setNavigationBarColorMethod =
                        Window.class.getDeclaredMethod("setNavigationBarColor", int.class);
                setStatusBarColorMethod.invoke(window, Color.TRANSPARENT);
                setNavigationBarColorMethod.invoke(window, Color.TRANSPARENT);
            } catch (NoSuchFieldException e) {
                Log.w(TAG, "NoSuchFieldException while setting up transparent bars");
            } catch (NoSuchMethodException ex) {
                Log.w(TAG, "NoSuchMethodException while setting up transparent bars");
            } catch (IllegalAccessException e) {
                Log.w(TAG, "IllegalAccessException while setting up transparent bars");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "IllegalArgumentException while setting up transparent bars");
            } catch (InvocationTargetException e) {
                Log.w(TAG, "InvocationTargetException while setting up transparent bars");
            } finally {}
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;

        if (mAttached) {
            unregisterReceiver(mReceiver);
            mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        mVisible = visibility == View.VISIBLE;
        updateRunning();
        // The following code used to be in onResume, but it turns out onResume is called when
        // you're in All Apps and click home to go to the workspace. onWindowVisibilityChanged
        // is a more appropriate event to handle
        if (mVisible) {
            mAppsCustomizeTabHost.onWindowVisible();
            if (!mWorkspaceLoading) {
                final ViewTreeObserver observer = mWorkspace.getViewTreeObserver();
                // We want to let Launcher draw itself at least once before we force it to build
                // layers on all the workspace pages, so that transitioning to Launcher from other
                // apps is nice and speedy.
                observer.addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
                    private boolean mStarted = false;

                    public void onDraw() {
                        if (mStarted) return;
                        mStarted = true;
                        // We delay the layer building a bit in order to give
                        // other message processing a time to run.  In particular
                        // this avoids a delay in hiding the IME if it was
                        // currently shown, because doing that may involve
                        // some communication back with the app.
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);
                        final ViewTreeObserver.OnDrawListener listener = this;
                        mWorkspace.post(new Runnable() {
                            public void run() {
                                if (mWorkspace != null &&
                                        mWorkspace.getViewTreeObserver() != null) {
                                    mWorkspace.getViewTreeObserver().
                                            removeOnDrawListener(listener);
                                }
                            }
                        });
                        return;
                    }
                });
            }
            clearTypedText();
        }
    }

    private void sendAdvanceMessage(long delay) {
        mHandler.removeMessages(ADVANCE_MSG);
        Message msg = mHandler.obtainMessage(ADVANCE_MSG);
        mHandler.sendMessageDelayed(msg, delay);
        mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void updateRunning() {
        boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
        if (autoAdvanceRunning != mAutoAdvanceRunning) {
            mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
                sendAdvanceMessage(delay);
            } else {
                if (!mWidgetsToAdvance.isEmpty()) {
                    mAutoAdvanceTimeLeft = Math.max(0, mAdvanceInterval -
                            (System.currentTimeMillis() - mAutoAdvanceSentTime));
                }
                mHandler.removeMessages(ADVANCE_MSG);
                mHandler.removeMessages(0); // Remove messages sent using postDelayed()
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ADVANCE_MSG) {
                int i = 0;
                for (View key : mWidgetsToAdvance.keySet()) {
                    final View v = key.findViewById(mWidgetsToAdvance.get(key).autoAdvanceViewId);
                    final int delay = mAdvanceStagger * i;
                    if (v instanceof Advanceable) {
                        postDelayed(new Runnable() {
                            public void run() {
                                ((Advanceable) v).advance();
                            }
                        }, delay);
                    }
                    i++;
                }
                sendAdvanceMessage(mAdvanceInterval);
            }
        }
    };

    void addWidgetToAutoAdvanceIfNeeded(View hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1) return;
        View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
        if (v instanceof Advanceable) {
            mWidgetsToAdvance.put(hostView, appWidgetInfo);
            ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    void removeWidgetToAutoAdvance(View hostView) {
        if (mWidgetsToAdvance.containsKey(hostView)) {
            mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        removeWidgetToAutoAdvance(launcherInfo.hostView);
        launcherInfo.hostView = null;
    }

    void showOutOfSpaceMessage(boolean isHotseatLayout) {
        int strId = (isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space);
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public Workspace getWorkspace() {
        return mWorkspace;
    }

    public Hotseat getHotseat() {
        return mHotseat;
    }

    public ViewGroup getOverviewPanel() {
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
    	ViewGroup vg = LauncherAppState.getInstance().isCurrentAndroidMode() ? 
    			mOverviewPanelAndroid : mOverviewPanel;
        return vg;
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
    }

    // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
    public XDockView getDockView() {
        return mDockView;
    }

    public WidgetListViewSwitcher getWidgetListPanel() {
        return mWidgetListPanel;
    }

    public MenuController getEditEntrycontroller() {
        return mMenuController;
    }

    public View getSettingPanel() {
        return mSettingsPanel;
    }

    public LinearLayout getQuickShareZone() {
        return mQuickShareZone;
    }
    // Lenovo-sw:yuanyl2, Add edit mode function. End.

    public SearchDropTargetBar getSearchBar() {
        return mSearchDropTargetBar;
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    protected SharedPreferences getSharedPrefs() {
        return mSharedPrefs;
    }

    public void closeSystemDialogs() {
        getWindow().closeAllPanels();

        // Whatever we were doing is hereby canceled.
        setWaitingForResult(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
    	LauncherLog.i(TAG, "onNewIntent");
        long startTime = 0;
        if (DEBUG_RESUME_TIME) {
            startTime = System.currentTimeMillis();
        }
        super.onNewIntent(intent);

        if (isBootProgressDialogShowing()) {
            return;
        }

        updateGridIfNeeded();

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();

            if (mTransitionEffectDialog != null) {
                mTransitionEffectDialog.cancel();
            }

            final boolean alreadyOnHome = mHasFocus && ((intent.getFlags() &
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                    != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            if (mWorkspace == null) {
                // Can be cases where mWorkspace is null, this prevents a NPE
                return;
            }

            Folder openFolder = mWorkspace.getOpenFolder();
            // In all these cases, only animate if we're already on home
            mWorkspace.exitWidgetResizeMode();
            if (alreadyOnHome && mState == State.WORKSPACE && !mWorkspace.isTouchActive() &&
                    openFolder == null && shouldMoveToDefaultScreenOnHomeIntent()
                    && !mWorkspace.isInOverviewMode()) {
                mWorkspace.moveToDefaultScreen(true);
            }

            closeFolder();
            exitSpringLoadedDragMode();

            // Lenovo-sw:yuanyl2, Add setting function. Begin.
            if (mIsSettingPanelShowing) {
                hideSettingsPanel();
            }
            updateStatusBarColor(Color.TRANSPARENT);
            mMenuController.onHomePressed();
            mWorkspace.exitOverviewMode(true);
            // Lenovo-sw:yuanyl2, Add setting function. End.

            // If we are already on home, then just animate back to the workspace,
            // otherwise, just wait until onResume to set the state back to Workspace
            if (alreadyOnHome) {
                showWorkspace(true);
            } else {
                mOnResumeState = State.WORKSPACE;
            }

            final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            onHomeIntent();
        }

        if (DEBUG_RESUME_TIME) {
            Log.d(TAG, "Time spent in onNewIntent: " + (System.currentTimeMillis() - startTime));
        }
    }

    /**
     * Override point for subclasses to prevent movement to the default screen when the home
     * button is pressed. Used (for example) in GEL, to prevent movement during a search.
     */
    protected boolean shouldMoveToDefaultScreenOnHomeIntent() {
        return true;
    }

    /**
     * Override point for subclasses to provide custom behaviour for when a home intent is fired.
     */
    protected void onHomeIntent() {
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        for (int page : mSynchronouslyBoundPages) {
            mWorkspace.restoreInstanceStateForChild(page);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"onSaveInstanceState");
        if (mWorkspace.getChildCount() > 0) {
            outState.putInt(RUNTIME_STATE_CURRENT_SCREEN,
                    mWorkspace.getCurrentPageOffsetFromCustomContent());
        }
        super.onSaveInstanceState(outState);

        outState.putInt(RUNTIME_STATE, mState.ordinal());
        // We close any open folder since it will not be re-opened, and we need to make sure
        // this state is reflected.
//        closeFolder(false);

        if (mPendingAddInfo.container != ItemInfo.NO_ID && mPendingAddInfo.screenId > -1 &&
                mWaitingForResult) {
            outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, mPendingAddInfo.container);
            outState.putLong(RUNTIME_STATE_PENDING_ADD_SCREEN, mPendingAddInfo.screenId);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, mPendingAddInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, mPendingAddInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, mPendingAddInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, mPendingAddInfo.spanY);
            outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO, mPendingAddWidgetInfo);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_WIDGET_ID, mPendingAddWidgetId);
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }

        // Save the current AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            AppsCustomizePagedView.ContentType type = mAppsCustomizeContent.getContentType();
            String currentTabTag = mAppsCustomizeTabHost.getTabTagForContentType(type);
            if (currentTabTag != null) {
                outState.putString("apps_customize_currentTab", currentTabTag);
            }
            int currentIndex = mAppsCustomizeContent.getSaveInstanceStateIndex();
            outState.putInt("apps_customize_currentIndex", currentIndex);
        }
        outState.putSerializable(RUNTIME_STATE_VIEW_IDS, mItemIdToViewId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LauncherLog.i("xixia", "onDestroy");
        // Remove all pending runnables
        mHandler.removeMessages(ADVANCE_MSG);
        mHandler.removeMessages(0);
        mWorkspace.removeCallbacks(mBuildLayersRunnable);

        // Stop callbacks from LauncherModel
        LauncherAppState app = (LauncherAppState.getInstance());

        // It's possible to receive onDestroy after a new Launcher activity has
        // been created. In this case, don't interfere with the new Launcher.
        if (mModel.isCurrentCallbacks(this)) {
            mModel.stopLoader();
            app.setLauncher(null);
        }

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        mAppWidgetHost = null;

        mWidgetsToAdvance.clear();

        TextKeyListener.getInstance().release();

        // Disconnect any of the callbacks and drawables associated with ItemInfos on the workspace
        // to prevent leaking Launcher activities on orientation change.
        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }

        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mCloseSystemDialogsReceiver);

        mDragLayer.clearAllResizeFrames();
        ((ViewGroup) mWorkspace.getParent()).removeAllViews();
        mWorkspace.removeAllWorkspaceScreens();
        mWorkspace = null;
        mDragController = null;

        PackageInstallerCompat.getInstance(this).onStop();
        LauncherAnimUtils.onDestroyActivity();

        unregisterReceiver(protectedAppsChangedReceiver);
        //取消mockAppReceiver广播注册
        if(mockAppReceiver != null){
            unregisterReceiver(mockAppReceiver);
        }
        Csc.destory(Launcher.this);
    }

    public DragController getDragController() {
        return mDragController;
    }

    public void validateLockForHiddenFolders(Bundle bundle, FolderIcon info) {
        // Validate Lock Pattern
        Intent lockPatternActivity = new Intent();
        lockPatternActivity.setClassName(
                "com.android.settings",
                "com.android.settings.applications.LockPatternActivity");
        startActivityForResult(lockPatternActivity, REQUEST_LOCK_PATTERN);
        mHiddenFolderAuth = false;

        mHiddenFolderIcon = info;
        mHiddenFolderFragment = new HiddenFolderFragment();
        mHiddenFolderFragment.setArguments(bundle);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) {
            setWaitingForResult(true);
        }
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for {@link #startSearch} to true.
     */
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
                            Bundle appSearchData, boolean globalSearch) {

        showWorkspace(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString("source", "launcher-search");
        }
        Rect sourceBounds = new Rect();
        if (mSearchDropTargetBar != null) {
            sourceBounds = mSearchDropTargetBar.getSearchBarBounds();
        }

        boolean clearTextImmediately = startSearch(initialQuery, selectInitialQuery,
                appSearchData, sourceBounds);
        if (clearTextImmediately) {
            clearTypedText();
        }
    }

    /**
     * Start a text search.
     *
     * @return {@code true} if the search will start immediately, so any further keypresses
     * will be handled directly by the search UI. {@code false} if {@link Launcher} should continue
     * to buffer keypresses.
     */
    public boolean startSearch(String initialQuery,
                               boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        startGlobalSearch(initialQuery, selectInitialQuery,
                appSearchData, sourceBounds);
        return false;
    }

    /**
     * Starts the global search activity. This code is a copied from SearchManager
     */
    private void startGlobalSearch(String initialQuery,
                                   boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w(TAG, "No global search activity found.");
            return;
        }
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(globalSearchActivity);
        // Make sure that we have a Bundle to put source in
        if (appSearchData == null) {
            appSearchData = new Bundle();
        } else {
            appSearchData = new Bundle(appSearchData);
        }
        // Set source to package name of app that starts global search, if not set already.
        if (!appSearchData.containsKey("source")) {
            appSearchData.putString("source", getPackageName());
        }
        intent.putExtra(SearchManager.APP_DATA, appSearchData);
        if (!TextUtils.isEmpty(initialQuery)) {
            intent.putExtra(SearchManager.QUERY, initialQuery);
        }
        if (selectInitialQuery) {
            intent.putExtra(SearchManager.EXTRA_SELECT_QUERY, selectInitialQuery);
        }
        intent.setSourceBounds(sourceBounds);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Global search activity not found: " + globalSearchActivity);
        }
    }

    public boolean isOnCustomContent() {
        return mWorkspace.isOnOrMovingToCustomContent();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        LauncherLog.i(TAG, "onPrepareOptionsMenu : " + menu);
        /* Lenovo-sw luyy1 add start to fix LANCHROW-204 2015-07-07
         * If Setting Panel is currently showing ,then there is no need to switch mode.*/
        if (mIsSettingPanelShowing) {
            return false;
        }
        /* Lenovo-sw luyy1 add end to fix LANCHROW-204 2015-07-07*/
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-920 START */
        if (mState == State.APPS_CUSTOMIZE) {
        	return false;
        }
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-920 END */
        if (!isOnCustomContent()) {
            // Close any open folders
            closeFolder();
            // Stop resizing any widgets
            mWorkspace.exitWidgetResizeMode();
            /** Lenovo-SW zhaoxin5 20150804 XTHREEROW-430 START */
            if (!mWorkspace.isInOverviewMode()) {
                // Show the overview mode
                mWorkspace.enterOverviewMode();
            } else {
            	mWorkspace.exitOverviewMode(true);
            }
            /** Lenovo-SW zhaoxin5 20150804 XTHREEROW-430 END */
        }
        return false;
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        // Use a custom animation for launching search
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }

    public boolean isWorkspaceLoading() {
        return mWorkspaceLoading;
    }

    private void setWorkspaceLoading(boolean value) {
        boolean isLocked = isWorkspaceLocked();
        mWorkspaceLoading = value;
        if (isLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    private void setWaitingForResult(boolean value) {
        boolean isLocked = isWorkspaceLocked();
        mWaitingForResult = value;
        if (isLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    protected void onWorkspaceLockedChanged() {
    }

    private void resetAddInfo() {
        mPendingAddInfo.container = ItemInfo.NO_ID;
        mPendingAddInfo.screenId = -1;
        mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
        mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
        mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
        mPendingAddInfo.dropPos = null;
    }

    void addAppWidgetImpl(final int appWidgetId, final ItemInfo info,
                          final AppWidgetHostView boundWidget, final AppWidgetProviderInfo appWidgetInfo) {
        addAppWidgetImpl(appWidgetId, info, boundWidget, appWidgetInfo, 0);
    }

    void addAppWidgetImpl(final int appWidgetId, final ItemInfo info,
                          final AppWidgetHostView boundWidget, final AppWidgetProviderInfo appWidgetInfo, int
                                  delay) {
        if (appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;
            mPendingAddWidgetId = appWidgetId;

            // Launch over to configure widget, if needed
            mAppWidgetManager.startConfigActivity(appWidgetInfo, appWidgetId, this,
                    mAppWidgetHost, REQUEST_CREATE_APPWIDGET);

        } else {
            // Otherwise just add it
            Runnable onComplete = new Runnable() {
                @Override
                public void run() {
                    // Exit spring loaded mode if necessary after adding the widget
                    exitSpringLoadedDragModeDelayed(true, EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT,
                            null);
                }
            };
            completeAddAppWidget(appWidgetId, info.container, info.screenId, boundWidget,
                    appWidgetInfo);
            /** Lenovo-SW zhaoxin5 20150730 XTHREEROW-301 START */
            if(!mWorkspace.isInOverviewMode()) {
            	mWorkspace.removeExtraEmptyScreenDelayed(true, onComplete, delay, false);
            }
            /** Lenovo-SW zhaoxin5 20150730 XTHREEROW-301 END */
        }
    }

    /*Lenovo-sw luyy1 add 20150729 for Yandex search start*/
    void addAppWidgetImplAuto(final int appWidgetId, final ItemInfo info,
            final AppWidgetHostView boundWidget, final AppWidgetProviderInfo appWidgetInfo) {
        // Otherwise just add it
        Runnable onComplete = new Runnable() {
          @Override
          public void run() {
              // Exit spring loaded mode if necessary after adding the widget
              exitSpringLoadedDragModeDelayed(true, EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
          }
        };
        completeAddAppWidget(appWidgetId, info.container, info.screenId, boundWidget, appWidgetInfo);
    }
    /*Lenovo-sw luyy1 add 20150729 for Yandex search end*/

    protected void moveToCustomContentScreen(boolean animate) {
        // Close any folders that may be open.
        closeFolder();
        mWorkspace.moveToCustomContentScreen(animate);
    }

    /**
     * Process a shortcut drop.
     *
     * @param componentName The name of the component
     * @param screenId      The ID of the screen where it should be added
     * @param cell          The cell it should be added to, optional
     * @param position      The location on the screen where it was dropped, optional
     */
    void processShortcutFromDrop(ComponentName componentName, long container, long screenId,
                                 int[] cell, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = container;
        mPendingAddInfo.screenId = screenId;
        mPendingAddInfo.dropPos = loc;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }

        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    /**
     * Process a widget drop.
     *
     * @param info     The PendingAppWidgetInfo of the widget being added.
     * @param screenId The ID of the screen where it should be added
     * @param cell     The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, long screenId,
                              int[] cell, int[] span, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = info.container = container;
        mPendingAddInfo.screenId = info.screenId = screenId;
        mPendingAddInfo.dropPos = loc;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }

        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
            Bundle options = info.bindOptions;

            boolean success = mAppWidgetManager.bindAppWidgetIdIfAllowed(
                    appWidgetId, info.info, options);
            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                mAppWidgetManager.getUser(mPendingAddWidgetInfo)
                        .addToIntent(intent, AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE);
                // TODO: we need to make sure that this accounts for the options bundle.
                // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    void processShortcut(Intent intent) {
        Utilities.startActivityForResultSafely(this, intent, REQUEST_CREATE_SHORTCUT);
    }

    void processWallpaper(Intent intent) {
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    FolderIcon addFolder(CellLayout layout, long container, final long screenId, int cellX,
                         int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        /*Lenovo-sw zhangyj19 add 2015/09/24 modify Default Folder Name begin */
        //folderInfo.title = getText(R.string.folder_name);
        folderInfo.title = "";
        /*Lenovo-sw zhangyj19 add 2015/09/24 modify Default Folder Name end */
        // Update the model
        LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screenId, cellX, cellY,
                false);
        sFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder =
                FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo, mIconCache);
        if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            newFolder.setTextVisible(!mHideIconLabels);
        }
        mWorkspace.addInScreen(newFolder, container, screenId, cellX, cellY, 1, 1,
                isWorkspaceLocked());
        // Force measure the new folder icon
        CellLayout parent = mWorkspace.getParentCellLayoutForView(newFolder);
        parent.getShortcutsAndWidgets().measureChild(newFolder);
        return newFolder;
    }

    void removeFolder(FolderInfo folder) {
        sFolders.remove(folder.id);
        
		/*Lenovo-sw [zhanggx1] [MODEL] [2013-01-16] [begin] (for auto reorder)*/
		autoReorder("removeFolder");
		/*Lenovo-sw [zhanggx1] [MODEL] [2013-01-16] [end]*/
    }

    protected ComponentName getWallpaperPickerComponent() {
        return new ComponentName(getPackageName(), LauncherWallpaperPickerActivity.class.getName());
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                true, mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                /** Lenovo-SW zhaoxin5 20150817 KOLEOSROW-993 START */
                /*case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (isPropertyEnabled(DUMP_STATE_PROPERTY)) {
                        dumpState();
                        return true;
                    }
                    break;*/
                /** Lenovo-SW zhaoxin5 20150817 KOLEOSROW-993 END */
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (isBootProgressDialogShowing()) {
            return;
        }

        Fragment f1 = getFragmentManager().findFragmentByTag(
                HiddenFolderFragment.HIDDEN_FOLDER_FRAGMENT);
        if (f1 != null) {
            mHiddenFolderFragment.saveHiddenFolderStatus(-1);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction
                    .remove(mHiddenFolderFragment).commit();
        }
        if (isAllAppsVisible()) {
            if (mAppsCustomizeContent.getContentType() ==
                    AppsCustomizePagedView.ContentType.Applications) {
                showWorkspace(true);
                // Background was set to gradient in onPause(), restore to black if in all apps.
                setWorkspaceBackground(mState == State.WORKSPACE);
            } else {
                showOverviewMode(true, true); // Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
            }
        } else if (mWorkspace.isInOverviewMode()) {
            TransitionEffectsFragment tef =
                    (TransitionEffectsFragment) getFragmentManager().findFragmentByTag(
                            TransitionEffectsFragment.TRANSITION_EFFECTS_FRAGMENT);
            Fragment f2 = getFragmentManager().findFragmentByTag(
                    DynamicGridSizeFragment.DYNAMIC_GRID_SIZE_FRAGMENT);
            if (tef != null) {
                updateStatusBarColor(getResources().getColor(R.color.settings_header_bg_color));
                tef.setEffect();
            } else if (f2 != null) {
                mDynamicGridSizeFragment.setSize();
            } else {
                // if a user backs up twice very quickly from the widget add screen to the
                // homescreen, the UI can get into a messed up state and mStateAnimation never
                // completes or gets cancelled. Cancelling mStateAnimation here fixes this bug
                if (mStateAnimation != null && mStateAnimation.isRunning()) {
                    mStateAnimation.cancel();
                    mStateAnimation = null;
                }

                // Lenovo-sw:yuanyl2, Add setting function. Begin.
                if (mIsSettingPanelShowing) {
                    hideSettingsPanel();
                }
                // Lenovo-sw:yuanyl2, Add setting function. End.

                // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
                // mWorkspace.exitOverviewMode(true);
                // Close the current opened folder
                if (mWorkspace.getOpenFolder() != null) {
                    Folder openFolder = mWorkspace.getOpenFolder();
                    if (openFolder.isEditingName()) {
                        openFolder.dismissEditingName();
                    } else {
                        closeFolder();
                    }

                    return;
                }

                boolean shouldExitOverviewMode = mMenuController.onBackPressed();
                if (shouldExitOverviewMode) {
                    mWorkspace.exitOverviewMode(true);
                    /** Lenovo-SW zhaoxin5 20150806 XTHREEROW-585 START */
                    this.onWorkspaceShown(true, false); // Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
                    /** Lenovo-SW zhaoxin5 20150806 XTHREEROW-585 END */
                }

                // Lenovo-sw:yuanyl2, Add edit mode function. End.
            }
        } else if (mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder();
            }
        }
        /** Lenovo-SW zhaoxin5 20150621 add for quick share support START */
        else if (isQuickSharing) {
        	/** Lenovo-SW zhaoxin5 20150714 add for LANCHROW-233 START */
        	if(mPreviousQuickShareState != null && mPreviousQuickShareState == QuickShareState.BODY_2_HIDE) {

        	} else {
            	startQuickShareStateChange(QuickShareState.BODY_2_HIDE);
        	}
        	/** Lenovo-SW zhaoxin5 20150714 add for LANCHROW-233 END */
        	//animateShowOrHideQuickShareBar(false, false);
        }
        /** Lenovo-SW zhaoxin5 20150621 add for quick share support END */
        else {
            mWorkspace.exitWidgetResizeMode();

            // Back button is a no-op here, but give at least some feedback for the button press
          //Lenovo-sw zhangyj19 delete 2015/07/02 redraw screen, when onback
           //mWorkspace.showOutlinesTemporarily();
        }
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        if (mAppWidgetHost != null) {
            mAppWidgetHost.startListening();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        // Make sure that rogue clicks don't get through while allapps is launching, or after the
        // view has detached (it's possible for this to happen if the view is removed mid touch).
        if (v.getWindowToken() == null) {
            return;
        }

        if (!mWorkspace.isFinishedSwitchingState()) {
            return;
        }

        // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
        /** Lenovo-SW zhaoxin5 20150828 XTHREEROW-1106 START */
        // 如果当前dockview中选中的应用数不为空的话,
        // 此时即使在点击了workspace或者是celllayout
        // 也是不允许退出回到桌面的
        boolean dockViewEmpty = true;
        if (null != mDockView) {
        	dockViewEmpty = (mDockView.getLayout().getChildCount() == 0);
        }
        
        if (v instanceof Workspace && mWorkspace.isInOverviewMode()) {
        	if (LauncherAppState.getInstance().isCurrentAndroidMode()) {
        		// 如果是Android模式,直接退出
                mWorkspace.exitOverviewMode(true);
                return;
        	} else if (LauncherAppState.getInstance().isCurrentVibeuiMode() && dockViewEmpty) {
        		// 如果是Vibeui模式,且dockview选中的应用数为空
        		// 如果是Android模式,直接退出
                mWorkspace.exitOverviewMode(true);
                return;
        	}
        }

        if (v instanceof CellLayout && mWorkspace.isInOverviewMode()) {
        	if (LauncherAppState.getInstance().isCurrentAndroidMode()) {
        		// 如果是Android模式,直接退出
        		mWorkspace.exitOverviewMode(mWorkspace.indexOfChild(v), true);
                return;
        	} else if (LauncherAppState.getInstance().isCurrentVibeuiMode() && dockViewEmpty) {
        		// 如果是Vibeui模式,且dockview选中的应用数为空
        		// 如果是Android模式,直接退出
        		mWorkspace.exitOverviewMode(mWorkspace.indexOfChild(v), true);
                return;
        	}
        }
        /** Lenovo-SW zhaoxin5 20150828 XTHREEROW-1106 END */
        // Lenovo-sw:yuanyl2, Add edit mode function. Begin.

        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            onClickAppShortcut(v);
        } else if (tag instanceof FolderInfo) {
            if (v instanceof FolderIcon) {
                //文件夹点击入口
                onClickFolderIcon(v);
            }
        } else if (v == mAllAppsButton) {
            onClickAllAppsButton(v);
        } else if (tag instanceof AppInfo) {
            startAppShortcutOrInfoActivity(v);
        } else if (tag instanceof LauncherAppWidgetInfo) {
            if (v instanceof PendingAppWidgetHostView) {
                onClickPendingWidget((PendingAppWidgetHostView) v);
            }
        }
    }

    public void onClickPagedViewIcon(View v) {
        startAppShortcutOrInfoActivity(v);
    }

    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    /**
     * Event handler for the app widget view which has not fully restored.
     */
    public void onClickPendingWidget(final PendingAppWidgetHostView v) {
        final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) v.getTag();
        if (v.isReadyForClickSetup()) {
            int widgetId = info.appWidgetId;
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widgetId);
            if (appWidgetInfo != null) {
                mPendingAddWidgetInfo = appWidgetInfo;
                mPendingAddInfo.copyFrom(info);
                mPendingAddWidgetId = widgetId;

                AppWidgetManagerCompat.getInstance(this).startConfigActivity(appWidgetInfo,
                        info.appWidgetId, this, mAppWidgetHost, REQUEST_RECONFIGURE_APPWIDGET);
            }
        } else if (info.installProgress < 0) {
            // The install has not been queued
            final String packageName = info.providerName.getPackageName();
            showBrokenAppInstallDialog(packageName,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivitySafely(v, LauncherModel.getMarketIntent(packageName), info);
                        }
                    });
        } else {
            // Download has started.
            final String packageName = info.providerName.getPackageName();
            startActivitySafely(v, LauncherModel.getMarketIntent(packageName), info);
        }
    }

    /**
     * Event handler for the search button
     *
     * @param v The view that was clicked.
     */
    public void onClickSearchButton(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        onSearchRequested();
    }

    /**
     * Event handler for the voice button
     *
     * @param v The view that was clicked.
     */
    public void onClickVoiceButton(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        startVoice();
    }

    public void startVoice() {
        try {
            final SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName activityName = searchManager.getGlobalSearchActivity();
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (activityName != null) {
                intent.setPackage(activityName.getPackageName());
            }
            startActivity(null, intent, "onClickVoiceButton");
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivitySafely(null, intent, "onClickVoiceButton");
        }
    }

    /**
     * Event handler for the "grid" button that appears on the home screen, which
     * enters all apps mode.
     *
     * @param v The view that was clicked.
     */
    protected void onClickAllAppsButton(View v) {
        if (LOGD) Log.d(TAG, "onClickAllAppsButton");
        if (!LauncherAppState.isDisableAllAppsHotseat()) {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.letv.android.letvlive", "com.letv.android.letvlive.LiveActivity");
                startActivity(intent);
                return;
            } catch (Exception e) {
                Log.e(TAG, " launch com.letv.android.letvlive.LiveActivity failed : " + e.toString());
            }
        }
        if (isAllAppsVisible()) {
            showWorkspace(true);
        } else {
            showAllApps(true, AppsCustomizePagedView.ContentType.Applications, false);
        }
    }

    private void showBrokenAppInstallDialog(final String packageName,
                                            DialogInterface.OnClickListener onSearchClickListener) {
        new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault))
                .setTitle(R.string.abandoned_promises_title)
                .setMessage(R.string.abandoned_promise_explanation)
                .setPositiveButton(R.string.abandoned_search, onSearchClickListener)
                .setNeutralButton(R.string.abandoned_clean_this,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final UserHandleCompat user = UserHandleCompat.myUserHandle();
                                mWorkspace.removeAbandonedPromise(packageName, user);
                            }
                        })
                .create().show();
        return;
    }

    /**
     * Event handler for an app shortcut click.
     *
     * @param v The view that was clicked. Must be a tagged with a {@link ShortcutInfo}.
     */
    protected void onClickAppShortcut(final View v) {
        if (LOGD) Log.d(TAG, "onClickAppShortcut");
        Object tag = v.getTag();
        if (!(tag instanceof ShortcutInfo)) {
            throw new IllegalArgumentException("Input must be a Shortcut");
        }

        // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
        if (mWorkspace.isInOverviewMode() && LauncherAppState.getInstance().isCurrentVibeuiMode()) {
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
            ItemInfo itemInfo = (ItemInfo) tag;
            if (itemInfo.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                Log.v(TAG, "edit view mode, this info is in hotseat. ");
                return;
            }
            if (!mMenuController.canAcceptShortcut()) {
                Log.v(TAG, "edit view mode, cannot move icon when sub item of menu list is showing");
                return;
            }

            showDockView();
            if (!mDockView.isDockViewFull()) {
                if (!mDockView.isFolderMode()) {
                    View view = mDockView.addExternalItem(itemInfo, 0);
                    mDockView.updateSelectCount();
                    if (view != null) {
                        animViewToDockView(v, view, (ItemInfo) tag);
                    }
                } else {
                    FolderIcon folderIcon = mDockView.getFolderIcon();
                    animViewToDockFolderIcon(v, folderIcon, ((ItemInfo) tag));
                }
            } else {
                mDockView.showOutOfDockViewMessage();
                if (v instanceof BubbleTextView) {
                    ((BubbleTextView) v).setStayPressed(false);
                    v.invalidate();
                }
            }
            return;
        }
        // Lenovo-sw:yuanyl2, Add edit mode function. End.

        // Open shortcut
        final ShortcutInfo shortcut = (ShortcutInfo) tag;
        final Intent intent = shortcut.intent;

        // Check for special shortcuts
        if (intent.getComponent() != null) {
            final String shortcutClass = intent.getComponent().getClassName();

            if (shortcutClass.equals(MemoryDumpActivity.class.getName())) {
                MemoryDumpActivity.startDump(this);
                return;
            } else if (shortcutClass.equals(ToggleWeightWatcher.class.getName())) {
                toggleShowWeightWatcher();
                return;
            }
        }

        // Check for abandoned promise
        if ((v instanceof BubbleTextView)
                && shortcut.isPromise()
                && !shortcut.hasStatusFlag(ShortcutInfo.FLAG_INSTALL_SESSION_ACTIVE)) {
            showBrokenAppInstallDialog(
                    shortcut.getTargetComponent().getPackageName(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startAppShortcutOrInfoActivity(v);
                        }
                    });
            return;
        }
        if (DeviceInfoUtils.isInstallShuangkaihezi(this, "com.ptns.da.dl")) {
            //判断微信是否有双开快捷方式
            if (intent.getComponent() != null && intent.getComponent().getPackageName().equals("com.tencent.mm") &&
                    mSharedPrefs.getBoolean(IS_SHOW_SK_SC_DIALOG_AGAIN_WECHAT, true)) {
                if (!isShungkaiShortCutExist("com.tencent.mm")) {
                    showShuangkaiCreateShortcutDialog(v, "com.tencent.mm");
                    return;
                }
            } else if (intent.getComponent() != null && intent.getComponent().getPackageName().equals("com.tencent.mobileqq") &&
                    mSharedPrefs.getBoolean(IS_SHOW_SK_SC_DIALOG_AGAIN_QQ, true)) {
                //判断QQ是否有双开快捷方式
                if (!isShungkaiShortCutExist("com.tencent.mobileqq")) {
                    showShuangkaiCreateShortcutDialog(v, "com.tencent.mobileqq");
                    return;
                }

            }
        }
        // Start activities
        startAppShortcutOrInfoActivity(v);
    }

    private void showShuangkaiCreateShortcutDialog(final View scview,final String packageName){
        final Dialog selectDialog = new Dialog(this, R.style.shuangkai_dialog);
        selectDialog.setCancelable(true);
        selectDialog.setContentView(R.layout.shuangkai_dialog_layout);
        ImageView iv = (ImageView) selectDialog
                .findViewById(R.id.ImageView01);
        TextView textView01 = (TextView) selectDialog
                .findViewById(R.id.title01);
        final CheckBox cb = (CheckBox) selectDialog
                .findViewById(R.id.create_sc_no_show_cb);
        TextView createbtn = (TextView) selectDialog.findViewById(R.id.create_sc_btn);
        TextView no_createbtn = (TextView) selectDialog.findViewById(R.id.no_create_sc_btn);

        if (packageName.equals("com.tencent.mm")) {
            textView01
                    .setText(R.string.shuangkai_shortcut_wc_tips);
            iv.setBackgroundResource(R.drawable.logo_wechat);
            createbtn.setOnClickListener(new TextView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectDialog.dismiss();
                    startAppShortcutOrInfoActivity(scview);
                    try {
                        Intent start = new Intent();
                        start.setClassName("com.ptns.da.dl","com.ptns.da.sdk.DlShortCutService");
                        start.putExtra("packageName", packageName);
                        start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startService(start);
                    }catch (Exception e){

                    }

                    if (cb.isChecked()) {
                        SharedPreferences.Editor editor = mSharedPrefs.edit();
                        editor.putBoolean(IS_SHOW_SK_SC_DIALOG_AGAIN_WECHAT, false);
                        editor.apply();
                    }
                }
            });
            no_createbtn.setOnClickListener(new TextView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectDialog.dismiss();
                    startAppShortcutOrInfoActivity(scview);
                    if (cb.isChecked()) {
                        SharedPreferences.Editor editor = mSharedPrefs.edit();
                        editor.putBoolean(IS_SHOW_SK_SC_DIALOG_AGAIN_WECHAT, false);
                        editor.apply();
                    }
                }
            });
        } else if (packageName.equals("com.tencent.mobileqq")) {
            textView01
                    .setText(R.string.shuangkai_shortcut_qq_tips);
            iv.setBackgroundResource(R.drawable.logo_qq);
            createbtn.setOnClickListener(new TextView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectDialog.dismiss();
                    startAppShortcutOrInfoActivity(scview);
                    try {
                        Intent start = new Intent();
                        start.setClassName("com.ptns.da.dl","com.ptns.da.sdk.DlShortCutService");
                        start.putExtra("packageName", packageName);
                        start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startService(start);
                    }catch (Exception e){

                    }
                    if (cb.isChecked()) {
                        SharedPreferences.Editor editor = mSharedPrefs.edit();
                        editor.putBoolean(IS_SHOW_SK_SC_DIALOG_AGAIN_QQ, false);
                        editor.apply();
                    }
                }
            });

            no_createbtn.setOnClickListener(new TextView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectDialog.dismiss();
                    startAppShortcutOrInfoActivity(scview);
                    if (cb.isChecked()) {
                        SharedPreferences.Editor editor = mSharedPrefs.edit();
                        editor.putBoolean(IS_SHOW_SK_SC_DIALOG_AGAIN_QQ, false);
                        editor.apply();
                    }
                }
            });
        }
        selectDialog.show();
    }

    private boolean isShungkaiShortCutExist(String packageName){
        return checkAppIsCreateShuangkaiShortcut(packageName);
    }

    private boolean checkAppIsCreateShuangkaiShortcut(String packageName){
        boolean isCreated = false;
        List<ItemInfo> binditems = mModel.getWorkSpaceBinditems();
        for (ItemInfo item : binditems) {
            try {
                ShortcutInfo si = (ShortcutInfo) item;
                if (si.getIntent() != null && si.getIntent().toString().contains("com.ptns.da.ShordlutActivityls")) {
                    Log.d("hw","checkAppIsCreateShuangkaiShortcut getIntent : " + si.getIntent().toString());
                    Bundle b = si.getIntent().getExtras();
                    if (b != null) {
                        String extras = b.toString();
                        Log.d("hw","checkAppIsCreateShuangkaiShortcut " + packageName + " extras : " + extras);
                        if (extras.contains(packageName)) {
                            return true;
                        }
                    }
                }
            }catch (Exception e){
            }
        }
        return isCreated;
    }

    private void startAppShortcutOrInfoActivity(View v) {
        Object tag = v.getTag();
        final ShortcutInfo shortcut;
        final Intent intent;
        if (tag instanceof ShortcutInfo) {
            shortcut = (ShortcutInfo) tag;
            intent = shortcut.intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1],
                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));

        } else if (tag instanceof AppInfo) {
            shortcut = null;
            intent = ((AppInfo) tag).intent;
        } else {
            throw new IllegalArgumentException("Input must be a Shortcut or AppInfo");
        }

        /** Lenovo-SW zhaoxin5 20151103 PASSIONROW-2236 PASSIONROW-2237 START */
        /*final ComponentName componentName = intent.getComponent();
        if (componentName != null) {
            String name = componentName.getPackageName();
            PackageManager packageManager = getPackageManager();

            try {
                String sourceDir = (packageManager.getApplicationInfo(name, 0)).sourceDir;

                if (!new File(sourceDir).exists()) {
                    Toast.makeText(this, R.string.activity_not_found,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NameNotFoundException e) {
                Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
                return;
            }
        }*/
        /** Lenovo-SW zhaoxin5 20151103 PASSIONROW-2236 PASSIONROW-2237 END */

        boolean success = startActivitySafely(v, intent, tag);
        mStats.recordLaunch(intent, shortcut);

        if (success && v instanceof BubbleTextView) {
            mWaitingForResume = (BubbleTextView) v;
            mWaitingForResume.setStayPressed(true);
        }
    }

    /**
     * Event handler for a folder icon click.
     *
     * @param v The view that was clicked. Must be an instance of {@link FolderIcon}.
     */
    protected void onClickFolderIcon(View v) {
        if (LOGD) Log.d(TAG, "onClickFolder");
        if (!(v instanceof FolderIcon)) {
            throw new IllegalArgumentException("Input must be a FolderIcon");
        }

        FolderIcon folderIcon = (FolderIcon) v;

        // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
        if(folderIcon.isStackFolderIcon == true){
            //click folder of dockview, it need fly items to this folder
            if(mDockView.isStackMode()){
                mDockView.flyToStackFolderIcon(folderIcon);
            }else if(mDockView.isFolderMode()){
                mDockView.freeStackFolderIcon();
            }
            return;
        }
        // Lenovo-sw:yuanyl2, Add edit mode function. End.

        final FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = mWorkspace.getFolderForTag(info);

        // If the folder info reports that the associated folder is open, then verify that
        // it is actually opened. There have been a few instances where this gets out of sync.
        if (info.opened && openFolder == null) {
            Log.d(TAG, "Folder info marked as open, but associated folder is not open. Screen: "
                    + info.screenId + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }

        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderIcon);
        } else {
            // Find the open folder...
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getPageForView(openFolder);
                // .. and close it
                closeFolder(openFolder, true);
                if (folderScreen != mWorkspace.getCurrentPage()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderIcon);
                }
            }
        }
    }

    /**
     * Event handler for the (Add) Widgets button that appears after a long press
     * on the home screen.
     */
    protected void onClickAddWidgetButton(View view) {
        if (LOGD) Log.d(TAG, "onClickAddWidgetButton");
        showAllApps(true, AppsCustomizePagedView.ContentType.Widgets, true);
    }

    /**
     * Event handler for the wallpaper picker button that appears after a long press
     * on the home screen.
     */
    protected void onClickWallpaperPicker(View v) {
        if (LOGD) Log.d(TAG, "onClickWallpaperPicker");
        // LENOVO-SW:YUANYL2, use LenovoWallpaper instead.
        /*        
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        pickWallpaper.setComponent(getWallpaperPickerComponent());
        startActivityForResult(pickWallpaper, REQUEST_PICK_WALLPAPER);
        */
        if (this.getResources().getBoolean(R.bool.is_tablet)) {
            Intent intent = new Intent();
            intent.setAction("com.lenovo.launcher.action.WALLPAPER_SETTING");
            startActivity(intent);
        } else {
        	/* Lenovo-SW zhaoxin5 20150601 start themecenter wallpaper */
            LauncherLog.i("xixia", "Start themecenter wallpaper picker");
            Intent newIntent = new Intent(
                    "com.lenovo.themecenter.action.wallpaperex");
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(newIntent);
        	/* Lenovo-SW zhaoxin5 20150601 start themecenter wallpaper */
        }
    }

    /**
     * Event handler for a click on the settings button that appears after a long press
     * on the home screen.
     */
    protected void onClickSettingsButton(View v) {
        if (LOGD) Log.d(TAG, "onClickSettingsButton");
    }

    public void onTouchDownAllAppsButton(View v) {
        // Provide the same haptic feedback that the system offers for virtual keys.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public void performHapticFeedbackOnTouchDown(View v) {
        // Provide the same haptic feedback that the system offers for virtual keys.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public View.OnTouchListener getHapticFeedbackTouchListener() {
        if (mHapticFeedbackTouchListener == null) {
            mHapticFeedbackTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    return false;
                }
            };
        }
        return mHapticFeedbackTouchListener;
    }

    public void onDragStarted(View view) {
    }

    /**
     * Called when the user stops interacting with the launcher.
     * This implies that the user is now on the homescreen and is not doing housekeeping.
     */
    protected void onInteractionEnd() {
    }

    /**
     * Called when the user starts interacting with the launcher.
     * The possible interactions are:
     * - open all apps
     * - reorder an app shortcut, or a widget
     * - open the overview mode.
     * This is a good time to stop doing things that only make sense
     * when the user is on the homescreen and not doing housekeeping.
     */
    protected void onInteractionBegin() {
    }

    void startApplicationDetailsActivity(ComponentName componentName, UserHandleCompat user) {
        String packageName = componentName.getPackageName();
        try {
            LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
            UserManagerCompat userManager = UserManagerCompat.getInstance(this);
            launcherApps.showAppDetailsForProfile(componentName, user);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have permission to launch settings");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch settings");
        }
    }

    // returns true if the activity was started
    boolean startApplicationUninstallActivity(ComponentName componentName, int flags,
                                              UserHandleCompat user) {
        if ((flags & AppInfo.DOWNLOADED_FLAG) == 0) {
            // System applications cannot be installed. For now, show a toast explaining that.
            // We may give them the option of disabling apps this way.
            int messageId = R.string.uninstall_system_app_text;
            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String packageName = componentName.getPackageName();
            String className = componentName.getClassName();
            Intent intent = new Intent(
                    Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            if (user != null) {
                user.addToIntent(intent, Intent.EXTRA_USER);
            }
            startActivity(intent);
            return true;
        }
    }

    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
            LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
            UserManagerCompat userManager = UserManagerCompat.getInstance(this);

            UserHandleCompat user = null;
            if (intent.hasExtra(AppInfo.EXTRA_PROFILE)) {
                long serialNumber = intent.getLongExtra(AppInfo.EXTRA_PROFILE, -1);
                user = userManager.getUserForSerialNumber(serialNumber);
            }

            Bundle optsBundle = null;
            if (useLaunchAnimation) {
                ActivityOptions opts = Utilities.isLmpOrAbove() ?
                        ActivityOptions.makeCustomAnimation(this, R.anim.task_open_enter, R.anim.no_anim) :
                        ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
                optsBundle = opts.toBundle();
            }

            if (user == null || user.equals(UserHandleCompat.myUserHandle())) {
                // Could be launching some bookkeeping activity
                startActivity(intent, optsBundle);
            } else {
                // TODO Component can be null when shortcuts are supported for secondary user
                launcherApps.startActivityForProfile(intent.getComponent(), user,
                        intent.getSourceBounds(), optsBundle);
            }
            String packageName = intent.getPackage();
            if (packageName == null && intent.getComponent() != null) {
                packageName = intent.getComponent().getPackageName();
            }
            PingManager.getInstance().reportUserAction4App(
                    PingManager.USER_ACTION_CLICK, packageName);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag=" + tag + " intent=" + intent, e);
        }
        return false;
    }

    public boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        if (mIsSafeModeEnabled && !Utilities.isSystemApp(this, intent)) {
            Toast.makeText(this, R.string.safemode_shortcut_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }

    /**
     * This method draws the FolderIcon to an ImageView and then adds and positions that ImageView
     * in the DragLayer in the exact absolute location of the original FolderIcon.
     */
    private void copyFolderIconToImage(FolderIcon fi) {
        final int width = fi.getMeasuredWidth();
        final int height = fi.getMeasuredHeight();

        // Lazy load ImageView, Bitmap and Canvas
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(this);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width ||
                mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }

        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }

        // The layout from which the folder is being opened may be scaled, adjust the starting
        // view size by this scale factor.
        float scale = mDragLayer.getDescendantRectRelativeToSelf(fi, mRectForFolderAnimation);
        lp.customPosition = true;
        lp.x = mRectForFolderAnimation.left;
        lp.y = mRectForFolderAnimation.top;
        lp.width = (int) (scale * width);
        lp.height = (int) (scale * height);

        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (fi.getFolder() != null) {
            mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        // Just in case this image view is still in the drag layer from a previous animation,
        // we remove it and re-add it.
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mDragLayer.addView(mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.5f);

        FolderInfo info = (FolderInfo) fi.getTag();
        if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            CellLayout cl = (CellLayout) fi.getParent().getParent();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
            cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
        }

        // Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
        copyFolderIconToImage(fi);
        fi.setVisibility(View.INVISIBLE);

        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        if (Utilities.isLmpOrAbove()) {
            oa.setInterpolator(new LogDecelerateInterpolator(100, 0));
        }
        oa.setDuration(getResources().getInteger(R.integer.config_folderExpandDuration));
        oa.start();
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);

        final CellLayout cl = (CellLayout) fi.getParent().getParent();

        // We remove and re-draw the FolderIcon in-case it has changed
        mDragLayer.removeView(mFolderIconImageView);
        copyFolderIconToImage(fi);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderExpandDuration));
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (cl != null) {
                    cl.clearFolderLeaveBehind();
                    // Remove the ImageView copy of the FolderIcon and make the original visible.
                    mDragLayer.removeView(mFolderIconImageView);
                    fi.setVisibility(View.VISIBLE);
                }
            }
        });
        oa.start();
    }

    /**
     * Opens the user folder described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    public void openFolder(FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        FolderInfo info = folder.mInfo;
        //根据 文件夹名称 设置文件夹ID
        info.setFolderId();
        if (info.hidden) {
            folder.startHiddenFolderManager();
            return;
        }

        info.opened = true;

        // Just verify that the folder hasn't already been added to the DragLayer.
        // There was a one-off crash where the folder had a parent already.
        if (folder.getParent() == null) {
            LauncherAppState app = LauncherAppState.getInstance();
            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int screenWidth = wm.getDefaultDisplay().getWidth();
            int screenHeight = wm.getDefaultDisplay().getHeight();
            int folderWidth = 3*grid.folderCellWidthPx;
            int folderHeight = (int)(screenHeight*0.9);
            int folderMargTop = (int)(screenHeight*0.1);
            //加载初始化推荐应用
//            folder.initAPUS(info.folderId,false);
            //DragLayer.LayoutParams params = new  DragLayer.LayoutParams(folderWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            DragLayer.LayoutParams params = new  DragLayer.LayoutParams(folderWidth, folderHeight);
            LogUtil.d("wqh_Folder",folderWidth+" x "+folderHeight);
            //左右边距
            int  margin = (screenWidth - 3*grid.folderCellWidthPx)/2;
            /*params.gravity = Gravity.CENTER_VERTICAL;
            params.setMargins(margin,folderMargTop,margin,0);*/
            params.gravity = Gravity.BOTTOM;
            //params.gravity = Gravity.CENTER_VERTICAL;
            //params.setMargins(margin,50,margin,0);
            params.setMargins(margin,100,margin,0);
//            params.setMargins(margin,folderMargTop,margin,0);
            mDragLayer.addView(folder,params);
            mDragController.addDropTarget((DropTarget) folder);
        } else {
            Log.w(TAG, "Opening folder (" + folder + ") which already has a parent (" +
                    folder.getParent() + ").");
        }
        folder.animateOpen();
        growAndFadeOutFolderIcon(folderIcon);

        // Notify the accessibility manager that this folder "window" has appeared and occluded
        // the workspace items
        folder.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    /** Lenovo-SW zhaoxin5 20150812 从文件夹中启动应用关闭退出文件夹的动画 START */
    public void closeFolder() {
    	closeFolder(true);
    }
    /** Lenovo-SW zhaoxin5 20150812 从文件夹中启动应用关闭退出文件夹的动画 END */

    public void closeFolder(boolean animate) {
        Folder folder = mWorkspace != null ? mWorkspace.getOpenFolder() : null;
        if (folder != null) {
            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            closeFolder(folder, animate);
        }
    }

    void closeFolder(Folder folder, boolean animate) {
        folder.getInfo().opened = false;

        ViewGroup parent = (ViewGroup) folder.getParent().getParent();
        if (parent != null) {
            FolderIcon fi = (FolderIcon) mWorkspace.getViewForTag(folder.mInfo);
            shrinkAndFadeInFolderIcon(fi);
            /** Lenovo-SW zhaoxin5 20150701 LANCHROW-181 START */
            if(folder.getItemCount() < 1) {
            	folder.removeEmptyFolderFromWorkspace();
            }
            /** Lenovo-SW zhaoxin5 20150701 LANCHROW-181 END */
        }
        //dimiss bannar 广告弹框
        if(folder.bannerPopuWindow != null && folder.bannerPopuWindow.isShowing()){
            folder.bannerPopuWindow.dismiss();
        }
        /** Lenovo-SW zhaoxin5 20150812 从文件夹中启动应用关闭退出文件夹的动画 START */
        if(animate) {
            folder.animateClosed();	
        } else {
        	folder.closeWithoutAnimate();
        }
        /** Lenovo-SW zhaoxin5 20150812 从文件夹中启动应用关闭退出文件夹的动画 END */

        // Notify the accessibility manager that this folder "window" has disappeard and no
        // longer occludeds the workspace items
        getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    public boolean onLongClick(View v) {
        if (!isDraggingEnabled()) return false;//klaucher 正在加载  不能操作 返回
        if (isWorkspaceLocked()) return false; //klaucher  锁定 不能操作
        if (mState != State.WORKSPACE) return false;
        if (v instanceof Workspace) {
            if (!mWorkspace.isInOverviewMode()) {
                if (mWorkspace.enterOverviewMode()) {
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        CellLayout.CellInfo longClickCellInfo = null;
        View itemUnderLongClick = null;
        if (v.getTag() instanceof ItemInfo) {
            ItemInfo info = (ItemInfo) v.getTag();
            longClickCellInfo = new CellLayout.CellInfo(v, info);
            itemUnderLongClick = longClickCellInfo.cell;
            resetAddInfo();
        }

        // The hotseat touch handling does not go through Workspace, and we always allow long press
        // on hotseat items.
        final boolean inHotseat = isHotseatLayout(v);
        boolean allowLongPress = inHotseat || mWorkspace.allowLongPress();
        if (allowLongPress && !mDragController.isDragging()) {
            if (itemUnderLongClick == null) {
                // User long pressed on empty space
                mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                if (mWorkspace.isInOverviewMode()) {
                    mWorkspace.startReordering(v);
                } else {
                    mWorkspace.enterOverviewMode();
                }
            } else {
                final boolean isAllAppsButton = inHotseat && isAllAppsButtonRank(
                        mHotseat.getOrderInHotseat(
                                longClickCellInfo.cellX,
                                longClickCellInfo.cellY));
                if (!(itemUnderLongClick instanceof Folder || isAllAppsButton)) {
                    // User long pressed on an item
                    mWorkspace.startDrag(longClickCellInfo);
                }
            }
        }
        return true;
    }

    boolean isHotseatLayout(View layout) {
        return mHotseat != null && layout != null &&
                (layout instanceof CellLayout) && (layout == mHotseat.getLayout());
    }

    View getDarkPanel() {
        return mDarkPanel;
    }

    /**
     * Returns the CellLayout of the specified container at the specified screen.
     */
    CellLayout getCellLayout(long container, long screenId) {
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (mHotseat != null) {
                return mHotseat.getLayout();
            } else {
                return null;
            }
        } else {
            return (CellLayout) mWorkspace.getScreenWithId(screenId);
        }
    }

    public AppsCustomizePagedView getAppsCustomizeContent() {
        return mAppsCustomizeContent;
    }

    public void updateOverviewPanel() {
        mOverviewSettingsPanel.update();
    }

    public boolean isAllAppsVisible() {
        return (mState == State.APPS_CUSTOMIZE) || (mOnResumeState == State.APPS_CUSTOMIZE);
    }

    private void setWorkspaceBackground(boolean workspace) {
        mLauncherView.setBackground(workspace ?
                mWorkspaceBackgroundDrawable : null);
    }

    protected void changeWallpaperVisiblity(boolean visible) {
        int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int curflags = getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (wpflags != curflags) {
            getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }
        setWorkspaceBackground(visible);
    }

    private void dispatchOnLauncherTransitionPrepare(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionPrepare(this, animated, toWorkspace);
        }
    }

    private void dispatchOnLauncherTransitionStart(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStart(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 0f);
    }

    private void dispatchOnLauncherTransitionStep(View v, float t) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
        }
    }

    private void dispatchOnLauncherTransitionEnd(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionEnd(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 1f);
    }

    /**
     * Things to test when changing the following seven functions.
     *   - Home from workspace
     *          - from center screen
     *          - from other screens
     *   - Home from all apps
     *          - from center screen
     *          - from other screens
     *   - Back from all apps
     *          - from center screen
     *          - from other screens
     *   - Launch app from workspace and quit
     *          - with back
     *          - with home
     *   - Launch app from all apps and quit
     *          - with back
     *          - with home
     *   - Go to a screen that's not the default, then all
     *     apps, and launch and app, and go back
     *          - with back
     *          -with home
     *   - On workspace, long press power and go back
     *          - with back
     *          - with home
     *   - On all apps, long press power and go back
     *          - with back
     *          - with home
     *   - On workspace, power off
     *   - On all apps, power off
     *   - Launch an app and turn off the screen while in that app
     *          - Go back with home key
     *          - Go back with back key  TODO: make this not go to workspace
     *          - From all apps
     *          - From workspace
     *   - Enter and exit car mode (becuase it causes an extra configuration changed)
     *          - From all apps
     *          - From the center workspace
     *          - From another workspace
     */

    /**
     * Zoom the camera out from the workspace to reveal 'toView'.
     * Assumes that the view to show is anchored at either the very top or very bottom
     * of the screen.
     */
    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded) {
        AppsCustomizePagedView.ContentType contentType = mAppsCustomizeContent.getContentType();
        showAppsCustomizeHelper(animated, springLoaded, contentType);
    }

    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded,
                                         final AppsCustomizePagedView.ContentType contentType) {
        /** Lenovo-SW luyy1 20150910 change for primary home screen START */
        if (mDefaultHomeScreenPanel != null) {
            mDefaultHomeScreenPanel.setVisibility(View.INVISIBLE);
        }
        /** Lenovo-SW luyy1 20150910 change for primary home screen END */
        if (mStateAnimation != null) {
            mStateAnimation.setDuration(0);
            mStateAnimation.cancel();
            mStateAnimation = null;
        }

        boolean material = Utilities.isLmpOrAbove();
        final boolean drawer = mDrawerType == AppDrawerListAdapter.DrawerType.Drawer;
        final Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomInTime);
        final int fadeDuration = res.getInteger(R.integer.config_appsCustomizeFadeInTime);
        final int revealDuration = res.getInteger(R.integer.config_appsCustomizeRevealTime);
        final int itemsAlphaStagger =
                res.getInteger(R.integer.config_appsCustomizeItemsAlphaStagger);

        final float scale = (float) res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mWorkspace;
        final View toView;

        if (drawer && contentType == AppsCustomizePagedView.ContentType.Applications) {
            toView = findViewById(R.id.app_drawer_container);
        } else {
            toView = mAppsCustomizeTabHost;
        }

        final ArrayList<View> layerViews = new ArrayList<View>();

        Workspace.State workspaceState = contentType == AppsCustomizePagedView.ContentType.Widgets ?
                Workspace.State.OVERVIEW_HIDDEN : Workspace.State.NORMAL_HIDDEN;
        Animator workspaceAnim =
                mWorkspace.getChangeStateAnimation(workspaceState, animated, layerViews);
        if (!LauncherAppState.isDisableAllApps()
                || contentType == AppsCustomizePagedView.ContentType.Widgets) {
            // Set the content type for the all apps/widgets space
            Log.i("xixia", "Launcher.showAppsCustomizeHelper");
            mAppsCustomizeTabHost.setContentTypeImmediate(contentType);
        }

        // If for some reason our views aren't initialized, don't animate
        boolean initialized = getAllAppsButton() != null;

        if (animated && initialized) {
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            final AppsCustomizePagedView content = (AppsCustomizePagedView)
                    toView.findViewById(R.id.apps_customize_pane_content);

            final View page = content != null ? content.getPageAt(content.getCurrentPage())
                    : toView.findViewById(R.id.app_drawer_view);
            final View revealView = toView.findViewById(R.id.fake_page);

            final float initialPanelAlpha = 1f;

            final boolean isWidgetTray = contentType == AppsCustomizePagedView.ContentType.Widgets;
            if (isWidgetTray) {
                revealView.setBackground(res.getDrawable(R.drawable.quantum_panel_dark));
            } else {
                if (drawer) {
                    revealView.setBackgroundColor(res.getColor(R.color.app_drawer_background));
                } else {
                    Drawable bg = res.getDrawable(R.drawable.quantum_panel);
                    if (bg != null) {
                    	// Lenovo-SW zhaoxin5 20150714 change allApps background to full white
                        // bg.setAlpha(128);
                        revealView.setBackground(bg);
                    }
                }
            }

            // Hide the real page background, and swap in the fake one
            if (content != null) {
                content.setPageBackgroundsVisible(false);
            } else {
                toView.setBackgroundColor(Color.TRANSPARENT);
            }
            revealView.setVisibility(View.VISIBLE);
            // We need to hide this view as the animation start will be posted.
            revealView.setAlpha(0);

            int width = revealView.getMeasuredWidth();
            int height = revealView.getMeasuredHeight();
            float revealRadius = (float) Math.sqrt((width * width) / 4 + (height * height) / 4);

            revealView.setTranslationY(0);
            revealView.setTranslationX(0);

            // Get the y delta between the center of the page and the center of the all apps button
            int[] allAppsToPanelDelta = Utilities.getCenterDeltaInScreenSpace(revealView,
                    getAllAppsButton(), null);

            float alpha = 0;
            float xDrift = 0;
            float yDrift = 0;
            if (material) {
                alpha = isWidgetTray ? 0.3f : 1f;
                yDrift = isWidgetTray ? height / 2 : allAppsToPanelDelta[1];
                xDrift = isWidgetTray ? 0 : allAppsToPanelDelta[0];
            } else {
                yDrift = 2 * height / 3;
                xDrift = 0;
            }
            final float initAlpha = alpha;

            revealView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            layerViews.add(revealView);
            PropertyValuesHolder panelAlpha = PropertyValuesHolder.ofFloat("alpha", initAlpha, 1f);
            PropertyValuesHolder panelDriftY =
                    PropertyValuesHolder.ofFloat("translationY", yDrift, 0);
            PropertyValuesHolder panelDriftX =
                    PropertyValuesHolder.ofFloat("translationX", xDrift, 0);

            ObjectAnimator panelAlphaAndDrift = ObjectAnimator.ofPropertyValuesHolder(revealView,
                    panelAlpha, panelDriftY, panelDriftX);

            panelAlphaAndDrift.setDuration(revealDuration);
            panelAlphaAndDrift.setInterpolator(new LogDecelerateInterpolator(100, 0));

            mStateAnimation.play(panelAlphaAndDrift);

            final View drawerContent = content == null ?
                    toView.findViewById(R.id.app_drawer_recyclerview) : null;
            final View drawerScrubber = content == null ?
                    toView.findViewById(R.id.scrubber_container) : null;

            if (page != null) {
                page.setVisibility(View.VISIBLE);
                page.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                layerViews.add(page);

                ObjectAnimator pageDrift = ObjectAnimator.ofFloat(page, "translationY", yDrift, 0);
                page.setTranslationY(yDrift);
                pageDrift.setDuration(revealDuration);
                pageDrift.setInterpolator(new LogDecelerateInterpolator(100, 0));
                pageDrift.setStartDelay(itemsAlphaStagger);
                mStateAnimation.play(pageDrift);

                page.setAlpha(0f);
                ObjectAnimator itemsAlpha = ObjectAnimator.ofFloat(page, "alpha", 0f, 1f);
                itemsAlpha.setDuration(revealDuration);
                itemsAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
                itemsAlpha.setStartDelay(itemsAlphaStagger);
                mStateAnimation.play(itemsAlpha);

                if (drawerContent != null) {
                    drawerContent.setTranslationY(toView.getHeight());
                    ObjectAnimator slideIn = ObjectAnimator.ofFloat(drawerContent,
                            "translationY", 1000, 0);
                    slideIn.setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION));
                    slideIn.setStartDelay(revealDuration / 2);
                    mStateAnimation.play(slideIn);
                }
                if (drawerScrubber != null) {
                    drawerScrubber.setAlpha(0f);
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(drawerScrubber,
                            "alpha", 0f, 1f);
                    fadeIn.setStartDelay(revealDuration / 2);
                    mStateAnimation.play(fadeIn);
                }
            }

            View pageIndicators = toView.findViewById(R.id.apps_customize_page_indicator);
            if (pageIndicators != null) {
                pageIndicators.setAlpha(0.01f);
                ObjectAnimator indicatorsAlpha =
                        ObjectAnimator.ofFloat(pageIndicators, "alpha", 1f);
                indicatorsAlpha.setDuration(revealDuration);
                mStateAnimation.play(indicatorsAlpha);
            }

            if (material) {
                final View allApps = getAllAppsButton();
                int allAppsButtonSize = LauncherAppState.getInstance().
                        getDynamicGrid().getDeviceProfile().allAppsButtonVisualSize;
                float startRadius = isWidgetTray ? 0 : allAppsButtonSize / 2;
                Animator reveal = ViewAnimationUtils.createCircularReveal(revealView, width / 2,
                        height / 2, startRadius, revealRadius);
                reveal.setDuration(revealDuration);
                reveal.setInterpolator(new LogDecelerateInterpolator(100, 0));

                reveal.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        if (!isWidgetTray) {
                            allApps.setVisibility(View.INVISIBLE);
                        }
                    }

                    public void onAnimationEnd(Animator animation) {
                        if (!isWidgetTray) {
                            allApps.setVisibility(View.VISIBLE);
                        }
                    }
                });
                mStateAnimation.play(reveal);
            }

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (drawer && mAppsCustomizeContent.getContentType()
                            == AppsCustomizePagedView.ContentType.Applications) {
                        updateStatusBarColor(res.getColor(R.color.app_drawer_drag_background));
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchOnLauncherTransitionEnd(fromView, animated, false);
                    dispatchOnLauncherTransitionEnd(toView, animated, false);

                    revealView.setVisibility(View.INVISIBLE);
                    revealView.setLayerType(View.LAYER_TYPE_NONE, null);
                    if (page != null) {
                        page.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                    if (content != null) {
                        content.setPageBackgroundsVisible(true);
                    } else {
                        toView.setBackgroundColor(res.getColor(R.color.app_drawer_background));
                    }

                    // Hide the search bar
                    if (mSearchDropTargetBar != null) {
                        mSearchDropTargetBar.hideSearchBar(false);
                    }
                }

            });

            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }

            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mStateAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mStateAnimation != stateAnimation)
                        return;
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);

                    revealView.setAlpha(initAlpha);
                    if (Utilities.isLmpOrAbove()) {
                        for (int i = 0; i < layerViews.size(); i++) {
                            View v = layerViews.get(i);
                            if (v != null) {
                                boolean attached = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    attached = v.isAttachedToWindow();
                                }
                                if (attached) v.buildLayer();
                            }
                        }
                    }
                    mStateAnimation.start();
                }
            };
            toView.bringToFront();
            toView.setVisibility(View.VISIBLE);
            toView.post(startAnimRunnable);
        } else {
            toView.setTranslationX(0.0f);
            toView.setTranslationY(0.0f);
            toView.setScaleX(1.0f);
            toView.setScaleY(1.0f);
            toView.setVisibility(View.VISIBLE);
            toView.bringToFront();
            if (drawer && mAppsCustomizeContent.getContentType()
                    == AppsCustomizePagedView.ContentType.Applications) {
                updateStatusBarColor(res.getColor(R.color.app_drawer_drag_background));
                toView.setBackgroundColor(res.getColor(R.color.app_drawer_background));
            }

            if (!springLoaded && !LauncherAppState.getInstance().isScreenLarge()) {
                // Hide the search bar
                if (mSearchDropTargetBar != null) {
                    mSearchDropTargetBar.hideSearchBar(false);
                }
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionStart(fromView, animated, false);
            dispatchOnLauncherTransitionEnd(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            dispatchOnLauncherTransitionStart(toView, animated, false);
            dispatchOnLauncherTransitionEnd(toView, animated, false);
        }
    }

    /**
     * Zoom the camera back into the workspace, hiding 'fromView'.
     * This is the opposite of showAppsCustomizeHelper.
     *
     * @param animated If true, the transition will be animated.
     */
    private void hideAppsCustomizeHelper(Workspace.State toState, final boolean animated,
                                         final boolean springLoaded, final Runnable onCompleteRunnable) {
        /** Lenovo-SW luyy1 20150910 change for primary home screen START */
        if (mDefaultHomeScreenPanel != null) {
            if (!mWorkspace.isInOverviewMode()) {
                mDefaultHomeScreenPanel.setVisibility(View.INVISIBLE);
            } else {
                mDefaultHomeScreenPanel.setVisibility(View.VISIBLE);
            }
        }
        /** Lenovo-SW luyy1 20150910 change for primary home screen END */
        if (mStateAnimation != null) {
            mStateAnimation.setDuration(0);
            mStateAnimation.cancel();
            mStateAnimation = null;
        }

        boolean material = Utilities.isLmpOrAbove();
        boolean drawer = mDrawerType == AppDrawerListAdapter.DrawerType.Drawer;
        final Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomOutTime);
        final int fadeOutDuration = res.getInteger(R.integer.config_appsCustomizeFadeOutTime);
        final int revealDuration = res.getInteger(R.integer.config_appsCustomizeConcealTime);
        final int itemsAlphaStagger =
                res.getInteger(R.integer.config_appsCustomizeItemsAlphaStagger);

        final float scaleFactor = (float)
                res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView;

        if (drawer && mAppsCustomizeContent.getContentType()
                != AppsCustomizePagedView.ContentType.Widgets) {
            fromView = (FrameLayout) findViewById(R.id.app_drawer_container);
        } else {
            fromView = mAppsCustomizeTabHost;
        }

        final View toView = mWorkspace;
        Animator workspaceAnim = null;
        final ArrayList<View> layerViews = new ArrayList<View>();

        if (toState == Workspace.State.NORMAL) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    toState, animated, layerViews);
        } else if (toState == Workspace.State.SPRING_LOADED ||
                toState == Workspace.State.OVERVIEW) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    toState, animated, layerViews);
        }

        // If for some reason our views aren't initialized, don't animate

        boolean initialized = getAllAppsButton() != null;

        if (animated && initialized) {
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }

            final AppsCustomizePagedView content = (AppsCustomizePagedView)
                    fromView.findViewById(R.id.apps_customize_pane_content);

            final View page = content != null ? content.getPageAt(content.getNextPage())
                    : fromView.findViewById(R.id.app_drawer_view);

            // We need to hide side pages of the Apps / Widget tray to avoid some ugly edge cases
            int count = content != null ? content.getChildCount() : 0;
            for (int i = 0; i < count; i++) {
                View child = content.getChildAt(i);
                if (child != page) {
                    child.setVisibility(View.INVISIBLE);
                }
            }
            final View revealView = fromView.findViewById(R.id.fake_page);

            // hideAppsCustomizeHelper is called in some cases when it is already hidden
            // don't perform all these no-op animations. In particularly, this was causing
            // the all-apps button to pop in and out.
            if (fromView.getVisibility() == View.VISIBLE) {
                AppsCustomizePagedView.ContentType contentType =
                        mAppsCustomizeContent.getContentType();
                final boolean isWidgetTray =
                        contentType == AppsCustomizePagedView.ContentType.Widgets;

                if (isWidgetTray) {
                    revealView.setBackground(res.getDrawable(R.drawable.quantum_panel_dark));
                } else {
                    if (drawer) {
                        revealView.setBackgroundColor(res.getColor(
                                R.color.app_drawer_background));
                    } else {
                        Drawable bg = res.getDrawable(R.drawable.quantum_panel);
                        if (bg != null) {
                        	// Lenovo-SW zhaoxin5 20150714 change allApps background to full white
                            // bg.setAlpha(128);
                            revealView.setBackground(bg);
                        }
                    }
                }

                int width = revealView.getMeasuredWidth();
                int height = revealView.getMeasuredHeight();
                float revealRadius = (float) Math.sqrt((width * width) / 4 + (height * height) / 4);

                // Hide the real page background, and swap in the fake one
                revealView.setVisibility(View.VISIBLE);
                if (content != null) {
                    content.setPageBackgroundsVisible(false);
                } else {
                    fromView.setBackgroundColor(Color.TRANSPARENT);
                    updateStatusBarColor(Color.TRANSPARENT);
                }

                final View allAppsButton = getAllAppsButton();
                revealView.setTranslationY(0);
                int[] allAppsToPanelDelta = Utilities.getCenterDeltaInScreenSpace(revealView,
                        allAppsButton, null);
                /** Lenovo-SW zhaoxin5 20150910 Kevin 说的Android模式下AllApps消失动画，中间原点有顿挫的感觉 START */
            	int textSize = (int) res.getDimension(R.dimen.infomation_count_textsize);
                allAppsToPanelDelta[1] = allAppsToPanelDelta[1] - textSize/4;
                /** Lenovo-SW zhaoxin5 20150910 Kevin 说的Android模式下AllApps消失动画，中间原点有顿挫的感觉 END */

                float xDrift = 0;
                float yDrift = 0;
                if (material) {
                    yDrift = isWidgetTray ? height / 2 : allAppsToPanelDelta[1];
                    xDrift = isWidgetTray ? 0 : allAppsToPanelDelta[0];
                } else {
                    yDrift = 5 * height / 4;
                    xDrift = 0;
                }

                revealView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                TimeInterpolator decelerateInterpolator = material ?
                        new LogDecelerateInterpolator(100, 0) :
                        new LogDecelerateInterpolator(30, 0);

                // The vertical motion of the apps panel should be delayed by one frame
                // from the conceal animation in order to give the right feel. We correpsondingly
                // shorten the duration so that the slide and conceal end at the same time.
                ObjectAnimator panelDriftY = LauncherAnimUtils.ofFloat(revealView, "translationY",
                        0, yDrift);
                panelDriftY.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                panelDriftY.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                panelDriftY.setInterpolator(decelerateInterpolator);
                mStateAnimation.play(panelDriftY);

                ObjectAnimator panelDriftX = LauncherAnimUtils.ofFloat(revealView, "translationX",
                        0, xDrift);
                panelDriftX.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                panelDriftX.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                panelDriftX.setInterpolator(decelerateInterpolator);
                mStateAnimation.play(panelDriftX);

                if (isWidgetTray || !material) {
                    float finalAlpha = material ? 0.4f : 0f;
                    revealView.setAlpha(1f);
                    ObjectAnimator panelAlpha = LauncherAnimUtils.ofFloat(revealView, "alpha",
                            1f, finalAlpha);
                    panelAlpha.setDuration(revealDuration);
                    panelAlpha.setInterpolator(material ? decelerateInterpolator :
                            new AccelerateInterpolator(1.5f));
                    mStateAnimation.play(panelAlpha);
                }

                final View drawerScrubber = content == null ?
                        fromView.findViewById(R.id.scrubber_container) : null;

                if (page != null) {
                    page.setLayerType(View.LAYER_TYPE_HARDWARE, null);

                    ObjectAnimator pageDrift = LauncherAnimUtils.ofFloat(page, "translationY",
                            0, yDrift);
                    page.setTranslationY(0);
                    pageDrift.setDuration(revealDuration - SINGLE_FRAME_DELAY);
                    pageDrift.setInterpolator(decelerateInterpolator);
                    pageDrift.setStartDelay(itemsAlphaStagger + SINGLE_FRAME_DELAY);
                    mStateAnimation.play(pageDrift);

                    page.setAlpha(1f);
                    ObjectAnimator itemsAlpha = LauncherAnimUtils.ofFloat(page, "alpha", 1f, 0f);
                    itemsAlpha.setDuration(100);
                    itemsAlpha.setInterpolator(decelerateInterpolator);
                    mStateAnimation.play(itemsAlpha);

                    if (drawerScrubber != null) {
                        drawerScrubber.setAlpha(1f);
                        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(drawerScrubber,
                                "alpha", 1f, 0f);
                        mStateAnimation.play(fadeOut);
                    }
                }

                View pageIndicators = fromView.findViewById(R.id.apps_customize_page_indicator);
                if (pageIndicators != null) {
                    pageIndicators.setAlpha(1f);
                    ObjectAnimator indicatorsAlpha =
                            LauncherAnimUtils.ofFloat(pageIndicators, "alpha", 0f);
                    indicatorsAlpha.setDuration(revealDuration);
                    indicatorsAlpha.setInterpolator(new DecelerateInterpolator(1.5f));
                    mStateAnimation.play(indicatorsAlpha);
                }

                width = revealView.getMeasuredWidth();

                if (material) {
                    if (!isWidgetTray) {
                        allAppsButton.setVisibility(View.INVISIBLE);
                    }
                    int allAppsButtonSize = LauncherAppState.getInstance().
                            getDynamicGrid().getDeviceProfile().allAppsButtonVisualSize;
                    float finalRadius = isWidgetTray ? 0 : allAppsButtonSize / 2;
                    Animator reveal =
                            LauncherAnimUtils.createCircularReveal(revealView, width / 2,
                                    height / 2, revealRadius, finalRadius);
                    reveal.setInterpolator(new LogDecelerateInterpolator(100, 0));
                    reveal.setDuration(revealDuration);
                    reveal.setStartDelay(itemsAlphaStagger);

                    reveal.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            revealView.setVisibility(View.INVISIBLE);
                            if (!isWidgetTray) {
                                allAppsButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    mStateAnimation.play(reveal);
                }

                dispatchOnLauncherTransitionPrepare(fromView, animated, true);
                dispatchOnLauncherTransitionPrepare(toView, animated, true);
                mAppsCustomizeContent.stopScrolling();
            }

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fromView.setVisibility(View.GONE);
                    dispatchOnLauncherTransitionEnd(fromView, animated, true);
                    dispatchOnLauncherTransitionEnd(toView, animated, true);
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }

                    revealView.setLayerType(View.LAYER_TYPE_NONE, null);
                    if (page != null) {
                        page.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                    if (content != null) {
                        content.setPageBackgroundsVisible(true);
                    } else {
                        fromView.setBackgroundColor(res.getColor(R.color.app_drawer_background));
                    }
                    // Unhide side pages
                    int count = content != null ? content.getChildCount() : 0;
                    for (int i = 0; i < count; i++) {
                        View child = content.getChildAt(i);
                        child.setVisibility(View.VISIBLE);
                    }

                    // Reset page transforms
                    if (page != null) {
                        page.setTranslationX(0);
                        page.setTranslationY(0);
                        page.setAlpha(1);
                    }
                    if (content != null) {
                        content.setCurrentPage(content.getNextPage());
                    }

                    mAppsCustomizeContent.updateCurrentPageScroll();
                }
            });

            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mStateAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mStateAnimation != stateAnimation)
                        return;
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);

                    if (Utilities.isLmpOrAbove()) {
                        for (int i = 0; i < layerViews.size(); i++) {
                            View v = layerViews.get(i);
                            if (v != null) {
                                boolean attached = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    attached = v.isAttachedToWindow();
                                }
                                if (attached) v.buildLayer();
                            }
                        }
                    }
                    mStateAnimation.start();
                }
            };
            fromView.post(startAnimRunnable);
        } else {
            fromView.setVisibility(View.GONE);
            if (drawer && mAppsCustomizeContent.getContentType()
                    == AppsCustomizePagedView.ContentType.Applications) {
                fromView.setBackgroundColor(Color.TRANSPARENT);
                updateStatusBarColor(Color.TRANSPARENT, 0);
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionEnd(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            dispatchOnLauncherTransitionEnd(toView, animated, true);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            mAppsCustomizeTabHost.onTrimMemory();
        }
    }

    /** Lenovo-SW zhaoxin5 20150805 XTHREEROW-585 START */
    /*private void changeDragLayerChildsPosition(boolean showWorkspace) {
		if (showWorkspace) {
			mDragLayer.bringChildToFront(mWorkspace);
			mWorkspace.bringToFront();
			mDragLayer.bringChildToFront(mHotseat);
			mHotseat.bringToFront();
		} else {
			mDragLayer.bringChildToFront(mDockView);
			mDockView.bringToFront();
			mDragLayer.bringChildToFront(mOverviewPanel);
			mOverviewPanel.bringToFront();
		}
    }*/
    
    private void bringChildOfDragLayerToFront(String from, View... views) {
        LauncherLog.i(TAG, "bringChildOfDragLayerToFront call from : " + from);
        if (mDragLayer != null) {
            for (int i = 0; i < views.length; i++) {
                View view = views[i];
                mDragLayer.bringChildToFront(view);
                if(view != null) { //NullPointer 判断
                    view.bringToFront();
                }
            }
        }
    }
    /** Lenovo-SW zhaoxin5 20150805 XTHREEROW-585 END */
    
    protected void showWorkspace(boolean animated) {
        showWorkspace(animated, null);
    }

    protected void showWorkspace() {
        showWorkspace(true);
    }

    void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
        if (mState != State.WORKSPACE || mWorkspace.getState() != Workspace.State.NORMAL) {
            boolean wasInSpringLoadedMode = (mState != State.WORKSPACE);
            mWorkspace.setVisibility(View.VISIBLE);
            hideAppsCustomizeHelper(Workspace.State.NORMAL, animated, false, onCompleteRunnable);

            // Show the search bar (only animate if we were showing the drop target bar in spring
            // loaded mode)
            if (mSearchDropTargetBar != null) {
                mSearchDropTargetBar.showSearchBar(animated && wasInSpringLoadedMode);
            }

            // Set focus to the AppsCustomize button
            if (mAllAppsButton != null) {
                mAllAppsButton.requestFocus();
            }
        }

        // Change the state *after* we've called all the transition code
        mState = State.WORKSPACE;
        setWorkspaceBackground(mState == State.WORKSPACE);

        // Resume the auto-advance of widgets
        mUserPresent = true;
        updateRunning();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        onWorkspaceShown(animated, false); // Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
    }

    void showOverviewMode(boolean animated, boolean backFromWiget) { // Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
        mWorkspace.setVisibility(View.VISIBLE);
        hideAppsCustomizeHelper(Workspace.State.OVERVIEW, animated, false, null);
        mState = State.WORKSPACE;
        onWorkspaceShown(animated, backFromWiget); // Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
    }

    public void onWorkspaceShown(boolean animated, boolean backFromWiget) {
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
    	boolean showOverview = mWorkspace.isInOverviewMode();
    	if(showOverview) {
    		if(LauncherAppState.getInstance().isCurrentVibeuiMode()) {
                /** Lenovo-SW luyy1 20150910 change for primary home screen START */
                bringChildOfDragLayerToFront("onWorkspaceShown showOverview isCurrentVibeuiMode", mWorkspace, mOverviewPanel, mDefaultHomeScreenPanel);
                /** Lenovo-SW luyy1 20150910 change for primary home screen END */
        	} else {
        		// Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
        		if (!backFromWiget) {
                    /** Lenovo-SW luyy1 20150910 change for primary home screen START */
                    bringChildOfDragLayerToFront("onWorkspaceShown showOverview !isCurrentVibeuiMode", mWorkspace, mOverviewPanelAndroid, mDefaultHomeScreenPanel);
                    /** Lenovo-SW luyy1 20150910 change for primary home screen END */
        		}
        		// Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
        	}
    	} else {
    		bringChildOfDragLayerToFront("onWorkspaceShown !showOverview", mWorkspace, mHotseat); 
    	}
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
        /** Lenovo-SW luyy1 20150910 change for primary home screen START */
        if (mDefaultHomeScreenPanel != null) {
            if (!mWorkspace.isInOverviewMode()) {
                mDefaultHomeScreenPanel.setVisibility(View.INVISIBLE);
            } else {
                mDefaultHomeScreenPanel.setVisibility(View.VISIBLE);
            }
        }
        /** Lenovo-SW luyy1 20150910 change for primary home screen END */
    }

    void showAllApps(boolean animated, AppsCustomizePagedView.ContentType contentType,
                     boolean resetPageToZero) {
        if (mState != State.WORKSPACE) return;

        if (resetPageToZero) {
            mAppsCustomizeTabHost.reset();
        }
        if (mAppsCustomizeContent.getSortMode() != AppsCustomizePagedView.SortMode.Title) {
            // optimize Title sort by not reinflating views every time we open the app drawer
            // since we already sort based on new app installs and change of sort mode
            mAppsCustomizeContent.sortApps();
        }
        showAppsCustomizeHelper(animated, false, contentType);
        mAppsCustomizeTabHost.post(new Runnable() {
            @Override
            public void run() {
                // We post this in-case the all apps view isn't yet constructed.
                mAppsCustomizeTabHost.requestFocus();
            }
        });

        // Change the state *after* we've called all the transition code
        mState = State.APPS_CUSTOMIZE;

        // Pause the auto-advance of widgets until we are out of AllApps
        mUserPresent = false;
        updateRunning();
        closeFolder();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    /**
     * Lenovo-SW 20150608 zhaoxin5 add for WidgetList START
     */
    public void enterSpringLoadedDragMode(boolean isFromWidgetList) {
        if (isAllAppsVisible() || isFromWidgetList) {
            hideAppsCustomizeHelper(Workspace.State.SPRING_LOADED, true, true, null);
            mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
        }
    }

    public void enterSpringLoadedDragMode() {
        enterSpringLoadedDragMode(false);
    }

    /**
     * Lenovo-SW 20150608 zhaoxin5 add for WidgetList END
     */

    void exitSpringLoadedDragModeDelayed(final boolean successfulDrop, int delay,
                                         final Runnable onCompleteRunnable) {
        if (mState != State.APPS_CUSTOMIZE_SPRING_LOADED) return;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (successfulDrop) {
                    // Before we show workspace, hide all apps again because
                    // exitSpringLoadedDragMode made it visible. This is a bit hacky; we should
                    // clean up our state transition functions
                    mAppsCustomizeTabHost.setVisibility(View.GONE);
                    showWorkspace(true, onCompleteRunnable);
                } else {
                    exitSpringLoadedDragMode();
                }
            }
        }, delay);
    }

    void exitSpringLoadedDragMode() {
        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            final boolean animated = true;
            final boolean springLoaded = true;
            showAppsCustomizeHelper(animated, springLoaded);
            mState = State.APPS_CUSTOMIZE;
        }
        // Otherwise, we are not in spring loaded mode, so don't do anything.
    }

    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    /**
     * Hides the hotseat area.
     */
    void hideHotseat(boolean animated) {
        if (!LauncherAppState.getInstance().isScreenLarge()) {
            if (animated) {
                if (mHotseat.getAlpha() != 0f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionOutDuration();
                    }
                    mHotseat.animate().alpha(0f).setDuration(duration);
                }
            } else {
                mHotseat.setAlpha(0f);
            }
        }
    }

    /**
     * Add an item from all apps or customize onto the given workspace screen.
     * If layout is null, add to the current screen.
     */
    void addExternalItemToScreen(ItemInfo itemInfo, final CellLayout layout) {
        if (!mWorkspace.addExternalItemToScreen(itemInfo, layout)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
        }
    }

    /**
     * Maps the current orientation to an index for referencing orientation correct global icons
     */
    private int getCurrentOrientationIndexForGlobalIcons() {
        // default - 0, landscape - 1
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return 1;
            default:
                return 0;
        }
    }

    private Drawable getExternalPackageToolbarIcon(ComponentName activityName, String resourceName) {
        try {
            PackageManager packageManager = getPackageManager();
            // Look for the toolbar icon specified in the activity meta-data
            Bundle metaData = packageManager.getActivityInfo(
                    activityName, PackageManager.GET_META_DATA).metaData;
            if (metaData != null) {
                int iconResId = metaData.getInt(resourceName);
                if (iconResId != 0) {
                    Resources res = packageManager.getResourcesForActivity(activityName);
                    return res.getDrawable(iconResId);
                }
            }
        } catch (NameNotFoundException e) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon; " + activityName.flattenToShortString() +
                    " not found", e);
        } catch (Resources.NotFoundException nfe) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon from " + activityName.flattenToShortString(),
                    nfe);
        }
        return null;
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateTextButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);
        Resources r = getResources();
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);

        TextView button = (TextView) findViewById(buttonId);
        // If we were unable to find the icon via the meta-data, use a generic one
        if (toolbarIcon == null) {
            toolbarIcon = r.getDrawable(fallbackDrawableId);
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return null;
        } else {
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return toolbarIcon.getConstantState();
        }
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        ImageView button = (ImageView) findViewById(buttonId);
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);

        if (button != null) {
            // If we were unable to find the icon via the meta-data, use a
            // generic one
            if (toolbarIcon == null) {
                button.setImageResource(fallbackDrawableId);
            } else {
                button.setImageDrawable(toolbarIcon);
            }
        }

        return toolbarIcon != null ? toolbarIcon.getConstantState() : null;

    }

    private void updateTextButtonWithDrawable(int buttonId, Drawable d) {
        TextView button = (TextView) findViewById(buttonId);
        button.setCompoundDrawables(d, null, null, null);
    }

    private void updateButtonWithDrawable(int buttonId, Drawable.ConstantState d) {
        ImageView button = (ImageView) findViewById(buttonId);
        button.setImageDrawable(d.newDrawable(getResources()));
    }

    private void invalidatePressedFocusedStates(View container, View button) {
        if (container instanceof HolographicLinearLayout) {
            HolographicLinearLayout layout = (HolographicLinearLayout) container;
            layout.invalidatePressedFocusedStates();
        } else if (button instanceof HolographicImageView) {
            HolographicImageView view = (HolographicImageView) button;
            view.invalidatePressedFocusedStates();
        }
    }

    public View getQsbBar() {
        if (mQsb == null) {
            mQsb = mInflater.inflate(R.layout.qsb, mSearchDropTargetBar, false);
            mSearchDropTargetBar.addView(mQsb);
        }
        return mQsb;
    }

    protected boolean updateGlobalSearchIcon() {
        final View searchButtonContainer = findViewById(R.id.search_button_container);
        final ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);

        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName activityName = searchManager.getGlobalSearchActivity();
        if (activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                    R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo,
                    TOOLBAR_SEARCH_ICON_METADATA_NAME);
            if (sGlobalSearchIcon[coi] == null) {
                sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                        R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo,
                        TOOLBAR_ICON_METADATA_NAME);
            }

            if (searchButtonContainer != null) searchButtonContainer.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            invalidatePressedFocusedStates(searchButtonContainer, searchButton);
            return true;
        } else {
            // We disable both search and voice search when there is no global search provider
            if (searchButtonContainer != null) searchButtonContainer.setVisibility(View.GONE);
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.GONE);
            if (searchButton != null) searchButton.setVisibility(View.GONE);
            if (voiceButton != null) voiceButton.setVisibility(View.GONE);
            updateVoiceButtonProxyVisible(true);
            return false;
        }
    }

    protected void updateGlobalSearchIcon(Drawable.ConstantState d) {
        final View searchButtonContainer = findViewById(R.id.search_button_container);
        final View searchButton = (ImageView) findViewById(R.id.search_button);
        updateButtonWithDrawable(R.id.search_button, d);
        invalidatePressedFocusedStates(searchButtonContainer, searchButton);
    }

    protected boolean updateVoiceSearchIcon(boolean searchVisible) {
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);

        // We only show/update the voice search icon if the search icon is enabled as well
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();

        ComponentName activityName = null;
        if (globalSearchActivity != null) {
            // Check if the global search activity handles voice search
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setPackage(globalSearchActivity.getPackageName());
            activityName = intent.resolveActivity(getPackageManager());
        }

        if (activityName == null) {
            // Fallback: check if an activity other than the global search activity
            // resolves this
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            activityName = intent.resolveActivity(getPackageManager());
        }
        if (searchVisible && activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                    R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo,
                    TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME);
            if (sVoiceSearchIcon[coi] == null) {
                sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                        R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo,
                        TOOLBAR_ICON_METADATA_NAME);
            }
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.VISIBLE);
            voiceButton.setVisibility(View.VISIBLE);
            updateVoiceButtonProxyVisible(false);
            invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
            return true;
        } else {
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.GONE);
            if (voiceButton != null) voiceButton.setVisibility(View.GONE);
            updateVoiceButtonProxyVisible(true);
            return false;
        }
    }

    protected void updateVoiceSearchIcon(Drawable.ConstantState d) {
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        updateButtonWithDrawable(R.id.voice_button, d);
        invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
    }

    public void updateVoiceButtonProxyVisible(boolean forceDisableVoiceButtonProxy) {
        final View voiceButtonProxy = findViewById(R.id.voice_button_proxy);
        if (voiceButtonProxy != null) {
            boolean visible = !forceDisableVoiceButtonProxy &&
                    mWorkspace.shouldVoiceButtonProxyBeVisible();
            voiceButtonProxy.setVisibility(visible ? View.VISIBLE : View.GONE);
            voiceButtonProxy.bringToFront();
        }
    }

    /**
     * This is an overrid eot disable the voice button proxy.  If disabled is true, then the voice button proxy
     * will be hidden regardless of what shouldVoiceButtonProxyBeVisible() returns.
     */
    public void disableVoiceButtonProxy(boolean disabled) {
        updateVoiceButtonProxyVisible(disabled);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final boolean result = super.dispatchPopulateAccessibilityEvent(event);
        final List<CharSequence> text = event.getText();
        text.clear();
        // Populate event with a fake title based on the current state.
        if (mState == State.APPS_CUSTOMIZE) {
            text.add(mAppsCustomizeTabHost.getContentTag());
        } else {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return result;
    }

    /**
     * Receives notifications when system dialogs are to be closed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    /**
     * If the activity is currently paused, signal that we need to run the passed Runnable
     * in onResume.
     * <p/>
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */
    private boolean waitUntilResume(Runnable run, boolean deletePreviousRunnables) {
        if (mPaused) {
            Log.i(TAG, "Deferring update until onResume");
            if (deletePreviousRunnables) {
                while (mBindOnResumeCallbacks.remove(run)) {
                }
            }
            mBindOnResumeCallbacks.add(run);
            return true;
        } else {
            return false;
        }
    }

    private boolean waitUntilResume(Runnable run) {
        return waitUntilResume(run, false);
    }

    public void addOnResumeCallback(Runnable run) {
        mOnResumeCallbacks.add(run);
    }

    /**
     * If the activity is currently paused, signal that we need to re-run the loader
     * in onResume.
     * <p/>
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */

    /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success START */
    public boolean setLoadOnResume(int flags) {
        if (mPaused) {
            Log.i(TAG, "setLoadOnResume");
            mOnResumeNeedsLoad = true;
            mOnResumeLoaderTaskFlags = flags;
            return true;
        } else {
            return false;
        }
    }
    /*public boolean setLoadOnResume() {
        if (mPaused) {
            Log.i(TAG, "setLoadOnResume");
            mOnResumeNeedsLoad = true;
            //mOnResumeLoaderTaskFlags = flags;
            return true;
        } else {
            return false;
        }
    }*/
    /** Lenovo-SW zhaoxin5 20150810 fix bug, restore can not success END */
    
    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentPage();
        } else {
            return SCREEN_COUNT / 2;
        }
    }
    /*Lenovo-sw zhangyj19 add 2015/07/29 modify theme start*/
    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void loadResource() {
       //ThemeResourceUtils.loadThemeResource(this, ThemeController.DEFAULT_APK_THEME);
        ThemeResourceUtils.loadThemeResource(this);
    }
    /*Lenovo-sw zhangyj19 add 2015/07/29 modify theme start*/
    /**
     * Refreshes the shortcuts shown on the workspace.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        setWorkspaceLoading(true);

        // If we're starting binding all over again, clear any bind calls we'd postponed in
        // the past (see waitUntilResume) -- we don't need them since we're starting binding
        // from scratch again
        mBindOnResumeCallbacks.clear();

        // Clear the workspace because it's going to be rebound
        mWorkspace.clearDropTargets();
        mWorkspace.removeAllWorkspaceScreens();

        mWidgetsToAdvance.clear();
        if (mHotseat != null) {
            // added by xutg1, reset the Hotseat's grid size
            mHotseat.resetGridSize();
            // add end, xutg1, reset the Hotseat's grid size
            mHotseat.resetLayout();
        }
    }

    @Override
    public void bindScreens(ArrayList<Long> orderedScreenIds) {
        //bind add screen
        bindAddScreens(orderedScreenIds);

        // If there are no screens, we need to have an empty screen
        if (orderedScreenIds.size() == 0) {
            mWorkspace.addExtraEmptyScreen();
        }

        // Create the custom content page (this call updates mDefaultScreen which calls
        // setCurrentPage() so ensure that all pages are added before calling this).
        if (hasCustomContentToLeft()) {
            mWorkspace.createCustomContentContainer();
            populateCustomContentContainer();
            /*if(mWorkspace.mCurrentPage == 1){
                setFullScreen(true);
            }*/

        }
    }

    @Override
    public void bindAddScreens(ArrayList<Long> orderedScreenIds) {
        // Log to disk
        Launcher.addDumpLog(TAG, "11683562 - bindAddScreens()", true);
        Launcher.addDumpLog(TAG, "11683562 -   orderedScreenIds: " +
                TextUtils.join(", ", orderedScreenIds), true);
        int count = orderedScreenIds.size();
        for (int i = 0; i < count; i++) {
            mWorkspace.insertNewWorkspaceScreenBeforeEmptyScreen(orderedScreenIds.get(i));
        }
    }

    private boolean shouldShowWeightWatcher() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getSharedPreferences(spKey, Context.MODE_PRIVATE);
        boolean show = sp.getBoolean(SHOW_WEIGHT_WATCHER, SHOW_WEIGHT_WATCHER_DEFAULT);

        return show;
    }

    private void toggleShowWeightWatcher() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getSharedPreferences(spKey, Context.MODE_PRIVATE);
        boolean show = sp.getBoolean(SHOW_WEIGHT_WATCHER, true);

        show = !show;

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SHOW_WEIGHT_WATCHER, show);
        editor.commit();

        if (mWeightWatcher != null) {
            mWeightWatcher.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void bindAppsAdded(final ArrayList<Long> newScreens,
                              final ArrayList<ItemInfo> addNotAnimated,
                              final ArrayList<ItemInfo> addAnimated,
                              final ArrayList<AppInfo> addedApps) {
        Runnable r = new Runnable() {
            public void run() {
                bindAppsAdded(newScreens, addNotAnimated, addAnimated, addedApps);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        // Add the new screens
        if (newScreens != null) {
            bindAddScreens(newScreens);
        }

        // We add the items without animation on non-visible pages, and with
        // animations on the new page (which we will try and snap to).
        if (addNotAnimated != null && !addNotAnimated.isEmpty()) {
            bindItems(addNotAnimated, 0,
                    addNotAnimated.size(), false);
        }
        if (addAnimated != null && !addAnimated.isEmpty()) {
            bindItems(addAnimated, 0,
                    addAnimated.size(), true);
        }

        // Remove the extra empty screen
        mWorkspace.removeExtraEmptyScreen(false, false);

        if (!LauncherAppState.isDisableAllApps() &&
                addedApps != null && mAppsCustomizeContent != null) {
            mAppsCustomizeContent.addApps(addedApps);
            mAppDrawerAdapter.addApps(addedApps);
        }
        
        //add by zhanggx1 for removing on 2013-11-13 . s
        autoReorder("bindAppsAdded");
        //add by zhanggx1 for removing on 2013-11-13 . e
    }

    /**
     * Bind the items start-end from the list.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end,
                          final boolean forceAnimateIcons) {
        Runnable r = new Runnable() {
            public void run() {
                bindItems(shortcuts, start, end, forceAnimateIcons);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        // Get the list of added shortcuts and intersect them with the set of shortcuts here
        final AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        final Collection<Animator> bounceAnims = new ArrayList<Animator>();
        final boolean animateIcons = forceAnimateIcons && canRunNewAppsAnimation();
        Workspace workspace = mWorkspace;
        long newShortcutsScreenId = -1;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);

            // Short circuit if we are loading dock items for a configuration which has no dock
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                    mHotseat == null) {
                continue;
            }

            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    View shortcut = createShortcut(info);

                    /*
                     * TODO: FIX collision case
                     */
                    if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                        CellLayout cl = mWorkspace.getScreenWithId(item.screenId);
                        if (cl != null && cl.isOccupied(item.cellX, item.cellY)) {
                            View v = cl.getChildAt(item.cellX, item.cellY);
                            Object tag = v.getTag();
                            String desc = "Collision while binding workspace item: " + item
                                    + ". Collides with " + tag;
                            if (LauncherAppState.isDogfoodBuild()) {
                                throw (new RuntimeException(desc));
                            } else {
                                Log.d(TAG, desc);
                            }
                        }
                    }
                    //item 显示在桌面上
                    workspace.addInScreenFromBind(shortcut, item.container, item.screenId, item.cellX,
                            item.cellY, 1, 1);
                    if (animateIcons) {
                        // Animate all the applications up now
                        shortcut.setAlpha(0f);
                        shortcut.setScaleX(0f);
                        shortcut.setScaleY(0f);
                        bounceAnims.add(createNewAppBounceAnimation(shortcut, i));
                        newShortcutsScreenId = item.screenId;
                    }
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                    FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()),
                            (FolderInfo) item, mIconCache);
                    newFolder.setTextVisible(!mHideIconLabels);
                    workspace.addInScreenFromBind(newFolder, item.container, item.screenId, item.cellX,
                            item.cellY, 1, 1);
                    break;
                default:
                    throw new RuntimeException("Invalid Item Type");
            }
        }

        if (animateIcons) {
            // Animate to the correct page
            if (newShortcutsScreenId > -1) {
                long currentScreenId = mWorkspace.getScreenIdForPageIndex(mWorkspace.getNextPage());
                final int newScreenIndex = mWorkspace.getPageIndexForScreenId(newShortcutsScreenId);
                final Runnable startBounceAnimRunnable = new Runnable() {
                    public void run() {
                        anim.playTogether(bounceAnims);
                        anim.start();
                    }
                };
                if (newShortcutsScreenId != currentScreenId) {
                    // We post the animation slightly delayed to prevent slowdowns
                    // when we are loading right after we return to launcher.
                    mWorkspace.postDelayed(new Runnable() {
                        public void run() {
                            if (mWorkspace != null) {
                                mWorkspace.snapToPage(newScreenIndex);
                                mWorkspace.postDelayed(startBounceAnimRunnable,
                                        NEW_APPS_ANIMATION_DELAY);
                            }
                        }
                    }, NEW_APPS_PAGE_MOVE_DELAY);
                } else {
                    mWorkspace.postDelayed(startBounceAnimRunnable, NEW_APPS_ANIMATION_DELAY);
                }
            }
        }
        workspace.requestLayout();
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(final HashMap<Long, FolderInfo> folders) {
        Runnable r = new Runnable() {
            public void run() {
                bindFolders(folders);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        sFolders.clear();
        sFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @SuppressWarnings("static-access")
    public void bindAppWidget(final LauncherAppWidgetInfo item) {
        Runnable r = new Runnable() {
            public void run() {
                bindAppWidget(item);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        final Workspace workspace = mWorkspace;
        if (item.providerName.toString().contains(LauncherModel.KLAUNCHER_WIDGET_CLOCK_CLASS) ) {
            LayoutInflater flater = LayoutInflater.from(this);
            ClockWidgetView view = new ClockWidgetView(this);
            view.setTag(item);
//            RelativeLayout clockLayout = (RelativeLayout) view.findViewById(R.id.clock_layout);
//            clockLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    PackageManager packageManager = getPackageManager();
//                    if (packageManager != null) {
//                        Intent AlarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(
//                                Intent.CATEGORY_LAUNCHER).setComponent(
//                                new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClock"));
//                        ResolveInfo resolved = packageManager.resolveActivity(AlarmClockIntent,
//                                PackageManager.MATCH_DEFAULT_ONLY);
//                        if (resolved != null) {
//                            startActivity(AlarmClockIntent);
//                        }
//                    }
//                }
//            });
//            clockLayout.setOnLongClickListener(new OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    return false;
//                }
//            });

//            RelativeLayout weatherLayout = (RelativeLayout) view.findViewById(R.id.weather_layout);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PackageManager packageManager = getPackageManager();
                    if (packageManager != null) {
                        Intent intent = null;
                        intent = packageManager.getLaunchIntentForPackage("com.moji.mjweather");
                        if (intent != null && intent.getComponent() != null) {
                            startActivity(intent);
                        }
                    }
                }
            });

            workspace.addInScreen(view, item.container, item.screenId, item.cellX,
                    item.cellY, item.spanX, item.spanY, false);
//            addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

            workspace.requestLayout();
            return;
        }else if( item.providerName.toString().contains(LauncherModel.KLAUNCHER_WIDGET_SEARCH_CLASS)){
            final SearchWidgetView view = new SearchWidgetView(this);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    int searchType = 0;//搜索 类型 1 搜狗搜索 2  百度搜索
                    if (view.isAppInstalled(Launcher.this, "com.sogou.activity.src")) {
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.setClassName("com.sogou.activity.src", "com.sogou.activity.src.SplashActivity");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        searchType = 1;
                    } else if (!view.isAppInstalled(Launcher.this, "com.sogou.activity.src") && view.isAppInstalled(Launcher.this, "com.baidu.searchbox")) {
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.setClassName("com.baidu.searchbox", "com.baidu.searchbox.SplashActivity");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        searchType = 2;
                    } else {
                        intent = new Intent(Launcher.this, SearchActivity.class);
                    }
                    if (searchType == 1) {
                        PingManager.getInstance().reportUserAction4App(
                                PingManager.KLAUNCHER_WIDGET_SOUGOU_SEARCH, Launcher.this.getPackageName());
                        LogUtil.d(TAG, "PingManager SOUGOU ");
                    } else if (searchType == 2) {
                        PingManager.getInstance().reportUserAction4App(
                                PingManager.KLAUNCHER_WIDGET_BAIDU_SEARCH, Launcher.this.getPackageName());
                        LogUtil.d(TAG, "PingManager BAIDU ");
                    }
                    startActivity(intent);
                }
            });
            mMySearchWidgetScreenId = (int) item.screenId -1;
            view.setTag(item);
            workspace.addInScreen(view, item.container, item.screenId, item.cellX,
                    item.cellY, item.spanX, item.spanY, false);
//            addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

            workspace.requestLayout();
            return;
        }

        AppWidgetProviderInfo appWidgetInfo;
        if (((item.restoreStatus & LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY) == 0) &&
                ((item.restoreStatus & LauncherAppWidgetInfo.FLAG_ID_NOT_VALID) != 0)) {

            appWidgetInfo = mModel.findAppWidgetProviderInfoWithComponent(this, item.providerName);
            if (appWidgetInfo == null) {
                if (DEBUG_WIDGETS) {
                    Log.d(TAG, "Removing restored widget: id=" + item.appWidgetId
                            + " belongs to component " + item.providerName
                            + ", as the povider is null");
                }
                LauncherModel.deleteItemFromDatabase(this, item);
                return;
            }
            // Note: This assumes that the id remap broadcast is received before this step.
            // If that is not the case, the id remap will be ignored and user may see the
            // click to setup view.
            PendingAddWidgetInfo pendingInfo = new PendingAddWidgetInfo(appWidgetInfo, null, null);
            pendingInfo.spanX = item.spanX;
            pendingInfo.spanY = item.spanY;
            pendingInfo.minSpanX = item.minSpanX;
            pendingInfo.minSpanY = item.minSpanY;
            Bundle options =
                    AppsCustomizePagedView.getDefaultOptionsForWidget(this, pendingInfo);

            int newWidgetId = mAppWidgetHost.allocateAppWidgetId();
            boolean success = mAppWidgetManager.bindAppWidgetIdIfAllowed(
                    newWidgetId, appWidgetInfo, options);

            // TODO consider showing a permission dialog when the widget is clicked.
//            if (!success) {
//                mAppWidgetHost.deleteAppWidgetId(newWidgetId);
//                if (DEBUG_WIDGETS) {
//                    Log.d(TAG, "Removing restored widget: id=" + item.appWidgetId
//                            + " belongs to component " + item.providerName
//                            + ", as the launcher is unable to bing a new widget id");
//                }
//                LauncherModel.deleteItemFromDatabase(this, item);
//                return;
//            }

            item.appWidgetId = newWidgetId;

            // If the widget has a configure activity, it is still needs to set it up, otherwise
            // the widget is ready to go.
            item.restoreStatus = (appWidgetInfo.configure == null)
                    ? LauncherAppWidgetInfo.RESTORE_COMPLETED
                    : LauncherAppWidgetInfo.FLAG_UI_NOT_READY;

            LauncherModel.updateItemInDatabase(this, item);
        }

        if (item.restoreStatus == LauncherAppWidgetInfo.RESTORE_COMPLETED) {
            final int appWidgetId = item.appWidgetId;
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            if (DEBUG_WIDGETS) {
                Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
            }

            item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        } else {
            appWidgetInfo = null;
            PendingAppWidgetHostView view = new PendingAppWidgetHostView(this, item);
            view.updateIcon(mIconCache);
            item.hostView = view;
            item.hostView.updateAppWidget(null);
            item.hostView.setOnClickListener(this);
        }

        item.hostView.setTag(item);
        item.onBindAppWidget(this);

        workspace.addInScreen(item.hostView, item.container, item.screenId, item.cellX,
                item.cellY, item.spanX, item.spanY, false);
        addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

        workspace.requestLayout();

        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id=" + item.appWidgetId + " in "
                    + (SystemClock.uptimeMillis() - start) + "ms");
        }
    }

    public void setMySearchWidgetScreenId(int id) {
        mMySearchWidgetScreenId = id;
    }

    /**
     * Restores a pending widget.
     *
     * @param appWidgetId The app widget id
     */
    private void completeRestoreAppWidget(final int appWidgetId) {
        LauncherAppWidgetHostView view = mWorkspace.getWidgetForAppWidgetId(appWidgetId);
        if ((view == null) || !(view instanceof PendingAppWidgetHostView)) {
            Log.e(TAG, "Widget update called, when the widget no longer exists.");
            return;
        }

        LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) view.getTag();
        info.restoreStatus = LauncherAppWidgetInfo.RESTORE_COMPLETED;

        mWorkspace.reinflateWidgetsIfNecessary();
        LauncherModel.updateItemInDatabase(this, info);
    }

    public void onPageBoundSynchronously(int page) {
        mSynchronouslyBoundPages.add(page);
    }

    /**
     * Callback saying that there aren't any more items to bind.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems(final boolean upgradePath) {
        Runnable r = new Runnable() {
            public void run() {
                finishBindingItems(upgradePath);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentPage()).requestFocus();
            }
            mSavedState = null;
        }

        mWorkspace.restoreInstanceStateForRemainingPages();

        setWorkspaceLoading(false);
        sendLoadingCompleteBroadcastIfNecessary();

        // If we received the result of any pending adds while the loader was running (e.g. the
        // widget configuration forced an orientation change), process them now.
        if (sPendingAddItem != null) {
            final long screenId = completeAdd(sPendingAddItem);

            // TODO: this moves the user to the page where the pending item was added. Ideally,
            // the screen would be guaranteed to exist after bind, and the page would be set through
            // the workspace restore process.
            mWorkspace.post(new Runnable() {
                @Override
                public void run() {
                    mWorkspace.snapToScreenId(screenId);
                }
            });
            sPendingAddItem = null;
        }

        if (upgradePath) {
            mWorkspace.getUniqueComponents(true, null);
            mIntentsOnWorkspaceFromUpgradePath = mWorkspace.getUniqueComponents(true, null);
        }
        PackageInstallerCompat.getInstance(this).onFinishBind();
        mModel.recheckRestoredItems(this);
        /*Lenovo-sw zhangyj19 add 2015/09/16 keep empty screens start*/
        //mWorkspace.stripEmptyScreens();
        /*Lenovo-sw zhangyj19 add 2015/09/16 keep empty screens end*/
        /* Lenovo-SW zhaoxin5 20150520 add for 2 Layer support START */
        // ModeSwitchHelper.onLauncherFinishBindingItems(this);
        /* Lenovo-SW zhaoxin5 20150520 add for 2 Layer support END */
        Csc.init(Launcher.this);
        isLoadUUsdkFolderAd();
    }


    private void isLoadUUsdkFolderAd(){
        if (Folder.isShowRecommendApp(this)) {
            List<String> words = new ArrayList<String>();
            Iterator<Map.Entry<Long, FolderInfo>> iterator = mModel.getsBgFolders().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, FolderInfo> entry = iterator.next();
                FolderInfo fi = entry.getValue();
                words.add((String) fi.title);
            }
            Launch.cfgKeys(this, words);
        }
    }

    private void sendLoadingCompleteBroadcastIfNecessary() {
        if (!mSharedPrefs.getBoolean(FIRST_LOAD_COMPLETE, false)) {
            String permission =
                    getResources().getString(R.string.receive_first_load_broadcast_permission);
            Intent intent = new Intent(ACTION_FIRST_LOAD_COMPLETE);
            sendBroadcast(intent, permission);
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putBoolean(FIRST_LOAD_COMPLETE, true);
            editor.apply();
        }
    }

    public boolean isAllAppsButtonRank(int rank) {
        if (mHotseat != null) {
            return mHotseat.isAllAppsButtonRank(rank);
        }
        return false;
    }

    private boolean canRunNewAppsAnimation() {
        long diff = System.currentTimeMillis() - mDragController.getLastGestureUpTime();
        return diff > (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
    }

    private ValueAnimator createNewAppBounceAnimation(View v, int i) {
        ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat("alpha", 1f),
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));
        bounceAnim.setDuration(InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION);
        bounceAnim.setStartDelay(i * InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY);
        bounceAnim.setInterpolator(new SmoothPagedView.OvershootInterpolator());
        return bounceAnim;
    }

    public boolean useVerticalBarLayout() {
        return LauncherAppState.getInstance().getDynamicGrid().
                getDeviceProfile().isVerticalBarLayout();
    }

    protected Rect getSearchBarBounds() {
        return LauncherAppState.getInstance().getDynamicGrid().
                getDeviceProfile().getSearchBarBounds();
    }

    @Override
    public void bindSearchablesChanged() {
        boolean searchVisible = updateGlobalSearchIcon();
        boolean voiceVisible = updateVoiceSearchIcon(searchVisible);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }

    /**
     * Add the icons for all apps.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAllApplications(final ArrayList<AppInfo> apps) {
    	/** Lenovo-SW zhaoxin5 20150909 Launcher 启动加速 START */
        if (LauncherAppState.isDisableAllApps()) {
        	// Vibeui 模式
            if (mIntentsOnWorkspaceFromUpgradePath != null) {
                if (LauncherModel.UPGRADE_USE_MORE_APPS_FOLDER) {
                    getHotseat().addAllAppsFolder(mIconCache, apps,
                            mIntentsOnWorkspaceFromUpgradePath, Launcher.this, mWorkspace);
                }
                mIntentsOnWorkspaceFromUpgradePath = null;
            }
            
            if (mWidgetListPanel != null) {
                mWidgetListPanel.setApps(apps);
                mWidgetListPanel.onPackagesUpdated(LauncherModel.getSortedWidgetsAndShortcuts(this));
            }
            
            /*if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.onPackagesUpdated(
                        LauncherModel.getSortedWidgetsAndShortcuts(this));
                
                 Lenovo-SW zhaoxin5 20150601 add for custom widget list START 
                if (mWidgetListPanel != null) {
                    mWidgetListPanel.setApps(apps);
                    mWidgetListPanel.onPackagesUpdated(LauncherModel.getSortedWidgetsAndShortcuts(this));
                }

                if (mQuickShareGridView != null) {
                    mQuickShareGridView.setAppInfo(apps);
                }
                 Lenovo-SW zhaoxin5 20150601 add for custom widget list END 
            }*/
        } else {
        	// Android 模式
            if (mAppDrawerAdapter != null) {
                mAppDrawerAdapter.setApps(apps);
            }
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setApps(apps);
                mAppsCustomizeContent.onPackagesUpdated(
                        LauncherModel.getSortedWidgetsAndShortcuts(this));
                
                /* Lenovo-SW zhaoxin5 20150601 add for custom widget list START */
                /*if (mWidgetListPanel != null) {
                    mWidgetListPanel.setApps(apps);
                    mWidgetListPanel.onPackagesUpdated(LauncherModel.getSortedWidgetsAndShortcuts(this));
                }

                if (mQuickShareGridView != null) {
                    mQuickShareGridView.setAppInfo(apps);
                }*/
                /* Lenovo-SW zhaoxin5 20150601 add for custom widget list END */
            }
        }
    	/** Lenovo-SW zhaoxin5 20150909 Launcher 启动加速 END */
    }

    /**
     * A package was updated.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(final ArrayList<AppInfo> apps) {
        Runnable r = new Runnable() {
            public void run() {
                bindAppsUpdated(apps);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        if (mWorkspace != null) {
            mWorkspace.updateShortcutsAndWidgets(apps);
        }

        // Lenovo-sw:yuanyl2, Add edit mode function.
        // update dockview items
        if (mDockView != null && isDockViewShowing()) {
            mDockView.updateShortcuts(apps);
        }

        if (!LauncherAppState.isDisableAllApps() &&
                mAppsCustomizeContent != null) {
            mAppsCustomizeContent.updateApps(apps);
            mAppDrawerAdapter.updateApps(apps);
        }
        
        //add by zhanggx1 for removing on 2013-11-13 . s
        autoReorder("bindAppsUpdated");
        //add by zhanggx1 for removing on 2013-11-13 . e
    }

    /**
     * Packages were restored
     */
    public void bindAppsRestored(final ArrayList<AppInfo> apps) {
        Runnable r = new Runnable() {
            public void run() {
                bindAppsRestored(apps);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        if (mWorkspace != null) {
            mWorkspace.updateShortcutsAndWidgets(apps);
        }
    }

    /**
     * Update the state of a package, typically related to install state.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void updatePackageState(ArrayList<PackageInstallInfo> installInfo) {
        if (mWorkspace != null) {
            mWorkspace.updatePackageState(installInfo);
        }
    }

    /**
     * Update the label and icon of all the icons in a package
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    @Override
    public void updatePackageBadge(String packageName) {
        if (mWorkspace != null) {
            mWorkspace.updatePackageBadge(packageName, UserHandleCompat.myUserHandle());
        }
    }

    /**
     * A package was uninstalled.  We take both the super set of packageNames
     * in addition to specific applications to remove, the reason being that
     * this can be called when a package is updated as well.  In that scenario,
     * we only remove specific components from the workspace, where as
     * package-removal should clear all items by package name.
     * <p/>
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindComponentsRemoved(final ArrayList<String> packageNames,
                                      final ArrayList<AppInfo> appInfos, final UserHandleCompat user) {
        Runnable r = new Runnable() {
            public void run() {
                bindComponentsRemoved(packageNames, appInfos, user);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        if (!packageNames.isEmpty()) {
            mWorkspace.removeItemsByPackageName(packageNames, user);
        }
        if (!appInfos.isEmpty()) {
            mWorkspace.removeItemsByApplicationInfo(appInfos, user);
        }

        // Lenovo-sw:yuanyl2, Add edit mode function.
        // remove dockview items
        mDockView.removeItemsByPackageName(packageNames);

        // Notify the drag controller
        mDragController.onAppsRemoved(packageNames, appInfos);

        // Update AllApps
        if (!LauncherAppState.isDisableAllApps() &&
                mAppsCustomizeContent != null) {
            mAppsCustomizeContent.removeApps(appInfos);
            mAppDrawerAdapter.removeApps(appInfos);
        }
        
        //add by zhanggx1 for removing on 2013-11-13 . s
        autoReorder("bindComponentsRemoved");
        //add by zhanggx1 for removing on 2013-11-13 . e
    }

    /**
     * A number of packages were updated.
     */
    private ArrayList<Object> mWidgetsAndShortcuts;
    private Runnable mBindPackagesUpdatedRunnable = new Runnable() {
        public void run() {
            bindPackagesUpdated(mWidgetsAndShortcuts);
            mWidgetsAndShortcuts = null;
        }
    };

    public void bindPackagesUpdated(final ArrayList<Object> widgetsAndShortcuts) {
        if (waitUntilResume(mBindPackagesUpdatedRunnable, true)) {
            mWidgetsAndShortcuts = widgetsAndShortcuts;
            return;
        }

        // Update the widgets pane
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated(widgetsAndShortcuts);
        }
        
        /* Lenovo-SW zhaoxin5 20150601 add for custom widget list START */
        if (mWidgetListPanel != null) {
        	/** Lenovo-SW zhaoxin5 20150917 when install a new app, the widget list refresh late START */
        	final ArrayList<AppInfo> list = (ArrayList<AppInfo>) mModel.mBgAllAppsList.data.clone();
        	mWidgetListPanel.setApps(list);
        	/** Lenovo-SW zhaoxin5 20150917 when install a new app, the widget list refresh late END */
            mWidgetListPanel.onPackagesUpdated(widgetsAndShortcuts);
        }
        /* Lenovo-SW zhaoxin5 20150601 add for custom widget list END */
    }

    /*Lenovo-sw luyy1 add 20150729 for Yandex search start*/
    public void bindGoogleSearchChanged() {
        replaceWidget(GOOGLE_WIDGET_NAME, YANDEX_WIDGET_NAME);
    }
    /*Lenovo-sw luyy1 add 20150729 for Yandex search start*/

    private int mapConfigurationOriActivityInfoOri(int configOri) {
        final Display d = getWindowManager().getDefaultDisplay();
        int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
        switch (d.getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                // We are currently in the same basic orientation as the natural orientation
                naturalOri = configOri;
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                // We are currently in the other basic orientation to the natural orientation
                naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE) ?
                        Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
                break;
        }

        int[] oriMap = {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        };
        // Since the map starts at portrait, we need to offset if this device's natural orientation
        // is landscape.
        int indexOffset = 0;
        if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
            indexOffset = 1;
        }
        return oriMap[(d.getRotation() + indexOffset) % 4];
    }

    public boolean isRotationEnabled() {
        boolean enableRotation = sForceEnableRotation ||
                getResources().getBoolean(R.bool.allow_rotation);
        return enableRotation;
    }

    public void lockScreenOrientation() {
        //强制竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /*if (isRotationEnabled()) {
            setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources()
                    .getConfiguration().orientation));
        }*/
    }

    public void unlockScreenOrientation(boolean immediate) {
        //强制竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /*if (isRotationEnabled()) {
            if (immediate) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                }, mRestoreScreenOrientationDelay);
            }
        }*/
    }

    /**
     * Called when the SearchBar hint should be changed.
     *
     * @param hint the hint to be displayed in the search bar.
     */
    protected void onSearchBarHintChanged(String hint) {

    }

    protected boolean isLauncherPreinstalled() {
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(getComponentName().getPackageName(), 0);
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            } else {
                return false;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This method indicates whether or not we should suggest default wallpaper dimensions
     * when our wallpaper cropper was not yet used to set a wallpaper.
     */
    protected boolean overrideWallpaperDimensions() {
        return true;
    }

    protected boolean shouldClingFocusHotseatApp() {
        return false;
    }

    protected String getFirstRunClingSearchBarHint() {
        return "";
    }

    protected String getFirstRunCustomContentHint() {
        return "";
    }

    protected int getFirstRunFocusedHotseatAppDrawableId() {
        return -1;
    }

    protected ComponentName getFirstRunFocusedHotseatAppComponentName() {
        return null;
    }

    protected int getFirstRunFocusedHotseatAppRank() {
        return -1;
    }

    protected String getFirstRunFocusedHotseatAppBubbleTitle() {
        return "";
    }

    protected String getFirstRunFocusedHotseatAppBubbleDescription() {
        return "";
    }

    /**
     * To be overridden by subclasses to indicate that there is an activity to launch
     * before showing the standard launcher experience.
     */
    protected boolean hasFirstRunActivity() {
        return false;
    }

    /**
     * To be overridden by subclasses to launch any first run activity
     */
    protected Intent getFirstRunActivity() {
        return null;
    }

    private boolean shouldRunFirstRunActivity() {
        return !ActivityManager.isRunningInTestHarness() &&
                !mSharedPrefs.getBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, false);
    }

    protected boolean hasRunFirstRunActivity() {
        return mSharedPrefs.getBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, false);
    }

    public boolean showFirstRunActivity() {
        if (shouldRunFirstRunActivity() &&
                hasFirstRunActivity()) {
            Intent firstRunIntent = getFirstRunActivity();
            if (firstRunIntent != null) {
                startActivity(firstRunIntent);
                markFirstRunActivityShown();
                return true;
            }
        }
        return false;
    }

    private void markFirstRunActivityShown() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(FIRST_RUN_ACTIVITY_DISPLAYED, true);
        editor.apply();
    }

    /**
     * To be overridden by subclasses to indicate that there is an in-activity full-screen intro
     * screen that must be displayed and dismissed.
     */
    protected boolean hasDismissableIntroScreen() {
        return false;
    }

    /**
     * Full screen intro screen to be shown and dismissed before the launcher can be used.
     */
    protected View getIntroScreen() {
        return null;
    }

    /**
     * To be overriden by subclasses to indicate whether the in-activity intro screen has been
     * dismissed. This method is ignored if #hasDismissableIntroScreen returns false.
     */
    private boolean shouldShowIntroScreen() {
        return hasDismissableIntroScreen() &&
                !mSharedPrefs.getBoolean(INTRO_SCREEN_DISMISSED, false);
    }

    protected void showIntroScreen() {
        View introScreen = getIntroScreen();
        changeWallpaperVisiblity(false);
        if (introScreen != null) {
            mDragLayer.showOverlayView(introScreen);
        }
    }

    public void dismissIntroScreen() {
        markIntroScreenDismissed();
        if (showFirstRunActivity()) {
            // We delay hiding the intro view until the first run activity is showing. This
            // avoids a blip.
            mWorkspace.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDragLayer.dismissOverlayView();
                    showFirstRunClings();
                }
            }, ACTIVITY_START_DELAY);
        } else {
            mDragLayer.dismissOverlayView();
            showFirstRunClings();
        }
        changeWallpaperVisiblity(true);
    }

    private void markIntroScreenDismissed() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(INTRO_SCREEN_DISMISSED, true);
        editor.apply();
    }

    private void showFirstRunClings() {
        // The two first run cling paths are mutually exclusive, if the launcher is preinstalled
        // on the device, then we always show the first run cling experience (or if there is no
        // launcher2). Otherwise, we prompt the user upon started for migration
        LauncherClings launcherClings = new LauncherClings(this);
        if (launcherClings.shouldShowFirstRunOrMigrationClings()) {
        	if (launcherClings.canShowMigrateClings()) {
            	LauncherLog.i("xixia", "showLongPressClingForMigrateFromXLauncher");
        		launcherClings.showLongPressClingForMigrateFromXLauncher();
        	}
        	/* else if (mModel.canMigrateFromOldLauncherDb(this)) {
                launcherClings.showMigrationCling();
            } else {
                launcherClings.showLongPressCling(true);
            }*/
        }
    }

    void showWorkspaceSearchAndHotseat() {
        if (mWorkspace != null) mWorkspace.setAlpha(1f);
        if (mHotseat != null) mHotseat.setAlpha(1f);
        if (mPageIndicators != null) mPageIndicators.setAlpha(1f);
        showSearch();
    }

    void hideWorkspaceSearchAndHotseat() {
        if (mWorkspace != null) mWorkspace.setAlpha(0f);
        if (mHotseat != null) mHotseat.setAlpha(0f);
        if (mPageIndicators != null) mPageIndicators.setAlpha(0f);
        hideSearch();
    }

    void hideSearch() {
        if (mSearchDropTargetBar != null) mSearchDropTargetBar.hideSearchBar(false);
    }

    void showSearch() {
        if (mSearchDropTargetBar != null) mSearchDropTargetBar.showSearchBar(false);
    }

    private void updateStatusBarColor(int color) {
        updateStatusBarColor(color, 300);
    }

    private void updateStatusBarColor(int color, int duration) {
    	if (!Utilities.isLmpOrAbove()) {
    		return;
    	}
        final Window window = getWindow();
        ObjectAnimator animator = ObjectAnimator.ofInt(window,
                "statusBarColor", window.getStatusBarColor(), color);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setDuration(duration);
        animator.start();
    }

    public ItemInfo createAppDragInfo(Intent appLaunchIntent) {
        // Called from search suggestion, not supported in other profiles.
        final UserHandleCompat myUser = UserHandleCompat.myUserHandle();
        LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
        LauncherActivityInfoCompat activityInfo = launcherApps.resolveActivity(appLaunchIntent,
                myUser);
        if (activityInfo == null) {
            return null;
        }
        return new AppInfo(this, activityInfo, myUser, mIconCache, null);
    }

    public ItemInfo createShortcutDragInfo(Intent shortcutIntent, CharSequence caption,
                                           Bitmap icon) {
        // Called from search suggestion, not supported in other profiles.
        return createShortcutDragInfo(shortcutIntent, caption, icon,
                UserHandleCompat.myUserHandle());
    }

    public ItemInfo createShortcutDragInfo(Intent shortcutIntent, CharSequence caption,
                                           Bitmap icon, UserHandleCompat user) {
        UserManagerCompat userManager = UserManagerCompat.getInstance(this);
        CharSequence contentDescription = userManager.getBadgedLabelForUser(caption, user);
        return new ShortcutInfo(shortcutIntent, caption, contentDescription, icon, user);
    }

    protected void moveWorkspaceToDefaultScreen() {
        mWorkspace.moveToDefaultScreen(false);
    }

    public void startDrag(View dragView, ItemInfo dragInfo, DragSource source) {
        dragView.setTag(dragInfo);
        mWorkspace.onExternalDragStartedWithItem(dragView);
        mWorkspace.beginExternalDragShared(dragView, source);
    }

    @Override
    public void onPageSwitch(View newPage, int newPageIndex) {
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcher3 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "sFolders.size=" + sFolders.size());
        mModel.dumpState();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.dumpState();
        }
        Log.d(TAG, "END launcher3 dump state");
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        synchronized (sDumpLogs) {
            writer.println(" ");
            writer.println("Debug logs: ");
            for (int i = 0; i < sDumpLogs.size(); i++) {
                writer.println("  " + sDumpLogs.get(i));
            }
        }
    }

    public static void dumpDebugLogsToConsole() {
        if (DEBUG_DUMP_LOG) {
            synchronized (sDumpLogs) {
                Log.d(TAG, "");
                Log.d(TAG, "*********************");
                Log.d(TAG, "Launcher debug logs: ");
                for (int i = 0; i < sDumpLogs.size(); i++) {
                    Log.d(TAG, "  " + sDumpLogs.get(i));
                }
                Log.d(TAG, "*********************");
                Log.d(TAG, "");
            }
        }
    }

    public static void addDumpLog(String tag, String log, boolean debugLog) {
        addDumpLog(tag, log, null, debugLog);
    }

    public static void addDumpLog(String tag, String log, Exception e, boolean debugLog) {
        if (debugLog) {
            if (e != null) {
                Log.d(tag, log, e);
            } else {
                Log.d(tag, log);
            }
        }
        if (DEBUG_DUMP_LOG) {
            sDateStamp.setTime(System.currentTimeMillis());
            synchronized (sDumpLogs) {
                sDumpLogs.add(sDateFormat.format(sDateStamp) + ": " + tag + ", " + log
                        + (e == null ? "" : (", Exception: " + e)));
            }
        }
    }

    public void dumpLogsToLocalData() {
        if (DEBUG_DUMP_LOG) {
            new AsyncTask<Void, Void, Void>() {
                public Void doInBackground(Void... args) {
                    boolean success = false;
                    sDateStamp.setTime(sRunStart);
                    @SuppressWarnings("deprecation")
                    String FILENAME = sDateStamp.getMonth() + "-"
                            + sDateStamp.getDay() + "_"
                            + sDateStamp.getHours() + "-"
                            + sDateStamp.getMinutes() + "_"
                            + sDateStamp.getSeconds() + ".txt";

                    FileOutputStream fos = null;
                    File outFile = null;
                    try {
                        outFile = new File(getFilesDir(), FILENAME);
                        outFile.createNewFile();
                        fos = new FileOutputStream(outFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fos != null) {
                        PrintWriter writer = new PrintWriter(fos);

                        writer.println(" ");
                        writer.println("Debug logs: ");
                        synchronized (sDumpLogs) {
                            for (int i = 0; i < sDumpLogs.size(); i++) {
                                writer.println("  " + sDumpLogs.get(i));
                            }
                        }
                        writer.close();
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                            success = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    @Override
    public Launcher getLauncherInstance() {
        return this;
    }

    public AppsCustomizePagedView.SortMode getAppsCustomizeContentSortMode() {
        return mAppsCustomizeContent.getSortMode();
    }

    public boolean shouldShowSearchBar() {
        return mWorkspace.getShowSearchBar();
    }

    public boolean shouldHideWorkspaceIconLables() {
        return mWorkspace.getHideIconLables();
    }

    public String getWorkspaceTransitionEffect() {
        TransitionEffect effect = mWorkspace.getTransitionEffect();
        return effect == null ? TransitionEffect.TRANSITION_EFFECT_NONE : effect.getName();
    }

    public String getAppsCustomizeTransitionEffect() {
        TransitionEffect effect = mAppsCustomizeContent.getTransitionEffect();
        return effect == null ? TransitionEffect.TRANSITION_EFFECT_NONE : effect.getName();
    }

    public void updateDynamicGrid() {
        updateDynamicGrid(mWorkspace.getRestorePage());
    }

    public void updateDynamicGrid(int page) {
        mSearchDropTargetBar.setupQSB(Launcher.this);

        initializeDynamicGrid();

        mGrid.layout(Launcher.this);

        // Synchronized reload
        mModel.resetLoadedState(true, true);
        /* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
        mModel.startLoader(true, page, ModeSwitchHelper.getLauncherModelLoaderTaskLoaderFlag(this));
        /* Lenovo-SW zhaoxin5 20150529 add for 2 layer support */
        mWorkspace.updateCustomContentVisibility();

        mAppDrawerAdapter.reset();
    }

    public void setUpdateDynamicGrid() {
        mDynamicGridUpdateRequired = true;
    }

    public boolean updateGridIfNeeded() {
        if (mDynamicGridUpdateRequired) {
            updateDynamicGrid(mWorkspace.getCurrentPage());
            mDynamicGridUpdateRequired = false;
            return true;
        }

        return false;
    }

    public boolean isSearchBarEnabled() {
        return SettingsProvider.getBoolean(this,
                SettingsProvider.SETTINGS_UI_HOMESCREEN_SEARCH,
                R.bool.preferences_interface_homescreen_search_default);
    }

    /* Lenovo-SW zhaoxin5 20150127 after Launcher onCreate finished, to apply the default theme START */
    private void applyDefaultTheme(){
    	/*String[] parameters = Placement.getDefaultThemeParameters(this);*/
        //String[] parameters = new String[]{"apk", ThemeController.DEFAULT_APK_THEME};

        String[] keywords = getResources().getStringArray(R.array.config_theme_load_keywords);
        String themeName = "default";
        for (String keyword : keywords) {
            String[] modelTheme = keyword.split(",");
            if (Build.MODEL.contains(modelTheme[0])) {
                themeName = modelTheme[1];
                break;
            }
        }
        String[] parameters = new String[]{"zip", ThemeController.UI_ZIP_THEME + themeName + ".ktm"};
        boolean enableThemeMask = !Utilities.isRowProduct();
        if (null != parameters) {
            Intent intent = new Intent();
            if(parameters[0].equals("apk")){
                Log.i(TAG, "apply default apk theme : " + parameters[1]);
                intent.setAction(ThemeController.ACTION_LAUNCHER_THEME_APK);
                intent.putExtra(ThemeController.ACTION_LAUNCHER_THEME_PACKAGE, parameters[1]);
            }
            if(parameters[0].equals("zip")){
                Log.i(TAG, "apply default zip theme : " + parameters[1]);
                intent.setAction(ThemeController.ACTION_LAUNCHER_THEME_ZIP);
                intent.putExtra(ThemeController.ACTION_LAUNCHER_THEME_PATH, parameters[1]);
            }
            intent.putExtra(ThemeController.EXTRA_LAUNCHER_THEME_ENABLE_THEME_MASK, enableThemeMask);
            this.sendBroadcast(intent);
        }
    }
    /* Lenovo-SW zhaoxin5 20150127 after Launcher onCreate finished, to apply the default theme END */

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
    // Dock functions
    private boolean isDockViewShowing = false;
    private boolean isDockViewAnimationing = false;

    public boolean isDockViewShowing() {
        return isDockViewShowing;
    }

    public void showDockView() {
        if (isDockViewShowing) {
            return;
        }
        getDockView().setVisibility(View.VISIBLE);
        XDockViewLayout mLayout = getDockView().getLayout();
        if (mLayout != null) {
            mLayout.setRelativeX(0.0f);
        }
        isDockViewShowing = true;
        mMenuController.onDockViewShow();
    }

    public void dismissDockView() {
        if (!isDockViewShowing) {
            return;
        }
        if (getDockView() == null || mWorkspace == null) {
            return;
        }
        mDockView.setVisibility(View.GONE);
        isDockViewShowing = false;
        mMenuController.onDockViewHide();
    }

    public void exitDockViewByReload() {
        if (!isDockViewShowing) {
            return;
        }
        if (getDockView() == null || mWorkspace == null) {
            return;
        }
        getDockView().setVisibility(View.GONE);
        isDockViewShowing = false;
        mMenuController.onDockViewHide(false);
    }

    public float getItemScale() {
        if (mWorkspace.isInOverviewMode()) {
            return mGrid.getOverviewModeScale();
        } else {
            return 1.0f;
        }
    }

    public int getDisplayWidth() {
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile Grid = app.getDynamicGrid().getDeviceProfile();
        return Grid.widthPx;
    }

    public int getBarHeight(Resources resources) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = resources.getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    public boolean isPositionInMenuList(int x , int y){
        View view = findViewById(R.id.editing_entry_view);
        Rect rect = new Rect();
        mDragLayer.getDescendantRectRelativeToSelf(view, rect);
        return rect.contains(x, y);
    }

    public int[] getLocationPosition(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location;
    }

    View createSnapshot(View view) {
        ImageView mSnapshot = new ImageView(this);
        Bitmap mBitmap = mWorkspace.createDragBitmap(view, new Canvas(),
                Workspace.DRAG_BITMAP_PADDING);
        Drawable mdrawable = Utilities.createIconDrawable(mBitmap);
        mSnapshot.setImageDrawable(mdrawable);
        return mSnapshot;
    }

    public int[] getDockViewTargetPosition() {
    	/**
    	 * Lenovo-SW zhaoxin5 20150730
    	 * 在port\launcher.xml中的如下定义中,
    	 * 如果android:visibility的值是gone的话,
    	 * 会导致getLocationPosition(xDockView.getLayout());返回的值都是0
    	 * 从而产生XTHREEROW-361的bug
    	   <include layout="@layout/multi_select_panel"
            android:id="@+id/multi_select_panel"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_gravity="bottom"
            android:visibility="invisible" />
    	 */
        XDockView xDockView = (XDockView) getDockView();
        int[] location = getLocationPosition(xDockView.getLayout());

        location[0] = location[0] + xDockView.getLayout().getPaddingLeft();
        location[1] = location[1] + xDockView.getLayout().getPaddingTop();

        return location;
    }

    public int[] getWorkspaceFolderIconPosition(FolderIcon folderIcon, View snapshot) {
        int[] location = new int[]{-1, -1};
        mDragLayer.getLocationInDragLayer(folderIcon, location);

        return location;
    }

    public FolderInfo getFolderInfoById(long id) {
        if (sFolders.containsKey(id)) {
            return sFolders.get(id);
        }
        return mModel.sBgFolders.get(id);
    }
    public int[] getFolderTargetPosition(Folder folder, View target) {
        ItemInfo info = (ItemInfo) target.getTag();
        return getFolderTargetPosition(folder, info);
    }

    public int[] getFolderTargetPosition(Folder folder, ItemInfo info) {
        DynamicGrid dg = LauncherAppState.getInstance().getDynamicGrid();
        DeviceProfile profile = dg.getDeviceProfile();

        // Get cell left corner position.
        int[] cellLocation = new int[]{-1, -1};
        CellLayout cellLayout = folder.getContent();
        mDragLayer.getLocationInDragLayer(cellLayout, cellLocation);

        int cellWidth = cellLayout.getCellWidth();
        int cellPaddingLeft = (cellWidth - profile.iconSizePx) / 2;
        int cellHeight = cellLayout.getCellHeight();

        // Calculate target position.
        int[] location = new int[]{-1, -1};
        if (info.cellX==0 && info.cellY==0) {
            int cellPaddingTop = (cellHeight - profile.cellHeightPx ) / 2;
            location[0] = cellPaddingLeft + cellLocation[0];
            location[1] = cellPaddingTop + cellLocation[1];
        } else {
            View firstChild = cellLayout.getChildAt(0, 0);
            int childWidth = firstChild.getWidth();
            int childHeight = firstChild.getHeight();

            int[] childLocation = new int[]{-1, -1};
            mDragLayer.getLocationInDragLayer(firstChild, childLocation);

            location[0] = cellPaddingLeft + childLocation[0] + childWidth * info.cellX;
            location[1] = firstChild.getPaddingTop() + childLocation[1] + childHeight * info.cellY;
            // Can't over folder mMaxCountY's position.
            int maxVisibleHeight = cellLayout.getCellHeight() * folder.getMaxCountY();
            if (location[1] > maxVisibleHeight) {
                location[1] = maxVisibleHeight + cellLayout.getHeightGap() + firstChild.getPaddingTop();
            }
        }
        return location;
    }

    public int[] getFolderCenter(Folder folder) {
        DynamicGrid dg = LauncherAppState.getInstance().getDynamicGrid();
        DeviceProfile profile = dg.getDeviceProfile();
        float scale = profile.getOverviewModeScale();
        int[] location = new int[]{0, 0};

        return location;
    }

    public int[] getWorkspaceSourcePosition(int[] pos, int width, int height, int paddingTop) {
        float scale = getItemScale();

        int centerX = width / 2;
        int snapshotcenterX = (int) (mGrid.cellWidthPx * scale / 2.0f);

        pos[0] = pos[0] + centerX - snapshotcenterX;
        pos[1] = pos[1] + paddingTop;//centerY - snapshotcenterY;

        return pos;
    }

    public int[] getWorkspaceTargetPosition(View target) {
        if (!mWorkspace.isInOverviewMode()) {
            return getWorkspaceTargetPositionInNomalMode(target);
        }

        int[] workspaceLeftCorner = {0, 0};
        CellLayout layout = mWorkspace.getCurrentDropLayout();
        mDragLayer.getLocationInDragLayer(layout, workspaceLeftCorner);

        DynamicGrid dg = LauncherAppState.getInstance().getDynamicGrid();
        DeviceProfile profile = dg.getDeviceProfile();

        float scale = getItemScale();
        int cellWidth = layout.getWidth() / (int) profile.numColumns;
        int cellHeight = layout.getHeight() / (int) profile.numRows;
        int scaleCellWidth = (int) (cellWidth * scale);
        int scaleCellHeight = (int) (cellHeight * scale);

        ItemInfo info = (ItemInfo) target.getTag();

        int iconPaddingLeft = (int) ((scaleCellWidth - profile.cellWidthPx * scale) / 2);
        int iconPaddingTop = (int) ((scaleCellHeight - profile.cellHeightPx * scale) / 2);

        int targetPage = mWorkspace.getPageIndexForScreenId(info.screenId);
        int currentPage = mWorkspace.getPageIndexForScreenId(mWorkspace.getCurrentScreenId());
        int dis = 0;
        int scaledCellLayoutWidth = (int) (layout.getWidth() * scale);
        if (targetPage < currentPage) {
            dis = -scaledCellLayoutWidth;
        } else if (targetPage > currentPage) {
            dis = scaledCellLayoutWidth;
        }

        int location[] = new int[]{-1, -1};
        location[0] = workspaceLeftCorner[0] + info.cellX * scaleCellWidth + iconPaddingLeft + dis;
        location[1] = workspaceLeftCorner[1] + info.cellY * scaleCellHeight + iconPaddingTop;

        return location;
    }

    private int[] getWorkspaceTargetPositionInNomalMode(View target) {
        int[] workspaceLeftCorner = {0, 0};
        workspaceLeftCorner[0] = normalModeCellLayoutRect.left;
        workspaceLeftCorner[1] = normalModeCellLayoutRect.top;
        int cellLayoutWidth = normalModeCellLayoutRect.right;
        int cellLayoutHeight = normalModeCellLayoutRect.bottom;

        DynamicGrid dg = LauncherAppState.getInstance().getDynamicGrid();
        DeviceProfile profile = dg.getDeviceProfile();

        float scale = getItemScale();

        int cellWidth = cellLayoutWidth / (int) profile.numColumns;
        int cellHeight = cellLayoutHeight / (int) profile.numRows;

        int iconPaddingLeft = (int) ((cellWidth - profile.cellWidthPx) / 2);
        int iconPaddingTop = (int) ((cellHeight - profile.cellHeightPx) / 2);

        ItemInfo info = (ItemInfo) target.getTag();
        int targetPage = mWorkspace.getPageIndexForScreenId(info.screenId);
        int currentPage = mWorkspace.getPageIndexForScreenId(mWorkspace.getCurrentScreenId());
        int dis = 0;
        if (targetPage < currentPage) {
            dis = -cellLayoutWidth;
        } else if (targetPage > currentPage) {
            dis = cellLayoutWidth;
        }

        int location[] = new int[]{-1, -1};
        location[0] = workspaceLeftCorner[0] + info.cellX * cellWidth + iconPaddingLeft + dis;
        location[1] = workspaceLeftCorner[1] + info.cellY * cellHeight + iconPaddingTop;

        return location;
    }

    private int getDurationTime() {
        int durationTime = 300;
        int iconDis = 0;

        if (mDockView == null) {
            return durationTime;
        }
        if (mDockView.getLayout() == null) {
            return durationTime;
        }

        iconDis = mDockView.getLayout().getIconDis();
        if (iconDis == 0) {
            return durationTime;
        }

        int maxSize = getDisplayWidth() / iconDis / 2;
        int iconSize = mDockView.getLayout().getChildCount();
        if (iconSize > maxSize) {
            return durationTime + 100;
        } else {
            return durationTime;
        }
    }

    // Click an shortcut to Dock bar
    public void animViewToDockView(View sourceView, final View targetView, ItemInfo info) {
        //add for active icon, begin;
        final View snapshot = createSnapshot(sourceView);
        final int[] itemPos = getLocationPosition(sourceView);
        final int itemWidth = sourceView.getWidth();
        final int itemHeight = sourceView.getHeight();
        final int paddingTop = sourceView.getPaddingTop();

        if (mWorkspace.getOpenFolder() == null) {
            CellLayout cellLayout = mWorkspace.getParentCellLayoutForView(sourceView);
            if (cellLayout != null) {
                cellLayout.removeView(sourceView);
                // add by zhanggx1.s
                autoReorder("animViewToDockView");
                // add by zhanggx1.e
            }
        } else {
            // item in folder
            Folder folder = mWorkspace.getOpenFolder();
            FolderInfo mInfo = folder.getInfo();
            mInfo.remove((ShortcutInfo) sourceView.getTag());
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;

        snapshot.setLayoutParams(lp);
        float scale = getItemScale();
        snapshot.setPivotX(0);
        snapshot.setPivotY(0);
        snapshot.setScaleX(scale);
        snapshot.setScaleY(scale);
        snapshot.setVisibility(View.INVISIBLE);
        getDragLayer().addView(snapshot);

        int[] sourcePos = getWorkspaceSourcePosition(itemPos, itemWidth,
                itemHeight, paddingTop);
        int[] targetPos = getDockViewTargetPosition();

        final int sourceX = sourcePos[0];
        final int sourceY = sourcePos[1];
        final int deltaX = targetPos[0] - sourcePos[0];
        final int deltaY = targetPos[1] - sourcePos[1];

        final ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);

        int durationTime = getDurationTime();
        anim.setDuration(durationTime);
        anim.setInterpolator(new DecelerateInterpolator(1.5f));
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                lockScreenOrientation();
                snapshot.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                unlockScreenOrientation(false);
                getDragLayer().removeView(snapshot);

                // If cleared all items in the celllayout, show 'x' sign
                if (mWorkspace.getOpenFolder() == null) {
                    for (int i = 0; i < mWorkspace.getPageCount(); i++) {
                        CellLayout cl = (CellLayout) mWorkspace.getPageAt(i);
                        if (cl != null) {
                            //Lenovo-sw:zhangyj19, Default page can be deleted
                            //if (cl.getItemChildCount() == 0 && i != mWorkspace.getDefaultPageIndex() && !cl.isAddLayout()) {
                            if (cl.getItemChildCount() == 0 && !cl.isAddLayout()) {
                                cl.setDeleteBackground(true);
                            }
                        }
                    }
                }

                targetView.setVisibility(View.VISIBLE);
                getDragLayer().invalidate();
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
                getDragLayer().invalidate();
            }
        });
        anim.start();
    }

    public void animViewToDockFolderIcon(View sourceView, final FolderIcon targetView,final ItemInfo info) {
        // add for active icon, begin;
        final View mSnapshot = createSnapshot(sourceView);

        final int[] itemPos = getLocationPosition(sourceView);
        final int itemWidth = sourceView.getWidth();
        final int itemHeight = sourceView.getHeight();
        final int paddingTop = sourceView.getPaddingTop();

        if (mWorkspace.getOpenFolder() == null) {
            CellLayout cellLayout = mWorkspace.getParentCellLayoutForView(sourceView);
            cellLayout.removeView(sourceView);
            //add by zhanggx1.s
            autoReorder("animViewToDockFolderIcon");
            //add by zhanggx1.e
        } else {
            // item in folder
            FolderInfo mInfo =mWorkspace.getOpenFolder().getInfo();
            mInfo.remove((ShortcutInfo) sourceView.getTag());
        }
        mDockView.addItemToFolderIcon((ItemInfo)info);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        //add for active icon, begin;
        mSnapshot.setLayoutParams(lp);
        float scale = getItemScale();
        mSnapshot.setPivotX(0);
        mSnapshot.setPivotY(0);
        mSnapshot.setScaleX(scale);
        mSnapshot.setScaleY(scale);
        mSnapshot.setVisibility(View.INVISIBLE);
        getDragLayer().addView(mSnapshot);
        //add for active icon, end;

        //add for active icon, begin;
        int[] sourcePos = getWorkspaceSourcePosition(itemPos, itemWidth,
                itemHeight, paddingTop);
        int[] targetPos = getLocationPosition(targetView);
        //add for active icon, end;
        final int sourceX = sourcePos[0];
        final int sourceY = sourcePos[1];
        final int deltaX = targetPos[0] - sourcePos[0];
        final int deltaY = targetPos[1] - sourcePos[1];

        final ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(300);
        anim.setInterpolator(new DecelerateInterpolator(1.5f));
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                lockScreenOrientation();
                mSnapshot.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                unlockScreenOrientation(false);
                getDragLayer().removeView(mSnapshot);

                if(mDockView.getFolderIcon() != null){
                    mDockView.getFolderIcon().showItem((ShortcutInfo) info);
                }
                //Added by Chandler Chen(chenchen23@lenovo.com)
                //非主页才可显示x
                //Lenovo-sw:zhangyj19, Default page can be deleted
                //if(mWorkspace.getCurrentPage() != mWorkspace.getDefaultPageIndex()){
                    if(mWorkspace.getCurrentDropLayout().getItemChildCount() == 0){
                        mWorkspace.getCurrentDropLayout().setDeleteBackground(true);
                    }
                //}
                getDragLayer().invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation
                        .getAnimatedValue();
                mSnapshot.setX(sourceX + value * deltaX);
                mSnapshot.setY(sourceY + value * deltaY);
                getDragLayer().invalidate();
            }
        });
        anim.start();
    }

    public void adjustViewLayer() {
        if (mDockView != null) {
            mDockView.show(true);
        }
    }

    // Dock functions end.
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final Handler widgetHanlder = new Handler();
    public boolean mIsWidgetFlying = false;

    private void setWidgetIsFlying(boolean isFlying) {
        if (isFlying) {
            // notify the others, we are adding widget
            mIsWidgetFlying = true;
        } else {
            widgetHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // notify the others, we had added the widget
                    mIsWidgetFlying = false;
                }
            }, 400);
        }
    }

    /**
     * @param view which view you want to get its position in the dragLayer
     * @return the position of the view in the dragLayer
     */
    public int[] getLocationInDragLayer(final View view) {
        int[] location = {-1, -1};
        mDragLayer.getLocationInDragLayer(view, location);
        return location;
    }

    /**
     * @param from                   animate start position
     * @param to                     animate stop position
     * @param flyView                the animating view
     * @param onAnimateStartRunnable do something when the animate start
     * @param onAnimateEndRunnable   do something when the animate stop
     */
    public void startFlyingAnimate(final int[] from, final int[] to, final View flyView,
                                   final Runnable onAnimateStartRunnable, final Runnable onAnimateEndRunnable, final int duration) {
        // add the fly view to dragLayer, so it can fly everywhere
        mDragLayer.addView(flyView);
        ValueAnimator flyAnimatior;
        flyAnimatior = ValueAnimator.ofFloat(0, 1f);
        flyAnimatior.setDuration(duration);
        flyAnimatior.setInterpolator(new DecelerateInterpolator(2f));
        flyAnimatior.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                flyView.setX(((to[0] - from[0]) * value + from[0]));
                flyView.setY(((to[1] - from[1]) * value + from[1]));
                flyView.setAlpha(1 - value / 2);
            }
        });

        flyAnimatior.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mDragLayer.removeView(flyView);
                setWidgetIsFlying(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mDragLayer.removeView(flyView);
                setWidgetIsFlying(false);
                if (onAnimateEndRunnable != null) {
                    onAnimateEndRunnable.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
                setWidgetIsFlying(true);
                if (onAnimateStartRunnable != null) {
                    onAnimateStartRunnable.run();
                }
            }
        });

        flyAnimatior.start();
    }

    private void setAddInfo(PendingAddItemInfo info) {
        mPendingAddInfo.container = info.container;
        mPendingAddInfo.screenId = info.screenId;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;

        mPendingAddInfo.cellX = info.cellX;
        mPendingAddInfo.cellY = info.cellY;

        mPendingAddInfo.spanX = info.spanX;
        mPendingAddInfo.spanY = info.spanY;
    }

    public void animateAddWidget(final PendingAddItemInfo info, final View sourceView, final View flyView) {
        if (getWorkspace().getOpenFolder() != null) {
            this.closeFolder();
        }
        resetAddInfo();
        setAddInfo(info);

        // animate start location
        int[] from = getLocationInDragLayer(sourceView);

        // animate end location
        CellLayout toCellLayout = getCellLayout(LauncherSettings.Favorites.CONTAINER_DESKTOP, info.screenId);
        int[] to = {-1, -1};
        float scale = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getOverviewModeScale();
        toCellLayout.regionToCenterPoint(info.cellX, info.cellY, info.spanX, info.spanY, to);
        final int toX = (int) ((to[0] - flyView.getWidth() / 2) * scale);
        final int toY = (int) ((to[1] - flyView.getHeight() / 2) * scale);

        Runnable onAnimateEnd = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (info instanceof PendingAddWidgetInfo) {
                    addWidgetAfterAnimation((PendingAddWidgetInfo) info);
                }
            }
        };

        startFlyingAnimate(from, new int[]{toX, toY}, flyView, null, onAnimateEnd, 300);
    }

    public void addWidgetAfterAnimation(final PendingAddWidgetInfo info) {
        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
//            Bundle options = info.bindOptions;

            boolean success = false;
            //wqh add
            //mAppWidgetManager.setBindAppWidgetPermission(this.getPackageName(), true);   //txk add
            //Modify by zhangdy3 for support sdk 15 start
//            if (options != null) {
//                //TODO this code need framework.jar 4.4 version
//                // success =
//                // mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
//                // info.componentName, options);
//                success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
//                        info.componentName);
//            } else {
//                success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
//                        info.componentName);
//            }

            if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                try {
                    success = AppWidgetManager.getInstance(this).bindAppWidgetIdIfAllowed(appWidgetId, info.componentName);
                } catch (java.lang.IllegalArgumentException e) {
                    Log.e("MartinAddWidget", "error-----widget add error");
                    //DISABLE WIDGET_LIST by huangyn1 START
                    //                    if(null != mWidgetListView){
                    //			        	mWidgetListView.onPackagesUpdated(
                    //			                    LauncherModel.getSortedWidgetsAndShortcuts(this));
                    //			        }
                    //DISABLE WIDGET_LIST by huangyn1 END
                    return;
                }
            } else {
                try {
                    AppWidgetManager.getInstance(this).bindAppWidgetIdIfAllowed(appWidgetId, info.componentName);
                } catch (SecurityException e) {
                    // Debug.printException("-------Launcher--SecurityException---------", e);
                }
                addAppWidgetImpl(appWidgetId, info, null, info.info);
                return;
            }
            //Modify by zhangdy3 for support sdk 15 end

            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                // mAppWidgetId = appWidgetId;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    public void animateShortcut(final PendingAddItemInfo info, final View sourceView, final View flyView) {
        if (getWorkspace().getOpenFolder() != null) {
            this.closeFolder();
        }

        resetAddInfo();
        setAddInfo(info);

        final int[] from = getLocationInDragLayer(sourceView);
        final int[] to = mWorkspace.getFlyTargetLoc(info.cellX, info.cellY);

        Runnable onAnimateEnd = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
                createShortcutIntent.setComponent(info.componentName);
                createShortcutIntent.putExtra("title", info.title);
                processShortcut(createShortcutIntent);
            }
        };

        startFlyingAnimate(from, to, flyView, null, onAnimateEnd, 300);
    }

    private boolean mIsFullScreen = false;

    private boolean hideStatusBar(boolean hide) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        boolean isFullScreen = ((winParams.flags & bits) == bits);
        if (hide) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);

        boolean stateChanged = (isFullScreen && !hide) || (!isFullScreen && hide);

        return stateChanged;
    }

    public void setFullScreen(boolean fullScreen) {
        LauncherLog.i(TAG, "setFullScreen, fullScreen" + fullScreen);
        boolean stateChanged = hideStatusBar(fullScreen);
        if (stateChanged) {
            reLayoutChildren(fullScreen);
        }
        mIsFullScreen = fullScreen;
    }

    /**
     * 划入信息流回调方法
     * 第一次加载过程中不调用
     */
    public void scrollToKinflow() {
        /*String pkgNameStr = CommonShareData.getString(CommonShareData.KEY_APP_PACKAGE_NAME,"");
        if(!TextUtils.isEmpty(pkgNameStr)&& !pkgNameStr.equals("null")){
            MobileStatistics.onPageStart(this,pkgNameStr);
            LogUtil.e("sIsKinflowIsUsed","scrollToKinflow pkgname= "+pkgNameStr);
        }*/
    }
    /**
     * 划出信息流回调方法
     * 第一次加载过程中不调用
     */
    public void scrollOutKinflow() {
        /*String pkgNameStr = CommonShareData.getString(CommonShareData.KEY_APP_PACKAGE_NAME,"");
        if(!TextUtils.isEmpty(pkgNameStr)&& !pkgNameStr.equals("null")){
            MobileStatistics.onPageEnd(this,pkgNameStr);
            LogUtil.e("sIsKinflowIsUsed","scrollOutKinflow pkgname= "+pkgNameStr);
        }*/

    }

    private void reLayoutChildren(boolean fullScreen) {
        if (mDragLayer == null) {
            return;
        }

        // Modify drag layer alpha
        Drawable d = mDragLayer.getBackground();
        if (d != null) {
            int alpha = fullScreen ? 0 : 0xFF;
            d.setAlpha(alpha);
        }

        reLayoutView(fullScreen, mWorkspace);
    }

    private void reLayoutView(boolean on, View v) {
        if (v == null) {
            return;
        }

        int left = v.getPaddingLeft();
        int top = v.getPaddingTop();
        int right = v.getPaddingRight();
        int bottom = v.getPaddingBottom();

        boolean custom = false;
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp instanceof DragLayer.LayoutParams) {
            DragLayer.LayoutParams dragLayerLp = (DragLayer.LayoutParams) lp;
            if (dragLayerLp.customPosition) {
                top = dragLayerLp.y;
                custom = true;
            }
        }

        int statusBarHeight = getStatusBarHeight(getResources());
        if (on) {
            top += statusBarHeight;
        } else {
            top -= statusBarHeight;
        }

        if (custom) {
            ((DragLayer.LayoutParams) lp).y = top;
        } else {
            v.setPadding(left, top, right, bottom);
        }
    }

    private static int mStatusBarHeight = Integer.MIN_VALUE;

    public static int getStatusBarHeight(Resources resources) {
        final int ENLARGE_FACTOR = 25;
        if (mStatusBarHeight == Integer.MIN_VALUE) {
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                mStatusBarHeight = resources.getDimensionPixelSize(resourceId);
            } else {
                mStatusBarHeight = (int) (ENLARGE_FACTOR * resources.getDisplayMetrics().density);
            }
        }

        return mStatusBarHeight;
    }

    public void showMultiSelectingTips() {
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.showMultiSelectToolTip(true);
        }
    }

    public void hideMultiSelectingTips() {
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.hideMultiSelectToolTip(false);
        }
    }

    ObjectAnimator mSettingsPanelAnim;

    private void setupSeetingsPanelAnimation() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        Configuration config = getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mSettingsPanelAnim = LauncherAnimUtils.ofFloat(mSettingsPanel, "translationX", -width, 0);
        } else {
            mSettingsPanelAnim = LauncherAnimUtils.ofFloat(mSettingsPanel, "translationX", width, 0);
        }
        mSettingsPanelAnim.setDuration(300);
    }

    public void showSettingsPanel() {
        setFullScreen(false);
        // mSettingsPanel.bringToFront();

        mSettingsPanel.setVisibility(View.VISIBLE);
        mSettingsPanelAnim.start();
        mIsSettingPanelShowing = true;
        updateStatusBarColor(getResources().getColor(R.color.settings_header_bg_color));

        /** Lenovo-SW zhaoxin5 20150805 XTHREEROW-585 START */
        // bringChildOfDragLayerToFront(mSettingsPanel);
        /** Lenovo-SW zhaoxin5 20150805 XTHREEROW-585 END */
    }

    public void hideSettingsPanel() {
        setFullScreen(false);
        // mWorkspace();
        updateStatusBarColor(Color.TRANSPARENT);
        mSettingsPanel.setVisibility(View.GONE);
        mIsSettingPanelShowing = false;
    }

    public void updateSettingsPanel() {
        mSettingsController.update();
    }

	public IconCache getIconCache() {
		return mIconCache;
	}

	private void startQuicShareGridViewAndSearchingTextViewChangeAnimate() {
		final View gridView = mQuickShareZone.findViewById(R.id.quick_share_grid_view);
		final View textView = mQuickShareZone.findViewById(R.id.quick_share_loading_tip);
		ValueAnimator animatior;
        animatior = ValueAnimator.ofFloat(0f, 1f);
        animatior.setDuration(500);
        animatior.setInterpolator(new DecelerateInterpolator(2f));
        animatior.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                gridView.setScaleX(value);
                gridView.setScaleY(value);
                gridView.setAlpha(value);

				textView.setAlpha(1f-value);
            }
        });
        animatior.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				gridView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				textView.setVisibility(View.INVISIBLE);
				textView.setAlpha(1f);
			}

			@Override
			public void onAnimationCancel(Animator animation) {}
		});
        animatior.start();
	}

	private void hideQuickShareGridView() {
		mQuickShareZone.findViewById(R.id.quick_share_grid_view).setVisibility(View.INVISIBLE);
		mQuickShareZone.findViewById(R.id.quick_share_loading_tip).setVisibility(View.VISIBLE);
	}

	@Override
	public void onShareToApkFileSearchEndListener(ComponentName cn) {
		// TODO Auto-generated method stub
		startQuicShareGridViewAndSearchingTextViewChangeAnimate();
	}

    private boolean isQuickSharing = false;
    public boolean isQuickSharing() {
    	return isQuickSharing;
    }

	private Animator getMultiViewsTranslateAnimator(
			final View[] views,
			final int[] startXs, final int[] startYs,
			final int[] endXs, final int[] endYs,
			final QuickShareState state,
			final Runnable onAnimatorStartRunnable,
			final Runnable onAnimatorEndRunnable) {
		ValueAnimator translateAnimatior;
        translateAnimatior = ValueAnimator.ofFloat(0, 1f);
        translateAnimatior.setDuration(300);
        translateAnimatior.setInterpolator(new DecelerateInterpolator(2f));
        translateAnimatior.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                for (int i = 0; i < views.length; i++) {
                    int curX = (int) ((endXs[i] - startXs[i]) * value + startXs[i]);
                    int curY = (int) ((endYs[i] - startYs[i]) * value + startYs[i]);
                    views[i].setX(curX);
                    views[i].setY(curY);
                }
            }
        });

        translateAnimatior.addListener(new AnimatorListenerAdapter() {
        	@Override
			public void onAnimationStart(Animator animation) {
        		/*if(state == QuickShareState.None2Head || state == QuickShareState.Head2All) {
        			isQuickSharing = true;
        		}*/
        		if(null != onAnimatorStartRunnable) {
        			onAnimatorStartRunnable.run();
        		}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				/*if(state == QuickShareState.All2None || state == QuickShareState.Head2None) {
					isQuickSharing = false;
				}*/
				if(null != onAnimatorEndRunnable) {
					onAnimatorEndRunnable.run();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				super.onAnimationCancel(animation);
				for(int i=0; i<views.length; i++) {
					views[i].setX(endXs[i]);
	                views[i].setY(endYs[i]);
				}
			}
		});

		return translateAnimatior;
	}
	QuickShareState mPreviousQuickShareState = null; // Lenovo-SW zhaoxin5 20150714 add for LANCHROW-233
	public void startQuickShareStateChange(final QuickShareState state) {

		// HIDE_2_HEAD, call from QuickShareDropTarget.onDragEnter, to show the quick share zone head
		// HEAD_2_HIDE, call from QuickShareDropTarget.onDragExit, means the user drag an app over the
		//	                                                       quick share zone, did not want to share
		//                                                         we need to hide the quic share zone head
		// HEAD_2_BODY, call from QuickShareDropTarget.acceptDrop, means the user drop an app to the quick share zone,
		//                                                         so we will show the entire qucik zone to user
		// BODY_2_HIDE, call from Launcher.onBackPressed,          when the quick share zone is open, we need to close
		//                                                         it, when user pressed back
		//              call from QuickShareGridView.item,         when user choose an app to do the share work, we need
		//                                                         to hide the quick share zone

		if(mQuickShareAnimatorInfos == null) {
			setupQuickShareAnimatorInfos();
		}

		if(null != mQuickShareAnimator && mQuickShareAnimator.isRunning()) {
			mQuickShareAnimator.cancel();
		}

		final View views[] = getQuickShareAnimatorViews();
		final int startXs[] = getQuickShareAnimatorParams(QuickShareAnimatorParamType.START_X, state);
		final int startYs[] = getQuickShareAnimatorParams(QuickShareAnimatorParamType.START_Y, state);
		final int endXs[] = getQuickShareAnimatorParams(QuickShareAnimatorParamType.END_X, state);
		final int endYs[] = getQuickShareAnimatorParams(QuickShareAnimatorParamType.END_Y, state);

		Runnable onAnimatorStartRunnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mPreviousQuickShareState = state;
				if(state == QuickShareState.HIDE_2_HEAD || state == QuickShareState.HEAD_2_BODY) {
					// when we try to show head
					// or try to show all the quick share zone
					// now we should told the others
					// we are in the quick sharing state
        			isQuickSharing = true;
        		}
				if(state == QuickShareState.HEAD_2_BODY) {
					getDragLayer().setBackgroundColor(Launcher.this.getResources().getColor(R.color.half_transparent_black));
					mQuickShareZone.setVisibility(View.VISIBLE);
					mQuickShareZone.setAlpha(1f);
					mQuickShareZone.findViewById(R.id.quick_share_grid_view).setVisibility(View.INVISIBLE);
					mQuickShareZone.findViewById(R.id.quick_share_loading_tip).setVisibility(View.VISIBLE);
					((TextView)mQuickShareZone.findViewById(R.id.quick_share_loading_tip)).
						setText(getResources().getString(R.string.quick_share_searching_label));// Lenovo-SW zhaoxin5 20150702 LANCHROW-191
				} else if(state == QuickShareState.BODY_2_HIDE) {
					getDragLayer().setBackgroundColor(Launcher.this.getResources().getColor(android.R.color.transparent));
					mQuickShareZone.setVisibility(View.INVISIBLE);
					mQuickShareZone.setAlpha(0f);
					((TextView)mQuickShareZone.findViewById(R.id.quick_share_loading_tip)).
						setText("");// Lenovo-SW zhaoxin5 20150702 LANCHROW-191
				}
			}
		};

		Runnable onAnimatorEndRunnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mPreviousQuickShareState = null;
				if(state == QuickShareState.BODY_2_HIDE || state == QuickShareState.HEAD_2_HIDE) {
					// after we hide the quick share zone,
					// we are out the quick share state
					isQuickSharing = false;
				}
				if(state == QuickShareState.BODY_2_HIDE) {
					setFullScreen(false);
				}
			}
		};

		mQuickShareAnimator =
				getMultiViewsTranslateAnimator(views, startXs, startYs, endXs, endYs, state,
						onAnimatorStartRunnable, onAnimatorEndRunnable);
		mQuickShareAnimator.start();
	}

	enum QuickShareState {
		/**
		 *
		 *  Quick Share State transition see the pic below
		 *
		 *  HIDE -------------------------------------------------
		 *         ↓ <- HIDE_2_HEAD     ↑ <- HEAD_2_HIDE       ↑
		 *  HEAD --------------------------                    |
		 *         |                                           | <- BODY_2_HIDE
		 *         | <- HEAD_2_BODY                            |
		 *         ↓                                           |
		 *  BODY -------------------------------------------------
		 *
		 */
		HIDE_2_HEAD,
		HEAD_2_HIDE,
		HEAD_2_BODY,
		BODY_2_HIDE
	}

	enum QuickShareAnimatorParamType {
		START_X,
		START_Y,
		END_X,
		END_Y
	}

	private View[] getQuickShareAnimatorViews() {
		View rt[] = new View[mQuickShareAnimatorInfos.size()];
		int i = 0;
		for(QuickShareAnimateInfo info : mQuickShareAnimatorInfos) {
			rt[i] = info.animateView;
			i++;
		}
		return rt;
	}

	private boolean mNeedInterruptOnDragEnd = false;
	public boolean needInterruptOnDragEnd() {
		return mNeedInterruptOnDragEnd;
	}
	public void setNeedInterruptOnDragEnd(boolean b) {
		mNeedInterruptOnDragEnd = b;
	}

	private int[] getQuickShareAnimatorParams(QuickShareAnimatorParamType type, QuickShareState state) {

		int rt[] = new int[mQuickShareAnimatorInfos.size()];
		int i = 0;
		int temp = Integer.MAX_VALUE;
		for(QuickShareAnimateInfo info : mQuickShareAnimatorInfos) {
			switch(type){
				case START_X:
					temp = (int) info.getStartX(state);
					break;
				case START_Y:
					temp = (int) info.getStartY(state);
					break;
				case END_X:
					temp = (int) info.getEndX(state);
					break;
				case END_Y:
					temp = (int) info.getEndY(state);
					break;
			}
			rt[i] = temp;
			i++;
		}
		return rt;
	}

	private void setupQuickShareAnimatorInfos() {
		mQuickShareAnimatorInfos = new ArrayList<Launcher.QuickShareAnimateInfo>();
		mQuickShareAnimatorInfos.add(new QuickShareAnimateInfo(getWorkspace()));
		mQuickShareAnimatorInfos.add(new QuickShareAnimateInfo(getHotseat()));
        mQuickShareAnimatorInfos.add(new QuickShareAnimateInfo(mPageIndicators));
	}

	Animator mQuickShareAnimator = null;
	public boolean isQuickShareAnimatorRunning() {
		if(null == mQuickShareAnimator) {
			return false;
		}
		return mQuickShareAnimator.isRunning();
	}
	List<QuickShareAnimateInfo> mQuickShareAnimatorInfos = null;
	class QuickShareAnimateInfo {
		final boolean DEBUG = false;

		final View animateView;
		final int hide2Head;
		final int head2Hide;
		final int head2Body;
		final int body2Hide;

		final float originalY;
		final float originalX;

		QuickShareAnimateInfo(View view) {
			// the app image height and the "Share * to" text height
			int appImageHeightAddShareToTextHeight =
					mQuickShareZone.findViewById(R.id.quick_share_app_iv).getHeight() +
					mQuickShareZone.findViewById(R.id.quick_share_app_tip).getHeight();
			// whole quick share zone height
			int shareToZoneHeight = mQuickShareZone.getHeight();

			hide2Head = appImageHeightAddShareToTextHeight;
			head2Hide = -hide2Head;
			head2Body = shareToZoneHeight - appImageHeightAddShareToTextHeight;
			body2Hide = -shareToZoneHeight;

			animateView = view;
			originalY = animateView.getY();
			originalX = animateView.getX();

			if(DEBUG) {
				StringBuilder sb = new StringBuilder();
				sb.append(animateView);
				sb.append("{ ");
				sb.append("hide2Head: " + hide2Head + ", ");
				sb.append("head2Hide: " + head2Hide + ", ");
				sb.append("head2Body: " + head2Body + ", ");
				sb.append("body2Hide: " + body2Hide + ", ");
				sb.append("originalY: " + originalY + ", ");
				sb.append("originalX: " + originalX);
				sb.append(" }");
				Log.i("xixia", sb.toString());
			}
		}

		float getStartX(QuickShareState state) {
			return originalX;
		}

		float getEndX(QuickShareState state) {
			return getStartX(state);
		}

		float getStartY(QuickShareState state) {
			switch(state) {
				case HIDE_2_HEAD:
					return originalY;
				case HEAD_2_HIDE:
					return originalY + hide2Head;
				case HEAD_2_BODY:
					return originalY + hide2Head;
				case BODY_2_HIDE:
					return originalY + hide2Head + head2Body;
			}
			return 0;
		}

		float getEndY(QuickShareState state) {

			final float startY = getStartY(state);

			switch(state) {
				case HIDE_2_HEAD:
					return startY + hide2Head;
				case HEAD_2_HIDE:
					return startY + head2Hide;
				case HEAD_2_BODY:
					return startY + head2Body;
				case BODY_2_HIDE:
					return startY + body2Hide;
			}
			return 0;
		}
	}

    public Handler getHandler() {
        return mHandler;
    }

    public boolean shouldCloseFolderFirst() {
        Folder openFolder = mWorkspace.getOpenFolder();
        if (openFolder == null) {
            return false;
        } else {
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            }
            closeFolder();
            return true;
        }
    }

    Rect normalModeCellLayoutRect = new Rect();
    public void enterOverviewMode() {
        CellLayout layout = mWorkspace.getCurrentDropLayout();
        int[] normalModeCellLayoutPos = {0, 0};
        mDragLayer.getLocationInDragLayer(layout, normalModeCellLayoutPos);
        int width = layout.getWidth();
        int height = layout.getHeight();
        normalModeCellLayoutRect.set(normalModeCellLayoutPos[0], normalModeCellLayoutPos[1],
                width, height);
        
        /** Lenovo-SW zhaoxin5 20150805 XTHREEROW-585 START */
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
        if(LauncherAppState.getInstance().isCurrentVibeuiMode()) {
            /** Lenovo-SW luyy1 20150910 change for primary home screen START */
            bringChildOfDragLayerToFront("enterOverviewMode isCurrentVibeuiMode", mDockView, mOverviewPanel, mDefaultHomeScreenPanel);
            /** Lenovo-SW luyy1 20150910 change for primary home screen END */
        } else {
            /** Lenovo-SW luyy1 20150910 change for primary home screen START */
            bringChildOfDragLayerToFront("enterOverviewMode !isCurrentVibeuiMode", mOverviewPanelAndroid, mDefaultHomeScreenPanel);
            /** Lenovo-SW luyy1 20150910 change for primary home screen END */
        }
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
        /** Lenovo-SW zhaoxin5 20150805 XTHREEROW-585 END */
        /** Lenovo-SW luyy1 20150910 add for primary home screen START */
        if (mDefaultHomeScreenPanel != null) {
            mDefaultHomeScreenPanel.setVisibility(View.VISIBLE);
        }
        /** Lenovo-SW luyy1 20150910 add for primary home screen END */
    }

    public void exitOverviewMode() {
        if (isDockViewShowing && mDockView!=null) {
            mDockView.flyBackAllItems();
        }
        /** Lenovo-SW luyy1 20150910 add for primary home screen START */
        if (mDefaultHomeScreenPanel != null) {
            mDefaultHomeScreenPanel.setVisibility(View.INVISIBLE);
        }
        /** Lenovo-SW luyy1 20150910 add for primary home screen END */
    }

    // Lenovo-sw:yuanyl2, Add edit mode function. End.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Lenovo-sw:yuanyl2, Add loading dialog. Begin
    private Dialog mBootProgressDlg = null;

    public void showBootProgressDialog() {
        showBootProgressDialog(getResources().getString(R.string.loading_worksapce),
                R.style.Theme_Launcher_ProgressDialog);
    }

    public void showBootProgressDialog(final String msgText, final int theme) {
        if (mBootProgressDlg != null && mBootProgressDlg.isShowing()) {
            return;
        }

        mBootProgressDlg = new Dialog(this, theme);
        mBootProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBootProgressDlg.setContentView(R.layout.boot_custom_progressdialog);
        final View view = mBootProgressDlg.findViewById(R.id.dialog);
        final TextView msg = (TextView) view.findViewById(R.id.progress_msg);
        msg.setText(msgText);
        view.setBackgroundColor(Color.TRANSPARENT);
        mBootProgressDlg.setContentView(view);
        mBootProgressDlg.setCancelable(false);

        Window window = mBootProgressDlg.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
        setupTransparentSystemBarsForLmp(window);
        if (mBootProgressDlg.isShowing()) {
            return;
        }
        LauncherLog.d(TAG, "showBootProgressDialog");
        mBootProgressDlg.show();
    }

    public void dismissBootProgressDialog() {
        LauncherLog.d(TAG, "dismissBootProgressDialog");
        try {
            if (mBootProgressDlg != null && mBootProgressDlg.isShowing()) {
                mBootProgressDlg.dismiss();
            }
            mBootProgressDlg = null;
        } catch (Exception e) {
            mBootProgressDlg = null;
        }
    }

    public boolean isBootProgressDialogShowing() {
        if (mBootProgressDlg != null && mBootProgressDlg.isShowing()) {
            return true;
        }

        return false;
    }
    // Lenovo-sw:yuanyl2, Add loading dialog. Begin
    
    /** Lenovo-SW zhaoxin5 20150721 add for reorder START */
    
    private boolean mIsAnimating = false;
    private String mAnimateName;    
    public boolean isAnimating(boolean debug){    	
    	//Modified by Chandler Chen(chenchen23@lenovo.com)
         /*Lenovo-sw [yumina] [KSEVENROW-759] [2014-05-29] [start] fixed the NPE bug*/
        if(mWorkspace == null) return false;
        /*Lenovo-sw [yumina] [KSEVENROW-759] [2014-05-29] [end] fixed the NPE bug*/

    	if (debug)
    	{
    		LauncherLog.i(TAG, "mIsAnimating = " + mIsAnimating + "mAnimateName = " + mAnimateName + "mWorkspace.isPageMoving() = " + mWorkspace.isPageMoving());
    	}
    	boolean rt = mIsAnimating /*|| (mWorkspace.getVisibility() == View.VISIBLE && mWorkspace.isPageMoving())*/;
    	return rt;
    }
    
    public boolean isAnimating(){
    	return isAnimating(true);
    }
    
    public void setAnimating(boolean animate, String animateName){
    	LauncherLog.i(TAG, animateName + " setAnimating " + animate);
    	mIsAnimating = animate;
    	mAnimateName = animateName;
    	if (!mIsAnimating) {
    		forceAutoReorder();
    	}
    }
    
    void forceAutoReorder() {
    	if (mForceAutoFlag) {
    		mForceAutoFlag = false;
    		autoReorder("forceAutoReorder");
    	}
    }
    
    private boolean mForceAutoFlag = false;
    void setAutoReorderFlag(boolean flag) {
    	mForceAutoFlag = flag;
    }
    
    @Override
    public void autoReorder(String from) {
    	android.util.Log.i("xixia-autoReorder", "autoReorder : " + from);
    	LauncherLog.i(TAG, "autoReorder : " + from);
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
    	if(!isAnimating() && !mModel.isLoadingWorkspace() && 
    			LauncherAppState.getInstance().isCurrentVibeuiMode()) {
            /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
			autoReorder(true);
		}
    }
    
    private void autoReorder(final boolean showToast) {
    	
    	if(!SettingsValue.isAutoReorderEnabled(this)) {
    		return;
    	}
    	/*if (SettingsValue.getLayoutLock(this)) {
    		return;
    	}
        if (!enableAutoReorder){
            return;
        } 
    	boolean autoReorder = SettingsValue.isAutoReorderEnabled(Launcher.this); */ // Lenovo-SW zhaoxin5 20150721
    	if (/*!autoReorder // Lenovo_SW zhaoxin5 20150721
    			|| */mWorkspace == null) {
    		return;
    	}
    	
    	TaskRunnable runnable = new TaskRunnable() {
			@Override
			public void run() {
				autoReorder(showToast);
			}

			@Override
			public int getAnimFlag() {
				return TaskRunnable.FLAG_AUTO_REORDER;
			}
    		
    	};
    	// 判断当前Launcher的状态是否将要进入resume还是正在处于reorder之中
    	// 如果是的话,生成一个runnable,待上述任务完成以后
    	// 让上述生成的runnable跑起来
    	if (waitForResumeBindingOrReorder(runnable, true)) {
            return;
        }
    	
    	if (isWorkspaceLocked()) {
			return;
		}
    	
		if (isAnimating() && !mWorkspace.isLeosReordering()) {
			setAutoReorderFlag(true);
    		return;
    	}
		
		mWorkspace.autoReorder();
    }
    
	private static interface TaskRunnable extends Runnable {
		static final int FLAG_EMPTY = 0;
		static final int FLAG_MANUAL_REORDER = 1;
		static final int FLAG_AUTO_REORDER = 2;

		int getAnimFlag();
	}
	
    private boolean waitForResumeBindingOrReorder(final TaskRunnable taskRunnable, final boolean deletePrevious) {
    	// 判断当前Launcher的状态是否将要进入resume还是正在处于reorder之中
    	// 如果是的话,生成一个runnable,待上述任务完成以后
    	// 让上述生成的runnable跑起来
    	if (waitForResumeOrReorder(taskRunnable, deletePrevious)) {
    		return true;
    	}
    	// 如果当前要进行自动填空,但是Launcher尚未完成
    	// 绑定,因此需要在Launcher完成绑定以后再让
    	// 这个runnable 跑起来
    	if (waitUntilBindFinished(taskRunnable)) {
        	return true;
        }
    	return false;
    }
    
    private boolean waitForResumeOrReorder(final TaskRunnable taskRunnable, final boolean deletePrevious) {
    	// 判断Launcher是否将要进入resume状态
    	// 是的话,则在Launcher完成resume空闲之后在
    	// 将该runnable 跑起来
        if (waitUntilResume(taskRunnable)) {
            return true;
        }
        // 判断Launcher当前是否正在执行自动填空
    	// 是的话,则在Launcher跑完这一轮的自动填空之后再
    	// 将该runnable 跑起来
        if (waitUntilReorder(taskRunnable, deletePrevious)) {
        	return true;
        }
        return false;
    }
    
    private boolean waitUntilBindFinished(Runnable run) {
    	if (mModel == null) {
    		return false;
    	}
        if (mModel.isLoadingWorkspace()) {
        	mModifingRunnableList.add(run);
            return true;
        }
        return false;
    }
    
    private boolean waitUntilReorder(TaskRunnable run, boolean deletePrevious) {
    	if (mWorkspace == null) {
    		return false;
    	}
        if (mWorkspace.isLeosReordering()) {
            if (deletePrevious) {
                this.sortPendingRunnableList(run);
            } else {
            	mPendingRunnableList.add(run);
            }
            return true;
        }
        return false;
    }
    
    private static ArrayList<TaskRunnable> mPendingRunnableList
    	= new ArrayList<TaskRunnable>();    
    private static ArrayList<Runnable> mModifingRunnableList
        = new ArrayList<Runnable>();
    
    private void sortPendingRunnableList(final TaskRunnable orderRunnable) {
    	TaskRunnable runnable = null;
    	int index = -1;
    	int size = mPendingRunnableList.size();
    	for (int i = 0; i < size; i++) {
    		runnable = mPendingRunnableList.get(i);
    		if (runnable.getAnimFlag() == TaskRunnable.FLAG_AUTO_REORDER) {
    			index = i;
    			break;
    		}
    	}
    	if (index != -1) {
    		mPendingRunnableList.remove(index);
    		mPendingRunnableList.add(runnable);
    	} else {
    		mPendingRunnableList.add(orderRunnable);
    	}
    }
    
	private ReorderActor.ReorderingChangedListener mReorderChangedListener = new ReorderActor.ReorderingChangedListener() {
		@Override
		public void onReorderEnd() {
			Launcher.this.setAnimating(false, "reorder");
			LauncherLog.i(TAG, "---------->onReorderEnd----------------");
			handlePendingRunnable();
		}
	};
	
    public void handlePendingRunnable() {
    	if (mWorkspace != null && mWorkspace.isLeosReordering()) {
    		return;
    	}
    	boolean isEmpty = mPendingRunnableList.isEmpty();
    	
    	Iterator<TaskRunnable> it = mPendingRunnableList.iterator();
    	TaskRunnable runnable = null;
    	while (it.hasNext()) {
    		runnable = it.next();
    		runnable.run();
    		it.remove();
    		it = mPendingRunnableList.iterator();
    		if (runnable.getAnimFlag() != TaskRunnable.FLAG_EMPTY) {
    			return;
    		}
    	}
    	
    	if (!isEmpty && runnable != null && runnable.getAnimFlag() == TaskRunnable.FLAG_EMPTY) {
    		new Runnable() {
    			@Override
    			public void run() {
    		        autoReorder("handlePendingRunnable");
    			}
    			
    		}.run();
    	}
		mPendingRunnableList.clear();
    }
    
    @Override
    public void finishLoadingAndBindingWorkspace() {
    	if (mModifingRunnableList.size() > 0) {
            for (int i = 0; i < mModifingRunnableList.size(); i++) {
            	mModifingRunnableList.get(i).run();
            }
            mModifingRunnableList.clear();
        }
    }
    /** Lenovo-SW zhaoxin5 20150721 add for reorder END */

    // LENOVO-SW:YUANYL2, Add loading progress function. Begin
    @Override
    public void beforeBinding() {
    	LauncherLog.i(TAG, "beforeBinding");
        //显示加载 dialog
        showBootProgressDialog();
    }

    @Override
    public void afterBinding() {
    	LauncherLog.i(TAG, "afterBinding");
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
    	updateVibeuiAndAndroidOverviewPanel();
        /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
    	/** Lenovo-SW zhaoxin5 20150916 KOLEOSROW-2238 KOLEOSROW-698 START */
    	mModel.sendCheckUnreadBroadcast("afterBinding");
    	/** Lenovo-SW zhaoxin5 20150916 KOLEOSROW-2238 KOLEOSROW-698 END */

    	if(ModeSwitchHelper.isFirstLoad(this)) {
        	/**
        	 * 设置主屏幕
        	 * 只有在当前Style首次加载的时候
        	 * 才能执行设置主屏的操作
        	 * */
	    	if(LauncherAppState.getInstance().isCurrentVibeuiMode()) {
		    	LauncherLog.i(TAG, "default page for vibeui mode : " + SettingsValue.getDefaultPage(this));
		    	mWorkspace.setDefaultPage(SettingsValue.getDefaultPage(this));
                mWorkspace.moveToDefaultScreen(true);
	    	} else {
		    	LauncherLog.i(TAG, "default page for android mode : 0");
		    	mWorkspace.setDefaultPage(0);
	    	}
    	}
        ModeSwitchHelper.onLauncherAfterBinding(this);
    	autoReorder("afterBinding");
    }

    /** Lenovo-SW zhaoxin5 20150909 Launcher 启动优化 START */
    @Override
    public void afterBindingCurrentPage() {
    	LauncherLog.i(TAG, "afterBindingCurrentPage");
        dismissBootProgressDialog();
    }
    /** Lenovo-SW zhaoxin5 20150909 Launcher 启动优化 END */

    /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 START */
    private void updateVibeuiAndAndroidOverviewPanel() {
    	if(LauncherAppState.getInstance().isCurrentAndroidMode()) {
    		LauncherLog.i(TAG, "updateVibeuiAndAndroidOverviewPanel.isCurrentAndroidMode");
    		if (!mWorkspace.isInOverviewMode()) {
        		LauncherLog.i(TAG, "updateVibeuiAndAndroidOverviewPanel.isCurrentAndroidMode.!mWorkspace.isInOverviewMode");
    			mOverviewPanelAndroid.setVisibility(View.INVISIBLE);
    		}
			mOverviewPanel.setVisibility(View.GONE);
    		mDockView.setVisibility(View.GONE);
    	} else {
    		LauncherLog.i(TAG, "updateVibeuiAndAndroidOverviewPanel.!isCurrentAndroidMode");
    		if (!mWorkspace.isInOverviewMode()) {
        		LauncherLog.i(TAG, "updateVibeuiAndAndroidOverviewPanel.!isCurrentAndroidMode.!mWorkspace.isInOverviewMode");
    			mOverviewPanel.setVisibility(View.INVISIBLE);
        		mDockView.setVisibility(View.INVISIBLE);
    		}
			mOverviewPanelAndroid.setVisibility(View.GONE);
    	}
    }
    /** Lenovo-SW zhaoxin5 20150817 XTHREEROW-876 END */
    // LENOVO-SW:YUANYL2, Add loading progress function. End

    /** Lenovo-SW zhaoxin5 20150813 add for KOLEOSROW-580 START */
    @Override
    public void loadSettingsAndFolderTitlesFromLbk() {
    	if(SettingsValue.isFirstLoadSettings(this)) {
    		// 第一次从LBK中读取设置的属性
    		LauncherLog.i("LbkSettingsLoader", "loadSettingsFromLbk");
	    	LbkSettingsLoader loader = LbkSettingsLoader.get(this, LauncherAppState.getInstance().getLauncherProvider());
	    	if(null != loader) {
	    		loader.parseSettings();
	    	} else {
	            LauncherLog.i(TAG, "loader == null, set first_load flag false");
	            // SettingsValue.setFirstLoadSettings(this, false);
	    	}
	    	
	    	/** Lenovo-SW zhaoxin5 20150924 KOLEOSROW-2601【评测】【运营商版本】通过差分包方式升级至运营商版本表现与文档不一致 START */
	    	// 第一次从LBK中读取并设置默认壁纸
	    	LbkWallpaperLoader wallpaperLoader = LbkWallpaperLoader.get(this);
	    	if (null != wallpaperLoader) {
	    		wallpaperLoader.applyDefaultWallpaper();
	    	} else {
	            LauncherLog.i(TAG, "can not read default wallpaper from lbk");
	    	}
	    	/** Lenovo-SW zhaoxin5 20150924 KOLEOSROW-2601【评测】【运营商版本】通过差分包方式升级至运营商版本表现与文档不一致 END */
    	}
    }
    /** Lenovo-SW zhaoxin5 20150813 add for KOLEOSROW-580 END */

    /** Lenovo-sw luyy1 add 20150819 for wallpaper listener start*/
    public void updateWallpaperItem(int id) {
        if (id <= LauncherModel.WALLPAPER_OTHER_THEME_SOURCE) {
            mWallpaperListPanel.reloadItemAndUpdateEditPoint(true, id);
        } else {
            mWallpaperListPanel.reloadItemAndUpdateEditPoint(false, id);
        }
    }

    public boolean isApkWallpaperItemClick() {
       if (mWallpaperListPanel.ismIsClickApk()) {
           mWallpaperListPanel.setmIsClickApk(false);
           return true;
       }
       return false;
    }

    public void reloadApkWallpaper() {
        if (mWallpaperListPanel != null) {
            mWallpaperListPanel.update();
        }
    }

    /** Lenovo-sw luyy1 add 20150819 for wallpaper listener end*/
    /*Lenovo-sw zhangyj19 add 2015/09/06 add search app modify start*/
	public ArrayList<AppInfo> getSearchItems(String content) {
		if(mModel != null){
			return mModel.mBgAllAppsList.getSearchItems(content);
		} else {
			return null;
		}
	}
    /*Lenovo-sw zhangyj19 add 2015/09/06 add search app modify end*/

    public void startPickAppWidget(){
        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }
}

interface LauncherTransitionable {
    View getContent();

    void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace);

    void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace);

    void onLauncherTransitionStep(Launcher l, float t);

    void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace);
}

interface DebugIntents {
    static final String DELETE_DATABASE = "com.android.launcher3.action.DELETE_DATABASE";
    static final String MIGRATE_DATABASE = "com.android.launcher3.action.MIGRATE_DATABASE";
}
