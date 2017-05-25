package com.kapp.kinflow.business.beans;


import com.kapp.knews.repository.bean.DongFangTouTiao;

/**
 * description：星座CardBean
 * <br>author：caowugao
 * <br>time： 2017/04/25 17:31
 */

public class ConstellationCardBean extends DongFangCardBean {
    public ConstellationCardBean(String imageUrl, String contentTittle, String content, String secondTittle, String
            thirdTittle) {
        super(imageUrl, contentTittle, content, secondTittle, thirdTittle);
    }

    public ConstellationCardBean(DongFangTouTiao main, DongFangTouTiao second, DongFangTouTiao third) {
        super(main, second, third);
    }
    public ConstellationCardBean(HeadlineCardBean cardBean) {
        super(cardBean);
    }

    @Override
    public int getItemType() {
        return TYPE_CONSTELLATION;
    }
}
