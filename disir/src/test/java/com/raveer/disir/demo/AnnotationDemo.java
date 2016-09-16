package com.raveer.disir.demo;

import com.raveer.disir.annotations.PropertyManagerDefaults;
import com.raveer.disir.annotations.PropertyManagerFields;
import com.raveer.disir.base.PropertyManagerBase;

@PropertyManagerDefaults(jndiDBName = "java:comp/env/jdbc/DisirJDBC", nameSpace = "SSL", preferPropertiesFile = "false", propertiesFile = "/tmp/ssl.properties")
public class AnnotationDemo extends PropertyManagerBase {
	private static final long serialVersionUID = 1L;
	@PropertyManagerFields(propertyKey = "keyring.hash", defaultValue = "SHA-256")
	String keyRingHash;
	@PropertyManagerFields(propertyKey = "keyring.salt", defaultValue = "128")
	String keyRingSalt;

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
