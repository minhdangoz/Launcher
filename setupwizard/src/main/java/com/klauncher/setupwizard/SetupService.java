package com.klauncher.setupwizard;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import static com.klauncher.setupwizard.SetupMain.SP_SETUP;
import static com.klauncher.setupwizard.SetupMain.SP_SETUP_START;

/**
 * Created by hw on 17-3-23.
 */

public class SetupService extends Service {


    private SharedPreferences sharedPreferences;
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getApplicationContext().getSharedPreferences(SP_SETUP, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(SP_SETUP_START, true).commit();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
