package com.kapp.kinflow.business.beans;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;


/**
 * description：头部应用列表
 * <br>author：caowugao
 * <br>time： 2017/04/19 20:20
 */

public class HeaderAppBean extends BaseItemBean implements Parcelable {
    public Bitmap icon;
    public String name;
    public Intent startIntent;

    public HeaderAppBean(Bitmap icon, String name, Intent startIntent) {
        setInnerData(icon, name, startIntent);
    }

    private void setInnerData(Bitmap icon, String name, Intent startIntent) {
        this.icon = icon;
        this.name = name;
        this.startIntent = startIntent;
    }

    public void updateContent(HeaderAppBean bean) {
        setInnerData(bean.icon, bean.name, bean.startIntent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        this.icon.writeToParcel(dest, flags);
        dest.writeString(this.name);
        startIntent.writeToParcel(dest, flags);
        dest.writeInt(this.type);
    }

    protected HeaderAppBean(Parcel in) {
        this.icon = Bitmap.CREATOR.createFromParcel(in);
        this.name = in.readString();
        this.startIntent = Intent.CREATOR.createFromParcel(in);
        this.type = in.readInt();
    }

    public static final Parcelable.Creator<HeaderAppBean> CREATOR = new Parcelable.Creator<HeaderAppBean>() {
        @Override
        public HeaderAppBean createFromParcel(Parcel source) {
            return new HeaderAppBean(source);
        }

        @Override
        public HeaderAppBean[] newArray(int size) {
            return new HeaderAppBean[size];
        }
    };
}
