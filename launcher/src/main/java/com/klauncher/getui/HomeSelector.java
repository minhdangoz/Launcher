package com.klauncher.getui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.klauncher.launcher.R;


/**
 * Created by wangqinghao on 2016/6/13.
 */
public class HomeSelector extends Activity {
    private Intent createHomeIntent() {
        Intent localIntent = new Intent("android.intent.action.MAIN", null);
        localIntent.addCategory("android.intent.category.HOME");
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return localIntent;
    }

    public void onCreate(Bundle paramBundle) {
        Log.i("HOMECONTROL", " ** started .. ()() ");
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        PackageManager localPackageManager = getPackageManager();
        ComponentName localComponentName = new ComponentName(getPackageName(), "com.klauncher.getui.FakeHomeApp");
        localPackageManager.setComponentEnabledSetting(localComponentName, 1, 1);
        Intent localIntent = createHomeIntent();
        localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(localIntent);
        localPackageManager.setComponentEnabledSetting(localComponentName, 2, 1);
        createHomeIntent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
    }
}

