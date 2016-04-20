// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-

package com.klauncher.kinflow.utilities;

import android.net.Uri;
import android.text.TextUtils;

import java.util.regex.Pattern;

public class UrlUtils {
    private static final Pattern SCHEME_PATTERN = Pattern.compile("[^-a-zA-Z0-9.+]");
    private static final Pattern ARABIC_NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");

    public static String prefixHttpScheme(String inUrl) {
        if (inUrl.length() == 0) {
            return inUrl; // Return ""
        }
        Uri uri = Uri.parse(inUrl);
        if (uri.getScheme() != null
        // if the "scheme" contained invalid char, treat it as missing scheme.
                && !SCHEME_PATTERN.matcher(uri.getScheme()).find()
                // if SchemeSpecificPart is all 0-9, consider it's a port number
                // and "scheme" is a domain, e.g "baidu.com:80/index.html".
                && !is0To9Only(extractPortPart(uri.getSchemeSpecificPart()))) {
            return inUrl;
        } else { // scheme missing
            return "http://" + inUrl;
        }
    }

    private static String extractPortPart(String ssp) {
        if (TextUtils.isEmpty(ssp)) {
            return "";
        }
        int pos = ssp.indexOf('/');
        if (pos > 0) {
            return ssp.substring(0, pos);
        } else {
            return ssp;
        }
    }

    /**
     * @return true iff the str consists of only [0-9] char.
     *
     * NOTE: null or "" would return false.
     */
    private static boolean is0To9Only(CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            return false;
        }
        return ARABIC_NUMERIC_PATTERN.matcher(s).matches();
    }
}
