package com.klauncher.biddingos.feeds;

/**
 * Created by：lizw on 2015/12/30 15:31
 */
public enum ErrorCode {
    REQUESTFAILED(0x000000), ASSETSNOTCOMPLETE(0x000001), HASADDOWNLOADING(0x000002);

    private int value;

    private ErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
