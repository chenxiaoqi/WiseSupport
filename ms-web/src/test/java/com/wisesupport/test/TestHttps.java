package com.wisesupport.test;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import sun.misc.HexDumpEncoder;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author c00286900
 * @version [版本号, 2017/8/19]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Ignore
public class TestHttps {


    @Test
    public void testPrivateCertificate() throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, KeyManagementException, UnrecoverableKeyException {

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(getKeyStore("/ssl/tomcat.keystore","jks"));
        X509Certificate[] fromKs = Arrays.stream(tmf.getTrustManagers()).map((tm) -> ((X509TrustManager) tm).getAcceptedIssuers()).flatMap(Stream::of).toArray(X509Certificate[]::new);

        X509Certificate fromCert;
        try (InputStream in = TestHttps.class.getResourceAsStream("/ssl/certcenter.cer")) {
            fromCert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(in);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(getKeyStore("/ssl/client.p12","PKCS12"), "123456".toCharArray());

        TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                try {
                    x509Certificates[0].verify(fromCert.getPublicKey());
                } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                    throw new CertificateException(e);
                }

                //or
                //((X509TrustManager)tmf.getTrustManagers()[0]).checkServerTrusted(x509Certificates,s);
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return fromKs;
            }
        };
        send("https://www.lazyman.com:8443/user/user_list", kmf.getKeyManagers(), tm);
    }


    @Test
    public void testTrusted() throws NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init((KeyStore) null);

        TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                ((X509TrustManager) tmf.getTrustManagers()[0]).checkServerTrusted(x509Certificates, s);
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        send("https://login.huawei.com/login/", null, tm);
    }

    private void send(String dest, KeyManager[] km, TrustManager tm) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        SSLContext context = SSLContext.getInstance("SSL");
        context.init(km, new TrustManager[]{tm}, null);

        URL url = new URL(dest);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setSSLSocketFactory(context.getSocketFactory());
        connection.setHostnameVerifier((s, sslSession) -> true);
        try {
            connection.connect();
            System.out.println(IOUtils.toString(connection.getInputStream(), "UTF-8"));
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testTrustManger() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        TrustManagerFactory managerFactory = TrustManagerFactory.getInstance("X509");
        managerFactory.init(getKeyStore("ssl/tomcat.keystore","jks"));
        TrustManager[] trustManagers = managerFactory.getTrustManagers();
        System.out.println(Arrays.asList(trustManagers));
    }

    @Test
    public void testRSA() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, SignatureException {
        KeyStore keyStore = getKeyStore("ssl/tomcat.keystore","jks");
        Certificate certificate = keyStore.getCertificate("tomcat");

        HexDumpEncoder encoder = new HexDumpEncoder();
        PublicKey publicKey = certificate.getPublicKey();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("tomcat", "123456".toCharArray());
        System.out.println("==================public key=========================");
        System.out.println(encoder.encode(publicKey.getEncoded()));
        System.out.println("====================private key=======================");
        System.out.println(encoder.encode(privateKey.getEncoded()));

//        CertificateFactory certificateFactory = CertificateFactory.getInstance("x.509","SUN");
//        System.out.println(certificateFactory.getProvider().getName());
//        Certificate certificateFromFile = certificateFactory.generateCertificate(new FileInputStream("D:\\programe\\apache-tomcat-9.0.0.M21\\conf\\sslstudy.cer"));
//        Assert.assertEquals(certificate,certificateFromFile);
//        certificate.verify(publicKey);
//
//        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
//        String txt = "123456";
//        byte[] encrypted = cipher.doFinal(txt.getBytes());
////        System.out.println(encoder.encode(encrypted));
//        cipher.init(Cipher.DECRYPT_MODE,privateKey);
//        Assert.assertEquals(txt,new String(cipher.doFinal(encrypted)));

    }

    private KeyStore getKeyStore(String path,String type) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance(type);
        try (InputStream in = this.getClass().getResourceAsStream(path)) {
            keyStore.load(in, "123456".toCharArray());
        }
        return keyStore;
    }

    @Test
    public void testVerify() throws CertificateException, NoSuchProviderException, FileNotFoundException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("x.509", "SUN");
        Certificate certificate = certificateFactory.generateCertificate(new FileInputStream("D:\\ssl\\icloud.cer"));
        Certificate certificateSigner = certificateFactory.generateCertificate(new FileInputStream("D:\\ssl\\signforicloud.cer"));
        certificate.verify(certificateSigner.getPublicKey());
    }
}
