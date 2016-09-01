package com.alpsdroid.ads.feeds;


import android.graphics.Bitmap;
import android.text.TextUtils;

import com.alpsdroid.ads.feeds.ImageLoadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 广告对象
 */
public class FeedsAppInstallAd implements Serializable {
    private String app_id;    //	该投放应用在BiddingOS的ID
    private String app_package;//	String	该投放应用的包名或BundleID
    private String app_logo_url;//	URL String	该投放应用的图标URL
    private String app_name;//	String	该投放应用的名称
    private String app_comment;//	String	该投放应用的一句话描述
    private String app_desc;//	String	该投放应用的详细描述
    private String app_size;//	String	该投放应用的安装包大小
    private String app_checksum;//	String	该投放应用的安装包MD5值
    private String app_rank;//该投放应用的评级，值为0-10，10 是最大
    private String app_image_url;//	String	该投放应用的图片素材的链接地址
    private Bitmap app_image;//	String	图片
    private Bitmap app_logo;//	String	logo图片
    private String app_star;//	String	星星
    private int zoneId;//	广告位
    private String action_text;
    private String pay_mode;

    public FeedsAppInstallAd() {

    }

    public String getPay_mode() {
		return pay_mode;
	}

	public FeedsAppInstallAd(int zoneId,String pay_mode,String action_text,JSONObject jsonAssets) throws JSONException {
        if (TextUtils.isEmpty(jsonAssets.toString())) {
            return;
        }
        this.zoneId=zoneId;
        this.pay_mode=pay_mode;
        this.action_text=action_text;
        this.app_id = jsonAssets.getString("app_id");
        this.app_package = jsonAssets.getString("app_package");
        this.app_logo_url = jsonAssets.getString("app_logo");

        if(jsonAssets.isNull("app_name")) {
        	throw new JSONException("app_name is empty");
        }
        this.app_name = jsonAssets.getString("app_name");
        this.app_desc = jsonAssets.getString("app_desc");
        this.app_size = jsonAssets.getString("app_size");
        this.app_comment = jsonAssets.getString("app_comment");
        this.app_checksum = jsonAssets.getString("app_checksum");
        this.app_rank = jsonAssets.getString("app_rank");
        this.app_image_url = jsonAssets.getString("app_image_url");
        this.app_star = jsonAssets.getString("app_star");
        try {
            ImageLoadUtils.onLoadImage(new URL(this.app_logo_url), new ImageLoadUtils.OnLoadImageListener() {
                @Override
                public void OnLoadImage(Bitmap bitmap, String bitmapPath) {
                    app_logo = bitmap;
                }
            });
                ImageLoadUtils.onLoadImage(new URL(this.app_image_url), new ImageLoadUtils.OnLoadImageListener() {
                @Override
                public void OnLoadImage(Bitmap bitmap, String bitmapPath) {
                    app_image=bitmap;
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getApp_id() {
        return app_id;
    }


    public String getApp_package() {
        return app_package;
    }


    public String getHeadline() {
        return app_name;
    }
    public int getZoneId() {
        return zoneId;
    }
    public String getAction_text() {
        return action_text;
    }

    public String getApp_desc() {
        return app_desc;
    }



    public String getApp_size() {
        return app_size;
    }



    public String getApp_checksum() {
        return app_checksum;
    }


    public String getApp_star() {
        return app_star;
    }

    public String getApp_rank() {
        return app_rank;
    }

    public String getStore() {
        return app_comment;
    }

    public Bitmap getImage() {
        return app_image;
    }
    public Bitmap getIcon() {
        return app_logo;
    }



    @Override
    public String toString() {
        return "FeedsAppInstallAd{" +
                "app_id='" + app_id + '\'' +
                ", app_package='" + app_package + '\'' +
                ", app_logo='" + app_logo + '\'' +
                ", app_name='" + app_name + '\'' +
                ", app_desc='" + app_desc + '\'' +
                ", app_size='" + app_size + '\'' +
                ", app_checksum='" + app_checksum + '\'' +
                ", app_rank='" + app_rank + '\'' +
                ", app_image_url='" + app_image_url + '\'' +
                ", app_image=" + app_image +
                '}';
    }
}
