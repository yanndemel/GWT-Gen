package com.hiperf.common.ui.client.validation;

import java.math.BigDecimal;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class MinValidator extends BigDecimalParamsAbstractValidator {

	private BigDecimal min;

	public MinValidator(BigDecimal min) {
		super();
		this.min = min;
	}

	@Override
	public void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException {
		if(value != null && value instanceof String) {
			BigDecimal d = new BigDecimal((String)value);
			if(d.compareTo(min) < 0)
				throw new ValidationException(NakedConstants.messages.more(min));
		}
		else {
			throw new ValidationException(NakedConstants.constants.notNull());
		}
		validatorCallback.onSuccess(null);
	}

}
