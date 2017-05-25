package com.kapp.knews;

import android.app.Application;
import android.content.Context;

import com.kapp.knews.base.imagedisplay.glide.GlideDisplay;
import com.kapp.knews.base.imagedisplay.option.DisplayOption;
import com.kapp.knews.repository.server.ServerControlManager;
import com.kapp.knews.repository.utils.CommonShareData;


//import com.android.utils.Kljhsdk;

/**
 * 作者:  android001
 * 创建时间:   16/10/27  上午10:18
 * 版本号:
 * 功能描述:
 */
public class KnewsApp extends Application {

    /**
     * A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher.
     */
    public static final boolean ENCRYPTED = false;//目前必须为false，否则会报错NotDefError
//    private DaoSession daoSession;

    public static Context context;
    public static final String FILE_PREFRENCE = "file_knews";

//    private static Map<String, String> tabsMap;

    private static KnewsApp app;

    //    private RefWatcher refWatcher;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        app = this;
//        refWatcher=LeakCanary.install(this);
        CommonShareData.init(context);
//        FileUtils.initSdCardAssets();
        ServerControlManager.getInstance().init(this);

        initGreenDAO();
//        NavigationDAOManager.getInstance().init(this);

//        M.i(this, null);

//        Kljhsdk.init(context);

        initTabs();//初始化tabs channel category

//        CrashHandler handler = CrashHandler.getInstance();
//        handler.init(this);
        initGlide();
    }

    //
//    public void watch(Object obj){
//        refWatcher.watch(obj);
//    }
    private void initGlide() {
        int placeHolderResId = R.color.image_place_holder;
        int errorResId = R.drawable.ic_load_fail;
        DisplayOption displayOption = new DisplayOption(placeHolderResId, errorResId);
        GlideDisplay.getInstance().init(displayOption);
    }

    private void initTabs() {
//        if (null == tabsMap) {
//            Resources res = getResources();
//            //
//            String[] defaultTabChannel = res.getStringArray(R.array.default_tab_channel);
//            String[] defaultTabCategory = res.getStringArray(R.array.default_tab_category);
//            //
//            String[] tabChannelNotAdded = res.getStringArray(R.array.tab_channel_not_added);
//            String[] tabCategoryNotAdded = res.getStringArray(R.array.tab_category_not_added);
//
//            int defaultLength = defaultTabChannel.length;
//            int notAddedLength = tabChannelNotAdded.length;
//
//            tabsMap = new HashMap<>(defaultLength + notAddedLength);
//            for (int i = 0; i < defaultLength; i++) {
//                tabsMap.put(defaultTabChannel[i], defaultTabCategory[i]);
//            }
//            for (int i = 0; i < notAddedLength; i++) {
//                tabsMap.put(tabChannelNotAdded[i], tabCategoryNotAdded[i]);
//            }
//
//        }
    }

//    public Map<String, String> getTabsMap() {
//        return tabsMap;
//    }

    public static KnewsApp getApp() {
        return app;
    }

    private void initGreenDAO() {
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "knews-db-encrypted" :
//                "knews-db");
//        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
//        daoSession = new DaoMaster(db).newSession();
    }

//    public DaoSession getDaoSession() {
//        return daoSession;
//    }

    public static Context getContext() {
        return context;
    }
}
