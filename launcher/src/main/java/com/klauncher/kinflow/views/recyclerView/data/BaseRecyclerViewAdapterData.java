package com.klauncher.kinflow.views.recyclerView.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.klauncher.kinflow.browser.KinflowBrower;
import com.klauncher.kinflow.cards.model.sougou.SougouSearchArticle;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.OpenMode2;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.launcher.BuildConfig;

import java.util.List;

/**
 * Created by xixionghui on 16/9/21.
 */
public class BaseRecyclerViewAdapterData {

    public static final int TYPE_BANNER = 11;
    public static final int TYPE_NEWS_JINRITOUTIAO = 12;
    public static final int TYPE_NEWS_SOUGOU = 13;

    public int kinflowConentType;
    public int order;
    public List<String> openOptions;

    public int getKinflowConentType() {
        return kinflowConentType;
    }

    public void setKinflowConentType(int kinflowConentType) {
        this.kinflowConentType = kinflowConentType;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<String> getOpenOptions() {
        return openOptions;
    }

    public void setOpenOptions(List<String> openOptions) {
        this.openOptions = openOptions;
    }

    /**
     * 这个方法应该放到到控制器中,目前控制器接口还没出,暂时放到这里.
     * @param context
     * @param extras
     * @param cardOpenOptionList
     * @return
     */
    public String open(Context context, Bundle extras,List<String> cardOpenOptionList) {
        String finalOpenComponent = "";
        //通过Bundle获取url&&uri
        String openUrl = null;
        String openUri = null;
        if (extras != null) {
            openUrl = extras.getString(OpenMode2.OPEN_URL_KEY);
            openUri = extras.getString(OpenMode2.FIRST_OPEN_MODE_TYPE_URI);
        } else {
            return finalOpenComponent;
        }
        //获取OpenMode
        if (null==openUrl) return finalOpenComponent;
        OpenMode2 openMode = new OpenMode2(context,cardOpenOptionList, openUrl,this);
        try {
            if (CommonUtils.getInstance().isInstalledAPK(context,openMode.getFirstIntent().getComponent())) {
                Log.e("Kinflow", "尝试使用第一打开方式: " + openMode.getFirstIntent().getComponent().toString() + " uri = " + openMode.getFirstIntent().getData().toString());
                context.startActivity(openMode.getFirstIntent());
            } else if (CommonUtils.getInstance().isInstalledAPK(context,openMode.getSecondIntent().getComponent())) {
                Log.e("Kinflow", "尝试使用第二打开方式: " + openMode.getSecondIntent().getComponent().toString() + " uri = " + openMode.getSecondIntent().getData().toString());
                context.startActivity(openMode.getSecondIntent());
            } else if (CommonUtils.getInstance().isInstalledAPK(context,openMode.getThirdIntent().getComponent())) {
                context.startActivity(openMode.getThirdIntent());
                Log.e("Kinflow", "尝试使用第三打开方式: " + openMode.getThirdIntent().getComponent().toString() + " uri = " + openMode.getThirdIntent().getData().toString());
            } else {
                CommonUtils.getInstance().openDefaultBrowserUrl(context,openUrl);
            }


        } catch (Exception e) {
            try {
                Intent secondIntent = openMode.getSecondIntent();
                Log.e("Kinflow", "open: 当前第一打开方式失败,错误原因:" + e.getMessage() + "\n" +
                        "尝试使用第二打开方式: \n componentName = "
                                + secondIntent.getComponent().toString()
                               + "uri = "+secondIntent.getData().toString());
                if (null==secondIntent.getComponent())  {
                    throw new AndroidRuntimeException("Unknown action intent...");
                }
                context.startActivity(openMode.getSecondIntent());
                finalOpenComponent = openMode.getSecondIntent().getComponent().getPackageName();
            } catch (Exception e1) {
                Intent thirdIntent = openMode.getThirdIntent();
                Log.e("Kinflow", "open: 当前第二打开方式失败,错误原因:"+e1.getMessage()+
                        "\n尝试使用第三打开方式: \n"
                        + "componentName = "+thirdIntent.getComponent().toString()
                        + "uri = "+ thirdIntent.getData().toString());
                context.startActivity(thirdIntent);
                finalOpenComponent = openMode.getThirdIntent().getComponent().getPackageName();
            }
        }finally {
            //以下代码用于做统计使用
            return finalOpenComponent;
        }
    }

    private static final String INNER_BROWSER_PACKAGENAME = "com.klauncher.launcher/com.klauncher.kinflow.browser.KinflowBrower";
    private static final String CLIENT_SCHEMA = "xxx/www";

    public String openByOrder (Context context) {

        String finalOpenComponent = BuildConfig.APPLICATION_ID;
        for (int i = 0; i < getOpenOptions().size(); i++) {
            try {
                KinflowLog.w("尝试第"+i+"打开方式");
                String openComponentName = getOpenOptions().get(i);
                Log.e("kinflow", "openByOrder: openComponentName"+openComponentName);
                if (openComponentName.equals(INNER_BROWSER_PACKAGENAME)) {//用自带浏览器打开
                    if (this.kinflowConentType == TYPE_NEWS_JINRITOUTIAO) {
                        JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle) this;
                        String articleUrl = TextUtils.isEmpty(jinRiTouTiaoArticle.getArticle_url()) ? jinRiTouTiaoArticle.getUrl() : jinRiTouTiaoArticle.getArticle_url();
                        KinflowBrower.openUrl(context,articleUrl);
                    } else if (this.kinflowConentType == TYPE_NEWS_SOUGOU) {
                        SougouSearchArticle sougouSearchArticle = (SougouSearchArticle) this;
                        String articleUrl = TextUtils.isEmpty(sougouSearchArticle.getLink()) ? sougouSearchArticle.getOpen_link() : sougouSearchArticle.getLink();
                        KinflowBrower.openUrl(context, articleUrl);
                    }
                    finalOpenComponent = BuildConfig.APPLICATION_ID;
                }else if (openComponentName.equals(CLIENT_SCHEMA)) {//用新闻对应的客户端打开
                    if (this.kinflowConentType == TYPE_NEWS_JINRITOUTIAO) {
                        JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle) this;
                        String uri = Const.URI_TOUTIAO_ARTICLE_DETAIL+jinRiTouTiaoArticle.getGroup_id();
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                        finalOpenComponent = Const.TOUTIAO_packageName;
                    } else if (this.kinflowConentType == TYPE_NEWS_SOUGOU) {
                        SougouSearchArticle sougouSearchArticle = (SougouSearchArticle) this;
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sougouSearchArticle.getSchema())));
                        finalOpenComponent = Const.SOUGOU_packageName;
                    }
                }else {//用浏览器打开
                    String articleUrl = "http://m.hao123.com/?union=1&from=1012581h&tn=ops1012581h";
                    if (this.kinflowConentType == TYPE_NEWS_JINRITOUTIAO) {
                        JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle) this;
                        articleUrl = TextUtils.isEmpty(jinRiTouTiaoArticle.getArticle_url()) ? jinRiTouTiaoArticle.getUrl() : jinRiTouTiaoArticle.getArticle_url();
                    } else if (this.kinflowConentType == TYPE_NEWS_SOUGOU) {
                        SougouSearchArticle sougouSearchArticle = (SougouSearchArticle) this;
                        articleUrl = TextUtils.isEmpty(sougouSearchArticle.getOpen_link())? sougouSearchArticle.getLink() : sougouSearchArticle.getOpen_link();
                    }
                    String[] cns = getOpenOptions().get(i).split("/");
                    ComponentName componentName = new ComponentName(cns[0],cns[1]);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setComponent(componentName);
                    browserIntent.setData(Uri.parse(articleUrl));
                    context.startActivity(browserIntent);
                    finalOpenComponent = componentName.getPackageName();
                }
                return finalOpenComponent;
            } catch (Exception e) {
                KinflowLog.w("第" + i + "打开方式失败");
                //判断如果是最后一个了,用默认打开,就别再继续了
                continue;
            }

        }
        return finalOpenComponent;
    }
}
