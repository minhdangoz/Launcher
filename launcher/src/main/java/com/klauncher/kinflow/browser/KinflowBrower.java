package com.klauncher.kinflow.browser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.klauncher.launcher.R;


/**
 * Created by xixionghui on 2016/3/16.
 */
public class KinflowBrower extends Activity {

    public static void openUrl(Context context, String url) {
        Intent defaultBrower = new Intent(context, KinflowBrower.class);
        defaultBrower.putExtra(KinflowBrower.KEY_EXTRA_URL, url);
        context.startActivity(defaultBrower);
    }

    public static final String KEY_EXTRA_URL = "";

    private WebView mWebView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        initWebView();

        Intent intent = getIntent();
        url = intent.getStringExtra(KEY_EXTRA_URL);

        mWebView.loadUrl(url);
    }

    void initWebView(){
        mWebView = (WebView) findViewById(R.id.webView);

        WebSettings webSettings = mWebView.getSettings();//通过WevView获取WevSitting
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);


        mWebView.setWebViewClient(new KinfowWebClient());


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}