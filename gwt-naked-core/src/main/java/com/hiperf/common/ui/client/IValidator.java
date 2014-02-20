package com.hiperf.common.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.exception.ValidationException;

/**
 * interface for Validators, based on <b>javax.validation annotations</b>
 *
 *  <p>
 * <b>example</b> :
 * public class A implements INakedObject {<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;@NotNull<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;Long getMyAttribute() {...}  //the {@link com.hiperf.common.ui.client.validation.NotNullValidator} will be used<br>
 * }
 *
 * */
public interface IValidator {
	void validate(Object value, AsyncCallback<String> validatorCallback) throws ValidationException;
}
