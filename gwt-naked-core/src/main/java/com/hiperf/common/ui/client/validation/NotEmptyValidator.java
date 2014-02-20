package com.hiperf.common.ui.client.validation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class NotEmptyValidator extends NoParamAbstractValidator {

	@Override
	public void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException {
		if(value == null)
			throw new ValidationException(NakedConstants.constants.notNullColl());
		if(value instanceof IWrapperListModel && ((IWrapperListModel)value).isEmpty())
			throw new ValidationException(NakedConstants.constants.notNullColl());
		if(value instanceof String && ((String)value).isEmpty())
			throw new ValidationException(NakedConstants.constants.notNullColl());
		validatorCallback.onSuccess(null);
	}
}
