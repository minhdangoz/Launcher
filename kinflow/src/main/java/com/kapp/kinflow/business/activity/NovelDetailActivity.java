package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * description：小说详情页面
 * <br>author：caowugao
 * <br>time： 2017/05/21 16:12
 */

public class NovelDetailActivity extends Activity {

    @BindView(R2.id.container_back)
    LinearLayout containerBack;
    @BindView(R2.id.container_loading)
    RelativeLayout containerLoading;
    @BindView(R2.id.image)
    ImageView image;
    @BindView(R2.id.tv_name)
    TextView tvName;
    @BindView(R2.id.tv_author)
    TextView tvAuthor;
    @BindView(R2.id.tv_status)
    TextView tvStatus;
    @BindView(R2.id.tv_read)
    TextView tvRead;
    @BindView(R2.id.tv_add)
    TextView tvAdd;
    @BindView(R2.id.book_detail_abstract_title)
    TextView bookDetailAbstractTitle;
    @BindView(R2.id.tv_simple_introduction)
    TextView tvSimpleIntroduction;
    @BindView(R2.id.book_detail_chapter_title)
    TextView bookDetailChapterTitle;
    @BindView(R2.id.container_chapter)
    LinearLayout containerChapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_detail);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();
        initViews();
        initData();
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, NovelDetailActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private void initData() {

    }

    private void initViews() {

    }
    @OnClick(R2.id.container_back)
    public void back(View view) {
        finish();
    }
}
