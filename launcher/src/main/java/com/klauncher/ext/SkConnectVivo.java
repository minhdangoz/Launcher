package com.klauncher.ext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by hw on 16-12-14.
 */
public class SkConnectVivo extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent start = new Intent();
            start.setClassName("com.ptns.da.dl","com.ptns.da.sdk.DlShortCutService");
//            start.putExtra("packageName", packageName);
            start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(start);
        }catch (Exception e){

        }
    }
}
