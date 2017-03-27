package com.klauncher.setupwizard.hw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.klauncher.setupwizard.R;
import com.klauncher.setupwizard.common.ConfigurationUtil;

import java.util.Locale;

import static com.klauncher.setupwizard.SetupMain.SP_SETUP;
import static com.klauncher.setupwizard.SetupMain.SP_SETUP_OVER;


/**
 * Created by hw on 17-3-20.
 */

public class HwLanguageSettings extends Activity implements View.OnClickListener {

    private TextView tvSimpleChinese;
    private TextView tvTraditionChinese;
    private TextView tvEnglish;
    private TextView callBtn;
    private Button nextBtn;

    private static final int INDEX_OF_SIMPLE_CHINESE = 1;
    private static final int INDEX_OF_TRADITIONAL_CHINESE = 2;
    private static final int INDEX_OF_ENGLISH = 3;
    private int currentLanguage = INDEX_OF_SIMPLE_CHINESE;

    private static Handler HANDLER = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(SP_SETUP, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(SP_SETUP_OVER, true).commit();

        setContentView(R.layout.hw_language_layout);
        tvSimpleChinese = (TextView) findViewById(R.id.tv_simple_chinese);
        tvTraditionChinese = (TextView) findViewById(R.id.tv_tradition_chinese);
        tvEnglish = (TextView) findViewById(R.id.tv_english);
        callBtn = (TextView) findViewById(R.id.hw_call);
        nextBtn = (Button) findViewById(R.id.hw_language_next);


        tvSimpleChinese.setOnClickListener(this);
        tvTraditionChinese.setOnClickListener(this);
        tvEnglish.setOnClickListener(this);
        callBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    private Runnable resumeRun = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setClass(HwLanguageSettings.this, HwWifiSettings.class);
            startActivity(intent);
        }
    };

    /**
     * @param context
     * @param language
     */
    public static void updateAppLanguage(Context context, Locale language) {
        ConfigurationUtil.updateAppLanguage(context, language);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_simple_chinese) {
            Resources resources = getResources();
            tvTraditionChinese.setTextColor(resources.getColor(android.R.color.black));
            tvSimpleChinese.setTextColor(resources.getColor(R.color.hw_custem_green));
            tvEnglish.setTextColor(resources.getColor(android.R.color.black));
            currentLanguage = INDEX_OF_SIMPLE_CHINESE;
        } else if (i == R.id.tv_tradition_chinese) {
            Resources resources = getResources();
            tvTraditionChinese.setTextColor(resources.getColor(R.color.hw_custem_green));
            tvSimpleChinese.setTextColor(resources.getColor(android.R.color.black));
            tvEnglish.setTextColor(resources.getColor(android.R.color.black));
            currentLanguage = INDEX_OF_TRADITIONAL_CHINESE;
        } else if (i == R.id.tv_english) {
            Resources resources = getResources();
            tvTraditionChinese.setTextColor(resources.getColor(android.R.color.black));
            tvSimpleChinese.setTextColor(resources.getColor(android.R.color.black));
            tvEnglish.setTextColor(resources.getColor(R.color.hw_custem_green));
            currentLanguage = INDEX_OF_ENGLISH;
        } else if (i == R.id.hw_call) {
        } else if (i == R.id.hw_language_next) {


            if (currentLanguage == INDEX_OF_SIMPLE_CHINESE) {
                updateAppLanguage(this, Locale.SIMPLIFIED_CHINESE);
            } else if (currentLanguage == INDEX_OF_TRADITIONAL_CHINESE) {
                updateAppLanguage(this, Locale.TRADITIONAL_CHINESE);
            } else if (currentLanguage == INDEX_OF_ENGLISH) {
                updateAppLanguage(this, Locale.ENGLISH);
            }
            HANDLER.postDelayed(resumeRun, 100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != HANDLER) {
            HANDLER.removeCallbacks(resumeRun);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
