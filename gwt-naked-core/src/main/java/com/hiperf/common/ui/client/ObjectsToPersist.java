package com.hiperf.common.ui.client;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.hiperf.common.ui.shared.util.Id;

public class ObjectsToPersist implements IsSerializable {

	private List<INakedObject> insertedObjects;
	private Map<String, Map<Id, Map<String, Serializable>>> updatedObjects;
	private Map<String, Set<Id>> removedObjectsIdsByClassName;
	private Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAddedByClassName;
	private Map<String, Map<Id, Map<String, List<Id>>>> manyToManyRemovedByClassName;

	public ObjectsToPersist() {
		super();
	}

	public ObjectsToPersist(
			List<INakedObject> insertedObjects,
			Map<String, Map<Id, Map<String, Serializable>>> updatedObjects,
			Map<String, Set<Id>> removedObjectsIdsByClassName,
			Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAddedByClassName,
			Map<String, Map<Id, Map<String, List<Id>>>> manyToManyRemovedByClassName) {
		super();
		this.insertedObjects = insertedObjects;
		this.updatedObjects = updatedObjects;
		this.removedObjectsIdsByClassName = removedObjectsIdsByClassName;
		this.manyToManyAddedByClassName = manyToManyAddedByClassName;
		this.manyToManyRemovedByClassName = manyToManyRemovedByClassName;
	}

	public List<INakedObject> getInsertedObjects() {
		return insertedObjects;
	}

	public Map<String, Map<Id, Map<String, Serializable>>> getUpdatedObjects() {
		return updatedObjects;
	}

	public Map<String, Set<Id>> getRemovedObjectsIdsByClassName() {
		return removedObjectsIdsByClassName;
	}

	public Map<String, Map<Id, Map<String, List<Id>>>> getManyToManyAddedByClassName() {
		return manyToManyAddedByClassName;
	}

	public Map<String, Map<Id, Map<String, List<Id>>>> getManyToManyRemovedByClassName() {
		return manyToManyRemovedByClassName;
	}

	public void setInsertedObjects(List<INakedObject> insertedObjects) {
		this.insertedObjects = insertedObjects;
	}

	public void setManyToManyAddedByClassName(
			Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAddedByClassName) {
		this.manyToManyAddedByClassName = manyToManyAddedByClassName;
	}



}
