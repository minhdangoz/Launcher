package com.kapp.kinflow.business.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * description：文件辅助类
 * <br>author：caowugao
 * <br>time： 2017/05/26 14:34
 */

public class FileUtil {
    private FileUtil() {
    }

    /**
     * 从assets中读取文本
     *
     * @param context
     * @param name
     * @return String
     * @throws Exception
     */
    public static String readFileFromAssets(Context context, String name) throws Exception {
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open(name);
        } catch (Exception e) {
            throw new Exception(FileUtil.class.getName() + ".readFileFromAssets---->" + name + " not found");
        }
        return inputStream2String(is);
    }

    /**
     * 输入流转字符串
     *
     * @param is
     * @return 一个流中的字符串
     */
    public static String inputStream2String(InputStream is) {
        if (null == is) {
            return null;
        }
        StringBuilder resultSb = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            resultSb = new StringBuilder();
            String line;
            while (null != (line = br.readLine())) {
                resultSb.append(line + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                closeIO(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null == resultSb ? null : resultSb.toString();
    }

    /**
     * 关闭流
     *
     * @param closeables
     * @throws Exception
     */
    public static void closeIO(Closeable... closeables) throws Exception {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                throw new Exception(FileUtil.class.getClass().getName(), e);
            }
        }
    }
}
