package com.kapp.kinflow.business.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Process;

/**
 * description：权限辅助类
 * <br>author：caowugao
 * <br>time： 2017/05/24 14:14
 */

public class PermissionUtil {
    private PermissionUtil() {
    }

    /**
     * 同ContextCompat.checkSelfPermission(Context context, String permission)
     *
     * @param context
     * @param permission
     * @return
     */
    public static int checkSelfPermission(Context context, String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }
        return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
    }

    /**
     * @param activity
     * @param permissions
     * @param requestCode
     */
    public static void requestPermissions(final Activity activity, final String[] permissions, final int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            activity.requestPermissions(permissions, requestCode);
        }
    }
}
