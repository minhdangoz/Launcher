package com.klauncher.kinflow.utilities;

import com.klauncher.launcher.BuildConfig;

public class ProtocolConst {
    public static final String CARD_UPDATE_URL = "/card/mobile?cid=" + BuildConfig.CHANNEL_ID;
    public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    public static final int HTTP_CONNECT_TIMEOUT = 30 * 1000; // 30s
    public static final int HTTP_READ_TIMEOUT = 60 * 1000; // 60s
}
