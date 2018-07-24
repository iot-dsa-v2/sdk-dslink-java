package com.acuity.iot.dsa.dslink.sys.cert;

import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.*;

/**
 * Adds support for self signed SSL.  If anonymous is not allowed
 * falls back to the default Java trust manager.
 *
 * @author Aaron Hansen
 */
public class AnonymousTrustFactory extends TrustManagerFactorySpi {

    /////////////////////////////////////////////////////////////////
    // Fields
    /////////////////////////////////////////////////////////////////

    private static X509TrustManager defaultX509Mgr;
    private static SysCertManager certManager;
    private static TrustManager[] trustManagers;

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method TrustAnon.
    /////////////////////////////////////////////////////////////////

    @Override
    public TrustManager[] engineGetTrustManagers() {
        return trustManagers;
    }

    @Override
    public void engineInit(KeyStore ks) {
    }

    @Override
    public void engineInit(ManagerFactoryParameters spec) {
    }

    /**
     * Captures the default trust factory and installs this one.
     */
    static void init(SysCertManager mgr) {
        certManager = mgr;
        try {
            TrustManagerFactory fac = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            fac.init((KeyStore) null);
            trustManagers = fac.getTrustManagers();
            if (trustManagers == null) {
                trustManagers = new TrustManager[]{new MyTrustManager()};
                return;
            }
            TrustManager tm;
            for (int i = 0, len = trustManagers.length; i < len; i++) {
                tm = trustManagers[i];
                if (tm instanceof X509TrustManager) {
                    defaultX509Mgr = (X509TrustManager) tm;
                    trustManagers[i] = new MyTrustManager();
                    break;
                }
            }
            if (defaultX509Mgr == null) {
                List<TrustManager> list = Arrays.asList(trustManagers);
                list.add(new MyTrustManager());
                trustManagers = list.toArray(new TrustManager[list.size()]);
            }
        } catch (Exception x) {
            certManager.error(certManager.getPath(), x);
        }
        try {
            Thread.currentThread().setContextClassLoader(
                    AnonymousTrustFactory.class.getClassLoader());
            System.setProperty("jsse.enableSNIExtension", "false");
            Security.setProperty("ssl.TrustManagerFactory.algorithm", "DSA_X509");
            Security.addProvider(new MyProvider());
        } catch (Exception x) {
            certManager.error(certManager.getPath(), x);
        }
    }


    /////////////////////////////////////////////////////////////////
    // Inner Classes - in alphabetical order by class TrustAnon.
    /////////////////////////////////////////////////////////////////

    /**
     * The hook that provides the anonymous trust factory.
     */
    private static class MyProvider extends Provider {

        public MyProvider() {
            super("DSAP", 1.0d, "DSA Provider");
            put("TrustManagerFactory.DSA_X509",
                "com.acuity.iot.dsa.dslink.sys.cert.AnonymousTrustFactory");
        }

    }

    /**
     * Checks with the SysCertService to see if self signed certificates are allowed.
     */
    private static class MyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            if (certManager.allowAnonymousClients()) {
                return;
            }
            if (certManager.isInTrustStore(chain[0])) {
                return;
            }
            if (defaultX509Mgr != null) {
                try {
                    defaultX509Mgr.checkClientTrusted(chain, authType);
                } catch (CertificateException e) {
                    certManager.addToQuarantine(chain[0]);
                    throw e;
                }
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            if (certManager.allowAnonymousServers()) {
                return;
            }
            if (certManager.isInTrustStore(chain[0])) {
                return;
            }
            if (defaultX509Mgr != null) {
                try {
                    defaultX509Mgr.checkServerTrusted(chain, authType);
                } catch (CertificateException e) {
                    certManager.addToQuarantine(chain[0]);
                    throw e;
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            if (defaultX509Mgr != null) {
                return defaultX509Mgr.getAcceptedIssuers();
            }
            return new X509Certificate[0];
        }

    }

}
