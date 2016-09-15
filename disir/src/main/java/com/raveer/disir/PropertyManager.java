package com.raveer.disir;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.raveer.disir.singletons.PropertiesContainer;

public class PropertyManager implements Serializable {

	private static final long serialVersionUID = 15L;
	private static final Logger LOGGER = Logger.getLogger(PropertyManager.class.getName());
	private static final String DISIR_PROPERTIES_FILE = "/disir.properties";
	public static final Properties DISIR_DEFAULTS = new Properties();
	
	static {
		
		String propertiesFile = System.getProperty("disir.defaults");
		if (propertiesFile == null) {
			propertiesFile = DISIR_PROPERTIES_FILE;
		} else {
			LOGGER.info("Found command line override for defaults properties file, using: " + propertiesFile);
		}
		
		try (InputStream stream1 = new FileInputStream(propertiesFile)) {
			DISIR_DEFAULTS.load(stream1);
			LOGGER.info("Loaded Disir Defaults from file system: " + propertiesFile);
		} catch (Exception e1) {
			LOGGER.info("Property file not found on file system, trying classpath: " + propertiesFile);
			LOGGER.fine("Error Message: " + e1);
			try (final InputStream stream2 = PropertyManager.class.getResourceAsStream(propertiesFile)) {
				DISIR_DEFAULTS.load(stream2);
				LOGGER.info("Loaded Disir Defaults from classpath: " + propertiesFile);
			} catch (Exception e2) {
				LOGGER.severe("Missing Disir default properties file: " +  propertiesFile + " - Using default configuration");
			}
		}
	}	
	
	public static final Long DEFAULT_CACHE_EXPIRES = Long.valueOf(DISIR_DEFAULTS.getProperty("default.cache_expires", "60000"));
	
	public String DEFAULT_NAMESPACE = DISIR_DEFAULTS.getProperty("default.nameSpace", "APPLICATION");
	public String DEFAULT_JNDI_DB_NAME = DISIR_DEFAULTS.getProperty("default.jndiDBName", "java:comp/env/jdbc/DisirJDBC");
	public String DEFAULT_DB_TABLE_NAME = DISIR_DEFAULTS.getProperty("default.dbTableName", "disir_properties");
	public String DEFAULT_SQL = DISIR_DEFAULTS.getProperty("default.sql", "SELECT key,value FROM %s WHERE nameSpace = ?");
	public String DEFAULT_PROPERTIES_FILE = DISIR_DEFAULTS.getProperty("default.propertiesFile", "/disir.properties");
	public Boolean DEFAULT_PREFER_PROPERTIES_FILE = Boolean.valueOf(DISIR_DEFAULTS.getProperty("default.preferPropertiesFile", "True"));
	
	public String getProperty(String key, String defaultValue){
		Properties localProperty = PropertiesContainer.INSTANCE.getPropertiesByCoordinate(DEFAULT_JNDI_DB_NAME, DEFAULT_DB_TABLE_NAME,
				DEFAULT_SQL, DEFAULT_NAMESPACE, DEFAULT_PROPERTIES_FILE, DEFAULT_PREFER_PROPERTIES_FILE);
		String value = localProperty.getProperty(key);
		if (value==null || value.length()==0) {
			value = defaultValue;
			LOGGER.fine("key ("+key+") not found in NameSpace (" +defaultValue+") using default value: "+value);
		}
		return value;
	}
	
	public String toString() {
		return Arrays.asList(new String[] {DEFAULT_NAMESPACE, DEFAULT_JNDI_DB_NAME, DEFAULT_DB_TABLE_NAME, DEFAULT_SQL, DEFAULT_PROPERTIES_FILE, DEFAULT_PREFER_PROPERTIES_FILE.toString() }).toString();
	}
}
