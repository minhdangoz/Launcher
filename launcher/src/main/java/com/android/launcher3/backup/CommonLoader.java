package com.android.launcher3.backup;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Xml;

import com.android.launcher3.AutoInstallsLayout.LayoutParserCallback;
import com.android.launcher3.AutoInstallsLayout.TagParser;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherProvider;
import com.android.launcher3.LauncherProvider.WorkspaceLoader;
import com.android.launcher3.LauncherSettings.Favorites;
import com.klauncher.ext.LauncherLog;
import com.klauncher.launcher.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 通用版LBK Loader
 */
public class CommonLoader implements WorkspaceLoader {

    private static final String TAG = "CommonLoader";

    private final long[] mTemp = new long[4];
    private final List<ComponentName> mItemInfoList = new LinkedList<>();

    private final ContentValues mValues;
    private final LayoutParserCallback mCallback;
    private final PackageManager mPackageManager;
    private final Resources mSourceRes;
    private final Context mContext;

    private SQLiteDatabase mDb;
    private int mLayoutId;

    public interface CommonTagParser extends TagParser {
        long parseAndAdd(XmlPullParser parser, Resources res)
                throws XmlPullParserException, IOException;
    }

    public CommonLoader(Context context, LayoutParserCallback callback) {
        this(context, callback, -1);
    }

    public CommonLoader(Context context, LayoutParserCallback callback, int layoutId) {
        mContext = context;
        mCallback = callback;
        mLayoutId = layoutId;

        mSourceRes = context.getResources();
        mPackageManager = context.getPackageManager();
        mValues = new ContentValues();
    }

    @Override
    public int loadLayout(SQLiteDatabase db, ArrayList<Long> screenIds) {
        mDb = db;
        if (screenIds == null) {
            screenIds = LauncherModel.loadWorkspaceScreenIdsDb(mContext);
        }

        try {
            if (mLayoutId > 0) {
                return parseLayout(mLayoutId, screenIds);
            } else {
                return parseLayout(LbkUtil.COMMON_FILE, screenIds);
            }
        } catch (XmlPullParserException | IOException | RuntimeException e) {
            LauncherLog.w(TAG, "Got exception parsing layout.", e);
            clearItemInfoList();
            return -1;
        }
    }

    protected int parseLayout(String commonFileName, ArrayList<Long> screenIds)
            throws XmlPullParserException, IOException {
        LauncherLog.i(TAG, "parseLayout from common lbk : " + commonFileName);

        InputStream inputStream = getDescFileInputStream(commonFileName);
        if (inputStream != null) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream, LbkUtil.INPUT_ENCODING);

                return parseLayoutInner(parser, screenIds);
            } finally {
                inputStream.close();
            }
        }

        return -1;
    }

    protected int parseLayout(int layoutId, ArrayList<Long> screenIds)
            throws XmlPullParserException, IOException {
        LauncherLog.i(TAG, "parseLayout from res layoutId: " + layoutId);
        return parseLayoutInner(mSourceRes.getXml(layoutId), screenIds);
    }

    private int parseLayoutInner(XmlPullParser parser, ArrayList<Long> screenIds) throws XmlPullParserException, IOException {
        beginDocument(parser, LbkUtil.Profiles.TAG);
        final int depth = parser.getDepth();
        int type;
        HashMap<String, TagParser> tagParserMap = getLayoutElementsMap();
        int count = 0;

        beforeParseLayout();
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            count += parseAndAddNode(parser, tagParserMap, screenIds);
        }
        afterParseLayout(screenIds);
        return count;
    }

    protected static final void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT);

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    protected void parseContainerScreenCell(XmlPullParser parser, ArrayList<Long> screenIds, long[] out) {
        out[0] = out[1] = out[2] = out[3] = 0;

        String container = getAttributeValue(parser, LbkUtil.Config.ATTR_CONTAINER);
        int containerType = LbkLoader.getContainerFormLbkAtrr(container);
        if (containerType == -1) {
            containerType = Favorites.CONTAINER_DESKTOP;
        }

        switch (containerType) {
            case Favorites.CONTAINER_DESKTOP:
                out[0] = Favorites.CONTAINER_DESKTOP;
                fillTmpArray(screenIds);
                LauncherLog.v(TAG, "screen : " + out[1] + " cellX: " + out[2] + " cellY:" + out[3]);
                break;
            case Favorites.CONTAINER_HOTSEAT:
            default:
                throw new IllegalArgumentException("unkonw type");
        }
    }

    protected int parseAndAddNode(
            XmlPullParser parser,
            HashMap<String, TagParser> tagParserMap,
            ArrayList<Long> screenIds)
            throws XmlPullParserException, IOException {
        mValues.clear();
        parseContainerScreenCell(parser, screenIds, mTemp);
        final long container = mTemp[0];
        final long screenId = mTemp[1];
        final long cellX = mTemp[2];
        final long cellY = mTemp[3];

        mValues.put(Favorites.CONTAINER, container);
        mValues.put(Favorites.SCREEN, screenId);
        mValues.put(Favorites.CELLX, cellX);
        mValues.put(Favorites.CELLY, cellY);

        CommonTagParser tagParser = (CommonTagParser) tagParserMap.get(parser.getName());
        if (tagParser == null) {
            LauncherLog.i(TAG, "Ignoring unknown element tag: " + parser.getName());
            return 0;
        }
        long newElementId = tagParser.parseAndAdd(parser, mSourceRes);
        if (newElementId >= 0) {
            if (!screenIds.contains(screenId) &&
                    container == Favorites.CONTAINER_DESKTOP) {
                screenIds.add(screenId);
            }
            return 1;
        }
        return 0;
    }

    protected long addApp(String title, Intent intent) {
        long id = mCallback.generateNewItemId();
        mValues.put(Favorites.INTENT, intent.toUri(0));
        mValues.put(Favorites.TITLE, title);
        mValues.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
        mValues.put(Favorites.SPANX, 1);
        mValues.put(Favorites.SPANY, 1);
        mValues.put(Favorites._ID, id);
        if (mCallback.insertAndCheck(mDb, mValues) < 0) {
            return -1;
        } else {
            return id;
        }
    }

    protected HashMap<String, TagParser> getLayoutElementsMap() {
        HashMap<String, TagParser> parsers = new HashMap<>();
        parsers.put(LbkUtil.Folder.TAG, new FolderParser());
        return parsers;
    }

    private HashMap<String, TagParser> getFolderElementsMap() {
        HashMap<String, TagParser> parsers = new HashMap<>();
        parsers.put(LbkUtil.App.TAG, new AppParser());
        return parsers;
    }

    protected class AppParser implements CommonTagParser {
        @Override
        public long parseAndAdd(XmlPullParser parser, Resources res) throws XmlPullParserException, IOException {
            final String title = getAttributeValue(parser, LbkUtil.App.ATTR_TITLE);
            final String packageName = getAttributeValue(parser, LbkUtil.App.ATTR_PACKAGE_NAME);
            final String className = getAttributeValue(parser, LbkUtil.App.ATTR_CLASS_NAME);

            if (!TextUtils.isEmpty(packageName)) {
                try {
                    ComponentName cn;
                    if (!TextUtils.isEmpty(className)) {
                        cn = new ComponentName(packageName, className);
                    } else {
                        String[] packages = mPackageManager.currentToCanonicalPackageNames(
                                new String[]{packageName});
                        cn = new ComponentName(packages[0], className);
                    }

                    mPackageManager.getActivityInfo(cn, 0);
                    if (mItemInfoList.contains(cn)) {
                        LauncherLog.w(TAG, "app: " + packageName + "/" + className + " was founded in another place, can not add again...");
                        return -1;
                    }
                    mItemInfoList.add(cn);

                    final Intent intent = new Intent(Intent.ACTION_MAIN, null)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setComponent(cn)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    return addApp(title, intent);
                } catch (NullPointerException | PackageManager.NameNotFoundException e) {
                    LauncherLog.w(TAG, "Unable to add app: " + packageName + "/" + className);
                }
                return -1;
            } else {
                return invalidPackageOrClass(parser);
            }
        }

        @Override
        public long parseAndAdd(XmlResourceParser parser, Resources resources) {
            return parseAndAdd(parser, resources);
        }

        protected long invalidPackageOrClass(XmlPullParser parser) {
            LauncherLog.w(TAG, "Skipping invalid <app> with no component");
            return -1;
        }
    }

    protected class FolderParser implements CommonTagParser {
        private final HashMap<String, TagParser> mFolderElements;

        public FolderParser() {
            this(getFolderElementsMap());
        }

        public FolderParser(HashMap<String, TagParser> elements) {
            mFolderElements = elements;
        }

        @Override
        public long parseAndAdd(XmlPullParser parser, Resources res) throws XmlPullParserException, IOException {
            final String title = getAttributeValue(parser, LbkUtil.Config.ATTR_TITLE);
            final String icon = getAttributeValue(parser, LbkUtil.Config.ATTR_ICON);

            mValues.put(LbkUtil.Config.ATTR_TITLE, title);
            mValues.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_FOLDER);
            mValues.put(Favorites.SPANX, 1);
            mValues.put(Favorites.SPANY, 1);
            mValues.put(Favorites._ID, mCallback.generateNewItemId());
            long folderId = mCallback.insertAndCheck(mDb, mValues);
            if (folderId < 0) {
                LauncherLog.e(TAG, "Unable to add folder");
                return -1;
            }
            ArrayList<Long> folderItems = new ArrayList<>();

            int type;
            int folderDepth = parser.getDepth();
            while ((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > folderDepth) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                mValues.clear();
                mValues.put(Favorites.CONTAINER, folderId);

                CommonTagParser tagParser = (CommonTagParser) mFolderElements.get(parser.getName());
                if (tagParser != null) {
                    final long id = tagParser.parseAndAdd(parser, res);
                    if (id >= 0) {
                        folderItems.add(id);
                    }
                } else {
                    LauncherLog.w(TAG, "Invalid folder item " + parser.getName());
                }
            }

            if (folderItems.isEmpty()) {
                final ContentResolver cr = mContext.getContentResolver();
                int id = cr.delete(Favorites.getContentUri(folderId, false), null, null);
                LauncherLog.w(TAG, id + "folder isEmpty, start delete... ");
            }
            return folderId;
        }

        public long parseAndAdd(XmlResourceParser parser, Resources res) throws XmlPullParserException, IOException {
            return parseAndAdd(parser, res);
        }
    }

    protected static String getAttributeValue(XmlPullParser parser, String attribute) {
        String value = parser.getAttributeValue(
                "http://schemas.android.com/apk/res-auto/com.android.launcher3", attribute);
        if (value == null) {
            value = parser.getAttributeValue(null, attribute);
        }
        return value;
    }

    private void fillTmpArray(ArrayList<Long> workspaceScreens) {
        int startSearchPageIndex = workspaceScreens.isEmpty() ? 0 :
                mSourceRes.getInteger(R.integer.config_workspaceInstalledAppStartScreen);
        Pair<Long, int[]> coords = LauncherModel.findNextAvailableIconSpace(mContext,
                null, null, startSearchPageIndex, workspaceScreens);
        if (coords == null) {
            LauncherProvider lp = LauncherAppState.getLauncherProvider();

            int numPagesToAdd = Math.max(1, startSearchPageIndex + 1 -
                    workspaceScreens.size());
            while (numPagesToAdd > 0) {
                long screenId = lp.generateNewScreenId();
                workspaceScreens.add(screenId);
                numPagesToAdd--;
            }
            coords = LauncherModel.findNextAvailableIconSpace(mContext,
                    null, null, startSearchPageIndex, workspaceScreens);
        }
        if (coords == null) {
            throw new RuntimeException("Coordinates should not be null");
        }
        mTemp[1] = coords.first;
        mTemp[2] = coords.second[0];
        mTemp[3] = coords.second[1];
    }

    private void clearItemInfoList() {
        if (mItemInfoList != null) {
            mItemInfoList.clear();
        }
    }

    private void afterParseLayout(ArrayList<Long> screenId) {
        clearItemInfoList();
        LauncherModel.updateWorkspaceScreenOrder(mContext, screenId);
    }

    private void beforeParseLayout() {
        clearItemInfoList();
        LauncherModel.loadWorkspaceAppComponentNamesDb(mContext, mItemInfoList);
    }

    private InputStream getDescFileInputStream(String fileName) {
        File lbkFile = LbkUtil.getCommonLbkFileFromPreloadDirectory(fileName);
        if (lbkFile == null) {
            LauncherLog.i(TAG, "not find lbk file in the root directory");
            return null;
        }

        try {
            LauncherLog.i(TAG, "getDescFileInputStream : " + lbkFile.getAbsolutePath());
            return LbkUtil.unZip(lbkFile.getAbsolutePath(), LbkUtil.DESC_FILE);
        } catch (Exception e) {
            LauncherLog.w(TAG, "error happened in upzip file");
            return null;
        }
    }
}
