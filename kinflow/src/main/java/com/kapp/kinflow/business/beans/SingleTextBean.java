package com.kapp.kinflow.business.beans;


import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

/**
 * description：单文本bean
 * <br>author：caowugao
 * <br>time： 2017/04/20 11:36
 */

public class SingleTextBean extends BaseItemBean {
    public String text;

    public SingleTextBean(String text) {
        this.text = text;
    }
}
