package com.klauncher.biddingos.commons.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {
    private static final String TAG = "StringUtils";
    public static String join(List<String> listString, String sep) {

        if(listString.size()<=0) return null;

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<listString.size(); i++) {
            if(i>0) sb.append(sep);
            sb.append(listString.get(i));
        }
        return sb.toString();
    }

    public static List<String> StringToList(String stringToList, String sep) {
        String[] strArr = stringToList.split(sep);
        List<String> list = new ArrayList<>(Arrays.asList(strArr));
        return list;
    }


    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            LogUtils.w(TAG, "", e);
            return null;
        }
    }

    public static String signature(String body, String signKey) {
        return body+ StringUtils.getMD5(body+signKey);
    }


}
