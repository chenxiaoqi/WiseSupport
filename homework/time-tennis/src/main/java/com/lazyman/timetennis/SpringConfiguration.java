package com.lazyman.timetennis;

import com.wisesupport.commons.httpclient.RequestLoggerInterceptor;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

@Configuration
public class SpringConfiguration {

    @Value("${wx.mch-id}")
    private String mchId;

    @Bean(destroyMethod = "close")
    public CloseableHttpClient httpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, IOException, CertificateException {

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(this.getClass().getClassLoader().getResourceAsStream("zlib.log"), mchId.toCharArray());
        kmf.init(keyStore, mchId.toCharArray());
        SSLContext context = SSLContextBuilder.create().loadKeyMaterial(keyStore, mchId.toCharArray()).build();
        return HttpClientBuilder.create()
                .setSSLContext(context)
                .setDefaultCookieStore(new BasicCookieStore())
                .addInterceptorLast(new RequestLoggerInterceptor())
                .build();
    }
}
