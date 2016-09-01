package com.alpsdroid.ads.commons;

import android.content.Context;

/**
 * Created by：lizw on 2016/1/6 11:49
 */
public interface AdConfigHelper {
    void init(int adMode, Context context, String mid, String afid, String kid, String secret, boolean
            debuggable);
    void destroy();
}
