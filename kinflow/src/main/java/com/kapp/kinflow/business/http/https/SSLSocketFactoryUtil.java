package com.kapp.kinflow.business.http.https;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okio.Buffer;


/**
 * description：oSSLSocketFactory工具类
 * <br>author：caowugao
 * <br>time： 2017/04/24 10:53
 */
public class SSLSocketFactoryUtil {

    /**
     *  访问自签名网站，初始化证书，放在Application 的onCreate方法中
     * @param certificates
     * @return void
     */
    public static SSLSocketFactory getSSLSocketFactory(InputStream... certificates) {
        /**
         * 构造CertificateFactory对象，通过它的generateCertificate(is)方法得到Certificate。
                                 然后讲得到的Certificate放入到keyStore中。
                                接下来利用keyStore去初始化我们的TrustManagerFactory
                                由trustManagerFactory.getTrustManagers获得TrustManager[]初始化我们的SSLContext
                                最后，设置我们mOkHttpClient.setSslSocketFactory即可。
         * 
         * 
         */

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null) certificate.close();
                } catch (IOException e) {
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            //            okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 访问自签名网站，初始化证书,放在Application 的onCreate方法中
     * @param ser ser文件证书的字符串内容
     * @return void
     */
    public static SSLSocketFactory getSSLSocketFactory(String ser) {
        return getSSLSocketFactory(new Buffer().writeUtf8(ser).inputStream());
    }

    /**
     * 访问自签名网站，双向认证，放在Application 的onCreate方法中
     * @param bks bks文件的输入流，用于双向认证，若是jks文件的话，要转成bks文件，见http://sourceforge.net/projects/portecle/files/latest/download?source=files
     * @param pwd
     * @param certificates
     * @return void
     */
    public static SSLSocketFactory getSSLSocketFactoryWithTwoWay(InputStream bks, String pwd, InputStream... certificates) {
        /**
         * 构造CertificateFactory对象，通过它的generateCertificate(is)方法得到Certificate。
                                 然后讲得到的Certificate放入到keyStore中。
                                接下来利用keyStore去初始化我们的TrustManagerFactory
                                由trustManagerFactory.getTrustManagers获得TrustManager[]初始化我们的SSLContext
                                最后，设置我们mOkHttpClient.setSslSocketFactory即可。
         * 
         * 
         */

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(bks, pwd.toCharArray());
            //            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null) certificate.close();
                } catch (IOException e) {
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, pwd.toCharArray());

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            //            okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 访问自签名网站，双向认证，放在Application 的onCreate方法中
     * @param context
     * @param assetsBKSFile 放在assert目录下的bks文件，用于双向认证，若是jks文件的话，要转成bks文件，见http://sourceforge.net/projects/portecle/files/latest/download?source=files
     * @param pwd
     * @param certificates 
     * @throws IOException
     * @return void
     */
    public static SSLSocketFactory getSSLSocketFactoryWithTwoWay(Context context, String assetsBKSFile, String pwd, InputStream... certificates)
            throws IOException {
        return getSSLSocketFactoryWithTwoWay(context.getAssets().open(assetsBKSFile), pwd, certificates);
    }

}
