package com.kapp.kinflow.business.beans;

import java.util.List;

/**
 * description：小说阅读
 * <br>author：caowugao
 * <br>time： 2017/05/19 20:13
 */

public class NovelCardBean extends BaseInformationFlowBean {

    public List<MineNovelsBean> datas;

    public NovelCardBean(List<MineNovelsBean> datas) {
        this.datas = datas;
    }

    @Override
    public int getItemType() {
        return TYPE_NOVEL;
    }

    public void updateCardBean(NovelCardBean cardBean) {
        this.datas = cardBean.datas;
    }
}
