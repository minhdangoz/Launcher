package com.klauncher.kinflow.views.recyclerView.data;

import com.klauncher.kinflow.common.utils.OpenMode;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 16/9/21.
 */
public class YokmobBanner extends BaseRecyclerViewAdapterData {

    String imageUrl;
    String clickUrl;
    int bannerType;

    public static final int BANNER_TYPE_COMMON = 111;
    public static final int BANNER_TYPE_GAODE = 112;

    public YokmobBanner (String imageUrl,String clickUrl,int bannerType,int order,List<String> openOptions) {
        this.imageUrl = imageUrl;
        this.clickUrl = clickUrl;
        this.bannerType = bannerType;
        this.order = order;
        this.openOptions = openOptions;
        setKinflowConentType(BaseRecyclerViewAdapterData.TYPE_BANNER);
    }

    public YokmobBanner (JSONObject jsonObjectRoot,int bannerType){
        try {
            if (null==jsonObjectRoot) return;
            this.imageUrl = jsonObjectRoot.optString("img");
            this.clickUrl = jsonObjectRoot.optString("clc");
            this.bannerType = bannerType;
            setKinflowConentType(BaseRecyclerViewAdapterData.TYPE_BANNER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 命名有问题,这里获取的高德——周末去哪儿
     * @return
     */
    public static YokmobBanner getDefaultYokmobBanner () {
        List<String> openOptions = new ArrayList<>();
        openOptions.add("com.autonavi.minimap/com.autonavi.map.activity.SplashActivity");
        YokmobBanner yokmobBanner = new YokmobBanner(
                "http://kapp.ufile.ucloud.com.cn/gaode.png",
                "http://www.cnlofter.com/",
                BANNER_TYPE_GAODE,
                4,//第五个位置
                openOptions
                );
        return yokmobBanner;
    }

    public static YokmobBanner getCommmonYokmobBanner(){
        List<String> openOptions = new ArrayList<>();
        openOptions.add(OpenMode.COMPONENT_NAME_UC_BROWSER);
        openOptions.add(OpenMode.COMPONENT_NAME_QQ_BROWSER);
        openOptions.add(OpenMode.COMPONENT_NAME_BAIDU_BROWSER_1);
        openOptions.add(BaseRecyclerViewAdapterData.INNER_BROWSER_PACKAGENAME);
        YokmobBanner yokmobBanner = new YokmobBanner(
                "http://imglf1.nosdn.127.net/img/a0JLb2MxL0R1RXF4Q2pndFMzTmw4Z3BnY2ZuTHNhdmtQanpKOWwzUE5vNkg4Rlk2NGN1NkJRPT0.png?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
                "http://s.click.taobao.com/v9KZ4Px",
                BANNER_TYPE_COMMON,
                6,
                openOptions
        );
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

    public int getBannerType() {
        return bannerType;
    }

    public void setBannerType(int bannerType) {
        this.bannerType = bannerType;
    }

    @Override
    public String toString() {
        return "YokmobBanner{" +
                "imageUrl='" + imageUrl + '\'' +
                ", clickUrl='" + clickUrl + '\'' +
                ", bannerType='" + bannerType + '\'' +
                '}';
    }
}
