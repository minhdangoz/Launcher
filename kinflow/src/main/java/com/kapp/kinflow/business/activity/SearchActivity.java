package com.kapp.kinflow.business.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.HotSearchItemFactory;
import com.kapp.kinflow.business.beans.WordLinkBean;
import com.kapp.kinflow.business.constant.Constant;
import com.kapp.kinflow.business.http.OKHttpUtil;
import com.kapp.kinflow.business.util.KeyBoardUtil;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.knews.common.browser.KinflowBrower;
import com.kapp.knews.repository.utils.Const;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：搜索页面
 * <br>author：caowugao
 * <br>time： 2017/04/20 10:40
 */

public class SearchActivity extends Activity implements OKHttpUtil.OnRequestListener, RecycleViewEvent
        .OnItemClickListener {
    private static final int REQUEST_CODE_BAIDU = 100;
    private static final int REQUEST_CODE_SHENMA = 101;
    @BindView(R2.id.edit_search)
    EditText editSearch;
    @BindView(R2.id.iv_refresh)
    ImageView ivRefresh;
    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;
    private boolean isLocked = false;
    private List<WordLinkBean> datas;

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
//        int size = 6;
//        List<SingleTextBean> words = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            words.add(new SingleTextBean("热词" + i));
//        }
//        IItemFactory factory = new HotSearchItemFactory();
//        RecycleViewCommonAdapter<SingleTextBean> adapter = new RecycleViewCommonAdapter<>(words, recyclerview,
// factory);
//        recyclerview.setAdapter(adapter);

        OKHttpUtil.get(Constant.HOT_WORD_BAIDU_URL, null, REQUEST_CODE_BAIDU, this);
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

    @Override
    public void onFailure(int requestCode, String msg) {
        LogUtils.e("requestCode=" + requestCode + ", msg=" + msg);
        switch (requestCode) {
            case REQUEST_CODE_BAIDU:
                LogUtils.e("获取百度热词失败！！！");
                OKHttpUtil.get(Constant.HOT_WORD_SHENMA_URL, null, REQUEST_CODE_SHENMA, this);
                break;
            case REQUEST_CODE_SHENMA:
                LogUtils.e("获取神马热词失败！！！");
                break;
        }
    }

    @Override
    public void onResponse(int requestCode, Object simpleResponse) {
        switch (requestCode) {
            case REQUEST_CODE_BAIDU:
                handlerBaiduResult(simpleResponse);
                break;
            case REQUEST_CODE_SHENMA:
                handlerShenmaResult(simpleResponse);
                break;
        }
    }

    private void handlerShenmaResult(Object simpleResponse) {
        if (null == simpleResponse) {
            LogUtils.e("神马热词返回结果为空！！！");
            return;
        }
        try {
            JSONArray rootArr = new JSONArray(simpleResponse.toString());
            int jsonLength = rootArr.length();
            int size = 6;
            int resultSize = jsonLength > size ? size : jsonLength;
            datas = new ArrayList<>(resultSize);
            for (int i = 0; i < resultSize; i++) {
                JSONObject itemJson = rootArr.getJSONObject(i);
                String title = itemJson.getString("title");
                String url = itemJson.getString("url");
                datas.add(new WordLinkBean(title, url));
            }
            IItemFactory factory = new HotSearchItemFactory();
            RecycleViewCommonAdapter<WordLinkBean> adapter = new RecycleViewCommonAdapter<>(datas, recyclerview,
                    factory);
            adapter.setOnItemClickListener(this);
            adapter.commitItemEvent();
            recyclerview.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handlerBaiduResult(Object simpleResponse) {
        if (null == simpleResponse) {
            LogUtils.e("百度热词返回结果为空！！！");
            return;
        }

        try {
            JSONObject rootJson = new JSONObject(simpleResponse.toString());
            JSONObject hot = rootJson.getJSONObject("hot");
            int size = 6;
//            int jsonLength = hot.length();
//            int resultSize = jsonLength > size ? size : jsonLength;
            datas = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                JSONObject itemJson = hot.optJSONObject(String.valueOf(i + 1));
                if (null == itemJson) {
                    continue;
                }
                String word = itemJson.optString("word");
                String url = itemJson.optString("url");
                datas.add(new WordLinkBean(word, url));
            }
            IItemFactory factory = new HotSearchItemFactory();
            RecycleViewCommonAdapter<WordLinkBean> adapter = new RecycleViewCommonAdapter<>(datas, recyclerview,
                    factory);
            adapter.setOnItemClickListener(this);
            adapter.commitItemEvent();
            recyclerview.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNormalItemClick(View view, int position) {
        WordLinkBean bean = datas.get(position);
        KinflowBrower.openUrl(this, bean.url);
    }

    @Override
    public void onHeaderItemClick(View view, SparseArrayCompat<View> headers, int position) {

    }

    @Override
    public void onFooterItemClick(View view, SparseArrayCompat<View> footers, int position) {

    }
}
