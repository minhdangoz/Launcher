/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.lenovosearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.webeye.launcher.R;

/**
 * The main activity for Quick Search Box. Shows the search UI.
 * 
 */
public class LenovoAppSearchActivity extends Activity {

	private SearchAppView mSearchAppView = null;
	private Launcher mLauncher;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// shortcut intent
		if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
			// shortcut intent
			Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
			shortcutIntent.setClassName(this, this.getClass().getName());

			// set name
			Intent intent = new Intent();
			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.search_widget));

			// set icon
			Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.search_app_icon);
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

			// // Now, return the result to the launcher
			setResult(RESULT_OK, intent);
			finish();
			return;
		}
		setContentView(R.layout.lenovo_search);
		mLauncher = getLauncher();
		mSearchAppView = (SearchAppView) findViewById(R.id.search_app_view);
		mSearchAppView.setLenovoAppSearchActivity(this, mLauncher);
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

}
