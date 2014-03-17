package org.gwtgen.api.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * used to validate a field / method on an object (The validator {@link com.hiperf.common.ui.client.validation.UniqueValidator} will be used)
 * */
@Target(value = {ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {

}
