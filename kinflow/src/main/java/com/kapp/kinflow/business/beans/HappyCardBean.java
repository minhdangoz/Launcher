package com.kapp.kinflow.business.beans;


import com.kapp.knews.repository.bean.DongFangTouTiao;

/**
 * description：开心一刻
 * <br>author：caowugao
 * <br>time： 2017/04/25 19:48
 */

public class HappyCardBean extends DongFangCardBean {

    public HappyCardBean(String imageUrl, String contentTittle, String content, String secondTittle, String
            thirdTittle) {
        super(imageUrl, contentTittle, content, secondTittle, thirdTittle);
    }

    public HappyCardBean(DongFangTouTiao main, DongFangTouTiao second, DongFangTouTiao third) {
        super(main, second, third);
    }

    public HappyCardBean(HeadlineCardBean cardBean) {
        super(cardBean);
    }

    @Override
    public int getItemType() {
        return TYPE_HAPPY;
    }

}
