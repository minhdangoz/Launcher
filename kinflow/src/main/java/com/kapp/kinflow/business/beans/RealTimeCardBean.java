package com.kapp.kinflow.business.beans;

import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

import java.util.List;


/**
 * description：实时热点CardBean
 * <br>author：caowugao
 * <br>time： 2017/05/18 17:17
 */

public class RealTimeCardBean extends BaseInformationFlowBean {
    public List<SingeRealTimeBean> beanList;

    public RealTimeCardBean(List<SingeRealTimeBean> beanList) {
        this.beanList = beanList;
    }

    @Override
    public int getItemType() {
        return TYPE_REAL_TIME;
    }

    public static class SingeRealTimeBean extends BaseItemBean {
        public String name;
        public String url;

        public SingeRealTimeBean(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    public void updateCardBean(RealTimeCardBean cardBean) {
        this.beanList = cardBean.beanList;
    }
}
