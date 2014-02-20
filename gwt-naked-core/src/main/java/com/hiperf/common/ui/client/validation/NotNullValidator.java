package com.hiperf.common.ui.client.validation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class NotNullValidator extends NoParamAbstractValidator {

	@Override
	public void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException {
		if(value == null)
			throw new ValidationException(NakedConstants.constants.notNull());
		if(value instanceof String && ((String)value).length() == 0)
			throw new ValidationException(NakedConstants.constants.notNull());
		validatorCallback.onSuccess(null);
	}
}
