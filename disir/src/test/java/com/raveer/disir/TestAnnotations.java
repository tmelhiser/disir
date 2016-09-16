package com.raveer.disir;

import static org.assertj.core.api.StrictAssertions.assertThat;

import org.junit.Test;

import com.raveer.disir.annotations.PropertyManagerDefaults;
import com.raveer.disir.annotations.PropertyManagerFields;
import com.raveer.disir.base.PropertyManagerBase;

@PropertyManagerDefaults(jndiDBName="java:comp/env/jdbc/DisirJDBC",nameSpace="SSL",preferPropertiesFile="false",propertiesFile="/tmp/ssl.properties")
public class TestAnnotations extends PropertyManagerBase {

	private static final long serialVersionUID = 15L;

	@PropertyManagerFields(propertyKey="keyring.hash", defaultValue="SHA-1")
	String keyRingHash;
	
	@PropertyManagerFields(propertyKey="keyring.salt", defaultValue="32")
	String keyRingSalt;
	
	@Test
	public void testFieldAnnotation() {
		assertThat(keyRingHash).contains("SHA-1");
		assertThat(keyRingSalt).contains("32");
	}
}