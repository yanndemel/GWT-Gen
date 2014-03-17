package org.gwtgen.api.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIManyToMany {

	Class<? extends INakedObject> returnClass();
	
	Class<? extends INakedObject> targetJoinClass();
	
	String targetIdField();
	
	String joinField();
	
	String targetJoinField();
	
	boolean nullable() default true;
}
