package com.kapp.kinflow.business.http.persistentcookiejar.persistence;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import okhttp3.Cookie;

public class SerializableCookie implements Serializable {
    private static final String TAG = SerializableCookie.class.getSimpleName();
    private static final long serialVersionUID = -8594045714036645534L;
    private transient Cookie cookie;
    private static long NON_VALID_EXPIRES_AT = -1L;

    public SerializableCookie() {
    }

    public String encode(Cookie cookie) {
        this.cookie = cookie;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;

        Object var5;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            return byteArrayToHexString(byteArrayOutputStream.toByteArray());
        } catch (IOException var15) {
            Log.d(TAG, "IOException in encodeCookie", var15);
            var5 = null;
        } finally {
            if(objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException var14) {
                    Log.d(TAG, "Stream not closed in encodeCookie", var14);
                }
            }

        }

        return (String)var5;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        byte[] arr$ = bytes;
        int len$ = bytes.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            byte element = arr$[i$];
            int v = element & 255;
            if(v < 16) {
                sb.append('0');
            }

            sb.append(Integer.toHexString(v));
        }

        return sb.toString();
    }

    public Cookie decode(String encodedCookie) {
        byte[] bytes = hexStringToByteArray(encodedCookie);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        ObjectInputStream objectInputStream = null;

        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableCookie)objectInputStream.readObject()).cookie;
        } catch (IOException var17) {
            Log.d(TAG, "IOException in decodeCookie", var17);
        } catch (ClassNotFoundException var18) {
            Log.d(TAG, "ClassNotFoundException in decodeCookie", var18);
        } finally {
            if(objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException var16) {
                    Log.d(TAG, "Stream not closed in decodeCookie", var16);
                }
            }

        }

        return cookie;
    }

    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];

        for(int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }

        return data;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.cookie.name());
        out.writeObject(this.cookie.value());
        out.writeLong(this.cookie.persistent()?this.cookie.expiresAt():NON_VALID_EXPIRES_AT);
        out.writeObject(this.cookie.domain());
        out.writeObject(this.cookie.path());
        out.writeBoolean(this.cookie.secure());
        out.writeBoolean(this.cookie.httpOnly());
        out.writeBoolean(this.cookie.hostOnly());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Cookie.Builder builder = new Cookie.Builder();
        builder.name((String)in.readObject());
        builder.value((String)in.readObject());
        long expiresAt = in.readLong();
        if(expiresAt != NON_VALID_EXPIRES_AT) {
            builder.expiresAt(expiresAt);
        }

        String domain = (String)in.readObject();
        builder.domain(domain);
        builder.path((String)in.readObject());
        if(in.readBoolean()) {
            builder.secure();
        }

        if(in.readBoolean()) {
            builder.httpOnly();
        }

        if(in.readBoolean()) {
            builder.hostOnlyDomain(domain);
        }

        this.cookie = builder.build();
    }
}
