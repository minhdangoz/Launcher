package com.klauncher.upgrade;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.launcher3.Launcher;
import com.delong.assistance.AssistanceServiceApi;
import com.delong.assistance.bean.UpgradeInfo;
import com.delong.assistance.config.HttpAction;
import com.delong.assistance.config.RxFactory;
import com.klauncher.launcher.BuildConfig;
import com.klauncher.launcher.R;
import com.klauncher.launcher.databinding.UpgradeTipsBinding;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/12.
 */

public class UpgradeHelper {

    private static UpgradeHelper mInstance;

    private final String ACTION_UPGRADE = "com.klauncher.action_ACTION_UPGRADE";

    private final int REQUEST_CODE = 1024;

    private AlarmManager mAlarmManager;

    private Launcher mLauncher;

    private BaseDownloadTask mDownloadTask;

    private FileDownloadListener mDownLoadListener = new FileDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void completed(BaseDownloadTask task) {
            showUpgradeInstallDialog(task);
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {

        }

        @Override
        protected void warn(BaseDownloadTask task) {

        }
    };


    private UpgradeHelper(Launcher launcher) {
        mLauncher = launcher;
    }

    private BroadcastReceiver mUpgradeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);

                //TODO param map is empty, we shell set param to this map if web service need
                final Map<String, String> commonParam = new HashMap<>();
                AssistanceServiceApi.service().checkUpgrade(commonParam, pi.versionCode, BuildConfig.FLAVOR)
                        .compose(RxFactory.<UpgradeInfo>callerSchedulers())
                        .subscribe(new HttpAction<UpgradeInfo>() {

                            @Override
                            public void onHttpError(retrofit2.Response response) {
                            }

                            @Override
                            public void onHttpSuccess(UpgradeInfo ui) {
                                if (ui == null || ui.upgrade_flag == 1) {
                                    return;
                                }

                                if (mDownloadTask != null && mDownloadTask.isRunning()) {
                                    if (!TextUtils.equals(mDownloadTask.getUrl(), ui.apk_url)) {
                                        mDownloadTask.cancel();
                                    } else {
                                        return;
                                    }
                                }

                                mDownloadTask = FileDownloader.getImpl().create(ui.apk_url)
                                        .setPath(Environment.getExternalStorageDirectory() + "/klauncher/download/upgrade_" + ui.new_ver_code + ".apk")
                                        .setListener(mDownLoadListener)
                                        .setWifiRequired(true);

                                mDownloadTask.start();

                            }
                        });

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    };

    public static UpgradeHelper getInstance(Launcher launcher) {

        synchronized (UpgradeHelper.class) {
            if (mInstance == null) {
                mInstance = new UpgradeHelper(launcher);
            }
        }
        return mInstance;
    }

    public void test() {
//        showUpgradeInstallDialog(null);
    }

    public void startUpgradeMission() {

        Intent intent = new Intent(ACTION_UPGRADE);
        IntentFilter filter = new IntentFilter(ACTION_UPGRADE);
        mLauncher.registerReceiver(mUpgradeReceiver, filter);
        PendingIntent pi = PendingIntent.getBroadcast(mLauncher, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager = (AlarmManager) mLauncher.getSystemService(Service.ALARM_SERVICE);
        mAlarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 30000, 3 * 60 * 60 * 1000, pi);
    }

    private void showUpgradeInstallDialog(BaseDownloadTask task) {

//        int cw = Resources.getSystem().getDisplayMetrics().widthPixels;

        UpgradeTipsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mLauncher), R.layout.upgrade_tips, null, false);

//        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(cw, binding.getRoot().getHeight());
//
//        binding.getRoot().setLayoutParams(lp);
//        binding.getRoot().requestLayout();

        binding.btnInstall.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.btnInstall.getPaint().setAntiAlias(true);

        binding.btnCancel.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.btnCancel.getPaint().setAntiAlias(true);

        FloatToast toast = FloatToast.getInstatce(mLauncher, binding.getRoot())
                .setDuration(20000)
                .setAnimations(R.style.ToastAnimation)
                .setShowLocation(0, 0)
                .makeMatchParent();

        toast.show();
    }

    private void cancelUpgradeMission() {

    }

}
