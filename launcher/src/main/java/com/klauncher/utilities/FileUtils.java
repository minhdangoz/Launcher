package com.klauncher.utilities;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public class FileUtils {
    private static final int BUFFER_SIZE = 512;

    public static boolean isFileExist(String path) {
        return new File(path).exists();
    }

    public static String loadStringFromStream(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(new BufferedInputStream(is));
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        while(isr.read(buffer) > 0) {
            builder.append(buffer);
        }
        return builder.toString();
    }

    public static boolean safeClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
                return true;
            } catch (Exception e) {

            }
        }
        return false;
    }

    public static boolean write(CharSequence from, File to, Charset charset) {
        if (from == null || to == null || charset == null) {
            throw new NullPointerException();
        }

        FileOutputStream out = null;
        Writer writer = null;
        boolean result = true;

        try {
            out = new FileOutputStream(to);
            writer = new OutputStreamWriter(out, charset).append(from);
        } catch (IOException e) {
            return false;
        } finally {
            if (writer == null) {
                safeClose(out);
            }
            result &= safeClose(writer);
        }

        return result;
    }

    public static boolean deleteDir(File dir, boolean deleteSelf) {
        boolean success = false;
        if (dir.isDirectory()) {
            File files[] = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        success = deleteDir(f, true);
                    } else {
                        success = f.delete();
                    }
                    if (!success) {
                        break;
                    }
                }
            }
        }
        if (deleteSelf) {
            success = dir.delete();
        }
        return success;
    }

    public static String FormatFileNameLength (String fileName, int maxLength) {

        if (fileName.length() <= maxLength)
            return fileName;

        String result = null;

        final int headLength = 3;
        final int tailLength = 3;
        final String omitStr = "...";
        int dot = fileName.lastIndexOf(".");
        final int extLength = fileName.length() - dot;
        final int minLength = headLength + tailLength + omitStr.length();

        if(maxLength < minLength)
            return fileName;

        if (dot >= 0) {
            result = fileName.substring(0, maxLength - tailLength - extLength - omitStr.length())
                    + omitStr + fileName.substring(dot - tailLength);
        } else {
            result = fileName.substring(0, maxLength);
        }

        return result;
    }
}
