package com.alpsdroid.ads;

/**
 * 用于标识一个广告位信息
 */
public class Placement {
    /**
     * 广告位在列表中的绝对位置
     */
    private int pos;

    /**
     * 该广告位置对应的广告位ID
     */
    private int zoneid;

    /**
     * 初始化一个广告位
     * @param pos 广告位在列表中的绝对位置
     * @param zoneid 该广告位置对应的广告位ID
     */
    public Placement(int pos, int zoneid) {
        this.pos = pos;
        this.zoneid = zoneid;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getZoneid() {
        return zoneid;
    }

    public void setZoneid(int zoneid) {
        this.zoneid = zoneid;
    }
}
