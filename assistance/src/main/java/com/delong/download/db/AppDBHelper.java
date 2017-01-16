package com.delong.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

class AppDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "app_info_dl.db";

    private static final int DB_VERSION = 1;

    private static AppDBHelper mDatabaseHelper;

    public synchronized static AppDBHelper getInstance(Context context) {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new AppDBHelper(context.getApplicationContext());
        }
        return mDatabaseHelper;
    }

    public synchronized static void destroy() {
        if (mDatabaseHelper != null) {
            mDatabaseHelper.close();
            mDatabaseHelper = null;
        }
    }

    private AppDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBSQLManager.AppsTable.getCreateSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
