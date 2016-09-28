package com.klauncher.kinflow.views.recyclerView.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.common.utils.OpenMode2;

import java.util.List;

/**
 * Created by xixionghui on 16/9/21.
 */
public class BaseRecyclerViewAdapterData {

    public static final int TYPE_BANNER = 11;
    public static final int TYPE_NEWS_JINRITOUTIAO = 12;
    public static final int TYPE_NEWS_SOUGOU = 13;

    public int type;
    public int order;
    public List<String> openOptions;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
}
