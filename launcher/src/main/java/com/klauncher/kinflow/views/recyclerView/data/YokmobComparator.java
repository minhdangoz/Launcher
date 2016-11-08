package com.klauncher.kinflow.views.recyclerView.data;

import java.util.Comparator;

/**
 * 作者:  android001
 * 创建时间:   16/11/8  下午2:20
 * 版本号:
 * 功能描述:
 */
public class YokmobComparator implements Comparator<YokmobBanner> {
    @Override
    public int compare(YokmobBanner lhs, YokmobBanner rhs) {

        try {
            if (lhs.getOrder()>rhs.getOrder()) {
                return 1;
            } else if (lhs.getOrder()<rhs.getOrder()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return  0;
        }

    }

    private static YokmobComparator yokmobComparator = null;
    public static YokmobComparator getInstance() {
        if (null==yokmobComparator)
            yokmobComparator = new YokmobComparator();
        return yokmobComparator;
    }

}
