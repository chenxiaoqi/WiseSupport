package com.lazyman.timetennis;

import com.wisesupport.commons.httpclient.RequestLoggerInterceptor;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class SpringConfiguration {

    @Bean(destroyMethod = "close")
    public CloseableHttpClient httpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        SSLContext context = SSLContextBuilder
                .create()
                .loadTrustMaterial(null, (x509Certificates, s) -> true).build();
        return HttpClientBuilder.create()
                .setSSLContext(context)
                .setDefaultCookieStore(new BasicCookieStore())
                .addInterceptorLast(new RequestLoggerInterceptor())
                .build();
    }
}
