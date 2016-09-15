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
	
	String nameSpace() default "";
	String jndiDBName() default "";
	String dbTableName() default "";
	String sql() default "";
	String propertiesFile() default "";
	String preferPropertiesFile() default "";
	String filesHonorNameSpace() default "";
}