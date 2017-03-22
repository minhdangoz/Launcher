package com.klauncher.setupwizard;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.klauncher.setupwizard.hw.HwLanguageSettings;
import com.klauncher.setupwizard.vivo.VivoSetupActivity;

/**
 * Created by hw on 17-3-21.
 */

public class SetupMain {
    public static final String TAG = "SetupMain";

    private static final String HUAWEI_FLAVOR = "huawei";
    private static final String VIVO_FLAVOR = "vivo";
    private static final String OPPO_FLAVOR = "oppo";

    public static void startSetup(Context context, String flavor) {
        try {
            if (TextUtils.equals(flavor, HUAWEI_FLAVOR)) {
                Intent intent = new Intent();
                intent.setClass(context, HwLanguageSettings.class);
                context.startActivity(intent);
            } else if (TextUtils.equals(flavor, VIVO_FLAVOR)) {
                Intent intent = new Intent();
                intent.setClass(context, VivoSetupActivity.class);
                context.startActivity(intent);
            } else if (TextUtils.equals(flavor, OPPO_FLAVOR)) {
            }
        }catch (Exception e){

        }

    }
}
