package com.kapp.knews.repository.bean.lejingda.klauncher;


import com.kapp.knews.base.recycler.data.NewsBaseDataBean;

/**
 * Created by xixionghui on 2016/12/16.
 */

public class KlauncherAdBean extends NewsBaseDataBean {



    private String imageUrl;
    private String clickUrl;


    public KlauncherAdBean(String imageUrl, String clickUrl){
        this.imageUrl = imageUrl;
        this.clickUrl = clickUrl;
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
        return "KlauncherAdBean{" +
                "imageUrl='" + imageUrl + '\'' +
                ", clickUrl='" + clickUrl + '\'' +
                '}';
    }
}
