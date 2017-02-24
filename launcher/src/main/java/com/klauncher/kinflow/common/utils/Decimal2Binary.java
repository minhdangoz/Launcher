package com.klauncher.kinflow.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by xixionghui on 16/6/3.
 */
public class Decimal2Binary {
    //lexicographical order
    public static String orderLexicographical(String[] strArray) {
        String t = null;
//        System.out.println("排序前");
//        for (String s : strArray)
//            System.out.print(s + "\t");
        int i, j, k;
        for (i = 0; i < strArray.length - 1; i++) {
            k = i;
            for (j = i + 1; j < strArray.length; j++) {
                Character c1 = Character.valueOf(strArray[j].charAt(0));
                Character c2 = Character.valueOf(strArray[k].charAt(0));
                if (c1.compareTo(c2) < 0)
                    k = j;
            }
            if (i != k) {
                t = strArray[i];
                strArray[i] = strArray[k];
                strArray[k] = t;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String str :
                strArray) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    public static String SHA1(String decript) {
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
