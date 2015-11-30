package com.android.launcher3.settings;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.backup.LbkLoader;
import com.android.launcher3.backup.LbkUtil;
import com.webeye.launcher.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingBaseActivity extends PreferenceActivity {
	protected Launcher mLauncher;
	protected String INENT_CLOSE_ALL_SETTINGS_ACTIVITY = "com.lenovo.olauncher.closesettings";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLauncher = getLauncher();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(INENT_CLOSE_ALL_SETTINGS_ACTIVITY);
		registerReceiver(mFinishSettingsReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mFinishSettingsReceiver);
	}



	protected BroadcastReceiver mFinishSettingsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}  
 
    };  

	private Launcher getLauncher() {
		try {
			LauncherAppState app = LauncherAppState.getInstance();
			LauncherModel launcherModel = app.getModel();
			Launcher launcher = null;
			if (launcherModel != null) {
				launcher = launcherModel.getLauncherInstance();
			}
			return launcher;

		} catch (Exception e) {
			return null;
		}
	}

}
