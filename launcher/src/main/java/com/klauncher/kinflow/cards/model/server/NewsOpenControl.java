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
public class NewsOpenControl implements Parcelable {

    public static final String NEWS_ORDER = "no";//必返回字段:排序位置
    public static final String NEWS_OPEN_OPTION = "ops";//必返回字段:打开方式

    public static final String OPEN_OPTION_NEWS_CLIENT = "xxx/www";//打开方式列表中,如果有这个,则找到对应客户端包名/类名

    private int newsOrder;//新闻排序位置
    private List<String> newsOpenOptions;//打开方式

    public int getNewsOrder() {
        return newsOrder;
    }

    public void setNewsOrder(int newsOrder) {
        this.newsOrder = newsOrder;
    }

    public List<String> getNewsOpenOptions() {
        return newsOpenOptions;
    }

    public void setNewsOpenOptions(List<String> newsOpenOptions) {
        this.newsOpenOptions = newsOpenOptions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.newsOrder);
        dest.writeStringList(this.newsOpenOptions);
    }

    public NewsOpenControl() {
    }

    protected NewsOpenControl(Parcel in) {
        this.newsOrder = in.readInt();
        this.newsOpenOptions = in.createStringArrayList();
    }

    public static final Parcelable.Creator<NewsOpenControl> CREATOR = new Parcelable.Creator<NewsOpenControl>() {
        @Override
        public NewsOpenControl createFromParcel(Parcel source) {
            return new NewsOpenControl(source);
        }

        @Override
        public NewsOpenControl[] newArray(int size) {
            return new NewsOpenControl[size];
        }
    };

    public NewsOpenControl (JSONObject jsonObject) {

        try {
            this.newsOrder = jsonObject.optInt(NEWS_ORDER);
            JSONArray jsonArrayOpenOptionList = jsonObject.optJSONArray(NEWS_OPEN_OPTION);
            List<String> opsList = new ArrayList<>();
            int opsJsonArrayLength = jsonArrayOpenOptionList.length();
            if (opsJsonArrayLength<=0) {
                opsList.add(OPEN_OPTION_NEWS_CLIENT);
                opsList.add("com.baidu.browser.apps/com.baidu.browser.framework.BdBrowserActivity");
                opsList.add("com.klauncher.launcher/com.klauncher.kinflow.browser.KinflowBrower");
            } else {
                for (int j = 0; j < opsJsonArrayLength; j++) {
                    opsList.add(jsonArrayOpenOptionList.optString(j));
                }
            }
            this.newsOpenOptions = opsList;
        } catch (Exception e) {
            KinflowLog.e("新闻控制器数据解析时出现错误,详情: " + e.getMessage());
        }

    }
}
