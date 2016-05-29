package com.klauncher.kinflow.cards.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AndroidRuntimeException;

import com.klauncher.kinflow.cards.manager.BaseCardContentManager;
import com.klauncher.kinflow.cards.manager.CardContentManagerFactory;
import com.klauncher.kinflow.common.utils.OpenMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 2016/3/18.
 */
public class CardInfo implements Parcelable {

    public static final int CARD_TYPE_AD = 1;
    public static final int CARD_TYPE_APPS = 2;
    public static final int CARD_TYPE_NEWS = 3;
    public static final int CARD_TYPE_SETTING = 4;

    //TODO: 填满所有类型
    public static final int CARD_TYPE_AD_KAPPMOB = 100;
    public static final int CARD_TYPE_AD_ADVIEW = 101;

    public static final int CARD_TYPE_NEWS_TT_REDIAN = 301;
    public static final int CARD_TYPE_NEWS_YD_JINGXUAN = 350;
    public static final int CARD_TYPE_NEWS_YD_REDIAN = 351;
    public static final int CARD_TYPE_NEWS_YD_YULE = 353;
    public static final int CARD_TYPE_NEWS_YD_JIANKANG = 361;
    public static final int CARD_TYPE_NEWS_YD_QICHE = 363;
    public static final int CARD_TYPE_NEWS_YD_LVYOU = 368;

    public static final int CARD_TYPE_SETTING_WIFI = 400;

    public static final String CARD_ID = "id";
    public static final String CARD_NAME = "carn";
    public static final String CARD_TYPE_ID = "tyd";
    public static final String CARD_ORDER = "ord";
    public static final String CARD_FOOTER = "fot";
    public static final String CARD_EXTRA = "ext";
    public static final String CARD_OPEN_OPTIONS = "ops";

    private int cardId;//card内部id
    private String cardName;//card名称(header)
    private int cardTypeId;//card类型id
    private int cardSecondTypeId;//card的二级id,例如:一点咨询的channel(热点,体育,财经...)
    private int cardOrder;//card排序
    private String cardFooter;//card底部信息
    private String cardExtra;//card附件信息
    private List<String> cardOpenOptionList = new ArrayList<>();//打开方式列表
    private BaseCardContentManager mCardContentManager;
    private Context mContext;

    public static CardInfo createWifiSettingCard(Context context) {
        final String info = "{\"id\":\"0\",\"tyd\":\"4,400\",\"carn\":\"wifi设置\",\"fot\":\"\",\"ord\":\"1\",\"tyn\":\"设置类,Wifi\",\"ext\":\"\",\"ops\":[\"37\",\"{}\",\"1\"],\"th\":\"\"}";
        CardInfo wifiCard = null;
        try {
            wifiCard = new CardInfo(new JSONObject(info),context);
        } catch (JSONException e) {
        }
        return wifiCard;
    }

    //APP端有关字段：cardOrder,cardTypeId,cardExtra,cardOpenOptionList,cardName(cardHeader)
    //我们根据排序决定

    public CardInfo(JSONObject json,Context context) {
        cardId = json.optInt(CARD_ID);
        cardName = json.optString(CARD_NAME);
        String idsValue = json.optString(CARD_TYPE_ID);
        String[] ids = idsValue.split(",");
        cardTypeId = Integer.parseInt(ids[0]);
        cardSecondTypeId = Integer.parseInt(ids[1]);
        cardOrder = json.optInt(CARD_ORDER);
        cardFooter = json.optString(CARD_FOOTER);
        cardExtra = json.optString(CARD_EXTRA);
        JSONArray array = json.optJSONArray(CARD_OPEN_OPTIONS);
        if (array.length()<=0) {
            cardOpenOptionList.add("23");
            cardOpenOptionList.add("com.baidu.browser.apps/com.baidu.browser.framework.BdBrowserActivity");
            cardOpenOptionList.add("0");
        }else {
            for (int i = 0; i < array.length(); i++) {
                String ops = array.optString(i);
                if (ops != null && ops.length() > 0) {
                    cardOpenOptionList.add(ops);
                }
            }
        }
        this.mContext = context;
        this.mCardContentManager = CardContentManagerFactory.createCardContentManager(this.mContext,cardSecondTypeId);
    }

    public BaseCardContentManager getmCardContentManager() {
        return mCardContentManager;
    }

    public String open(Context context, Bundle extras) {
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
        OpenMode openMode = new OpenMode(context,this.cardOpenOptionList, openUrl);
        Intent firstIntent = openMode.getFirstIntent();
        try {
            Uri uri = null;
            String openType = openMode.getCurrentFirstOpenMode();
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
            finalOpenComponent = openMode.getFirstIntent().getComponent().getPackageName();

        } catch (Exception e) {
            try {
                Intent secondIntent = openMode.getSecondIntent();
                if (null==secondIntent.getComponent())  {
                    throw new AndroidRuntimeException("Unknown action intent...");
                }
                context.startActivity(openMode.getSecondIntent());
                finalOpenComponent = openMode.getSecondIntent().getComponent().getPackageName();
            } catch (Exception e1) {
                Intent thirdIntent = openMode.getThirdIntent();
                context.startActivity(thirdIntent);
                finalOpenComponent = openMode.getThirdIntent().getComponent().getPackageName();
            }
        }finally {
            //以下代码用于做统计使用
            return finalOpenComponent;
        }
    }

    public int getCardId() {
        return cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public int getCardTypeId() {
        return cardTypeId;
    }

    public int getCardSecondTypeId() {
        return cardSecondTypeId;
    }

    public int getCardOrder() {
        return cardOrder;
    }

    public String getCardFooter() {
        return cardFooter;
    }

    public String getCardExtra() {
        return cardExtra;
    }

    public List<String> getCardOpenOptionList() {
        return cardOpenOptionList;
    }

    public void openByOption(Context context) {
        PackageManager packageManager = context.getPackageManager();
    }

    @Override
    public String toString() {
        return "Card{" +
                "cardId='" + cardId + '\'' +
                ", cardName='" + cardName + '\'' +
                ", cardTypeId='" + cardTypeId + '\'' +
                ", cardSecondTypeId='" + cardSecondTypeId + '\'' +
                ", cardOrder='" + cardOrder + '\'' +
                ", cardFooter='" + cardFooter + '\'' +
                ", cardExtra='" + cardExtra + '\'' +
                ", cardOpenOptionList=" + opsToString() +
                '}';
    }

    String opsToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < cardOpenOptionList.size(); i++) {
            stringBuilder.append(cardOpenOptionList.get(i)).append("   ,   ");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.cardId);
        dest.writeString(this.cardName);
        dest.writeInt(this.cardTypeId);
        dest.writeInt(this.cardSecondTypeId);
        dest.writeInt(this.cardOrder);
        dest.writeString(this.cardFooter);
        dest.writeString(this.cardExtra);
        dest.writeStringList(this.cardOpenOptionList);
    }

    protected CardInfo(Parcel in) {
        this.cardId = in.readInt();
        this.cardName = in.readString();
        this.cardTypeId = in.readInt();
        this.cardSecondTypeId = in.readInt();
        this.cardOrder = in.readInt();
        this.cardFooter = in.readString();
        this.cardExtra = in.readString();
        this.cardOpenOptionList = in.createStringArrayList();
        this.mCardContentManager = CardContentManagerFactory.createCardContentManager(this.mContext, cardSecondTypeId);
    }

    public static final Creator<CardInfo> CREATOR = new Creator<CardInfo>() {
        @Override
        public CardInfo createFromParcel(Parcel source) {
            return new CardInfo(source);
        }

        @Override
        public CardInfo[] newArray(int size) {
            return new CardInfo[size];
        }
    };

}
