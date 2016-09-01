package com.klauncher.biddingos.commons.task;


import com.klauncher.biddingos.commons.utils.LogUtils;

/**
 * 后台任务对象
 */
public abstract class Task<I, O> implements Runnable {

    private static final String TAG = "TASK";
    /**
     * 任务输入数据
     */
    private I input;

    /**
     * 任务回调对象
     */
    private TaskCallback<I, O> callback;

    protected Task(I input,TaskCallback<I, O> callback) {
        this.input = input;
        this.callback = callback;
    }

    /**
     * 调用指定
     * @param input
     * @return
     */
    public abstract O execute(I input) throws Exception;

    @Override
    public final void run() {
        try {
            O output = execute(input);
            try {
                callback.onSuccess(input, output);
            } catch (Exception e) {
            }
        } catch (Throwable th) {
            LogUtils.w(TAG, "", th);
            try {
                callback.onFailure(input, th);
            } catch (Exception e) {
            }
        }
    }
}
