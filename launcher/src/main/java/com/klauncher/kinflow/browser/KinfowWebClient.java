package com.klauncher.kinflow.browser;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by xixionghui on 2016/3/16.
 */
public class KinfowWebClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}
