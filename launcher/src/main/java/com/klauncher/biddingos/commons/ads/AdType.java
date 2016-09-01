package com.klauncher.biddingos.commons.ads;

/**
 * 广告类型
 */
public enum AdType {
    APP("app");

    private String val;

    private AdType(String val) {
        this.val = val;
    }

    public String val() {
        return val;
    }

    /**
     * 根据文本内容返回对应广告类型枚举对象
     * @param val
     * @return
     */
    public static AdType parse(String val) {
        for (AdType t : AdType.values()) {
            if (t.val().equalsIgnoreCase(val))
                return t;
        }

        return null;
    }


}
