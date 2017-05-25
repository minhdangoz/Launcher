package com.kapp.knews.repository.server;

import android.os.Parcel;
import android.os.Parcelable;

import com.apkfuns.logutils.LogUtils;
import com.kapp.knews.helper.java.collection.CollectionsUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xixionghui on 16/9/27.
 * 实体类
 * 每一个item的控制器
 */
public class ServerController implements Parcelable {
    //
    private List<NewsOpenControl> mNewsOpenControlList;//新闻控制---已可忽略
    private List<AdControl> mAdControlList;//广告控制

    /**
     * 获取新闻控制---可忽略
     * @return
     */
    public List<NewsOpenControl> getNewsOpenControlList() {
        return mNewsOpenControlList;
    }


    /**
     * 设置新闻控制---可忽略
     * @param newsOpenControlList
     */
    public void setNewsOpenControlList(List<NewsOpenControl> newsOpenControlList) {
        mNewsOpenControlList = newsOpenControlList;
    }

    /**
     * 获取广告控制
     * @return
     */
    public List<AdControl> getAdControlList() {
        return mAdControlList;
    }

    /**
     * 设置广告控制
     * @param adControlList
     */
    public void setAdControlList(List<AdControl> adControlList) {
        mAdControlList = adControlList;
    }

    /**
     * 判断服务端控制是否为空，包含新闻控制和广告控制
     * @return
     */
    public boolean isNull () {
        if (
//                CollectionsUtils.collectionIsNull(mNewsOpenControlList)
//                ||
        CollectionsUtils.collectionIsNull(mAdControlList)
                )
            return true;
        return false;
    }

    /**
     * toString
     * @return
     */
    @Override
    public String toString() {
        return "ServerController{" +
                "mNewsOpenControlList=" + newsOpenControlToString() +
                ", mAdControlList=" + adOpenControlToString() +
                '}';
    }

     String newsOpenControlToString () {
        StringBuilder stringBuilder = new StringBuilder();
         if (CollectionsUtils.collectionIsNull(this.mNewsOpenControlList)) {
             stringBuilder.append("新闻控制数据为空");
         } else {
             stringBuilder.append("\n");//先换行
             for (int i = 0; i < this.mNewsOpenControlList.size(); i++) {
                 stringBuilder.append("第").append(String.valueOf(i)).append("个新闻控制器详情:  ").append(mNewsOpenControlList.get(i).toString()).append("\n");
             }
         }
         return stringBuilder.toString();
    }

    String adOpenControlToString () {
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionsUtils.collectionIsNull(this.mAdControlList)) {
            stringBuilder.append("广告控制数据为空");
        } else {
            stringBuilder.append("\n");//先换行
            for (int i = 0; i < this.mAdControlList.size(); i++) {
                stringBuilder.append("第").append(String.valueOf(i)).append("个广告控制器详情:  ").append(mAdControlList.get(i).toString()).append("\n");
            }
        }
        return stringBuilder.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mNewsOpenControlList);
        dest.writeTypedList(this.mAdControlList);
    }

    public ServerController() {
    }

    protected ServerController(Parcel in) {
        this.mNewsOpenControlList = in.createTypedArrayList(NewsOpenControl.CREATOR);
        this.mAdControlList = in.createTypedArrayList(AdControl.CREATOR);
    }

    public static final Creator<ServerController> CREATOR = new Creator<ServerController>() {
        @Override
        public ServerController createFromParcel(Parcel source) {
            return new ServerController(source);
        }

        @Override
        public ServerController[] newArray(int size) {
            return new ServerController[size];
        }
    };

    /**
     * 构造函数+解析
     * @param jsonObject
     */
    public ServerController(JSONObject jsonObject) {

        try {
            /*
            //解析新闻控制器列表
            LogUtils.e("开始解析新闻控制器");
            List<NewsOpenControl> newsOpenControlList = new ArrayList<>();
            JSONArray newsOpenControlJsonArray = jsonObject.optJSONArray("news");
            int newsOpenControlLength = newsOpenControlJsonArray.length();
            if (newsOpenControlLength<=0) {
                LogUtils.w("使用存储到本地的新闻接口控制器(可使用assets)");
                InputStream is = ContentHelper.getAssets().open("default_server_control");
                String json = FileUtils.loadStringFromStream(is);
                JSONObject localJsonRoot = new JSONObject(json);
                JSONArray newsOpenControlLocalJsonArray = localJsonRoot.optJSONArray("news");
                int newsOpenControlLocalJsonLength = newsOpenControlLocalJsonArray.length();
                for (int x = 0; x < newsOpenControlLocalJsonLength; x++) {
                    newsOpenControlList.add(new NewsOpenControl(newsOpenControlLocalJsonArray.optJSONObject(x)));
                }
            }else {
                for (int j = 0; j < newsOpenControlLength; j++) {
                    newsOpenControlList.add(new NewsOpenControl(newsOpenControlJsonArray.optJSONObject(j)));
                }
            }
            this.mNewsOpenControlList = newsOpenControlList;
            */

            //--------------------------------------------------------------------------------------

            //解析广告控制器列表

//            LogUtils.e("开始解析广告控制器");
            List<AdControl> adControlList = new ArrayList<>();
            JSONArray adControlJsonArray = jsonObject.optJSONArray("ads");
            int adControlLength = adControlJsonArray.length();
            if (adControlLength<=0) {
                LogUtils.w("使用存储到本地的新闻接口控制器(可使用assets)");
            }else {
                for (int j = 0; j < adControlLength; j++) {
                    adControlList.add(new AdControl(adControlJsonArray.optJSONObject(j)));
                }
            }
            this.mAdControlList = adControlList;
        } catch (Exception e) {
            LogUtils.e("服务端控制器数据解析时出现错误,详情: " + e.getMessage());//服务端控制器数据解析时出现错误,详情: Attempt to invoke virtual method 'int org.json.JSONArray.length()' on a null object reference
        }

    }
}
