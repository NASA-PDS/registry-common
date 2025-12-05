package gov.nasa.pds.registry.common.connection;


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;


/**
 * This class is used for HTTPS connection configuration
 * to support self-signed certificates.
 */
class TrustAllManager implements X509TrustManager
{
    private X509Certificate[] certs;
    
    
    public TrustAllManager()
    {
        certs = new X509Certificate[0];
    }

    
    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return certs;
    }

}
