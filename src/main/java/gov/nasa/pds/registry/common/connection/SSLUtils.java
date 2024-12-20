package gov.nasa.pds.registry.common.connection;


import java.security.SecureRandom;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManagerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


/**
 * TLS/SSL utility methods.
 * 
 * @author karpenko
 */
class SSLUtils
{
    /**
     * Create "trust all" SSL context to support self-signed certificates.
     * @return SSL context object
     * @throws Exception an exception
     */
    public static SSLContext createTrustAllContext() throws Exception
    {
        // Load the self-signed certificate
        File certificateFile = new File("path/to/self-signed-certificate");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        X509Certificate generatedCertificate;
        try (InputStream cert = new FileInputStream(certificateFile)) {
            generatedCertificate = (X509Certificate) CertificateFactory.getInstance("X509")
                    .generateCertificate(cert);
        }
        keyStore.setCertificateEntry(certificateFile.getName(), generatedCertificate);
        
        // Initialize TrustManagerFactory with the KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        TrustManager[] trustManagers = tmf.getTrustManagers();
        
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustManagers, new SecureRandom());
        return sc;
    }
}
