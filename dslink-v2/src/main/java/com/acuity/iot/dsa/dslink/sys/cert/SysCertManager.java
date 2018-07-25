package com.acuity.iot.dsa.dslink.sys.cert;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.format.ResolverStyle;
import java.util.Iterator;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.DSAbstractAction;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSActionValues;
import org.iot.dsa.security.DSPasswordAes128;
import org.iot.dsa.util.DSException;

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
    private static final String GENERATE_CSR = "Generate_Certificate_Signing_Request";

    // Fields
    // ------

    private DSInfo allowClients = getInfo(ALLOW_CLIENTS);
    private DSInfo allowServers = getInfo(ALLOW_SERVERS);
    private DSInfo keystore = getInfo(CERTFILE);
    private DSInfo keystorePass = getInfo(CERTFILE_PASS);
    private DSInfo keystoreType = getInfo(CERTFILE_TYPE);
    private DSInfo generateCSR = getInfo(GENERATE_CSR);
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
        declareDefault(GENERATE_CSR, getGenerateCSRAction());
    }
    
    private DSAbstractAction getGenerateCSRAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((SysCertManager) info.getParent()).generateCSR(info);
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addValueResult("CSR", DSValueType.STRING);
        return act;
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
    
    private ActionResult generateCSR(DSInfo actionInfo) {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            DSException.throwRuntime(e);
            return null;
        }
        keyGen.initialize(2048, new SecureRandom());
        KeyPair pair = keyGen.generateKeyPair();
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
            new X500Principal("CN=dslink-java-v2"), pair.getPublic());
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer;
        try {
            signer = csBuilder.build(pair.getPrivate());
        } catch (OperatorCreationException e) {
            DSException.throwRuntime(e);
            return null;
        }
        PKCS10CertificationRequest csr = p10Builder.build(signer);
        StringWriter str = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(str);
        try {
            pemWriter.writeObject(csr);
        } catch (IOException e) {
            DSException.throwRuntime(e);
            return null;
        } finally {
            try {
                pemWriter.close();
                str.close();
            } catch (IOException e) {
                DSException.throwRuntime(e);
                return null;
            }
        }
        return new DSActionValues(actionInfo.getAction()).addResult(DSString.valueOf(str));
    }

}
