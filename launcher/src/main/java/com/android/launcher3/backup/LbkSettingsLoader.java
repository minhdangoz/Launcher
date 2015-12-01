package com.android.launcher3.backup;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.android.launcher3.FolderTitle;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherProvider;
import com.android.launcher3.ModeSwitchHelper;
import com.android.launcher3.ModeSwitchHelper.Mode;
import com.android.launcher3.settings.SettingsValue;
import com.webeye.launcher.ext.LauncherLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Xml;

/**
 * 用来加载LBK中的设置属性
 * @author zhaoxin5
 *
 */
public class LbkSettingsLoader {
	
	private final static String TAG = "LbkSettingsLoader";
    private static final boolean LOGD = true;
	final Context mContext;
	final LauncherProvider mLauncherProvider; //用于文件夹多语言支持
	
	final static String TYPE_STRING = "String";
	final static String TYPE_BOOLEAN = "Boolean";
	final static String TYPE_INTEGER = "Integer";
	final static String SETTINGS_LAUNCHER_STYLE = "launcher_style";
	// 默认主页,按照0,1,2,3的顺序排列
	final static String SETTINGS_DEFAULT_PAGE = "DefaultPage";
        
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
    public static LbkSettingsLoader get(Context context, LauncherProvider lp) {
		if(LbkUtil.isPreloadLbkFileExist()) {
			return new LbkSettingsLoader(context, lp);			
		}
		return null;
	}
    
	private LbkSettingsLoader(Context context, LauncherProvider lp) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
		mLauncherProvider = lp;
	}
	                    
	private InputStream getDescFileInputStream() {
		File lbkFile = LbkUtil.getLbkFileFromPreloadDirectory();
		
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
	
	public void parseSettings() {
		InputStream is = getDescFileInputStream();
		if(null != is) {
			try {
				LauncherLog.i(TAG, "unzip lbk file success");
				startParseSettings(is);
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
	
	private void startParseSettings(InputStream is) {
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
                		dispatchStartParser(parser);
                		break;
                	case XmlPullParser.END_TAG:
                		dispatchEndParser(parser);
                		break;
                }
                eventType = parser.next();  
            }
            // 已经加载完了设置,
            // 将初次加载的标识置为false
            LauncherLog.i(TAG, "Load Settings from LBK success, set first_load flag false");
            SettingsValue.setFirstLoadSettings(mContext, false);
            
            /** Lenovo-SW zhaoxin5 20150826 KOLEOSROW-1136 START */
            // 有可能当前Launcher Style是第一次加载,
            // 但是EMPTY_DATABASE_CREATED的标识量是关闭的
            // 那会导致当前Launcher Style的桌面为空
            if (ModeSwitchHelper.isFirstLoad(mContext)) {
            	LauncherLog.i(TAG, LauncherAppState.getInstance().getCurrentLayoutString() + " is first load");
            	// 当前模式是第一次加载
            	String spKey = LauncherAppState.getSharedPreferencesKey();
                SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
                boolean mainBranchIsOpened = sp.getBoolean(LauncherProvider.EMPTY_DATABASE_CREATED, false);
                if(!mainBranchIsOpened) {
                	LauncherLog.i(TAG, "need load database for " + LauncherAppState.getInstance().getCurrentLayoutString());
                	// LauncherProvider中的loadDefaultFavoritesIfNecessary
                	// 函数中的分支已经关闭,需要打开
                	mLauncherProvider.setFlagEmptyDbCreated();
                }
            }
            /** Lenovo-SW zhaoxin5 20150826 KOLEOSROW-1136 END */
            
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void dispatchStartParser(XmlPullParser parser) {
		String name = parser.getName();
		
		if(name.equalsIgnoreCase(LbkUtil.Settings.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Settings.TAG);
			}
			parseSettings(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.Setting.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.Setting.TAG);
			}
			parseSetting(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.LabelList.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.LabelList.TAG);
			}
			parseLabelList(parser);
		} else if(name.equalsIgnoreCase(LbkUtil.LabelTag.TAG)) {
			if(LOGD) {
				LauncherLog.i(TAG, LbkUtil.LabelTag.TAG);
			}
			parseLabel(parser);
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
		}
	}
	
	private void dispatchEndParser(XmlPullParser parser) {
		String name = parser.getName();
		if (name.equalsIgnoreCase(LbkUtil.Folder.TAG)) {
			if (LOGD) {
				LauncherLog.i(TAG, LbkUtil.Folder.TAG);
			}
			parseFolder(parser, false);
		}
	}
	
	void parseSettings(XmlPullParser parser) {}
	
	void parseSetting(XmlPullParser parser) {
		String type = parser.getAttributeValue(null, LbkUtil.Setting.ATTR_TYPE);
		String category = parser.getAttributeValue(null, LbkUtil.Setting.ATTR_CATEGORY);
		String name = parser.getAttributeValue(null, LbkUtil.Setting.ATTR_NAME);
		String value = parser.getAttributeValue(null, LbkUtil.Setting.ATTR_VALUE);
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.Setting.ATTR_TYPE + ":" + type + " , ");
			sb.append(LbkUtil.Setting.ATTR_CATEGORY + ":" + category + " , ");
			sb.append(LbkUtil.Setting.ATTR_NAME + ":" + name + " , ");
			sb.append(LbkUtil.Setting.ATTR_VALUE + ":" + value);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
		
		if(null != name) {
			if(name.equals(SETTINGS_LAUNCHER_STYLE)) {
				parseSetting_LauncherStyle(type, category, value);
			} else if(name.equals(SETTINGS_DEFAULT_PAGE)) {
				parseSetting_DefaultPage(type, category, value);
			}
		}
	}
	
	void parseSetting_LauncherStyle(String type, String category, String value) {
		if(null == value) {
			return;
		}
		if(value.equalsIgnoreCase("vibeui")) {
			LauncherLog.i(TAG, SETTINGS_LAUNCHER_STYLE + ", vibeui");
			LauncherAppState.getInstance().setCurrentLayoutMode(Mode.VIBEUI);
			SettingsValue.setDisableAllApps(mContext, true);	
		} else {
			LauncherLog.i(TAG, SETTINGS_LAUNCHER_STYLE + ", android");
			LauncherAppState.getInstance().setCurrentLayoutMode(Mode.ANDROID);
			SettingsValue.setDisableAllApps(mContext, false);
		}
	}
	
	void parseSetting_DefaultPage(String type, String category, String value) {
		if(null == value) {
			return;
		}
		if(type.equalsIgnoreCase(TYPE_INTEGER)) {
			int defaultPage = Integer.valueOf(value);
			LauncherLog.i(TAG, "default page : " + defaultPage);
			SettingsValue.setDefaultPage(mContext, defaultPage);
		}
	}
	
	private void parseLabelList(XmlPullParser parser) {}
	
	private void parseLabel(XmlPullParser parser) {
		
		if(null == mLauncherProvider) {
			return;
		}
		
		// 解析当前
		String countrycode = parser.getAttributeValue(null, LbkUtil.LabelTag.ATTR_COUNTRYCODE);
		String value = parser.getAttributeValue(null, LbkUtil.LabelTag.ATTR_VALUE);
		
		if(LOGD) {
			StringBuilder sb = new StringBuilder();
			sb.append("{ ");
			sb.append(LbkUtil.LabelTag.ATTR_COUNTRYCODE + ":" + countrycode + " , ");
			sb.append(LbkUtil.LabelTag.ATTR_VALUE + ":" + value);
			sb.append(" }");
			LauncherLog.i(TAG, sb.toString());
		}
		
		if(null != mPendingFolderTitleInfo) {
			mPendingFolderTitleInfo.titles.add(value);
			mPendingFolderTitleInfo.countries.add(countrycode);
		}
	}
	
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
		if(isStart) {
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
		}
	}
	
	private void parseConfig(XmlPullParser parser) {
		// 这是解析文件夹
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
		
		if(mPendingFolderTitleInfo != null) {
    		mPendingFolderTitleInfo.containerId = Long.valueOf(icon.substring(0, icon.indexOf(".")));
    	}
	}
	
	PendingFolderTitleInfo mPendingFolderTitleInfo = new PendingFolderTitleInfo();
	class PendingFolderTitleInfo {
		public static final int CLEAR_CONTAINER_ID = -909;
		
		long containerId = CLEAR_CONTAINER_ID;
		List<String> titles = new ArrayList<String>();
		List<String> countries = new ArrayList<String>();
		
		public void clear() {
			containerId = CLEAR_CONTAINER_ID;
			titles.clear();
			countries.clear();
		}
		
		PendingFolderTitleInfo() {
			clear();
		}
		
		boolean isDataRight() {
			boolean bId = containerId != CLEAR_CONTAINER_ID;
			boolean bTitles = titles.size() > 0;
			boolean bCountries = countries.size() > 0;
			return bId && bTitles && bCountries && titles.size() == countries.size();
		}
		
		String[] getTitles() {
			final int size = titles.size();
			if(size < 1) {
				return null;
			}
			return (String[]) titles.toArray(new String[size]);
		}
		
		String[] getCountries() {
			final int size = countries.size();
			if(size < 1) {
				return null;
			}
			return (String[]) countries.toArray(new String[size]);
		}
	}
}
