package com.klauncher.kinflow.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.klauncher.kinflow.common.task.AsynchronousGet;
import com.klauncher.kinflow.search.model.HotWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Cache;

/**
 * Created by xixionghui on 2016/3/19.
 */
public class CacheHotWord {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Gson gson;

    private static CacheHotWord instance;
    static String fileName = "cacheHotWord";

    public static CacheHotWord getInstance() {
        if (null == instance) instance = new CacheHotWord();
        return instance;
    }

    public void createCacheHotWord(Context context) {
        sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new GsonBuilder().create();
    }

    private CacheHotWord() {
    }

    public String hotWord2String(HotWord hotWord) {
        return gson.toJson(hotWord);
    }

    public HotWord string2HotWord(String hotWordString) {
        return gson.fromJson(hotWordString, HotWord.class);
    }

    public void putHotWord(HotWord hotWord) throws NullPointerException {
        if (hotWord == null) throw new NullPointerException("hotWord is null");
        editor.putString(hotWord.getId(), hotWord2String(hotWord));
        editor.apply();
    }

    public HotWord getHotWord(String id) throws NullPointerException {
        if (id == null) throw new NullPointerException("id is null");
        return string2HotWord(id);
    }

    /**
     * 获取所有
     */
    public List<HotWord> getAll() {
        List<HotWord> hotWordList = new ArrayList<>();
        HashMap<String, String> allHotWord2String = (HashMap<String, String>) sharedPreferences.getAll();
        Set<Map.Entry<String, String>> entrySet = allHotWord2String.entrySet();
        if (entrySet.size() != 0) {
            for (Map.Entry entry : entrySet) {
                hotWordList.add(string2HotWord((String) entry.getValue()));
            }
        }
        return hotWordList;
    }

    public void clear() {
        editor.clear();
    }

    public void putNewGroup(List<HotWord> hotWordList){
        clear();
        putNewGroup(hotWordList);
        count = hotWordList.size();
        currentGroupId = 1;
    }

    private int count;
    private int currentGroupId = 0;//此id代表在UI界面上展现的两条热词中id比较大那个。默认为0，用户点击一下+1。

    public List<HotWord> getNextGroup() {
        List<HotWord> cacheHotWordList = getAll();
        int residualCount = count- currentGroupId*2;//剩余的没有展现给用户的热点新闻条数
        if (residualCount>2){//本地缓存的新闻条数大于2
                currentGroupId++;
                return cacheHotWordList.subList(currentGroupId*2,currentGroupId*2+2);
        }else {//本地缓存新闻条数小于2
            //随机两条
            return random(1,cacheHotWordList.size()-1);
            //异步请求
        }
    }

    /**
     * 随机生成两个HotWord
     * @param start
     * @param end
     * @return
     */
    public List<HotWord> random(int start,int end){
        List<HotWord> hotWordList = new ArrayList<>();
        for (int i = 0 ;i< 2;i++) {
            int id  = (int) (Math.random()*(end - start)+start);
           hotWordList.add(getHotWord(String.valueOf(id)));
        }
        return hotWordList;
    }
}

