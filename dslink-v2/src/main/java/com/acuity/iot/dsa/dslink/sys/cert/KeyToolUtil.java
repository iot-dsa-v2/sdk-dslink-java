package com.acuity.iot.dsa.dslink.sys.cert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.time.Time;


/**
 * @author Daniel Shapiro
 */
public class KeyToolUtil extends DSLogger {

    private static KeyToolUtil inst = new KeyToolUtil();

    private KeyToolUtil() {

    }

    public static String deleteEntry(String keystore, String password) {
        String[] cmd = new String[]{
                "keytool",
                "-delete",
                "-keystore", keystore,
                "-storepass", password,
                "-alias", "org/iot/dsa",
        };
        return inst.executeCommand(cmd);
    }

    public static String[] generateCSR(String keystore, String password) throws IOException {
        String filename = "dsa.csr";
        String[] cmd = new String[]{
                "keytool",
                "-certreq",
                "-keystore", keystore,
                "-storepass", password,
                "-alias", "org/iot/dsa",
                "-keyalg", "RSA",
                "-validity", "18000",
                "-dname", "CN=dslink-java-v2, O=DSA, C=US",
                "-file", filename
        };
        String result = inst.executeCommand(cmd);
        String csr = new String(Files.readAllBytes(Paths.get(filename)));
        return new String[]{result, csr};
    }

    public static String generateSelfSigned(String keystore, String password) {
        String[] cmd = new String[]{
                "keytool",
                "-genkey",
                "-keystore", keystore,
                "-storepass", password,
                "-keypass", password,
                "-alias", "org/iot/dsa",
                "-keyalg", "RSA",
                "-validity", "18000",
                "-dname", "CN=dslink-java-v2, O=DSA, C=US"
        };
        return inst.executeCommand(cmd);
    }

    public static String getEntry(String keystore, String password) {
        String[] cmd = new String[]{
                "keytool",
                "-list",
                "-v",
                "-keystore", keystore,
                "-storepass", password,
                "-alias", "org/iot/dsa",
        };
        return inst.executeCommand(cmd);
    }

    public static String help() {
        String[] cmd = new String[]{
                "keytool",
                "-help"
        };
        return inst.executeCommand(cmd);
    }

    public static String importCACert(String keystore, String certStr, String alias,
                                      String password) throws IOException {
        String filename = Time.encodeForFiles(Time.getCalendar(System.currentTimeMillis()),
                                              new StringBuilder("tempCACert")).toString();
        Files.write(Paths.get(filename), certStr.getBytes());
        String[] cmd = new String[]{
                "keytool",
                "-import",
                "-trustcacerts",
                "-keystore", keystore,
                "-storepass", password,
                "-alias", alias,
                "-file", filename
        };
        String result = inst.executeCommand(cmd);

        new File(filename).delete();
        return result;
    }

    public static String importPrimaryCert(String keystore, String certStr, String password)
            throws IOException {
        String filename = Time.encodeForFiles(Time.getCalendar(System.currentTimeMillis()),
                                              new StringBuilder("tempCert")).toString();
        Files.write(Paths.get(filename), certStr.getBytes());
        String[] cmd = new String[]{
                "keytool",
                "-import",
                "-trustcacerts",
                "-keystore", keystore,
                "-storepass", password,
                "-alias", "org/iot/dsa",
                "-file", filename
        };
        String result = inst.executeCommand(cmd);

        new File(filename).delete();
        return result;
    }

    private String executeCommand(String[] cmd) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            Process process = builder.command(cmd).redirectErrorStream(true).start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } catch (Exception e) {
            error("", e);
            return "";
        }
    }

}
