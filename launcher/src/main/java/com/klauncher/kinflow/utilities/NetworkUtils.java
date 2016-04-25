package com.klauncher.kinflow.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;

public class NetworkUtils {

    public static Proxy getProxy(URI uri) {
        Proxy proxy = Proxy.NO_PROXY;
        try {
            proxy = ProxySelector.getDefault().select(uri).get(0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return proxy;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
