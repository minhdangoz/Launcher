package com.klauncher.biddingos.commons.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.ads.AdAffairBean;
import com.klauncher.biddingos.commons.ads.Constants;
import com.klauncher.biddingos.commons.utils.AESUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.util.Date;


public class BiddingOSDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "BiddingOSDBHelper";
    /**
     * 数据库名称 *
     */
    private static final String DATABASE_NAME = "biddingos"+ Setting.SDK_VERSION+".db";
    /**
     * 数据库版本号 *
     */
    private static final int DATABASE_VERSION = 2;
    /**
     * 数据库表*
     */
    private static final String TBL_AD = "ad";
    /**
     * 广告事务表*
     */
    private static final String TBL_AFFAIR = "affair";

    /**
     * 数据库SQL语句 添加一个表*
     */
    private static final String AD_TABLE_CREATE = " CREATE TABLE IF NOT EXISTS ad " +
            "(listid VARCHAR PRIMARY KEY NOT NULL," +
            "delivery TEXT NOT NULL, " +
            "timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP );\n";

    /**
     * 数据库SQL语句 添加一个表TBL_AFFAIR*
     */
    private static final String AFFAIR_TABLE_CREATE = " CREATE TABLE IF NOT EXISTS affair " +
            "("+ Constants.TRANSACTIONID+" TEXT PRIMARY KEY NOT NULL ,"+
            Constants.IMPRESSIONHAPPEN+" INTEGER NOT NULL,"+
            Constants.IMPRESSIONSTATUS+" INTEGER NOT NULL,"+
            Constants.IMPRESSIONURL+" TEXT NOT NULL,"+
            Constants.CLICKHAPPEN+" INTEGER NOT NULL,"+
            Constants.CLICKSTATUS+" INTEGER NOT NULL,"+
            Constants.CLICKURL+" TEXT NOT NULL,"+
            Constants.CONVERSIONHAPPEN+" INTEGER NOT NULL,"+
            Constants.CONVERSIONSTATUS+" INTEGER NOT NULL,"+
            Constants.CONVERSIONURL+" TEXT NOT NULL,"+
            Constants.CONTENT+" TEXT NOT NULL,"+
            Constants.TIMESTAMP+" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);\n";


    private static BiddingOSDBHelper mInstance = null;

    public static synchronized BiddingOSDBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BiddingOSDBHelper(context);
        }
        return mInstance;
    }

    private BiddingOSDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AD_TABLE_CREATE);
//        db.execSQL(AFFAIR_TABLE_CREATE);
        LogUtils.v(TAG, "Create Table : AD");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //*************AdInfo*****************************

    /**
     * 广告事务插入数据
     */
    public synchronized void insterOrUpdateAdAffairCaches(AdAffairBean adAffairBean) {
        SQLiteDatabase db = getWritableDatabase();
        if (null == db) {
            LogUtils.e(TAG, "getWritableDatabase is null");
            return;
        }
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(Constants.TRANSACTIONID, adAffairBean.getTransactionid());
            values.put(Constants.IMPRESSIONHAPPEN, adAffairBean.getImpressionhappen());
            values.put(Constants.IMPRESSIONSTATUS, adAffairBean.getImpressionstatus());
            values.put(Constants.IMPRESSIONURL, AESUtils.encode("1234567890123456", adAffairBean.getImpressionurl()));
            values.put(Constants.CLICKHAPPEN, adAffairBean.getClickhappen());
            values.put(Constants.CLICKSTATUS, adAffairBean.getClickstatus());
            values.put(Constants.CLICKURL, adAffairBean.getClickurl());
            values.put(Constants.CONVERSIONHAPPEN, adAffairBean.getConversionhappen());
            values.put(Constants.CONVERSIONSTATUS, adAffairBean.getConversionstatus());
            values.put(Constants.CONVERSIONURL, adAffairBean.getConversionurl());
            values.put(Constants.TIMESTAMP, adAffairBean.getTimestamp());
            values.put(Constants.CONTENT, adAffairBean.getcontent());
            db.replace(TBL_AFFAIR, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtils.e(TAG, "insterOrUpdateAdAffairCaches", e);
        } finally{
            db.endTransaction();
            DBUtils.safeClose(db);
        }
    }

    /**
     * 从数据库获取广告事务对象
     * @param transactionid
     * @return
     */
    public synchronized AdAffairBean getAdAffairBeanByTransactionid(String transactionid) {
        SQLiteDatabase db = getReadableDatabase();
        if (null == db) {
            LogUtils.e(TAG, "getReadableDatabase is null");
            return null;
        }
        String[] a =  new String[]{transactionid};

        Cursor cursor = null;
        AdAffairBean adAffairBean=null;
        try {
            cursor = db.query(TBL_AFFAIR, null, Constants.TRANSACTIONID+"=?", a, null, null, null);
            if (null!=cursor && cursor.getCount() > 0 && cursor.moveToNext()) {
                adAffairBean=new AdAffairBean();
                adAffairBean.setTransactionid(cursor.getString(cursor.getColumnIndex(Constants.TRANSACTIONID)));
                adAffairBean.setImpressionhappen(cursor.getInt(cursor.getColumnIndex(Constants.IMPRESSIONHAPPEN)));
                adAffairBean.setImpressionstatus(cursor.getInt(cursor.getColumnIndex(Constants.IMPRESSIONSTATUS)));
                adAffairBean.setImpressionurl(cursor.getString(cursor.getColumnIndex(Constants.IMPRESSIONURL)));
                adAffairBean.setClickhappen(cursor.getInt(cursor.getColumnIndex(Constants.CLICKHAPPEN)));
                adAffairBean.setClickstatus(cursor.getInt(cursor.getColumnIndex(Constants.CLICKSTATUS)));
                adAffairBean.setClickurl(cursor.getString(cursor.getColumnIndex(Constants.CLICKURL)));
                adAffairBean.setConversionhappen(cursor.getInt(cursor.getColumnIndex(Constants.CONVERSIONHAPPEN)));
                adAffairBean.setConversionstatus(cursor.getInt(cursor.getColumnIndex(Constants.CONVERSIONSTATUS)));
                adAffairBean.setConversionurl(cursor.getString(cursor.getColumnIndex(Constants.CONVERSIONURL)));
                adAffairBean.setTimestamp(cursor.getLong(cursor.getColumnIndex(Constants.TIMESTAMP)));
                adAffairBean.setcontent(cursor.getString(cursor.getColumnIndex(Constants.CONTENT)));
            }
        }catch (Exception e) {
            LogUtils.e(TAG, "getAdAffairBeanByTransactionid", e);
        }finally{
            DBUtils.safeClose(cursor);
            DBUtils.safeClose(db);
        }

        return adAffairBean;
    }

    /**
     * 删除一条事务记录
     * @param transactionid
     */
    public synchronized void deleteAdAffairBeanByTransactionid(String transactionid) {
        SQLiteDatabase db = getReadableDatabase();
        if (null == db) {
            LogUtils.e(TAG, "getReadableDatabase is null");
            return;
        }

        String whereClause =  Constants.TRANSACTIONID+"=?";
        String[] whereArgs = new String[] {String.valueOf(transactionid)};

        try {
            db.delete(TBL_AFFAIR, whereClause, whereArgs);
        }catch (Exception e) {
            LogUtils.e(TAG, "deleteAdAffairBeanByTransactionid", e);
        }finally{
            DBUtils.safeClose(db);
        }

    }
    /**
     * 修改一条事务记录
     * @param transactionid
     */
    public synchronized void updateAdAffairBeanByTransactionid(String transactionid,String name,Object value) {
        SQLiteDatabase db = getReadableDatabase();
        if (null == db) {
            LogUtils.e(TAG, "getReadableDatabase is null");
            return;
        }
        String whereClause =  Constants.TRANSACTIONID+"=?";
        String[] whereArgs = new String[] {String.valueOf(transactionid)};

        try {
            ContentValues values = new ContentValues();
            if(Constants.CONTENT.equals(name)) {
                values.put(name, (String) value);
            }else {
                values.put(name, (int) value);
            }
            db.update(TBL_AFFAIR,values,whereClause, whereArgs);
        }catch (Exception e) {
            LogUtils.e(TAG, "updateAdAffairBeanByTransactionid", e);
        }finally{
            DBUtils.safeClose(db);
        }

    }
    public synchronized void insterOrUpdateAdCaches(String listId, String cipherText) {
        SQLiteDatabase db = getWritableDatabase();
        if (null == db) {
            LogUtils.e(TAG, "getWritableDatabase is null");
            return;
        }

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("listid", listId);
            values.put("delivery", cipherText);
            values.put("timestamp", new Date().getTime());
            db.replace(TBL_AD, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
        } finally{
            db.endTransaction();
            DBUtils.safeClose(db);
        }
    }

    public synchronized String getDeliveryDataByListId(String listId) {
        SQLiteDatabase db = getReadableDatabase();
        if (null == db) {
            LogUtils.e(TAG, "getReadableDatabase is null");
            return "";
        }
        String[] a =  new String[]{listId};

        Cursor cursor = null;
        String delivery = "";
        try {
            cursor = db.query(TBL_AD, null, "listid=?", a, null, null, null);
            if (null!=cursor && cursor.getCount() > 0 && cursor.moveToNext()) {
                delivery = cursor.getString(cursor.getColumnIndex("delivery"));
            }
        }catch (Exception e) {
            LogUtils.e(TAG, "getDeliveryDataByListId", e);
        }finally{
            DBUtils.safeClose(cursor);
            DBUtils.safeClose(db);
        }

        return delivery;
    }


//    private synchronized AdInfo toAdInfo(Cursor cursor) {
//        AdInfo adInfo = new AdInfo(cursor);
//        return adInfo;
//    }

    //*************CREATIVE*****************************

//    public synchronized void insterOrUpdateCreativeCaches(ContentValues creative) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.replace(TBL_CREATIVE, null, creative);
//        DBUtils.safeClose(db);
//        return;
//    }
//    public synchronized DataObj searchCreativeCachesByCreative(String creativeId) {
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor cursor = null;
//        try {
//            cursor = db.query(TBL_CREATIVE, null, "creativeid=?", new String[]{creativeId}, null, null, null);
//            if (null != cursor && cursor.getCount() > 0 && cursor.moveToNext()) {
//                return new DataObj(
//                        cursor.getInt(cursor.getColumnIndex("creativeid")),
//                        cursor.getString(cursor.getColumnIndex("assets")),
//                        new Date(cursor.getLong(cursor.getColumnIndex("timestamp")))
//                );
//            }
//            return null;
//        } finally {
//            DBUtils.safeClose(cursor);
//            DBUtils.safeClose(db);
//        }
//    }

//    public synchronized void deleteCreativeCachesByCreative(String creativeId) {
//        try {
//            SQLiteDatabase db = getWritableDatabase();
//            db.delete(TBL_CREATIVE, "creativeid=?", new String[]{creativeId});
//        }catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    public static class DataObj {
//        public int creativeId;
//        public String assets;
//        public Date timestamp;
//
//        public DataObj(int creativeId, String assets, Date timestamp) {
//            this.creativeId = creativeId;
//            this.assets = assets;
//            this.timestamp = timestamp;
//        }
//
//        public int getCreativeId() {
//            return creativeId;
//        }
//
//        public void setCreativeId(int creativeId) {
//            this.creativeId = creativeId;
//        }
//
//        public String getAssets() {
//            return assets;
//        }
//
//        public void setAssets(String assets) {
//            this.assets = assets;
//        }
//
//        public Date getTimestamp() {
//            return timestamp;
//        }
//
//        public void setTimestamp(Date timestamp) {
//            this.timestamp = timestamp;
//        }
//    }


}
