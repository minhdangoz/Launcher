package com.kapp.knews.repository.bean;

import com.apkfuns.logutils.LogUtils;
import com.kapp.knews.base.recycler.data.NewsBaseDataBean;
import com.kapp.knews.repository.utils.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by xixionghui on 2016/11/28.
 */

public class DongFangTouTiaoBean extends NewsBaseDataBean {

    private DongFangTouTiaoBean(String summaryTitle, String summaryPublishTime, List<String> summaryImageUrlList) {
        this.summaryTitle = summaryTitle;
        this.summaryPublishTime = summaryPublishTime;
        this.summaryImageUrlList = summaryImageUrlList;
    }
//    public String endkey;//当前返回新闻列表最后一条新闻的唯一标识符，下次请求时上传服务器端作为下次获取新闻列表的依据
//    public String newkey;//当前类别新闻的最新一条新闻的标识符，下次请求时上传服务器

    private long date;//新闻日期（时间戳）
    private boolean bigpic;//1：以大图形式展现；0：以小图形式展现
    private boolean hotnews;//1：是热点新闻；0：不是热点新闻
    private boolean isrecom;//1：是推荐新闻；0：不是推荐新闻
    private boolean isvideo;//1：是视频新闻；0：不是视频新闻

    private DongFangImageInfo[] lbimg;//大图模式数据。数组，默认只返回一张图
    private DongFangImageInfo[] miniimg;//缩略图数据模式，数组，可返回多张图片，数量由miniimg_size决定

    private int miniimg_size;//缩略图的数量
    private String source;//新闻来源
    private String type;//guonei ,当前新闻的类别
    private String topic;//新闻标题
    private String url;//新闻详情的url地址
    private String rowkey;//当前新闻的唯一标识符


    public DongFangTouTiaoBean(String jsonRoot) {

        try {

            JSONObject jsonDongFangTouTiaoBean = new JSONObject(jsonRoot);
            //时间
            this.date = jsonDongFangTouTiaoBean.optLong("date");
            //是否为大图
            this.bigpic = jsonDongFangTouTiaoBean.optInt("bigpic", 0) == 1 ? true : false;
            //是否为热点新闻
            this.hotnews = jsonDongFangTouTiaoBean.optInt("hotnews", 0) == 1 ? true : false;
            //是否为推荐新闻
            this.hotnews = jsonDongFangTouTiaoBean.optInt("isrecom", 0) == 1 ? true : false;
            //是否为视频新闻
            this.hotnews = jsonDongFangTouTiaoBean.optInt("isvideo", 0) == 1 ? true : false;
            //缩略图的数量
            this.miniimg_size = jsonDongFangTouTiaoBean.optInt("miniimg_size", 0);
            //新闻来源
            this.source = jsonDongFangTouTiaoBean.optString("source");
            //当前新闻的类别
            this.type = jsonDongFangTouTiaoBean.optString("type");
            //当前新闻的标题
            this.topic = jsonDongFangTouTiaoBean.optString("topic");
            //当前新闻详情的url地址
            this.url = jsonDongFangTouTiaoBean.optString("url");
            //当前新闻的唯一标识符
            this.rowkey = jsonDongFangTouTiaoBean.optString("rowkey");
            //解析大图
            lbimg = new DongFangImageInfo[1];
            JSONArray lbimgArray = jsonDongFangTouTiaoBean.optJSONArray("lbimg");
            DongFangImageInfo lbimgInfo = new DongFangImageInfo(lbimgArray.optJSONObject(0));
            lbimg[0] = lbimgInfo;
            //解析缩略图
            JSONArray miniimgArray = jsonDongFangTouTiaoBean.optJSONArray("miniimg");
            miniimg = new DongFangImageInfo[this.miniimg_size];
            for (int i = 0; i < this.miniimg_size; i++) {
                DongFangImageInfo miniimgInfo = new DongFangImageInfo(miniimgArray.optJSONObject(i));
                miniimg[i] = miniimgInfo;
            }


            /**
             * 赋值基类信息
             */

            this.summaryPublishTime = DateUtils.getInstance().second2TimeOrDate(this.date);
            this.summaryTitle = this.topic;
            this.summaryImageUrlList = new ArrayList<>();
            if (bigpic) {//如果是大图
                this.summaryImageUrlList.add(this.lbimg[0].getSrc());
            } else {//缩略图
                for (int i = 0; i < this.miniimg_size; i++) {
                    this.summaryImageUrlList.add(this.miniimg[i].getSrc());
                }
            }

        } catch (Exception e) {
            LogUtils.e("解析东方头条数据时，出错："+e.getMessage());
        }

    }

    public DongFangTouTiaoBean(JSONObject jsonDongFangTouTiaoBean) {
        try {
            //时间
            this.date = jsonDongFangTouTiaoBean.optLong("date");
            //是否为大图
            this.bigpic = jsonDongFangTouTiaoBean.optInt("bigpic", 0) == 1 ? true : false;
            //是否为热点新闻
            this.hotnews = jsonDongFangTouTiaoBean.optInt("hotnews", 0) == 1 ? true : false;
            //是否为推荐新闻
            this.hotnews = jsonDongFangTouTiaoBean.optInt("isrecom", 0) == 1 ? true : false;
            //是否为视频新闻
            this.hotnews = jsonDongFangTouTiaoBean.optInt("isvideo", 0) == 1 ? true : false;
            //缩略图的数量
            this.miniimg_size = jsonDongFangTouTiaoBean.optInt("miniimg_size", 0);
            //新闻来源
            this.source = jsonDongFangTouTiaoBean.optString("source");
            //当前新闻的类别
            this.type = jsonDongFangTouTiaoBean.optString("type");
            //当前新闻的标题
            this.topic = jsonDongFangTouTiaoBean.optString("topic");
            //当前新闻详情的url地址
            this.url = jsonDongFangTouTiaoBean.optString("url");
            //当前新闻的唯一标识符
            this.rowkey = jsonDongFangTouTiaoBean.optString("rowkey");
            //解析大图
            lbimg = new DongFangImageInfo[1];
            JSONArray lbimgArray = jsonDongFangTouTiaoBean.optJSONArray("lbimg");
            DongFangImageInfo lbimgInfo = new DongFangImageInfo(lbimgArray.optJSONObject(0));
            lbimg[0] = lbimgInfo;
            //解析缩略图
            JSONArray miniimgArray = jsonDongFangTouTiaoBean.optJSONArray("miniimg");
            miniimg = new DongFangImageInfo[this.miniimg_size];
            for (int i = 0; i < this.miniimg_size; i++) {
                DongFangImageInfo miniimgInfo = new DongFangImageInfo(miniimgArray.optJSONObject(i));
                miniimg[i] = miniimgInfo;
            }


            /**
             * 赋值基类信息
             */

            this.summaryPublishTime = DateUtils.getInstance().second2TimeOrDate(this.date);
            this.summaryTitle = this.topic;
            this.summaryImageUrlList = new ArrayList<>();
            if (bigpic) {//如果是大图
                this.summaryImageUrlList.add(this.lbimg[0].getSrc());
            } else {//缩略图
                for (int i = 0; i < this.miniimg_size; i++) {
                    this.summaryImageUrlList.add(this.miniimg[i].getSrc());
                }
            }

        } catch (Exception e) {
            LogUtils.e("解析东方头条数据时，出错："+e.getMessage());
        }
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isBigpic() {
        return bigpic;
    }

    public void setBigpic(boolean bigpic) {
        this.bigpic = bigpic;
    }

    public boolean isHotnews() {
        return hotnews;
    }

    public void setHotnews(boolean hotnews) {
        this.hotnews = hotnews;
    }

    public boolean isrecom() {
        return isrecom;
    }

    public void setIsrecom(boolean isrecom) {
        this.isrecom = isrecom;
    }

    public boolean isvideo() {
        return isvideo;
    }

    public void setIsvideo(boolean isvideo) {
        this.isvideo = isvideo;
    }

    public DongFangImageInfo[] getLbimg() {
        return lbimg;
    }

    public void setLbimg(DongFangImageInfo[] lbimg) {
        this.lbimg = lbimg;
    }

    public DongFangImageInfo[] getMiniimg() {
        return miniimg;
    }

    public void setMiniimg(DongFangImageInfo[] miniimg) {
        this.miniimg = miniimg;
    }

    public int getMiniimg_size() {
        return miniimg_size;
    }

    public void setMiniimg_size(int miniimg_size) {
        this.miniimg_size = miniimg_size;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    /**
     * 大图默认只有一张
     * 小图多张
     */
    class DongFangImageInfo {
        int imgheight;
        int imgwidth;
        String src;

        @Override
        public String toString() {
            return "DongFangImageInfo{" +
                    "imgheight=" + imgheight +
                    ", imgwidth=" + imgwidth +
                    ", src='" + src + '\'' +
                    '}';
        }

        public DongFangImageInfo(int imgheight, int imgwidth, String src) {
            this.imgheight = imgheight;
            this.imgwidth = imgwidth;
            this.src = src;
        }

        public DongFangImageInfo(JSONObject jsonDongFangImageInfo) {
            try {
                this.imgheight = jsonDongFangImageInfo.optInt("imgheight", 0);
                this.imgwidth = jsonDongFangImageInfo.optInt("imgwidth", 0);
                this.src = jsonDongFangImageInfo.optString("src");

            } catch (Exception e) {
                LogUtils.e("在解析东方头条的图片时，出错：" + e.getMessage());
            }

        }

        public int getImgheight() {
            return imgheight;
        }

        public void setImgheight(int imgheight) {
            this.imgheight = imgheight;
        }

        public int getImgwidth() {
            return imgwidth;
        }

        public void setImgwidth(int imgwidth) {
            this.imgwidth = imgwidth;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }
    }

    @Override
    public String toString() {
        return "DongFangTouTiaoBean{" +
                "date=" + date +
                ", bigpic=" + bigpic +
                ", hotnews=" + hotnews +
                ", isrecom=" + isrecom +
                ", isvideo=" + isvideo +
                ", lbimg=" + Arrays.toString(lbimg) +
                ", miniimg=" + Arrays.toString(miniimg) +
                ", miniimg_size=" + miniimg_size +
                ", source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", topic='" + topic + '\'' +
                ", url='" + url + '\'' +
                ", rowkey='" + rowkey + '\'' +
                '}';
    }
}
