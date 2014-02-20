package com.hiperf.common.ui.client.util;

import java.util.List;
import java.util.Map;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.shared.util.Id;

public class PersistenceResult {

	private List<INakedObject> insertedObjects;
	private Map<Id, INakedObject> result;
	
	public PersistenceResult(List<INakedObject> insertedObjects,
			Map<Id, INakedObject> result) {
		super();
		this.insertedObjects = insertedObjects;
		this.result = result;
	}

	public List<INakedObject> getInsertedObjects() {
		return insertedObjects;
	}

	public Map<Id, INakedObject> getResult() {
		return result;
	}
	
	

}
