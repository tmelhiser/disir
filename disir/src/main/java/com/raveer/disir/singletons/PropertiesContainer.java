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

import com.raveer.disir.PropertyManager;
import com.raveer.disir.utils.StringUtils;

public enum PropertiesContainer {
	INSTANCE;
	
	private final HashMap<String,Properties> propertiesMap = new HashMap<String, Properties>();
	private final HashMap<String,Long> propertiesMapAge = new HashMap<String, Long>();
	private static final Logger LOGGER = Logger.getLogger(PropertiesContainer.class.getName());
	
	
	private PropertiesContainer() {
	}

	public Properties getPropertiesByCoordinate(String jndiDBName, String dbTableName, String sql, String nameSpace, String propertiesFile, Boolean preferPropertiesFile) {
		String propertiesMapKey = StringUtils.join(Arrays.asList(new String[] {jndiDBName, dbTableName, sql, nameSpace, propertiesFile, preferPropertiesFile.toString()}), "###");
		Properties targetProperties = propertiesMap.get(propertiesMapKey); 
				
		if (targetProperties==null) {
			synchronized (this) {
				targetProperties = propertiesMap.get(propertiesMapKey);
				if ( targetProperties == null) {
					LOGGER.info("Trying to create Cache Property Coordinate: " + propertiesMapKey);
					targetProperties = new Properties();
					if (preferPropertiesFile) {
						newNameSpaceFromFiles(propertiesFile,targetProperties);
						if (targetProperties.isEmpty()) {
							LOGGER.severe("No values loaded from file (" + propertiesFile +"): Trying Database");
							newNameSpaceFromDB(jndiDBName, dbTableName, sql, nameSpace,targetProperties);
						}
					} else {
						newNameSpaceFromDB(jndiDBName, dbTableName, sql, nameSpace,targetProperties);
						if (targetProperties.isEmpty()) {
							LOGGER.severe("No values lodaed from Database (" + jndiDBName + ") Table (" + dbTableName + "): Trying files");
							newNameSpaceFromFiles(propertiesFile,targetProperties);
						}
					}
					if (!targetProperties.isEmpty()) {
						LOGGER.info("Caching Property Coordinate: " + propertiesMapKey);
						if (LOGGER.getLevel() == Level.FINEST) {
							for (Entry<?, ?> entry : targetProperties.entrySet()) {
								LOGGER.finest("Properties for NameSpace ("+propertiesMapKey+"): " + entry.getKey() +"=" + entry.getValue());
							}
						}
						propertiesMap.put(propertiesMapKey, targetProperties);
						propertiesMapAge.put(propertiesMapKey, System.currentTimeMillis());
					} else {
						LOGGER.severe("No entries found! NOT Caching: " + propertiesMapKey);
					}
				}
			};
		}
		
		return targetProperties;
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

	private void newNameSpaceFromDB(String jndiDBName, String dbTableName, String sql, String nameSpace, Properties targetProperties) {
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
	}

	private void newNameSpaceFromFiles(String propertiesFile, Properties targetProperties) {
		LOGGER.info("Trying to load from Properties File: " + propertiesFile);
		try (InputStream stream1 = new FileInputStream(propertiesFile)) {
			targetProperties.load(stream1);
			LOGGER.info("Loaded from filesystem (" + propertiesFile +"): " + targetProperties.size());
		} catch (Exception e1) {
			LOGGER.severe("Unable to find Properties File on filesystem, trying classpath: " + e1);
			try (InputStream stream2 = this.getClass().getResourceAsStream(propertiesFile)) {
				targetProperties.load(stream2);
				LOGGER.info("Loaded from classpath (" + propertiesFile +"): " + targetProperties.size());
			} catch (Exception e2) {
				LOGGER.severe("No properties loaded from file: " + e2);
			}
		}
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
