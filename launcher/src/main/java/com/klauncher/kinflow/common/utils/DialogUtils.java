package com.klauncher.kinflow.common.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by xixionghui on 16/4/23.
 */
public class DialogUtils {

    public static AlertDialog getCommontAlertDialog(Context context,
                                                    DialogInterface.OnClickListener netativeButtonListener,
                                                    DialogInterface.OnClickListener positiveButtonListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("无法获取当前位置信息");
        builder.setMessage("是否打开位置服务,不打开位置服务将无法获取天气信息");
        builder.setNegativeButton("取消", netativeButtonListener);
        builder.setPositiveButton("好的", positiveButtonListener);
        return builder.create();
    }

}
