
package com.android.launcher3;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class FolderTitle {
    private static final String TAG = "FolderTitle";

    private FolderTitle() {

    }

    public static int insert(LauncherProvider launcherProvider, long containerId, String[] titles, String[] counties) {
        int id = -1;

        if (containerId < 0 || null == titles || titles.length == 0 ||
                counties == null || counties.length == 0) {
            return -1;
        }

        Log.d(TAG, "count = " + titles.length);
        Log.d(TAG, "containerId = " + containerId);

        ContentValues[] allValues = new ContentValues[titles.length];

        for (int i = 0; i < titles.length; i++) {

            Log.d(TAG, "title[" + i + "] = " + titles[i]);
            Log.d(TAG, "counties[" + i + "] = " + counties[i]);

            allValues[i] = new ContentValues();
            allValues[i].put(FolderTitleColumns.FOLDER_ID, containerId);
            allValues[i].put(FolderTitleColumns.TITLE, titles[i]);
            allValues[i].put(FolderTitleColumns.COUNTRY, counties[i]);

        }

        return launcherProvider.bulkInsertFolderTitles(allValues);
    }

    public static final class FolderTitleColumns implements BaseColumns {

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + LauncherProvider.AUTHORITY + "/"
                + LauncherProvider.TABLE_FOLDER_TITLES);

        /**
         * The id of the folder storaged in table favorites
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String FOLDER_ID = "folder_id";

        /**
         * The country
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String COUNTRY = "country";

        /**
         * The title of folder
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String TITLE = "title";
    }
}
