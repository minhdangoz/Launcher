package com.klauncher.kinflow.cards.model.sougou;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Created by xixionghui on 16/9/26.
 */
public class SougouSearchArticle implements Parcelable {

    public static final String URL_SOUGOU_ARTICLE = "http://wxrd.appsearch.m.sogou.com:80/dist_sogou?mid=3fb7DeLongDeskTop";

    /**
     * 最多返回100条，支持指定起始位置（skip）取一定数量（limit）的文章
     * @param skip 起始位置
     * @param limit 指定数量的文章
     * @return
     */
    public static String getPostJsonBody(int skip,int limit) {
        String postJson = "{"
                + "\"ver\":\"4.9.0\","
                + "\"mid\":\"3fb7DeLongDeskTop\","
                + "\"source\":\"delongdesktop\","
                + "\"os\":\"android\","
                + "\"skip\":"+skip+","
                + "\"limit\":"+limit
                +"}";
        return "req="+ URLEncoder.encode(postJson);
    }

    //status=1标识正常返回,status=0标识出错
    public static final int RESPONSE_STATUS_SUCCESS = 1;
    public static final int RESPONSE_STATUS_ERROR = 0;


    private String link;//文章链接
    private String open_link;//文章链接
    private String title;//文章标题
    private String schema;//调起app接口
    private String durl;//搜狗搜索app下载中间页
    private String account;//文章来源
    private int read_num;//文章阅读数
    private long page_time;//文章发布时间
    private int type;//文章类型
    private String[] img_list;//文章内提取出来的图片地址

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getOpen_link() {
        return open_link;
    }

    public void setOpen_link(String open_link) {
        this.open_link = open_link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDurl() {
        return durl;
    }

    public void setDurl(String durl) {
        this.durl = durl;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getRead_num() {
        return read_num;
    }

    public void setRead_num(int read_num) {
        this.read_num = read_num;
    }

    public long getPage_time() {
        return page_time;
    }

    public void setPage_time(long page_time) {
        this.page_time = page_time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String[] getImg_list() {
        return img_list;
    }

    public void setImg_list(String[] img_list) {
        this.img_list = img_list;
    }

    @Override
    public String toString() {
        return "SougouSearchArticle{" +
                "link='" + link + '\'' +
                ", open_link='" + open_link + '\'' +
                ", title='" + title + '\'' +
                ", schema='" + schema + '\'' +
                ", durl='" + durl + '\'' +
                ", account='" + account + '\'' +
                ", read_num=" + read_num +
                ", page_time=" + page_time +
                ", type=" + type +
                ", img_list=" + Arrays.toString(img_list) +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.link);
        dest.writeString(this.open_link);
        dest.writeString(this.title);
        dest.writeString(this.schema);
        dest.writeString(this.durl);
        dest.writeString(this.account);
        dest.writeInt(this.read_num);
        dest.writeLong(this.page_time);
        dest.writeInt(this.type);
        dest.writeStringArray(this.img_list);
    }

    public SougouSearchArticle() {
    }

    protected SougouSearchArticle(Parcel in) {
        this.link = in.readString();
        this.open_link = in.readString();
        this.title = in.readString();
        this.schema = in.readString();
        this.durl = in.readString();
        this.account = in.readString();
        this.read_num = in.readInt();
        this.page_time = in.readLong();
        this.type = in.readInt();
        this.img_list = in.createStringArray();
    }

    public static final Creator<SougouSearchArticle> CREATOR = new Creator<SougouSearchArticle>() {
        @Override
        public SougouSearchArticle createFromParcel(Parcel source) {
            return new SougouSearchArticle(source);
        }

        @Override
        public SougouSearchArticle[] newArray(int size) {
            return new SougouSearchArticle[size];
        }
    };

    //=====

    /*
    private String link;
    private String open_link;
    private String title;
    private String schema;
    private String durl;
    private String account;
    private int read_num;
    private long page_time;
    private int type;
    private String[] img_list;
     */
    public SougouSearchArticle(JSONObject jsonObject) {

        try {
            this.link = jsonObject.optString("link");
            this.open_link = jsonObject.optString("open_link");
            this.title = jsonObject.optString("title");
            this.schema = jsonObject.optString("schema");
            this.durl = jsonObject.optString("durl");
            this.account = jsonObject.optString("account");
            this.read_num = jsonObject.optInt("read_num");
            this.page_time = jsonObject.optLong("page_time");
            this.type = jsonObject.optInt("type");
            //图片
            JSONArray imageListJsonArray = jsonObject.optJSONArray("img_list");
            int length = imageListJsonArray.length();
            this.img_list = new String[length];
            for (int i = 0; i < length; i++) {
                img_list[i] = imageListJsonArray.optString(i);
            }
        } catch (Exception e) {
            Log.w("kinflow2","在解析搜狗搜索新闻时出错: "+e.getMessage());
        }

    }
}
