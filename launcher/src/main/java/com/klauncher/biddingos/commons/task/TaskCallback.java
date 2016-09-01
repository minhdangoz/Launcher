package com.klauncher.biddingos.commons.task;

/**
 * 任务回调对象
 */
public interface TaskCallback<I, O> {
    /**
     * 任务执行成功时的回调函数
     * @param input 任务输入数据
     * @param output 任务输出数据
     */
    public void onSuccess(I input, O output);

    /**
     * 任务执行失败时的回调函数
     * @param input 任务输入数据
     * @param th 失败的堆栈信息
     */
    public void onFailure(I input, Throwable th);
}
