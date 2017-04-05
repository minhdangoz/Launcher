package com.klauncher.setupwizard.hw;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.klauncher.setupwizard.BaseActivity;
import com.klauncher.setupwizard.R;


/**
 * Created by hw on 17-3-22.
 */

public class HwSetupComplete extends BaseActivity {

    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hw_setup_over_layout);
        startBtn = (Button) findViewById(R.id.hw_setup_start);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    Intent setup = new Intent();
//                    setup.setClassName("com.klauncher.hwlauncher","com.klauncher.ext.KLauncher");
//                    setup.putExtra("fromSetupwizard", true);
//                    startActivity(setup);
                    finishAll();
                }catch (Exception e){
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
