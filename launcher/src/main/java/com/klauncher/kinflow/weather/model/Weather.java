package com.klauncher.kinflow.weather.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xixionghui on 2016/3/21.
 */
public class Weather implements Parcelable {

    /**
     * city : 北京
     * pinyin : beijing
     * citycode : 101010100
     * date : 16-03-21
     * time : 18:00
     * postCode : 100000
     * longitude : 116.391
     * latitude : 39.904
     * altitude : 33
     * weather : 阴
     * temp : 8
     * l_tmp : 8
     * h_tmp : 20
     * WD : 无持续风向
     * WS : 微风(<10km/h)
     * sunrise : 06:15
     * sunset : 18:27
     */

    private String city;
    private String pinyin;
    private String citycode;
    private String date;
    private String time;
    private String postCode;
    private double longitude;
    private double latitude;
    private String altitude;//还把
    private String weather;
    private String temp;
    private String l_tmp;
    private String h_tmp;
    private String WD;
    private String WS;
    private String sunrise;
    private String sunset;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getL_tmp() {
        return l_tmp;
    }

    public void setL_tmp(String l_tmp) {
        this.l_tmp = l_tmp;
    }

    public String getH_tmp() {
        return h_tmp;
    }

    public void setH_tmp(String h_tmp) {
        this.h_tmp = h_tmp;
    }

    public String getWD() {
        return WD;
    }

    public void setWD(String WD) {
        this.WD = WD;
    }

    public String getWS() {
        return WS;
    }

    public void setWS(String WS) {
        this.WS = WS;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public Weather() {
    }

    public Weather(String city, String pinyin, String citycode, String date, String time, String postCode, double longitude, double latitude, String altitude, String weather, String temp, String l_tmp, String h_tmp, String WD, String WS, String sunrise, String sunset) {
        this.city = city;
        this.pinyin = pinyin;
        this.citycode = citycode;
        this.date = date;
        this.time = time;
        this.postCode = postCode;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.weather = weather;
        this.temp = temp;
        this.l_tmp = l_tmp;
        this.h_tmp = h_tmp;
        this.WD = WD;
        this.WS = WS;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "city='" + city + '\'' +
                ", pinyin='" + pinyin + '\'' +
                ", citycode='" + citycode + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", postCode='" + postCode + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", altitude='" + altitude + '\'' +
                ", weather='" + weather + '\'' +
                ", temp='" + temp + '\'' +
                ", l_tmp='" + l_tmp + '\'' +
                ", h_tmp='" + h_tmp + '\'' +
                ", WD='" + WD + '\'' +
                ", WS='" + WS + '\'' +
                ", sunrise='" + sunrise + '\'' +
                ", sunset='" + sunset + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.city);
        dest.writeString(this.pinyin);
        dest.writeString(this.citycode);
        dest.writeString(this.date);
        dest.writeString(this.time);
        dest.writeString(this.postCode);
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.latitude);
        dest.writeString(this.altitude);
        dest.writeString(this.weather);
        dest.writeString(this.temp);
        dest.writeString(this.l_tmp);
        dest.writeString(this.h_tmp);
        dest.writeString(this.WD);
        dest.writeString(this.WS);
        dest.writeString(this.sunrise);
        dest.writeString(this.sunset);
    }

    protected Weather(Parcel in) {
        this.city = in.readString();
        this.pinyin = in.readString();
        this.citycode = in.readString();
        this.date = in.readString();
        this.time = in.readString();
        this.postCode = in.readString();
        this.longitude = in.readDouble();
        this.latitude = in.readDouble();
        this.altitude = in.readString();
        this.weather = in.readString();
        this.temp = in.readString();
        this.l_tmp = in.readString();
        this.h_tmp = in.readString();
        this.WD = in.readString();
        this.WS = in.readString();
        this.sunrise = in.readString();
        this.sunset = in.readString();
    }

    public static final Parcelable.Creator<Weather> CREATOR = new Parcelable.Creator<Weather>() {
        @Override
        public Weather createFromParcel(Parcel source) {
            return new Weather(source);
        }

        @Override
        public Weather[] newArray(int size) {
            return new Weather[size];
        }
    };
}
