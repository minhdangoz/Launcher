
package com.klauncher.kinflow.search.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.klauncher.kinflow.common.utils.Const;

/**
 * Created by xixionghui on 2016/3/15.
 */
public class HotWord implements Parcelable {
    private static final String defaultHotword1 = "人生若只如初见";
    private static final String defaultHotword2 = "又岂在朝朝暮暮";
    private static final String defaultHotword3 = "搜你所想";

    String word;
    String url;
    String id;

    private static HotWord defaultHotWord1 = new HotWord(String.valueOf(-1), defaultHotword1,
            Const.URL_SEARCH_WITH_BAIDU + defaultHotword1);
    private static HotWord defaultHotWord2 = new HotWord(String.valueOf(-2), defaultHotword2,
            Const.URL_SEARCH_WITH_BAIDU + defaultHotword2);
    private static HotWord hintHotWord = new HotWord(String.valueOf(-3), defaultHotword3, Const.URL_SEARCH_WITH_BAIDU + defaultHotword3);

    public static HotWord getDefaultHotWord1() {
        return defaultHotWord1;
    }

    public static HotWord getDefaultHotWord2() {
        return defaultHotWord2;
    }

    public static HotWord getHintHotWord() {
        return hintHotWord;
    }

    public HotWord(String id, String word, String url) {
        this.word = word;
        this.url = url;
        this.id = id;
    }

    public HotWord() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "HotWord{" +
                "word='" + word + '\'' +
                ", url='" + url + '\'' +
                ", id='" + id + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.word);
        dest.writeString(this.url);
        dest.writeString(this.id);
    }

    protected HotWord(Parcel in) {
        this.word = in.readString();
        this.url = in.readString();
        this.id = in.readString();
    }

    public static final Parcelable.Creator<HotWord> CREATOR = new Parcelable.Creator<HotWord>() {
        @Override
        public HotWord createFromParcel(Parcel source) {
            return new HotWord(source);
        }

        @Override
        public HotWord[] newArray(int size) {
            return new HotWord[size];
        }
    };
}
