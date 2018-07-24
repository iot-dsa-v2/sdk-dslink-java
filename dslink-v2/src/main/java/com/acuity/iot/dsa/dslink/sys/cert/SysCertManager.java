package com.acuity.iot.dsa.dslink.sys.cert;

import java.io.File;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.security.DSPasswordAes128;

/**
 * Certificate management for the whole process.  This is basically a stub for future
 * functionality.  Right now it generates a self signed cert for the link as well
 * as accepts self signed (anonymous) certs from the broker.
 *
 * @author Aaron Hansen
 */
public class SysCertManager extends DSNode {

    // Constants
    // ---------

    private static final String ALLOW_CLIENTS = "Allow_Anonymous_Clients";
    private static final String ALLOW_SERVERS = "Allow_Anonymous_Servers";
    private static final String CERTFILE = "Cert_File";
    private static final String CERTFILE_PASS = "Cert_File_Pass";
    private static final String CERTFILE_TYPE = "Cert_File_Type";
    private static final String LOCAL_TRUSTSTORE = "Local_Truststore";
    private static final String QUARANTINE = "Quarantine";

    // Fields
    // ------

    private DSInfo allowClients = getInfo(ALLOW_CLIENTS);
    private DSInfo allowServers = getInfo(ALLOW_SERVERS);
    private DSInfo keystore = getInfo(CERTFILE);
    private DSInfo keystorePass = getInfo(CERTFILE_PASS);
    private DSInfo keystoreType = getInfo(CERTFILE_TYPE);
    private CertCollection localTruststore; 
    private CertCollection quarantine;

    private CertCollection getLocalTruststore() {
        if (localTruststore == null) {
            localTruststore = (CertCollection) getInfo(LOCAL_TRUSTSTORE).getObject();
        }
        return localTruststore;
    }
    
    private CertCollection getQuarantine() {
        if (quarantine == null) {
            quarantine = (CertCollection) getInfo(QUARANTINE).getObject();
        }
        return quarantine;
    }
    
    // Methods
    // -------

    /**
     * True if self signed anonymous client certs are allowed.
     */
    public boolean allowAnonymousClients() {
        return allowClients.getElement().toBoolean();
    }

    /**
     * True if self signed anonymous server certs are allowed.
     */
    public boolean allowAnonymousServers() {
        return allowServers.getElement().toBoolean();
    }

    @Override
    public void declareDefaults() {
        declareDefault(ALLOW_CLIENTS, DSBool.FALSE);
        declareDefault(ALLOW_SERVERS, DSBool.TRUE);
        declareDefault(CERTFILE, DSString.valueOf("dslink.jks"));
        declareDefault(CERTFILE_TYPE, DSString.valueOf("JKS"));
        declareDefault(CERTFILE_PASS, DSPasswordAes128.valueOf("dsarocks"));
        declareDefault(LOCAL_TRUSTSTORE, new CertCollection());
        declareDefault(QUARANTINE, new CertCollection()).setTransient(true);
    }

    private String getCertFilePass() {
        DSPasswordAes128 pass = (DSPasswordAes128) keystorePass.getObject();
        return pass.decode();
    }

    /**
     * Executes the java keytool to generate a new self signed cert.
     */
    private void keytoolGenkey() {
        try {
            String pass = getCertFilePass();
            String[] cmd = new String[]{
                    "keytool",
                    "-genkey",
                    "-keystore", keystore.getElement().toString(),
                    "-storepass", pass,
                    "-keypass", pass,
                    "-alias", "dsa",
                    "-keyalg", "RSA",
                    "-validity", "18000",
                    "-dname", "\"CN=dslink-java-v2, O=DSA, C=US\""
            };
            ProcessBuilder builder = new ProcessBuilder();
            Process process = builder.command(cmd).start();
            process.waitFor();
        } catch (Exception x) {
            error(getPath(), x);
        }
    }

    @Override
    public void onStarted() {
        AnonymousTrustFactory.init(this);
        String keystore = this.keystore.getElement().toString();
        File f = new File(keystore);
        if (!f.exists()) {
            keytoolGenkey();
        }
        try {
            System.setProperty("javax.net.ssl.keyStore", keystore);
            System.setProperty("javax.net.ssl.keyStoreType",
                               keystoreType.getElement().toString());
            System.setProperty("javax.net.ssl.keyStorePassword", getCertFilePass());
        } catch (Exception x) {
            error(getParent(), x);
        }
    }

    public boolean isInTrustStore(X509Certificate cert) {
        return getLocalTruststore().containsCertificate(cert);
    }
    
    public void addToQuarantine(X509Certificate cert) {
        try {
            getQuarantine().addCertificate(cert);
        } catch (CertificateEncodingException e) {
            error("", e);
        }
    }
    
    public void allow(DSInfo certInfo) {
        String name = certInfo.getName();
        CertNode certNode = (CertNode) certInfo.getNode();
        String certStr = certNode.toElement().toString();
        getQuarantine().remove(certInfo);
        getLocalTruststore().addCertificate(name, certStr);
    }
    

}
