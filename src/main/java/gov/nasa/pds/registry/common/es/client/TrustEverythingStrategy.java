package gov.nasa.pds.registry.common.es.client;

import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TrustEverythingStrategy implements TrustStrategy {
//    public static final org.apache.http.conn.ssl.TrustSelfSignedStrategy INSTANCE = new org.apache.http.conn.ssl.TrustSelfSignedStrategy();

    public TrustEverythingStrategy() {
    }

    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        return Boolean.TRUE;
    }
}
