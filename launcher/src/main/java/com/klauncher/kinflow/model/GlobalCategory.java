package com.klauncher.kinflow.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.klauncher.launcher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 16/8/24.
 */
public class GlobalCategory implements Parcelable {

    String  categoryIcon;
    String categoryName;

    public GlobalCategory(String categoryIcon, String categoryName) {
        this.categoryIcon = categoryIcon;
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "GlobalCategory{" +
                "categoryIcon='" + categoryIcon + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }

    public boolean allFieldsIsNotNull () {
        if (TextUtils.isEmpty(this.categoryIcon)||TextUtils.isEmpty(this.categoryName))
            return false;
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.categoryIcon);
        dest.writeString(this.categoryName);
    }

    protected GlobalCategory(Parcel in) {
        this.categoryIcon = in.readString();
        this.categoryName = in.readString();
    }

    public static final Creator<GlobalCategory> CREATOR = new Creator<GlobalCategory>() {
        @Override
        public GlobalCategory createFromParcel(Parcel source) {
            return new GlobalCategory(source);
        }

        @Override
        public GlobalCategory[] newArray(int size) {
            return new GlobalCategory[size];
        }
    };

    public static List<GlobalCategory> getDefaultGlobalCategoryList() {
        List<GlobalCategory> globalCategoryList = new ArrayList<>();
        for(int i = 0 ; i <5 ; i++) {
            String name = "导航";
            int resId = R.drawable.notification_tool_light_app;
            switch (i) {
                case 0://网址
                    name = "网址";
                    resId = R.drawable.notification_tool_light_app;//这里应该是网址
                    break;
                case 1://小说
                    name = "小说";
                    resId = R.drawable.notification_tool_light_novel;
                    break;
                case 2://视频
                    name = "视频";
                    resId = R.drawable.notification_tool_light_video;
                    break;
                case 3://奇趣
                    name = "奇趣";
                    resId = R.drawable.notification_tool_light_video;//这里应该是奇趣
                    break;
                case 4://应用
                    name = "应用";
                    resId = R.drawable.notification_tool_light_app;
                    break;

            }
            GlobalCategory globalCategory = new GlobalCategory(String.valueOf(resId),name);
            globalCategoryList.add(globalCategory);
        }
        return globalCategoryList;
    }
}

