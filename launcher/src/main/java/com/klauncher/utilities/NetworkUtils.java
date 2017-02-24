package com.klauncher.utilities;

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
}
