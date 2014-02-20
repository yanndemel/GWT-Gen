package com.hiperf.common.ui.server.storage;



public abstract class AbstractPersistenceHelper implements IPersistenceHelper {

	protected static IPersistenceHelper instance = null;

	public static IPersistenceHelper getInstance() {
		return instance;
	}


}
