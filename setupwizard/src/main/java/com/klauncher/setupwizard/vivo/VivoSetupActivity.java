package com.klauncher.setupwizard.vivo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import static com.klauncher.setupwizard.SetupMain.SP_SETUP;
import static com.klauncher.setupwizard.SetupMain.SP_SETUP_OVER;
import static com.klauncher.setupwizard.SetupMain.TAG;

/**
 * Created by hw on 17-3-21.
 */

public class VivoSetupActivity extends Activity {
    private ComponentName mDefault;
    private PackageManager mPm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(SP_SETUP, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(SP_SETUP_OVER, true).commit();

        try {
            Intent setup = new Intent();
            setup.setClassName("com.vivo.setupwizard","com.vivo.setupwizard.MainActivity");
            setup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setup);
        }catch (Exception e){
            Log.e(TAG, "no vivo default setupwizard");
        }

        mDefault = getComponentName();
        Log.e(TAG, "mDefault : " + mDefault.toString());
        mPm = getApplicationContext().getPackageManager();
        disableComponent(mDefault);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void disableComponent(ComponentName componentName) {
        mPm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
