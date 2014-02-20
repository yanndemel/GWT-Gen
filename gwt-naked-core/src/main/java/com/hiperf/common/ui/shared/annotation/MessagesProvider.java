package com.hiperf.common.ui.shared.annotation;

public @interface MessagesProvider {
	/**
	 * Name of the static reference to an instance of an extension of {@link com.google.gwt.i18n.client.Messages}
	 * must be a static field or method
	 * */
	String name() default "";
}
