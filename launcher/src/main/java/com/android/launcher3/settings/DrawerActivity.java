package com.android.launcher3.settings;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.MenuItem;

import com.android.launcher3.AppsCustomizePagedView;
import com.webeye.launcher.R;

public class DrawerActivity extends SettingBaseActivity implements OnPreferenceClickListener {
	private static final String PREFERENCE_DRAWER_SCROLL_EFFECT = "drawer_scroll_effect_key";
	private static final String PREFERENCE_DRAWER_SORTING = "drawer_sorting_key";

	private ListPreference mDrawerSorting;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 从xml文件中添加Preference项
		addPreferencesFromResource(R.xml.launcher_drawer_settings_preferences);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
	}
	@Override
	public void onStart() {
		super.onStart();
		Preference drawerScrollEffect = (Preference) findPreference(PREFERENCE_DRAWER_SCROLL_EFFECT);
		if (drawerScrollEffect != null) {
			String currentState = SettingsProvider.getString(mLauncher, SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_TRANSITION_EFFECT, R.string.preferences_interface_drawer_scrolling_transition_effect);
			currentState = mapEffectToValue(currentState);
			drawerScrollEffect.setSummary(currentState);
			drawerScrollEffect.setOnPreferenceClickListener(this);
		}
		mDrawerSorting = (ListPreference) findPreference(PREFERENCE_DRAWER_SORTING);
		if (mDrawerSorting != null) {
			int index = SettingsProvider.getIntCustomDefault(mLauncher, SettingsProvider.SETTINGS_UI_DRAWER_SORT_MODE, 0);
			mDrawerSorting.setValueIndex(index);
			mDrawerSorting.setSummary(mDrawerSorting.getEntries()[index]);
			mDrawerSorting.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					setDrawerSort(newValue);
					return true;
				}
			});
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		final String key = preference.getKey();

		if (key.equals(PREFERENCE_DRAWER_SCROLL_EFFECT)) {
			Intent intent = new Intent(this, TransitionEffectsActivity.class);
			intent.putExtra(TransitionEffectsActivity.PAGE_OR_DRAWER_SCROLL_SELECT, true);
			startActivity(intent);
			return true;
		}
		return false;
	}

	private void setDrawerSort(Object newValue) {
		final String summary = newValue.toString();
		final int index = mDrawerSorting.findIndexOfValue(summary);
		mDrawerSorting.setSummary(mDrawerSorting.getEntries()[index]);
		mDrawerSorting.setValue(summary);
		mLauncher.getAppsCustomizeContent().setSortMode(AppsCustomizePagedView.SortMode.getModeForValue(index));

		SettingsProvider.putInt(mLauncher, SettingsProvider.SETTINGS_UI_DRAWER_SORT_MODE, index);
		return;
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
}
