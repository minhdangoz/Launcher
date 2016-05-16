package com.klauncher.kinflow.search.model;

import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by xixionghui on 16/5/4.
 */
public enum SearchEnum {

    BAIDU(1, Const.URL_HOT_WORD), SHENMA(2, Const.SHEN_MA_HOTWORD_24);

    private int id;
    private String url_obtainHotword;
//    private String url_searchKeyword;

    SearchEnum(int id, String url_obtainHotword) {
        this.id = id;
        this.url_obtainHotword = url_obtainHotword;
    }

    public int getId() {
        return id;
    }

    public String getUrl_obtainHotword() {
        return url_obtainHotword;
    }

    @Override
    public String toString() {
        return "SearchEnum: id= " + this.id
                + " url_obtainHotword= " + this.url_obtainHotword;
    }

    /**
     * 随机获取百度||神马热词
     *
     * @return
     */
    public static SearchEnum random() {
        //(数据类型)(最小值+Math.random()*(最大值-最小值+1))
        int i = (int) (1 + Math.random() * (2 - 1 + 1));
        switch (i) {
            case 1:
                return BAIDU;
            case 2:
                return SHENMA;
            default:
                return BAIDU;
        }
    }

    /**
     * 概率选择
     * @param keyChanceMap key为唯一标识，value为该标识的概率，是去掉%的数字
     * @return 被选中的key。未选中返回随机random
     */
    public static SearchEnum chanceSelect (Map<SearchEnum,Integer> keyChanceMap) {
        if (null == keyChanceMap || keyChanceMap.size() ==0) return random();
        Integer sum = 0;
        for (Integer value : keyChanceMap.values()) {
            sum += value;
        }
        // 从1开始
        Integer rand = new Random().nextInt(sum) + 1;
        for (Map.Entry<SearchEnum,Integer> entry : keyChanceMap.entrySet()) {
            rand -= entry.getValue();
            //选中
            if (rand <= 0) {
                return entry.getKey();
            }
        }
        return random();
    }

    public static SearchEnum rateSearchEnum(){
        Map<SearchEnum,Integer> keyChanceMap = new HashMap<>();
        Integer baiduWeight = 50;
        try {
            baiduWeight = Integer.valueOf(CommonShareData.getString("bd_prct","50"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        keyChanceMap.put(BAIDU,baiduWeight);

        Integer shenmaWeight = 50;
        try {
            shenmaWeight = Integer.valueOf(CommonShareData.getString("sm_prct","50"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        keyChanceMap.put(SHENMA,shenmaWeight);
        return chanceSelect(keyChanceMap);
    }
}
