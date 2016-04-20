package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.klauncher.launcher.R;
import com.klauncher.kinflow.cards.model.Card;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.task.AsynchronousGet;
import com.klauncher.kinflow.navigation.adapter.NavigationAdapter;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.utilities.KinflowLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xixionghui on 16/4/12.
 * 此类是TTCardContentManager和YDCardContentManager的基类
 */
public abstract class BaseCardContentManager{

    Context mContext;
    /**
     *
     * @param context
     * @param Handler mainHandler 用于当Card信息加载完毕后,通知{@link MainControl#mHandler}进行信号量控制.
     */
    BaseCardContentManager (Context context) {
        this.mContext = context;
    }

    public abstract void moreNews();
    public abstract void changeNews();
    //ourDefineChannelId----CardInfo.cardSecondTypeId
//    public abstract void requestCardContent(Handler mainControlHandler,int ourDefineChannelId);
    public abstract void requestCardContent(Handler mainControlHandler,CardInfo cardInfo);

    /**
     * 获取数据失败(无论任何原因)
     * @param msg
     */
    void onFailToast(Message msg) {
        switch (msg.arg1) {
            case AsynchronousGet.CONNECTION_ERROR:
                Toast.makeText(mContext, mContext.getResources().getString(R.string.kinflow_string_connection_error), Toast.LENGTH_SHORT).show();
                break;
            case AsynchronousGet.RESPONSE_FAIL:
                Toast.makeText(mContext, mContext.getResources().getString(R.string.kinflow_string_response_fail), Toast.LENGTH_SHORT).show();
                break;
            case AsynchronousGet.OBTAIN_RESULT_NULL:
                Toast.makeText(mContext, mContext.getResources().getString(R.string.kinflow_string_obtain_result_null), Toast.LENGTH_SHORT).show();
                break;
            case AsynchronousGet.PARSE_ERROR:
                Toast.makeText(mContext, mContext.getResources().getString(R.string.kinflow_string_parse_error), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    final protected static void log(String msg) {
        KinflowLog.i(msg);
    }

}
