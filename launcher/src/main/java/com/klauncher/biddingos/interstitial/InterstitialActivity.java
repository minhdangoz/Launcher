package com.klauncher.biddingos.interstitial;

import android.app.Activity;
import android.os.Bundle;


/**
 * Created by：lizw on 2016/3/29 10:20
 */
public class InterstitialActivity extends Activity {
    public static String INTERSTITIALKEY="interstitial_key";
    private int zoneId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zoneId=getIntent().getIntExtra(INTERSTITIALKEY,-1);
        new InterstitialHelperImpl().onCreate(InterstitialActivity.this,zoneId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new InterstitialHelperImpl().onDestroy(zoneId);
    }
}
