package com.raveer.disir.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PropertyKeyManager {
	String defaultValue();
	String propertyKey();	
	
	String nameSpace() default "GLOBAL";
	String jndiDBName() default "java:comp/env/DisirJDBC";
	String dbTableName() default "disir_properties";
	String propertiesFile() default "/disir.properties";
	boolean preferPropertiesFile() default true;
}