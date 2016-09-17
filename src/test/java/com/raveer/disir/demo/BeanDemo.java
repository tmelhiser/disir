package com.raveer.disir.demo;

import com.raveer.disir.beans.PropertyManager;

public class BeanDemo {
	private static final PropertyManager pm = new PropertyManager();
	static {
		pm.DEFAULT_JNDI_DB_NAME = "java:comp/env/jdbc/DisirJDBC";
		pm.DEFAULT_NAMESPACE = "SSL";
		pm.DEFAULT_PREFER_PROPERTIES_FILE = false;
		pm.DEFAULT_PROPERTIES_FILE = "/tmp/ssl.properties";
	}

	private String keyRingHash = pm.getProperty("keyring.hash", "SHA-1");
	private String keyRingSalt = pm.getProperty("keyring.salt", "32");

	public String getKeyRingHash() {
		return keyRingHash;
	}

	public String getKeyRingSalt() {
		return keyRingSalt;
	}

	public static void main(String args[]) {
		AnnotationDemo tests = new AnnotationDemo();
		System.out.println("Hash Size: " + tests.getKeyRingHash());
		System.out.println("Salt Size: " + tests.getKeyRingSalt());
	}
}