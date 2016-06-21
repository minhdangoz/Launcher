package com.android.launcher3.backup;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherProvider;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.ModeSwitchHelper;
import com.android.launcher3.AutoInstallsLayout.LayoutParserCallback;
import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.ModeSwitchHelper.Mode;
import com.klauncher.ext.LauncherLog;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Xml;

/**
 * this class used to load the desk data form
 * XLauncher LBK backup file  
 * @author zhaoxin5
 *
 */
public class LbkLoader implements LauncherProvider.WorkspaceLoader {
	
	private final static String TAG = "LbkLoader";
    private static final boolean LOGD = true;
    public final static String OLDER_LBK_IMG_PATH = "snapshot/shortcut/";
	final LayoutParserCallback mCallback;
	final Context mContext;
	final AppWidgetHost mAppWidgetHost;
	private SQLiteDatabase mDb;
	final ContentValues mValues;
	final PackageManager mPackageManager;
	// 因为LBK文件中在文件夹中的应用保存的container是原来的
	// 文件夹的id,现在要创建新的文件夹id会变化
	// 这个hashMap存放的是老的id和新的id的映射
    final HashMap<Long, Long> mFolderIds;
    
    /**
     * called from outside to start
     * to recovery the desktop data 
     * from lbk
     */
    public static boolean recoveryFromLbk() {
    	if(!LbkUtil.isXLauncherBackupLbkFileExist()) {
    		LauncherLog.i(TAG, "No XLauncher backup file");
    		return false;
    	}
		// 下面这句是为了在LauncherProvider的loadDefaultFavoritesIfNecessary函数的第一个分支能够进入
		LauncherAppState.getLauncherProvider().setFlagEmptyDbCreated();
    	LauncherAppState.getInstance().reloadWorkspaceForcibly(LauncherModel.LOADER_FLAG_RECOVERY_FROM_LBK);
    	return true;
    }
    
    /**
     * called from launcherprovider
     * if XLauncher lbk backup file exist
     * then return a new XLauncherLbkLoader instance
     * 
     * @param context
     * @param appWidgetHost
     * @param callback
     * @return
     */
    public static LbkLoader get(Context context, AppWidgetHost appWidgetHost,
            LayoutParserCallback callback) {
		if(LbkUtil.isXLauncherBackupLbkFileExist()) {
			isLoadPreloadLBK = false;
			return new LbkLoader(context, appWidgetHost, callback);			
		}
		return null;
	}
    
    private static boolean isLoadPreloadLBK = false;
    public static LbkLoader getPreLoadLBKLoader(Context context, AppWidgetHost appWidgetHost,
            LayoutParserCallback callback) {
		if(LbkUtil.isPreloadLbkFileExist()) {
			isLoadPreloadLBK = true;
			return new LbkLoader(context, appWidgetHost, callback);			
		}
		return null;
	}
    
	private LbkLoader(Context context, AppWidgetHost appWidgetHost,
            LayoutParserCallback callback) {
		super();
		// TODO Auto-generated constructor stub
		mCallback = callback;
		mContext = context;
		mAppWidgetHost = appWidgetHost;
		mValues = new ContentValues();
		mPackageManager = mContext.getPackageManager();
		mFolderIds = new HashMap<Long, Long>();
	}
	
	@Override
	public int loadLayout(SQLiteDatabase db, ArrayList<Long> screenIds) {
		// TODO Auto-generated method stub
		mDb = db;
		// 首先需要将当前模式下的桌面数据清空
		ModeSwitchHelper.clearDesktopDataForCurrentMode(mDb);
		// 开始解析
		parseLayout(screenIds);
		return 0;
	}
                    
	private InputStream getDescFileInputStream() {
		File lbkFile;
		if(!isLoadPreloadLBK) {
			lbkFile = LbkUtil.getLbkFileFromXLauncherBackupDirectory();
		} else {
			lbkFile = LbkUtil.getLbkFileFromPreloadDirectory();
		}
		if( null == lbkFile ) {
    		LauncherLog.i(TAG, "not find lbk file in the root directory");
    		return null;
		}
		
		try {
			LauncherLog.i(TAG, "getDescFileInputStream : " + lbkFile.getAbsolutePath());
			return LbkUtil.unZip(lbkFile.getAbsolutePath(), LbkUtil.DESC_FILE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LauncherLog.i(TAG, "error happened in upzip file");
			return null;
		}
	}
    
	// 用来去筛选出对应的.mp3文件

	static class LbkFileFilter implements FilenameFilter {
		/*
		 * accept方法的两个参数的意义： dir：文件夹对像，也就是你原来调用list方法的File文件夹对像 name：当前判断的文件名，
		 * 这个文件名就是文件夹下面的文件
		 * 返回：这个文件名是否符合条件，当为true时，list和listFiles方法会把这个文件加入到返回的数组里，false时则不会加入
		 */
		public boolean accept(File dir, String filename) {
			// TODO Auto-generated method stub
			return (filename.endsWith(".lbk"));
		}
	}
	
    static int getContainerFormLbkAtrr(String type) {
    	if("hotseat".equals(type)) {
    		return LauncherSettings.Favorites.CONTAINER_HOTSEAT;
    	}
    	if("desktop".equals(type)) {
    		return LauncherSettings.Favorites.CONTAINER_DESKTOP;
    	}
    	return -1;
    }

	public void parseLayout(ArrayList<Long> screenIds) {
		InputStream is = getDescFileInputStream();
		if(null != is) {
			try {
				LauncherLog.i(TAG, "unzip lbk file success");
				startParseLayout(is, screenIds);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
					is = null;
					LbkUtil.closeUnZipFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			LauncherLog.i(TAG, "unzip lbk file failed");
		}
	}
	
	private void startParseLayout(InputStream is, ArrayList<Long> screenIds) {
		XmlPullParser parser = Xml.newPullParser();
		parser = Xml.newPullParser();
		
		try {
			parser.setInput(is, LbkUtil.INPUT_ENCODING);
			int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch(eventType) {
                	case XmlPullParser.START_DOCUMENT:
                		break;
                	case XmlPullParser.START_TAG:
                		dispatchStartParser(parser, screenIds);
                		break;
                	case XmlPullParser.END_TAG:
                		dispatchEndParser(parser, screenIds);
                		break;
                }
                eventType = parser.next();  
            }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void dispatchEndParser(XmlPullParser parser, ArrayList<Long> screenIds) {
		String name = parser.getName();
		if(name.equalsIgnoreCase(LbkUtil.Screens.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Screens.TAG);
			}
			parseScreens(parser, false, screenIds);
		} 
	}
	
	private void dispatchStartParser(XmlPullParser parser, ArrayList<Long> screenIds) {
		//Thread.dumpStack();
		String name = parser.getName();
		
		mValues.clear();
		mValues.put(LauncherSettings.Favorites.FOR_2LAYER_WORKSPACE, 
				String.valueOf(LauncherAppState.getInstance().getCurrentLayoutInt()));
		
		if(name.equalsIgnoreCase(LbkUtil.Profiles.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Profiles.TAG);
			}
			parseProfiles(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.Profile.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Profile.TAG);
			}
			parseProfile(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.Folders.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Folders.TAG);
			}
			parseFolders(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.Folder.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Folder.TAG);
			}
			parseFolder(parser, true);
		} else if(name.equalsIgnoreCase(LbkUtil.Config.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Config.TAG);
			}
			parseConfig(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.AppList.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.AppList.TAG);
			}
			parseAppList(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.App.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.App.TAG);
			}
			parseApp(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.Widgets.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Widgets.TAG);
			}
			parseWidgets(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.Widget.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Widget.TAG);
			}
			parseWidget(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.LEOSE2EWidgets.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.LEOSE2EWidgets.TAG);
			}
			parseLEOSE2EWidgets(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.LEOSE2EWidget.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.LEOSE2EWidget.TAG);
			}
			parseLEOSE2EWidget(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.ShortCuts.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.ShortCuts.TAG);
			}
			parseShortCuts(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.ShortCut.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.ShortCut.TAG);
			}
			parseShortCut(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.Screens.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Screens.TAG);
			}
			parseScreens(parser, true, screenIds);
		} else if(name.equalsIgnoreCase(LbkUtil.Screen.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Screen.TAG);
			}
			parseScreen(parser);
		} /*else if(name.equalsIgnoreCase(LbkUtil.LabelList.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.LabelList.TAG);
			}
			parseLabelList(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.LabelTag.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.LabelTag.TAG);
			}
			parseLabel(parser);
		}*/   
	}
	
	private void parseProfiles(XmlPullParser parser) {
		String version = parser.getAttributeValue(null, LbkUtil.Profiles.ATTR_VERSION);
		if(LOGD) {
			LauncherLog.i(TAG, LbkUtil.Profiles.ATTR_VERSION + ":" + version);
		}
	}
	
	private void parseProfile(XmlPullParser parser) {
		String key = parser.getAttributeValue(null, LbkUtil.Profile.ATTR_KEY);
		String name = parser.getAttributeValue(null, LbkUtil.Profile.ATTR_NAME);
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.Profile.ATTR_KEY + ":" + key + " , ");
			sb.append(LbkUtil.Profile.ATTR_NAME + ":" + name);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
	}
	
	private void parseFolders(XmlPullParser parser) {}
	
	private void parseFolder(XmlPullParser parser, boolean isStart) {
		String key = parser.getAttributeValue(null, LbkUtil.Folder.ATTR_KEY);
		String name = parser.getAttributeValue(null, LbkUtil.Folder.ATTR_NAME);
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.Folder.ATTR_KEY + ":" + key + " , ");
			sb.append(LbkUtil.Folder.ATTR_NAME + ":" + name);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
		/*if(isStart) {
			mPendingFolderTitleInfo.clear();
		} else {
			if(mPendingFolderTitleInfo.isDataRight() && mLauncherProvider != null) {
				// 需要将当前文件夹名字的数据插入到数据库中
				String[] titles = mPendingFolderTitleInfo.getTitles();
				String[] countries = mPendingFolderTitleInfo.getCountries();
				if(titles != null && countries != null) {
					FolderTitle.insert(mLauncherProvider, mPendingFolderTitleInfo.containerId, titles, countries);
				}
			}
		}*/
	}
	
	private long parseConfig(XmlPullParser parser) {
		// 这是解析文件夹属性
		String cellX = parser.getAttributeValue(null, LbkUtil.Config.ATTR_CELL_X);
		String cellY = parser.getAttributeValue(null, LbkUtil.Config.ATTR_CELL_Y);
		String title = parser.getAttributeValue(null, LbkUtil.Config.ATTR_TITLE);
		String screen = parser.getAttributeValue(null, LbkUtil.Config.ATTR_SCREEN);
		String container = parser.getAttributeValue(null, LbkUtil.Config.ATTR_CONTAINER);
		String icon = parser.getAttributeValue(null, LbkUtil.Config.ATTR_ICON);
		
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.Config.ATTR_CELL_X + ":" + cellX + " , ");
			sb.append(LbkUtil.Config.ATTR_CELL_Y + ":" + cellY + " , ");
			sb.append(LbkUtil.Config.ATTR_TITLE + ":" + title + " , ");
			sb.append(LbkUtil.Config.ATTR_SCREEN + ":" + screen + " , ");
			sb.append(LbkUtil.Config.ATTR_ICON + ":" + icon + " , ");
			sb.append(LbkUtil.Config.ATTR_CONTAINER + ":" + container);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
		
        long id = mCallback.generateNewItemId();
        mValues.put(Favorites._ID, id);
        
        if(getContainerFormLbkAtrr(container) == LauncherSettings.Favorites.CONTAINER_HOTSEAT
        		&& Integer.valueOf(cellX) > 1 && Integer.valueOf(screen) > 1 
        		&& LauncherAppState.getInstance().getCurrentLayoutMode() == Mode.ANDROID) {
        	// 如果是Android模式下
        	// Hotseat上的应用如果是排在第二个以后的应用
        	// 应该将其往后排排
        	// 因为Android模式下的Hotseat上中间有一个allapps按钮
        	fillValuesForAll(Integer.valueOf(cellX) + 1, Integer.valueOf(cellY), Integer.valueOf(screen) + 1, getContainerFormLbkAtrr(container), 
    				1, 1, title);
        } else {
        	fillValuesForAll(Integer.valueOf(cellX), Integer.valueOf(cellY), Integer.valueOf(screen), getContainerFormLbkAtrr(container), 
    				1, 1, title);	
        }
        
        fillValuesForFolder(icon, id);
        
		if (mCallback.insertAndCheck(mDb, mValues) < 0) {
            return -1;
        } else {
        	/*if(mPendingFolderTitleInfo != null) {
        		mPendingFolderTitleInfo.containerId = id;
        	}*/
            return id;
        }
	}
	
	private void parseAppList(XmlPullParser parser) {}

	private long parseApp(XmlPullParser parser) {
		String cellX = parser.getAttributeValue(null, LbkUtil.App.ATTR_CELL_X);
		String cellY = parser.getAttributeValue(null, LbkUtil.App.ATTR_CELL_Y);
		String title = parser.getAttributeValue(null, LbkUtil.App.ATTR_TITLE);
		String screen = parser.getAttributeValue(null, LbkUtil.App.ATTR_SCREEN);
		String container = parser.getAttributeValue(null, LbkUtil.App.ATTR_CONTAINER);
		String packageName = parser.getAttributeValue(null, LbkUtil.App.ATTR_PACKAGE_NAME);
		String className = parser.getAttributeValue(null, LbkUtil.App.ATTR_CLASS_NAME);
		String action = parser.getAttributeValue(null, LbkUtil.App.ATTR_ACTION);
		String iconPackage = parser.getAttributeValue(null, LbkUtil.App.ATTR_ICON_PACKAGE);
		String iconResource = parser.getAttributeValue(null, LbkUtil.App.ATTR_ICON_RESOURCE);
		String icon = parser.getAttributeValue(null, LbkUtil.App.ATTR_ICON);

		if (className == null) {
			Intent launchIntent = mPackageManager.getLaunchIntentForPackage(packageName);
			if (launchIntent != null) {
				className = launchIntent.getComponent().getClassName();
			}
		}
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.App.ATTR_CELL_X + ":" + cellX + " , ");
			sb.append(LbkUtil.App.ATTR_CELL_Y + ":" + cellY + " , ");
			sb.append(LbkUtil.App.ATTR_TITLE + ":" + title + " , ");
			sb.append(LbkUtil.App.ATTR_SCREEN + ":" + screen + " , ");
			sb.append(LbkUtil.App.ATTR_CONTAINER + ":" + container + " , ");
			sb.append(LbkUtil.App.ATTR_PACKAGE_NAME + ":" + packageName + " , ");
			sb.append(LbkUtil.App.ATTR_ACTION + ":" + action + " , ");
			sb.append(LbkUtil.App.ATTR_ICON_PACKAGE + ":" + iconPackage + " , ");
			sb.append(LbkUtil.App.ATTR_ICON_RESOURCE + ":" + iconResource + " , ");
			sb.append(LbkUtil.App.ATTR_CLASS_NAME + ":" + className);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}

		boolean isShortcut = false;
		Intent intent = null;
        if((packageName == null || className == null) && action != null) {
        	try {
				intent = Intent.getIntentOld(action);
				LauncherLog.i(TAG, intent.toURI());
				isShortcut = true;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isShortcut = false;
				return -1;
			}	
        }
        
        long id = mCallback.generateNewItemId();
        mValues.put(Favorites._ID, id);
        
        Long newFolderId = mFolderIds.get(Long.valueOf(container));
        if(cellX == null || cellY == null || screen == null) {
    		fillValuesForAll(0, 0, 0, newFolderId, 1, 1, title);
        } else {
    		fillValuesForAll(Integer.valueOf(cellX), Integer.valueOf(cellY), Integer.valueOf(screen), newFolderId, 1, 1, title);	
        }
        
        if(isShortcut) {
        	fillValuesForShortcut(intent, iconPackage, iconResource, icon);
        } else {
    		fillValuesForApp(className, packageName);	
        }

		if (containsPackage(packageName)) {
			if (mCallback.insertAndCheck(mDb, mValues) < 0) {
				return -1;
			} else {
				return id;
			}
		} else {
			return -1;
		}
	}

	public boolean containsPackage(String packageName) {
		String name = null;
		try {
			name = mPackageManager.getApplicationLabel(
					mPackageManager.getApplicationInfo(packageName,
							PackageManager.GET_META_DATA)).toString();
		} catch (PackageManager.NameNotFoundException e) {
			if (LOGD) {
				LauncherLog.i(TAG, "No package named:" + packageName + " have installed on this device");
			}
		}
		return name != null;
	}

	private void parseWidgets(XmlPullParser parser) {}
	
	private long parseWidget(XmlPullParser parser) {
		String cellX = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_CELL_X);
		String cellY = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_CELL_Y);
		String spanX = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_SPAN_X);
		String spanY = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_SPAN_Y);
		String label = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_LABEL);
		String screen = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_SCREEN);
		String packageName = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_PACKAGE_NAME);
		String className = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_CLASS_NAME);
		String appWidgetId = parser.getAttributeValue(null, LbkUtil.Widget.ATTR_APPWIDGET_ID);
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.Widget.ATTR_CELL_X + ":" + cellX + " , ");
			sb.append(LbkUtil.Widget.ATTR_CELL_Y + ":" + cellY + " , ");
			sb.append(LbkUtil.Widget.ATTR_SPAN_X + ":" + spanX + " , ");
			sb.append(LbkUtil.Widget.ATTR_SPAN_Y + ":" + spanY + " , ");
			sb.append(LbkUtil.Widget.ATTR_LABEL + ":" + label + " , ");
			sb.append(LbkUtil.Widget.ATTR_SCREEN + ":" + screen + " , ");
			sb.append(LbkUtil.Widget.ATTR_PACKAGE_NAME + ":" + packageName + " , ");
			sb.append(LbkUtil.Widget.ATTR_CLASS_NAME + ":" + className + " , ");
			sb.append(LbkUtil.Widget.ATTR_APPWIDGET_ID + ":" + appWidgetId);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
		
        long id = mCallback.generateNewItemId();
        mValues.put(Favorites._ID, id);
        
        fillValuesForAll(Integer.valueOf(cellX), Integer.valueOf(cellY), Integer.valueOf(screen), 
        		LauncherSettings.Favorites.CONTAINER_DESKTOP, Integer.valueOf(spanX), Integer.valueOf(spanY), label);
        
        fillValuesForWidget(className, packageName, appWidgetId);
        
		if (mCallback.insertAndCheck(mDb, mValues) < 0) {
            return -1;
        } else {
            return id;
        }
	}
	
	private void parseLEOSE2EWidgets(XmlPullParser parser) {}
	
	private long parseLEOSE2EWidget(XmlPullParser parser) {
		String cellX = parser.getAttributeValue(null, LbkUtil.LEOSE2EWidget.ATTR_CELL_X);
		String cellY = parser.getAttributeValue(null, LbkUtil.LEOSE2EWidget.ATTR_CELL_Y);
		String spanX = parser.getAttributeValue(null, LbkUtil.LEOSE2EWidget.ATTR_SPAN_X);
		String spanY = parser.getAttributeValue(null, LbkUtil.LEOSE2EWidget.ATTR_SPAN_Y);
		String screen = parser.getAttributeValue(null, LbkUtil.LEOSE2EWidget.ATTR_SCREEN);
		String packageName = parser.getAttributeValue(null, LbkUtil.LEOSE2EWidget.ATTR_PACKAGE_NAME);
		String className = parser.getAttributeValue(null, LbkUtil.LEOSE2EWidget.ATTR_CLASS_NAME);
		String container = parser.getAttributeValue(null, LbkUtil.LEOSE2EWidget.ATTR_CONTAINER);
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.LEOSE2EWidget.ATTR_CELL_X + ":" + cellX + " , ");
			sb.append(LbkUtil.LEOSE2EWidget.ATTR_CELL_Y + ":" + cellY + " , ");
			sb.append(LbkUtil.LEOSE2EWidget.ATTR_SPAN_X + ":" + spanX + " , ");
			sb.append(LbkUtil.LEOSE2EWidget.ATTR_SPAN_Y + ":" + spanY + " , ");
			sb.append(LbkUtil.LEOSE2EWidget.ATTR_SCREEN + ":" + screen + " , ");
			sb.append(LbkUtil.LEOSE2EWidget.ATTR_PACKAGE_NAME + ":" + packageName + " , ");
			sb.append(LbkUtil.LEOSE2EWidget.ATTR_CLASS_NAME + ":" + className + " , ");
			sb.append(LbkUtil.LEOSE2EWidget.ATTR_CONTAINER + ":" + container);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
		
        long id = mCallback.generateNewItemId();
        mValues.put(Favorites._ID, id);
        
        if(className.equals(LbkUtil.LEOSE2EWidget.OLD_WEATHER_WIDGET_CLASS_NAME)) {
        	className = LbkUtil.LEOSE2EWidget.NEW_WEATHER_WIDGET_CLASS_NAME;
        }
        
        if(packageName.equals(LbkUtil.LEOSE2EWidget.OLD_WEATHER_WIDGET_PACKAGE_NAME)) {
        	packageName = LbkUtil.LEOSE2EWidget.NEW_WEATHER_WIDGET_PACKAGE_NAME;
        }
        
        fillValuesForAll(Integer.valueOf(cellX), Integer.valueOf(cellY), Integer.valueOf(screen), 
        		LauncherSettings.Favorites.CONTAINER_DESKTOP, Integer.valueOf(spanX), Integer.valueOf(spanY), "");
        
        fillValuesForWidget(className, packageName, null);
        
		if (mCallback.insertAndCheck(mDb, mValues) < 0) {
            return -1;
        } else {
            return id;
        }
	}
	
	private void parseShortCuts(XmlPullParser parser) {}
	
	private long parseShortCut(XmlPullParser parser) {
		String cellX = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_CELL_X);
		String cellY = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_CELL_Y);
		String screen = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_SCREEN);
		String packageName = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_PACKAGE_NAME);
		String className = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_CLASS_NAME);
		String title = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_TITLE);
		String container = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_CONTAINER);
		String action = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_ACTION);
		String iconPackage = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_ICON_PACKAGE);
		String iconResource = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_ICON_RESOURCE);
		String icon = parser.getAttributeValue(null, LbkUtil.ShortCut.ATTR_ICON);

		if (packageName.contains(",")) {
			String[] packages = packageName.split(",");
			for (String pkgName : packages) {
				if (pkgName.contains("/")) {
					String[] cns = pkgName.split("/");
					if (cns != null && cns.length == 2) {
						try {
							ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(
                                    cns[0], PackageManager.GET_META_DATA);
							if (info != null) {
								packageName = cns[0];
								className = cns[1];
								break;
							}
						} catch (PackageManager.NameNotFoundException e) {
							continue;
						}
					}
				} else {
					Intent launchIntent = mPackageManager.getLaunchIntentForPackage(pkgName);
					if (launchIntent != null) {
						packageName = pkgName;
						className = launchIntent.getComponent().getClassName();
						break;
					}
				}
			}
		}
		if (className == null) {
			Intent launchIntent = mPackageManager.getLaunchIntentForPackage(packageName);
			if (launchIntent != null) {
				className = launchIntent.getComponent().getClassName();
			}
		}
		if (className == null) {
			return -1;
		}

		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.ShortCut.ATTR_TITLE + ":" + title + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_CELL_X + ":" + cellX + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_CELL_Y + ":" + cellY + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_SCREEN + ":" + screen + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_PACKAGE_NAME + ":" + packageName + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_CLASS_NAME + ":" + className + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_ACTION + ":" + action + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_ICON_PACKAGE + ":" + iconPackage + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_ICON_RESOURCE + ":" + iconResource + " , ");
			sb.append(LbkUtil.ShortCut.ATTR_CONTAINER + ":" + container);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
		
		boolean isShortcut = false;
		Intent intent = null;
        if((packageName == null || className == null) && action != null) {
        	try {
				intent = Intent.parseUri(action, 0); // Lenovo-SW zhaoxin5 20150722 LANCHROW-239
				LauncherLog.i(TAG, intent.toURI());
				isShortcut = true;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isShortcut = false;
				return -1;
			}	
        }
		
        long id = mCallback.generateNewItemId();
        mValues.put(Favorites._ID, id);
        
        if(getContainerFormLbkAtrr(container) == LauncherSettings.Favorites.CONTAINER_HOTSEAT
        		&& Integer.valueOf(cellX) > 1 && Integer.valueOf(screen) > 1 
        		&& LauncherAppState.getInstance().getCurrentLayoutMode() == Mode.ANDROID) {
        	// 如果是Android模式下
        	// Hotseat上的应用如果是排在第二个以后的应用
        	// 应该将其往后排排
        	// 因为Android模式下的Hotseat上中间有一个allapps按钮
        	fillValuesForAll(Integer.valueOf(cellX) + 1, Integer.valueOf(cellY), Integer.valueOf(screen) + 1, getContainerFormLbkAtrr(container), 
    				1, 1, title);
        } else {
        	fillValuesForAll(Integer.valueOf(cellX), Integer.valueOf(cellY), Integer.valueOf(screen), getContainerFormLbkAtrr(container), 
    				1, 1, title);
        }
        
		if(!isShortcut) {
			fillValuesForApp(className, packageName);	
		} else {
			fillValuesForShortcut(intent, iconPackage, iconResource, icon);
		}
        
		if (mCallback.insertAndCheck(mDb, mValues) < 0) {
            return -1;
        } else {
            return id;
        }
	}
	
	Map<Long, Long> mScreenRanksIds = new HashMap<Long, Long>();
	List<Long> mScreenRanks = new ArrayList<Long>();
	
	private void parseScreens(XmlPullParser parser, boolean isStart, ArrayList<Long> screenIds) {
		if(isStart) {
			mScreenRanksIds.clear();
			mScreenRanks.clear();
		} else {
			// 先将mScreenRanks中的数据按照从小到大排序
			Collections.sort(mScreenRanks);
			for(int i=0; i<mScreenRanks.size(); i++) {
				Long rank = mScreenRanks.get(i);
				Long screenId = mScreenRanksIds.get(rank);
				if(!screenIds.contains(screenId)) {
					screenIds.add(screenId);
					LauncherLog.i(TAG, "rank : " + rank + ", screenId : " + screenId);
				}
			}
		}
	}
	
	private void parseScreen(XmlPullParser parser) {
		String screenId = parser.getAttributeValue(null, LbkUtil.Screen.ATTR_SCREEN_ID);
		String rank = parser.getAttributeValue(null, LbkUtil.Screen.ATTR_RANK);
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.Screen.ATTR_SCREEN_ID + ":" + screenId + " , ");
			sb.append(LbkUtil.Screen.ATTR_RANK + ":" + rank);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
		
		if(null != screenId && null != rank) {
			int sId = Integer.valueOf(screenId);
			int r = Integer.valueOf(rank);
			
			if(!mScreenRanks.contains(new Long(r))) {
				mScreenRanks.add(new Long(r));
				mScreenRanksIds.put(new Long(r), new Long(sId));
			}
		}
	}
	
	private void fillValuesForAll(int cellX, int cellY, int screen, 
			long container, int spanX, int spanY, String title) {
		mValues.put(LauncherSettings.Favorites.CELLX, cellX);
		mValues.put(LauncherSettings.Favorites.CELLY, cellY);
		mValues.put(LauncherSettings.Favorites.SCREEN, screen);
		mValues.put(LauncherSettings.Favorites.SPANX, spanX);
		mValues.put(LauncherSettings.Favorites.SPANY, spanY);
		mValues.put(LauncherSettings.Favorites.CONTAINER, container);
		mValues.put(LauncherSettings.Favorites.TITLE, title);
	}
	
	private void fillValuesForShortcut(Intent intent, String iconPackage, String iconResource, String icon) {
		if(null != intent) {
            mValues.put(Favorites.INTENT, intent.toUri(0));
            mValues.put(Favorites.APPWIDGET_ID, -1);
            mValues.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
            mValues.put(Favorites.ICON_PACKAGE, iconPackage);
            mValues.put(Favorites.ICON_RESOURCE, iconResource);
            mValues.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
            if(null != icon && !icon.equals("")) {
            	mValues.put(Favorites.ICON, parseIcon(icon));
            }
		}
	}
	
	private void fillValuesForApp(String className, String packageName) {
		if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
            try {
                ComponentName cn;
                try {
                    cn = new ComponentName(packageName, className);
                } catch (Exception nnfe) {
                    String[] packages = mPackageManager.currentToCanonicalPackageNames(
                            new String[] { packageName });
                    cn = new ComponentName(packages[0], className);
                }

				final Intent intent = new Intent(Intent.ACTION_MAIN, null)
						.addCategory(Intent.CATEGORY_LAUNCHER)
						.setComponent(cn)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                
                mValues.put(Favorites.INTENT, intent.toUri(0));
                mValues.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
                mValues.put(Favorites.APPWIDGET_ID, -1);
                
            } catch (Exception e) {
                LauncherLog.w(TAG, "Unable to add favorite: " + packageName + "/" + className, e);
            }
        } else {
            if (LOGD) LauncherLog.d(TAG, "Skipping invalid <favorite> with no component or uri");
        }
	}

	private void fillValuesForFolder(String icon, long id) {
		// 建立LBK文件中的folder的Id和新生成的id的映射
		// 以备后面的改文件夹中的应用需要填充container字段时
		// 使用
        Long oldFolderContainer = Long.valueOf(icon.substring(0, icon.indexOf(".")));
        mFolderIds.put(oldFolderContainer, id);
        
		// TODO Auto-generated method stub
        mValues.put(LauncherSettings.Favorites.ITEM_TYPE, LauncherSettings.Favorites.ITEM_TYPE_FOLDER);
	}
	
	private void fillValuesForWidget(String className, String packageName, String appWidgetID) {
		if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
            try {
                ComponentName cn;
                try {
                    cn = new ComponentName(packageName, className);
                } catch (Exception e) {
                    String[] packages = mPackageManager.currentToCanonicalPackageNames(
                            new String[] { packageName });
                    cn = new ComponentName(packages[0], className);
                }
                
                boolean hasPackage = true;
                try {
                    mPackageManager.getReceiverInfo(cn, 0);
                } catch (Exception e) {
                    LauncherLog.i("xixia", "Can't find widget provider");
                    hasPackage = false;
                }
                
                if (hasPackage) {
                    final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                    int appWidgetId = -1;
                    if(appWidgetID != null) {
                    	if(null != appWidgetManager.getAppWidgetInfo(Integer.valueOf(appWidgetID))) {
                    		appWidgetId = Integer.valueOf(appWidgetID);
                    		LauncherLog.i(TAG, "find old widget id for : " + cn.flattenToShortString());
                    	}
                    }
                    try {
                    	if(appWidgetId == -1) {
	                        appWidgetId = mAppWidgetHost.allocateAppWidgetId();
	                        LauncherLog.i("xixia", "appWidgetId:" + appWidgetId);
                    	}
                        mValues.put(Favorites.APPWIDGET_ID, appWidgetId);
                        mValues.put(Favorites.APPWIDGET_PROVIDER, cn.flattenToString());
                        mValues.put(LauncherSettings.Favorites.ITEM_TYPE, LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET);

                        LauncherLog.i("xixia", " 1 cn.flattenToString() : " + cn.flattenToString());
                        // TODO: need to check return value
                        appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, cn);

                    } catch (RuntimeException ex) {
                        LauncherLog.e("xixia", "Problem allocating appWidgetId", ex);
                    }
                }   
            } catch (Exception e) {
                LauncherLog.w(TAG, "Unable to add favorite: " + packageName + "/" + className, e);
            }
        } else {
            if (LOGD) LauncherLog.d(TAG, "Skipping invalid <favorite> with no component or uri");
        }
	}
	
	private byte[] parseIcon(String icon) {
		File lbkFile;
		if(!isLoadPreloadLBK) {
			lbkFile = LbkUtil.getLbkFileFromXLauncherBackupDirectory();
		} else {
			lbkFile = LbkUtil.getLbkFileFromPreloadDirectory();
		}

		try {
			InputStream is = LbkUtil.unZip(lbkFile.getAbsolutePath(), icon);
			if(is == null){
				icon = OLDER_LBK_IMG_PATH + icon;
				is = LbkUtil.unZip(lbkFile.getAbsolutePath(), icon);
			}
			byte[] data = LbkUtil.inputStream2ByteArray(is);
			if(null != is) {
				is.close();
				is = null;
			}
			LbkUtil.closeUnZipFile();
			return data;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
