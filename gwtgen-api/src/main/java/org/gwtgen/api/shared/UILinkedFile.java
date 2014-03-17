package org.gwtgen.api.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UILinkedFile {
	boolean download() default true;
	boolean upload() default false;
	String fileClassName();	
	String fileStorageFieldName();
	String fileFieldName();
	String localKeyField();
}
