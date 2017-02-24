package com.delong.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.delong.assistance.bean.AppInfo;
import com.delong.assistance.bean.ServerAppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuruqiao on 2016/12/6.
 */

public class AppsDao {

    private static SQLiteDatabase mWritableDB;

    private static SQLiteDatabase mReadableDB;

    private static SQLiteDatabase getWritableDB(Context ctx) {
        if (mWritableDB == null || !mWritableDB.isOpen()) {
            AppDBHelper helper = AppDBHelper.getInstance(ctx);
            mWritableDB = helper.getWritableDatabase();
        }
        return mWritableDB;

    }

    private static SQLiteDatabase getReadableDB(Context ctx) {
        if (mReadableDB == null || !mReadableDB.isOpen()) {
            AppDBHelper helper = AppDBHelper.getInstance(ctx);
            mReadableDB = helper.getReadableDatabase();
        }
        return mReadableDB;
    }

    public static boolean insert(Context ctx, ServerAppInfo info) {


        SQLiteDatabase db = null;
        long insert = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(DBSQLManager.AppsTable.Column.APP_NAME.name, info.appName);
            values.put(DBSQLManager.AppsTable.Column.PKG_NAME.name, info.pkgName);
            values.put(DBSQLManager.AppsTable.Column.VER_CODE.name, info.versionCode);
            values.put(DBSQLManager.AppsTable.Column.CATEGORY.name, info.category);
            values.put(DBSQLManager.AppsTable.Column.TASK_ID.name, info.downloadTask==null?info.taskId: info.downloadTask.getId());
            values.put(DBSQLManager.AppsTable.Column.DOWNLOAD_URL.name, info.downloadUrl);
            values.put(DBSQLManager.AppsTable.Column.ICON_URL.name, info.iconUrl);
            values.put(DBSQLManager.AppsTable.Column.FILE_SIZE.name, info.fileSize);
            values.put(DBSQLManager.AppsTable.Column.CURRENT_LENGTH.name, info.currentLength);
            values.put(DBSQLManager.AppsTable.Column.CREATE_TIME.name, info.createTime==0?System.currentTimeMillis():info.createTime);
            values.put(DBSQLManager.AppsTable.Column.CRC.name, info.crc);
            values.put(DBSQLManager.AppsTable.Column.MD5.name, info.fileMd5);
            values.put(DBSQLManager.AppsTable.Column.APP_ID.name, info.appId);
            values.put(DBSQLManager.AppsTable.Column.APP_STATUS.name, info.status.get());
            values.put(DBSQLManager.AppsTable.Column.APP_TYPE.name, info.appType.get());
            db = getWritableDB(ctx);
            insert = db.insert(DBSQLManager.AppsTable.TABLE_NAME, null, values);

            db.close();
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            return false;
        }
        return insert == -1 ? false : true;
    }


    public static boolean updateCurrentLength(Context ctx, ServerAppInfo info) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        int update = 0;
        try {
            db = getWritableDB(ctx);
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBSQLManager.AppsTable.Column.CURRENT_LENGTH.name, info.currentLength);
            update = db.update(DBSQLManager.AppsTable.TABLE_NAME
                    , contentValues, DBSQLManager.AppsTable.Column.DOWNLOAD_URL.name + "=?"
                    , new String[]{info.downloadUrl + ""});
            if (update == 1) {
                return true;
            }
            cursor.close();
            db.close();
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * 查询当前app在下载任务中是否存在
     *
     * @param info
     * @return
     */
    public static ServerAppInfo query(Context ctx, ServerAppInfo info) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDB(ctx);
            cursor = db.query(DBSQLManager.AppsTable.TABLE_NAME, null
                    , DBSQLManager.AppsTable.Column.DOWNLOAD_URL.name + "=?"
                    , new String[]{info.downloadUrl}, null, null, null);
            if (cursor.getCount() == 1) {
                cursor.moveToNext();
                info.currentLength = cursor.getLong(DBSQLManager.AppsTable.Column.CURRENT_LENGTH.index);

            }
            cursor.close();
            db.close();
            return info;
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;

    }

    /**
     * 查询获取所有下载记录
     *
     * @param
     * @return List<ServerAppInfo>
     */
    public static List<ServerAppInfo> getDownloadAppInfo(Context ctx) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<ServerAppInfo> appInfos = new ArrayList<>();
        try {
            db = getReadableDB(ctx);
            cursor = db.query(DBSQLManager.AppsTable.TABLE_NAME, null
                    , DBSQLManager.AppsTable.Column.APP_STATUS.name + "=? or " + DBSQLManager.AppsTable.Column.APP_STATUS.name + " =?"
                    , new String[]{ServerAppInfo.FINISHED + "", ServerAppInfo.PAUSE + ""}, null, null, null);
            while (cursor.moveToNext()) {
                ServerAppInfo serverAppInfo = new ServerAppInfo();
                serverAppInfo.appName = cursor.getString(DBSQLManager.AppsTable.Column.APP_NAME.index);
                serverAppInfo.pkgName = cursor.getString(DBSQLManager.AppsTable.Column.PKG_NAME.index);
                serverAppInfo.versionCode = cursor.getInt(DBSQLManager.AppsTable.Column.VER_CODE.index);
                serverAppInfo.category = cursor.getInt(DBSQLManager.AppsTable.Column.CATEGORY.index);
                serverAppInfo.downloadUrl = cursor.getString(DBSQLManager.AppsTable.Column.DOWNLOAD_URL.index);
                serverAppInfo.iconUrl = cursor.getString(DBSQLManager.AppsTable.Column.ICON_URL.index);
                serverAppInfo.fileSize = cursor.getLong(DBSQLManager.AppsTable.Column.FILE_SIZE.index);
                serverAppInfo.currentLength = cursor.getLong(DBSQLManager.AppsTable.Column.CURRENT_LENGTH.index);
                serverAppInfo.crc = cursor.getString(DBSQLManager.AppsTable.Column.CRC.index);
                serverAppInfo.fileMd5 = cursor.getString(DBSQLManager.AppsTable.Column.MD5.index);
                serverAppInfo.appId = cursor.getInt(DBSQLManager.AppsTable.Column.APP_ID.index);
                serverAppInfo.appType.set(cursor.getInt(DBSQLManager.AppsTable.Column.APP_TYPE.index));
                serverAppInfo.status.set(cursor.getInt(DBSQLManager.AppsTable.Column.APP_STATUS.index));
                appInfos.add(serverAppInfo);
            }
            cursor.close();
            db.close();
            return appInfos;
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return appInfos;

    }


    /**
     * 获取去除用户删除后的所有app
     *
     * @return
     */
    public static List<ServerAppInfo> getAllAppWhithOutForbidden(Context ctx, int category) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<ServerAppInfo> appInfos = new ArrayList<>();
        try {
            db = getReadableDB(ctx);
            cursor = db.query(DBSQLManager.AppsTable.TABLE_NAME, null
                    , DBSQLManager.AppsTable.Column.CATEGORY.name + "=? or " + DBSQLManager.AppsTable.Column.CATEGORY.name + "=?"
                    , new String[]{category + "", AppInfo.GAY + ""}, null, null, DBSQLManager.AppsTable.Column.CREATE_TIME.name + " ASC", null);
            while (cursor.moveToNext()) {
                ServerAppInfo serverAppInfo = new ServerAppInfo();
                serverAppInfo.appName = cursor.getString(DBSQLManager.AppsTable.Column.APP_NAME.index);
                serverAppInfo.pkgName = cursor.getString(DBSQLManager.AppsTable.Column.PKG_NAME.index);
                serverAppInfo.versionCode = cursor.getInt(DBSQLManager.AppsTable.Column.VER_CODE.index);
                serverAppInfo.category = cursor.getInt(DBSQLManager.AppsTable.Column.CATEGORY.index);
                serverAppInfo.downloadUrl = cursor.getString(DBSQLManager.AppsTable.Column.DOWNLOAD_URL.index);
                serverAppInfo.iconUrl = cursor.getString(DBSQLManager.AppsTable.Column.ICON_URL.index);
                serverAppInfo.fileSize = cursor.getLong(DBSQLManager.AppsTable.Column.FILE_SIZE.index);
                serverAppInfo.currentLength = cursor.getLong(DBSQLManager.AppsTable.Column.CURRENT_LENGTH.index);
                serverAppInfo.crc = cursor.getString(DBSQLManager.AppsTable.Column.CRC.index);
                serverAppInfo.fileMd5 = cursor.getString(DBSQLManager.AppsTable.Column.MD5.index);
                serverAppInfo.appId = cursor.getInt(DBSQLManager.AppsTable.Column.APP_ID.index);
                serverAppInfo.appType.set(cursor.getInt(DBSQLManager.AppsTable.Column.APP_TYPE.index));
                serverAppInfo.status.set(cursor.getInt(DBSQLManager.AppsTable.Column.APP_STATUS.index));
                appInfos.add(serverAppInfo);
            }
            cursor.close();
            db.close();
            return appInfos;
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return appInfos;
    }


    /**
     * 根据应用名称删除表中的某个应用
     *
     * @param info
     * @return
     */
    public static boolean deleteApp(Context ctx, AppInfo info) {

        SQLiteDatabase db = null;
        try {
            db = getReadableDB(ctx);

            int delete = db.delete(DBSQLManager.AppsTable.TABLE_NAME
                    , DBSQLManager.AppsTable.Column.PKG_NAME.name + "= ?"
                    , new String[]{info.pkgName});

            db.close();
            if (delete == 1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    public static boolean updateStatus(Context ctx, ServerAppInfo appInfo) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        int update = 0;
        try {
            db = getWritableDB(ctx);
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBSQLManager.AppsTable.Column.APP_STATUS.name, appInfo.status.get());
            update = db.update(DBSQLManager.AppsTable.TABLE_NAME
                    , contentValues, DBSQLManager.AppsTable.Column.APP_ID.name + "=?"
                    , new String[]{appInfo.appId + ""});
            if (update == 1) {
                return true;
            }
            cursor.close();
            db.close();
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }


    /**
     * 更改状态
     *
     * @param appInfo
     * @return
     */

    public static boolean updateCategory(Context ctx, ServerAppInfo appInfo) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        int update = 0;
        try {
            db = getWritableDB(ctx);
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBSQLManager.AppsTable.Column.CATEGORY.name, appInfo.category);
            update = db.update(DBSQLManager.AppsTable.TABLE_NAME
                    , contentValues, DBSQLManager.AppsTable.Column.APP_NAME.name + "=?"
                    , new String[]{appInfo.appName + ""});
            if (update == 1) {
                return true;
            }
            cursor.close();
            db.close();
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * 获取app的状态
     *
     * @param appInfo
     * @return
     */
    public static int getAppCategory(Context ctx, ServerAppInfo appInfo) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDB(ctx);
            cursor = db.query(DBSQLManager.AppsTable.TABLE_NAME, new String[]{DBSQLManager.AppsTable.Column.CATEGORY.name}
                    , DBSQLManager.AppsTable.Column.PKG_NAME.name + "=? "
                    , new String[]{appInfo.pkgName}, null, null, null, null);


            while (cursor.moveToNext()) {
                return cursor.getInt(0);
            }

            cursor.close();
            db.close();
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * 获取app的状态
     *
     * @param appInfo
     * @return
     */
    public static int getAppStatus(Context ctx, ServerAppInfo appInfo) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDB(ctx);
            cursor = db.query(DBSQLManager.AppsTable.TABLE_NAME, new String[]{DBSQLManager.AppsTable.Column.APP_STATUS.name}
                    , DBSQLManager.AppsTable.Column.PKG_NAME.name + "=? "
                    , new String[]{appInfo.pkgName}, null, null, null, null);


            while (cursor.moveToNext()) {
                return cursor.getInt(0);
            }

            cursor.close();
            db.close();
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * 获取所有表中应用
     *
     * @return
     */
    public static List<ServerAppInfo> getAllApps(Context ctx) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<ServerAppInfo> appInfos = new ArrayList<>();
        try {
            db = getReadableDB(ctx);
            cursor = db.query(DBSQLManager.AppsTable.TABLE_NAME, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                ServerAppInfo serverAppInfo = new ServerAppInfo();
                serverAppInfo.appName = cursor.getString(DBSQLManager.AppsTable.Column.APP_NAME.index);
                serverAppInfo.pkgName = cursor.getString(DBSQLManager.AppsTable.Column.PKG_NAME.index);
                serverAppInfo.versionCode = cursor.getInt(DBSQLManager.AppsTable.Column.VER_CODE.index);
                serverAppInfo.category = cursor.getInt(DBSQLManager.AppsTable.Column.CATEGORY.index);
                serverAppInfo.downloadUrl = cursor.getString(DBSQLManager.AppsTable.Column.DOWNLOAD_URL.index);
                serverAppInfo.iconUrl = cursor.getString(DBSQLManager.AppsTable.Column.ICON_URL.index);
                serverAppInfo.fileSize = cursor.getLong(DBSQLManager.AppsTable.Column.FILE_SIZE.index);
                serverAppInfo.currentLength = cursor.getLong(DBSQLManager.AppsTable.Column.CURRENT_LENGTH.index);
                serverAppInfo.createTime = cursor.getLong(DBSQLManager.AppsTable.Column.CREATE_TIME.index);
                serverAppInfo.taskId = cursor.getInt(DBSQLManager.AppsTable.Column.TASK_ID.index);
                serverAppInfo.crc = cursor.getString(DBSQLManager.AppsTable.Column.CRC.index);
                serverAppInfo.fileMd5 = cursor.getString(DBSQLManager.AppsTable.Column.MD5.index);
                serverAppInfo.appId = cursor.getInt(DBSQLManager.AppsTable.Column.APP_ID.index);
                serverAppInfo.appType.set(cursor.getInt(DBSQLManager.AppsTable.Column.APP_TYPE.index));
                serverAppInfo.status.set(cursor.getInt(DBSQLManager.AppsTable.Column.APP_STATUS.index));
                appInfos.add(serverAppInfo);
            }
            cursor.close();
            db.close();
            return appInfos;
        } catch (Exception e) {

            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return appInfos;
    }


}
