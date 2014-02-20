package com.hiperf.common.ui.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WrapperListValidationResults implements IsSerializable {

	private Map<String, WrapperValidationResults> validationErrorsByClassName;

	public WrapperListValidationResults() {
		super();
		this.validationErrorsByClassName = new HashMap<String, WrapperValidationResults>();
	}

	public void merge(WrapperListValidationResults l) {
		Map<String, WrapperValidationResults> vc = l.getValidationErrorsByClassName();
		if(vc != null &&  !vc.isEmpty()) {
			for(Entry<String, WrapperValidationResults> e : vc.entrySet()) {
				WrapperValidationResults r = validationErrorsByClassName.get(e.getKey());
				if(r != null) {
					r.merge(e.getValue());
				} else {
					validationErrorsByClassName.put(e.getKey(), e.getValue());
				}
			}
		}
	}
	
	public boolean hasErrors() {
		for(WrapperValidationResults r : validationErrorsByClassName.values()) {
			if(r.hasErrors())
				return true;
		}
		return false;
	}

	public boolean isValidationDone() {
		for(WrapperValidationResults r : validationErrorsByClassName.values()) {
			if(!r.isValidationDone())
				return false;
		}
		return true;
	}

	public void addEmptyWrapperValidationResults(String className, WrapperValidationResults r) {
		validationErrorsByClassName.put(className, r);
	}

	public void addWrapperValidationResults(String className, WrapperValidationResults r, boolean client) {		
		WrapperValidationResults res = validationErrorsByClassName.get(className);
		if(res == null) {
			res = new WrapperValidationResults(className, client);
			validationErrorsByClassName.put(className, res);
		}
		for(Entry<String, Set<String>> e : r.getValidationErrorsByAttribute().entrySet()) {
			String att = e.getKey();
			if(e.getValue() != null && !e.getValue().isEmpty()) {
				res.addValidationErrors(att, e.getValue());
			}
		}
	}
	
	public void addWrapperValidationResults(String className, WrapperValidationResults r) {		
		addWrapperValidationResults(className, r, true);
	}

	public Map<String, WrapperValidationResults> getValidationErrorsByClassName() {
		return validationErrorsByClassName;
	}



}
