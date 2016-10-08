package com.klauncher.biddingos.commons.task;

import android.content.Context;

import com.klauncher.biddingos.commons.Setting;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 后台任务管理器
 */
public class TaskManager {

    public static TaskManager sInstance;

    /**
     * 获取唯一实例
     * @return
     */
    public static synchronized TaskManager getInstance() {
        if(sInstance == null ) {
            sInstance = new TaskManager(Setting.context.getApplicationContext());
        }
        return sInstance;
    }

    private Context mContext;
    public ScheduledExecutorService scheduledThreadPool = null;

    private TaskManager(Context context) {
        mContext = context;
        if(scheduledThreadPool==null)
            scheduledThreadPool = Executors.newScheduledThreadPool(2);
    }

    /**
     * 提交实时任务
     * <br><br>
     *     实时任务即立即新建线程执行任务，并不做队列处理
     * @param task
     */
    public void submitRealTimeTask(Task<?, ?> task) {
        new Thread(task).start();
    }

    /**
     * 提交延时任务
     * <br><br>
     *     延时任务由线程池调度执行
     * @param task
     */
    public void submitDelayTask(Task<?, ?> task) {
        //TODO：：唤醒条件 wifi 时间
        scheduledThreadPool.schedule(task, 600, TimeUnit.SECONDS);
    }
}
