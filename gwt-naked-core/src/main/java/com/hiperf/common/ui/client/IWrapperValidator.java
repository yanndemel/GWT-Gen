package com.hiperf.common.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.shared.WrapperListValidationResults;
import com.hiperf.common.ui.shared.WrapperValidationResults;

public interface IWrapperValidator {

	void validate(IWrapper w, AsyncCallback<WrapperListValidationResults> tableCallback, AsyncCallback<WrapperValidationResults> formCallback, WrapperListValidationResults wrapperListResults, WrapperValidationResults wrapperResults) throws ValidationException;
	
}
