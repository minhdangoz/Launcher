package com.android.launcher3.settings;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.view.MenuItem;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.ModeSwitchHelper;
import com.android.launcher3.ModeSwitchHelper.Mode;
import com.android.launcher3.reaper.Reaper;
import com.webeye.launcher.R;

public class LauncherSettingActivity extends SettingBaseActivity implements OnPreferenceClickListener {
	// 定义相关变量
	private static final String PREFERENCE_DESKTOP_STYLE = "desktop_style_key";
	private static final String PREFERENCE_INFINITE_SCROLLING = "pref_workspace_loop";
	private static final String PREFERENCE_HOMESCREEN = "home_screen_key";
	private static final String PREFERENCE_DRAWER = "drawer_settings_key";
	private static final String PREFERENCE_BACKUP_RESTORE = "backup_restore_key";
	private static final String PREFERENCE_EXTEND_SETTINGS = "extend_settings_key";

	private ListPreference mDesktopStyle;

	private int mCount = 0;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 从xml文件中添加Preference项
		addPreferencesFromResource(R.xml.launcher_settings_preferences);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadSettings();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		// 判断是哪个Preference被点击了
		final String key = preference.getKey();

		if (key.equals(PREFERENCE_HOMESCREEN)) {
			Intent intent = new Intent(this, HomescreenActivity.class);
			startActivity(intent);
		} else if (key.equals(PREFERENCE_DRAWER)) {
			Intent intent = new Intent(this, DrawerActivity.class);
			startActivity(intent);
		} else if (key.equals(PREFERENCE_BACKUP_RESTORE)) {
			Intent intent = new Intent(this, BackupRestoreActivity.class);
			startActivity(intent);
		} else {
			return false;
		}
		return true;
	}

	private void setDesktopStyleChanged(Object newValue) {
		final String summary = newValue.toString();
		final int index = mDesktopStyle.findIndexOfValue(summary);
		mDesktopStyle.setSummary(mDesktopStyle.getEntries()[index]);
		mDesktopStyle.setValue(summary);
		Mode mode = LauncherAppState.getInstance().getCurrentLayoutMode();
		/** Lenovo-SW zhaoxin5 20150813 fix bug, when change Launcher style, will miss some pages START */
		if (mLauncher != null && mLauncher.getWorkspace().isInOverviewMode()) {
			mLauncher.getWorkspace().exitOverviewMode(false);
		}
		/** Lenovo-SW zhaoxin5 20150813 fix bug, when change Launcher style, will miss some pages END */
		if (index == 0 && mode != Mode.VIBEUI) {
			LauncherAppState.getInstance().setCurrentLayoutMode(Mode.VIBEUI);
			SettingsValue.setDisableAllApps(mLauncher, true);
			ModeSwitchHelper.loadDefaultDesktop(mLauncher);
			/** Lenovo-SW zhaoxin5 20151118 add for Reaper support START */
            Reaper.processReaper( this, 
            	   Reaper.REAPER_EVENT_CATEGORY_OLAUNCHER_STYLE_SWITCH, 
				   Reaper.REAPER_EVENT_ACTION_OLAUNCHER_STYLE_SWITCH_VIBEUI,
				   Reaper.REAPER_NO_LABEL_VALUE, 
				   Reaper.REAPER_NO_INT_VALUE );
			/** Lenovo-SW zhaoxin5 20151118 add for Reaper support END */
		} else if (index == 1 && mode != Mode.ANDROID) {
			LauncherAppState.getInstance().setCurrentLayoutMode(Mode.ANDROID);
			SettingsValue.setDisableAllApps(mLauncher, false);
			ModeSwitchHelper.loadDefaultDesktop(mLauncher);
			/** Lenovo-SW zhaoxin5 20151118 add for Reaper support START */
            Reaper.processReaper( this, 
            	   Reaper.REAPER_EVENT_CATEGORY_OLAUNCHER_STYLE_SWITCH, 
				   Reaper.REAPER_EVENT_ACTION_OLAUNCHER_STYLE_SWITCH_ANDROID,
				   Reaper.REAPER_NO_LABEL_VALUE, 
				   Reaper.REAPER_NO_INT_VALUE );
			/** Lenovo-SW zhaoxin5 20151118 add for Reaper support END */
		}
		finish();
		return;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mLauncher != null && mLauncher.getWorkspace().isInOverviewMode()) {
				mLauncher.getWorkspace().exitOverviewMode(true);
				mLauncher.onWorkspaceShown(true, false); // Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
			}
			finish();
			return true;
		}
		return true;
	}

	private void setWorkspaceLoop() {
		boolean current = SettingsValue.isWorkspaceLoop(this);
		SettingsValue.setWorkspaceLoop(this, !current);
		/** Lenovo-SW zhaoxin5 20151118 add for Reaper support START */
        Reaper.processReaper( this, 
        	   Reaper.REAPER_EVENT_CATEGORY_OLAUNCHER_DESKTOP_SETTINGS, 
			   !current ? Reaper.REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_DESKTOP_LOOP_OPEN 
					    : Reaper.REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_DESKTOP_LOOP_NOT_OPEN,
			   Reaper.REAPER_NO_LABEL_VALUE, 
			   Reaper.REAPER_NO_INT_VALUE );
		/** Lenovo-SW zhaoxin5 20151118 add for Reaper support END */
		/*Lenovo-sw zhangyj19 add 2015/09/09 add search app definition begin */
		mCount++;
		if(mCount == 5){
			mCount = 0;
			boolean currentSearchApp = SettingsValue.isSearchApp(this);
			SettingsValue.setSearchApp(this, !currentSearchApp);
		}
		/*Lenovo-sw zhangyj19 add 2015/09/09 add search app definition end */
        return;
	}
	@SuppressWarnings("deprecation")
	private void loadSettings() {
		Mode mode = LauncherAppState.getInstance().getCurrentLayoutMode();
		mDesktopStyle = (ListPreference) findPreference(PREFERENCE_DESKTOP_STYLE);
		if (mDesktopStyle != null) {
			int index = 0;
			if (mode == Mode.VIBEUI) {
				index = 0;
			} else {
				index = 1;
			}
			mDesktopStyle.setValueIndex(index);
			mDesktopStyle.setSummary(mDesktopStyle.getEntries()[index]);
			mDesktopStyle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					setDesktopStyleChanged(newValue);
					return true;
				}
			});
		}
		SwitchPreference workspaceLoop = (SwitchPreference) findPreference(PREFERENCE_INFINITE_SCROLLING);
		boolean current = SettingsValue.isWorkspaceLoop(this);
		workspaceLoop.setChecked(current);
        workspaceLoop.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	setWorkspaceLoop();
                return true;

            }
        });
		Preference homeScreen = (Preference) findPreference(PREFERENCE_HOMESCREEN);
		if (homeScreen != null) {
			homeScreen.setOnPreferenceClickListener(this);
		}
		PreferenceCategory extendSettings = (PreferenceCategory) findPreference(PREFERENCE_EXTEND_SETTINGS);
		Preference drawerPrefs = (Preference) findPreference(PREFERENCE_DRAWER);
		if (mode == Mode.VIBEUI) {
			if (drawerPrefs != null) {
				extendSettings.removePreference(drawerPrefs);
			}
		} else {
			drawerPrefs.setOnPreferenceClickListener(this);
		}
		Preference backupRestore = (Preference) findPreference(PREFERENCE_BACKUP_RESTORE);
		if (backupRestore != null) {
			backupRestore.setOnPreferenceClickListener(this);
		}
	}
}
