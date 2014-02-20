package com.hiperf.common.ui.client.validation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class SizeValidator extends IntParamsAbstractValidator {

	private int min;
	private int max;

	public SizeValidator(int min, int max) {
		super();
		this.min = min;
		this.max = max;
	}

	@Override
	public void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException {
		if(value == null && min == 0)
			validatorCallback.onSuccess(null);
		else if(value != null) {
			if(value instanceof String) {
				String s= (String)value;
				if(s.length() < min || s.length() > max)
					throw new ValidationException(NakedConstants.messages.lenBetween(min, max));
			} 
			validatorCallback.onSuccess(null);
		} else {
			throw new ValidationException(NakedConstants.messages.lenBetween(min, max));
		}
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

}
