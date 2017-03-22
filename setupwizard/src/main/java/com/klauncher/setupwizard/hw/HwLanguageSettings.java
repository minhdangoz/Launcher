package com.klauncher.setupwizard.hw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.klauncher.setupwizard.R;


/**
 * Created by hw on 17-3-20.
 */

public class HwLanguageSettings extends Activity implements View.OnClickListener{

    private TextView ftTv;
    private TextView jtTv;
    private TextView enTv;
    private TextView callBtn;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hw_language_layout);
        ftTv = (TextView) findViewById(R.id.hw_zh_ft);
        jtTv = (TextView) findViewById(R.id.hw_zh_jt);
        enTv = (TextView) findViewById(R.id.hw_en);
        callBtn = (TextView) findViewById(R.id.hw_call);
        nextBtn = (Button) findViewById(R.id.hw_language_next);


        ftTv.setOnClickListener(this);
        jtTv.setOnClickListener(this);
        enTv.setOnClickListener(this);
        callBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.hw_zh_ft) {
        } else if (i == R.id.hw_zh_jt) {
        } else if (i == R.id.hw_en) {
        } else if (i == R.id.hw_call) {
        } else if (i == R.id.hw_language_next) {
            Intent intent = new Intent();
            intent.setClass(this, HwWifiSettings.class);
            startActivity(intent);

        }
    }
}
