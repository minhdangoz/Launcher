package com.kapp.kinflow.business.beans;

import com.kapp.kinflow.R;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.KnewsApp;
import com.kapp.knews.repository.bean.DongFangTouTiao;

import java.util.List;


/**
 * description：热门游戏bean
 * <br>author：caowugao
 * <br>time： 2017/04/20 18:41
 */
public class HotGameCardBean extends BaseInformationFlowBean {
    public String secondTittle;
    public String thirdTittle;
    public DongFangTouTiao second;
    public DongFangTouTiao third;

    @Override
    public int getItemType() {
        return TYPE_HOT_GAME;
    }

    public List<SingleHotGameBean> gameBeanList;

    public HotGameCardBean(String secondTittle, String thirdTittle, List<SingleHotGameBean> gameBeanList) {
        this.secondTittle = secondTittle;
        this.thirdTittle = thirdTittle;
        this.gameBeanList = gameBeanList;
    }

    public HotGameCardBean(NewsBean gameNewsCardBean, List<SingleHotGameBean> gameBeanList) {
        setDongFangTouTiao(gameNewsCardBean.second, gameNewsCardBean.third);
        this.gameBeanList = gameBeanList;
    }

    private void setDongFangTouTiao(DongFangTouTiao second, DongFangTouTiao third) {
        this.second = second;
        this.third = third;

        this.secondTittle = null == second ? null : second.getmNewsBean().getSummaryTitle();
        this.thirdTittle = null == third ? null : third.getmNewsBean().getSummaryTitle();
    }

    public void updateDongFangTouTiao(NewsBean news) {
        setDongFangTouTiao(news.second, news.third);
    }

    public void updateGameList(List<SingleHotGameBean> gameBeanList) {
        this.gameBeanList = gameBeanList;
    }

    public static class NewsBean {
        public DongFangTouTiao second;
        public DongFangTouTiao third;

        public NewsBean(DongFangTouTiao second, DongFangTouTiao third) {
            this.second = second;
            this.third = third;
        }
    }

    public static class SingleHotGameBean extends BaseItemBean {
        public String imageUrl;
        public String name;
        public int status;
        public static final int STATUS_NOT_DOWNLOAD = 1;
        public static final int STATUS_INSTALLED = 2;
        private static String DESCRPTION_STATUS_INSTALLED;
        private static String DESCRPTION_STATUS_NOT_DOWNLOAD;
        private static String DESCRPTION_STATUS_UNKNOWN;

        static {
            DESCRPTION_STATUS_INSTALLED = KnewsApp.getApp().getResources().getString(R.string.status_installed);
            DESCRPTION_STATUS_NOT_DOWNLOAD = KnewsApp.getApp().getResources().getString(R.string
                    .status_not_download);
            DESCRPTION_STATUS_UNKNOWN = KnewsApp.getApp().getResources().getString(R.string
                    .status_unknown);
        }

        public SingleHotGameBean(String imageUrl, String name, int status) {
            this.imageUrl = imageUrl;
            this.name = name;
            this.status = status;
        }

        public String getStatusDescrption() {
            if (STATUS_NOT_DOWNLOAD == status) {
                return DESCRPTION_STATUS_NOT_DOWNLOAD;
            } else if (STATUS_INSTALLED == status) {
                return DESCRPTION_STATUS_INSTALLED;
            }
            return DESCRPTION_STATUS_UNKNOWN;
        }
    }

}
