package com.klauncher.ext;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.klauncher.launcher.R;
import com.klauncher.utilities.DeviceInfoUtils;

/**
 * Created by hw on 16-10-27.
 */
public class TestConfigActivity extends Activity {

    TextView cfgTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_config);
        cfgTv = (TextView) findViewById(R.id.config_tv);
        cfgTv.setText(DeviceInfoUtils.getDeviceInfo(this));
    }
}
