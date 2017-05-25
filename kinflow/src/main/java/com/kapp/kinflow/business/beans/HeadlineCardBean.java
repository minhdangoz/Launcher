package com.kapp.kinflow.business.beans;


import com.kapp.knews.repository.bean.DongFangTouTiao;

/**
 * description：头条CardBean
 * <br>author：caowugao
 * <br>time： 2017/04/19 17:51
 */

public class HeadlineCardBean extends DongFangCardBean {

    public HeadlineCardBean(String imageUrl, String contentTittle, String content, String secondTittle, String
            thirdTittle) {
        super(imageUrl, contentTittle, content, secondTittle, thirdTittle);
    }

    public HeadlineCardBean(DongFangTouTiao main, DongFangTouTiao second, DongFangTouTiao third) {
        super(main, second, third);
    }

    public HeadlineCardBean(HeadlineCardBean cardBean) {
        super(cardBean);
    }

    @Override
    public int getItemType() {
        return TYPE_HEADER_LINE;
    }

}
