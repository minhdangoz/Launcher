package com.klauncher.notifier;

import android.net.Uri;

public class NotifierConsts {
    /*Data Field*/
    public static final String ID = "_id";
    /**
     * The name of the package that the component exists in
     */
    public static final String PACKAGE = "_package";
    /**
     * The name of the class inside of <var>PACKAGE</var> that implements the component.
     */
    public static final String CLASS = "_class";
    /**
     * The count of the notification count.
     */
    public static final String BADGE_COUNT = "_badge_count";

    /*Default sort order*/
    public static final String DEFAULT_SORT_ORDER = "_id asc";

    /*Call Method*/
    public static final String METHOD_GET_ITEM_COUNT = "METHOD_GET_ITEM_COUNT";
    public static final String KEY_ITEM_COUNT = "KEY_ITEM_COUNT";

    /*Authority*/
    public static final String AUTHORITY = "com.klauncher.providers.notifier";

    /*Match Code*/
    public static final int ITEM = 1;
    public static final int ITEM_ID = 2;
    public static final int ITEM_POS = 3;

    /*MIME*/
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.klauncher.notifier";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.klauncher.notifier";

    /*Content URI*/
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/item");
    public static final Uri CONTENT_POS_URI = Uri.parse("content://" + AUTHORITY + "/pos");
}