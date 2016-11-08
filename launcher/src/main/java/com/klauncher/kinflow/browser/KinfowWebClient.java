package com.klauncher.kinflow.browser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.utilities.KinflowLog;

/**
 * Created by xixionghui on 2016/3/16.
 */
public class KinfowWebClient extends WebViewClient {

    private Context mContext;

    public KinfowWebClient(Context mContext) {
        this.mContext = mContext;
    }

    public Context getmContext() {
        try {
            if (null == mContext) {
                this.mContext = KLauncherApplication.mKLauncherApplication;
            }
            return mContext;
        } catch (Exception e) {
            return KLauncherApplication.mKLauncherApplication;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            if (url.startsWith("http:") || url.startsWith("https:")) {//本webView处理
                return false;
            } else {//启动第三方应用
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    getmContext().startActivity(intent);
                    return true;
                } catch (Exception e) {
                    KinflowLog.e("内置浏览器加载html5时,html5要求启动第三方应用时失败,推测可能是没安装这个应用" + e.getMessage());
                    return false;
                }
            }
        } catch (Exception e) {
            KinflowLog.e("内置浏览器加载html5时,html5要求启动第三方应用时失败,推测可能是没安装这个应用" + e.getMessage());
            return false;
        }
    }
}
