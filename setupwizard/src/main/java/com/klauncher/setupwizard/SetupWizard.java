package com.klauncher.setupwizard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import static com.klauncher.setupwizard.SetupMain.SP_SETUP;
import static com.klauncher.setupwizard.SetupMain.SP_SETUP_START;

/**
 * Created by hw on 17-4-5.
 */

public class SetupWizard extends Activity {
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getApplicationContext().getSharedPreferences(SP_SETUP, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(SP_SETUP_START, true).commit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
