package com.raveer.disir.tests;

import com.raveer.disir.PropertyManager;

public class Test2 {
	public static void main(String[] args) {
		PropertyManager pm = new PropertyManager();
		pm.DEFAULT_JNDI_DB_NAME="java:comp/env/jdbc/DisirJDBC";
		pm.DEFAULT_NAMESPACE="SSL";
		pm.DEFAULT_PREFER_PROPERTIES_FILE=false;
		pm.DEFAULT_PROPERTIES_FILE="/tmp/ssl.properties";
		
		System.out.println("Hash Size: " + pm.getProperty("keyring.hash", "SHA-1"));
		System.out.println("Salt Size: " + pm.getProperty("keyring.salt", "SHA-32"));
	}
}
