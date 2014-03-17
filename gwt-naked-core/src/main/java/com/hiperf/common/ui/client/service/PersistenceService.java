package com.hiperf.common.ui.client.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.model.Filter;
import com.hiperf.common.ui.shared.model.ScreenLabels;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.hiperf.common.ui.shared.util.TableConfig;

@RemoteServiceRelativePath("PersistenceService")
public interface PersistenceService extends RemoteService {

	Map<Id, INakedObject> persist(ObjectsToPersist toPersist) throws PersistenceException;

	NakedObjectsList loadAll(String className, String currentFilter, String sortAttribute, boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist) throws PersistenceException;

	NakedObjectsList loadAll(String className, String currentFilter, String sortAttribute, boolean distinct, boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist) throws PersistenceException;

	NakedObjectsList sort(String nakedObjectName, String currentFilter, String attribute, boolean distinct, boolean asc, int page, int nbRows,
			ObjectsToPersist toPersist) throws PersistenceException;

	List<INakedObject> reload(String nakedObjectName, List<String> idFields,
			List<List<Object>> idList) throws PersistenceException;

	INakedObject get(String nakedObjectName, Id id) throws PersistenceException;

	Collection<INakedObject> getCollection(String className, Id id, String attributeName) throws PersistenceException;

	INakedObject getLinkedObject(String className, Id id, String attributeName) throws PersistenceException;

	Map<String, String> getAll(String rootClassName, String filter, String attPrefix, String childClassName, String childAttribute) throws PersistenceException;

	Long saveFilter(Filter f) throws PersistenceException;

	Map<Long, String> getFilters(String viewName, String className) throws PersistenceException;

	Filter getFilter(Long id) throws PersistenceException;

	NakedObjectsList getSortedCollection(String nakedObjectName, Id id,
			String attribute, String sortAttribute, boolean asc, int page,
			int nbRows, ObjectsToPersist toPersist) throws PersistenceException;

	NakedObjectsList getSortedCollectionInverse(String wrappedClassName, String attribute,
			Id id, String sortAttribute, Boolean asc, int page, int nbRows,
			ObjectsToPersist allObjectsToPersist) throws PersistenceException;

	NakedObjectsList getCollection(String parentClassName, Id id, String attributeName, int page,
			int nbRows, ObjectsToPersist toPersist) throws PersistenceException;

	NakedObjectsList getCollectionInverse(String wrappedClassName, String attribute, Id id, int page,
			int nbRows, ObjectsToPersist toPersist) throws PersistenceException;

	void saveConfiguration(String viewName, String className, int nbRows, List<HeaderInfo> headers,
			ScreenLabels labels, String langCode) throws PersistenceException;

	Map<String, TableConfig> getScreenConfigurations(String langCode) throws PersistenceException;

	String getFileName(String fileClass, String fileNameField, String id) throws PersistenceException;

	String checkExists(String className, String attribute, String value) throws PersistenceException;

	String checkExists(String className, Id id, String attribute, String value) throws PersistenceException;

	CollectionInfo getLazyCollection(String wrappedClassName, Id id, String attribute) throws PersistenceException;

	void removeFilter(Long id) throws PersistenceException;
	
	List<INakedObject> getAll(String rootClassName, String jpqlQuery) throws PersistenceException;

	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static PersistenceServiceAsync instance;
		public static PersistenceServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(PersistenceService.class);
			}
			return instance;
		}
	}


}
