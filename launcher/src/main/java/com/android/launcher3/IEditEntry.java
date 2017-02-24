package com.android.launcher3;

import android.view.View;

/**
 * Created by LEI on 2015/6/5.
 */
public interface IEditEntry {

    /**
     * Handle back pressed event
     * @return return false, edit menu need switch view to menu root. else need not handle it.
     */
    boolean onBackPressed();
    boolean onHomePressed();
}
