package com.alpsdroid.ads.feeds;

/**
 * Created by：lizw on 2015/12/24 10:13
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;


public class ImageLoadUtils {


    private static final String TAG ="ImageLoadUtils" ;

    public static void onLoadImage(final URL bitmapUrl, final OnLoadImageListener onLoadImageListener) {
                // TODO Auto-generated method stub
                URL imageUrl = bitmapUrl;
                try {
                    HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                    InputStream inputStream = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    onLoadImageListener.OnLoadImage(bitmap, null);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    onLoadImageListener.OnLoadImage(null, null);
                }
    }

    public interface OnLoadImageListener {
        public void OnLoadImage(Bitmap bitmap, String bitmapPath);
    }

}
