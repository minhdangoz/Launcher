package com.kapp.knews.repository.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by xixionghui on 2016/11/24.
 */

public class StringUtils {

    public static int[] string2Ascii(String value) {
        char[] chars = value.toCharArray();
        int[] charsAsccii = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            charsAsccii[i] = (int) chars[i];
        }
        return charsAsccii;
    }

    /**
     * 返回字符串长度
     *
     * @param s 字符串
     * @return null返回0，其他返回自身长度
     */
    public static int length(CharSequence s) {
        return s == null ? 0 : s.length();
    }


    /**
     * 反转字符串
     *
     * @param s 待反转字符串
     * @return 反转字符串
     */
    public static String reverse(String s) {
        int len = length(s);
        if (len <= 1) return s;
        int mid = len >> 1;
        char[] chars = s.toCharArray();
        char c;
        for (int i = 0; i < mid; ++i) {
            c = chars[i];
            chars[i] = chars[len - i - 1];
            chars[len - i - 1] = c;
        }
        return new String(chars);
    }



    /**
     * Splits a string given a pattern (regex), considering escapes.
     * <p> For example considering a pattern "," we have:
     * one,two,three => {one},{two},{three}
     * one\,two\\,three => {one,two\\},{three}
     * <p>
     * NOTE: Untested with pattern regex as pattern and untested for escape chars in text or pattern.
     */
    public static String[] split(String text, String pattern) {
        String[] array = text.split(pattern, -1);
        ArrayList list = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            boolean escaped = false;
            if (i > 0 && array[i - 1].endsWith("\\")) {
                // When the number of trailing "\" is odd then there was no separator and this pattern is part of
                // the previous match.
                int depth = 1;
                while (depth < array[i-1].length() && array[i-1].charAt(array[i-1].length() - 1 - depth) == '\\') depth ++;
                escaped = depth % 2 == 1;
            }
            if (!escaped) list.add(array[i]);
            else {
                String prev = (String) list.remove(list.size() - 1);
                list.add(prev.substring(0, prev.length() - 1) + pattern + array[i]);
            }
        }
        return (String[]) list.toArray(new String[0]);
    }

    /**
     * Creates an MD5 digest from the message.
     * Note that this implementation is unsalted.
     * @param message not null
     * @return MD5 digest, not null
     */
    public static String md5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            StringBuffer sb = new StringBuffer();
            byte buf[] = message.getBytes();
            byte[] md5 = md.digest(buf);
            //System.out.println(message);
            for (int i = 0; i < md5.length; i++) {
                String tmpStr = "0" + Integer.toHexString((0xff & md5[i]));
                sb.append(tmpStr.substring(tmpStr.length() - 2));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 16位的md5
     * @param str
     * @return
     */
    public static final String md5Hex(final String str) {

            return md5(str).substring(8,24);
    }

    /**
     * Capitalizes each word in the given text by converting the
     * first letter to upper case.
     * @param data text to be capitalize, possibly null
     * @return text with each work capitalized,
     * or null when the text is null
     */
    public static String capitalizeWords(String data) {
        if (data==null) return null;
        StringBuffer res = new StringBuffer();
        char ch;
        char prevCh = '.';
        for ( int i = 0;  i < data.length();  i++ ) {
            ch = data.charAt(i);
            if ( Character.isLetter(ch)) {
                if (!Character.isLetter(prevCh) ) res.append( Character.toUpperCase(ch) );
                else res.append( Character.toLowerCase(ch) );
            } else res.append( ch );
            prevCh = ch;
        }
        return res.toString();
    }





}
