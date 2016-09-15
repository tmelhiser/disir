package com.raveer.disir.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PropertyManager {
	String jndiDBName() default "";
	String dbTableName() default "";
	String sql() default "";
	String nameSpace() default "";
	String propertiesFile() default "";
	String preferPropertiesFile() default "";
	String filesHonorNameSpace() default "";
}