// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-

package com.kapp.knews.common.utils;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException t) {
            }
        }
    }
}
