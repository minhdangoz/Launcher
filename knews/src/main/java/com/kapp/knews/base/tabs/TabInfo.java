package com.kapp.knews.base.tabs;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 作者:  android001
 * 创建时间:   16/11/3  下午2:08
 * 版本号:
 * 功能描述:
 * 名称,类型,id
 */
public class TabInfo implements Parcelable {

    public static final int TYPE_COMMON = 0x0001;
    public static final int TYPE_REFRESH =0x0002 ;

    //1. tab类型----决定不同的Fragment
    int  tabChannelType;

    //2. tab名称
    String tabChannelName;
    String tabCategory;//内容提供商，提供的类别，例如：toutiao,shehui

    //3. tab的id
    int  tabChannelId;

    //4. tab是否被选中
    boolean tabChannelIsSelected;

    public TabInfo(int tabChannelType, String tabChannelName, String tabCategory, int tabChannelId, boolean tabChannelIsSelected) {
        this.tabChannelType = tabChannelType;
        this.tabChannelName = tabChannelName;
        this.tabCategory = tabCategory;
        this.tabChannelId = tabChannelId;
        this.tabChannelIsSelected = tabChannelIsSelected;
    }

    public TabInfo(String tabChannelName, String tabCategory, int tabChannelId) {
        this.tabChannelName = tabChannelName;
        this.tabCategory = tabCategory;
        this.tabChannelId = tabChannelId;
    }

    public int getTabChannelType() {
        return tabChannelType;
    }

    public void setTabChannelType(int tabChannelType) {
        this.tabChannelType = tabChannelType;
    }

    public String getTabChannelName() {
        return tabChannelName;
    }

    public void setTabChannelName(String tabChannelName) {
        this.tabChannelName = tabChannelName;
    }

    public String getTabCategory() {
        return tabCategory;
    }

    public void setTabCategory(String tabCategory) {
        this.tabCategory = tabCategory;
    }

    public int getTabChannelId() {
        return tabChannelId;
    }

    public void setTabChannelId(int tabChannelId) {
        this.tabChannelId = tabChannelId;
    }

    public boolean isTabChannelIsSelected() {
        return tabChannelIsSelected;
    }

    public void setTabChannelIsSelected(boolean tabChannelIsSelected) {
        this.tabChannelIsSelected = tabChannelIsSelected;
    }

    @Override
    public String toString() {
        return "TabInfo{" +
                "tabChannelType=" + tabChannelType +
                ", tabChannelName='" + tabChannelName + '\'' +
                ", tabCategory='" + tabCategory + '\'' +
                ", tabChannelId=" + tabChannelId +
                ", tabChannelIsSelected=" + tabChannelIsSelected +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.tabChannelType);
        dest.writeString(this.tabChannelName);
        dest.writeString(this.tabCategory);
        dest.writeInt(this.tabChannelId);
        dest.writeByte(this.tabChannelIsSelected ? (byte) 1 : (byte) 0);
    }

    protected TabInfo(Parcel in) {
        this.tabChannelType = in.readInt();
        this.tabChannelName = in.readString();
        this.tabCategory = in.readString();
        this.tabChannelId = in.readInt();
        this.tabChannelIsSelected = in.readByte() != 0;
    }

    public static final Creator<TabInfo> CREATOR = new Creator<TabInfo>() {
        @Override
        public TabInfo createFromParcel(Parcel source) {
            return new TabInfo(source);
        }

        @Override
        public TabInfo[] newArray(int size) {
            return new TabInfo[size];
        }
    };

//    public static List<TabInfo> createDefaultTabInfo (){
//        List<TabInfo> mTabInfoList = new ArrayList<>();
//        int length = ValuesHelper.getStringArrays(R.array.default_tab_channel).length;
//        String[] channelNames = ValuesHelper.getStringArrays(R.array.default_tab_channel);
//        String[] channelCategory = ValuesHelper.getStringArrays(R.array.default_tab_category);
//        for (int i = 0; i < length; i++) {
//            mTabInfoList.add(new TabInfo(channelNames[i],channelCategory[i],i));
//        }
//        return  mTabInfoList;
//    }
}
