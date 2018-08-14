package com.acuity.iot.dsa.dslink.sys.cert;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
//import org.bouncycastle.asn1.ASN1ObjectIdentifier;
//import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.asn1.x509.BasicConstraints;
//import org.bouncycastle.cert.CertIOException;
//import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
//import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
//import org.bouncycastle.operator.ContentSigner;
//import org.bouncycastle.operator.OperatorCreationException;
//import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
//import org.bouncycastle.pkcs.PKCS10CertificationRequest;
//import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
//import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.DSAbstractAction;
import org.iot.dsa.node.action.DSActionValues;
import org.iot.dsa.security.DSPasswordAes128;
import org.iot.dsa.util.DSException;
import com.acuity.iot.dsa.dslink.sys.cert.HostnameWhitelist.WhitelistValue;

/**
 * Certificate management for the whole process.  This is basically a stub for future
 * functionality.  Right now it generates a self signed cert for the link as well
 * as accepts self signed (anonymous) certs from the broker.
 *
 * @author Aaron Hansen
 * @author Daniel Shapiro
 */
public class SysCertManager extends DSNode {

    // Constants
    // ---------

    private static final String ALLOW_CLIENTS = "Allow Anonymous Clients";
    private static final String ALLOW_SERVERS = "Allow Anonymous Servers";
    private static final String VERIFY_HOSTNAMES = "Enable Hostname-Certificate Verification";
    private static final String HOSTNAME_WHITELIST = "Hostname Whitelist";
    private static final String CERTFILE = "Cert File";
    private static final String CERTFILE_PASS = "Cert File Pass";
    private static final String CERTFILE_TYPE = "Cert File Type";
    private static final String LOCAL_TRUSTSTORE = "Local Truststore";
    private static final String QUARANTINE = "Quarantine";
    private static final String GENERATE_CSR = "Generate Certificate Signing Request";
    private static final String IMPORT_CA_CERT = "Import CA Certificate";
    private static final String IMPORT_PRIMARY_CERT = "Import Primary Certificate";
    private static final String GENERATE_SELF_SIGNED = "Generate Self-Signed Certificate";
    private static final String DELETE_KS_ENTRY = "Delete Keystore Entry";
    private static final String GET_KS_ENTRY = "Get Keystore Entry";

    // Fields
    // ------

    private DSInfo allowClients = getInfo(ALLOW_CLIENTS);
    private DSInfo allowServers = getInfo(ALLOW_SERVERS);
    private DSInfo verifyHostnames = getInfo(VERIFY_HOSTNAMES);
    private DSInfo keystorePath = getInfo(CERTFILE);
    private DSInfo keystorePass = getInfo(CERTFILE_PASS);
    private DSInfo keystoreType = getInfo(CERTFILE_TYPE);
    private CertCollection localTruststore; 
    private CertCollection quarantine;
    private HostnameWhitelist whitelist;
    private static SysCertManager inst;
    
    private static HostnameVerifier oldHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    private HostnameVerifier hostnameVerifier = new SysHostnameVerifier();
    
    public SysCertManager() {
    }
    
    public static SysCertManager getInstance() {
        return inst;
    }
    
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

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
    
    private HostnameWhitelist getHostnameWhitelist() {
        if (whitelist == null) {
            whitelist = (HostnameWhitelist) getInfo(HOSTNAME_WHITELIST).getObject();
        }
        return whitelist;
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
    
    public boolean hostnameVerificationEnabled() {
        return verifyHostnames.getElement().toBoolean();
    }

    @Override
    public void declareDefaults() {
        declareDefault(ALLOW_CLIENTS, DSBool.FALSE);
        declareDefault(ALLOW_SERVERS, DSBool.TRUE);
        declareDefault(VERIFY_HOSTNAMES, DSBool.TRUE);
        declareDefault(HOSTNAME_WHITELIST, new HostnameWhitelist());
        declareDefault(CERTFILE, DSString.valueOf("dslink.jks"));
        declareDefault(CERTFILE_TYPE, DSString.valueOf("JKS"));
        declareDefault(CERTFILE_PASS, DSPasswordAes128.valueOf("dsarocks"));
        declareDefault(LOCAL_TRUSTSTORE, new CertCollection());
        declareDefault(QUARANTINE, new CertCollection()).setTransient(true);
        declareDefault(GENERATE_CSR, getGenerateCSRAction());
        declareDefault(IMPORT_CA_CERT, getImportCACertAction());
        declareDefault(IMPORT_PRIMARY_CERT, getImportPrimaryCertAction());
        declareDefault(GENERATE_SELF_SIGNED, getGenerateSelfSignedAction());
        declareDefault(GET_KS_ENTRY, getGetKSEntryAction());
        declareDefault(DELETE_KS_ENTRY, getDeleteKSEntryAction());
    }
    
    private DSAbstractAction getGenerateCSRAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                String csr = ((SysCertManager) info.getParent()).generateCSR();
                return csr != null ? new DSActionValues(info.getAction()).addResult(DSString.valueOf(csr)) : null;
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addValueResult("CSR", DSValueType.STRING).setEditor("textarea");
        return act;
    }
    
    private String generateCSR() {
        try {
            return KeyToolUtil.generateCSR(getKeystorePath(), getCertFilePass());
        } catch (IOException e) {
            DSException.throwRuntime(e);
            return null;
        }
    }
    
    private DSAbstractAction getImportCACertAction() {
    	DSAbstractAction act = new DSAbstractAction() {
			
			@Override
			public void prepareParameter(DSInfo info, DSMap parameter) {				
			}
			
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				DSMap parameters = invocation.getParameters();
				((SysCertManager) info.getParent()).importCACert(parameters);
				return null;
			}
		};
		act.addParameter("Alias", DSValueType.STRING, null);
		act.addParameter("Certificate", DSValueType.STRING, null).setEditor("textarea");
		return act;
    }
    
    private void importCACert(DSMap parameters) {
        String alias = parameters.getString("Alias");
        String certStr = parameters.getString("Certificate");
        try {
            KeyToolUtil.importCACert(getKeystorePath(), certStr, alias, getCertFilePass());
        } catch (IOException e) {
            DSException.throwRuntime(e);
        }
    }
    
    private DSAbstractAction getImportPrimaryCertAction() {
    	DSAbstractAction act = new DSAbstractAction() {
			
			@Override
			public void prepareParameter(DSInfo info, DSMap parameter) {				
			}
			
			@Override
			public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
				DSMap parameters = invocation.getParameters();
				((SysCertManager) info.getParent()).importPrimaryCert(parameters);
				return null;
			}
		};
		act.addParameter("Certificate", DSValueType.STRING, null).setEditor("textarea");
		return act;
    }
    
    private void importPrimaryCert(DSMap parameters) {
        String certStr = parameters.getString("Certificate");
        try {
            KeyToolUtil.importPrimaryCert(getKeystorePath(), certStr, getCertFilePass());
        } catch (IOException e) {
            DSException.throwRuntime(e);
        }
    }
    
    private DSAbstractAction getGenerateSelfSignedAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {                
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((SysCertManager) info.getParent()).keytoolGenkey();
                return null;
            }
        };
        return act;
    }
    
    private DSAbstractAction getGetKSEntryAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {                
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                String result = ((SysCertManager) info.getParent()).getKSEntry();
                return new DSActionValues(info.getAction()).addResult(DSString.valueOf(result));
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addValueResult("Entry", DSValueType.STRING).setEditor("textarea");
        return act;
    }
    
    private String getKSEntry() {
        return KeyToolUtil.getEntry(getKeystorePath(), getCertFilePass());
    }
    
    private DSAbstractAction getDeleteKSEntryAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((SysCertManager) info.getParent()).deleteKSEntry();
                return null;
            }
        };
        return act;
    }
    
    private void deleteKSEntry() {
        KeyToolUtil.deleteEntry(getKeystorePath(), getCertFilePass());
    }

    private String getCertFilePass() {
        DSPasswordAes128 pass = (DSPasswordAes128) keystorePass.getObject();
        return pass.decode();
    }
    
    private String getKeystorePath() {
        return keystorePath.getElement().toString();
    }

    /**
     * Executes the java keytool to generate a new self signed cert.
     */
    private void keytoolGenkey() {
        KeyToolUtil.generateSelfSigned(getKeystorePath(), getCertFilePass());
    }

    @Override
    public void onStarted() {
        inst = this;
        AnonymousTrustFactory.init(this);
        String keystore = this.keystorePath.getElement().toString();
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
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
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
    
    private class SysHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            if (getHostnameWhitelist().isEnabled()) {
                WhitelistValue wlval = getHostnameWhitelist().checkHostname(hostname);
                if (wlval != null) {
                    switch (wlval) {
                        case ALLOWED:
                            return true;
                        case FORBIDDEN:
                            return false;
                    }
                }
            }
            if (hostnameVerificationEnabled()) {
                return oldHostnameVerifier.verify(hostname, session);
            } else {
                return true;
            }
        }
    }

}
