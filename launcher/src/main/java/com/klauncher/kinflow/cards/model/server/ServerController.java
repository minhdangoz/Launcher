package com.klauncher.kinflow.cards.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import com.klauncher.kinflow.utilities.CollectionsUtils;
import com.klauncher.kinflow.utilities.KinflowLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 16/9/27.
 * 每一个item的控制器
 */
public class ServerController implements Parcelable {

    private List<NewsOpenControl> mNewsOpenControlList;
    private List<AdControl> mAdControlList;

    public List<NewsOpenControl> getNewsOpenControlList() {
        return mNewsOpenControlList;
    }

    public void setNewsOpenControlList(List<NewsOpenControl> newsOpenControlList) {
        mNewsOpenControlList = newsOpenControlList;
    }

    public List<AdControl> getAdControlList() {
        return mAdControlList;
    }

    public void setAdControlList(List<AdControl> adControlList) {
        mAdControlList = adControlList;
    }

    public boolean isNull () {
        if (CollectionsUtils.collectionIsNull(mNewsOpenControlList)
//                ||CollectionsUtils.collectionIsNull(mAdControlList)
                )
            return true;
        return false;
    }

    @Override
    public String toString() {
        return "ServerController{" +
                "mNewsOpenControlList=" + newsOpenControlToString() +
                ", mAdControlList=" + adOpenControlToString() +
                '}';
    }

     String newsOpenControlToString () {
        StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("\n");//先换行
         for (int i = 0; i < this.mNewsOpenControlList.size(); i++) {
             stringBuilder.append("第").append(String.valueOf(i)).append("个新闻控制器详情:  ").append(mNewsOpenControlList.get(i).toString()).append("\n");
         }
         return stringBuilder.toString();
    }

    String adOpenControlToString () {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");//先换行
        for (int i = 0; i < this.mAdControlList.size(); i++) {
            stringBuilder.append("第").append(String.valueOf(i)).append("个广告控制器详情:  ").append(mAdControlList.get(i).toString()).append("\n");
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

    public static final Parcelable.Creator<ServerController> CREATOR = new Parcelable.Creator<ServerController>() {
        @Override
        public ServerController createFromParcel(Parcel source) {
            return new ServerController(source);
        }

        @Override
        public ServerController[] newArray(int size) {
            return new ServerController[size];
        }
    };

    public ServerController (JSONObject jsonObject) {

        try {
            //解析新闻控制器列表
            List<NewsOpenControl> newsOpenControlList = new ArrayList<>();
            JSONArray newsOpenControlJsonArray = jsonObject.optJSONArray("news");
            int newsOpenControlLength = newsOpenControlJsonArray.length();
            if (newsOpenControlLength<=0) {
                KinflowLog.w("使用存储到本地的新闻接口控制器(可使用assets)");
            }else {
                for (int j = 0; j < newsOpenControlLength; j++) {
                    newsOpenControlList.add(new NewsOpenControl(newsOpenControlJsonArray.optJSONObject(j)));
                }
            }
            this.mNewsOpenControlList = newsOpenControlList;
            //解析广告控制器列表
            List<AdControl> adControlList = new ArrayList<>();
            JSONArray adControlJsonArray = jsonObject.optJSONArray("ads");
            int adControlLength = adControlJsonArray.length();
            if (adControlLength<=0) {
                KinflowLog.w("使用存储到本地的新闻接口控制器(可使用assets)");
            }else {
                for (int j = 0; j < adControlLength; j++) {
                    adControlList.add(new AdControl(adControlJsonArray.optJSONObject(j)));
                }
            }
            this.mAdControlList = adControlList;
        } catch (Exception e) {
            KinflowLog.e("服务端控制器数据解析时出现错误,详情: " + e.getMessage());
        }

    }
}
