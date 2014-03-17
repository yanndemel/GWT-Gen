package org.gwtgen.api.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIClass {

	String tableLabelKey() default "";
	String formLabelKey() default "";
	String onUpdate() default "";
	boolean editable() default true;
	String rowFormatter() default "";
	String validator() default "";
	boolean importable() default false;
	String importValidator() default "";
	String createForm() default "";
	String onCommit() default "";;

}
