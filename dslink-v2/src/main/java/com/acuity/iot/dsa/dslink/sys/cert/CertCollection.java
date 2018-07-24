package com.acuity.iot.dsa.dslink.sys.cert;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSNode;

public class CertCollection extends DSNode {

    public void addCertificate(X509Certificate cert) throws CertificateEncodingException {
        String name = certToName(cert);
        addCertificate(name, encodeCertificate(cert));
    }
    
    public void addCertificate(String name, String cert) {
        put(name, new CertNode().updateValue(cert));
    }
    
    public boolean containsCertificate(X509Certificate cert) {
        DSIObject obj = get(certToName(cert));
        String certStr;
        try {
            certStr = encodeCertificate(cert);
        } catch (CertificateEncodingException e) {
            warn(e);
            return false;
        }
        return obj != null && obj instanceof CertNode && certStr.equals(((CertNode) obj).toElement().toString());
    }
    
    public static String certToName(X509Certificate cert) {
        return cert.getIssuerX500Principal().getName() + "-" + Integer.toHexString(cert.hashCode());
    }
    
    public static String encodeCertificate(X509Certificate cert) throws CertificateEncodingException {
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(cert.getEncoded());
    }

}
