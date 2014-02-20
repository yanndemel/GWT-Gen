package com.hiperf.common.ui.client.validation;

import com.hiperf.common.ui.client.IValidator;

public abstract class NoParamAbstractValidator implements IValidator {

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return getClass().getName().equals(obj.getClass().getName());		
	}
		
	public int hashcode() {
		return super.hashCode();
	}
	
}
