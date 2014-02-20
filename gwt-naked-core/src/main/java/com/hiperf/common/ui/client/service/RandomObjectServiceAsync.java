package com.hiperf.common.ui.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.INakedObject;

public interface RandomObjectServiceAsync {
	
	void generateRandomObjects(String className, int size, AsyncCallback<List<INakedObject>> callback);
}
