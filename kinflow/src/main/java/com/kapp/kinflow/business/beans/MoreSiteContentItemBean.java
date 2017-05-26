package com.kapp.kinflow.business.beans;

import com.kapp.kinflow.view.DetailRightLayout;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;


/**
 * description：网址大全
 * <br>author：caowugao
 * <br>time： 2017/04/27 20:10
 */

public class MoreSiteContentItemBean extends BaseItemBean {
    public DetailRightLayout.DetailRightData uiData;

    public MoreSiteContentItemBean(DetailRightLayout.DetailRightData uiData) {
        this.uiData = uiData;
    }
}
