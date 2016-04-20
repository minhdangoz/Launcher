package com.klauncher.kinflow.cards.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by xixionghui on 2016/3/18.
 */
public class Card implements Parcelable {

    public static final String CARD_ID = "cd";
    public static final String CARD_NAME = "cn";
    public static final String CARD_TYPE_ID = "td";
    public static final String CARD_TYPE_NAME = "tn";
    public static final String CARD_ORDER = "od";
    public static final String CARD_FOOTER = "ft";
    public static final String CARD_EXTRA = "ai";
    public static final String CARD_OPEN_OPTIONS = "ops";

    private String cardId;//card内部id
    private String cardName;//card名称(header)
    private String cardTypeId;//card类型id
    private String cardTypeName;//card类型名称
    private String cardOrder;//card排序
    private String cardFooter;//card底部信息
    private String cardExtra;//card附件信息
    private List<String> cardOpenOptionList;//打开方式列表

    //APP端有关字段：cardOrder,cardTypeId,cardExtra,cardOpenOptionList,cardName(cardHeader)
    //我们根据排序决定

    public Card() {
    }

    public Card(String cardId, String cardName, String cardTypeId, String cardTypeName, String cardOrder, String cardFooter, String cardExtra, List<String> cardOpenOptionList) {
        this.cardId = cardId;
        this.cardName = cardName;
        this.cardTypeId = cardTypeId;
        this.cardTypeName = cardTypeName;
        this.cardOrder = cardOrder;
        this.cardFooter = cardFooter;
        this.cardExtra = cardExtra;
        this.cardOpenOptionList = cardOpenOptionList;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardTypeId() {
        return cardTypeId;
    }

    public void setCardTypeId(String cardTypeId) {
        this.cardTypeId = cardTypeId;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public String getCardOrder() {
        return cardOrder;
    }

    public void setCardOrder(String cardOrder) {
        this.cardOrder = cardOrder;
    }

    public String getCardFooter() {
        return cardFooter;
    }

    public void setCardFooter(String cardFooter) {
        this.cardFooter = cardFooter;
    }

    public String getCardExtra() {
        return cardExtra;
    }

    public void setCardExtra(String cardExtra) {
        this.cardExtra = cardExtra;
    }

    public List<String> getCardOpenOptionList() {
        return cardOpenOptionList;
    }

    public void setCardOpenOptionList(List<String> cardOpenOptionList) {
        this.cardOpenOptionList = cardOpenOptionList;
    }

    @Override
    public String toString() {
        return "Card{" +
                "cardId='" + cardId + '\'' +
                ", cardName='" + cardName + '\'' +
                ", cardTypeId='" + cardTypeId + '\'' +
                ", cardTypeName='" + cardTypeName + '\'' +
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
        dest.writeString(this.cardId);
        dest.writeString(this.cardName);
        dest.writeString(this.cardTypeId);
        dest.writeString(this.cardTypeName);
        dest.writeString(this.cardOrder);
        dest.writeString(this.cardFooter);
        dest.writeString(this.cardExtra);
        dest.writeStringList(this.cardOpenOptionList);
    }

    protected Card(Parcel in) {
        this.cardId = in.readString();
        this.cardName = in.readString();
        this.cardTypeId = in.readString();
        this.cardTypeName = in.readString();
        this.cardOrder = in.readString();
        this.cardFooter = in.readString();
        this.cardExtra = in.readString();
        this.cardOpenOptionList = in.createStringArrayList();
    }

    public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel source) {
            return new Card(source);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
}
