package com.kapp.kinflow.business.beans;

import com.apkfuns.logutils.LogUtils;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.common.utils.FileUtils;
import com.kapp.knews.helper.content.ContentHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * description：网址导航卡片
 * <br>author：caowugao
 * <br>time： 2017/04/27 11:43
 */

public class SiteNavigationCardBean extends BaseInformationFlowBean {

    public List<SingleNaviBean> naviBeanList;
    private static final String LOCAL_NAV_LIST = "local_server_nav.json";

    public SiteNavigationCardBean(List<SingleNaviBean> naviBeanList) {
        this.naviBeanList = naviBeanList;
    }

    @Override
    public int getItemType() {
        return TYPE_SITE_NAVI;
    }

    public void updateCardBean(SiteNavigationCardBean cardBean) {
        this.naviBeanList = cardBean.naviBeanList;
    }

    public static class SingleNaviBean extends BaseItemBean {
        public String name;
        public String url;
        public boolean isHot;
        private static final String NAV_ID = "nd";
        private static final String NAV_NAME = "nn";
        private static final String NAV_ICON = "ni";
        private static final String NAV_URL = "nu";
        private static final String NAV_ORDER = "no";

        public SingleNaviBean(String name, String url, boolean isHot) {
            this.name = name;
            this.url = url;
            this.isHot = isHot;
        }

        public SingleNaviBean(JSONObject jsonObject) {

            //内容部分
            try {
                this.name = jsonObject.optString(NAV_NAME);
//            this.navIcon = jsonObject.optString(NAV_ICON);
                this.url = jsonObject.optString(NAV_URL);
//            this.navOrder = jsonObject.optInt(NAV_ORDER);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e("解析导航网址出错！！！");
            }
        }
    }

    public static List<SingleNaviBean> getDefaultNaviList() {
        List<SingleNaviBean> result = null;
        try {
            InputStream is = ContentHelper.getAssets().open(LOCAL_NAV_LIST);
            String string = FileUtils.inputStream2String(is);
            if (null == string) {
                throw new RuntimeException(LOCAL_NAV_LIST + " 解析出错!!!");
            }
            JSONObject jsonObject = new JSONObject(string);
            JSONArray wnavs = jsonObject.getJSONArray("wnavs");
            int length = wnavs.length();
            result = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                JSONObject data = wnavs.getJSONObject(i);
                String url = data.getString("nu");
//                String no = data.getString("no");
//                int order=Integer.parseInt(no);
                String name = data.getString("nn");
//                String iconName = data.getString("localIcon");
                SingleNaviBean singleNaviBean = new SingleNaviBean(name, url, true);
                result.add(singleNaviBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
