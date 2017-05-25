package com.kapp.kinflow.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.kapp.kinflow.R;


/**
 * description：弹出框
 * <br>author：caowugao
 * <br>time： 2017/04/21 10:57
 */

public class ActionSheetDialog extends Dialog {


    public ActionSheetDialog(Context context) {
        super(context);
    }

    public ActionSheetDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public ActionSheetDialog(Builder builder) {
        this(builder.contentView.getContext(), builder.themeResId);
    }

    public static class Builder {
        int themeResId;
        View contentView;
        int gravity = Gravity.CENTER;
        int x = 0;
        int y = 0;
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean canceledOnTouchOutside = true;
        boolean cancelable = true;
        OnCancelListener onCancelListener;
        OnDismissListener onDismissListener;
        OnKeyListener onKeyListener;
        OnShowListener onShowListener;

        public Builder() {
            this(R.style.ActionSheetDialogStyle);
        }

        public Builder(int themeResId) {
            this.themeResId = themeResId;
        }

        public Builder contentView(View contentView) {
            this.contentView = contentView;
            return this;
        }

        public Builder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder canceledOnTouchOutside(boolean canceledOnTouchOutside) {
            this.canceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        public Builder cancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder showAt(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder onCancelListener(OnCancelListener listener) {
            this.onCancelListener = listener;
            return this;
        }

        public Builder onDismissListener(OnDismissListener listener) {
            this.onDismissListener = listener;
            return this;
        }

        public Builder onKeyListener(OnKeyListener listener) {
            this.onKeyListener = listener;
            return this;
        }

        public Builder onShowListener(OnShowListener listener) {
            this.onShowListener = listener;
            return this;
        }

        public ActionSheetDialog build() {
            ActionSheetDialog dialog = new ActionSheetDialog(this);
            dialog.setContentView(contentView);

            Window window = dialog.getWindow();
            window.setGravity(gravity);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.x = x;
            attributes.y = y;
            attributes.width = width;
            attributes.height = height;
            window.setAttributes(attributes);

            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
            dialog.setCancelable(cancelable);
            if (null != onCancelListener) {
                dialog.setOnCancelListener(onCancelListener);
            }
            if (null != onDismissListener) {
                dialog.setOnDismissListener(onDismissListener);
            }
            if (null != onShowListener) {
                dialog.setOnShowListener(onShowListener);
            }
            if (null != onKeyListener) {
                dialog.setOnKeyListener(onKeyListener);
            }
            return dialog;
        }

    }
}
