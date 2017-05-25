package com.kapp.knews.helper.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kapp.knews.helper.Utils;


/**
 * Created by xixionghui on 2016/12/8.
 */

public class ViewHelper {

    public static LayoutInflater getInflater(Context context){
        return LayoutInflater.from(context);
    }

    public static LayoutInflater getInflater() {
        return LayoutInflater.from(Utils.getContext());
    }

    public static View getView(int resId, ViewGroup viewGroup){
        return getInflater().inflate(resId,viewGroup);
    }

    public static View getView(int resId, ViewGroup viewGroup, boolean attachToRoot){
        return getInflater().inflate(resId,viewGroup,attachToRoot);
    }


}
