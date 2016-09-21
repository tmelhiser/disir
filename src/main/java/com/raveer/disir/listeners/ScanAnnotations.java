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
package com.raveer.disir.listeners;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.raveer.disir.annotations.PropertyManagerDefaults;
import com.raveer.disir.annotations.PropertyManagerField;
import com.raveer.disir.beans.PropertyManager;

@WebListener
public class ScanAnnotations implements ServletContextListener {

	private static final Logger LOGGER = Logger.getLogger(ScanAnnotations.class.getName());
    public static Map<String, Map<String, Map<String, String>>> DATASOURCES = new HashMap<String,Map<String,Map<String,String>>>(); 

    @Override
    public void contextInitialized(ServletContextEvent event) {
    	LOGGER.info("Starting Annotation Scan");
		PropertyManager pm = new PropertyManager();
		new FastClasspathScanner(new String[] {""}).matchClassesWithAnnotation(PropertyManagerDefaults.class, c-> {
			scanClassForAnnotations(pm,c);
		}).matchClassesWithAnnotation(PropertyManagerField.class, c-> {
			scanClassForAnnotations(pm,c);
		}).scan();
		LOGGER.info("Finished Annotation Scan");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    	LOGGER.info("Shutting Down Annotation Scanner");
    }
    
    private void scanClassForAnnotations(PropertyManager pm, Class<?> c) {
    	PropertyManagerDefaults pmd = c.getAnnotation(PropertyManagerDefaults.class);
		if (pmd != null) {
			String jndiDBName = pmd.jndiDBName().equals("") ? pm.getDEFAULT_JNDI_DB_NAME() : pmd.jndiDBName();
			String dbTableName = pmd.dbTableName().equals("") ? pm.getDEFAULT_DB_TABLE_NAME() : pmd.dbTableName();
			String sqlRow = pmd.sqlRow().equals("") ? pm.getDEFAULT_SQL_ROW() : pmd.sqlRow();
			String sqlSelect = pmd.sqlSelect().equals("") ? pm.getDEFAULT_SQL_SELECT() : pmd.sqlSelect();
			String sqlUpdate = pmd.sqlUpdate().equals("") ? pm.getDEFAULT_SQL_UPDATE() : pmd.sqlUpdate();
			String sqlDelete = pmd.sqlDelete().equals("") ? pm.getDEFAULT_SQL_DELETE() : pmd.sqlDelete();
			String sqlInsert = pmd.sqlInsert().equals("") ? pm.getDEFAULT_SQL_INSERT() : pmd.sqlInsert();
			
			populateMap(c, jndiDBName, dbTableName, sqlRow, sqlSelect, sqlUpdate, sqlDelete, sqlInsert);
		}
		
		for (Field field : c.getDeclaredFields()) {
			for (Annotation annotation : field.getAnnotations()) {
				if (annotation.annotationType().equals(PropertyManagerField.class)) {
					PropertyManagerField pmf = ((PropertyManagerField)annotation);
					
					String jndiDBName = pmf.jndiDBName().equals("") ? pm.getDEFAULT_JNDI_DB_NAME() : pmf.jndiDBName();
					String dbTableName = pmf.dbTableName().equals("") ? pm.getDEFAULT_DB_TABLE_NAME() : pmf.dbTableName();
					String sqlRow = pmf.sqlRow().equals("") ? pm.getDEFAULT_SQL_ROW() : pmf.sqlRow();
					String sqlSelect = pmf.sqlSelect().equals("") ? pm.getDEFAULT_SQL_SELECT() : pmf.sqlSelect();
					String sqlUpdate = pmf.sqlUpdate().equals("") ? pm.getDEFAULT_SQL_UPDATE() : pmf.sqlUpdate();
					String sqlDelete = pmf.sqlDelete().equals("") ? pm.getDEFAULT_SQL_DELETE() : pmf.sqlDelete();
					String sqlInsert = pmf.sqlInsert().equals("") ? pm.getDEFAULT_SQL_INSERT() : pmf.sqlInsert();
					
					populateMap(c, jndiDBName, dbTableName, sqlRow, sqlSelect, sqlUpdate, sqlDelete, sqlInsert);
				}
			}
		}
    }

	private void populateMap(Class<?> c, String jndiDBName, String dbTableName, String sqlRow, String sqlSelect, String sqlUpdate, String sqlDelete, String sqlInsert) {
		Map<String,Map<String,String>> associatedTables = DATASOURCES.get(jndiDBName);
		if (associatedTables == null) {
			associatedTables = new HashMap<String,Map<String,String>>();
			Map<String,String> queries = new HashMap<String,String>();
			queries.put("settingClass", c.getName());
			queries.put("sqlRow",sqlRow);
			queries.put("sqlSelect", sqlSelect);
			queries.put("sqlUpdate", sqlUpdate);
			queries.put("sqlDelete", sqlDelete);
			queries.put("sqlInsert", sqlInsert);
			associatedTables.put(dbTableName, queries);
			DATASOURCES.put(jndiDBName, associatedTables);
		} else {
			Map<String,String> queries = associatedTables.get(dbTableName);
			if (queries == null) {
				queries = new HashMap<String,String>();
				queries.put("settingClass", c.getName());
				queries.put("sqlRow",sqlRow);
				queries.put("sqlSelect", sqlSelect);
				queries.put("sqlUpdate", sqlUpdate);
				queries.put("sqlDelete", sqlDelete);
				queries.put("sqlInsert", sqlInsert);
				associatedTables.put(dbTableName, queries);
			} else {
				if (!queries.get("sqlRow").equals(sqlRow)) {
					logConflict(c, "sqlRow", sqlRow, queries);
				}
				if (!queries.get("sqlSelect").equals(sqlSelect)) {
					logConflict(c, "sqlSelect", sqlSelect, queries);
				}
				if (!queries.get("sqlUpdate").equals(sqlUpdate)) {
					logConflict(c, "sqlUpdate", sqlUpdate, queries);
				}
				if (!queries.get("sqlDelete").equals(sqlDelete)) {
					logConflict(c, "sqlDelete", sqlDelete, queries);
				}
				if (!queries.get("sqlInsert").equals(sqlInsert)) {
					logConflict(c, "sqlInsert", sqlInsert, queries);
				}
			}
			
		}
	}

	private void logConflict(Class<?> c, String type, String currentValue, Map<String, String> queries) {
		LOGGER.severe("Confict in "+ type + ": " + c.getName() + "trying to overide value initially set in " + queries.get("settingClass") + ": initial/used value (" + queries.get(type) + ") - this value (" + currentValue + ")");
	}

}