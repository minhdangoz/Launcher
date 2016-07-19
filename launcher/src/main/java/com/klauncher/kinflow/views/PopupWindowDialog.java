package com.klauncher.kinflow.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.launcher.R;

/**
 * Created by xixionghui on 16/7/12.
 */
public class PopupWindowDialog extends PopupWindow implements View.OnClickListener,CheckBox.OnCheckedChangeListener{
    //必定有
    private Context mContext;
    private View mRootView;
    private PopupWindowDialogListener mDialogListener;

    //根据rootView而定
    private CheckBox cb_select;
    private TextView tv_cancle,tv_ok;


    public PopupWindowDialog(Context context) {
        this.mContext = context;
        initConfig();
        initView();
    }

//    public PopupWindowDialog(Context context) {
//        this.mContext = context;
//        this.mDialogListener = popupWindowDialogListener;
//        initConfig();
//        initView();
//    }

    private void initConfig() {
        //必选项
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(getRootView());
        //可选
        setBackgroundDrawable(new ColorDrawable(0x00000000));
        setOutsideTouchable(true);
        setFocusable(true);
    }

    private void initView() {
        cb_select = (CheckBox) mRootView.findViewById(R.id.popupWindow_selectCheckBox);
        tv_cancle = (TextView) mRootView.findViewById(R.id.cancel);
        tv_ok = (TextView) mRootView.findViewById(R.id.ok);
        tv_cancle.setOnClickListener(this);
        tv_ok.setOnClickListener(this);
    }

    public View getRootView() {
        if (null==mRootView) mRootView = LayoutInflater.from(getContext()).inflate(R.layout.popupwindow_kinflow_use_net,null);
        return mRootView;
    }

    public Context getContext() {
        if (null==mContext) mContext = KLauncherApplication.mKLauncherApplication;
        return mContext;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dismissPopupWindowDialog();
                if (null!=mDialogListener)
                    mDialogListener.cancleClick();
                break;
            case R.id.ok:
                dismissPopupWindowDialog();
                if (null!=mDialogListener)
                    mDialogListener.okClick();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        mDialogListener.checkBoxIsChecked(isChecked);
    }

    public void showPopupWindowDialog() {
        this.showAtLocation(getRootView(), Gravity.CENTER, 0, 0);
    }

    public void showPopupWindowDialog(PopupWindowDialogListener popupWindowDialogListener) {
        showPopupWindowDialog();
        mDialogListener = popupWindowDialogListener;
    }

    public void dismissPopupWindowDialog() {
        this.dismiss();
    }

    public boolean getCompoundState () {
        return cb_select.isChecked();
    }

   public interface PopupWindowDialogListener {
//        void checkBoxIsChecked(boolean isSchecked);
        void cancleClick();
        void okClick();
    }
}
