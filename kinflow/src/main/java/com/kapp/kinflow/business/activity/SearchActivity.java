package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.HotSearchItemFactory;
import com.kapp.kinflow.business.beans.SingleTextBean;
import com.kapp.kinflow.business.util.KeyBoardUtil;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.knews.common.browser.KinflowBrower;
import com.kapp.knews.repository.utils.Const;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：搜索页面
 * <br>author：caowugao
 * <br>time： 2017/04/20 10:40
 */

public class SearchActivity extends Activity {
    @BindView(R2.id.edit_search)
    EditText editSearch;
    @BindView(R2.id.iv_refresh)
    ImageView ivRefresh;
    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;
    private boolean isLocked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kinflow_search);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();
        initViews();
        initData();
    }

    private void initData() {
        int size = 6;
        List<SingleTextBean> words = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            words.add(new SingleTextBean("热词" + i));
        }
        IItemFactory factory = new HotSearchItemFactory();
        RecycleViewCommonAdapter<SingleTextBean> adapter = new RecycleViewCommonAdapter<>(words, recyclerview, factory);
        recyclerview.setAdapter(adapter);
    }

    private void initViews() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent event) {
                if ((event != null && event.getKeyCode() == KeyEvent
                        .KEYCODE_ENTER)) {
                    if (isLocked) {
                        return true;
                    }
                    String keyword = editSearch.getText().toString();
                    if (null != keyword && !"".equals(keyword)) {
                        openBrowser(keyword);
                        KeyBoardUtil.closeKeybord(editSearch, editSearch.getContext());
                        isLocked = true;
                    }
                    return true;
                }
                return false;
            }
        });

        recyclerview.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void openBrowser(String keyword) {
        String url = Const.URL_SEARCH_WITH_BAIDU + keyword;
        KinflowBrower.openUrl(this, url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLocked = false;
    }
}
