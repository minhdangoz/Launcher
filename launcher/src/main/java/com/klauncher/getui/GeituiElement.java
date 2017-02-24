package com.klauncher.getui;

import java.io.Serializable;

/**
 * 推送透传的数据格式 json解析
 * Created by wangqinghao on 2016/5/3.
 */
public class GeituiElement implements Serializable {
    //{"type":"1","title":"title","icon":"icon","pkgname":"pkgname","startActname":"startActname","url":"url","showStatistUrl":"showStatistUrl","clickStatistUrl":"clickStatistUrl"}
    private int type;
    private String title;
    private String content;
    private String icon;
    private String pkgname;
    private String startActname;
    private String url;
    private String showStatistUrl;
    private String clickStatistUrl;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPkgname() {
        return pkgname;
    }

    public void setPkgname(String pkgname) {
        this.pkgname = pkgname;
    }

    public String getStartActname() {
        return startActname;
    }

    public void setStartActname(String startActname) {
        this.startActname = startActname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getShowStatistUrl() {
        return showStatistUrl;
    }

    public void setShowStatistUrl(String showStatistUrl) {
        this.showStatistUrl = showStatistUrl;
    }

    public String getClickStatistUrl() {
        return clickStatistUrl;
    }

    public void setClickStatistUrl(String clickStatistUrl) {
        this.clickStatistUrl = clickStatistUrl;
    }
}
