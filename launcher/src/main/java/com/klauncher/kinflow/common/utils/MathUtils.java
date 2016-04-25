package com.klauncher.kinflow.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xixionghui on 16/4/14.
 */
public class MathUtils {

    /**
     * 生成0-n个不重复的随机数
     */
    public static List<Integer> randomNumber(int count) {
        List<Integer> randomList = new ArrayList<>();
        Random random = new Random();
        boolean[] bool = new boolean[count];
        int num = 0;
        for (int i = 0; i < count; i++) {
            do {
                num = random.nextInt(count);
            } while (bool[num]);
            bool[num] = true;
            randomList.add(num);
        }
        return randomList;
    }
}
