package com.klauncher.biddingos.commons.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by：lizw on 2016/1/21 16:34
 */
public class AdAffairThreadPool {
    private static ExecutorService cachedThreadPool = null;

    public static ExecutorService getAffairThreadPool() {
        if(null==cachedThreadPool) {
            synchronized (AdAffairThreadPool.class) {
                if(null==cachedThreadPool) {
                    cachedThreadPool= Executors.newCachedThreadPool();
                }
            }
        }
        return cachedThreadPool;
    }
}
