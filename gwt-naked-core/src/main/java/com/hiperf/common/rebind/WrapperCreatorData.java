package com.hiperf.common.rebind;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.hiperf.common.ui.client.model.ClassInfo;

public class WrapperCreatorData {
	private ClassInfo info;
	private boolean hasCollections = false;
	private boolean hasObjectsCollections = false;
	private boolean hasLinkedObjects = false;
	private boolean hasEnums = false;
	private Set<JMethod> methods = new HashSet<JMethod>();
	private Map<String, AttributeInfo> getters;
	private List<String> attributes;
	private List<Integer> indexes;
	private JClassType classType;
	private boolean entity; 

	public WrapperCreatorData(JClassType classType) {
		this.classType = classType;
	}

	public ClassInfo getInfo() {
		return info;
	}

	public void setInfo(ClassInfo info) {
		this.info = info;
	}

	public boolean isEntity() {
		return entity;
	}

	public void setEntity(boolean entity) {
		this.entity = entity;
	}

	public JClassType getClassType() {
		return classType;
	}

	public boolean isHasObjectsCollections() {
		return hasObjectsCollections;
	}

	public void setHasObjectsCollections(boolean hasObjectsCollections) {
		this.hasObjectsCollections = hasObjectsCollections;
	}

	public boolean isHasCollections() {
		return hasCollections;
	}

	public void setHasCollections(boolean hasCollections) {
		this.hasCollections = hasCollections;
	}

	public boolean isHasLinkedObjects() {
		return hasLinkedObjects;
	}

	public void setHasLinkedObjects(boolean hasLinkedObjects) {
		this.hasLinkedObjects = hasLinkedObjects;
	}
	
	public Set<JMethod> getMethods() {
		return methods;
	}

	public void addMethods(JMethod[] methods) {
		for(JMethod m : methods) {
			String name = m.getName();
			if(name.startsWith("get") || name.startsWith("set") || name.startsWith("is")) {
				boolean toAdd = true;
				for(JMethod mm : this.methods) {
					if(mm.getName().equals(name)) {
						toAdd = false;
						break;
					}
				}
				if(toAdd) {
					this.methods.add(m);			
				}
			}
		}
		
	}

	public Map<String, AttributeInfo> getGetters() {
		return getters;
	}

	public void setGetters(Map<String, AttributeInfo> getters) {
		this.getters = getters;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}

	public List<Integer> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<Integer> indexes) {
		this.indexes = indexes;
	}

	public boolean isHasEnums() {
		return hasEnums;
	}

	public void setHasEnums(boolean hasEnums) {
		this.hasEnums = hasEnums;
	}
}