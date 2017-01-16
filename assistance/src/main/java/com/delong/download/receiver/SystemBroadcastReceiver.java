package com.delong.download.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 15/11/21.
 */
public class SystemBroadcastReceiver extends BroadcastReceiver {

    private static List<ConnectivityChangeListener> mConnectivityChangeListenerList = new ArrayList<>();
    private static List<PackageActiveListener> mPackageActiveListenerList = new ArrayList<>();
    private static List<BootListener> mBootListenerList = new ArrayList<>();
    private static List<SDCardActionListener> mSDCardActionListenerList = new ArrayList<>();


    @Override
    public void onReceive(Context context, Intent intent) {

        if (TextUtils.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            int networkType = -1;
            boolean isAvailable = false;
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        isAvailable = true;
                        networkType = info.getType();
                    }
                }
            }
            for (ConnectivityChangeListener l : mConnectivityChangeListenerList) {
                l.onConnectivityChange(isAvailable, networkType);
            }

            return;
        }

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {
            String pkg = intent.getData().getSchemeSpecificPart();
            for (PackageActiveListener l : mPackageActiveListenerList) {
                l.onPackageInstall(context, pkg);
            }
            return;
        }

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {
            String pkg = intent.getData().getSchemeSpecificPart();
            for (PackageActiveListener l : mPackageActiveListenerList) {
                l.onPackageUninstall(context, pkg);
            }
            return;
        }

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED)) {
            String pkg = intent.getData().getSchemeSpecificPart();
            for (PackageActiveListener l : mPackageActiveListenerList) {
                l.onPackageReplaced(context, pkg);
            }
            return;
        }

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_MEDIA_EJECT)
                || TextUtils.equals(intent.getAction(), Intent.ACTION_MEDIA_REMOVED)
                || TextUtils.equals(intent.getAction(), Intent.ACTION_MEDIA_UNMOUNTED)) {

            return;
        }

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_MEDIA_MOUNTED)) {

            return;
        }

    }

    public static void registerConnectivityChangeListener(ConnectivityChangeListener l) {
        synchronized (mConnectivityChangeListenerList) {
            mConnectivityChangeListenerList.add(l);
        }
    }

    public static void unregisterConnectivityChangeListener(ConnectivityChangeListener l) {
        synchronized (mConnectivityChangeListenerList) {
            mConnectivityChangeListenerList.remove(l);
        }
    }

    public static void registerPackageActiveListener(PackageActiveListener l) {
        synchronized (mPackageActiveListenerList) {
            mPackageActiveListenerList.add(l);
        }
    }

    public static void unregisterPackageActiveListener(PackageActiveListener l) {
        synchronized (mPackageActiveListenerList) {
            mPackageActiveListenerList.remove(l);
        }
    }

    public static void registerBootListener(BootListener l) {
        synchronized (mBootListenerList) {
            mBootListenerList.add(l);
        }
    }

    public static void unregisterBootListener(BootListener l) {
        synchronized (mBootListenerList) {
            mBootListenerList.remove(l);
        }
    }

    public static void registerSDCardActionListener(SDCardActionListener l) {
        synchronized (mSDCardActionListenerList) {
            mSDCardActionListenerList.add(l);
        }
    }

    public static void unregisterSDCardActionListener(SDCardActionListener l) {
        synchronized (mSDCardActionListenerList) {
            mSDCardActionListenerList.remove(l);
        }
    }

    public interface ConnectivityChangeListener {
        void onConnectivityChange(boolean isNetworkAvailable, int netType);
    }

    public interface PackageActiveListener {
        void onPackageInstall(Context context, String pkg);

        void onPackageUninstall(Context context, String pkg);

        void onPackageReplaced(Context context, String pkg);
    }

    public interface BootListener {

    }

    public interface SDCardActionListener {
        void onSDCardMount();

        void onSDCardRemoved();
    }


}
