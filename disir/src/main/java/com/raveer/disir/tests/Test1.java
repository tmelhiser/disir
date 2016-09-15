package com.raveer.disir.tests;

import com.raveer.disir.annotations.PropertyKeyManager;
import com.raveer.disir.annotations.PropertyManager;
import com.raveer.disir.base.PropertyManagerBase;

@PropertyManager(jndiDBName="java:comp/env/jdbc/DisirJDBC",nameSpace="SSL",preferPropertiesFile="false",propertiesFile="/tmp/ssl.properties")
public class Test1 extends PropertyManagerBase {

	private static final long serialVersionUID = 15L;

	@PropertyKeyManager(propertyKey="keyring.hash", defaultValue="SHA-1")
	String keyRingHash;
	
	@PropertyKeyManager(propertyKey="keyring.salt", defaultValue="32")
	String keyRingSalt;
	
	public String getKeyRingHash() {
		return keyRingHash;
	}

	public String getKeyRingSalt() {
		return keyRingSalt;
	}

	public static void main(String args[]) {
		Test1 tests = new Test1();
		System.out.println("Hash Size: " + tests.getKeyRingHash());
		System.out.println("Salt Size: " + tests.getKeyRingSalt());
	}

}