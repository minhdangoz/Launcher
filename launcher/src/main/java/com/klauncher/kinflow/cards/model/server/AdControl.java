package com.klauncher.kinflow.cards.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import com.klauncher.kinflow.utilities.KinflowLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 16/9/27.
 */
public class AdControl implements Parcelable {

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

    public void setAdExtra(String adExtra) {
        this.adExtra = adExtra;
    }

    @Override
    public String toString() {
        return "AdControl{" +
                "adOrder=" + adOrder +
                ", adOpenOptions=" + adOpenOptions +
                ", adSID='" + adSID + '\'' +
                '}';
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
