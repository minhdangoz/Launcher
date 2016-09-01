package com.klauncher.biddingos.commons.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OriginCache {
    private static final String TAG = "OriginCache";
    private static OriginCache originCache = null;
    private static Map<String, ArrayList> mListId_aOrigin = new ConcurrentHashMap<>();

    public static synchronized OriginCache shareInstance() {
        if (null == originCache) {
            originCache = new OriginCache();
        }
        return originCache;
    }


    public synchronized void insertOriginInfo(Object originInfo, String listId) {

        ArrayList aOrigin = this.getArrayOriginbyList(listId);
        if (null!=aOrigin) {
            aOrigin.add(originInfo);
        }else {
            //初始化
            ArrayList arrayList = new ArrayList();
            arrayList.add(originInfo);
            mListId_aOrigin.put(listId, arrayList);
        }

    }

    public synchronized void clearAll(String listId) {
        Iterator<String> iterator = mListId_aOrigin.keySet().iterator();
        while(iterator.hasNext())
        {
            if (listId == iterator.next()) {
                ArrayList aOrigin = mListId_aOrigin.get(listId);
                if(null!=aOrigin) {
                    aOrigin.clear();
                }
            }
        }

    }

    public ArrayList getArrayOriginbyList(String listId) {
        Iterator<String> iterator = mListId_aOrigin.keySet().iterator();
        while(iterator.hasNext())
        {
            if (listId == iterator.next()) {
                ArrayList aOrigin = mListId_aOrigin.get(listId);
                return aOrigin;
            }
        }

        return null;
    }

    public boolean isOriginCacheEmptyInList(String listId) {
        ArrayList aOrigin = this.getArrayOriginbyList(listId);
        if (null!=aOrigin && aOrigin.size()>0) {
            return true;
        }
        return false;
    }

    public synchronized Object getOneOriginInList(String listId) {
        Object obj = null;
        ArrayList aOrigin = this.getArrayOriginbyList(listId);
        if (null!=aOrigin && aOrigin.size()>0) {
            obj = aOrigin.get(0);
            aOrigin.remove(0);
            return obj;
        }
        return obj;
    }



}
