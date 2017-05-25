package com.kapp.kinflow.business.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;


/**
 * description：分享的item
 * <br>author：caowugao
 * <br>time： 2017/05/21 18:53
 */

public class ShareItemBean extends BaseItemBean implements Parcelable {
    public int imageResId;
    public String text;

    public ShareItemBean(int imageResId, String text) {
        this.imageResId = imageResId;
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.imageResId);
        dest.writeString(this.text);
    }

    protected ShareItemBean(Parcel in) {
        this.imageResId = in.readInt();
        this.text = in.readString();
    }

    public static final Creator<ShareItemBean> CREATOR = new Creator<ShareItemBean>() {
        @Override
        public ShareItemBean createFromParcel(Parcel source) {
            return new ShareItemBean(source);
        }

        @Override
        public ShareItemBean[] newArray(int size) {
            return new ShareItemBean[size];
        }
    };
}
