package com.android.launcher3;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.launcher3.WallpaperListViewSwitcher.ShowType;
import com.webeye.launcher.R;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WallpaperListHorizontalRecyclerView extends RecyclerView {

    private ShowType mMyShowType = ShowType.FIRST_PRELOAD;
    private OnWallpaperListClickListener mClickListener = null;
    private WallpaperListHorizontalRecyclerViewViewAdapter mAdatper = null;
    private Context mContext = null;

    // Tags.
    public final String SHOW_THEME_CENTER_TAG = "ShowThemeCenter";
    public final String SHOW_CURRENT_WALLPAPER_TAG = "ShowCurrentWallpaper";
    public final String SHOW_APK_WALLPAPER_TAG = "ShowApkWallpaper";
    public final String SHOW_FILE_WALLPAPER_TAG = "ShowFileWallpaper";
    public final String SHOW_OTHERS_TAG = "ShowOthers";

    // Show cases.
    private final int SHOW_TEXT = 0; // Default.
    private final int SHOW_CURRENT_IMAGE = 1;
    private final int SHOW_APK_IMAGE = 2;
    private final int SHOW_FILE_IMAGE = 3;

    // For currently showned wallpaper.
    private String CURRENT_WALLPAPAER_PATH = "path";
    public final String CURRENT_FROM_OTHER_APP = "others";
    private String CURRENT_WALLPAPAER_FLAG = "flag";
    private String CURRENT_WALLPAPAER_RES_ID = "id";
    private int mCurResId = LauncherModel.WALLPAPER_DEFAULT_SOURCE;

    // For APK wallpaper.
    private WallpaperManager mWallpaperManager;
    private PicassoHelper mPicassoHelper;
    private final String WALLPAPER_PACKAGE = "com.lenovo.ideawallpaper";
    private final String WALLPAPER_PACKAGE_EX = "com.lenovo.syswallpaper";
    private HashMap<Integer, Integer> mApkImage = new HashMap<Integer, Integer>();
    private int mApkNum = 0;
    private String mWallpaperPackage = WALLPAPER_PACKAGE;

    // For File wallpaper.
    private static final boolean isSupportFileImage = false;
    private static final String CUSTOMED_WALLPAPER_XML = "/system/etc/localwallpaper/customed_wallpaper.xml";
    private static final String CUSTOMED_WALLPAPER_DIR = "/system/etc/localwallpaper/";
    private String[] mWallpaperFileList = new File(CUSTOMED_WALLPAPER_DIR).list();

    // For other wallpaper.
    List<ResolveInfo> mResolveInfos = null;
    private static final String SET_WALLPAPER_INTENT = "android.intent.action.SET_WALLPAPER";

    private ArrayList<FirstLayerItem> mFirstLayer = new ArrayList<FirstLayerItem>();
    private ArrayList<SecondLayerItem> mSecondLayer = new ArrayList<SecondLayerItem>();

    private class FirstLayerItem {

        int showCase = SHOW_TEXT;
        String itemTextStrOrloadPathStr = "";
        Bitmap currentBitmap = null;
        WallpaperListItemInfo listinfo = null;

        private FirstLayerItem(int show, String itemText, Bitmap Bitmap, WallpaperListItemInfo info) {
            showCase = show;
            itemTextStrOrloadPathStr = itemText;
            currentBitmap = Bitmap;
            listinfo = info;
        }

        private FirstLayerItem(int show, String itemText, WallpaperListItemInfo info) {
            showCase = show;
            itemTextStrOrloadPathStr = itemText;
            listinfo = info;
        }

    }

    private class SecondLayerItem {

        String itemTextStr = "";
        WallpaperListOtherItemInfo listinfo = null;

        private SecondLayerItem(String itemText, WallpaperListOtherItemInfo info) {
            itemTextStr = itemText;
            listinfo = info;
        }

    }

    public void initFirstLayerDataForAdapter(boolean showOtherAppImage) {
        mFirstLayer.clear();
        // Position One.
        mFirstLayer.add(new FirstLayerItem(SHOW_TEXT, getResources().getString(R.string.wallpaper_show_theme_center),
            new WallpaperListItemInfo(SHOW_THEME_CENTER_TAG)));
        // Position just for other app image.
        if (showOtherAppImage) {
            mFirstLayer.add(new FirstLayerItem(SHOW_CURRENT_IMAGE, CURRENT_FROM_OTHER_APP,
                getCurrentWallpaper(), new WallpaperListItemInfo(SHOW_CURRENT_WALLPAPER_TAG)));
        }
        // Position Middle.
        for (int pos = 1; pos < mApkImage.size() + 1; pos++) {
            String loadPathStr = "android.resource://" + mWallpaperPackage + "/drawable/" + mApkImage.get(pos);
            mFirstLayer.add(new FirstLayerItem(SHOW_APK_IMAGE, loadPathStr,
                new WallpaperListItemInfo(SHOW_APK_WALLPAPER_TAG, loadPathStr, mWallpaperManager,
                    mWallpaperPackage, mApkImage.get(pos))));
        }
        /*if (isSupportFileImage && mWallpaperFileList != null && mWallpaperFileList.length != 0) {
            for (int pos = 0; pos < mWallpaperFileList.length; pos++) {
                if (isImageFile(mWallpaperFileList[pos])) {
                    String loadPathStr = CUSTOMED_WALLPAPER_DIR + mWallpaperFileList[pos];
                    mFirstLayer.add(new FirstLayerItem(SHOW_FILE_IMAGE, loadPathStr,
                            new WallpaperListItemInfo(SHOW_FILE_WALLPAPER_TAG, loadPathStr, mWallpaperManager,
                                mWallpaperPackage, -1)));
                }
            }
        }*/
        // Position Last.
        mFirstLayer.add(new FirstLayerItem(SHOW_TEXT, getResources().getString(R.string.wallpaper_other_app),
            new WallpaperListItemInfo(SHOW_OTHERS_TAG)));
    }

    public void updateEditPoint(String loadPath, boolean isShown) {
        mAdatper.notifyDataSetChanged();
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CURRENT_WALLPAPAER_PATH, loadPath);
        editor.putBoolean(CURRENT_WALLPAPAER_FLAG, isShown);
        editor.commit();
     }

    public void updateEditPoint(int id, boolean isShown) {
        this.mCurResId = id;
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(CURRENT_WALLPAPAER_RES_ID, id);
        editor.putBoolean(CURRENT_WALLPAPAER_FLAG, isShown);
        editor.commit();
        mAdatper.notifyDataSetChanged();
     }

    public Bitmap getCurrentWallpaper() {
        Bitmap bitmap = null;
        Drawable wallpaperDrawable = null;
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        if (wallpaperManager != null) {
            WallpaperInfo wallInfo = wallpaperManager.getWallpaperInfo();
            // If wallInfo is null, then it is static wallpaper;
            // otherwise, it is a live wallpaper.Add by Lenovo-sw luyy1.
            if (wallInfo != null) {
                wallpaperDrawable = wallInfo.loadThumbnail(mContext.getPackageManager());
            } else {
                wallpaperDrawable = wallpaperManager.getDrawable();
            }
            if (wallpaperDrawable != null) {
                BitmapDrawable bd = (BitmapDrawable) wallpaperDrawable;
                int width = (int) mContext.getResources().getDimension(R.dimen.menu_list_icon_width);
                bitmap = Bitmap.createScaledBitmap(bd.getBitmap(), width, width, true);
            }
        }
            return bitmap;
    }

    public boolean isCurrentWallpaperLive() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        // If wallInfo is null, then it is static wallpaper;
        // otherwise, it is a live wallpaper.Add by Lenovo-sw luyy1.
        if (wallpaperManager != null && wallpaperManager.getWallpaperInfo() != null) {
            return true;
        }
        return false;
    }

    private void initSecondLayerDataForAdapter() {
        searchOtherApp();
        mSecondLayer.clear();
        for (int pos = 0; pos < mResolveInfos.size(); pos++) {
            String appName = mResolveInfos.get(pos).loadLabel(mContext.getPackageManager()).toString();
            String packageName = mResolveInfos.get(pos).activityInfo.packageName;
            if (packageName != null && packageName.equals("com.google.android.apps.photos")) {
                try {
                    appName = mContext.getPackageManager().getApplicationLabel(
                            mContext.getPackageManager().getApplicationInfo("com.google.android.apps.photos",
                            PackageManager.GET_META_DATA)).toString();
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            String className = mResolveInfos.get(pos).activityInfo.name;
            mSecondLayer.add(new SecondLayerItem(appName,
                new WallpaperListOtherItemInfo(appName, packageName, className)));
        }
    }

    public WallpaperListHorizontalRecyclerView(Context context) {
        super(context);
        setupViews(context);
    }

    public WallpaperListHorizontalRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupViews(context);
    }

    public WallpaperListHorizontalRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupViews(context);
    }

    private void setupViews(Context context) {
        mContext = context;
        mWallpaperManager = WallpaperManager.getInstance(context);
        mPicassoHelper = PicassoHelper.getInstance(context);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        this.setLayoutManager(layoutManager);
    }

    public void updateFirstList(OnWallpaperListClickListener clickListener) {
        mMyShowType = ShowType.FIRST_PRELOAD;
        loadApkWallpaper();
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        Boolean currentFlag = sp.getBoolean(CURRENT_WALLPAPAER_FLAG, true);
        initFirstLayerDataForAdapter(currentFlag);
        createAdapter();
        mClickListener = clickListener;
    }

    public void updateSecondList(OnWallpaperListClickListener clickListener) {
        mMyShowType = ShowType.SECOND_OTHERS;
        initSecondLayerDataForAdapter();
        createAdapter();
        mClickListener = clickListener;
    }

    private void createAdapter() {
        mAdatper = new WallpaperListHorizontalRecyclerViewViewAdapter();
        this.setAdapter(mAdatper);
    }

    private void searchOtherApp() {
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = new Intent(SET_WALLPAPER_INTENT);
        mResolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    private void loadApkWallpaper() {
        Resources themeResources = null;
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent();
        intent.setPackage(WALLPAPER_PACKAGE);
        ResolveInfo info = pm.resolveActivity(intent,PackageManager.GET_META_DATA);
        if (info != null) {
            try {
                mWallpaperPackage = WALLPAPER_PACKAGE;
                themeResources = pm.getResourcesForApplication(info.activityInfo.applicationInfo);
                String[] extras = null;
                int id = themeResources.getIdentifier("wallpapers", "array", WALLPAPER_PACKAGE);
                if (id != 0) {
                    extras = themeResources.getStringArray(id);
                    scanApkWallpaper(themeResources, WALLPAPER_PACKAGE, extras);
                }
                id = themeResources.getIdentifier("extra_wallpapers", "array", WALLPAPER_PACKAGE);
                if (id != 0) {
                    extras = themeResources.getStringArray(id);
                    scanApkWallpaper(themeResources, WALLPAPER_PACKAGE, extras);
                }
                id = themeResources.getIdentifier("lockscreen_wallpapers", "array", WALLPAPER_PACKAGE);
                if (id != 0) {
                    extras = themeResources.getStringArray(id);
                    scanApkWallpaper(themeResources, WALLPAPER_PACKAGE, extras);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            intent = new Intent();
            intent.setPackage(WALLPAPER_PACKAGE_EX);
            info = pm.resolveActivity(intent, PackageManager.GET_META_DATA);
            if (info != null) {
                try {
                    mWallpaperPackage = WALLPAPER_PACKAGE_EX;
                    themeResources = pm.getResourcesForApplication(info.activityInfo.applicationInfo);
                    String[] extras = null;
                    int id = themeResources.getIdentifier("wallpapers", "array", WALLPAPER_PACKAGE_EX);
                    if (id != 0) {
                        extras = themeResources.getStringArray(id);
                        scanApkWallpaper(themeResources, WALLPAPER_PACKAGE_EX, extras);
                    }
                    id = themeResources.getIdentifier("extra_wallpapers","array", WALLPAPER_PACKAGE_EX);
                    if (id != 0) {
                        extras = themeResources.getStringArray(id);
                        scanApkWallpaper(themeResources, WALLPAPER_PACKAGE_EX, extras);
                    }
                    id = themeResources.getIdentifier("lockscreen_wallpapers","array", WALLPAPER_PACKAGE_EX);
                    if (id != 0) {
                        extras = themeResources.getStringArray(id);
                        scanApkWallpaper(themeResources, WALLPAPER_PACKAGE_EX, extras);
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void scanApkWallpaper(Resources resources, String packageName, String[] extras) {
        for (String extra : extras) {
            int id = resources.getIdentifier(extra, "drawable", packageName);
            if (id != 0) {
                mApkNum ++;
                mApkImage.put(mApkNum, id);
            }
        }
    }

    public class WallpaperListHorizontalRecyclerViewViewAdapter extends
        RecyclerView.Adapter<WallpaperListHorizontalRecyclerViewViewAdapter.ViewHolder> {

        @Override
        public int getItemCount() {
          return (mMyShowType == ShowType.FIRST_PRELOAD) ? mFirstLayer.size() : mSecondLayer.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            switch (mMyShowType) {
                case FIRST_PRELOAD:
                    FirstLayerItem fItem = mFirstLayer.get(position);
                    //showEditPoint(viewHolder, fItem.itemTextStrOrloadPathStr, position);
                    showEditPoint(viewHolder, fItem.listinfo.ResId, position);
                    switch (fItem.showCase) {
                        case SHOW_CURRENT_IMAGE:
                            viewHolder.mFirst.image.setImageBitmap(fItem.currentBitmap);
                            break;
                        case SHOW_APK_IMAGE:
                            mPicassoHelper.getPicasso().load(Uri.parse(fItem.itemTextStrOrloadPathStr)).
                            resizeDimen(R.dimen.menu_list_icon_width, R.dimen.menu_list_icon_width).
                            into(viewHolder.mFirst.image);
                            break;
                        case SHOW_FILE_IMAGE:
                            mPicassoHelper.getPicasso().load(new File(fItem.itemTextStrOrloadPathStr)).
                            resizeDimen(R.dimen.menu_list_icon_width, R.dimen.menu_list_icon_width).
                            into(viewHolder.mFirst.image);
                            break;
                        default:
                            viewHolder.mFirst.itemName.setText(fItem.itemTextStrOrloadPathStr);
                            break;
                    }
                    viewHolder.mRootView.setTag(fItem.listinfo);
                    showTextOrImage(viewHolder, fItem.showCase);
                    break;
                default:
                    SecondLayerItem sItem = mSecondLayer.get(position);
                    viewHolder.mSecond.name.setText(sItem.itemTextStr);
                    viewHolder.mRootView.setTag(sItem.listinfo);
                    break;
            }
        }

        private void showTextOrImage(ViewHolder viewHolder, int showCase) {
            if (showCase == SHOW_TEXT) {
                viewHolder.mRootView.findViewById(R.id.wallpaper_show_image).setVisibility(GONE);
                viewHolder.mFirst.image.setVisibility(GONE);
                viewHolder.mRootView.findViewById(R.id.wallpaper_show_item).setVisibility(VISIBLE);
                viewHolder.mFirst.itemName.setVisibility(VISIBLE);
            } else {
                viewHolder.mRootView.findViewById(R.id.wallpaper_show_image).setVisibility(VISIBLE);
                viewHolder.mFirst.image.setVisibility(VISIBLE);
                viewHolder.mRootView.findViewById(R.id.wallpaper_show_item).setVisibility(GONE);
                viewHolder.mFirst.itemName.setVisibility(GONE);
            }
        }

        private void showEditPoint(ViewHolder viewHolder, String loadPath, int position) {
            String spKey = LauncherAppState.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            String currentPath = sp.getString(CURRENT_WALLPAPAER_PATH, CURRENT_FROM_OTHER_APP);
            if (TextUtils.equals(loadPath, currentPath)) {
                viewHolder.mRootView.findViewById(R.id.wallpaper_current).setVisibility(VISIBLE);
            } else {
                viewHolder.mRootView.findViewById(R.id.wallpaper_current).setVisibility(GONE);
            }
        }

        private void showEditPoint(ViewHolder viewHolder, int ResId, int position) {
            String spKey = LauncherAppState.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            mCurResId = sp.getInt(CURRENT_WALLPAPAER_RES_ID, LauncherModel.WALLPAPER_DEFAULT_SOURCE);
            if (mCurResId <= LauncherModel.WALLPAPER_OTHER_THEME_SOURCE) {
                if (position == 1) {
                    viewHolder.mRootView.findViewById(R.id.wallpaper_current).setVisibility(VISIBLE);
                } else {
                    viewHolder.mRootView.findViewById(R.id.wallpaper_current).setVisibility(GONE);
                }
                return;
            } else if (ResId == mCurResId) {
                viewHolder.mRootView.findViewById(R.id.wallpaper_current).setVisibility(VISIBLE);
            } else {
                viewHolder.mRootView.findViewById(R.id.wallpaper_current).setVisibility(GONE);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
            View view = null;
            switch (mMyShowType) {
            case FIRST_PRELOAD:
                view = View.inflate(viewGroup.getContext(),R.layout.preload_wallpaperlist_first_layer, null);
                break;
            default:
                view = View.inflate(viewGroup.getContext(),R.layout.other_wallpaperlist_second_layer, null);
                break;
            }
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public View mRootView;
            public Second mSecond = new Second();
            public First mFirst = new First();

            class First {
                LinearLayout root;
                ImageView image;
                TextView itemName;
            }

            class Second {
                LinearLayout root;
                TextView name;
            }

            public ViewHolder(View rootView) {
                super(rootView);
                mRootView = rootView;
                mRootView.setOnClickListener(this);

                switch (mMyShowType) {
                    case FIRST_PRELOAD:
                        mFirst.root = (LinearLayout) rootView;
                        mFirst.image = (ImageView) mFirst.root.findViewById(R.id.wallpaper_image);
                        mFirst.itemName = (TextView) mFirst.root.findViewById(R.id.wallpaper_name);
                        break;
                    default:
                        mSecond.root = (LinearLayout) rootView;
                        mSecond.name = (TextView) mSecond.root.findViewById(R.id.wallpaper_other_name);
                        break;
                }
            }

            @Override
            public void onClick(View v) {
                if(mClickListener != null) {
                    mClickListener.onClick(v);
                }
            }
        }
    }

    private boolean isImageFile(String filePath) {
        String nameExt = getFileNameExt(filePath);
        boolean result = false;
        if ("jpg".equalsIgnoreCase(nameExt) || "jpeg".equalsIgnoreCase(nameExt)
                || "png".equalsIgnoreCase(nameExt)
                || "bmp".equalsIgnoreCase(nameExt)) {
            result = true;
        }
        return result;
    }

    private String getFileNameExt(String filePath) {
        String str = filePath.toLowerCase();
        int lastDot = str.lastIndexOf('.');
        int strLen = str.length();
        String ext = null;
        if ((lastDot > 0) && (lastDot < strLen - 1)) {
            int i = lastDot + 1;
            ext = str.substring(i, strLen);
        }
        return ext;
    }
}
