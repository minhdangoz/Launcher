package com.webeye.launcher.ext;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.launcher3.Launcher;
import com.webeye.launcher.R;

public class WeLauncher extends Launcher {
    private static final String TAG = "WeLauncher";

	private static final String HOME_PAGE = "file:///android_asset/home.html";
    private static final String APP_NAME = "今日头题";
    private static final String NEWS_BAIDU = "http://m.baidu.com/news?fr=mohome&ssid=1dc073756e6c6169724503&from=&uid=&pu=sz%40224_220%2Cta%40iphone___3_537&bd_page_type=1";
    private static final String DEFAULT_APP_PACKAGENAME= "com.ss.android.article.news";
    private static final String DEFAULT_APP_CLASS ="com.ss.android.article.news.activity.SplashActivity";
    private static final String APP_DLD_LINK = "http://mobile.baidu.com/simple?action=content&type=soft&docid=8356755";

    private boolean firstRun = true;
    private boolean isShown = false;

    private WebView mWebView;

    @Override
	protected boolean hasCustomContentToLeft() {
		return false;
	}

	@Override
	protected void populateCustomContentContainer() {
		View customView = getLayoutInflater().inflate(R.layout.custom, null);

        mWebView = (WebView) customView.findViewById(R.id.webcontent);
		if (null != mWebView) {
            setupWebView();
		}
		
        CustomContentCallbacks callbacks = new CustomContentCallbacks() {
			@Override
			public void onShow(boolean fromResume) {
                LauncherLog.i(TAG, "callbacks: " + fromResume);
                isShown = true;
			}

			@Override
			public void onHide() {
                isShown = false;
			}

			@Override
			public void onScrollProgressChanged(float progress) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isScrollingAllowed() {
				// TODO Auto-generated method stub
				return true;
			}
        };
        
		addToCustomContentPage(customView, callbacks, "custom-view");
	}

    @Override
    protected void onResume() {
        super.onResume();
        Intent service = new Intent(this, ClockWidgetService.class);
        startService(service);
    }

    @Override
    public void onBackPressed() {
        if (null!= mWebView && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void setupWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);//设置使用够执行JS脚本
        mWebView.getSettings().setBuiltInZoomControls(true);//设置使支持缩放
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setWebViewClient(new WeWebViewClient());

        mWebView.loadUrl(HOME_PAGE);
    }

    private class WeWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            if (!isShown) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            if (url.equals(NEWS_BAIDU)) {
                view.loadUrl(url);
                return true;
            }

            if (url.startsWith(HOME_PAGE)) {
                view.loadUrl(url);
            } else {
                if (firstRun) {
                    view.loadUrl(url);
                    firstRun = false;
                } else {
                    start3rdActivity(view, url);
                }
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d(TAG, "onReceivedError: " + failingUrl);
        }
    }

    private void start3rdActivity(WebView view, String url) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setComponent(new ComponentName(DEFAULT_APP_PACKAGENAME, DEFAULT_APP_CLASS));
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            String message = APP_NAME + "未安装";
            final AlertDialog aboutDialog = new AlertDialog.Builder(WeLauncher.this).
                    setMessage(message).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri content_url = Uri.parse(APP_DLD_LINK);
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    }).create();
            aboutDialog.show();
            view.loadUrl(url);
        }
    }
}
