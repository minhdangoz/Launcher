package com.android.launcher3.settings;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.SwitchPreference;
import android.view.MenuItem;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.ModeSwitchHelper.Mode;
import com.android.launcher3.reaper.Reaper;
import com.webeye.launcher.R;

public class HomescreenActivity extends SettingBaseActivity implements OnPreferenceClickListener {
	private static final String PREFERENCE_AUTO_RECORDER = "pref_auto_reorder";
	private static final String PREFERENCE_SCROLL_EFFECT = "scroll_effect_key";
	private static final String PREFERENCE_SCROLLING_WALLPAPER = "pref_wallpaper_slide";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 从xml文件中添加Preference项
		addPreferencesFromResource(R.xml.launcher_homescreen_settings_preferences);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
	}
	@Override
	public void onStart() {
		super.onStart();
		Mode mode = LauncherAppState.getInstance().getCurrentLayoutMode();
		SwitchPreference autoReorder = (SwitchPreference) findPreference(PREFERENCE_AUTO_RECORDER);
		if (mode == Mode.VIBEUI) {
			if (autoReorder != null) {
				boolean current = SettingsValue.isAutoReorderEnabled(this);
				autoReorder.setChecked(current);
				autoReorder.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						setAutoReorder();
						return true;
					}
				});
			}
		} else {
			if (autoReorder != null) {
				 getPreferenceScreen().removePreference(autoReorder);
			}
		}
		Preference scrollEffect = (Preference) findPreference(PREFERENCE_SCROLL_EFFECT);
		if (scrollEffect != null) {
			String currentState = SettingsProvider.getString(this, SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_TRANSITION_EFFECT, R.string.preferences_interface_homescreen_scrolling_transition_effect);
			currentState = mapEffectToValue(currentState);
			scrollEffect.setSummary(currentState);
			scrollEffect.setOnPreferenceClickListener(this);
		}
		SwitchPreference scrollingWallpaper = (SwitchPreference) findPreference(PREFERENCE_SCROLLING_WALLPAPER);
		if (scrollingWallpaper != null) {
			boolean current = SettingsValue.isScrollingWallpaper(this);
			scrollingWallpaper.setChecked(current);
			scrollingWallpaper.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					boolean current = SettingsValue.isScrollingWallpaper(mLauncher);
					SettingsValue.setScrollingWallpaperState(mLauncher, !current);
					mLauncher.setUpdateDynamicGrid();
					/** Lenovo-SW zhaoxin5 20151118 add for Reaper support START */
			        Reaper.processReaper( HomescreenActivity.this, 
			        	   Reaper.REAPER_EVENT_CATEGORY_OLAUNCHER_DESKTOP_SETTINGS, 
						   !current ? Reaper.REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_WALLPAPER_SCROLL_OPEN 
								    : Reaper.REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_WALLPAPER_SCROLL_NOT_OPEN,
						   Reaper.REAPER_NO_LABEL_VALUE, 
						   Reaper.REAPER_NO_INT_VALUE );
					/** Lenovo-SW zhaoxin5 20151118 add for Reaper support END */
					return true;
				}
			});
		}
	}
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		final String key = preference.getKey();

		if (key.equals(PREFERENCE_SCROLL_EFFECT)) {
			Intent intent = new Intent(this, TransitionEffectsActivity.class);
			intent.putExtra(TransitionEffectsActivity.PAGE_OR_DRAWER_SCROLL_SELECT, false);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return true;
	}

	private String mapEffectToValue(String effect) {
        /* Lenovo-sw luyy1 add start for removing rotate effect 2015-08-31*/
        //final String[] titles = getResources().getStringArray(R.array.transition_effect_entries);
        final String[] titles = TransitionEffectsActivity.removeRotateEffect(
            getResources().getStringArray(R.array.transition_effect_entries));
        /* Lenovo-sw luyy1 add end for removing rotate effect 2015-08-31*/
		final String[] values = getResources().getStringArray(R.array.transition_effect_values);

		int length = values.length;
		for (int i = 0; i < length; i++) {
			if (effect.equals(values[i])) {
				return titles[i];
			}
		}
		return "";
	}

	private void setAutoReorder() {
		boolean current = SettingsValue.isAutoReorderEnabled(this);
		SettingsValue.setAutoReorderState(this, !current);
		/** Lenovo-SW zhaoxin5 20151118 add for Reaper support START */
        Reaper.processReaper( HomescreenActivity.this, 
        	   Reaper.REAPER_EVENT_CATEGORY_OLAUNCHER_DESKTOP_SETTINGS, 
			   !current ? Reaper.REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_AUTO_SORT_OPEN 
					    : Reaper.REAPER_EVENT_ACTION_OLAUNCHER_DESKTOP_SETTINGS_AUTO_SORT_NOT_OPEN,
			   Reaper.REAPER_NO_LABEL_VALUE, 
			   Reaper.REAPER_NO_INT_VALUE );
		/** Lenovo-SW zhaoxin5 20151118 add for Reaper support END */
		if (SettingsValue.isAutoReorderEnabled(this)) {
			// need call Launcher to start a auto-reorder
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					mLauncher.autoReorder("SettingsPinnedHeaderAdapter");
					return null;
				}
			}.execute();
		}
		return;
	}
}
