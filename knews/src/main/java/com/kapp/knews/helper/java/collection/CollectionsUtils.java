package com.kapp.knews.helper.java.collection;

import java.util.Collection;

/**
 * Created by xixionghui on 16/6/29.
 */
public class CollectionsUtils {

    public static boolean collectionIsNull (Collection collection) {
        if (null!=collection&&collection.size()!=0) return false;
        return true;
    }
}
