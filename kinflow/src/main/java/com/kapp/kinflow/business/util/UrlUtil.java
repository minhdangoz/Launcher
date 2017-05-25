package com.kapp.kinflow.business.util;

import java.util.Map;
import java.util.Set;

/**
 * description：url辅助类
 * <br>author：caowugao
 * <br>time： 2017/05/03 10:47
 */

public class UrlUtil {
    private UrlUtil(){}
    /**
     * 用于get方法的参数拼凑
     *
     * @param params
     * @return String
     */
    public static String pieceParmasByGET(Map<String, String> params) {
        if (params == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Set<Map.Entry<String, String>> paramsSet = params.entrySet();
        for (Map.Entry<String, String> item : paramsSet) {
            String key = item.getKey();
            String value = item.getValue();
            stringBuilder.append(key + "=" + value + "&");
        }
        return "?" + stringBuilder.substring(0, stringBuilder.length() - 1);
    }
}
