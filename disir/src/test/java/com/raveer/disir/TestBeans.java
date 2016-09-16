package com.raveer.disir;

import static org.assertj.core.api.StrictAssertions.assertThat;

import org.junit.Test;

import com.raveer.disir.PropertyManager;

public class TestBeans {
	private static final PropertyManager pm = new PropertyManager();
	static {
		pm.DEFAULT_JNDI_DB_NAME="java:comp/env/jdbc/DisirJDBC";
		pm.DEFAULT_NAMESPACE="SSL";
		pm.DEFAULT_PREFER_PROPERTIES_FILE=false;
		pm.DEFAULT_PROPERTIES_FILE="/tmp/ssl.properties";
	}
	
	private String keyRingHash = pm.getProperty("keyring.hash", "SHA-1");
	private String keyRingSalt = pm.getProperty("keyring.salt", "SHA-32");
	
	@Test
	public void testBeanPropertyManager() {
		assertThat(keyRingHash).contains("SHA-1");
		assertThat(keyRingSalt).contains("32");
	}
}
