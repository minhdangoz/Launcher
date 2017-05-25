package com.kapp.knews.helper.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kapp.knews.R;
import com.kapp.knews.helper.Utils;


public class DialogUtils {

    public static Toast make(int paramInt) {

        View localView = LayoutInflater.from(Utils.getContext()).inflate(R.layout.short_message, null);
        ((TextView) localView.findViewById(R.id.message)).setText(paramInt);
        Toast localToast = new Toast(Utils.getContext());
        localToast.setDuration(Toast.LENGTH_SHORT);
        localToast.setView(localView);
        return localToast;
    }

    public static Toast make(String paramString) {
        View localView = LayoutInflater.from(Utils.getContext()).inflate(R.layout.short_message, null);
        ((TextView) localView.findViewById(R.id.message)).setText(paramString);
        Toast localToast = new Toast(Utils.getContext());
        localToast.setDuration(Toast.LENGTH_SHORT);
        localToast.setView(localView);
        return localToast;
    }
}
