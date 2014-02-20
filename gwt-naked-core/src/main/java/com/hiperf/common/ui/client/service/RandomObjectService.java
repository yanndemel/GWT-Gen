package com.hiperf.common.ui.client.service;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.exception.RandomObjectException;

@RemoteServiceRelativePath("RandomObjectService")
public interface RandomObjectService extends RemoteService {
	
	List<INakedObject> generateRandomObjects(String className, int size) throws RandomObjectException;
	
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static RandomObjectServiceAsync instance;
		public static RandomObjectServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(RandomObjectService.class);
			}
			return instance;
		}
	}
}
