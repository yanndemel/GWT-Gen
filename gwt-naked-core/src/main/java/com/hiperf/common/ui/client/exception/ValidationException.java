package com.hiperf.common.ui.client.exception;

import java.util.Map;
import java.util.Set;

public class ValidationException extends Exception {

	private Map<String,Set<String>> errorsByAttribute;
	
	public ValidationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Map<String,Set<String>> errorsByAttribute) {
		super();
		this.errorsByAttribute = errorsByAttribute;
	}

	public Map<String, Set<String>> getErrorsByAttribute() {
		return errorsByAttribute;
	}

	
	
}
