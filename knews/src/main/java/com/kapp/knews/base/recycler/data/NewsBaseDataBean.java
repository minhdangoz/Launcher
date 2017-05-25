package com.kapp.knews.base.recycler.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.kapp.knews.helper.java.collection.CollectionsUtils;

import java.util.List;


/**
 * Created by xixionghui on 2016/11/28.
 */

public class NewsBaseDataBean implements BaseDataInterface, Parcelable {

    protected String summaryTitle;//摘要标题
    protected String summaryPublishTime;//摘要时间
    protected List<String> summaryImageUrlList;//图片url列表

//    public NewsBaseDataBean(String summaryTitle, String summaryPublishTime, List<String> summaryImageUrlList) {
//        this.summaryTitle = summaryTitle;
//        this.summaryPublishTime = summaryPublishTime;
//        this.summaryImageUrlList = summaryImageUrlList;
//    }

    public String getSummaryTitle() {
        return summaryTitle;
    }

    public void setSummaryTitle(String summaryTitle) {
        this.summaryTitle = summaryTitle;
    }

    public String getSummaryPublishTime() {
        return summaryPublishTime;
    }

    public void setSummaryPublishTime(String summaryPublishTime) {
        this.summaryPublishTime = summaryPublishTime;
    }

    public List<String> getSummaryImageUrlList() {
        return summaryImageUrlList;
    }

    public void setSummaryImageUrlList(List<String> summaryImageUrlList) {
        this.summaryImageUrlList = summaryImageUrlList;
    }

    @Override
    public boolean isNull() {
        if (TextUtils.isEmpty(summaryTitle)|| TextUtils.isEmpty(summaryPublishTime)|| CollectionsUtils.collectionIsNull(summaryImageUrlList))
            return true;
        return false;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.summaryTitle);
        dest.writeString(this.summaryPublishTime);
        dest.writeStringList(this.summaryImageUrlList);
    }

    public NewsBaseDataBean() {
    }

    protected NewsBaseDataBean(Parcel in) {
        this.summaryTitle = in.readString();
        this.summaryPublishTime = in.readString();
        this.summaryImageUrlList = in.createStringArrayList();
    }

    public static final Creator<NewsBaseDataBean> CREATOR = new Creator<NewsBaseDataBean>() {
        @Override
        public NewsBaseDataBean createFromParcel(Parcel source) {
            return new NewsBaseDataBean(source);
        }

        @Override
        public NewsBaseDataBean[] newArray(int size) {
            return new NewsBaseDataBean[size];
        }
    };
}
