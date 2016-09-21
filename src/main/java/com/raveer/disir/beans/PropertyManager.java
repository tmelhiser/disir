/*
 * This file is part of FastClasspathScanner.
 * 
 * Author: Travis Melhiser
 * 
 * Hosted at: https://github.com/tmelhiser/disir
 * 
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Travis Melhiser
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.raveer.disir.beans;

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
	public String DEFAULT_PROPERTIES_FILE = DISIR_DEFAULTS.getProperty("default.propertiesFile", "/app.properties");
	public Boolean DEFAULT_PREFER_PROPERTIES_FILE = Boolean.valueOf(DISIR_DEFAULTS.getProperty("default.preferPropertiesFile", "True"));
	
	private String DEFAULT_JNDI_DB_NAME = DISIR_DEFAULTS.getProperty("default.jndiDBName", "java:comp/env/jdbc/DisirJDBC");
	private String DEFAULT_DB_TABLE_NAME = DISIR_DEFAULTS.getProperty("default.dbTableName", "disir_properties");
	private String DEFAULT_SQL_ROW    = DISIR_DEFAULTS.getProperty("default.sqlRow", "SELECT id,namespace,key,value FROM %s WHERE nameSpace = ? AND key = ?");
	private String DEFAULT_SQL_SELECT = DISIR_DEFAULTS.getProperty("default.sqlSelect", "SELECT id,namespace,key,value FROM %s");
	private String DEFAULT_SQL_UPDATE = DISIR_DEFAULTS.getProperty("default.sqlUpdate", "UPDATE %s SET namespace = ?, key=?, value = ? WHERE id = ?");
	private String DEFAULT_SQL_DELETE = DISIR_DEFAULTS.getProperty("default.sqlDelete", "DELETE FROM %s WHERE id = ?");
	private String DEFAULT_SQL_INSERT = DISIR_DEFAULTS.getProperty("default.sqlInsert", "INSERT INTO %s (NAMESPACE,KEY,VALUE) VALUES (?,?,?)");
	
	public String getDEFAULT_JNDI_DB_NAME() {
		return DEFAULT_JNDI_DB_NAME;
	}

	public String getDEFAULT_DB_TABLE_NAME() {
		return DEFAULT_DB_TABLE_NAME;
	}

	public String getDEFAULT_SQL_ROW() {
		return DEFAULT_SQL_ROW;
	}

	public String getDEFAULT_SQL_SELECT() {
		return DEFAULT_SQL_SELECT;
	}

	public String getDEFAULT_SQL_UPDATE() {
		return DEFAULT_SQL_UPDATE;
	}

	public String getDEFAULT_SQL_DELETE() {
		return DEFAULT_SQL_DELETE;
	}

	public String getDEFAULT_SQL_INSERT() {
		return DEFAULT_SQL_INSERT;
	}

	public String getProperty(String key, String defaultValue){
		Properties localProperty = PropertiesContainer.INSTANCE.getPropertiesByCoordinate(DEFAULT_JNDI_DB_NAME, DEFAULT_DB_TABLE_NAME,
				DEFAULT_SQL_SELECT, DEFAULT_NAMESPACE, DEFAULT_PROPERTIES_FILE, DEFAULT_PREFER_PROPERTIES_FILE);
		String value = localProperty.getProperty(key);
		if (value==null || value.length()==0) {
			value = defaultValue;
			LOGGER.fine("key ("+key+") not found in NameSpace (" +defaultValue+") using default value: "+value);
		}
		return value;
	}
	
	public String toString() {
		return Arrays.asList(new String[] {DEFAULT_NAMESPACE, DEFAULT_JNDI_DB_NAME, DEFAULT_DB_TABLE_NAME, DEFAULT_SQL_SELECT, DEFAULT_PROPERTIES_FILE, DEFAULT_PREFER_PROPERTIES_FILE.toString() }).toString();
	}
}
