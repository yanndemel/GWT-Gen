package com.hiperf.common.ui.client.service;

import java.util.List;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RandomObjectServiceAsync {
	
	void generateRandomObjects(String className, int size, AsyncCallback<List<INakedObject>> callback);
}
