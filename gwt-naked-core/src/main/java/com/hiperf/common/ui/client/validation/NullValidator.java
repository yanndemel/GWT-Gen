package com.hiperf.common.ui.client.validation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;

public class NullValidator extends NoParamAbstractValidator {

	@Override
	public void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException {
		if(value != null) {
			if(value instanceof String && ((String)value).length() == 0) {
				validatorCallback.onSuccess(null);
				return;
			}
			throw new ValidationException("Value must be null");
		}
		validatorCallback.onSuccess(null);
	}

}
