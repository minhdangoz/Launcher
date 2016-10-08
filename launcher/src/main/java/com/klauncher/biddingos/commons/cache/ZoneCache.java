package com.klauncher.biddingos.commons.cache;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneCache {

    private static final String TAG = "ZoneCache";
    private static ZoneCache zoneCache = null;
    private static Map<String, ArrayList> mListId_aPkg = new ConcurrentHashMap<>();

    public static synchronized ZoneCache shareInstance() {
        if (null == zoneCache) {
            zoneCache = new ZoneCache();
        }
        return zoneCache;
    }

    public boolean isIncludedWithPkg(String pkg, String listId) {
        Iterator<String> iterator = mListId_aPkg.keySet().iterator();
        while(iterator.hasNext())
        {
            if (listId == iterator.next()) {
                ArrayList aPkg = mListId_aPkg.get(listId);
                if(null==aPkg) {
                    return false;
                }
                if (aPkg.indexOf(pkg) == -1) {
                    return false;
                }else {
                    return true;
                }
            }
        }

        return false;
    }

    public synchronized void clearAll(String listId) {
        Iterator<String> iterator = mListId_aPkg.keySet().iterator();
        while(iterator.hasNext())
        {
            if (listId == iterator.next()) {
                ArrayList aZone = mListId_aPkg.get(listId);
                if(null!=aZone) {
                    aZone.clear();
                }
            }
        }

    }

    public synchronized void insertPkg(String pkg, String listId) {
        ArrayList aPkg = this.getArrayZonesbyList(listId);
        if (null!=aPkg) {
            aPkg.add(pkg);
        }else {
            //初始化
            ArrayList arrayList = new ArrayList();
            arrayList.add(pkg);
            mListId_aPkg.put(listId, arrayList);
        }
    }

    public ArrayList getArrayZonesbyList(String listId) {
        Iterator<String> iterator = mListId_aPkg.keySet().iterator();
        while(iterator.hasNext())
        {
            if (listId == iterator.next()) {
                ArrayList aZone = mListId_aPkg.get(listId);
                return aZone;
            }
        }

        return null;
    }


}



