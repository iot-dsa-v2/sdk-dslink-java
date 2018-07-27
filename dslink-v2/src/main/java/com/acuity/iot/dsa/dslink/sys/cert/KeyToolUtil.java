package com.acuity.iot.dsa.dslink.sys.cert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.time.DSTime;

public class KeyToolUtil extends DSLogger {
	
	private static KeyToolUtil inst = new KeyToolUtil();
	private KeyToolUtil() {
		
	}
	
	private void executeCommand(String[] cmd) {
		try {
			ProcessBuilder builder = new ProcessBuilder();
	        Process process = builder.command(cmd).start();
	        process.waitFor();
		} catch (Exception e) {
			error("", e);
		}
	}
	
	public static void generateSelfSigned(String keystore, String password) {
		String[] cmd = new String[]{
                "keytool",
                "-genkey",
                "-keystore", keystore,
                "-storepass", password,
                "-keypass", password,
                "-alias", "dsa",
                "-keyalg", "RSA",
                "-validity", "18000",
                "-dname", "\"CN=dslink-java-v2, O=DSA, C=US\""
        };
		inst.executeCommand(cmd);
	}
	
	public static String generateCSR(String keystore) throws IOException {
		String filename = "dsa.csr";
		String[] cmd = new String[]{
                "keytool",
                "-certreq",
                "-keystore", keystore,
                "-alias", "dsa",
                "-keyalg", "RSA",
                "-validity", "18000",
                "-dname", "\"CN=dslink-java-v2, O=DSA, C=US\"",
                "-file", filename
        };
		inst.executeCommand(cmd);
		return new String(Files.readAllBytes(Paths.get(filename)));
	}
	
	public static void importCACert(String keystore, String certStr, String alias) throws IOException {
		String filename = DSTime.encodeForFiles(DSTime.getCalendar(System.currentTimeMillis()), new StringBuilder("tempCACert")).toString();
		Files.write(Paths.get(filename), certStr.getBytes());
		String[] cmd = new String[]{
                "keytool",
                "-import",
                "-trustcacerts",
                "-keystore", keystore,
                "-alias", alias,
                "-file", filename
        };
		inst.executeCommand(cmd);
		
		new File(filename).delete();
	}
	
	public static void importPrimaryCert(String keystore, String certStr) throws IOException {
		String filename = DSTime.encodeForFiles(DSTime.getCalendar(System.currentTimeMillis()), new StringBuilder("tempCert")).toString();
		Files.write(Paths.get(filename), certStr.getBytes());
		String[] cmd = new String[]{
                "keytool",
                "-import",
                "-trustcacerts",
                "-keystore", keystore,
                "-alias", "dsa",
                "-file", filename
        };
		inst.executeCommand(cmd);
		
		new File(filename).delete();
	}

}
