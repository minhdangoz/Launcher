package com.android.launcher3;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.settings.SettingsValue;
import com.klauncher.ext.LauncherLog;
import com.klauncher.launcher.R;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

interface OnWidgetListClickListener{
	void onClick(View v);
	void onLongClick(View v);
}

/************************************* 
 * 2) Second module
 *    Widget Preview Loader
 * 
 * 	  START 
 *************************************/

/*************************************
 * 2) Second module
 *    END 
 *************************************/

class DirectlyShowWidgetInfo extends AppInfo {
	DirectlyShowWidgetInfo(Resources res, String pkgName, String title) {
		this.iconBitmap = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_home);
		this.componentName = new ComponentName(pkgName, pkgName);
		this.title = title;
	}
}

public class WidgetListViewSwitcher extends ViewSwitcher implements DragSource, IEditEntry{
	
	private OnWidgetListClickListener mClickListener = new OnWidgetListClickListener() {
		@Override
		public void onClick(View v) {
			LauncherLog.i(TAG, "onClick");
			// TODO Auto-generated method stub
			Object o = v.getTag();
			if (null == o)
				return;

			if (o instanceof AppInfo && !isDirectlyShowWidget((AppInfo) o)) {
				AppInfo a = (AppInfo) o;
				LauncherLog.i(TAG, a.componentName.flattenToShortString());
				// 要切换到第二层
				final ArrayList<Object> csl = mAllSecondList.get(a.componentName.getPackageName());
				mSecondListRecyclerView.updateAdapterForSecond(a, csl,
						mClickListener, getWidgetPreviewLoader());
				showNextType();
			} else {
				onSingleClick(v);  
			}
		}

		@Override
		public void onLongClick(View v) {
			onLongClickWidget(v);
		}
	};
	
	private PendingAddWidgetInfo getPendingAddWidgetInfo(AppWidgetProviderInfo aInfo) {
		PendingAddWidgetInfo createItemInfo = new PendingAddWidgetInfo(
				aInfo, null, null);
		// Determine the widget spans and min resize spans.
		int[] spanXY = Launcher.getSpanForWidget(mLauncher, aInfo);
		createItemInfo.spanX = spanXY[0];
		createItemInfo.spanY = spanXY[1];
		int[] minSpanXY = Launcher.getMinSpanForWidget(mLauncher, aInfo);
		createItemInfo.minSpanX = minSpanXY[0];
		createItemInfo.minSpanY = minSpanXY[1];
		createItemInfo.title = aInfo.label;
		return createItemInfo;
	}
	
	private PendingAddShortcutInfo getPendingAddShortcutInfo(ResolveInfo info) {
		PendingAddShortcutInfo createItemInfo = new PendingAddShortcutInfo(
				info.activityInfo);
		createItemInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
		createItemInfo.componentName = new ComponentName(
				info.activityInfo.packageName, info.activityInfo.name);
		createItemInfo.title = info.loadLabel(mPackageManager);
		return createItemInfo;
	}
	
	public boolean onBackPressed(){
		if(mCurrentType == ShowType.FIRST_APP) {
			// 如果当前处于第一层,直接退出编辑模式
			return false;	
		}
		showNextType();
		return true;
	}

	@Override
	public boolean onHomePressed() {
		if(mCurrentType == ShowType.FIRST_APP) {
			// 如果当前处于第一层,直接退出编辑模式
			return false;	
		}
		showNextType();
		return false;
	}

	private void showNextType() {
		this.showNext();
		if(mCurrentType == ShowType.FIRST_APP) {
			mCurrentType = ShowType.SECOND_WIDGET;
		} else {
			mCurrentType = ShowType.FIRST_APP;
		}
	}
	
	private ShowType mCurrentType = ShowType.FIRST_APP;
	enum ShowType { // 当前正在显示的层次
		FIRST_APP,
		SECOND_WIDGET
	}
	
	/************************************* 
	 * 0) Zero module
	 *    Initialization
	 * 
	 * 	  START 
	 *************************************/	
	final static String TAG = WidgetListViewSwitcher.class.getSimpleName();
	private Context mContext = null;
	private Launcher mLauncher;
    private DragController mDragController;
	
	private WidgetListHorizontalRecyclerView mFirstListRecyclerView = null;
	private WidgetListHorizontalRecyclerView mSecondListRecyclerView = null;	
	
	public WidgetListViewSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setupViews(context);
	}

	public WidgetListViewSwitcher(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setupViews(context);
	}
	
	void setupViews(Context context){
		mContext = context;
        mIconCache = (LauncherAppState.getInstance()).getIconCache();
		
		// 初始化
        mWidgets = new ArrayList<Object>();
        mApps = new ArrayList<AppInfo>();
        mPackageManager = context.getPackageManager();
        mWidgetSpacingLayout = new PagedViewCellLayout(getContext());
        
        setup2ListViews();
	}
	
    private void setup2ListViews() {
    	mFirstListRecyclerView = (WidgetListHorizontalRecyclerView) this.findViewById(R.id.widget_list_first);
    	// mFirstListRecyclerView.setOnWidgetListClickListener(mClickListener);
    	
        mSecondListRecyclerView = (WidgetListHorizontalRecyclerView) this.findViewById(R.id.widget_list_second);
        // mSecondListRecyclerView.setOnWidgetListClickListener(mClickListener);
    }
    
	public void setup(Launcher launcher, DragController dragController) {
		mLauncher = launcher;
        mDragController = dragController;
	} 
	/*************************************
	 * 0) Zero module
	 *    END 
	 *************************************/	
	
	
	/************************************* 
	 * 1) First module
	 *    Sync apps and widgets data
	 * 
	 * 	  START 
	 *************************************/	
	
	// the list to hold the widgets, which will show directory in the first layer
	private ArrayList<DirectlyShowWidgetInfo> mDirectlyShowWidget = new ArrayList<DirectlyShowWidgetInfo>();
	private void initDirectlyShowWidgetList() {
		mDirectlyShowWidget.clear();
		// Lenovo Weather
		DirectlyShowWidgetInfo aInfo = new DirectlyShowWidgetInfo(getResources(), LEWEATHER_PACKAGE, "Lenovo Weather");
		mDirectlyShowWidget.add(aInfo);
		// Magic Search
		aInfo = new DirectlyShowWidgetInfo(getResources(), MAGIC_SEARCH_PACKAGE, "Magic Search");
		mDirectlyShowWidget.add(aInfo);
	}
	
	public static boolean isDirectlyShowWidget(AppInfo a) {
		return (a instanceof DirectlyShowWidgetInfo);
	}
	
	private final static String LEWEATHER_PACKAGE = "com.lenovo.lewea";
	private final static String MAGIC_SEARCH_PACKAGE = "com.magic.googlesearch.com.google.android.googlequicksearchbox";
	private ArrayList<Object> mWidgets;
	private ArrayList<AppInfo> mApps;
	private PackageManager mPackageManager;
    private ArrayList<AppInfo> mAllFirstList = new ArrayList<AppInfo>();
    
    // 存放所有第二层的wiget的信息
    private HashMap<String, ArrayList<Object>> mAllSecondList = new HashMap<String, ArrayList<Object>>();

	static final String GOOGLE_SEARCH_PACKAGE = "com.google.android.googlequicksearchbox";
	static final String GOOGLE_SEARCH_CLASSNAME = "com.google.android.googlequicksearchbox.SearchActivity";
	static final String GOOGLE_VOICE_SEARCH_CLASSNAME = "com.google.android.googlequicksearchbox.VoiceSearchActivity";
	static final String HUAWEI_WEATHER_PACKAGE = "com.huawei.android.totemweather";
    
	public void setApps(ArrayList<AppInfo> list) {
		/** Lenovo-SW zhaoxin5 20150921 KOLEOSROW-2589 START */
		/**
		 * 这个bug是俄罗斯场测说WidgetList的列表中,google now所处的那个分组使用的是
		 * Google Voice的图标,因此移除Google Voice的AppInfo,使用Google Search的图标
		 */
		mApps = (ArrayList<AppInfo>) list.clone();
		// 准备显示
		int voiceIndex = -1;
		int searchIndex = -1;
		int weatherIndex = -1;
		for (int i = 0; i < mApps.size(); i++) {
			AppInfo aInfo = mApps.get(i);
			if (null != aInfo.componentName) {
				if (aInfo.componentName.getPackageName().equals(HUAWEI_WEATHER_PACKAGE)) {
					weatherIndex = i;
				}
				if (aInfo.componentName.getPackageName().equals(GOOGLE_SEARCH_PACKAGE) &&
						aInfo.componentName.getClassName().equals(GOOGLE_VOICE_SEARCH_CLASSNAME)) {
					voiceIndex = i;
				}
				if (aInfo.componentName.getPackageName().equals(GOOGLE_SEARCH_PACKAGE) &&
						aInfo.componentName.getClassName().equals(GOOGLE_SEARCH_CLASSNAME)) {
					searchIndex = i;
				}
			}
		}
		if ((voiceIndex >= 0) && (searchIndex >= 0)) {
			mApps.remove(voiceIndex);
		}
		if (weatherIndex >= 0) {
			mApps.remove(weatherIndex);
		}
		/** Lenovo-SW zhaoxin5 20150921 KOLEOSROW-2589 END */
	}

	/**
	 * @category Same as AppsCustomizePagedView
	 * 
	 * @param widgetsAndShortcuts
	 */
	public void onPackagesUpdated(ArrayList<Object> widgetsAndShortcuts) {
		long start = System.currentTimeMillis();
		
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

		// Get the list of widgets and shortcuts
		mWidgets.clear();
		for (Object o : widgetsAndShortcuts) {
			if (o instanceof AppWidgetProviderInfo) {
				AppWidgetProviderInfo widget = (AppWidgetProviderInfo) o;
				if (!app.shouldShowAppOrWidgetProvider(widget.provider)) {
					continue;
				}
				if (widget.minWidth > 0 && widget.minHeight > 0) {
					// Ensure that all widgets we show can be added on a
					// workspace of this size
					int[] spanXY = Launcher.getSpanForWidget(mLauncher, widget);
					int[] minSpanXY = Launcher.getMinSpanForWidget(mLauncher,
							widget);
					int minSpanX = Math.min(spanXY[0], minSpanXY[0]);
					int minSpanY = Math.min(spanXY[1], minSpanXY[1]);
					if (minSpanX <= (int) grid.numColumns
							&& minSpanY <= (int) grid.numRows) {
						mWidgets.add(widget);
					} else {
						LauncherLog.e(TAG, "Widget " + widget.provider
								+ " can not fit on this device ("
								+ widget.minWidth + ", " + widget.minHeight
								+ ")");
					}
				} else {
					LauncherLog.e(TAG, "Widget " + widget.provider
							+ " has invalid dimensions (" + widget.minWidth
							+ ", " + widget.minHeight + ")");
				}
			} else {
				// just add shortcuts
				mWidgets.add(o);
			}
		}	
		filterWidgets();
		
		long end = System.currentTimeMillis();
		LauncherLog.i(TAG, "WidgetList onPackagesUpdated cost time : " + (end - start));
	}
	
	public void filterWidgets() {
		// TODO 在这里显示widget
		// clear mAllSecondList and  mAllSecondList
		mAllFirstList.clear();
		mAllSecondList.clear();
		initDirectlyShowWidgetList();
		/** Lenovo-SW zhaoxin5 20151015 XTHREEROW-2364 START */
		final ArrayList<AppInfo> allApps = (ArrayList<AppInfo>) mApps.clone();
		/** Lenovo-SW zhaoxin5 20151015 XTHREEROW-2364 END */
		
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				/** Lenovo-SW zhaoxin5 20151015 XTHREEROW-2364 START */
				syncWidgetPageItems(allApps);
				/** Lenovo-SW zhaoxin5 20151015 XTHREEROW-2364 END */
				return null;
			}
			@Override
			protected void onPostExecute(Void result){
		        // 检索
		        update();
			}
		}.execute();

	}
		
	public void syncWidgetPageItems(ArrayList<AppInfo> allApps) { /** Lenovo-SW zhaoxin5 20151015 XTHREEROW-2364 START */
		// 初始化Widget 的Preview loader的信息
		getWidgetPreviewLoader().setPreviewSize(mLauncher.getResources().getDimensionPixelSize(R.dimen.widget_list_imageview_size), 
				mLauncher.getResources().getDimensionPixelSize(R.dimen.widget_list_imageview_size), mWidgetSpacingLayout);
        
		for (int i = 0; i < mWidgets.size(); ++i) {
            Object rawInfo = mWidgets.get(i);
            PendingAddItemInfo createItemInfo = null;
            if (rawInfo instanceof AppWidgetProviderInfo) {
                // Fill in the widget information
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) rawInfo;
                createItemInfo = new PendingAddWidgetInfo(info, null, null);

                // Determine the widget spans and min resize spans.
                int[] spanXY = Launcher.getSpanForWidget(mLauncher, info);
                createItemInfo.spanX = spanXY[0];
                createItemInfo.spanY = spanXY[1];
                int[] minSpanXY = Launcher.getMinSpanForWidget(mLauncher, info);
                createItemInfo.minSpanX = minSpanXY[0];
                createItemInfo.minSpanY = minSpanXY[1];
                createItemInfo.title = info.label;
                
                // 提前加载Widget的Preview信息
                getWidgetPreviewLoader().getPreview(rawInfo);
            } else if (rawInfo instanceof ResolveInfo) {
                // Fill in the shortcuts information
                ResolveInfo info = (ResolveInfo) rawInfo;
                createItemInfo = new PendingAddShortcutInfo(info.activityInfo);
                createItemInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
                createItemInfo.componentName = new ComponentName(info.activityInfo.packageName,
                        info.activityInfo.name);
                createItemInfo.title = info.loadLabel(mPackageManager);
            }
            if(createItemInfo != null && createItemInfo.componentName != null) {
            	if(!mAllSecondList.containsKey(createItemInfo.componentName.getPackageName())) {
                	for(AppInfo a : allApps) { /** Lenovo-SW zhaoxin5 20151015 XTHREEROW-2364 START */
                		if(a.componentName != null && 
                				a.componentName.getPackageName().equals(
                						createItemInfo.componentName.getPackageName())) {
                			mAllSecondList.put(a.componentName.getPackageName(), new ArrayList<Object>());
                			mAllFirstList.add(a);
                			break;
                		}
                	}
            	}
                /*Lenovo-sw zhangyj19 add 2015/09/06 add search app modify start*/
                boolean isSearchIcon = createItemInfo.componentName.getPackageName().equals("com.lenovo.olauncher");
                if(isSearchIcon){
                    boolean currentSearchApp = SettingsValue.isSearchApp(getContext());
                    isSearchIcon = isSearchIcon & currentSearchApp;
                }
                final String settingsPkg = "com.android.settings";
                if(isSearchIcon) {
                   if(!mAllSecondList.containsKey(settingsPkg)) {
                       for(AppInfo a : allApps) { /** Lenovo-SW zhaoxin5 20151015 XTHREEROW-2364 START */
                            if(a.componentName != null &&
                                   a.componentName.getPackageName().equals(settingsPkg)) {
                                mAllSecondList.put(a.componentName.getPackageName(), new ArrayList<Object>());
                                mAllFirstList.add(a);
                                break;
                            }
                       }
                   }
               }
                /*Lenovo-sw zhangyj19 add 2015/09/06 add search app modify end*/
            	// 找到联想天气和Google Search Widget
            	// 然后将它们添加到列表的最前方
            	for(int k=0; k<mDirectlyShowWidget.size(); k++) {
            		AppInfo aInfo = mDirectlyShowWidget.get(k);
            		if(aInfo.componentName.getPackageName().equals(createItemInfo.componentName.getPackageName())) {
                		AppWidgetProviderInfo wInfo = (AppWidgetProviderInfo) rawInfo;
    					aInfo.iconBitmap = getWidgetPreviewLoader().getPreview(wInfo);
    					aInfo.title = wInfo.label;
    					
    					mAllSecondList.put(aInfo.componentName.getPackageName(), new ArrayList<Object>());
            	        mAllFirstList.add(0, aInfo);
            		}
            	}
                /*Lenovo-sw zhangyj19 add 2015/09/06 add search app modify start*/
                if (isSearchIcon) {
                    if(!mAllSecondList.containsKey(settingsPkg)) {
                        continue;
                    }
                } else if(!mAllSecondList.containsKey(createItemInfo.componentName.getPackageName())) {
                    continue;
                }

                if (isSearchIcon) {
                    ArrayList<Object> list = mAllSecondList.get(settingsPkg);
                    LauncherLog.i(TAG, "    " + createItemInfo.componentName.getPackageName());
                    list.add(rawInfo);
                } else {
                    ArrayList<Object> list = mAllSecondList.get(createItemInfo.componentName.getPackageName());
                    LauncherLog.i(TAG, "    " + createItemInfo.componentName.getPackageName());
                    list.add(rawInfo);
                }
                /*Lenovo-sw zhangyj19 add 2015/09/06 add search app modify end*/
            }
        }

    }
	
    private void update(){
    	if(mFirstListRecyclerView == null || mSecondListRecyclerView == null){
    		setup2ListViews();
    	}
    	// 先显示第一层
    	mFirstListRecyclerView.updateAdapterForFirst(mAllFirstList, mAllSecondList, mClickListener);
    }	
	/*************************************
	 * 1) First module
	 *    END 
	 *************************************/	

	/************************************* 
	 * 2) Second module
	 *    Widget Preview Loader
	 * 
	 * 	  START 
	 *************************************/
	WidgetPreviewLoader mWidgetPreviewLoader;
	private PagedViewCellLayout mWidgetSpacingLayout;

	WidgetPreviewLoader getWidgetPreviewLoader() {
		if (mWidgetPreviewLoader == null) {
			mWidgetPreviewLoader = new WidgetPreviewLoader(mLauncher);
		}
		return mWidgetPreviewLoader;
	}

	/*************************************
	 * 2) Second module
	 *    END 
	 *************************************/    



	/************************************* 
	 * 3) Third module
	 *    Long Click to add Widget
	 * 
	 * 	  START 
	 *************************************/
    private boolean mDraggingWidget = false;
    PendingAddWidgetInfo mCreateWidgetInfo = null;    
    // Caching
    private IconCache mIconCache;
    private static Rect sTmpRect = new Rect();
    static final int WIDGET_NO_CLEANUP_REQUIRED = -1;
    static final int WIDGET_PRELOAD_PENDING = 0;
    static final int WIDGET_BOUND = 1;
    static final int WIDGET_INFLATED = 2;
    int mWidgetCleanupState = WIDGET_NO_CLEANUP_REQUIRED;
    private Runnable mInflateWidgetRunnable = null;
    private Runnable mBindWidgetRunnable = null;
    int mWidgetLoadingId = -1;
	
	private void onLongClickWidget(View v) {
		LauncherLog.i(TAG, "onLongClick");
		// TODO Auto-generated method stub
		Object o = v.getTag();
		if (null == o)
			return;
		if(o instanceof AppWidgetProviderInfo) {
			LauncherLog.i(TAG, "AppWidgetProviderInfo onLongClick");
			onShortPress(v, getPendingAddWidgetInfo((AppWidgetProviderInfo)o));
			beginDragging(v, getPendingAddWidgetInfo((AppWidgetProviderInfo)o));
		} else if(o instanceof ResolveInfo) {
			beginDragging(v, getPendingAddShortcutInfo((ResolveInfo)o));
		}
	}
	
	/**
	 * @category Same as AppsCustomizePagedView
	 * 
	 * @param v
	 * @param pInfo
	 */
    public void onShortPress(View v, PendingAddWidgetInfo pInfo) {
        // We are anticipating a long press, and we use this time to load bind and instantiate
        // the widget. This will need to be cleaned up if it turns out no long press occurs.
        if (mCreateWidgetInfo != null) {
            // Just in case the cleanup process wasn't properly executed. This shouldn't happen.
            cleanupWidgetPreloading(false);
        }
        mCreateWidgetInfo = pInfo;
        preloadWidget(mCreateWidgetInfo);
    }
    
    /**
     * @category Same as AppsCustomizePagedView
     * 
     * @param v
     * @param pInfo
     * @return
     */
    protected boolean beginDragging(final View v, PendingAddItemInfo pInfo) {
        // if (!super.beginDragging(v)) return false;

        if (v instanceof BubbleTextView) {
            // beginDraggingApplication(v);
        } else {
            if (!beginDraggingWidget(v, pInfo)) {
                return false;
            }
        }

        // We delay entering spring-loaded mode slightly to make sure the UI
        // thready is free of any work.
        /** Lenovo-SW zhaoxin5 20150707 add for LANCHROW-195 START */
        /*postDelayed(new Runnable() {
            @Override
            public void run() {
                // We don't enter spring-loaded mode if the drag has been cancelled
                if (mLauncher.getDragController().isDragging()) {
                    // Go into spring loaded mode (must happen before we startDrag())
                    mLauncher.enterSpringLoadedDragMode(true);
                }
            }
        }, 150);*/
        /** Lenovo-SW zhaoxin5 20150707 add for LANCHROW-195 END */

        return true;
    }
    
    /**
     * @category Same as AppsCustomizePagedView
     * 
     * @param v
     * @param pInfo
     * @return
     */
    private boolean beginDraggingWidget(View v, PendingAddItemInfo pInfo) {
        mDraggingWidget = true;
        // Get the widget preview as the drag representation
        ImageView image = (ImageView) v.findViewById(R.id.image_view);
        PendingAddItemInfo createItemInfo = pInfo;

        // If the ImageView doesn't have a drawable yet, the widget preview hasn't been loaded and
        // we abort the drag.
        if (image.getDrawable() == null) {
            mDraggingWidget = false;
            return false;
        }

        // Compose the drag image
        Bitmap preview;
        Bitmap outline;
        float scale = 1f;
        Point previewPadding = null;
        if (createItemInfo instanceof PendingAddWidgetInfo) {
            // This can happen in some weird cases involving multi-touch. We can't start dragging
            // the widget if this is null, so we break out.
            if (mCreateWidgetInfo == null) {
                return false;
            }

            PendingAddWidgetInfo createWidgetInfo = mCreateWidgetInfo;
            createItemInfo = createWidgetInfo;
            int spanX = createItemInfo.spanX;
            int spanY = createItemInfo.spanY;
            int[] size = mLauncher.getWorkspace().estimateItemSize(spanX, spanY,
                    createWidgetInfo, true);

            FastBitmapDrawable previewDrawable = (FastBitmapDrawable) image.getDrawable();
            float minScale = 1.25f;
            int maxWidth, maxHeight;
            maxWidth = Math.min((int) (previewDrawable.getIntrinsicWidth() * minScale), size[0]);
            maxHeight = Math.min((int) (previewDrawable.getIntrinsicHeight() * minScale), size[1]);

            int[] previewSizeBeforeScale = new int[1];

            preview = getWidgetPreviewLoader().generateWidgetPreview(createWidgetInfo.info,
                    spanX, spanY, maxWidth, maxHeight, null, previewSizeBeforeScale);

            // Compare the size of the drag preview to the preview in the AppsCustomize tray
            int previewWidthInAppsCustomize = Math.min(previewSizeBeforeScale[0],
                    getWidgetPreviewLoader().maxWidthForWidgetPreview(spanX));
            scale = previewWidthInAppsCustomize / (float) preview.getWidth();

            // The bitmap in the AppsCustomize tray is always the the same size, so there
            // might be extra pixels around the preview itself - this accounts for that
            if (previewWidthInAppsCustomize < previewDrawable.getIntrinsicWidth()) {
                int padding =
                        (previewDrawable.getIntrinsicWidth() - previewWidthInAppsCustomize) / 2;
                previewPadding = new Point(padding, 0);
            }
        } else {
        	// FIXME zhaoxin5 
            PendingAddShortcutInfo createShortcutInfo = (PendingAddShortcutInfo) pInfo;
            Drawable icon = mIconCache.getFullResIcon(createShortcutInfo.shortcutActivityInfo);
            preview = Utilities.createIconBitmap(icon, mLauncher);
            createItemInfo.spanX = createItemInfo.spanY = 1;
        }

        // Don't clip alpha values for the drag outline if we're using the default widget preview
        boolean clipAlpha = !(createItemInfo instanceof PendingAddWidgetInfo &&
                (((PendingAddWidgetInfo) createItemInfo).previewImage == 0));

        // Save the preview for the outline generation, then dim the preview
        outline = Bitmap.createScaledBitmap(preview, preview.getWidth(), preview.getHeight(),
                false);

        // Start the drag
        mLauncher.lockScreenOrientation();
        mLauncher.getWorkspace().onDragStartedWithItem(createItemInfo, outline, clipAlpha);
        mDragController.startDrag(image, preview, this, createItemInfo,
                DragController.DRAG_ACTION_COPY, previewPadding, scale);
        outline.recycle();
        preview.recycle();
        return true;
    }
    
    /**
     * @category Same as AppsCustomizePagedView
     * 
     * @param info
     */
    private void preloadWidget(final PendingAddWidgetInfo info) {
        final AppWidgetProviderInfo pInfo = info.info;
        final Bundle options = getDefaultOptionsForWidget(mLauncher, info);

        if (pInfo.configure != null) {
            info.bindOptions = options;
            return;
        }

        mWidgetCleanupState = WIDGET_PRELOAD_PENDING;
        mBindWidgetRunnable = new Runnable() {
            @Override
            public void run() {
                mWidgetLoadingId = mLauncher.getAppWidgetHost().allocateAppWidgetId();
                if(AppWidgetManagerCompat.getInstance(mLauncher).bindAppWidgetIdIfAllowed(
                        mWidgetLoadingId, pInfo, options)) {
                    mWidgetCleanupState = WIDGET_BOUND;
                }
            }
        };
        post(mBindWidgetRunnable);

        mInflateWidgetRunnable = new Runnable() {
            @Override
            public void run() {
                if (mWidgetCleanupState != WIDGET_BOUND) {
                    return;
                }
                AppWidgetHostView hostView = mLauncher.
                        getAppWidgetHost().createView(getContext(), mWidgetLoadingId, pInfo);
                info.boundWidget = hostView;
                mWidgetCleanupState = WIDGET_INFLATED;
                hostView.setVisibility(INVISIBLE);
                int[] unScaledSize = mLauncher.getWorkspace().estimateItemSize(info.spanX,
                        info.spanY, info, false);

                // We want the first widget layout to be the correct size. This will be important
                // for width size reporting to the AppWidgetManager.
                DragLayer.LayoutParams lp = new DragLayer.LayoutParams(unScaledSize[0],
                        unScaledSize[1]);
                lp.x = lp.y = 0;
                lp.customPosition = true;
                hostView.setLayoutParams(lp);
                mLauncher.getDragLayer().addView(hostView);
            }
        };
        post(mInflateWidgetRunnable);
    }
    
    /**
     * @category Same as AppsCustomizePagedView
     * 
     * @param launcher
     * @param info
     * @return
     */
	static Bundle getDefaultOptionsForWidget(Launcher launcher, PendingAddWidgetInfo info) {
        Bundle options = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            AppWidgetResizeFrame.getWidgetSizeRanges(launcher, info.spanX, info.spanY, sTmpRect);
            Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(launcher,
                    info.componentName, null);

            float density = launcher.getResources().getDisplayMetrics().density;
            int xPaddingDips = (int) ((padding.left + padding.right) / density);
            int yPaddingDips = (int) ((padding.top + padding.bottom) / density);

            options = new Bundle();
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                    sTmpRect.left - xPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                    sTmpRect.top - yPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
                    sTmpRect.right - xPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                    sTmpRect.bottom - yPaddingDips);
        }
        return options;
    }
    
	@Override
	public boolean supportsFlingToDelete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsAppInfoDropTarget() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsDeleteDropTarget() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getIntrinsicIconScaleFactor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onFlingToDeleteCompleted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	/**
	 * @category Same as AppsCustomizePagedView
	 */
	public void onDropCompleted(View target, DragObject d,
			boolean isFlingToDelete, boolean success) {
		// TODO Auto-generated method stub
        // Return early and wait for onFlingToDeleteCompleted if this was the result of a fling
        if (isFlingToDelete) return;

        endDragging(target, false, success);

        // Display an error message if the drag failed due to there not being enough space on the
        // target layout we were dropping on.
        if (!success) {
            boolean showOutOfSpaceMessage = false;
            if (target instanceof Workspace) {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace.getChildAt(currentScreen);
                ItemInfo itemInfo = (ItemInfo) d.dragInfo;
                if (layout != null) {
                    layout.calculateSpans(itemInfo);
                    showOutOfSpaceMessage =
                            !layout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
                }
            }
            /** Lenovo-SW zhaoxin5 20150828 XTHREEROW-1187 START
             *  
             *  这是手动从WidgetList拖动Widget失败后的路径
             *  但是因为Workspace已经提示过一次了
             *  因此这里无需再次提示
             * 
             */
            /*if (showOutOfSpaceMessage) {
                mLauncher.showOutOfSpaceMessage(false);
            }*/
            /** Lenovo-SW zhaoxin5 20150828 XTHREEROW-1187 END */

            d.deferDragViewCleanupPostAnimation = false;
        }
        cleanupWidgetPreloading(success);
        mDraggingWidget = false;
	}   
	
	/**
	 * @category Same as AppsCustomizePagedView
	 * 
	 * @param target
	 * @param isFlingToDelete
	 * @param success
	 */
    private void endDragging(View target, boolean isFlingToDelete, boolean success) {
        if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() &&
                !(target instanceof DeleteDropTarget) && !(target instanceof Folder))) {
            // Exit spring loaded mode if we have not successfully dropped or have not handled the
            // drop in Workspace
            mLauncher.exitSpringLoadedDragMode();
            mLauncher.unlockScreenOrientation(false);
        } else {
            mLauncher.unlockScreenOrientation(false);
        }
        /** Lenovo-SW zhaoxin5 20150707 add for LANCHROW-195 START */
        /*this.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
		        mLauncher.getEditEntrycontroller().onHomePressed();
			}
		}, 300);*/
        /** Lenovo-SW zhaoxin5 20150707 add for LANCHROW-195 END */
    }
	
    /**
     * @category Same as AppsCustomizePagedView
     * 
     * @param widgetWasAdded
     */
    private void cleanupWidgetPreloading(boolean widgetWasAdded) {
        if (!widgetWasAdded) {
            // If the widget was not added, we may need to do further cleanup.
            PendingAddWidgetInfo info = mCreateWidgetInfo;
            mCreateWidgetInfo = null;

            if (mWidgetCleanupState == WIDGET_PRELOAD_PENDING) {
                // We never did any preloading, so just remove pending callbacks to do so
                removeCallbacks(mBindWidgetRunnable);
                removeCallbacks(mInflateWidgetRunnable);
            } else if (mWidgetCleanupState == WIDGET_BOUND) {
                 // Delete the widget id which was allocated
                if (mWidgetLoadingId != -1) {
                    mLauncher.getAppWidgetHost().deleteAppWidgetId(mWidgetLoadingId);
                }

                // We never got around to inflating the widget, so remove the callback to do so.
                removeCallbacks(mInflateWidgetRunnable);
            } else if (mWidgetCleanupState == WIDGET_INFLATED) {
                // Delete the widget id which was allocated
                if (mWidgetLoadingId != -1) {
                    mLauncher.getAppWidgetHost().deleteAppWidgetId(mWidgetLoadingId);
                }

                // The widget was inflated and added to the DragLayer -- remove it.
                AppWidgetHostView widget = info.boundWidget;
                mLauncher.getDragLayer().removeView(widget);
            }
        }
        mWidgetCleanupState = WIDGET_NO_CLEANUP_REQUIRED;
        mWidgetLoadingId = -1;
        mCreateWidgetInfo = null;
        // PagedViewWidget.resetShortPressTarget();
    }
	/*************************************
	 * 3) Third module
	 *    END 
	 *************************************/	
	
 
	/************************************* 
	 * 4) Forth module
	 *    Single Click to add Widget
	 * 
	 * 	  START 
	 *************************************/
    private void onSingleClick(View v) {
    	
    	Object o = v.getTag();
		if (null == o)
			return;
    	
    	if (o instanceof AppWidgetProviderInfo) {
			AppWidgetProviderInfo info = (AppWidgetProviderInfo) o;
			PendingAddWidgetInfo createItemInfo = getPendingAddWidgetInfo(info);
			onShortClick(v, createItemInfo);
			LauncherLog.i(TAG, "AppWidgetProviderInfo : " + createItemInfo.title);
		} else if (o instanceof ResolveInfo) {
			PendingAddItemInfo createItemInfo = getPendingAddShortcutInfo((ResolveInfo)o);
			onShortClick(v, createItemInfo);
			LauncherLog.i(TAG, "ResolveInfo : " + createItemInfo.title);
		}
    }
    
    /**
     * Draw the view into a bitmap.
     */
    Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        float alpha = v.getAlpha();
        v.setAlpha(1.0f);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            LauncherLog.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setAlpha(alpha);
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }
    
    private View createShortcutAnimateView(View v) {
        final ImageView iv = new ImageView(mContext);
        iv.setImageBitmap(getViewBitmap(v));
        float scale = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getOverviewModeScale();
        int size = (int) (LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().allAppsIconSizePx * scale);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(size, size);
        iv.setLayoutParams(lp);
    	return iv;
    }
    
    private View createWidgetAnimateView(View v) {
    	final ImageView iv = new ImageView(mContext);
        iv.setImageBitmap(getViewBitmap(v));
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(v.getWidth(), v.getHeight());
        iv.setLayoutParams(lp);
    	return iv;
    }
    
    private void onShortClick(View view ,PendingAddItemInfo createItemInfo) {
    	// find a location in the current cellLayout
    	int currentScreen = mLauncher.getCurrentWorkspaceScreen();
		Workspace workspace = mLauncher.getWorkspace();
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
			mLauncher.showOutOfSpaceMessage(false);
		} else {
			createItemInfo.cellX = targetCell[0];
			createItemInfo.cellY = targetCell[1];
			createItemInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
			createItemInfo.screenId = workspace.getIdForScreen(layout);
			
			if (createItemInfo instanceof PendingAddWidgetInfo) {
				// add a widget
				mLauncher.animateAddWidget(createItemInfo, view.findViewById(R.id.image_view), createWidgetAnimateView(view.findViewById(R.id.image_view)));
			} else {
				// add a shortcut
				mLauncher.animateShortcut(createItemInfo, view, createShortcutAnimateView(view.findViewById(R.id.image_view)));
			}
		}
    }
	/*************************************
	 * 4) Forth module
	 *    END 
	 *************************************/	
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE && mFirstListRecyclerView != null) {
            mFirstListRecyclerView.scrollToPosition(0);
        }
    }
}
