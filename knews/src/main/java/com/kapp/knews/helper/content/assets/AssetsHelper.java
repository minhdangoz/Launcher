package com.kapp.knews.helper.content.assets;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kapp.knews.R;
import com.kapp.knews.helper.content.ContentHelper;

import java.io.InputStream;


/**
 * Created by xixionghui on 2016/12/14.
 */

public class AssetsHelper {

    public static Bitmap readAssetsBitmap(String fileName){

        try {
            AssetManager assetManager = ContentHelper.getAssets();
            InputStream is = assetManager.open("navsite/"+fileName);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {//java.io.FileNotFoundException: navsite/1
            e.printStackTrace();
            return BitmapFactory.decodeResource(ContentHelper.getResource(), R.mipmap.ic_launcher);
        }

    }
}
