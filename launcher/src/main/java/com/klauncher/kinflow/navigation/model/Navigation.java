package com.klauncher.kinflow.navigation.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.browser.KinflowBrower;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.launcher.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 2016/3/18.
 */
public class Navigation implements Parcelable, Comparable {


    public static final String rootJsonKey_CONTENT_NAVIGATION  = "cnavs";
    public static final String rootJsonKey_WEB_NAVIGATION  = "wnavs";

    public static final String NAV_ID = "nd";
    public static final String NAV_NAME = "nn";
    public static final String NAV_ICON = "ni";
    public static final String NAV_URL = "nu";
    public static final String NAV_ORDER = "no";
    public static final String NAV_OPEN_OPTIONS = "ops";

    private String navId;//导航id
    private String navName;//导航名称
    private String navIcon;//导航图片icon
    private String navUrl;//导航URL
    private int navOrder;//导航排序order（所在位置）
    private List<String> navOpenOptions;//导航打开方式

    public void open(Context context, Bundle extras) {
        //通过Bundle获取url&&uri
        String openUrl = extras.getString(OpenMode.OPEN_URL_KEY);
        String openUri = extras.getString(OpenMode.FIRST_OPEN_MODE_TYPE_URI);
        //获取OpenMode
        OpenMode openMode = new OpenMode(context, this.navOpenOptions, openUrl);
        Intent firtIntent = openMode.getFirstIntent();
        try {
            Uri uri = null;
            String openType = openMode.getCurrentFirstOpenMode();
            switch (openType) {
                case OpenMode.FIRST_OPEN_MODE_TYPE_URI:
                    uri = Uri.parse(openUri);
                    break;
                case OpenMode.FIRST_OPEN_MODE_TYPE_COMPONENT:
                    uri = Uri.parse(openUrl);
                    break;
                case OpenMode.FIRST_OPEN_MODE_TYPE_DEFAULT:
                    break;
            }
            firtIntent.setData(uri);
            context.startActivity(openMode.getFirstIntent());
        } catch (Exception e) {
            try {
                Intent secondIntent = openMode.getSecondIntent();
                if (null == secondIntent.getComponent())
                    throw new Exception("服务器返回的数据为空字符串或者没有按照指定格式返回数据");
                context.startActivity(openMode.getSecondIntent());
            } catch (Exception e1) {
                try {
                    Intent thridIntent = openMode.getThirdIntent();
                    context.startActivity(thridIntent);
                } catch (Exception e2) {//三种打开方式均失败情况
                    Intent myIntent = new Intent(mContext, KinflowBrower.class);
                    myIntent.setData(Uri.parse(openUrl));
                    context.startActivity(myIntent);
                }

            }
        }
    }

    public String openByOrder (Context context) {
        String finalOpenComponent = BuildConfig.APPLICATION_ID;
        String articleUrl = "http://m.hao123.com/?union=1&from=1012581h&tn=ops1012581h";
        String openUrl = TextUtils.isEmpty(getNavUrl())?articleUrl:getNavUrl();
        for (int i = 0; i < getNavOpenOptions().size(); i++) {
            try {
                KinflowLog.w("尝试第" + i + "打开方式");
                String openComponentName = getNavOpenOptions().get(i);
                Log.e("kinflow", "openByOrder: openComponentName" + openComponentName);
                    String[] cns = getNavOpenOptions().get(i).split("/");
                    ComponentName componentName = new ComponentName(cns[0],cns[1]);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setComponent(componentName);
                    browserIntent.setData(Uri.parse(openUrl));
                    context.startActivity(browserIntent);
                    finalOpenComponent = componentName.getPackageName();
                    return finalOpenComponent;
            } catch (Exception e) {
                KinflowLog.w("第" + i + "打开方式失败");
                //判断如果是最后一个了,用默认打开,就别再继续了
                if (i==getNavOpenOptions().size()-1) {
//                    finalOpenComponent = openWithInnerBrowser(context);
                    KinflowBrower.openUrl(context,openUrl);
                } else {
                    continue;
                }
            }

        }
        return finalOpenComponent;
    }

    public Navigation() {
//        "23", "com.baidu.browser.apps/com.baidu.browser.framework.BdBrowserActivity", "0"
    }

    public Navigation(String navId, String navName, String navIcon, String navUrl, int navOrder, List<String> navOpenOptions) {
        this.navId = navId;
        this.navName = navName;
        this.navIcon = navIcon;
        this.navUrl = navUrl;
        this.navOrder = navOrder;
        if (null == navOpenOptions || navOpenOptions.size() == 0) {
            List<String> ops = new ArrayList<>();
            ops.add("23");
            ops.add("com.baidu.browser.apps/com.baidu.browser.framework.BdBrowserActivity");
            ops.add("0");
            this.navOpenOptions = ops;
        }
        this.navOpenOptions = navOpenOptions;
    }

    public String getNavId() {
        return navId;
    }

    public void setNavId(String navId) {
        this.navId = navId;
    }

    public String getNavName() {
        return navName;
    }

    public void setNavName(String navName) {
        this.navName = navName;
    }

    public String getNavIcon() {
        return navIcon;
    }

    public void setNavIcon(String navIcon) {
        this.navIcon = navIcon;
    }

    public String getNavUrl() {
        return navUrl;
    }

    public void setNavUrl(String navUrl) {
        this.navUrl = navUrl;
    }

    public int getNavOrder() {
        return navOrder;
    }

    public void setNavOrder(int navOrder) {
        this.navOrder = navOrder;
    }

    public List<String> getNavOpenOptions() {
        return navOpenOptions;
    }

    public void setNavOpenOptions(List<String> navOpenOptions) {
        this.navOpenOptions = navOpenOptions;
    }

    public String getNavigationList() {
        StringBuilder stringBuilder = new StringBuilder("第"+getNavOrder()+"个导航的大开方式如下:\n");
        for (int i = 0; i < this.navOpenOptions.size(); i++) {
            stringBuilder.append(this.navOpenOptions.get(i)).append(",");
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "Navigation{" +
                "navId='" + navId + '\'' +
                ", navName='" + navName + '\'' +
                ", navIcon='" + navIcon + '\'' +
                ", navUrl='" + navUrl + '\'' +
                ", navOrder=" + navOrder +
                ", navOpenOptions=" + navOpenOptions +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.navId);
        dest.writeString(this.navName);
        dest.writeString(this.navIcon);
        dest.writeString(this.navUrl);
        dest.writeInt(this.navOrder);
        dest.writeStringList(this.navOpenOptions);
    }

    protected Navigation(Parcel in) {
        this.navId = in.readString();
        this.navName = in.readString();
        this.navIcon = in.readString();
        this.navUrl = in.readString();
        this.navOrder = in.readInt();
        this.navOpenOptions = in.createStringArrayList();
    }

    public static final Creator<Navigation> CREATOR = new Creator<Navigation>() {
        @Override
        public Navigation createFromParcel(Parcel source) {
            return new Navigation(source);
        }

        @Override
        public Navigation[] newArray(int size) {
            return new Navigation[size];
        }
    };

    @Override
    public int compareTo(Object another) {
        Navigation anotherNavigation = (Navigation) another;
        if (this.navOrder < anotherNavigation.getNavOrder()) {
            return -1;
        } else if (this.getNavOrder() > anotherNavigation.getNavOrder()) {
            return 1;
        }
        return 0;
    }

    //以下为kinflow第二版========================================================
    private Context mContext;

    public Context getContext() {
        if (null != mContext)
            return mContext;
        return KLauncherApplication.mKLauncherApplication;
    }

    public Navigation(JSONObject navigationJsonObject) {

        try {
            //内容部分
            this.navId = navigationJsonObject.optString(Navigation.NAV_ID);
            this.navName = navigationJsonObject.optString(Navigation.NAV_NAME);
            this.navIcon = navigationJsonObject.optString(Navigation.NAV_ICON);
            this.navUrl = navigationJsonObject.optString(Navigation.NAV_URL);
            this.navOrder = navigationJsonObject.optInt(Navigation.NAV_ORDER);
            //控制部分--打开方式
            JSONArray opsJsonArray = navigationJsonObject.optJSONArray(NAV_OPEN_OPTIONS);
            List<String> opsList = new ArrayList<>();
            int opsJsonArrayLength = opsJsonArray.length();
            if (opsJsonArrayLength <= 0) {
                opsList.add("com.tencent.mtt/com.tencent.mtt.SplashActivity");
                opsList.add("com.baidu.browser.apps/com.baidu.browser.framework.BdBrowserActivity");
                opsList.add("com.klauncher.launcher/com.klauncher.kinflow.browser.KinflowBrower");
            } else {
                for (int j = 0; j < opsJsonArrayLength; j++) {
                    opsList.add(opsJsonArray.optString(j));
                }
            }
            this.navOpenOptions = opsList;
        } catch (Exception e) {
            KinflowLog.e("Navigation导航数据解析时出现错误,详情: " + e.getMessage());
        }

    }
}
