package com.raveer.disir;


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.raveer.disir.annotations.PropertyKeyManager;
import com.raveer.disir.annotations.PropertyManager;
import com.raveer.disir.singletons.PropertiesContainer;

public abstract class PropertyManagerBase {
	
	public static Logger LOGGER = null;
	private boolean isInitialized = false;
	
	private static final String NAME_SPACE_DEFAULT = "GLOBAL";
	private static final String JNDI_DB_NAME = "java:comp/env/DisirJDBC";
	private static final String DB_TABLE_NAME = "disir_properties";
	private static final String PROPERTIES_FILE = "/disir.properties";
	private static final Boolean PREFER_PROPERTIES_FILE = true;
	
	private String jndiDBName = JNDI_DB_NAME;
	private String dbTableName = DB_TABLE_NAME;
	private String nameSpace = NAME_SPACE_DEFAULT;
	private String propertiesFile = PROPERTIES_FILE;
	private Boolean preferPropertiesFile = PREFER_PROPERTIES_FILE;
	
	{
		if (!isDisirInitialized()) {
			initializeDisir();
		}
	}

	private void initializeDisir() {
		String className = this.getClass().getName();
		LOGGER = Logger.getLogger(className);
		
		if (this.getClass().isAnnotationPresent(PropertyManager.class)) {
			LOGGER.fine("Injecting Property Annotations at Class: " + className);
			PropertyManager propertyManager = this.getClass().getAnnotation(PropertyManager.class);
			jndiDBName=propertyManager.jndiDBName();
			dbTableName=propertyManager.dbTableName();
			nameSpace=propertyManager.nameSpace();
			propertiesFile=propertyManager.propertiesFile();
			preferPropertiesFile=propertyManager.preferPropertiesFile();
			LOGGER.finer("Setting jndiDBName    : " + jndiDBName);
			LOGGER.finer("Setting dbTableName   : " + dbTableName);
			LOGGER.finer("Setting nameSpace     : " + nameSpace);
			LOGGER.finer("Setting propertiesFile: " + propertiesFile);
			LOGGER.fine("Setting preferPropertiesFile: " + preferPropertiesFile);
		}
		
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			
			LOGGER.fine("Injecting Property Annotations for Fields: " + className);
			if (field.isAnnotationPresent(PropertyKeyManager.class)) {
				
				PropertyKeyManager propertyKeyManager = field.getAnnotation(PropertyKeyManager.class);
				
				String fieldLevelJndiDBName = getOverRideValue(JNDI_DB_NAME, jndiDBName, propertyKeyManager.jndiDBName());
				String fieldLeveldbTableName = getOverRideValue(DB_TABLE_NAME, dbTableName, propertyKeyManager.dbTableName());
				String fieldLevelNameSpace = getOverRideValue(NAME_SPACE_DEFAULT, nameSpace, propertyKeyManager.nameSpace());
				String fieldLevelPropertiesFile = getOverRideValue(PROPERTIES_FILE, propertiesFile, propertyKeyManager.propertiesFile());
				Boolean fieldLevelPreferPropertiesFile = getOverRideValue(PREFER_PROPERTIES_FILE, preferPropertiesFile, propertyKeyManager.preferPropertiesFile());
	
				Properties localProperty = getPropertiesSet(fieldLevelJndiDBName,fieldLeveldbTableName,fieldLevelNameSpace,fieldLevelPropertiesFile,fieldLevelPreferPropertiesFile);
				
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
	
	private String getOverRideValue(String defaultValue, String topValue, String localValue) {
		String returnValue = topValue;
		if (defaultValue.compareToIgnoreCase(localValue)!=0 && topValue.compareToIgnoreCase(localValue) !=0) {
			returnValue = localValue;
		}
		return returnValue;
	}
	
	private Boolean getOverRideValue(Boolean defaultValue, Boolean topValue, Boolean localValue) {
		Boolean returnValue = topValue;
		if (defaultValue!=localValue && topValue!=localValue) {
			returnValue = localValue;
		}
		return returnValue;
	}
	
	private Properties getPropertiesSet(String jndiDBName, String dbTableName, String nameSpace, String propertiesFile, Boolean preferPropertiesFile) {
		LOGGER.fine("Properties Coordinates: " + Arrays.toString(new String[] {jndiDBName, dbTableName, nameSpace, propertiesFile, preferPropertiesFile.toString()}));
		return PropertiesContainer.INSTANCE.getPropertiesByCoordinate(jndiDBName, dbTableName, nameSpace, propertiesFile, preferPropertiesFile);
	}

	private boolean isDisirInitialized() {
		return isInitialized;
	}

}