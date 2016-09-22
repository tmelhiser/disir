/*
 * This file is part of Disir.
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
package com.raveer.disir.base;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.raveer.disir.annotations.PropertyManagerField;
import com.raveer.disir.annotations.PropertyManagerDefaults;
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
		
		com.raveer.disir.beans.PropertyManager pm = new com.raveer.disir.beans.PropertyManager();
		
		String nameSpace = pm.DEFAULT_NAMESPACE;
		String jndiDBName = pm.getDEFAULT_JNDI_DB_NAME();
		String dbTableName = pm.getDEFAULT_DB_TABLE_NAME();
		String sqlRow = pm.getDEFAULT_SQL_ROW();
		String sqlSelect = pm.getDEFAULT_SQL_SELECT();
		String sqlUpdate = pm.getDEFAULT_SQL_UPDATE();
		String sqlDelete = pm.getDEFAULT_SQL_DELETE();
		String sqlInsert = pm.getDEFAULT_SQL_INSERT();
		String propertiesFile = pm.DEFAULT_PROPERTIES_FILE;
		Boolean preferPropertiesFile = pm.DEFAULT_PREFER_PROPERTIES_FILE;
		LOGGER.finer("Default jndiDBName    : " + jndiDBName);
		LOGGER.finer("Default dbTableName   : " + dbTableName);
		LOGGER.finer("Default sqlRow        : " + sqlRow);
		LOGGER.finer("Default sqlSelect     : " + sqlSelect);
		LOGGER.finer("Default sqlUpdate     : " + sqlUpdate);
		LOGGER.finer("Default sqlDelete     : " + sqlDelete);
		LOGGER.finer("Default sqlInsert     : " + sqlInsert);
		LOGGER.finer("Default nameSpace     : " + nameSpace);
		LOGGER.finer("Default propertiesFile: " + propertiesFile);
		LOGGER.finer("Default preferPropertiesFile: " + preferPropertiesFile);
				
		if (this.getClass().isAnnotationPresent(PropertyManagerDefaults.class)) {
			LOGGER.fine("Injecting Property Annotations at Class: " + className);
			PropertyManagerDefaults propertyManager = this.getClass().getAnnotation(PropertyManagerDefaults.class);
			
			jndiDBName = overRideValue(jndiDBName,propertyManager.jndiDBName());
			dbTableName = overRideValue(dbTableName,propertyManager.dbTableName());
			sqlRow = overRideValue(sqlRow,propertyManager.sqlRow());
			sqlSelect = overRideValue(sqlSelect,propertyManager.sqlSelect());
			sqlUpdate = overRideValue(sqlUpdate,propertyManager.sqlUpdate());
			sqlDelete = overRideValue(sqlDelete,propertyManager.sqlDelete());
			sqlInsert = overRideValue(sqlInsert,propertyManager.sqlInsert());
			
			
			nameSpace = overRideValue(nameSpace,propertyManager.nameSpace());
			propertiesFile = overRideValue(propertiesFile,propertyManager.propertiesFile());
			preferPropertiesFile = overRideValue(preferPropertiesFile,propertyManager.preferPropertiesFile());
			LOGGER.finer("Setting Class defaults jndiDBName    : " + jndiDBName);
			LOGGER.finer("Setting Class defaults dbTableName   : " + dbTableName);
			LOGGER.finer("Setting Class defaults sqlRow        : " + sqlSelect);
			LOGGER.finer("Setting Class defaults sqlSelect     : " + sqlSelect);
			LOGGER.finer("Setting Class defaults sqlUpdate     : " + sqlUpdate);
			LOGGER.finer("Setting Class defaults sqlDelete     : " + sqlDelete);
			LOGGER.finer("Setting Class defaults sqlInsert     : " + sqlInsert);
			LOGGER.finer("Setting Class defaults nameSpace     : " + nameSpace);
			LOGGER.finer("Setting Class defaults propertiesFile: " + propertiesFile);
			LOGGER.finer("Setting Class defaults preferPropertiesFile: " + preferPropertiesFile);
		}
		
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			
			LOGGER.fine("Injecting Property Annotations for Fields: " + className);
			if (field.isAnnotationPresent(PropertyManagerField.class)) {
				
				PropertyManagerField propertyKeyManager = field.getAnnotation(PropertyManagerField.class);
				
				String fieldLevelJndiDBName = overRideValue(jndiDBName, propertyKeyManager.jndiDBName());
				String fieldLeveldbTableName = overRideValue(dbTableName, propertyKeyManager.dbTableName());
				String fieldLevelSqlRow = overRideValue(sqlRow, propertyKeyManager.sqlRow());
				String fieldLevelSqlSelect = overRideValue(sqlSelect, propertyKeyManager.sqlSelect());
				String fieldLevelSqlUpdate = overRideValue(sqlUpdate, propertyKeyManager.sqlUpdate());
				String fieldLevelSqlDelete = overRideValue(sqlDelete, propertyKeyManager.sqlDelete());
				String fieldLevelSqlInsert = overRideValue(sqlInsert, propertyKeyManager.sqlInsert());
				String fieldLevelNameSpace = overRideValue(nameSpace, propertyKeyManager.nameSpace());
				String fieldLevelPropertiesFile = overRideValue(propertiesFile, propertyKeyManager.propertiesFile());
				Boolean fieldLevelPreferPropertiesFile = overRideValue(preferPropertiesFile, propertyKeyManager.preferPropertiesFile());
				LOGGER.finer("Setting Field defaults jndiDBName    : " + fieldLevelJndiDBName);
				LOGGER.finer("Setting Field defaults dbTableName   : " + fieldLeveldbTableName);
				LOGGER.finer("Setting Field defaults sqlRow        : " + fieldLevelSqlRow);
				LOGGER.finer("Setting Field defaults sqlSelect     : " + fieldLevelSqlSelect);
				LOGGER.finer("Setting Field defaults sqlUpdate     : " + fieldLevelSqlUpdate);
				LOGGER.finer("Setting Field defaults sqlDelete     : " + fieldLevelSqlDelete);
				LOGGER.finer("Setting Field defaults sqlInsert     : " + fieldLevelSqlInsert);
				LOGGER.finer("Setting Field defaults nameSpace     : " + fieldLevelNameSpace);
				LOGGER.finer("Setting Field defaults propertiesFile: " + fieldLevelPropertiesFile);
				LOGGER.finer("Setting Field defaults preferPropertiesFile: " + fieldLevelPreferPropertiesFile);
	
				Properties localProperty = getPropertiesSet(fieldLevelJndiDBName,fieldLeveldbTableName,fieldLevelSqlSelect,fieldLevelNameSpace,fieldLevelPropertiesFile,fieldLevelPreferPropertiesFile);
				
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