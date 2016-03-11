package com.android.launcher3.settings;

import java.util.ArrayList;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.klauncher.launcher.R;

public class TransitionEffectsActivity extends Activity {
	public static final String PAGE_OR_DRAWER_SCROLL_SELECT = "pageOrDrawer";
	public static final String SELECTED_TRANSITION_EFFECT = "selectedTransitionEffect";
	public static final String TRANSITION_EFFECTS_FRAGMENT = "transitionEffectsFragment";
	// Lenovo-sw luyy1 add for removing rotate effect 2015-08-31
	public static final int TRANSITION_EFFECTS_ROTATE_UP = 3;
	public static final int TRANSITION_EFFECTS_ROTATE_DOWN = 4;
	ImageView mTransitionIcon;
	ListView mListView;
	View mCurrentSelection;

	String[] mTransitionStates;
	TypedArray mTransitionDrawables;
	String mCurrentState;
	int mCurrentPosition;
	boolean mIsDrawer;
	String mSettingsProviderValue;
	int mPreferenceValue;
	private Launcher mLauncher;

	OnClickListener mSettingsItemListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mCurrentPosition == (Integer) v.getTag()) {
				return;
			}
			mCurrentPosition = (Integer) v.getTag();
			mCurrentState = mTransitionStates[mCurrentPosition];

			setCleared(mCurrentSelection);
			setSelected(v);
			mCurrentSelection = v;
			mLauncher.setTransitionEffectBySetting(mIsDrawer, mCurrentState);
			new Thread(new Runnable() {
				public void run() {
					mTransitionIcon.post(new Runnable() {
						public void run() {
							setImageViewToEffect();
						}
					});
				}
			}).start();

			((TransitionsArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_transitions_effect);
		mListView = (ListView) findViewById(R.id.settings_transitions_list);

		Intent intent = getIntent();
		mIsDrawer = intent.getBooleanExtra(PAGE_OR_DRAWER_SCROLL_SELECT, false);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
		mSettingsProviderValue = mIsDrawer ? SettingsProvider.SETTINGS_UI_DRAWER_SCROLLING_TRANSITION_EFFECT : SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_TRANSITION_EFFECT;
		mPreferenceValue = mIsDrawer ? R.string.preferences_interface_drawer_scrolling_transition_effect : R.string.preferences_interface_homescreen_scrolling_transition_effect;

		mTransitionIcon = (ImageView) findViewById(R.id.settings_transition_image);
        /* Lenovo-sw luyy1 change start for removing rotate effect 2015-08-31*/
        //String[] titles = getResources().getStringArray(R.array.transition_effect_entries);
        String[] titles = removeRotateEffect(getResources().getStringArray(R.array.transition_effect_entries));
        /* Lenovo-sw luyy1 change end for removing rotate effect 2015-08-31*/
        mListView.setAdapter(new TransitionsArrayAdapter(this, R.layout.settings_pane_list_item, titles));

		mTransitionStates = getResources().getStringArray(R.array.transition_effect_values);
		mTransitionDrawables = getResources().obtainTypedArray(R.array.transition_effect_drawables);

		mCurrentState = SettingsProvider.getString(this, mSettingsProviderValue, mPreferenceValue);
		mCurrentPosition = mapEffectToPosition(mCurrentState);

		mListView.setSelection(mCurrentPosition);
		setImageViewToEffect();
		mLauncher = getLauncher();
	}

	private int mapEffectToPosition(String effect) {
		int length = mTransitionStates.length;
		for (int i = 0; i < length; i++) {
			if (effect.equals(mTransitionStates[i])) {
				return i;
			}
		}
		return -1;
	}

	private void setImageViewToEffect() {
		mTransitionIcon.setBackgroundResource(mTransitionDrawables.getResourceId(mCurrentPosition, R.drawable.transition_none));

		AnimationDrawable frameAnimation = (AnimationDrawable) mTransitionIcon.getBackground();
		frameAnimation.start();
	}

	@Override
	public void onStop() {
		super.onStop();

		// explicitly stop animation to ensure that we release references from
		// the
		// view root's run queue
		AnimationDrawable frameAnimation = (AnimationDrawable) mTransitionIcon.getBackground();
		if (frameAnimation != null) {
			frameAnimation.stop();
		}
	}

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return true;
	}

	private void setSelected(View v) {
		//v.setBackgroundColor(getResources().getColor(R.color.settings_list_item_select_bg_color));
		TextView t = (TextView) v.findViewById(R.id.item_name);
		t.setTextColor(getResources().getColor(R.color.settings_list_item_select_text_color));
	}

	private void setCleared(View v) {
		//v.setBackgroundColor(getResources().getColor(R.color.settings_list_item_select_text_color));
		TextView t = (TextView) v.findViewById(R.id.item_name);
		t.setTextColor(getResources().getColor(R.color.settings_list_item_select_bg_color));
	}

	private class TransitionsArrayAdapter extends ArrayAdapter<String> {
		Context mContext;
		String[] titles;

		public TransitionsArrayAdapter(Context context, int textViewResourceId, String[] objects) {
			super(context, textViewResourceId, objects);

			mContext = context;
			titles = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.settings_pane_list_item, parent, false);
			TextView textView = (TextView) convertView.findViewById(R.id.item_name);

			// RTL
			Configuration config = getResources().getConfiguration();
			if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
				textView.setGravity(Gravity.RIGHT);
			}

			textView.setText(titles[position]);
			// Set Selected State
			if (position == mCurrentPosition) {
				mCurrentSelection = convertView;
				setSelected(mCurrentSelection);
			}

			convertView.setOnClickListener(mSettingsItemListener);
			convertView.setTag(position);
			return convertView;
		}
	}

    /* Lenovo-sw luyy1 add start for removing rotate effect 2015-08-31*/
    public static String[] removeRotateEffect(String[] str) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < str.length; i++) {
            if (i != TRANSITION_EFFECTS_ROTATE_UP && i != TRANSITION_EFFECTS_ROTATE_DOWN) {
                list.add(str[i]);
            }
        }
        return list.toArray(new String[0]);
    }
    /* Lenovo-sw luyy1 add end for removing rotate effect 2015-08-31*/
}
