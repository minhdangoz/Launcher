package com.klauncher.ext;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hw on 16-8-12.
 */
public class SearchWidgetView extends FrameLayout {

    private LinearLayout mRootView;
    private Context mContext;

    public SearchWidgetView(Context context) {
        super(context);
        mRootView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.search_widget, this, false);
        addView(mRootView);
        mContext = context;
        init();
    }

    public SearchWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRootView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.search_widget, this, false);
        addView(mRootView);
        mContext = context;
        init();
    }

    public SearchWidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRootView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.search_widget, this, false);
        addView(mRootView);
        mContext = context;
        init();
    }

    private void init(){
       /* mRootView.setOnClickListener(new LinearLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if ( isAppInstalled(mContext, "com.sogou.activity.src")) {
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.setClassName("com.sogou.activity.src","com.sogou.activity.src.SplashActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PingManager.getInstance().reportUserAction4App(
                            PingManager.KLAUNCHER_WIDGET_SOUGOU_SEARCH, mContext.getPackageName());
                }else if(!isAppInstalled(mContext, "com.sogou.activity.src")&& isAppInstalled(mContext,"com.baidu.searchbox")){
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.setClassName("com.baidu.searchbox","com.baidu.searchbox.SplashActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PingManager.getInstance().reportUserAction4App(
                            PingManager.KLAUNCHER_WIDGET_BAIDU_SEARCH, mContext.getPackageName());
                }else {
                    intent = new Intent(mContext, SearchActivity.class);
                }
                mContext.startActivity(intent);
            }
        });*/
    }

    public boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }
}
