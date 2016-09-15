package com.raveer.disir.base;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.raveer.disir.annotations.PropertyKeyManager;
import com.raveer.disir.annotations.PropertyManager;
import com.raveer.disir.singletons.PropertiesContainer;

public abstract class PropertyManagerBase implements Serializable {
	
	private static final long serialVersionUID = 15L;
	public static Logger LOGGER = null;
	private boolean isInitialized = false;
	
	{
		if (!isDisirInitialized()) {
			initializeDisir();
		}
	}

	private void initializeDisir() {
		String className = this.getClass().getName();
		LOGGER = Logger.getLogger(className);
		
		com.raveer.disir.PropertyManager pm = new com.raveer.disir.PropertyManager();
		
		String nameSpace = pm.DEFAULT_NAMESPACE;
		String jndiDBName = pm.DEFAULT_JNDI_DB_NAME;
		String dbTableName = pm.DEFAULT_DB_TABLE_NAME;
		String sql = pm.DEFAULT_SQL;
		String propertiesFile = pm.DEFAULT_PROPERTIES_FILE;
		Boolean preferPropertiesFile = pm.DEFAULT_PREFER_PROPERTIES_FILE;
		LOGGER.finer("Default jndiDBName    : " + jndiDBName);
		LOGGER.finer("Default dbTableName   : " + dbTableName);
		LOGGER.finer("Default sql           : " + sql);
		LOGGER.finer("Default nameSpace     : " + nameSpace);
		LOGGER.finer("Default propertiesFile: " + propertiesFile);
		LOGGER.finer("Default preferPropertiesFile: " + preferPropertiesFile);
				
		if (this.getClass().isAnnotationPresent(PropertyManager.class)) {
			LOGGER.fine("Injecting Property Annotations at Class: " + className);
			PropertyManager propertyManager = this.getClass().getAnnotation(PropertyManager.class);
			
			jndiDBName = overRideValue(jndiDBName,propertyManager.jndiDBName());
			dbTableName = overRideValue(dbTableName,propertyManager.dbTableName());
			sql = overRideValue(sql,propertyManager.sql());
			nameSpace = overRideValue(nameSpace,propertyManager.nameSpace());
			propertiesFile = overRideValue(propertiesFile,propertyManager.propertiesFile());
			preferPropertiesFile = overRideValue(preferPropertiesFile,propertyManager.preferPropertiesFile());
			LOGGER.finer("Setting Class defaults jndiDBName    : " + jndiDBName);
			LOGGER.finer("Setting Class defaults dbTableName   : " + dbTableName);
			LOGGER.finer("Setting Class defaults sql           : " + sql);
			LOGGER.finer("Setting Class defaults nameSpace     : " + nameSpace);
			LOGGER.finer("Setting Class defaults propertiesFile: " + propertiesFile);
			LOGGER.finer("Setting Class defaults preferPropertiesFile: " + preferPropertiesFile);
		}
		
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			
			LOGGER.fine("Injecting Property Annotations for Fields: " + className);
			if (field.isAnnotationPresent(PropertyKeyManager.class)) {
				
				PropertyKeyManager propertyKeyManager = field.getAnnotation(PropertyKeyManager.class);
				
				String fieldLevelJndiDBName = overRideValue(jndiDBName, propertyKeyManager.jndiDBName());
				String fieldLeveldbTableName = overRideValue(dbTableName, propertyKeyManager.dbTableName());
				String fieldLevelSql = overRideValue(sql, propertyKeyManager.sql());
				String fieldLevelNameSpace = overRideValue(nameSpace, propertyKeyManager.nameSpace());
				String fieldLevelPropertiesFile = overRideValue(propertiesFile, propertyKeyManager.propertiesFile());
				Boolean fieldLevelPreferPropertiesFile = overRideValue(preferPropertiesFile, propertyKeyManager.preferPropertiesFile());
				LOGGER.finer("Setting Field defaults jndiDBName    : " + fieldLevelJndiDBName);
				LOGGER.finer("Setting Field defaults dbTableName   : " + fieldLeveldbTableName);
				LOGGER.finer("Setting Field defaults sql           : " + fieldLevelSql);
				LOGGER.finer("Setting Field defaults nameSpace     : " + fieldLevelNameSpace);
				LOGGER.finer("Setting Field defaults propertiesFile: " + fieldLevelPropertiesFile);
				LOGGER.finer("Setting Field defaults preferPropertiesFile: " + fieldLevelPreferPropertiesFile);
	
				Properties localProperty = getPropertiesSet(fieldLevelJndiDBName,fieldLeveldbTableName,fieldLevelSql,fieldLevelNameSpace,fieldLevelPropertiesFile,fieldLevelPreferPropertiesFile);
				
				String value = localProperty.getProperty(propertyKeyManager.propertyKey());
				if (value==null || value.length()==0) {
					value = propertyKeyManager.defaultValue();
					LOGGER.fine("key ("+propertyKeyManager.propertyKey()+") not found in NameSpace (" + fieldLevelNameSpace+") using default value: "+value);
				}
				if(!"".equals(value)) {
					try {
						LOGGER.finer("Injecting value for field: " + field.getName());
						field.setAccessible(true);
						field.set(this, value);
						LOGGER.finer("Injected " + field.getName() + ": " + value);
					} catch (IllegalAccessException | IllegalArgumentException e) {
						LOGGER.severe("Error injecting field("+field.getName()+"): " + e);
					}
				}
			}
		}
				
		isInitialized=true;
	}
	
	private String overRideValue(String currentValue, String newValue) {
		if (newValue != null && !newValue.equals("")) {
			currentValue = newValue;
		}
		return currentValue;
	}
	
	private Boolean overRideValue(Boolean currentValue, String newValue) {
		if (newValue != null && !newValue.equals("")) {
			currentValue = Boolean.valueOf(newValue);
		}
		return currentValue;
	}
	
	private Properties getPropertiesSet(String jndiDBName, String dbTableName, String sql, String nameSpace, String propertiesFile, Boolean preferPropertiesFile) {
		LOGGER.fine("Properties Coordinates: " + Arrays.toString(new String[] {jndiDBName, dbTableName, nameSpace, propertiesFile, preferPropertiesFile.toString()}));
		return PropertiesContainer.INSTANCE.getPropertiesByCoordinate(jndiDBName, dbTableName, sql, nameSpace, propertiesFile, preferPropertiesFile);
	}

	private boolean isDisirInitialized() {
		return isInitialized;
	}

}