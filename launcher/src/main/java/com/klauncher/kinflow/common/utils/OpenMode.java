package com.klauncher.kinflow.common.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.klauncher.kinflow.browser.KinflowBrower;

import java.util.List;

/**
 * Created by xixionghui on 16/4/6.
 */
public class OpenMode {
    private Context mContext;

    public static final int ID_INTERNAL_BROWSER = 0;//内置浏览器
    public static final int ID_JIN_RI_TOU_TIAO = 2;//对应打开方式:Uri
    public static final int ID_QQ_BROWSER = 23;//对应打开方式:component
    public static final int ID_SYSTEM_SETTING = 37; // 系统设置

    public static final String COMPONENT_NAME_QQ_BROWSER = "com.tencent.mtt/com.tencent.mtt.SplashActivity";

    private Intent firstIntent = new Intent();//已知应用
    private Intent secondIntent = new Intent();//指定应用
    private Intent thirdIntent = new Intent();//内嵌浏览器

    //已知应用全部按照id返回,但是对应的打开方式任意一种都存在,下面是打开方式

    public static final String OPEN_URL_KEY = "open_url_key";//第二打开方式,第三打开方式都会用到url

    public static final String FIRST_OPEN_MODE_TYPE_DEFAULT = "firt_open_mode_type_default";
    public static final String FIRST_OPEN_MODE_TYPE_URI = "first_open_mode_type_uri";
    public static final String FIRST_OPEN_MODE_TYPE_COMPONENT = "first_open_mode_type_component";
    public static final String FIRST_OPEN_MODE_TYPE_KEY = "open_mode_type_key";
    public static final String FIRST_OPEN_MODE_TYPE_ACTION = "first_open_mode_type_action";

    private String currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_DEFAULT;

    public Intent setFirstIntent(int id) {
        firstIntent.setAction(Intent.ACTION_VIEW);
        switch (id) {
            case ID_INTERNAL_BROWSER:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_DEFAULT;
                firstIntent.setClass(mContext, KinflowBrower.class);
                break;
            case ID_JIN_RI_TOU_TIAO:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_URI;//设置当前打开方式
                break;
            case ID_QQ_BROWSER:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_COMPONENT;//设置当前打开方式
                String[] cn = COMPONENT_NAME_QQ_BROWSER.split("/");
                ComponentName componentName = new ComponentName(cn[0], cn[1]);
                firstIntent.setComponent(componentName);
                break;
            case ID_SYSTEM_SETTING:
                currentFirstOpenMode = FIRST_OPEN_MODE_TYPE_ACTION;//设置当前打开方式
                if (android.os.Build.VERSION.SDK_INT > 10) {
                    firstIntent.setAction(android.provider.Settings.ACTION_SETTINGS);
                } else {
                    firstIntent.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                }
                break;
        }
        return firstIntent;
    }

    public Intent setSecondIntent(String packNameAndClassName,String openUrl) {
        secondIntent.setAction(Intent.ACTION_VIEW);
        if (TextUtils.isEmpty(packNameAndClassName)) return secondIntent;
        String[] cn = packNameAndClassName.split("/");
        if (cn.length == 2) {
            ComponentName componentName = new ComponentName(cn[0], cn[1]);
            secondIntent.setComponent(componentName);
        }
        if (openUrl != null) {
            secondIntent.setData(Uri.parse(openUrl));
        }
        return secondIntent;
    }

    public Intent getFirstIntent() {
        return firstIntent;
    }

    public Intent getSecondIntent() {
        return secondIntent;
    }

    public Intent getThirdIntent() {
        return thirdIntent;
    }

    public String getCurrentFirstOpenMode() {
        return currentFirstOpenMode;
    }

    public OpenMode(Context context, List<String> cardOpenOptionList, String openUrl) {
        this.mContext = context;
        int size = cardOpenOptionList.size();
        for (int i = 0; i < size; i++) {
            switch (i) {
                case 0:
                    setFirstIntent(Integer.valueOf(cardOpenOptionList.get(i)));
                    break;
                case 1:
                    setSecondIntent(cardOpenOptionList.get(i),openUrl);
                    break;
                case 2:
                    if (openUrl != null) {
                        thirdIntent.setAction(Intent.ACTION_VIEW);
                        thirdIntent.setClass(context, KinflowBrower.class);
                        thirdIntent.putExtra(KinflowBrower.KEY_EXTRA_URL, openUrl);
                        thirdIntent.setData(Uri.parse(openUrl));
                    }
                    break;

            }
        }
    }

}
