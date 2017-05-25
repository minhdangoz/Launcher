package com.kapp.kinflow.business.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * description：数学计算辅助类
 * <br>author：caowugao
 * <br>time： 2017/05/23 14:50
 */

public class MathUtil {
    private static final Random RANDOM = new Random(100);// 种子100与生成的数值区间无关，种子相同的两个Random表示他们是相同对象

    private MathUtil() {
    }

    /**
     * 返回随机不同的数字的数组
     *
     * @param total      取数字的范围，[0,total)
     * @param resultSize 要返回结果的个数， total>resultSize
     * @return Integer[]
     */
    public static Integer[] getRandDifferentIntValue(int total, int resultSize) {
        if (total < 0) {
            return null;
        }
        if (resultSize <= 0) {
            new IllegalArgumentException("resultSize can not less than 0");
        }
        if (total <= resultSize) {
            new IllegalArgumentException("total must bigger than resultSize");
        }
        Integer[] results = new Integer[resultSize];
        Set<Integer> resultSet = new HashSet<>();
        while (resultSet.size() != resultSize) {
            int randInt = RANDOM.nextInt(total);
            resultSet.add(randInt);
        }
        resultSet.toArray(results);
        return results;
    }
}
