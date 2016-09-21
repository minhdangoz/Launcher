package com.klauncher.kinflow.views.recyclerView.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AndroidRuntimeException;

import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.launcher.BuildConfig;

import java.util.List;

/**
 * Created by xixionghui on 16/9/21.
 */
public class BaseRecyclerViewAdapterData {

    public static final int TYPE_BANNER = 11;
    public static final int TYPE_NEWS = 12;

    public int type;
    public int order;

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
            openUrl = extras.getString(OpenMode.OPEN_URL_KEY);
            openUri = extras.getString(OpenMode.FIRST_OPEN_MODE_TYPE_URI);
        } else {
            return finalOpenComponent;
        }
        //获取OpenMode
        if (null==openUrl) return finalOpenComponent;
        OpenMode openMode = new OpenMode(context,cardOpenOptionList, openUrl);
        Intent firstIntent = openMode.getFirstIntent();
        try {
            Uri uri = null;
            String openType = openMode.getCurrentFirstOpenMode();//获取当前第一打开方式
            switch (openType) {
                case OpenMode.FIRST_OPEN_MODE_TYPE_URI:
                    uri = Uri.parse(openUri);
                    break;
                case OpenMode.FIRST_OPEN_MODE_TYPE_COMPONENT:
                    uri = Uri.parse(openUrl);
                    break;
                case OpenMode.FIRST_OPEN_MODE_TYPE_DEFAULT:
                    break;
            }
            firstIntent.setData(uri);
            context.startActivity(openMode.getFirstIntent());

            if (!openType.equals(OpenMode.FIRST_OPEN_MODE_TYPE_URI)) {//第一打开方式非uri
                finalOpenComponent = openMode.getFirstIntent().getComponent().getPackageName();
            } else {//第一打开方式为uri
                //获取第一打开方式的id
                int id = Integer.parseInt(cardOpenOptionList.get(0));
                switch (id) {
                    case OpenMode.ID_JIN_RI_TOU_TIAO:
                        finalOpenComponent = Const.TOUTIAO_packageName;
                        break;
                    default:
                        finalOpenComponent = BuildConfig.APPLICATION_ID;
                        break;
                }
            }

        } catch (Exception e) {
            try {
                Intent secondIntent = openMode.getSecondIntent();
//                Log.e("Kinflow", "open: 当前第一打开方式失败,错误原因:"+e.getMessage()+"\n" +
//                        "尝试使用第二打开方式: " + secondIntent.getComponent().toString());
                if (null==secondIntent.getComponent())  {
                    throw new AndroidRuntimeException("Unknown action intent...");
                }
                context.startActivity(openMode.getSecondIntent());
                finalOpenComponent = openMode.getSecondIntent().getComponent().getPackageName();
            } catch (Exception e1) {
                Intent thirdIntent = openMode.getThirdIntent();
//                Log.e("Kinflow", "open: 当前第二打开方式失败,错误原因:"+e1.getMessage()+
//                        "\n尝试使用第三打开方式: "+thirdIntent.getComponent().toString());
                context.startActivity(thirdIntent);
                finalOpenComponent = openMode.getThirdIntent().getComponent().getPackageName();
            }
        }finally {
            //以下代码用于做统计使用
            return finalOpenComponent;
        }
    }
}
