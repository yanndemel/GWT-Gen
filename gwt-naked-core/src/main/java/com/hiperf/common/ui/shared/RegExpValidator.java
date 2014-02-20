package com.hiperf.common.ui.shared;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class RegExpValidator extends StringParamsAbstractValidator {

	private JavaScriptObject regExp;

	private String pattern;

	public RegExpValidator(String regExp) {
		super();
		this.pattern = regExp;
		this.regExp = regexp(regExp);
	}

	private native JavaScriptObject regexp(String s)/*-{
	   return new RegExp(s);
	}-*/;

	public native boolean test(String s)/*-{
	   var regExp = this.@com.hiperf.common.ui.shared.RegExpValidator::regExp;
	   return regExp.test(s);
	}-*/;

	@Override
	public void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException {
		//GWT.log("validate with "+value);
		if(value != null && !test((String)value)) {
			throw new ValidationException(NakedConstants.messages.noMatch(value));
		}
		validatorCallback.onSuccess(null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegExpValidator other = (RegExpValidator) obj;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		return true;
	}


}
