package com.klauncher.setupwizard.common;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/03/24 21:30
 */

public class ConfigurationUtil {

    private ConfigurationUtil() {
    }

    public static void updateAppLanguage(Context context, Locale language) {
        Resources resource = context.getResources();
        Configuration config = resource.getConfiguration();
        config.locale = language;
        resource.updateConfiguration(config, null);
    }
}
