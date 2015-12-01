package com.android.launcher3.settings;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.Utilities;
import com.android.launcher3.backup.LbkLoader;
import com.android.launcher3.backup.LbkPackager;
import com.android.launcher3.backup.LbkPackager.BackupListener;
import com.android.launcher3.backup.LbkUtil;
import com.webeye.launcher.ext.LauncherLog;
import com.webeye.launcher.R;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

public class BackupRestoreActivity extends SettingBaseActivity implements OnPreferenceClickListener {
	private static final String PREFERENCE_BACKUP = "local_backup_key";
	private static final String PREFERENCE_RESTORE = "local_restore_key";
	public static final String LBK_SUFFIX = ".lbk";
	public static final String LOCAL_LOCATION = ".IdeaDesktop/.backup/.localbackup";
	static final String TAG = "BackupRestoreActivity";

	private Preference mLocalBackup;
	private Preference mLocalRestore;
	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 从xml文件中添加Preference项
		addPreferencesFromResource(R.xml.launcher_backup_recovery_settings_preferences);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
		mLocalBackup = (Preference) findPreference(PREFERENCE_BACKUP);
		if (mLocalBackup != null) {
			mLocalBackup.setOnPreferenceClickListener(this);
		}
		mLocalRestore = (Preference) findPreference(PREFERENCE_RESTORE);
		if (mLocalRestore != null) {
			mLocalRestore.setOnPreferenceClickListener(this);
		}
		setBackupAndRestoreSummary();
		mContext = this;
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

		if (key.equals(PREFERENCE_BACKUP)) {
			if(LauncherApplication.mDebug){
				if(mLauncher != null) mLauncher.dumpState();
				printLauncherTrace();
			}
			onClickBackUp();
			return true;
		} else if (key.equals(PREFERENCE_RESTORE)) {
			onClickRestore();
			return true;
		}
		return false;
	}

	private Dialog mBootProgressDlg = null;

	@TargetApi(19)
	public void setupTransparentSystemBarsForLmp(Window window) {
		// TODO(sansid): use the APIs directly when compiling against L sdk.
		// Currently we use reflection to access the flags and the API to set
		// the transparency
		// on the System bars.
		if (Utilities.isLmpOrAbove()) {
			try {
				window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				Field drawsSysBackgroundsField = WindowManager.LayoutParams.class.getField("FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS");
				window.addFlags(drawsSysBackgroundsField.getInt(null));

				Method setStatusBarColorMethod = Window.class.getDeclaredMethod("setStatusBarColor", int.class);
				Method setNavigationBarColorMethod = Window.class.getDeclaredMethod("setNavigationBarColor", int.class);
				setStatusBarColorMethod.invoke(window, Color.TRANSPARENT);
				setNavigationBarColorMethod.invoke(window, Color.TRANSPARENT);
			} catch (NoSuchFieldException e) {
				Log.w(TAG, "NoSuchFieldException while setting up transparent bars");
			} catch (NoSuchMethodException ex) {
				Log.w(TAG, "NoSuchMethodException while setting up transparent bars");
			} catch (IllegalAccessException e) {
				Log.w(TAG, "IllegalAccessException while setting up transparent bars");
			} catch (IllegalArgumentException e) {
				Log.w(TAG, "IllegalArgumentException while setting up transparent bars");
			} catch (InvocationTargetException e) {
				Log.w(TAG, "InvocationTargetException while setting up transparent bars");
			} finally {
			}
		}
	}

	public void showBootProgressDialog(final String msgText, final int theme) {
		if (mBootProgressDlg != null && mBootProgressDlg.isShowing()) {
			return;
		}

		mBootProgressDlg = new Dialog(mContext, theme);
		mBootProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mBootProgressDlg.setContentView(R.layout.boot_custom_progressdialog);
		final View view = mBootProgressDlg.findViewById(R.id.dialog);
		final TextView msg = (TextView) view.findViewById(R.id.progress_msg);
		msg.setText(msgText);
		view.setBackgroundColor(Color.TRANSPARENT);
		mBootProgressDlg.setContentView(view);
		mBootProgressDlg.setCancelable(false);

		Window window = mBootProgressDlg.getWindow();
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		window.setGravity(Gravity.CENTER);
		setupTransparentSystemBarsForLmp(window);
		if (mBootProgressDlg.isShowing()) {
			return;
		}
		LauncherLog.d(TAG, "showBootProgressDialog");
		mBootProgressDlg.show();
	}

	public void dismissBootProgressDialog() {
		LauncherLog.d(TAG, "dismissBootProgressDialog");
		try {
			if (mBootProgressDlg != null && mBootProgressDlg.isShowing()) {
				mBootProgressDlg.dismiss();
			}
			mBootProgressDlg = null;
		} catch (Exception e) {
			mBootProgressDlg = null;
		}
	}

    private void printLauncherTrace(){
        if(LauncherApplication.mLogArray != null){
            for(int i = 0; i < LauncherApplication.mLogArray.length; i++){
            	if(LauncherApplication.mLogArray[i] != null)
            	    Log.e("LauncherTrace",LauncherApplication.mLogArray[i]);
            }
        }
    }
	private void onClickBackUp() {
		LbkUtil.backupStartDialog(this, new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				LbkPackager.startBackup(mContext, new BackupListener() {
					@Override
					public void onBackupStart() {
						showBootProgressDialog(mContext.getResources().getString(R.string.local_backup_process_msg),
								R.style.Theme_Launcher_ProgressDialog);
					}
					@Override
					public void onBackupEnd(boolean success) {
						// TODO Auto-generated method stub
						Log.i("xixia", "onBackupEnd");
						dismissBootProgressDialog();
						setBackupAndRestoreSummary();
						LbkUtil.backupEndDialog(mContext, success, null);
					}
				});
			}
		}, null);
		return;
	}

	private void setBackupAndRestoreSummary() {
		File lbkFile = LbkUtil.getLbkFileFromXLauncherBackupDirectory();
		String summary = null;
		if (lbkFile != null) {
			String backupFilename = lbkFile.getAbsolutePath();
			if (backupFilename.indexOf(LbkUtil.ANDROID_BACKUP_LBK) != 0 || backupFilename.indexOf(LbkUtil.VIBEUI_BACKUP_LBK) != 0) {
				long timeStamp = lbkFile.lastModified();
				if (timeStamp != 0) {
					summary = getString((R.string.lastest_backup_tips), getTimeStamp(timeStamp));
					if (summary != null) {
						mLocalBackup.setSummary(summary);
						String location = getString(R.string.local_location);
						mLocalRestore.setSummary(location + " " + LOCAL_LOCATION);
					}
				}
			}
		}
		if (summary == null) {
			mLocalBackup.setSummary(R.string.no_profile_num);
			mLocalRestore.setSummary("");
		}
	}

	private String getTimeStamp(long tmpTime) {
		long currentTime = System.currentTimeMillis();
		String processDay = null;
		Date date = new Date(tmpTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		long year = cal.get(Calendar.YEAR);
		long month = cal.get(Calendar.MONTH);
		long day = cal.get(Calendar.DAY_OF_MONTH);

		Date currentDate = new Date(currentTime);
		Calendar currentCal = Calendar.getInstance();
		currentCal.setTime(currentDate);
		long currentYear = currentCal.get(Calendar.YEAR);
		long currentMonth = currentCal.get(Calendar.MONTH);
		long currentDay = currentCal.get(Calendar.DAY_OF_MONTH);

		if (year == currentYear && month == currentMonth && day == currentDay) {
			processDay = getString(R.string.backup_preference_today);
		} else if (year == currentYear && month == currentMonth && day == currentDay - 1) {
			processDay = getString(R.string.backup_preference_yesterday);
		} else if (year == currentYear && month == currentMonth && day == currentDay - 2) {
			processDay = getString(R.string.backup_preference_before_yesterday);
		}

		String result = DateFormat.format("yyyy/MM/dd kk:mm", tmpTime).toString();
		String hourAndMinute = DateFormat.format("kk:mm", tmpTime).toString();
		if (hourAndMinute != null && hourAndMinute.length() > 0 && processDay != null && processDay.length() > 0) {
			result = processDay + " " + hourAndMinute;
		}
		return result;
	}

	private void onClickRestore() {
		if (LbkUtil.isXLauncherBackupLbkFileExist()) {
			LbkUtil.restoreStartDialog(this, new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					if(LbkLoader.recoveryFromLbk()) {
						Intent intent = new Intent(INENT_CLOSE_ALL_SETTINGS_ACTIVITY);
						sendBroadcast(intent);
						if (mLauncher != null && mLauncher.getWorkspace().isInOverviewMode()) {
							mLauncher.getWorkspace().exitOverviewMode(true);
							mLauncher.onWorkspaceShown(true, false); // Lenovo-SW zhaoxin5 20150910 KOLEOSROW-2314
						}
                    }
				}

			}, null);
		} else {
			Toast.makeText(this, getString(R.string.no_profile_num_restore_toast), Toast.LENGTH_SHORT).show();
		}
		return;
	}
}
