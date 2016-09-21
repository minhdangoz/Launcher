package com.klauncher.kinflow.cards.model.toutiao;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.klauncher.kinflow.common.utils.DateUtils;
import com.klauncher.kinflow.utilities.CollectionsUtils;
import com.klauncher.kinflow.views.recyclerView.data.BaseRecyclerViewAdapterData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by xixionghui on 16/6/13.
 */
public class JinRiTouTiaoArticle extends com.klauncher.kinflow.views.recyclerView.data.BaseRecyclerViewAdapterData implements Parcelable {

    public static final String TAG = "Kinflow";

    private long group_id;//新闻的 id
    private String title;//新闻的标题
//    private String site;//新闻的来源:site不需要了，用source字段
    private String source;//新闻的来源:site不需要了，用source字段
    private String url;//新闻的来源的网址
    private long behot_time;//新闻展示时间:第二版出现,替代publish_time和display_time
    private long publish_time;//新闻的发表时间:时间戳
    private long display_time;//新闻展示时间:时间戳
    private String abstractArticle;//新闻的简介
    private String article_url;//新闻的头条网址
    private int tip;//新闻的热、荐的状态:0, 无额外信息. 默认 ;1 代表 热  ; 10 代表 推荐  ; 11 代表推荐 + 热 (1|2)
    private int digg_count;//顶的数量
    private int bury_count;//踩的数量
    private int comment_count;//评论的数量

    private String share_url;//新闻的分享链接:第二版出现
    private String label;//细粒度的文章标签:如置顶,图片,视频,推广等。由于 tip 和 label 都可以控制标签显示,所以二者有优先 级,label 如果有值则使用 label,如果没有 则去判断 tip 字段

    private ImageInfo middle_image;//右图的信息
    private List<ImageInfo> large_image_list;//大图列表的信息
    private List<ImageInfo> image_list;//三图列表的信息

    @Override
    public String toString() {
        return "JinRiTouTiaoArticle{" +
                "group_id=" + group_id +
                ", title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", url='" + url + '\'' +
                ", behot_time='" + behot_time + '\'' +
                ", publish_time=" + publish_time +
                ", display_time=" + display_time +
                ", abstractArticle='" + abstractArticle + '\'' +
                ", article_url='" + article_url + '\'' +
                ", share_url='" + share_url + '\'' +
                ", tip=" + tip +
                ", label=" + label +
                ", digg_count=" + digg_count +
                ", bury_count=" + bury_count +
                ", comment_count=" + comment_count +
                ", middle_image=" + middle_image +
                ", large_image_list=" + large_image_list +
                ", image_list=" + image_list +
                '}';
    }

    /**
     * 第一版
     * @param articleJsonObject
     */
//    public JinRiTouTiaoArticle(JSONObject articleJsonObject) {
//        Log.e(TAG, "获取到的今日头条JSON对象:\n"+articleJsonObject.toString());
//        try {
//            this.group_id = articleJsonObject.optLong("group_id");//新闻的 id
//            this.title = articleJsonObject.optString("title");//新闻的标题
//            this.source = articleJsonObject.optString("source");//新闻的来源
//            this.url = articleJsonObject.optString("url");//新闻的来源的网址
//            this.publish_time = articleJsonObject.optLong("publish_time");//新闻的发表时间:时间戳
//            this.display_time = articleJsonObject.optLong("display_time");//新闻展示时间:时间戳
//            this.abstractArticle = articleJsonObject.optString("abstract");//新闻的简介
//            this.article_url = articleJsonObject.optString("article_url");//新闻的头条网址
//            this.tip = articleJsonObject.optInt("tip");//新闻的热、荐的状态:0, 无额外信息. 默认 ;1 代表 热  ; 10 代表 推荐  ; 11 代表推荐 + 热 (1|2)
//            this.digg_count = articleJsonObject.optInt("digg_count");//顶的数量
//            this.bury_count = articleJsonObject.optInt("bury_count");//踩的数量
//            this.comment_count = articleJsonObject.optInt("comment_count");//评论的数量
//            //1,右图信息
//            if (articleJsonObject.isNull("middle_image")) {//图片信息为空
//                Log.e(TAG, "parseJinRiTouTiao: 右图信息为空");
//            } else {//对JinRiTouTiaoArticle.ImageInfo解析,middleImage全部替换为image
//                this.middle_image = new ImageInfo(articleJsonObject.optJSONObject("middle_image"));
//            }
//            //2,三图列表的信息
//            List<ImageInfo> imageInfos_three = new ArrayList<>();
//            if (articleJsonObject.isNull("image_list")) {
//                Log.e(TAG, "parseJinRiTouTiao: 三图列表的信息为空");
//            } else {
//                JSONArray imageListJSONArray = articleJsonObject.optJSONArray("image_list");
//                if (null == imageListJSONArray || imageListJSONArray.length() == 0) {
//                    Log.e(TAG, "parseJinRiTouTiao: 三图列表的信息为空");
//                } else {
//                    int imageListJSONArrayLength = imageListJSONArray.length();
//                    for (int i = 0; i < imageListJSONArrayLength; i++) {
//                        imageInfos_three.add(new ImageInfo(imageListJSONArray.optJSONObject(i)));
//                    }
//                }
//            }
//            this.image_list = imageInfos_three;
//            //3,大图列表的信息
//            List<ImageInfo> imageInfos_large = new ArrayList<>();
//            if (articleJsonObject.isNull("large_image_list")) {
//                Log.e(TAG, "parseJinRiTouTiao: 大图列表的信息为空");
//            } else {
//                JSONArray largeImageJSONArray = articleJsonObject.optJSONArray("large_image_list");
//                if (null == largeImageJSONArray || largeImageJSONArray.length() == 0) {
//                    Log.e(TAG, "parseJinRiTouTiao: 大图列表的信息为空");
//                } else {
//                    int largeImageLstJSONArrayLength = largeImageJSONArray.length();
//                    for (int j = 0; j < largeImageLstJSONArrayLength; j++) {
//                        imageInfos_large.add(new ImageInfo(largeImageJSONArray.optJSONObject(j)));
//                    }
//                }
//            }
//            this.large_image_list = imageInfos_large;
//        } catch (Exception e) {
//            Log.e(TAG, "JinRiTouTiaoArticle: 解析JinRiTouTiaoArticle时,出错" + e.getMessage());
//        }
//    }

    /**
     * 第二版
     * @param articleJsonObject
     */
    public JinRiTouTiaoArticle(JSONObject articleJsonObject) {
        Log.e(TAG, "获取到的今日头条JSON对象:\n"+articleJsonObject.toString());
        try {
            this.group_id = articleJsonObject.optLong("group_id");//新闻的 id
            this.title = articleJsonObject.optString("title");//新闻的标题
            this.source = articleJsonObject.optString("source");//新闻的来源
            this.article_url = articleJsonObject.optString("article_url");//新闻的头条网址
            this.url = articleJsonObject.optString("url");//新闻的来源的网址
            this.behot_time = articleJsonObject.optLong("behot_time");//新闻展示时间:第二版出现,替代publish_time和display_time
            if (articleJsonObject.optLong("publish_time")==0) {
                this.publish_time = this.behot_time;//如果没有publish_time这个字段,则使用
            } else {
                this.publish_time = articleJsonObject.optLong("publish_time");//新闻的发表时间:时间戳
            }
            this.display_time = articleJsonObject.optLong("display_time");//新闻展示时间aa:时间戳
            this.abstractArticle = articleJsonObject.optString("abstract");//新闻的简介
            this.share_url = articleJsonObject.optString("share_url");//新闻的分享链接
            this.tip = articleJsonObject.optInt("tip");//新闻的热、荐的状态:0, 无额外信息. 默认 ;1 代表 热  ; 10 代表 推荐  ; 11 代表推荐 + 热 (1|2)
            this.label = articleJsonObject.optString("label");//细粒度的文章标签:label 都可以控制标签显示,所以二者有优先 级,label 如果有值则使用 label,如果没有 则去判断 tip 字段
            this.digg_count = articleJsonObject.optInt("digg_count");//顶的数量
            this.bury_count = articleJsonObject.optInt("bury_count");//踩的数量
            this.comment_count = articleJsonObject.optInt("comment_count");//评论的数量
            //1,右图信息
            if (articleJsonObject.isNull("middle_image")) {//图片信息为空
                Log.e(TAG, "parseJinRiTouTiao: 右图信息为空");
            } else {//对JinRiTouTiaoArticle.ImageInfo解析,middleImage全部替换为image
                this.middle_image = new ImageInfo(articleJsonObject.optJSONObject("middle_image"));
            }
            //2,三图列表的信息
            List<ImageInfo> imageInfos_three = new ArrayList<>();
            if (articleJsonObject.isNull("image_list")) {
                Log.e(TAG, "parseJinRiTouTiao: 三图列表的信息为空");
            } else {
                JSONArray imageListJSONArray = articleJsonObject.optJSONArray("image_list");
                if (null == imageListJSONArray || imageListJSONArray.length() == 0) {
                    Log.e(TAG, "parseJinRiTouTiao: 三图列表的信息为空");
                } else {
                    int imageListJSONArrayLength = imageListJSONArray.length();
                    for (int i = 0; i < imageListJSONArrayLength; i++) {
                        imageInfos_three.add(new ImageInfo(imageListJSONArray.optJSONObject(i)));
                    }
                }
            }
            this.image_list = imageInfos_three;
            //3,大图列表的信息
            List<ImageInfo> imageInfos_large = new ArrayList<>();
            if (articleJsonObject.isNull("large_image_list")) {
                Log.e(TAG, "parseJinRiTouTiao: 大图列表的信息为空");
            } else {
                JSONArray largeImageJSONArray = articleJsonObject.optJSONArray("large_image_list");
                if (null == largeImageJSONArray || largeImageJSONArray.length() == 0) {
                    Log.e(TAG, "parseJinRiTouTiao: 大图列表的信息为空");
                } else {
                    int largeImageLstJSONArrayLength = largeImageJSONArray.length();
                    for (int j = 0; j < largeImageLstJSONArrayLength; j++) {
                        imageInfos_large.add(new ImageInfo(largeImageJSONArray.optJSONObject(j)));
                    }
                }
            }
            this.large_image_list = imageInfos_large;
            //设置类型
            setType(BaseRecyclerViewAdapterData.TYPE_NEWS);
        } catch (Exception e) {
            Log.e(TAG, "JinRiTouTiaoArticle: 解析JinRiTouTiaoArticle时,出错" + e.getMessage());
        }
    }

    public long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(long group_id) {
        this.group_id = group_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getBehot_time() {
        return behot_time;
    }

    public void setBehot_time(long behot_time) {
        this.behot_time = behot_time;
    }

    public long getPublish_time() {
        long publishTime = this.publish_time;
        if (publishTime==0) publishTime = this.display_time;
        if (publishTime==0) publishTime = this.behot_time;
        if (publishTime==0) publishTime= DateUtils.getInstance().calendar2Seconds(Calendar.getInstance());
        return publishTime;
    }

    public void setPublish_time(long publish_time) {
        this.publish_time = publish_time;
    }

    public long getDisplay_time() {
        return display_time;
    }

    public void setDisplay_time(long display_time) {
        this.display_time = display_time;
    }

    public String getAbstractArticle() {
        return abstractArticle;
    }

    public void setAbstractArticle(String abstractArticle) {
        this.abstractArticle = abstractArticle;
    }

    public String getArticle_url() {
        return article_url;
    }

    public void setArticle_url(String article_url) {
        this.article_url = article_url;
    }

    public String getShare_url() {
        return share_url;
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public int getTip() {
        return tip;
    }

    public void setTip(int tip) {
        this.tip = tip;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getDigg_count() {
        return digg_count;
    }

    public void setDigg_count(int digg_count) {
        this.digg_count = digg_count;
    }

    public int getBury_count() {
        return bury_count;
    }

    public void setBury_count(int bury_count) {
        this.bury_count = bury_count;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }

    public ImageInfo getMiddle_image() {
        return middle_image;
    }

    public void setMiddle_image(ImageInfo middle_image) {
        this.middle_image = middle_image;
    }

    public List<ImageInfo> getLarge_image_list() {
        return large_image_list;
    }

    public void setLarge_image_list(List<ImageInfo> large_image_list) {
        this.large_image_list = large_image_list;
    }

    public List<ImageInfo> getImage_list() {
        return image_list;
    }

    public void setImage_list(List<ImageInfo> image_list) {
        this.image_list = image_list;
    }

    public static final int IMAGE_TYPE_0_IMAGE = 0;
    public static final int IMAGE_TYPE_1_IMAGE = 1;
    public static final int IMAGE_TYPE_3_IMAGE = 3;
    public static final int IMAGE_TYPE_LARGE_IMAGE = 10;

    //图片展示的优 先 级 顺 序 为 large_image_list> image_list> middle_image>无图
    public int getImageType () {
        if (!CollectionsUtils.collectionIsNull(this.large_image_list)) return IMAGE_TYPE_LARGE_IMAGE;
        if (!CollectionsUtils.collectionIsNull(this.image_list)) return IMAGE_TYPE_3_IMAGE;
        if (null!=this.middle_image) return IMAGE_TYPE_1_IMAGE;
        return IMAGE_TYPE_0_IMAGE;
    }

    public static Creator<JinRiTouTiaoArticle> getCREATOR() {
        return CREATOR;
    }

    public JinRiTouTiaoArticle() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.group_id);
        dest.writeString(this.title);
        dest.writeString(this.source);
        dest.writeString(this.url);
        dest.writeLong(this.behot_time);
        dest.writeLong(this.publish_time);
        dest.writeLong(this.display_time);
        dest.writeString(this.abstractArticle);
        dest.writeString(this.article_url);
        dest.writeString(this.share_url);
        dest.writeInt(this.tip);
        dest.writeString(this.label);
        dest.writeInt(this.digg_count);
        dest.writeInt(this.bury_count);
        dest.writeInt(this.comment_count);
        dest.writeParcelable(this.middle_image, flags);
        dest.writeTypedList(this.large_image_list);
        dest.writeTypedList(this.image_list);
    }

    protected JinRiTouTiaoArticle(Parcel in) {
        this.group_id = in.readLong();
        this.title = in.readString();
        this.source = in.readString();
        this.url = in.readString();
        this.behot_time = in.readLong();
        this.publish_time = in.readLong();
        this.display_time = in.readLong();
        this.abstractArticle = in.readString();
        this.article_url = in.readString();
        this.share_url = in.readString();
        this.tip = in.readInt();
        this.label = in.readString();
        this.digg_count = in.readInt();
        this.bury_count = in.readInt();
        this.comment_count = in.readInt();
        this.middle_image = in.readParcelable(ImageInfo.class.getClassLoader());
        this.large_image_list = in.createTypedArrayList(ImageInfo.CREATOR);
        this.image_list = in.createTypedArrayList(ImageInfo.CREATOR);
    }

    public static final Creator<JinRiTouTiaoArticle> CREATOR = new Creator<JinRiTouTiaoArticle>() {
        @Override
        public JinRiTouTiaoArticle createFromParcel(Parcel source) {
            return new JinRiTouTiaoArticle(source);
        }

        @Override
        public JinRiTouTiaoArticle[] newArray(int size) {
            return new JinRiTouTiaoArticle[size];
        }
    };

    //inner class ------------------------------------------------------------------------------------------

    /**
     * 图片的信息
     */
    public static class ImageInfo implements Parcelable {
        String url;
        String uri;
        int width;
        int height;
        List<ImageUrl> url_list;//相同的图片,相同的尺寸,可能会返回多个url.今日头条解释:防止cdn有问题，会有重试策略.我们在使用时,任取其一即可.

        public ImageInfo() {
        }

        public ImageInfo(String url, String uri, int width, int height, List<ImageUrl> url_list) {
            this.url = url;
            this.uri = uri;
            this.width = width;
            this.height = height;
            this.url_list = url_list;
        }

        public ImageInfo(JSONObject image_JsonObject) {
            try {
                this.url = image_JsonObject.optString("url");
                this.uri = image_JsonObject.optString("uri");
                this.width = image_JsonObject.optInt("width");
                this.height = image_JsonObject.optInt("height");

                //开始解析图片中urlList地址信息ImageUrl
                List<JinRiTouTiaoArticle.ImageUrl> imageUrlList = new ArrayList<>();
                JSONArray image_urlListJsonArray = image_JsonObject.optJSONArray("url_list");
                if (null == image_urlListJsonArray || image_urlListJsonArray.length() == 0) {
                    Log.e(TAG, "parseJinRiTouTiao: 解析图片信息时,图片urlList中数据为空");
                } else {//图片urlList中数据非空,开始循环遍历
                    int image_urlList_length = image_urlListJsonArray.length();
                    for (int j = 0; j < image_urlList_length; j++) {
                        if (image_urlListJsonArray.isNull(j)) {
                            //
                            Log.e(TAG, "parseJinRiTouTiao: 图片信息ImageInfo中,的图片urlList列表中,第" + j + "个url为空");
                        } else {
                            imageUrlList.add(new ImageUrl(image_urlListJsonArray.optJSONObject(j)));
                        }
                    }
                }
                this.url_list = imageUrlList;
            } catch (Exception e) {
                Log.e(TAG, "ImageInfo: 解析ImageInfo时出错" + e.getMessage());
            }
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public List<ImageUrl> getUrl_list() {
            return url_list;
        }

        public void setUrl_list(List<ImageUrl> url_list) {
            this.url_list = url_list;
        }

        @Override
        public String toString() {
            return "ImageInfo{" +
                    "url='" + url + '\'' +
                    ", uri='" + uri + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", url_list=" + url_list +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.url);
            dest.writeString(this.uri);
            dest.writeInt(this.width);
            dest.writeInt(this.height);
            dest.writeTypedList(this.url_list);
        }

        protected ImageInfo(Parcel in) {
            this.url = in.readString();
            this.uri = in.readString();
            this.width = in.readInt();
            this.height = in.readInt();
            this.url_list = in.createTypedArrayList(ImageUrl.CREATOR);
        }

        public static final Parcelable.Creator<ImageInfo> CREATOR = new Parcelable.Creator<ImageInfo>() {
            @Override
            public ImageInfo createFromParcel(Parcel source) {
                return new ImageInfo(source);
            }

            @Override
            public ImageInfo[] newArray(int size) {
                return new ImageInfo[size];
            }
        };
    }

    /**
     * 图片url对象
     */
    public static class ImageUrl implements Parcelable {
        String url;

        public ImageUrl(JSONObject imageUrlJsonObject) {
            try {
                if (null == imageUrlJsonObject) {
                    Log.e(TAG, "ImageUrl: 解析图片IamgeUrl的JSONObject为空");
//                url = "";
                } else {
                    url = imageUrlJsonObject.optString("url");
                    ;
                }
            } catch (Exception e) {
                Log.e(TAG, "ImageUrl: 解析ImageUrl时,出错:" + e.getMessage());
            }
        }

        @Override
        public String toString() {
            return "ImageUrl{" +
                    "url='" + url + '\'' +
                    '}';
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public ImageUrl(String url) {

            this.url = url;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.url);
        }

        protected ImageUrl(Parcel in) {
            this.url = in.readString();
        }

        public static final Parcelable.Creator<ImageUrl> CREATOR = new Parcelable.Creator<ImageUrl>() {
            @Override
            public ImageUrl createFromParcel(Parcel source) {
                return new ImageUrl(source);
            }

            @Override
            public ImageUrl[] newArray(int size) {
                return new ImageUrl[size];
            }
        };
    }

}
