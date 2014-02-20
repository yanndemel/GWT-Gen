package com.hiperf.common.ui.client.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiperf.common.ui.client.IValidator;
import com.hiperf.common.ui.client.IWrapperValidator;

/**
 * @author 492959
 * 
 */
public class ValidatorFactory {

	private static final Map<String, List<IValidator>> VALIDATORS = new HashMap<String, List<IValidator>>();
	private static final Map<String, IWrapperValidator> WRAPPER_VALIDATORS = new HashMap<String, IWrapperValidator>();

	public static Map<String, IWrapperValidator> getWrapperValidators() {
		return WRAPPER_VALIDATORS;
	}

	public static Map<String, List<IValidator>> getValidators() {
		return VALIDATORS;
	}

}
