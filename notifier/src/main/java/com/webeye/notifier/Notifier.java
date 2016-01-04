package com.webeye.notifier;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog.Calls;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class Notifier extends Service {
    public static final String TAG = "WebeyeNotifier";
    public static final boolean DEBUG = true;

    // Unread preference file
    private static final String PREF_UNREAD_COUNT = "unread_count";

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

    /**
     * Lenovo-SW zhaoxin5 20150828 KOLEOSROW-1400 KOLEOSROW-1398 START
     */
    private Handler mHandler = new Handler();
    private static int sPostDelayed = 2 * 1000;
    private Runnable mDialerRunnable = new Runnable() {
        @Override
        public void run() {
            int missedCallNum = getMissedCallNum(Notifier.this);
            if (DEBUG) {
                Log.i(TAG, "mUnreadCallLogObserver : " + missedCallNum);
            }
            sendUnreadBroadcast(mDailer, missedCallNum);
        }
    };
    private Runnable mMessageRunnable = new Runnable() {

        @Override
        public void run() {
            int missedMessageNum = getUnreadMessageCount(Notifier.this);
            if (DEBUG) {
                Log.e(TAG, "mUnreadMessageObserver : " + missedMessageNum/*, new IllegalArgumentException()*/);
            }
            sendUnreadBroadcast(mMessage, missedMessageNum);
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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        if (DEBUG) {
            Log.i(TAG, "onCreate");
        }
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
    }

    private void registerUnreadObservers() {
        getContentResolver().registerContentObserver(Calls.CONTENT_URI, true,
                mUnreadCallLogObserver);
        getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true,
                mUnreadMessageObserver);
    }

    private void unregisterUnreadObservers() {
        if (null != mUnreadCallLogObserver) {
            getContentResolver().unregisterContentObserver(mUnreadCallLogObserver);
        }
        if (null != mUnreadMessageObserver) {
            getContentResolver().unregisterContentObserver(mUnreadMessageObserver);
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
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) !=
                    PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "No permission to read call log");
                return 0;
            }
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