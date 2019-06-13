package com.acuity.iot.dsa.dslink.sys.cert;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.time.DSTime;
import org.iot.dsa.util.DSException;

/**
 * @author Daniel Shapiro
 */
public class CertCollection extends DSNode {
    
    private static final String ADD_CERT = "Add Certificate";
    private static final String CERT = "Certificate";
    
    private CertificateFactory certFactory;
    
    private CertificateFactory getCertFactory() {
        if (certFactory == null) {
            try {
                certFactory = CertificateFactory.getInstance("X.509");
            } catch (CertificateException e) {
                warn("", e);
            }
        }
        return certFactory;
    }

    public void addCertificate(X509Certificate cert) throws CertificateEncodingException {
        String name = certToName(cert);
        addCertificate(name, encodeCertificate(cert));
    }

    public void addCertificate(String name, String cert) {
        CertNode certNode = new CertNode().updateValue(cert);
        put(name, certNode);
        try {
            certNode.getCertManager().onCertAddedToCollection(this, name, certFromString(cert));
        } catch (CertificateException e) {
            warn("", e);
        }
    }

    public static String certToName(X509Certificate cert) {
        return DSTime.encodeForFiles(DSTime.getCalendar(System.currentTimeMillis()),
                                     new StringBuilder(cert.getIssuerX500Principal().getName()))
                     .toString();
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
        return obj != null && obj instanceof CertNode && certStr
                .equals(((CertNode) obj).toElement().toString());
    }
    
    public Map<String, X509Certificate> getCertificates() {
        Map<String, X509Certificate> certs = new HashMap<String, X509Certificate>();
        for (DSInfo info: this) {
            DSIObject obj = info.get();
            if (obj instanceof CertNode) {
                String certStr = ((CertNode) obj).toElement().toString();
                X509Certificate cert;
                try {
                    cert = certFromString(certStr);
                    certs.put(info.getName(), cert);
                } catch (CertificateException e) {
                    warn("", e);
                }
            }
        }
        return certs;
    }

    public static String encodeCertificate(X509Certificate cert)
            throws CertificateEncodingException {
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(cert.getEncoded());
    }
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(ADD_CERT, makeAddCertAction());
    }
    
    private DSAction makeAddCertAction() {
        DSAction act = new DSAction.Parameterless() {
            
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation request) {
                ((CertCollection) target.get()).addCert(request.getParameters());
                return null;
            }
        };
        act.addParameter(CERT, DSValueType.STRING, null).setEditor(DSMetadata.STR_EDITOR_TEXT_AREA);
        return act;
    }
    
    private void addCert(DSMap parameters) {
        String certStr = parameters.getString(CERT);
        try {
            X509Certificate cert = certFromString(certStr);
            addCertificate(cert);
        } catch (CertificateException e) {
            warn("", e);
            DSException.throwRuntime(e);
        }
    }
    
    private X509Certificate certFromString(String certStr) throws CertificateException {
        certStr = certStr.trim();
        if (!certStr.startsWith("-----BEGIN CERTIFICATE-----")) {
            certStr = "-----BEGIN CERTIFICATE-----\n" + certStr;
        }
        if (!certStr.endsWith("-----END CERTIFICATE-----")) {
            certStr = certStr + "\n-----END CERTIFICATE-----";
        }
        return (X509Certificate) getCertFactory().generateCertificate(new ByteArrayInputStream(certStr.getBytes()));
    }

}
