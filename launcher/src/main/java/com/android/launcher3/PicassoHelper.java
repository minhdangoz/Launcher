package com.android.launcher3;

import android.content.Context;

import com.squareup.picasso.Picasso;

public class PicassoHelper {

    public static PicassoHelper mPicassoHelper;
    private Context mContext;
    private Picasso mPicasso;

    private PicassoHelper(Context context) {
        mContext = context.getApplicationContext();
        mPicasso = new Picasso.Builder(mContext).build();
    }

    public static PicassoHelper getInstance(Context context) {
        if (mPicassoHelper == null)
            mPicassoHelper = new PicassoHelper(context);
        return mPicassoHelper;
    }

    public Picasso getPicasso() {
        if (mPicasso == null)
            mPicasso = new Picasso.Builder(mContext).build();
        return mPicasso;
    }

    public void clear() {
        if (mPicasso != null) {
            mPicasso.shutdown();
            mPicasso = null;
        }
    }

}
