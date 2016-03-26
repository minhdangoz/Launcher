// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-

package com.klauncher.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import com.klauncher.ext.LauncherLog;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRouteParams;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class ProxyUtils {
    private static final String TAG = "ProxyUtils";
    private static final boolean NEED_SYNC_PROXY = checkIfNeedSyncProxy();

    private ProxyUtils() {
    }

    public static boolean needSyncProxy() {
        return NEED_SYNC_PROXY;
    }

    private static boolean checkIfNeedSyncProxy() {
        if (Build.VERSION.SDK_INT >= 19/*Build.VERSION_CODES.KITKAT*/) {
            String firmwareVersion = Build.VERSION.RELEASE;
            int versionIdx = firmwareVersion.indexOf("4.4.");
            if (versionIdx >= 0 && versionIdx + 4 < firmwareVersion.length() &&
                firmwareVersion.charAt(versionIdx + 4) <= '3') {
                LauncherLog.d(TAG, "Android version: " + firmwareVersion + ", need sync proxy.");
                return true;
            }
        }
        return false;
    }

    public static void configHttpClientProxy(HttpClient httpClient, Context context, URI uri) {
        HttpHost httpProxy = getHttpProxy(context, uri);
        if (httpProxy != null) {
            httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, httpProxy);
        }
    }

    public static void updateHttpClientProxy(HttpClient httpClient, Context context, URI uri) {
        HttpHost newProxy = getHttpProxy(context, uri);
        Object tmp = httpClient.getParams().getParameter(ConnRouteParams.DEFAULT_PROXY);
        HttpHost oldProxy = (tmp != null) ? (HttpHost) tmp : null;
        boolean proxyChanged = false;

        if (newProxy == null && oldProxy == null) {
            proxyChanged = true; // always set ConnRouteParams.NO_HOST if no system default proxy
        } else if (newProxy == null && oldProxy != null) {
            if (oldProxy.getSchemeName().equalsIgnoreCase("no-host")) {
                proxyChanged = false;
            }else {
                proxyChanged = true;
            }
        } else if (newProxy != null && oldProxy == null) {
            proxyChanged = true;
        } else if (oldProxy.toString().equals(newProxy.toString())) {
            proxyChanged = false;
        } else {
            proxyChanged = true;
        }

        if (!proxyChanged) {
            return;
        }

        if (newProxy != null) {
            httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, newProxy);
        } else {
            httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
                    ConnRouteParams.NO_HOST);
        }
    }

    @SuppressWarnings("deprecation")
    private static Proxy getProxy(Context context, URI uri) {
        assert context != null;

        Proxy proxy = Proxy.NO_PROXY;
        NetworkInfo netInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            String host = android.net.Proxy.getHost(context);
            int port = android.net.Proxy.getPort(context);
            if (!TextUtils.isEmpty(host)) {
                port = port != -1 ? port : 80;
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            }
        } else {
            try {
                if (needSyncProxy()) {
                    synchronized (ProxyUtils.class) {
                        proxy = ProxySelector.getDefault().select(uri).get(0);
                    }
                } else {
                    proxy = ProxySelector.getDefault().select(uri).get(0);
                }
            } catch (IllegalArgumentException e) {
            }
        }

        return proxy;
    }

    public static HttpHost getHttpProxy(Context context) {
        try {
            return getHttpProxy(context, new URI("http://www.baidu.com"));
        } catch (URISyntaxException e) {
        }
        return null;
    }

    public static boolean isWapNetwork(Context context){
        try {
            return isWapProxy(getProxy(context, new URI("http://www.baidu.com")));
        } catch (URISyntaxException e){
        }
        return false;
    }

    private static final Set<String> WAP_GATEWAYS = new HashSet<String>();

    static {
        WAP_GATEWAYS.add("10.0.0.172"); // cmwap/uniwap/3gwap
        WAP_GATEWAYS.add("10.0.0.200"); // ctwap
    }

    private static boolean isWapProxy(Proxy proxy) {
        if (proxy == null || proxy.type() != Proxy.Type.HTTP) {
            return false;
        }

        if (proxy.address() instanceof InetSocketAddress) {
            InetSocketAddress inetSockAddr = (InetSocketAddress) proxy.address();

            if (WAP_GATEWAYS.contains(inetSockAddr.getHostName())) {
                return true;
            }

            InetAddress inetAddr = inetSockAddr.getAddress();
            if (inetAddr != null) {
                return WAP_GATEWAYS.contains(inetAddr.getHostAddress());
            }
        }

        return false;
    }

    private static HttpHost getHttpProxy(Context context, URI uri) {

        String proxyHost = null;
        int proxyPort = -1;
        try {
            Proxy p = getProxy(context, uri);
            if (p != null && p.type() == Proxy.Type.HTTP) {
                java.net.SocketAddress socketAddress = p.address();
                if (socketAddress instanceof InetSocketAddress) {
                    InetAddress inetAddr = ((InetSocketAddress) socketAddress).getAddress();
                    proxyHost = (inetAddr != null) ? inetAddr.getHostAddress()
                            : ((InetSocketAddress) socketAddress).getHostName();
                    proxyPort = ((InetSocketAddress) socketAddress).getPort();
                }
            }
        } catch (Exception e) {
            LauncherLog.d(TAG, "get system proxy error");
        }
        LauncherLog.d(TAG, "Proxy Host:" + proxyHost + " Port:" + String.valueOf(proxyPort));
        if (!TextUtils.isEmpty(proxyHost)) {
            if (Build.VERSION.SDK_INT >= 21/*Build.VERSION_CODES.LOLLIPOP*/ && proxyHost.startsWith("127.0.0.1")) {
                return null;
            }
            proxyPort = (proxyPort == -1 || proxyPort == 0) ? 80 : proxyPort;
            return new HttpHost(proxyHost, proxyPort);
        }
        return null;
    }
}
