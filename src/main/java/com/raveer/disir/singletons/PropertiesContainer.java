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
package com.raveer.disir.singletons;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.raveer.disir.beans.PropertyManager;
import com.raveer.disir.utils.StringUtils;

public enum PropertiesContainer {
	INSTANCE;
	
	public static final Integer LOAD_FAILED = 0;
	public static final Integer LOAD_FROM_SYSTEM_PATH = 1;
	public static final Integer LOAD_FROM_CLASSPATH = 2;
	public static final Integer LOAD_FROM_DATABASE = 3;
	private final HashMap<String,Properties> propertiesMap = new HashMap<String, Properties>();
	private final HashMap<String,Long> propertiesMapAge = new HashMap<String, Long>();
	private static final Logger LOGGER = Logger.getLogger(PropertiesContainer.class.getName());
	public static String VERSION;
	
	private PropertiesContainer() {
		setVersion();
	}
	
	private void setVersion() {
		String version = this.getClass().getPackage().getImplementationVersion();
		VERSION = version != null ? version : "DEV-ENV";
	}

	public Properties getPropertiesByCoordinate(String jndiDBName, String dbTableName, String sql, String nameSpace, String propertiesFile, Boolean preferPropertiesFile) {
		String propertiesMapKey = StringUtils.join(Arrays.asList(new String[] {jndiDBName, dbTableName, sql, nameSpace, propertiesFile, preferPropertiesFile.toString()}), "###");
		Properties properties = propertiesMap.get(propertiesMapKey);
				
		if (properties==null) {
			synchronized (this) {
				properties = propertiesMap.get(propertiesMapKey);
				if ( properties == null) {
					properties = new Properties();
					int returnedFrom = LOAD_FAILED;
					LOGGER.info("Trying to create Cache Property Coordinate: " + propertiesMapKey);
					
					if (preferPropertiesFile) {
						returnedFrom = newNameSpaceFromFiles(propertiesFile,properties);
						if (properties.isEmpty()) {
							LOGGER.severe("No values loaded from file (" + propertiesFile +"): Trying Database");
							returnedFrom = newNameSpaceFromDB(jndiDBName, dbTableName, sql, nameSpace, properties);
						}
					} else {
						returnedFrom = newNameSpaceFromDB(jndiDBName, dbTableName, sql, nameSpace,properties);
						if (properties.isEmpty()) {
							LOGGER.severe("No values lodaed from Database (" + jndiDBName + ") Table (" + dbTableName + "): Trying files");
							returnedFrom = newNameSpaceFromFiles(propertiesFile,properties);
						}
					}
					if (!properties.isEmpty()) {
						LOGGER.info("Caching Property Coordinate: " + propertiesMapKey);
						if (LOGGER.getLevel() == Level.FINEST) {
							for (Entry<?, ?> entry : properties.entrySet()) {
								if (returnedFrom == LOAD_FROM_SYSTEM_PATH) {
									LOGGER.finest("Properties from Classpath (" + propertiesFile +") for NameSpace ("+propertiesMapKey+"): " + entry.getKey() +"=" + entry.getValue());
								} else if (returnedFrom == LOAD_FROM_CLASSPATH) {
									LOGGER.finest("Properties from File System (" + propertiesFile +") NameSpace ("+propertiesMapKey+"): " + entry.getKey() +"=" + entry.getValue());
								} else if (returnedFrom == LOAD_FROM_DATABASE) {
									LOGGER.finest("Loaded from DB (" + jndiDBName + ") Table ("+dbTableName+") Properties for NameSpace ("+propertiesMapKey+"): " + entry.getKey() +"=" + entry.getValue());
								} else {
									LOGGER.finest("Loaded Properties for Coordinate (" + propertiesMapKey +") : " + entry.getKey() +"=" + entry.getValue());
								}
							}
						}
						propertiesMap.put(propertiesMapKey, properties);
						propertiesMapAge.put(propertiesMapKey, System.currentTimeMillis());
					} else {
						LOGGER.severe("No entries found! NOT Caching: " + propertiesMapKey);
					}
				}
			};
		}
		
		return properties;
	}
	
	public void refreshNameSpace(String refreshNameSpace) {
		LOGGER.info("Trying to refresh coordinates with NameSpace: " + refreshNameSpace);
		for (String nameSpace : propertiesMap.keySet()) {
			if (nameSpace.split("###")[2].equals(refreshNameSpace)) {
				LOGGER.info("Refreshing NameSpace: " + nameSpace);
				propertiesMap.remove(nameSpace);
			}
		}
	}

	private Integer newNameSpaceFromDB(String jndiDBName, String dbTableName, String sql, String nameSpace, Properties targetProperties) {
		LOGGER.info("Trying to load NameSpace ("+nameSpace+") from Database DB ("+jndiDBName+") Table: " + dbTableName);
		try {
			Context context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup(jndiDBName);
			try (Connection connection = dataSource.getConnection()) {
				try(PreparedStatement prep = connection.prepareStatement(String.format(sql,dbTableName))) {
					prep.setString(1, nameSpace);
					
					try(ResultSet rs = prep.executeQuery()) {
						while(rs.next()) {
							String key = rs.getString("key");
							String value = rs.getString("value");
							targetProperties.put(key, value);
						}
					}
				}
				LOGGER.info("Loaded from NameSpace ("+nameSpace+") DB ("+jndiDBName+") Table ("+ dbTableName +"): " + targetProperties.size());
			} catch (SQLException e) {
				LOGGER.severe("Error connecting to DataBase: " + jndiDBName);
				LOGGER.severe("Database Stack Trace: " + e);
			}
		} catch (NamingException e) {
			LOGGER.severe("Error connecting to DataBase: " + jndiDBName);
			LOGGER.severe("Database Stack Trace: " + e);
		}
		return LOAD_FROM_DATABASE;
	}

	private Integer newNameSpaceFromFiles(String propertiesFile, Properties targetProperties) {
		LOGGER.info("Trying to load from Properties File: " + propertiesFile);
		try (InputStream stream1 = new FileInputStream(propertiesFile)) {
			targetProperties.load(stream1);
			LOGGER.info("Loaded from filesystem (" + propertiesFile +"): " + targetProperties.size());
			return LOAD_FROM_SYSTEM_PATH;
		} catch (Exception e1) {
			LOGGER.severe("Unable to find Properties File on filesystem, trying classpath: " + e1);
			try (InputStream stream2 = this.getClass().getResourceAsStream(propertiesFile)) {
				targetProperties.load(stream2);
				LOGGER.info("Loaded from classpath (" + propertiesFile +"): " + targetProperties.size());
				return LOAD_FROM_CLASSPATH;
			} catch (Exception e2) {
				LOGGER.severe("No properties loaded from file: " + e2);
			}
		}
		return LOAD_FAILED;
	}

	public Runnable refreshNameSpaces() {	
		return new Runnable() {
	         public void run() {
	        	 LOGGER.fine("Scanning Disir Cache");
	        	 for (Entry<String, Long> entry : propertiesMapAge.entrySet()) {
	     			if ((System.currentTimeMillis() - entry.getValue())>PropertyManager.DEFAULT_CACHE_EXPIRES) {
	     				LOGGER.info("Refreshing Coordinate: " + entry.getKey());
	     				propertiesMapAge.remove(entry.getKey());
	     				propertiesMap.remove(entry.getKey());
	     			}
	     		}
	         }
	     };  	
	}
}
