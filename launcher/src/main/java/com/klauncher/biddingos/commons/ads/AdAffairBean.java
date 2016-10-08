package com.klauncher.biddingos.commons.ads;

/**
 * 用于控制广告上报事务使用
 * Created by：lizw on 2016/1/21 14:34
 */
public class AdAffairBean {
    String transactionid;//key
    int impressionhappen;
    int impressionstatus;
    String impressionurl;
    int clickhappen;
    int clickstatus;
    String clickurl;
    int conversionhappen;
    int conversionstatus;
    String conversionurl;
    String content;
    long timestamp;

    public int getConversionhappen() {
        return conversionhappen;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public int getImpressionhappen() {
        return impressionhappen;
    }

    public int getImpressionstatus() {
        return impressionstatus;
    }


    public int getClickhappen() {
        return clickhappen;
    }

    public int getClickstatus() {
        return clickstatus;
    }


    public int getConversionstatus() {
        return conversionstatus;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    public void setImpressionhappen(int impressionhappen) {
        this.impressionhappen = impressionhappen;
    }

    public void setImpressionstatus(int impressionstatus) {
        this.impressionstatus = impressionstatus;
    }


    public void setClickhappen(int clickhappen) {
        this.clickhappen = clickhappen;
    }

    public void setClickstatus(int clickstatus) {
        this.clickstatus = clickstatus;
    }


    public void setConversionhappen(int conversionhappen) {
        this.conversionhappen = conversionhappen;
    }

    public void setConversionstatus(int conversionstatus) {
        this.conversionstatus = conversionstatus;
    }


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImpressionurl() {
        return impressionurl;
    }

    public String getClickurl() {
        return clickurl;
    }

    public String getConversionurl() {
        return conversionurl;
    }

    public String getcontent() {
        return content;
    }

    public void setImpressionurl(String impressionurl) {
        this.impressionurl = impressionurl;
    }

    public void setClickurl(String clickurl) {
        this.clickurl = clickurl;
    }

    public void setConversionurl(String conversionurl) {
        this.conversionurl = conversionurl;
    }

    public void setcontent(String content) {
        this.content = content;
    }
}
