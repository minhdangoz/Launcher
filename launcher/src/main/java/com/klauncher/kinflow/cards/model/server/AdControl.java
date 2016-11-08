package com.klauncher.kinflow.cards.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import com.klauncher.kinflow.utilities.KinflowLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xixionghui on 16/9/27.
 */
public class AdControl implements Parcelable {

    //广告内容来源类型
    public static final int SID_FROM_EXTRAS_INFO = 0;//广告内容来源于附加信息
    public static final int SID_FROM_OUR_AD_PLATFORM = 1;//广告内容来源于我公司自身的广告平台
    public static final int SID_FROM_BAIDU_AD = 2;// 广告内容来源于百度
    public static final int SID_FROM_ADVIE_AD = 3;// 广告内容来源于adview

    public static final String AD_ORDER = "no";//必返回字段:排序位置
    public static final String AD_OPEN_OPTION = "ops";//必返回字段:打开方式
    public static final String AD_SID = "sid";//必返回字段:广告位,id是广告平台分配给我们的值，对接api时需要这个字段
    public static final String AD_EXTRA = "ai";//附加字段

    private int adOrder;//广告插入的位置
    private List<String> adOpenOptions;//广告打开方式列表
    private String adSID;//广告的SID:广告平台分配给我们的值，对接api时需要这个字段
    private String adExtra;//广告的预留字段

    public int getAdOrder() {
        return adOrder;
    }

    public void setAdOrder(int adOrder) {
        this.adOrder = adOrder;
    }

    public List<String> getAdOpenOptions() {
        return adOpenOptions;
    }

    public void setAdOpenOptions(List<String> adOpenOptions) {
        this.adOpenOptions = adOpenOptions;
    }

    public String getAdSID() {
        return adSID;
    }

    public void setAdSID(String adSID) {
        this.adSID = adSID;
    }

    public String getAdExtra() {
        return adExtra;
    }

    public static final String KEY_IMAGE_URL = "img";
    public static final String KEY_IMAGE_CLICK_URL = "clc";

    public static final String defaultImageUrl = "http://imglf1.nosdn.127.net/img/a0JLb2MxL0R1RXFTK0xvaTVuZ3o5L3oyV3lLMXlJYTlKdHorYkNsdU5vZHYzV0pxdHpKSy9RPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg";
    public static final String defaultClickImageUrl = "http://www.cnlofter.com/";
    public Map<String,String> adExtra2ImageUrl(){
        Map<String,String> imageUrlMap = new HashMap<String,String>();
        try {
            JSONObject adExtraJson = new JSONObject(getAdExtra());
            String imageUrl = adExtraJson.optString(KEY_IMAGE_URL,defaultImageUrl);
            String imageClickUrl = adExtraJson.optString(KEY_IMAGE_CLICK_URL,defaultClickImageUrl);
            imageUrlMap.put(KEY_IMAGE_URL,imageUrl);
            imageUrlMap.put(KEY_IMAGE_CLICK_URL,imageClickUrl);

        } catch (Exception e) {
            imageUrlMap.put(KEY_IMAGE_URL,defaultImageUrl);
            imageUrlMap.put(KEY_IMAGE_CLICK_URL,defaultClickImageUrl);
        }

        return imageUrlMap;
    }

    public void setAdExtra(String adExtra) {
        this.adExtra = adExtra;
    }

    @Override
    public String toString() {
        return "AdControl{" +"\n"
                + "adOrder=" + adOrder +"\n"
                + ", adOpenOptions=" + adOpenOptions +"\n"
                + ", adSID='" + adSID +"\n"
                + ", adExtra="+adExtra +"\n"
                + '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.adOrder);
        dest.writeStringList(this.adOpenOptions);
        dest.writeString(this.adSID);
    }

    protected AdControl(Parcel in) {
        this.adOrder = in.readInt();
        this.adOpenOptions = in.createStringArrayList();
        this.adSID = in.readString();
    }

    public static final Parcelable.Creator<AdControl> CREATOR = new Parcelable.Creator<AdControl>() {
        @Override
        public AdControl createFromParcel(Parcel source) {
            return new AdControl(source);
        }

        @Override
        public AdControl[] newArray(int size) {
            return new AdControl[size];
        }
    };

    public AdControl(JSONObject jsonObject) {

        try {
            //两个比返回
            this.adOrder = jsonObject.optInt(AD_ORDER,0);
            JSONArray jsonArrayOpenOptionList = jsonObject.optJSONArray(AD_OPEN_OPTION);
            List<String> opsList = new ArrayList<>();
            int opsJsonArrayLength = jsonArrayOpenOptionList.length();
            if (opsJsonArrayLength<=0) {
                opsList.add("com.tencent.mtt/com.tencent.mtt.SplashActivity");
                opsList.add("com.baidu.browser.apps/com.baidu.browser.framework.BdBrowserActivity");
                opsList.add("com.klauncher.launcher/com.klauncher.kinflow.browser.KinflowBrower");
            } else {
                for (int j = 0; j < opsJsonArrayLength; j++) {
                    opsList.add(jsonArrayOpenOptionList.optString(j));
                }
            }
            this.adOpenOptions = opsList;
            this.adSID = jsonObject.optString(AD_SID);
            this.adExtra = jsonObject.optString(AD_EXTRA);
        } catch (Exception e) {
            KinflowLog.e("广告控制器数据解析时出现错误,详情: " + e.getMessage());
        }

    }

}
