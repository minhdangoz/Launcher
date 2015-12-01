package com.lenovo.launcher.ext;

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.launcher3.Launcher;
import com.webeye.launcher.R;

public class WeLauncher extends Launcher {
	
	@Override
	protected boolean hasCustomContentToLeft() {
		return true;
	}

	@Override
	protected void populateCustomContentContainer() {
		View customView = getLayoutInflater().inflate(R.layout.custom, null);
		
		WebView webView = (WebView) customView.findViewById(R.id.webcontent);
		if (null != webView) {
			webView.getSettings().setJavaScriptEnabled(true);//设置使用够执行JS脚本  
	        webView.getSettings().setBuiltInZoomControls(true);//设置使支持缩放  
//	      webView.getSettings().setDefaultFontSize(5);  
	          
	        webView.loadUrl("http://www.baidu.com");  
	        webView.setWebViewClient(new WebViewClient(){  
	            @Override  
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
	                // TODO Auto-generated method stub  
	                view.loadUrl(url);// 使用当前WebView处理跳转  
	                return true;//true表示此事件在此处被处理，不需要再广播  
	            }  
	            @Override   //转向错误时的处理  
	            public void onReceivedError(WebView view, int errorCode,  
	                    String description, String failingUrl) {  
	                // TODO Auto-generated method stub  
	            }  
	        });  
		}
		
        CustomContentCallbacks callbacks = new CustomContentCallbacks() {

			@Override
			public void onShow(boolean fromResume) {

				
				
			}

			@Override
			public void onHide() {
				// TODO Auto-generated method stub
				
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
}
