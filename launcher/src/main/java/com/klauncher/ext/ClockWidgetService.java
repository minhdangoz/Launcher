package com.klauncher.ext;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Calendar;

/**
 * Created by yanni on 16/3/11.
 */
public class ClockWidgetService extends Service {
    public static final String ACTION_CLOCK_UPDATE = "com.klauncher.action.ACTION_CLOCK_UPDATE";

    private int mTimeMinute = -1;

    private Thread mClockUpdateThread = new Thread() {
        @Override
        public void run() {
            super.run();
            while (!interrupted()) {
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                if (mTimeMinute != minute) {
                    sendBroadcast(new Intent(ACTION_CLOCK_UPDATE));
                }
                mTimeMinute = minute;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClockUpdateThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClockUpdateThread.interrupt();
    }
}
