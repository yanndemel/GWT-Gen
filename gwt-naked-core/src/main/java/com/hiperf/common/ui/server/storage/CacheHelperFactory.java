package com.hiperf.common.ui.server.storage;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.hiperf.common.ui.client.exception.PersistenceException;

public class CacheHelperFactory {

	private static final Logger logger = Logger.getLogger(CacheHelperFactory.class
			.getName());
	
	private static final String DEFAULT_HELPER_IMPL = "com.hiperf.gwtgen.gwtgen.coherence.CoherenceCacheHelper";
	
	public static ICoherenceCacheHelper getDefaultCacheHelper() throws PersistenceException {
		try {
			return (ICoherenceCacheHelper) Class.forName(DEFAULT_HELPER_IMPL).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Exception while loading default cache helper", e);
			throw new PersistenceException(e);
		}
	}
	
}
