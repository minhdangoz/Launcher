package com.klauncher.kinflow.cards.model.yidian;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by xixionghui on 2016/3/30.
 */
public class YiDianModel implements Parcelable {
    private String title;//文章标题
    private String docid;//文章id，用于去重
    private String date;//文章发布时间
    private String url;//文章链接，用于打开h5新闻详情页
    private String source;//文章来源
    private String[] images;//图片链接数组，目前支持单图和三图两种模式

    public YiDianModel() {
    }

    public YiDianModel(String title, String docid, String date, String url, String source, String[] images) {
        this.title = title;
        this.docid = docid;
        this.date = date;
        this.url = url;
        this.source = source;
        if (null == images) {
            this.images = new String[0];
        }else {
            this.images = images;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocid() {
        return docid;
    }

    public void setDocid(String docid) {
        this.docid = docid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "YiDianModel{" +
                "title='" + title + '\'' +
                ", docid='" + docid + '\'' +
                ", date='" + date + '\'' +
                ", url='" + url + '\'' +
                ", source='" + source + '\'' +
                ", images=" + Arrays.toString(images) +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.docid);
        dest.writeString(this.date);
        dest.writeString(this.url);
        dest.writeString(this.source);
        dest.writeStringArray(this.images);
    }

    protected YiDianModel(Parcel in) {
        this.title = in.readString();
        this.docid = in.readString();
        this.date = in.readString();
        this.url = in.readString();
        this.source = in.readString();
        this.images = in.createStringArray();
    }

    public static final Parcelable.Creator<YiDianModel> CREATOR = new Parcelable.Creator<YiDianModel>() {
        @Override
        public YiDianModel createFromParcel(Parcel source) {
            return new YiDianModel(source);
        }

        @Override
        public YiDianModel[] newArray(int size) {
            return new YiDianModel[size];
        }
    };
}
