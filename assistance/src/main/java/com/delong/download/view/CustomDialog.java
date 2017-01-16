package com.delong.download.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.IntDef;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.delong.download.R;


/**
 * Created by alex on 15/12/8.
 */
public class CustomDialog extends Dialog {

    private TextView mTitle;
    private TextView mMessage;
    private TextView mBtnLift;
    private TextView mBtnRight;

    public final static int NORMAL = 0;

    public final static int SINGLE_BUTTON = 1;

    @IntDef({NORMAL, SINGLE_BUTTON})
    public @interface DialogMode {

    }

    private View.OnClickListener mDefaultListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CustomDialog.this.cancel();
        }
    };


    public CustomDialog(Context context, @DialogMode int mode) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog);
        mTitle = (TextView) findViewById(R.id.dialog_title);
        mMessage = (TextView) findViewById(R.id.dialog_message);
        mBtnLift = (TextView) findViewById(R.id.dialog_btn_lift);
        mBtnLift.setOnClickListener(mDefaultListener);
        mBtnRight = (TextView) findViewById(R.id.dialog_btn_right);
        mBtnRight.setOnClickListener(mDefaultListener);
        if (mode == SINGLE_BUTTON) {
            mBtnRight.setVisibility(View.GONE);
        }
    }

    public CustomDialog setTitleText(CharSequence title) {
        mTitle.setText(title);
        return this;
    }

    public CustomDialog setTitleText(int titleId) {
        mTitle.setText(titleId);
        return this;
    }

    public CustomDialog setMessage(CharSequence msg) {
        mMessage.setText(msg);
        return this;
    }

    public CustomDialog setMessage(int msgId) {
        mMessage.setText(msgId);
        return this;
    }

    public CustomDialog setLiftButtonText(CharSequence msg) {
        mBtnLift.setText(msg);
        return this;
    }

    public CustomDialog setLiftButtonText(int msgId) {
        mBtnLift.setText(msgId);
        return this;
    }

    public CustomDialog setRightButtonText(CharSequence msg) {
        mBtnRight.setText(msg);
        return this;
    }

    public CustomDialog setRightButtonText(int msgId) {
        mBtnRight.setText(msgId);
        return this;
    }

    public CustomDialog setOnLiftBtnClickListener(View.OnClickListener l) {
        mBtnLift.setOnClickListener(l);
        return this;
    }

    public CustomDialog setOnRightBtnClickListener(View.OnClickListener l) {
        mBtnRight.setOnClickListener(l);
        return this;
    }

}
