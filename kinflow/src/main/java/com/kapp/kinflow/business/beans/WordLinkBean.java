package com.kapp.kinflow.business.beans;

/**
 * description：词和链接bean
 * <br>author：caowugao
 * <br>time： 2017/05/26 11:35
 */

public class WordLinkBean extends SingleTextBean {
    public String url;

    public WordLinkBean(String text) {
        super(text);
    }

    public WordLinkBean(String text, String url) {
        super(text);
        this.url = url;
    }
}
