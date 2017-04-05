package com.klauncher.setupwizard;

import android.app.Activity;
import android.os.Bundle;

import static com.klauncher.setupwizard.SetupMain.activityList;

/**
 * Created by hw on 17-4-5.
 */

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addActivityStack(this);
    }

    protected void addActivityStack(Activity activity){
        activityList.add(activity);
    }

    protected void finishAll(){
        for (Activity a : activityList) {
            a.finish();
        }
        activityList.clear();
        activityList = null;
    }
}
