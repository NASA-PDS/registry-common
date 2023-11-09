package gov.nasa.pds.registry.common.es.client;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import gov.nasa.pds.registry.common.util.JavaProps;


/**
 * TLS/SSL utility methods.
 * 
 * @author karpenko
 */
public class SSLUtils
{
    /**
     * Create "trust all" SSL context to support self-signed certificates.
     * @return SSL context object
     * @throws Exception an exception
     */
    public static SSLContext createTrustAllContext(JavaProps props) throws Exception {
      SSLContext context = SSLContext.getInstance("TLS");
      File certificateFile = new File(props.getProperty("ssl.self.signed.cert"));
      // Create a `KeyStore` with default type
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      // `keyStore` is initially empty
      keyStore.load(null, null);
      X509Certificate generatedCertificate;
      try (InputStream cert = new FileInputStream(certificateFile)) {
          generatedCertificate = (X509Certificate) CertificateFactory.getInstance("X509")
                  .generateCertificate(cert);
      }
      // Add the self-signed certificate to the key store
      keyStore.setCertificateEntry(certificateFile.getName(), generatedCertificate);
      // Get default `TrustManagerFactory`
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      // Use it with our key store that trusts our self-signed certificate
      tmf.init(keyStore);
      TrustManager[] trustManagers = tmf.getTrustManagers();
      context.init(null, trustManagers, null);
      return context;
    }
}
