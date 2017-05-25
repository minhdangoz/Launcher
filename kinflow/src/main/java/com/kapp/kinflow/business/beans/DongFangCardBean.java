package com.kapp.kinflow.business.beans;


import com.kapp.knews.repository.bean.DongFangTouTiao;

/**
 * description：东方CardBean
 * <br>author：caowugao
 * <br>time： 2017/04/25 17:28
 */

public class DongFangCardBean extends BaseInformationFlowBean {
    public String imageUrl;
    public String contentTittle;
    public String content;

    public String secondTittle;
    public String thirdTittle;

    public DongFangTouTiao main;
    public DongFangTouTiao second;
    public DongFangTouTiao third;

    public DongFangCardBean(String imageUrl, String contentTittle, String content, String secondTittle, String
            thirdTittle) {
        this.imageUrl = imageUrl;
        this.contentTittle = contentTittle;
        this.content = content;
        this.secondTittle = secondTittle;
        this.thirdTittle = thirdTittle;
    }

    public DongFangCardBean(DongFangTouTiao main, DongFangTouTiao second, DongFangTouTiao third) {
        setDongFangTouTiao(main, second, third);
    }

    private void setDongFangTouTiao(DongFangTouTiao main, DongFangTouTiao second, DongFangTouTiao third) {
        this.main = main;
        this.second = second;
        this.third = third;

        this.imageUrl = main.getmNewsBean().getSummaryImageUrlList().get(0);
        this.contentTittle = main.getmNewsBean().getSummaryTitle();
        this.content = main.getmNewsBean().getSource();
        this.secondTittle = null == second ? null : second.getmNewsBean().getSummaryTitle();
        this.thirdTittle = null == third ? null : third.getmNewsBean().getSummaryTitle();
    }

    public DongFangCardBean(DongFangCardBean cardBean) {
        this(cardBean.main, cardBean.second, cardBean.third);
    }

    public void updateCardBean(DongFangCardBean cardBean) {
        setDongFangTouTiao(cardBean.main, cardBean.second, cardBean.third);
    }
}
