package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.MineNovelsItemFactory;
import com.kapp.kinflow.business.beans.MineNovelsBean;
import com.kapp.kinflow.business.constant.Constant;
import com.kapp.kinflow.business.http.OKHttpUtil;
import com.kapp.kinflow.business.util.KeyBoardUtil;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.PullUpRecyclerviewAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.knews.base.recycler.decoration.ItemDecorationDivider;
import com.kapp.knews.common.browser.KinflowBrower;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * description：小说搜索页面
 * <br>author：caowugao
 * <br>time： 2017/05/21 14:23
 */

public class NovelSearchActivity extends Activity implements OKHttpUtil.OnRequestListener,
        PullUpRecyclerviewAdapter.OnLoadMoreListener, RecycleViewEvent.OnItemClickListener {

    private static final int REQUEST_CODE_SEARCH = 100;
    @BindView(R2.id.container_back)
    LinearLayout containerBack;
    @BindView(R2.id.editext)
    EditText editext;
    @BindView(R2.id.container_search)
    LinearLayout containerSearch;
    @BindView(R2.id.container_loading)
    RelativeLayout containerLoading;
    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;
    private boolean isLocked = false;
    private static final int PAGE_SIZE = 10;
    private int pageNum = 1;
    private PullUpRecyclerviewAdapter adapter;
    private String keyword;
    private boolean isReSearch = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_search);
        ButterKnife.bind(this);
//        getSupportActionBar().hide();
        initViews();
        initData();
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, NovelSearchActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private void initData() {
    }

    private void initViews() {
        editext.addTextChangedListener(new TextWatcher() {
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
        editext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent event) {
                if ((event != null && event.getKeyCode() == KeyEvent
                        .KEYCODE_ENTER)) {
                    if (isLocked) {
                        return true;
                    }
                    String keyword = editext.getText().toString();
                    if (null != keyword && !"".equals(keyword)) {
                        doSearch(keyword);
                        KeyBoardUtil.closeKeybord(editext, editext.getContext());
                        isLocked = true;
                    }
                    return true;
                }
                return false;
            }
        });


        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerview.addItemDecoration(new ItemDecorationDivider(this, LinearLayoutManager.VERTICAL));
    }

    private void doSearch(@NonNull String keyword) {
        containerLoading.setVisibility(View.VISIBLE);
//        recyclerview.setVisibility(View.GONE);
        isReSearch = true;
        if (null != adapter) {
            adapter.clearNormal();
        }

        this.keyword = keyword;
        pageNum = 1;
        request(keyword, pageNum);
    }

    private void request(@NonNull String keyword, int pageNum) {
        String novelSearchUrl = Constant.getNovelSearchUrl(keyword, pageNum, PAGE_SIZE);
        OKHttpUtil.get(novelSearchUrl, null, REQUEST_CODE_SEARCH, this);
    }

    @OnClick(R2.id.container_search)
    public void onSearchBtnClick(View view) {
        String keyword = editext.getText().toString();
        if (null != keyword && !"".equals(keyword)) {
            doSearch(keyword);
            KeyBoardUtil.closeKeybord(editext, editext.getContext());
            isLocked = true;
        }
    }

    @OnClick(R2.id.container_back)
    public void back(View view) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLocked = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OKHttpUtil.cancel(REQUEST_CODE_SEARCH);
    }

    @Override
    public void onFailure(int requestCode, String msg) {

        if (isReSearch) {
            containerLoading.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);
            isReSearch = false;
        }

        if (null != adapter) {
            adapter.hideLoadMoreView();
        }
        isLocked = false;
        showShortToast(msg);
    }

    private void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(int requestCode, Object simpleResponse) {
        switch (requestCode) {
            case REQUEST_CODE_SEARCH:
                if (isReSearch) {
                    containerLoading.setVisibility(View.GONE);
//                    recyclerview.setVisibility(View.VISIBLE);
                    isReSearch = false;
                }

                if (null != adapter) {
                    adapter.hideLoadMoreView();
                }
                isLocked = false;
                handlerResult(simpleResponse);
                break;
        }
    }

    private void handlerResult(Object simpleResponse) {
        if (null == simpleResponse) {
            LogUtils.e("请求搜索接口失败！！！");
            return;
        }
        try {
            JSONObject rootJson = new JSONObject(simpleResponse.toString());
            int flag = rootJson.getInt("flag");
            if (1 == flag) {
                JSONArray data = rootJson.getJSONArray("data");
                int length = data.length();
                List<MineNovelsBean> datas = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    JSONObject itemJson = data.getJSONObject(i);
                    MineNovelsBean bean = new MineNovelsBean(itemJson);
                    datas.add(bean);
                }
                if (null == adapter) {
                    IItemFactory factory = new MineNovelsItemFactory();
                    adapter = new PullUpRecyclerviewAdapter(datas, recyclerview, factory);
                    adapter.setLoadMoreListener(this);
                    adapter.setOnItemClickListener(this);
                    adapter.commitItemEvent();
                    recyclerview.setAdapter(adapter);
                } else {
                    adapter.addAll(datas);
                }

            } else {
                showShortToast(getString(R.string.load_fail));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoadMore(RecyclerView recyclerView) {
        adapter.showLoadMoreView();
        pageNum++;
        request(keyword, pageNum);
    }

    @Override
    public void onNormalItemClick(View view, int position) {
        if (null != adapter) {
            List<MineNovelsBean> normalDatas = adapter.getNormalDatas();
            MineNovelsBean bean = normalDatas.get(position);
            if (null != bean) {
                KinflowBrower.openUrl(this, bean.clickUrl);
            }
        }
    }

    @Override
    public void onHeaderItemClick(View view, SparseArrayCompat<View> headers, int position) {

    }

    @Override
    public void onFooterItemClick(View view, SparseArrayCompat<View> footers, int position) {

    }
}
