package com.hiperf.common.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class WrapperGenerator extends Generator {

	/**
	 * Called during deferred binding sequence
	 * 
	 * @return the class name
	 */
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		WrapperCreator binder = new WrapperCreator(logger, context, typeName);
		String className = binder.createWrapper();
		return className;
	}

}
