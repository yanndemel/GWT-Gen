package com.hiperf.common.ui.shared;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.hiperf.common.ui.client.IFieldInfo;

public class WrapperValidationResults implements IsSerializable {

	private String className;
	private Map<String, Set<String>> validationErrorsByAttribute;
	private int nbValidators;
	private int count;
	private String message;
	private boolean validDone = false;

	public WrapperValidationResults() {
	}
	
	public WrapperValidationResults(String className) {
		this(className, true);
	}

	public WrapperValidationResults(String className, boolean client) {
		super();
		this.className = className;
		this.validationErrorsByAttribute = new HashMap<String, Set<String>>();
		this.count = 0;
		this.nbValidators = 0;
		if(client) {
			Map<String, Map<String, IFieldInfo>> fn = WrapperContext.getFieldInfoByName();
			if(fn != null && fn.containsKey(className)) {
				for(IFieldInfo fi : fn.get(className).values()) {
					if(fi.getValidators() != null)
						this.nbValidators += fi.getValidators().size();
				}
			}			
		}
		this.message = null;
	}

	public boolean isValidationDone() {
		return validDone || nbValidators == count;
	}


	public void addValidationError(String attribute, String msg) {
		Set<String> set = validationErrorsByAttribute.get(attribute);
		if(set == null) {
			set = new HashSet<String>();
			validationErrorsByAttribute.put(attribute, set);
		}
		set.add(msg);
		incrementValidationCount();
	}

	public void addValidationErrors(String attribute, Set<String> msgList) {
		Set<String> set = validationErrorsByAttribute.get(attribute);
		if(set == null) {
			set = new HashSet<String>();
			validationErrorsByAttribute.put(attribute, set);
		}
		set.addAll(msgList);
		count = count + msgList.size();
	}

	public void incrementValidationCount() {
		count++;
	}

	public int getNbValidators() {
		return nbValidators;
	}

	public int getCount() {
		return count;
	}

	public Map<String, Set<String>> getValidationErrorsByAttribute() {
		return validationErrorsByAttribute;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public boolean hasErrors() {
		return message != null || !validationErrorsByAttribute.isEmpty();
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setValidationDone(boolean b) {
		validDone  = b;
	}

	public void merge(WrapperValidationResults r) {
		Map<String, Set<String>> err = r.getValidationErrorsByAttribute();
		if(err != null && !err.isEmpty()) {
			for(Entry<String, Set<String>> e : err.entrySet()) {
				Set<String> set = validationErrorsByAttribute.get(e.getKey());
				if(set != null) {
					set.addAll(e.getValue());
				} else
					validationErrorsByAttribute.put(e.getKey(), e.getValue());
			}
		}
		String m = r.getMessage();
		if(m != null && !m.isEmpty())
			message = message != null ? message + "\n" + m : m;
	}


}
