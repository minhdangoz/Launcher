package com.klauncher.setupwizard.hw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.klauncher.setupwizard.R;


/**
 * Created by hw on 17-3-22.
 */

public class HwAgreement extends Activity implements View.OnClickListener{

    private Button forwardBtn;
    private Button nextBtn;

    private RadioButton yesRb;
    private RadioButton noRb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hw_agreement_layout);
        initUi();
    }

    private void initUi(){
        forwardBtn = (Button) findViewById(R.id.hw_agreement_forward);
        nextBtn = (Button) findViewById(R.id.hw_agreement_next);
        forwardBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        yesRb = (RadioButton) findViewById(R.id.hw_agreement_yes);
        noRb = (RadioButton) findViewById(R.id.hw_agreement_no);

        yesRb.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    noRb.setChecked(!isChecked);
                    nextBtn.setEnabled(true);
                }
            }
        });

        noRb.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    yesRb.setChecked(!isChecked);
                    nextBtn.setEnabled(false);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.hw_agreement_forward) {
            finish();

        } else if (i == R.id.hw_agreement_next) {
            Intent intent = new Intent();
            intent.setClass(this, HwSetupComplete.class);
            startActivity(intent);

        }
    }
}
