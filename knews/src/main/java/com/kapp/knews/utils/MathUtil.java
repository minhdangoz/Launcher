package com.kapp.knews.utils;

import java.util.Random;

/**
 * description：数学辅助类
 * <br>author：caowugao
 * <br>time： 2017/01/09 18:05
 */

public class MathUtil {
    private static final Random RANDOM = new Random(100);// 种子100与生成的数值区间无关

    private MathUtil() {
    }

    /**
     * 随机生成int，区间[0,n)之间的整数
     *
     * @param range
     * @return
     */
    public static int randomInt(int range) {
        return RANDOM.nextInt(range);
    }
}
