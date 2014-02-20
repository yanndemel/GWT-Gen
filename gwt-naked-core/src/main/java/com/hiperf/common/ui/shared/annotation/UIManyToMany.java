package com.hiperf.common.ui.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hiperf.common.ui.client.INakedObject;

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
