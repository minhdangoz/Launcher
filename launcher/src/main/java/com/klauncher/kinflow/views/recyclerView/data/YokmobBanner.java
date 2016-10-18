package com.klauncher.kinflow.views.recyclerView.data;

import org.json.JSONObject;

/**
 * Created by xixionghui on 16/9/21.
 */
public class YokmobBanner extends BaseRecyclerViewAdapterData {

    String imageUrl;
    String clickUrl;

    public YokmobBanner (String imageUrl,String clickUrl) {
        this.imageUrl = imageUrl;
        this.clickUrl = clickUrl;
        setKinflowConentType(BaseRecyclerViewAdapterData.TYPE_BANNER);
    }

    public YokmobBanner (JSONObject jsonObjectRoot){
        try {
            if (null==jsonObjectRoot) return;
            this.imageUrl = jsonObjectRoot.optString("img");
            this.clickUrl = jsonObjectRoot.optString("clc");
            setKinflowConentType(BaseRecyclerViewAdapterData.TYPE_BANNER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static YokmobBanner getDefaultYokmobBanner () {
        YokmobBanner yokmobBanner = new YokmobBanner(
                "http://imglf1.nosdn.127.net/img/a0JLb2MxL0R1RXFTK0xvaTVuZ3o5L3oyV3lLMXlJYTlKdHorYkNsdU5vZHYzV0pxdHpKSy9RPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
                "http://www.cnlofter.com/");
        return yokmobBanner;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getClickUrl() {
        return clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    @Override
    public String toString() {
        return "YokmobBanner{" +
                "imageUrl='" + imageUrl + '\'' +
                ", clickUrl='" + clickUrl + '\'' +
                '}';
    }
}
