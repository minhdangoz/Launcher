package com.klauncher.setupwizard.oppo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.klauncher.setupwizard.R;
import com.suke.widget.SwitchButton;


/**
 * description：Oppo 使用条款
 * <br>author：caowugao
 * <br>time： 2017/03/24 12:52
 */
public class OpAgreement extends Activity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_agreement);
        initUi();
    }

    private void initUi() {
        View back = findViewById(R.id.iv_back);
        View resume = findViewById(R.id.tv_page_resume);
        SwitchButton switchButton = (SwitchButton) findViewById(R.id.switch_button);
        switchButton.setChecked(true);
        back.setOnClickListener(this);
        resume.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_back) {
            finish();

        } else if (i == R.id.tv_page_resume) {
            Intent intent = new Intent();
            intent.setClass(this,OpSetupComplete.class);
            startActivity(intent);

        }
    }
}
