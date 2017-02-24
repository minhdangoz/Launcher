package com.klauncher.kinflow.weather.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xixionghui on 2016/3/21.
 */
public class AddressComponent implements Parcelable {

    @Override
    public String toString() {
        return "AddressComponent{" +
                "city='" + city + '\'' +
                ", direction='" + direction + '\'' +
                ", distance='" + distance + '\'' +
                ", district='" + district + '\'' +
                ", province='" + province + '\'' +
                ", street='" + street + '\'' +
                ", street_number='" + street_number + '\'' +
                '}';
    }

    /**
     * city : 成都市
     * direction : near
     * distance : 2
     * district : 都江堰市
     * province : 四川省
     * street : 景中路
     * street_number : 201号
     */

    private String city;
    private String direction;
    private String distance;
    private String district;
    private String province;
    private String street;
    private String street_number;

    public AddressComponent() {
    }

    public AddressComponent(String city, String direction, String distance, String district, String province, String street, String street_number) {
        this.city = city;
        this.direction = direction;
        this.distance = distance;
        this.district = district;
        this.province = province;
        this.street = street;
        this.street_number = street_number;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet_number() {
        return street_number;
    }

    public void setStreet_number(String street_number) {
        this.street_number = street_number;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.city);
        dest.writeString(this.direction);
        dest.writeString(this.distance);
        dest.writeString(this.district);
        dest.writeString(this.province);
        dest.writeString(this.street);
        dest.writeString(this.street_number);
    }

    protected AddressComponent(Parcel in) {
        this.city = in.readString();
        this.direction = in.readString();
        this.distance = in.readString();
        this.district = in.readString();
        this.province = in.readString();
        this.street = in.readString();
        this.street_number = in.readString();
    }

    public static final Parcelable.Creator<AddressComponent> CREATOR = new Parcelable.Creator<AddressComponent>() {
        @Override
        public AddressComponent createFromParcel(Parcel source) {
            return new AddressComponent(source);
        }

        @Override
        public AddressComponent[] newArray(int size) {
            return new AddressComponent[size];
        }
    };
}
