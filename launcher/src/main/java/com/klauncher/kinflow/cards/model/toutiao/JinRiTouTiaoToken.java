package com.klauncher.kinflow.cards.model.toutiao;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by xixionghui on 16/6/14.
 * expires_in代表还剩下多少秒,token过期.
 */
public class JinRiTouTiaoToken implements Parcelable {
//    private String msg;
//    private int ret;

    private String access_token;
    private long expires_in;

    public JinRiTouTiaoToken(JSONObject jsonObject) {
        if (null == jsonObject) return;
        try {
            this.access_token = jsonObject.optString("access_token");
            this.expires_in = jsonObject.optLong("expires_in");
        } catch (Exception e) {
            Log.e("Kinflow", "JinRiTouTiaoToken: 解析JinRiTouTiaoToken时出错");
        }
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }

    @Override
    public String toString() {
        return "JinRiTouTiaoToken{" +
                "access_token='" + access_token + '\'' +
                ", expires_in=" + expires_in +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.access_token);
        dest.writeLong(this.expires_in);
    }

    protected JinRiTouTiaoToken(Parcel in) {
        this.access_token = in.readString();
        this.expires_in = in.readLong();
    }

    public static final Parcelable.Creator<JinRiTouTiaoToken> CREATOR = new Parcelable.Creator<JinRiTouTiaoToken>() {
        @Override
        public JinRiTouTiaoToken createFromParcel(Parcel source) {
            return new JinRiTouTiaoToken(source);
        }

        @Override
        public JinRiTouTiaoToken[] newArray(int size) {
            return new JinRiTouTiaoToken[size];
        }
    };
}
