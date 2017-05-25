package com.kapp.kinflow.business.beans;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * description：每日美图CardBean
 * <br>author：caowugao
 * <br>time： 2017/05/18 11:35
 */

public class DailyMitoCardBean extends BaseInformationFlowBean {
    public String picUrl;
    public String clickUrl;
    private static final String DEFAULT_IMAGE_URL = "http://opgirl-tmp.adbxb" +
            ".cn/images1/75/4_b4a6217cebe89d01ab80e076338aed87_78.jpg";
    private static final String DEFAULT_CLICK_URL = "www.27270.com";

    public DailyMitoCardBean(String picUrl, String clickUrl) {
        this.picUrl = picUrl;
        this.clickUrl = clickUrl;
    }

    public static DailyMitoCardBean getDefault() {
        return new DailyMitoCardBean(DEFAULT_IMAGE_URL, DEFAULT_CLICK_URL);
    }

    public DailyMitoCardBean(JSONObject jsonObject) {
        try {
            picUrl = jsonObject.getString("url");
            clickUrl = jsonObject.getString("detail");
        } catch (JSONException e) {
            e.printStackTrace();
            picUrl = DEFAULT_IMAGE_URL;
            clickUrl = DEFAULT_CLICK_URL;
        }
    }

    @Override
    public int getItemType() {
        return TYPE_DAILY_MITO;
    }

    public void updateCardBean(DailyMitoCardBean cardBean) {
        picUrl = cardBean.picUrl;
        clickUrl = cardBean.clickUrl;
    }

}
