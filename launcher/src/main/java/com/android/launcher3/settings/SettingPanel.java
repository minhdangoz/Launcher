package com.android.launcher3.settings;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.ModeSwitchHelper;
import com.android.launcher3.ModeSwitchHelper.Mode;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SettingPanel extends LinearLayout {

	public SettingPanel(Context context) {
		this(context, null);
	}

	public SettingPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// Do save preference work after settings panel hidden.
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {

		Mode oldMode = SettingsValue.isDisableAllApps(getContext()) ? Mode.VIBEUI
				: Mode.ANDROID;
		Mode newMode = LauncherAppState.getInstance().getCurrentLayoutMode();
		if (visibility != View.VISIBLE && (oldMode != newMode)) {
			boolean value = newMode == Mode.VIBEUI ? true : false;
			SettingsValue.setDisableAllApps(getContext(), value);
			ModeSwitchHelper.loadDefaultDesktop(getContext());
		}
	}
}
