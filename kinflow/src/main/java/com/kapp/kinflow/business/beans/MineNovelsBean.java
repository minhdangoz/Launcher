package com.kapp.kinflow.business.beans;

import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * description：我的小说
 * <br>author：caowugao
 * <br>time： 2017/05/19 17:42
 */

public class MineNovelsBean extends BaseItemBean {
    public String imageUrl;
    public String title;
    public boolean isEnd;//完结
    public int total;//供多少章
    public boolean isEdite;
    public String clickUrl;//跳转链接

    public MineNovelsBean(String imageUrl, String title, boolean isEnd, int total, boolean isEditt, String clickUrl) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.isEnd = isEnd;
        this.total = total;
        this.isEdite = isEditt;
        this.clickUrl = clickUrl;
    }

    public MineNovelsBean(JSONObject jsonObject) {
        try {
            imageUrl = jsonObject.getString("book_pic_url");
            title = jsonObject.getString("resource_name");
            clickUrl = jsonObject.getString("book_read_url");
            total = jsonObject.optInt("chapterNum");

            String status = jsonObject.getString("status");
            if ("连载".equals(status)) {
                isEnd = false;
            } else if ("全本".equals(status)) {
                isEnd = true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
