package com.klauncher.kinflow.views.recyclerView.data;

/**
 * Created by xixionghui on 16/9/21.
 */
public class YokmobBanner extends BaseRecyclerViewAdapterData {

    String imageUrl;
    String clickUrl;

    public YokmobBanner (String imageUrl,String clickUrl) {
        this.imageUrl = imageUrl;
        this.clickUrl = clickUrl;
        setType(BaseRecyclerViewAdapterData.TYPE_BANNER);
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
