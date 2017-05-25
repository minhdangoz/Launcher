package com.kapp.kinflow.business.beans;


import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

/**
 * description：添加卡片bean
 * <br>author：caowugao
 * <br>time： 2017/04/20 14:07
 */

public class AddCardBean extends BaseItemBean {
    public int imageResId;
    public String tittle;
    public String subTtittle;
    public boolean isShowed;

    public AddCardBean(int imageResId, String tittle, String subTtittle, boolean isShowed) {
        this.imageResId = imageResId;
        this.tittle = tittle;
        this.subTtittle = subTtittle;
        this.isShowed = isShowed;
    }
}
