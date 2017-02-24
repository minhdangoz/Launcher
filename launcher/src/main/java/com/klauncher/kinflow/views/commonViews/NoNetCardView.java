package com.klauncher.kinflow.views.commonViews;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.launcher.R;

/**
 * Created by xixionghui on 16/10/11.
 */
public class NoNetCardView extends CardView implements View.OnClickListener{

    private RelativeLayout mRootView;
    private Context mContext;

    public Context getMContext() {
        if (null == mContext)
            mContext = KLauncherApplication.mKLauncherApplication;
        return mContext;
    }

    public NoNetCardView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public NoNetCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    public NoNetCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        mRootView = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.settings_wifi,this,false);
        addView(mRootView);
        mRootView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        try {
            Intent intentWifi=new Intent();
            if (android.os.Build.VERSION.SDK_INT > 10) {
                intentWifi.setAction(android.provider.Settings.ACTION_SETTINGS);
            } else {
                intentWifi.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
            }
            getMContext().startActivity(intentWifi);
        } catch (Exception e) {
            Toast.makeText(getMContext(), "打开网络设置失败,请在设置中手动打开", Toast.LENGTH_SHORT).show();
            KinflowLog.w("打开网络设置失败,请在设置中手动打开");
        }

    }
}
