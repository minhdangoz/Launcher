package com.klauncher.kinflow.search.model;

import com.klauncher.kinflow.common.utils.Const;

/**
 * Created by xixionghui on 16/5/4.
 */
public enum SearchEnum {

    BAIDU(1,Const.URL_HOT_WORD),SHENMA(2,Const.SHEN_MA_HOTWORD_24);

    private int id;
    private String url_obtainHotword;
//    private String url_searchKeyword;

    SearchEnum(int id,String url_obtainHotword){
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
        return "SearchEnum: id= "+this.id
                +" url_obtainHotword= "+this.url_obtainHotword;
    }

    public static SearchEnum random() {
        //(数据类型)(最小值+Math.random()*(最大值-最小值+1))
        int i = (int)(1+Math.random()*(2-1+1));
        switch (i) {
            case 1:
                return BAIDU;
            case 2:
                return SHENMA;
            default:
                return BAIDU;
        }
    }
}
