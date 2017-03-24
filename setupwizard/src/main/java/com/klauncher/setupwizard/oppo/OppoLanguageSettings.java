package com.klauncher.setupwizard.oppo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.klauncher.setupwizard.R;
import com.klauncher.setupwizard.common.ConfigurationUtil;

import java.util.Locale;

/**
 * description：Oppo语言选择
 * <br>author：caowugao
 * <br>time： 2017/03/24 12:52
 */

public class OppoLanguageSettings extends Activity implements View.OnClickListener {

    private View tickSimpleChinese;
    private View tickTraditionalChinese;
    private View tickEnglish;

    private static final int INDEX_OF_SIMPLE_CHINESE = 1;
    private static final int INDEX_OF_TRADITIONAL_CHINESE = 2;
    private static final int INDEX_OF_ENGLISH = 3;
    private int currentLanguage = INDEX_OF_SIMPLE_CHINESE;

    private static Handler HANDLER = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_language_layout);
        initViews();
    }

    private Runnable resumeRun = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(OppoLanguageSettings.this, OpWifiSettings.class);
            startActivity(intent);
        }
    };

    private void initViews() {
        View containerSimpleChinese = findViewById(R.id.container_simple_chinese);
        View containerTraditionalChinese = findViewById(R.id.container_traditional_chinese);
        View containerEnglish = findViewById(R.id.container_english);
        View resume = findViewById(R.id.tv_page_resume);

        containerSimpleChinese.setOnClickListener(this);
        containerTraditionalChinese.setOnClickListener(this);
        containerEnglish.setOnClickListener(this);
        resume.setOnClickListener(this);


        tickSimpleChinese = findViewById(R.id.tick_simple_chinese);
        tickTraditionalChinese = findViewById(R.id.tick_traditional_chinese);
        tickEnglish = findViewById(R.id.tick_english);

        tickSimpleChinese.setVisibility(View.VISIBLE);
        tickTraditionalChinese.setVisibility(View.GONE);
        tickEnglish.setVisibility(View.GONE);

    }

    /**
     * @param context
     * @param language
     */
    public static void updateAppLanguage(Context context, Locale language) {
        ConfigurationUtil.updateAppLanguage(context, language);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.container_simple_chinese) {
            tickSimpleChinese.setVisibility(View.VISIBLE);
            tickTraditionalChinese.setVisibility(View.GONE);
            tickEnglish.setVisibility(View.GONE);
            currentLanguage = INDEX_OF_SIMPLE_CHINESE;
        } else if (id == R.id.container_traditional_chinese) {
            tickSimpleChinese.setVisibility(View.GONE);
            tickTraditionalChinese.setVisibility(View.VISIBLE);
            tickEnglish.setVisibility(View.GONE);
            currentLanguage = INDEX_OF_TRADITIONAL_CHINESE;
        } else if (id == R.id.container_english) {
            tickSimpleChinese.setVisibility(View.GONE);
            tickTraditionalChinese.setVisibility(View.GONE);
            tickEnglish.setVisibility(View.VISIBLE);
            currentLanguage = INDEX_OF_ENGLISH;
        } else if (id == R.id.tv_page_resume) {

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
}
