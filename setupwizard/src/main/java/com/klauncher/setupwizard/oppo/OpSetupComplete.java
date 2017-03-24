package com.klauncher.setupwizard.oppo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.klauncher.setupwizard.R;

/**
 * description：oppo设置完成
 * <br>author：caowugao
 * <br>time： 2017/03/24 17:39
 */

public class OpSetupComplete extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_setup_comlete);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.tv_start).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.tv_start) {
            try {
                Intent setup = new Intent();
                setup.setClassName("com.klauncher.oplauncher", "com.klauncher.ext.KLauncher");
                startActivity(setup);
            } catch (Exception e) {
            }
        }

    }
}
