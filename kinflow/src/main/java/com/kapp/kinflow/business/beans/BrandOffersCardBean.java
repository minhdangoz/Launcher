package com.kapp.kinflow.business.beans;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * description：品牌优惠卡片
 * <br>author：caowugao
 * <br>time： 2017/05/22 15:08
 */

public class BrandOffersCardBean extends BaseInformationFlowBean {

    public List<SingleBrandOffersBean> brandOffersBeanList;

    public BrandOffersCardBean(List<SingleBrandOffersBean> brandOffersBeanList) {
        this.brandOffersBeanList = brandOffersBeanList;
    }

    public static class SingleBrandOffersBean {
        public String imageUrl;
        public String clickUrl;

        public SingleBrandOffersBean(String imageUrl, String clickUrl) {
            this.imageUrl = imageUrl;
            this.clickUrl = clickUrl;
        }

        public SingleBrandOffersBean(JSONObject jsonObject) {
            try {
                imageUrl = jsonObject.getString("masterMap");
                clickUrl = jsonObject.getString("detailsUrl");
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
    }

    public void updateCardBean(BrandOffersCardBean cardBean) {
        this.brandOffersBeanList = cardBean.brandOffersBeanList;
    }

    @Override
    public int getItemType() {
        return TYPE_BRAND_OFFERS;
    }
}
