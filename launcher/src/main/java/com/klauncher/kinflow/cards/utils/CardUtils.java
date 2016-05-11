package com.klauncher.kinflow.cards.utils;

import android.text.TextUtils;
import android.util.Log;

import com.klauncher.kinflow.cards.CardContentManager;
import com.klauncher.kinflow.cards.manager.CardContentManagerFactory;
import com.klauncher.kinflow.cards.model.yidian.YiDianModel;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.DateUtils;
import com.ss.android.sdk.minusscreen.model.Article;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xixionghui on 2016/3/28.
 */
public class CardUtils {

    public static boolean isContainsAbstract(Article article) {
        if (null == article) return false;
        if (!TextUtils.isEmpty(article.mAbstract.trim())) return true;
        return false;
    }

    /**
     * 获取带有abstract的Article集合
     * @param articleList
     * @return
     */
    public static List<Article> getAbstractArticleList(List<Article> articleList) {
        List<Article> articleContainsAbstractList = new ArrayList<>();
        for (Article article : articleList) {
            if (isContainsAbstract(article)) articleContainsAbstractList.add(article);
        }
        return articleContainsAbstractList;
    }

    /*
    *//**
     * 给获取到的Article分组
     *
     * @param articleListSrc
     * @return
     *//*
    public static HashMap<String, LinkedHashSet<Article>> groupByAbstract(List<Article> articleListSrc) {
        HashMap<String, LinkedHashSet<Article>> group = new HashMap<>();
        LinkedHashSet<Article> group1 = new LinkedHashSet<>();
        LinkedHashSet<Article> group2 = new LinkedHashSet<>();


        List<Article> abstractArticleList = getAbstractArticleList(articleListSrc);
        if (articleListSrc.size() >= 10 && null != abstractArticleList && abstractArticleList.size() >= 2) {//首轮获取到数据，足够两组
            group1.add(abstractArticleList.get(0));
            group2.add(abstractArticleList.get(1));
            //移除已经添加到分组的Article
            articleListSrc.remove(abstractArticleList.get(0));
            articleListSrc.remove(abstractArticleList.get(1));
            group1.addAll(articleListSrc.subList(0, 4));
            group2.addAll(articleListSrc.subList(4, 8));//报错了，因为size=6，而你要截取5-9所有出错
            group.put(CardContentManager.key_group1, group1);
            group.put(CardContentManager.key_group2, group2);
        } else {
            group1.add(abstractArticleList.get(0));
            articleListSrc.remove(abstractArticleList.get(0));
            if (articleListSrc.size() < 5) {//一组也分不够
                group1.addAll(articleListSrc);
            } else {//能分够一组
                group1.addAll(articleListSrc.subList(0, 4));
            }
            group.put(CardContentManager.key_group1, group1);
        }
        return group;
    }
    */


    public static List<Article>[] groupByAbstract2(List<Article> articleListSrc) {
        List<Article>[] articleListArrays = new List[2];
        List<Article> group1 = new ArrayList<>();
        List<Article> group2 = new ArrayList<>();

        List<Article> abstractArticleList = getAbstractArticleList(articleListSrc);
        int abstractArticleListSize = abstractArticleList.size();
        if (articleListSrc.size() < 10) {//接收到的Article不足10条：可能是1组，也可能是0组
            if (abstractArticleListSize>1) {//有一组
                Article firstArticle = abstractArticleList.get(0);
                group1.add(firstArticle);
                articleListSrc.remove(firstArticle);
                if (articleListSrc.size() < 5) {
                    group1.addAll(articleListSrc);
                } else {
                    group1.addAll(articleListSrc.subList(0, 5));
                }
//                group.put(CardContentManager.key_group1, group1);
                articleListArrays[0] = group1;
            }
        }else {//接收到的article>=10条
            if (abstractArticleListSize>=2) {//两组以上
                group1.add(abstractArticleList.get(0));
                group2.add(abstractArticleList.get(1));
                articleListSrc.remove(abstractArticleList.get(0));
                articleListSrc.remove(abstractArticleList.get(1));
                group1.addAll(articleListSrc.subList(0, 4));
                group2.addAll(articleListSrc.subList(4, 8));//报错了，因为size=6，而你要截取5-9所有出错
//                group.put(CardContentManager.key_group1, group1);
//                group.put(CardContentManager.key_group2, group2);
                articleListArrays[0] = group1;
                articleListArrays[1] = group2;
            }else if (abstractArticleListSize == 1) {//就一组
                group1.add(abstractArticleList.get(0));
                group1.addAll(articleListSrc.subList(0, 4));
//                group.put(CardContentManager.key_group1, group1);
                articleListArrays[0] = group1;
            }
        }
        return articleListArrays;
    }

    public static HashMap<String, LinkedHashSet<Article>> groupByAbstract(List<Article> articleListSrc) {
        HashMap<String, LinkedHashSet<Article>> group = new HashMap<>();
        LinkedHashSet<Article> group1 = new LinkedHashSet<>();
        LinkedHashSet<Article> group2 = new LinkedHashSet<>();
        List<Article> abstractArticleList = getAbstractArticleList(articleListSrc);
        int abstractArticleListSize = abstractArticleList.size();
        if (articleListSrc.size() < 10) {//接收到的Article不足10条：可能是1组，也可能是0组
            if (abstractArticleListSize>1) {//有一组
                Article firstArticle = abstractArticleList.get(0);
                group1.add(firstArticle);
                articleListSrc.remove(firstArticle);
                if (articleListSrc.size() < 5) {
                    group1.addAll(articleListSrc);
                } else {
                    group1.addAll(articleListSrc.subList(0, 5));
                }
                group.put(CardContentManager.key_group1, group1);
            }
        }else {//接收到的article>=10条
            if (abstractArticleListSize>=2) {//两组以上
                group1.add(abstractArticleList.get(0));
                group2.add(abstractArticleList.get(1));
                articleListSrc.remove(abstractArticleList.get(0));
                articleListSrc.remove(abstractArticleList.get(1));
                group1.addAll(articleListSrc.subList(0, 4));
                group2.addAll(articleListSrc.subList(4, 8));//报错了，因为size=6，而你要截取5-9所有出错
                group.put(CardContentManager.key_group1, group1);
                group.put(CardContentManager.key_group2, group2);
            }else if (abstractArticleListSize == 1) {//就一组
                group1.add(abstractArticleList.get(0));
                group1.addAll(articleListSrc.subList(0, 4));
                group.put(CardContentManager.key_group1, group1);
            }
        }
        return group;
    }


    /**
     * 对获取到的一点咨询进行排序.尽量保证第一个是有图的
     * @param yiDianModelList
     * @return
     */
    public static List<YiDianModel> sortYiDianModelList(List<YiDianModel> yiDianModelList) {
        LinkedList<YiDianModel> sortListYiDianModel = new LinkedList<>();
        int size = yiDianModelList.size();
        for (int i = 0 ; i < size ; i++) {
           YiDianModel yiDianModel = yiDianModelList.get(i);
            if (yiDianModel.getImages().length>0) {//如果有图,则放入第一个
                sortListYiDianModel.addFirst(yiDianModel);
            }else {
                sortListYiDianModel.add(yiDianModel);
            }
        }
        return sortListYiDianModel;
    }

    /**
     * 判断是否超过了4个小时
     * @return
     */
    private static boolean isOver4hour() {
        Calendar latestModifiedCalendar = DateUtils.getInstance().millis2Calendar(CommonShareData.getString(Const.KEY_CARD_CLEAR_OFFSET, "0"));//默认最后更新时间为0
        latestModifiedCalendar.add(Calendar.SECOND,14400);//14400S=4hour
        if (latestModifiedCalendar.before(Calendar.getInstance())) return true;
        return false;
    }

    public static void clearOffset () {
        if (isOver4hour()) {
//            log("超过4小时清空offset");
            Log.i("Kinflow","超过4小时清空offset");
            CardContentManagerFactory.clearAllOffset();
            try {
                CommonShareData.putString(Const.KEY_CARD_CLEAR_OFFSET,String.valueOf(Calendar.getInstance().getTimeInMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

