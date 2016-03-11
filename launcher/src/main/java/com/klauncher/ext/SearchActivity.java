package com.klauncher.ext;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.klauncher.launcher.R;

/**
 * Search Activity
 */
public class SearchActivity extends Activity {
    private static final String BAIDU_SEARCH = "http://m.baidu.com/ssid=1dc073756e6c6169724503/s?word=%s";
    private static Bitmap tab_bg;
    EditText mSearchText;
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        mSearchText = (EditText) findViewById(R.id.search_edit_text);
        mSearchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    doSearch();
                }
                return false;
            }
        });
        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch();
            }
        });


        Launcher launcher = LauncherAppState.getInstance().getModel().getLauncherInstance();
        int currentScreen = launcher.getCurrentWorkspaceScreen();

        rootView = findViewById(R.id.search_activity_root);
        setBackground();
    }

    private void setBackground() {
        // 获取当前壁纸
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = displaymetrics.heightPixels;

        // 截取相应屏幕的Bitmap
        Bitmap pbm = Bitmap.createBitmap(bm, width/4, height/4,
                width/2, height/2);
        final Bitmap blurBmp = FastBlur.doBlur(pbm, 17, true);
        final Drawable newBitmapDrawable = new BitmapDrawable(getResources(), blurBmp); // 将Bitmap转换为Drawable
        rootView.post(new Runnable() {
            @Override
            public void run() {
                rootView.setBackground(newBitmapDrawable);//设置背景
            }
        });
    }

    private void doSearch() {
        String text = mSearchText.getText().toString();
        if (null!=text && !text.isEmpty()) {
            String url = String.format(BAIDU_SEARCH, text);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
