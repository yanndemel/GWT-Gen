package com.hiperf.common.ui.client.validation;

import java.math.BigDecimal;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class MaxValidator extends BigDecimalParamsAbstractValidator {

	private BigDecimal max;

	public MaxValidator(BigDecimal max) {
		super();
		this.max = max;
	}

	@Override
	public void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException {
		if(value != null && value instanceof String) {
			BigDecimal d = new BigDecimal((String)value);
			if(d.compareTo(max) > 0)
				throw new ValidationException(NakedConstants.messages.less(max));
		}
		else {
			throw new ValidationException(NakedConstants.constants.notNull());
		}
		validatorCallback.onSuccess(null);
	}

}
