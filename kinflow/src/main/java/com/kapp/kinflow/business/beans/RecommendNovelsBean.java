package com.kapp.kinflow.business.beans;

import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * description：小说推荐bean
 * <br>author：caowugao
 * <br>time： 2017/05/19 17:13
 */

public class RecommendNovelsBean extends BaseItemBean {
    public String imageUrl;
    public String title;
    public String author;
    public String subtitle;
    public String clickUrl;

    public RecommendNovelsBean(String imageUrl, String title, String author, String subtitle, String clickUrl) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.subtitle = subtitle;
        this.clickUrl = clickUrl;
    }

    public RecommendNovelsBean(JSONObject jsonObject) {
        try {
            imageUrl = jsonObject.getString("book_pic_url");
            title = jsonObject.getString("resource_name");
            author = jsonObject.getString("author");
            subtitle = jsonObject.getString("incipit");
            clickUrl = jsonObject.getString("book_read_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
