package com.hiperf.common.ui.client.validation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;

public class AssertTrueValidator extends NoParamAbstractValidator {


	@Override
	public void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException {
		if(value == null)
			throw new ValidationException("Value has to be true");
		if(value instanceof String && !Boolean.parseBoolean((String)value))
			throw new ValidationException("Value has to be true");
		else if(value instanceof Boolean && !((Boolean)value).booleanValue())
			throw new ValidationException("Value has to be true");
		validatorCallback.onSuccess(null);

	}

}
