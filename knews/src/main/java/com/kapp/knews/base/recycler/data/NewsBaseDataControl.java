package com.kapp.knews.base.recycler.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.kapp.knews.helper.java.collection.CollectionsUtils;

import java.util.List;


/**
 * Created by xixionghui on 2016/11/28.
 */

public class NewsBaseDataControl implements BaseDataInterface, Comparable<NewsBaseDataControl>, Parcelable {
    protected int order;//顺序
    protected List<String> openOptions;//打开方式

    public NewsBaseDataControl(int order, List<String> openOptions) {
        this.order = order;
        this.openOptions = openOptions;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<String> getOpenOptions() {
        return openOptions;
    }

    public void setOpenOptions(List<String> openOptions) {
        this.openOptions = openOptions;
    }


    @Override
    public boolean isNull() {
        if (CollectionsUtils.collectionIsNull(openOptions))
            return true;
        return false;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.order);
        dest.writeStringList(this.openOptions);
    }

    protected NewsBaseDataControl(Parcel in) {
        this.order = in.readInt();
        this.openOptions = in.createStringArrayList();
    }

    public static final Creator<NewsBaseDataControl> CREATOR = new Creator<NewsBaseDataControl>() {
        @Override
        public NewsBaseDataControl createFromParcel(Parcel source) {
            return new NewsBaseDataControl(source);
        }

        @Override
        public NewsBaseDataControl[] newArray(int size) {
            return new NewsBaseDataControl[size];
        }
    };

    @Override
    public int compareTo(NewsBaseDataControl another) {
        if (getOrder() < another.getOrder()) {
            return 1;
        } else if (getOrder() > another.getOrder()) {
            return -1;
        }
        return 0;
    }
}
