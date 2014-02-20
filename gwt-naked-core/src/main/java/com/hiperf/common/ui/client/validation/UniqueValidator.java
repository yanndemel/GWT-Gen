package com.hiperf.common.ui.client.validation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.StringParamsAbstractValidator;
import com.hiperf.common.ui.shared.util.Id;

public class UniqueValidator extends StringParamsAbstractValidator {

	private String className;
	private String attribute;

	public UniqueValidator(String className, String attr) {
		this.className = className;
		this.attribute = attr;
	}

	@Override
	public void validate(final Object value, final AsyncCallback<String> validatorCallback) throws ValidationException {
		if(value == null)
			validatorCallback.onSuccess(null);
		else {
			PersistenceServiceAsync srv = PersistenceService.Util.getInstance();
			srv.checkExists(className, attribute, value.toString(), validatorCallback);
		}
	}

	public void validate(IWrapper w, final Object value,
			final AsyncCallback<String> validatorCallback) throws ValidationException {
		Id id = PersistenceManager.getId(w);
		if(id.isLocal()) {
			validate(value, validatorCallback);
		} else if(value != null) {
			PersistenceServiceAsync srv = PersistenceService.Util.getInstance();
			srv.checkExists(className, id, attribute, value.toString(), validatorCallback);
		} else {
			validatorCallback.onSuccess(null);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((className == null) ? 0 : className.hashCode());
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
		UniqueValidator other = (UniqueValidator) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		return true;
	}
	
	

}
