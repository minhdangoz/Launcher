package com.klauncher.kinflow.config;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xixionghui on 16/5/12.
 */
public class Config implements Parcelable {

    private String app_active;
    private String kinfo;

    public String getApp_active() {
        return app_active;
    }

    public void setApp_active(String app_active) {
        this.app_active = app_active;
    }

    public String getKinfo() {
        return kinfo;
    }

    public void setKinfo(String kinfo) {
        this.kinfo = kinfo;
    }

    @Override
    public String toString() {
        return "Config{" +
                "app_active='" + app_active + '\'' +
                ", kinfo='" + kinfo + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.app_active);
        dest.writeString(this.kinfo);
    }

    public Config() {
    }

    public Config(String app_active, String kinfo) {
        this.app_active = app_active;
        this.kinfo = kinfo;
    }

    protected Config(Parcel in) {
        this.app_active = in.readString();
        this.kinfo = in.readString();
    }

    public static final Parcelable.Creator<Config> CREATOR = new Parcelable.Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel source) {
            return new Config(source);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };
}
