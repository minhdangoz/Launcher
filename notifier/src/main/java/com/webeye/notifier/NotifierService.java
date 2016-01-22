package com.webeye.notifier;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog.Calls;
import android.util.Log;

import java.util.LinkedList;

public class NotifierService extends Service {
    public static final String TAG = "WebeyeNotifier";
    public static final boolean DEBUG = true;

    // Unread broadcast intent and extra name.
    private static final String ACTION_UNREAD_CHANGED = "com.android.launcher.action.UNREAD_CHANGED";
    private static final String ACTION_UNREAD_EXTRA_COMPONENT = "component_name";
    private static final String ACTION_UNREAD_EXTRA_COUNT = "unread_number";

    // Notify component.
    private static final String DIALER_PACKAGE_NAME = "com.lenovo.ideafriend";
    private static final String DIALER_CLASS_NAME = "com.lenovo.ideafriend.alias.DialtactsActivity";
    private static final String MESSAGE_PAKCAGE_NAME = "com.lenovo.ideafriend";
    private static final String MESSAGE_CLASS_NAME = "com.lenovo.ideafriend.alias.MmsActivity";

    private static final ComponentName mDailer = new ComponentName(DIALER_PACKAGE_NAME,
            DIALER_CLASS_NAME);
    private static final ComponentName mMessage = new ComponentName(MESSAGE_PAKCAGE_NAME,
            MESSAGE_CLASS_NAME);

    ContentResolver mReslover;
    /**
     * Lenovo-SW zhaoxin5 20150828 KOLEOSROW-1400 KOLEOSROW-1398 START
     */
    private Handler mHandler = new Handler();
    private static int sPostDelayed = 2 * 1000;
    private Runnable mDialerRunnable = new Runnable() {
        @Override
        public void run() {
            int missedCallNum = getMissedCallNum(NotifierService.this);
            if (DEBUG) {
                Log.i(TAG, "mUnreadCallLogObserver : " + missedCallNum);
            }
            sendUnreadBroadcast(mDailer, missedCallNum);
        }
    };
    private Runnable mMessageRunnable = new Runnable() {

        @Override
        public void run() {
            int missedMessageNum = getUnreadMessageCount(NotifierService.this);
            if (DEBUG) {
                Log.e(TAG, "mUnreadMessageObserver : " + missedMessageNum/*, new IllegalArgumentException()*/);
            }
            sendUnreadBroadcast(mMessage, missedMessageNum);
        }
    };
    private Runnable mCommonNotifierRunnable = new Runnable() {
        @Override
        public void run() {
            LinkedList<NotifierEntry> notifierList = getAllNotifiers();
            for (NotifierEntry notifier : notifierList) {
                Log.e(TAG, "Notifier : " + notifier);
                sendUnreadBroadcast(new ComponentName(notifier.getPackageName(), notifier.getClassName()),
                        notifier.getBadgeCount());
            }
        }
    };
    /**
     * Lenovo-SW zhaoxin5 20150828 KOLEOSROW-1400 KOLEOSROW-1398 END
     */

    private ContentObserver mUnreadMessageObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            // Send new message intent.
            if (DEBUG) {
                Log.i(TAG, "onChange mUnreadMessageObserver");
            }
            mHandler.removeCallbacks(mMessageRunnable);
            mHandler.postDelayed(mMessageRunnable, sPostDelayed);
            super.onChange(selfChange);
        }
    };

    private ContentObserver mUnreadCallLogObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // Send new call log intent.
            if (DEBUG) {
                Log.i(TAG, "onChange mUnreadCallLogObserver");
            }
            mHandler.removeCallbacks(mDialerRunnable);
            mHandler.postDelayed(mDialerRunnable, sPostDelayed);
            super.onChange(selfChange);
        }
    };

    private ContentObserver mCommonNotifierObserver =  new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (DEBUG) {
                Log.i(TAG, "onChange mUnreadCallLogObserver");
            }
            mHandler.removeCallbacks(mCommonNotifierRunnable);
            mHandler.postDelayed(mCommonNotifierRunnable, sPostDelayed);
            super.onChange(selfChange);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (DEBUG) {
            Log.i(TAG, "onCreate");
        }

        mReslover = getContentResolver();

        // Register Unread observers.
        registerUnreadObservers();
        checkUnreadCountAfterBootCompleted();
        registerCheckUnreadReceiver();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.i(TAG, "onDestroy");
        }
        unregisterUnreadObservers();
        unregisterCheckUnreadReceiver();
        super.onDestroy();
    }

    private void checkUnreadCountAfterBootCompleted() {
        if (DEBUG) {
            Log.i(TAG, "checkUnreadCountAfterBootCompleted");
        }
        mHandler.removeCallbacks(mMessageRunnable);
        mHandler.postDelayed(mMessageRunnable, sPostDelayed);

        mHandler.removeCallbacks(mDialerRunnable);
        mHandler.postDelayed(mDialerRunnable, sPostDelayed);

        mHandler.removeCallbacks(mCommonNotifierRunnable);
        mHandler.postDelayed(mCommonNotifierRunnable, sPostDelayed);
    }

    private void registerUnreadObservers() {
        getContentResolver().registerContentObserver(Calls.CONTENT_URI, true,
                mUnreadCallLogObserver);
        getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true,
                mUnreadMessageObserver);
        getContentResolver().registerContentObserver(NotifierConsts.CONTENT_URI, true,
                mCommonNotifierObserver);
    }

    private void unregisterUnreadObservers() {
        if (null != mUnreadCallLogObserver) {
            getContentResolver().unregisterContentObserver(mUnreadCallLogObserver);
        }
        if (null != mUnreadMessageObserver) {
            getContentResolver().unregisterContentObserver(mUnreadMessageObserver);
        }
        if (null != mCommonNotifierObserver) {
            getContentResolver().unregisterContentObserver(mCommonNotifierObserver);
        }
    }

    private void sendUnreadBroadcast(ComponentName componentName, int unreadCount) {
        Intent intent = new Intent();
        intent.setAction(ACTION_UNREAD_CHANGED);
        intent.putExtra(ACTION_UNREAD_EXTRA_COMPONENT, componentName);
        intent.putExtra(ACTION_UNREAD_EXTRA_COUNT, unreadCount);
        sendBroadcast(intent);
    }

    private static int getMissedCallNum(Context context) {
        int newCallNumber = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Calls.CONTENT_URI,
                    new String[]{
                            Calls.NEW
                    },
                    "type=" + Calls.MISSED_TYPE + " AND new=1", null, null);
            if (cursor != null) {
                newCallNumber = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (DEBUG) {
            Log.i(TAG, "getMissedCallNum - newCallNumber : " + newCallNumber);
        }
        return newCallNumber;
    }

    private int getUnreadMessageCount(Context context) {
        int count = getUnreadSmsCount(context) + getUnreadMmsCount(context);
        if (DEBUG) {
            Log.i(TAG, "getUnreadMessageCount : " + count);
        }
        return count;
    }

    /**
     * Get unread SMS count
     *
     * @param context the context
     * @return unread SMS count.
     */
    private static int getUnreadSmsCount(Context context) {
        int newSmsCount = 0;
        Cursor csr = null;
        try {
            csr = context.getContentResolver().query(
                    Uri.parse("content://sms/inbox"), new String[]{
                            "_id"
                    },
                    "read = 0", null, null);
            if (csr != null) {
                newSmsCount = csr.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csr != null) {
                csr.close();
            }
        }

        if (DEBUG) {
            Log.i(TAG, "getUnreadSmsCount : " + newSmsCount);
        }

        return newSmsCount;
    }

    /**
     * Get unread MMS count
     *
     * @param context the Context
     * @return the unread MMS count.
     */
    private static int getUnreadMmsCount(Context context) {
        int newMmsCount = 0;
        Cursor csr = null;
        try {
            csr = context
                    .getContentResolver()
                    .query(
                            Uri.parse("content://mms/inbox"),
                            new String[]{
                                    "_id"
                            },
                            "read = 0 and m_type != 128 and m_type != 129 and m_type != 134 and m_type!=136 and m_type!=135",
                            null, null);
            if (csr != null) {
                newMmsCount = csr.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csr != null) {
                csr.close();
            }
        }

        if (DEBUG) {
            Log.i(TAG, "getUnreadSmsCount : " + newMmsCount);
        }

        return newMmsCount;
    }

    public LinkedList<NotifierEntry> getAllNotifiers() {
        LinkedList<NotifierEntry> articles = new LinkedList<>();

        String[] projection = new String[] {
                NotifierConsts.ID,
                NotifierConsts.PACKAGE,
                NotifierConsts.CLASS,
                NotifierConsts.BADGE_COUNT
        };

        Cursor cursor = mReslover.query(NotifierConsts.CONTENT_URI, projection, null, null,
                NotifierConsts.DEFAULT_SORT_ORDER);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String packageName = cursor.getString(1);
                String className = cursor.getString(2);
                int badgeCount = cursor.getInt(3);

                NotifierEntry article = new NotifierEntry(id, packageName, className, badgeCount);
                articles.add(article);
            } while(cursor.moveToNext());
        }
        cursor.close();

        return articles;
    }

    /**
     * Lenovo-SW zhaoxin5 20150916 KOLEOSROW-2238 KOLEOSROW-698 START
     */
    CheckUnreadReceiver mCheckUnreadReceiver = new CheckUnreadReceiver();
    static String CHECK_UNREAD_BROADCAST = "com.lenovo.notifier.CHECK_UNREAD_BROADCAST";

    class CheckUnreadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.i(TAG, "CheckUnreadReceiver");
            mHandler.removeCallbacks(mMessageRunnable);
            mHandler.postDelayed(mMessageRunnable, sPostDelayed);

            mHandler.removeCallbacks(mDialerRunnable);
            mHandler.postDelayed(mDialerRunnable, sPostDelayed);

            mHandler.removeCallbacks(mCommonNotifierRunnable);
            mHandler.postDelayed(mCommonNotifierRunnable, sPostDelayed);
        }
    }

    void unregisterCheckUnreadReceiver() {
        Log.i(TAG, "unregisterCheckUnreadReceiver");
        if (null != mCheckUnreadReceiver) {
            this.unregisterReceiver(mCheckUnreadReceiver);
        }
    }

    void registerCheckUnreadReceiver() {
        Log.i(TAG, "registerCheckUnreadReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(CHECK_UNREAD_BROADCAST);
        this.registerReceiver(mCheckUnreadReceiver, filter);
    }
    /** Lenovo-SW zhaoxin5 20150916 KOLEOSROW-2238 KOLEOSROW-698 END */
}